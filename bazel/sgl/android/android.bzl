load("//bazel/sgl:assets.bzl", "SglAssetsInfo")


def _android_resource_name(name):
    normalized = ""
    lowercase = name.lower()
    for index in range(len(lowercase)):
        char = lowercase[index]
        normalized += char if char in "abcdefghijklmnopqrstuvwxyz0123456789_" else "_"
    if not normalized or normalized[0] not in "abcdefghijklmnopqrstuvwxyz":
        normalized = "asset_" + normalized
    return normalized


def _label_and_jar(package_name, label):
    if label.startswith(":"):
        name = label[1:]
        full_label = "//%s:%s" % (package_name, name) if package_name else "//:%s" % name
    else:
        name = label.split(":")[-1]
        full_label = label

    jar = "bazel-bin/%s/%s.jar" % (package_name, name) if package_name else "bazel-bin/%s.jar" % name
    return full_label, jar


def _system_bars_mode(value):
    return {
        "safe-area": "AndroidSystemBarsMode.SafeArea",
        "edge-to-edge": "AndroidSystemBarsMode.EdgeToEdge",
        "immersive": "AndroidSystemBarsMode.Immersive",
    }[value]


def _system_bars_behavior(value):
    return {
        "default": "AndroidSystemBarsBehavior.Default",
        "transient-by-swipe": "AndroidSystemBarsBehavior.TransientBySwipe",
    }[value]


def _expand(ctx, template, output, substitutions):
    ctx.actions.expand_template(
        template = template,
        output = output,
        substitutions = substitutions,
    )


