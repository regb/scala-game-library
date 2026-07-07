def _core_label_and_jar(ctx):
    core = ctx.attr.core
    if core.startswith(":"):
        core_name = core[1:]
        core_label = "//%s:%s" % (ctx.label.package, core_name)
    else:
        core_name = core.split(":")[-1]
        core_label = core
    core_jar = "bazel-bin/%s/%s.jar" % (ctx.label.package, core_name)
    return core_label, core_jar


def _expand(ctx, template, output, substitutions):
    ctx.actions.expand_template(
        template = template,
        output = output,
        substitutions = substitutions,
    )


def _android_runner_impl(ctx):
    package_path = ctx.attr.package.replace(".", "/")
    core_label, core_jar = _core_label_and_jar(ctx)
    project_template_dir = ctx.label.name + "_project"
    project_dir = ".bazel-android/%s/%s" % (ctx.label.package, ctx.label.name)
    launch_cmd = "adb shell am start -n %s/.MainActivity" % ctx.attr.package if ctx.attr.launch else "true"

    substitutions = {
        "@PACKAGE@": ctx.attr.package,
        "@LABEL@": ctx.attr.label,
        "@CORE_JAR@": core_jar,
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
            "@CORE_LABEL@": core_label,
            "@GRADLE_TASK@": ctx.attr.gradle_task,
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
        "gradle_task": attr.string(mandatory = True),
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


def sgl_android_app(name, package, label, core = ":core"):
    _android_runner(
        name = name + "-debug",
        package = package,
        label = label,
        core = core,
        gradle_task = ":app:assembleDebug",
        launch = False,
        tags = ["manual", "local", "no-sandbox"],
    )
    _android_runner(
        name = name + "-install",
        package = package,
        label = label,
        core = core,
        gradle_task = ":app:installDebug",
        launch = False,
        tags = ["manual", "local", "no-sandbox"],
    )
    _android_runner(
        name = name + "-run",
        package = package,
        label = label,
        core = core,
        gradle_task = ":app:installDebug",
        launch = True,
        tags = ["manual", "local", "no-sandbox"],
    )
