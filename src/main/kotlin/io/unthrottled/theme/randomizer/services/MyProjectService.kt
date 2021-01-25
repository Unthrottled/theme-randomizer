package io.unthrottled.theme.randomizer.services

import com.intellij.openapi.project.Project
import io.unthrottled.theme.randomizer.MyBundle

class MyProjectService(project: Project) {

  init {
    println(MyBundle.message("projectService", project.name))
  }
}
