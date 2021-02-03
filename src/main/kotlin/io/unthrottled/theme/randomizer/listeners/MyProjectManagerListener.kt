package io.unthrottled.theme.randomizer.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.theme.randomizer.PluginMaster

internal class MyProjectManagerListener : ProjectManagerListener {

  override fun projectOpened(project: Project) {
    PluginMaster.instance.projectOpened(project)
  }

  override fun projectClosed(project: Project) {
    PluginMaster.instance.projectClosed(project)
  }
}
