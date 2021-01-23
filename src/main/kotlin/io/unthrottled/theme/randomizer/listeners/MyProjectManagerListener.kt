package io.unthrottled.theme.randomizer.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.unthrottled.theme.randomizer.onboarding.UserOnBoarding
import io.unthrottled.theme.randomizer.services.MyProjectService

internal class MyProjectManagerListener : ProjectManagerListener {

  override fun projectOpened(project: Project) {
    UserOnBoarding.attemptToPerformNewUpdateActions(project)
    project.service<MyProjectService>()
  }
}