def _android_runner_impl(ctx):
    package_path = ctx.attr.package.replace(".", "/")
    android_rule_dir_suffix = "/bazel/sgl/android"
    android_rule_dir = ctx.file._runner_template.dirname
    if android_rule_dir == android_rule_dir_suffix.lstrip("/"):
        inferred_android_root = "."
    elif android_rule_dir.endswith(android_rule_dir_suffix):
        inferred_android_root = android_rule_dir[:-len(android_rule_dir_suffix)]
    else:
        fail("Unexpected SGL Android rule path: %s" % android_rule_dir)
    sgl_android_repo_root = ctx.attr.sgl_android_root or inferred_android_root
    project_sgl_android_root = "@SGL_ANDROID_ROOT@"
    game_labels = []
    game_jars = []
    for label in [ctx.attr.core] + ctx.attr.extra_jars:
        full_label, jar = _label_and_jar(ctx.label.package, label)
        game_labels.append(full_label)
        game_jars.append(jar)

    project_template_dir = ctx.label.name + "_project"
    package_fragment = ctx.label.package if ctx.label.package else "root"
    project_dir = ".bazel-android/%s/%s" % (package_fragment, ctx.attr.project_name)
    launch_cmd = "adb shell am start -n %s/.MainActivity" % ctx.attr.package if ctx.attr.launch else "true"
    wiring_expression = ctx.attr.wiring_expression or "%s.core.Wiring.wire(platformProxy)" % ctx.attr.package

    jar_dependencies = "\n".join([
        "    implementation(files(\"@WORKSPACE@/%s\"))" % jar
        for jar in game_jars
    ])

    application_icon = "        android:icon=\"%s\"\n" % ctx.attr.launcher_icon if ctx.attr.launcher_icon else ""
    firebase_enabled = bool(ctx.attr.google_services_json)
    admob_enabled = bool(ctx.attr.admob_application_id)
    play_games_enabled = bool(ctx.attr.play_games_project_id)

    optional_modules = []
    optional_dependencies = []
    optional_imports = []
    optional_res_values = []
    if firebase_enabled:
        optional_modules.append("include(\":sgl-android-firebase\")\nproject(\":sgl-android-firebase\").projectDir = file(\"%s/android-kotlin/firebase\")" % project_sgl_android_root)
        optional_dependencies.append("    implementation(project(\":sgl-android-firebase\"))")
        optional_imports.append("import sgl.android.analytics.AndroidFirebaseAnalytics\n")
    if admob_enabled:
        optional_modules.append("include(\":sgl-android-admob\")\nproject(\":sgl-android-admob\").projectDir = file(\"%s/android-kotlin/admob\")" % project_sgl_android_root)
        optional_dependencies.append("    implementation(project(\":sgl-android-admob\"))")
        optional_imports.append("import sgl.android.ads.AndroidAdMobAds\n")
        optional_res_values.append("        resValue(\"string\", \"sgl_admob_interstitial_ad_unit_id\", \"%s\")" % ctx.attr.admob_interstitial_ad_unit_id)
        optional_res_values.append("        resValue(\"string\", \"sgl_admob_rewarded_ad_unit_id\", \"%s\")" % ctx.attr.admob_rewarded_ad_unit_id)
        optional_res_values.append("        resValue(\"bool\", \"sgl_admob_always_preload\", \"%s\")" % ("true" if ctx.attr.admob_always_preload else "false"))
    if play_games_enabled:
        optional_modules.append("include(\":sgl-android-play-games\")\nproject(\":sgl-android-play-games\").projectDir = file(\"%s/android-kotlin/play-games\")" % project_sgl_android_root)
        optional_dependencies.append("    implementation(project(\":sgl-android-play-games\"))")
        optional_imports.append("import sgl.android.play.AndroidPlayGamesServices\n")

    admob_application_id_metadata = "        <meta-data\n            android:name=\"com.google.android.gms.ads.APPLICATION_ID\"\n            android:value=\"%s\" />\n" % ctx.attr.admob_application_id if admob_enabled else ""
    play_games_metadata = "        <meta-data\n            android:name=\"com.google.android.gms.games.APP_ID\"\n            android:value=\"@string/play_games_project_id\" />\n" if play_games_enabled else ""
    if play_games_enabled:
        optional_res_values.append("        resValue(\"string\", \"play_games_project_id\", \"%s\")" % ctx.attr.play_games_project_id)

    substitutions = {
        "@PACKAGE@": ctx.attr.package,
        "@LABEL@": ctx.attr.label,
        "@APPLICATION_ICON@": application_icon,
        "@JAR_DEPENDENCIES@": jar_dependencies,
        "@WIRING_EXPRESSION@": wiring_expression,
        "@OPTIONAL_MODULES@": "\n".join(optional_modules),
        "@OPTIONAL_DEPENDENCIES@": "\n".join(optional_dependencies),
        "@OPTIONAL_IMPORTS@": "".join(optional_imports),
        "@ADMOB_APPLICATION_ID_METADATA@": admob_application_id_metadata,
        "@PLAY_GAMES_METADATA@": play_games_metadata,
        "@OPTIONAL_RES_VALUES@": ("\n".join(optional_res_values) + "\n") if optional_res_values else "",
        "@VERSION_CODE@": str(ctx.attr.version_code),
        "@VERSION_NAME@": ctx.attr.version_name,
        "@ENABLE_BACK_BUTTON_EVENTS@": str(ctx.attr.enable_back_button_events).lower(),
        "@KEEP_SCREEN_ON@": str(ctx.attr.keep_screen_on).lower(),
        "@SYSTEM_BARS_MODE@": _system_bars_mode(ctx.attr.system_bars_mode),
        "@SYSTEM_BARS_BEHAVIOR@": _system_bars_behavior(ctx.attr.system_bars_behavior),
        "@NAVIGATION_BAR_CONTRAST_ENFORCED@": str(ctx.attr.navigation_bar_contrast_enforced).lower(),
    }

    project_files = []
    typed_asset_lines = []
    typed_asset_files = []
    android_drawable_destinations = {}
    for group in ctx.attr.assets:
        info = group[SglAssetsInfo]
        for file in info.drawables.to_list():
            marker_index = file.path.rfind("/drawable-")
            if marker_index < 0:
                fail("Typed drawable output has no drawable density directory: %s" % file.path)
            relative_path = file.path[marker_index + 1:]
            density_directory = relative_path.split("/", 1)[0]
            extension_index = file.basename.rfind(".")
            extension = file.basename[extension_index:].lower() if extension_index >= 0 else ""
            stem = file.basename[:extension_index] if extension_index >= 0 else file.basename
            destination = "res/%s/%s%s" % (density_directory, _android_resource_name(stem), extension)
            if destination in android_drawable_destinations:
                fail("Typed drawables %s and %s both map to Android resource %s" % (android_drawable_destinations[destination], file.path, destination))
            android_drawable_destinations[destination] = file.path
            typed_asset_lines.append("%s\t%s" % (file.path, destination))
            typed_asset_files.append(file)
        for category, files in [
            ("raw-image", info.raw_images.to_list()),
            ("binary", info.binaries.to_list()),
            ("text", info.texts.to_list()),
            ("audio", info.audio.to_list()),
            ("font", info.fonts.to_list()),
        ]:
            marker = "/%s/" % category
            for file in files:
                marker_index = file.path.rfind(marker)
                if marker_index < 0:
                    fail("Typed %s output has no category directory: %s" % (category, file.path))
                typed_asset_lines.append("%s\tassets/%s" % (file.path, file.path[marker_index + 1:]))
                typed_asset_files.append(file)

    typed_assets_manifest = ctx.actions.declare_file("%s/typed-assets.tsv" % project_template_dir)
    ctx.actions.write(typed_assets_manifest, "\n".join(typed_asset_lines))
    project_files.append(typed_assets_manifest)

    outputs = [
        (ctx.file._settings_template, ctx.actions.declare_file("%s/settings.gradle.kts" % project_template_dir), substitutions),
        (ctx.file._root_build_template, ctx.actions.declare_file("%s/build.gradle.kts" % project_template_dir), substitutions),
        (ctx.file._gradle_properties_template, ctx.actions.declare_file("%s/gradle.properties" % project_template_dir), substitutions),
        (ctx.file._app_build_template, ctx.actions.declare_file("%s/app/build.gradle.kts" % project_template_dir), substitutions),
        (ctx.file._manifest_template, ctx.actions.declare_file("%s/app/src/main/AndroidManifest.xml" % project_template_dir), substitutions),
        (ctx.file._main_activity_template, ctx.actions.declare_file("%s/app/src/main/java/%s/MainActivity.kt" % (project_template_dir, package_path)), substitutions),
        (ctx.file._proguard_template, ctx.actions.declare_file("%s/app/proguard-rules.pro" % project_template_dir), substitutions),
    ]
    for template, output, subs in outputs:
        _expand(ctx, template, output, subs)
        project_files.append(output)

    runner = ctx.actions.declare_file(ctx.label.name + ".sh")
    ctx.actions.expand_template(
        template = ctx.file._runner_template,
        output = runner,
        substitutions = {
            "@PROJECT_DIR@": project_dir,
            "@PROJECT_TEMPLATE_DIR@": project_template_dir,
            "@GAME_LABELS@": " ".join(game_labels),
            "@SGL_ANDROID_REPO_ROOT@": sgl_android_repo_root,
            "@ASSETS_DIR@": ctx.attr.assets_dir,
            "@ANDROID_RESOURCES_DIR@": ctx.attr.android_resources_dir,
            "@GOOGLE_SERVICES_JSON@": ctx.attr.google_services_json,
            "@GRADLE_TASK@": ctx.attr.gradle_task,
            "@GRADLE_ENV@": ctx.attr.gradle_env,
            "@LAUNCH_CMD@": launch_cmd,
        },
        is_executable = True,
    )

    return [
        DefaultInfo(
            executable = runner,
            files = depset([runner] + project_files + typed_asset_files),
            runfiles = ctx.runfiles(files = project_files + typed_asset_files),
        ),
    ]


