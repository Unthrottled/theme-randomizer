package io.unthrottled.theme.randomizer.services

import io.unthrottled.theme.randomizer.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

  init {
    println(MyBundle.message("projectService", project.name))
  }
}
