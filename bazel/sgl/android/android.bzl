def _label_and_jar(package_name, label):
    if label.startswith(":"):
        name = label[1:]
        full_label = "//%s:%s" % (package_name, name) if package_name else "//:%s" % name
    else:
        name = label.split(":")[-1]
        full_label = label

    jar = "bazel-bin/%s/%s.jar" % (package_name, name) if package_name else "bazel-bin/%s.jar" % name
    return full_label, jar


def _expand(ctx, template, output, substitutions):
    ctx.actions.expand_template(
        template = template,
        output = output,
        substitutions = substitutions,
    )


def _android_runner_impl(ctx):
    package_path = ctx.attr.package.replace(".", "/")
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
    optional_modules = "include(\":sgl-android-firebase\")\nproject(\":sgl-android-firebase\").projectDir = file(\"%s/android-kotlin/firebase\")" % ctx.attr.sgl_android_root if firebase_enabled else ""
    optional_dependencies = "    implementation(project(\":sgl-android-firebase\"))" if firebase_enabled else ""
    optional_imports = "import sgl.android.analytics.AndroidFirebaseAnalytics\n" if firebase_enabled else ""

    substitutions = {
        "@PACKAGE@": ctx.attr.package,
        "@LABEL@": ctx.attr.label,
        "@APPLICATION_ICON@": application_icon,
        "@JAR_DEPENDENCIES@": jar_dependencies,
        "@WIRING_EXPRESSION@": wiring_expression,
        "@OPTIONAL_MODULES@": optional_modules,
        "@OPTIONAL_DEPENDENCIES@": optional_dependencies,
        "@OPTIONAL_IMPORTS@": optional_imports,
        "@SGL_ANDROID_ROOT@": ctx.attr.sgl_android_root,
        "@VERSION_CODE@": str(ctx.attr.version_code),
        "@VERSION_NAME@": ctx.attr.version_name,
    }

    project_files = []
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
            "@SGL_ANDROID_ROOT@": ctx.attr.sgl_android_root,
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
            files = depset([runner] + project_files),
            runfiles = ctx.runfiles(files = project_files),
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
        "sgl_android_root": attr.string(default = "/home/regb/vcs/games/scala-game-library-android"),
        "version_code": attr.int(default = 1),
        "version_name": attr.string(default = "1.0"),
        "assets_dir": attr.string(default = ""),
        "android_resources_dir": attr.string(default = ""),
        "google_services_json": attr.string(default = ""),
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
        sgl_android_root = "/home/regb/vcs/games/scala-game-library-android",
        version_code = 1,
        version_name = "1.0",
        assets_dir = "",
        android_resources_dir = "",
        google_services_json = "",
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
            assets_dir = assets_dir,
            android_resources_dir = android_resources_dir,
            google_services_json = google_services_json,
            launcher_icon = launcher_icon,
            project_name = name,
            gradle_task = task,
            gradle_env = gradle_env,
            launch = launch,
            tags = ["manual", "local", "no-sandbox"],
        )
