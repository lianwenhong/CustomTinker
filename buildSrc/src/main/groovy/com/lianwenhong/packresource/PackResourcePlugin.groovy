package com.lianwenhong.packresource

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class PackResourcePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        Task taskA = project.task("abc") {
            println(" >>>> >>>> >>>> >>>> >>>> ")
            doFirst(new Action<Task>() {
                @Override
                void execute(Task task) {
                    print(" >>>> >>>> inputs:" + inputs.files.size())
                }
            })
            doLast {

            }
        }.dependsOn("compileDebugJavaWithJavac") {}

        taskA.mustRunAfter("compileDebugJavaWithJavac")
    }
}
