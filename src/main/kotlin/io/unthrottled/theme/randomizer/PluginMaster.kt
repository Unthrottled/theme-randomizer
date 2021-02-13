package io.unthrottled.theme.randomizer

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.theme.randomizer.onboarding.UserOnBoarding
import io.unthrottled.theme.randomizer.services.LAFProbabilityService
import io.unthrottled.theme.randomizer.services.ThemeChangeEventEmitter
import io.unthrottled.theme.randomizer.tools.Logging

class PluginMaster : ProjectManagerListener, Disposable, Logging {

  companion object {
    val instance: PluginMaster
      get() = ServiceManager.getService(PluginMaster::class.java)
  }

  init {
    LAFProbabilityService.instance.toString() // wake up the look and feel probability service
  }

  private val themeChangeEventEmitter = ThemeChangeEventEmitter()

  override fun projectOpened(project: Project) {
    registerListenersForProject(project)
  }

  private fun registerListenersForProject(project: Project) {
    UserOnBoarding.attemptToPerformNewUpdateActions(project)
  }

  override fun projectClosed(project: Project) {
  }

  override fun dispose() {
    themeChangeEventEmitter.dispose()
  }

  fun onUpdate() {
    ProjectManager.getInstance().openProjects
      .forEach { registerListenersForProject(it) }
  }
}
