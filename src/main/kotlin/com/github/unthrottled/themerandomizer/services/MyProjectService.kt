package com.github.unthrottled.themerandomizer.services

import com.github.unthrottled.themerandomizer.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
