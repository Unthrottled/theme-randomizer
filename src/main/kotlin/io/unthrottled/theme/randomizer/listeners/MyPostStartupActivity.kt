package io.unthrottled.theme.randomizer.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import io.unthrottled.theme.randomizer.PluginMaster

internal class MyPostStartupActivity : StartupActivity {
  override fun runActivity(project: Project) {
    PluginMaster.instance.projectOpened(project)
  }
}