_android_runner = rule(
    implementation = _android_runner_impl,
    executable = True,
    attrs = {
        "package": attr.string(mandatory = True),
        "label": attr.string(mandatory = True),
        "core": attr.string(default = ":core"),
        "extra_jars": attr.string_list(default = []),
        "wiring_expression": attr.string(default = ""),
        "sgl_android_root": attr.string(default = ""),
        "version_code": attr.int(default = 1),
        "version_name": attr.string(default = "1.0"),
        "enable_back_button_events": attr.bool(default = False),
        "keep_screen_on": attr.bool(default = True),
        "system_bars_mode": attr.string(default = "immersive", values = ["safe-area", "edge-to-edge", "immersive"]),
        "system_bars_behavior": attr.string(default = "transient-by-swipe", values = ["default", "transient-by-swipe"]),
        "navigation_bar_contrast_enforced": attr.bool(default = False),
        "assets_dir": attr.string(default = ""),
        "assets": attr.label_list(providers = [SglAssetsInfo]),
        "android_resources_dir": attr.string(default = ""),
        "google_services_json": attr.string(default = ""),
        "admob_application_id": attr.string(default = ""),
        "admob_interstitial_ad_unit_id": attr.string(default = "ca-app-pub-3940256099942544/1033173712"),
        "admob_rewarded_ad_unit_id": attr.string(default = "ca-app-pub-3940256099942544/5224354917"),
        "admob_always_preload": attr.bool(default = True),
        "play_games_project_id": attr.string(default = ""),
        "launcher_icon": attr.string(default = ""),
        "project_name": attr.string(mandatory = True),
        "gradle_task": attr.string(mandatory = True),
        "gradle_env": attr.string(default = ""),
        "launch": attr.bool(default = False),
        "_settings_template": attr.label(default = "//bazel/sgl/android:settings.gradle.kts.tpl", allow_single_file = True),
        "_root_build_template": attr.label(default = "//bazel/sgl/android:root-build.gradle.kts.tpl", allow_single_file = True),
        "_gradle_properties_template": attr.label(default = "//bazel/sgl/android:gradle.properties.tpl", allow_single_file = True),
        "_app_build_template": attr.label(default = "//bazel/sgl/android:app-build.gradle.kts.tpl", allow_single_file = True),
        "_manifest_template": attr.label(default = "//bazel/sgl/android:AndroidManifest.xml.tpl", allow_single_file = True),
        "_main_activity_template": attr.label(default = "//bazel/sgl/android:MainActivity.kt.tpl", allow_single_file = True),
        "_proguard_template": attr.label(default = "//bazel/sgl/android:proguard-rules.pro.tpl", allow_single_file = True),
        "_runner_template": attr.label(default = "//bazel/sgl/android:AndroidRunner.sh.tpl", allow_single_file = True),
    },
)


