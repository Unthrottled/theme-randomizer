package io.unthrottled.theme.randomizer.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import io.unthrottled.theme.randomizer.PluginMaster

internal class MyPostStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    PluginMaster.instance.projectOpened()
  }
}