def sgl_android_app(
        name,
        package,
        label,
        core = ":core",
        extra_jars = [],
        wiring_expression = "",
        sgl_android_root = "",
        version_code = 1,
        version_name = "1.0",
        enable_back_button_events = False,
        keep_screen_on = True,
        system_bars_mode = "immersive",
        system_bars_behavior = "transient-by-swipe",
        navigation_bar_contrast_enforced = False,
        assets_dir = "",
        assets = [],
        android_resources_dir = "",
        google_services_json = "",
        admob_application_id = "",
        admob_interstitial_ad_unit_id = "ca-app-pub-3940256099942544/1033173712",
        admob_rewarded_ad_unit_id = "ca-app-pub-3940256099942544/5224354917",
        admob_always_preload = True,
        play_games_project_id = "",
        launcher_icon = ""):
    for suffix, task, launch, gradle_env in [
        ("debug", ":app:assembleDebug", False, ""),
        ("release", ":app:assembleRelease", False, ""),
        ("bundle", ":app:bundleRelease", False, ""),
        ("publish-internal", ":app:publishReleaseBundle", False, "export ANDROID_PLAY_TRACK=internal\nexport ANDROID_PLAY_RELEASE_STATUS=COMPLETED"),
        ("install", ":app:installDebug", False, ""),
        ("run", ":app:installDebug", True, ""),
    ]:
        _android_runner(
            name = name + "-" + suffix,
            package = package,
            label = label,
            core = core,
            extra_jars = extra_jars,
            wiring_expression = wiring_expression,
            sgl_android_root = sgl_android_root,
            version_code = version_code,
            version_name = version_name,
            enable_back_button_events = enable_back_button_events,
            keep_screen_on = keep_screen_on,
            system_bars_mode = system_bars_mode,
            system_bars_behavior = system_bars_behavior,
            navigation_bar_contrast_enforced = navigation_bar_contrast_enforced,
            assets_dir = assets_dir,
            assets = assets,
            android_resources_dir = android_resources_dir,
            google_services_json = google_services_json,
            admob_application_id = admob_application_id,
            admob_interstitial_ad_unit_id = admob_interstitial_ad_unit_id,
            admob_rewarded_ad_unit_id = admob_rewarded_ad_unit_id,
            admob_always_preload = admob_always_preload,
            play_games_project_id = play_games_project_id,
            launcher_icon = launcher_icon,
            project_name = name,
            gradle_task = task,
            gradle_env = gradle_env,
            launch = launch,
            tags = ["manual", "local", "no-sandbox"],
        )
