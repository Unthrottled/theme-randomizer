package io.unthrottled.theme.randomizer.onboarding

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.util.Disposer
import com.intellij.ui.BalloonLayoutData
import icons.ThemeRandomizerIcons.PLUGIN_ICON
import io.unthrottled.theme.randomizer.config.Constants.PLUGIN_NAME
import io.unthrottled.theme.randomizer.tools.BalloonTools.fetchBalloonParameters

object UpdateNotification {
  private const val UPDATE_CHANNEL_NAME = "$PLUGIN_NAME Updates"

  private val notificationGroup = NotificationGroupManager.getInstance()
    .getNotificationGroup(UPDATE_CHANNEL_NAME)

  fun sendMessage(
    title: String,
    message: String,
    project: Project? = null
  ) {
    showRegularNotification(
      title,
      message,
      project = project,
    )
  }

  private fun showRegularNotification(
    title: String = "",
    content: String,
    project: Project? = null,
  ) {
    notificationGroup.createNotification(
      content,
      NotificationType.INFORMATION
    )
      .setTitle(title)
      .setIcon(PLUGIN_ICON)
      .notify(project)
  }

  private fun showNotification(
    project: Project,
    updateNotification: Notification
  ) {
    try {
      val (ideFrame, notificationPosition) = fetchBalloonParameters(project)
      val balloon = NotificationsManagerImpl.createBalloon(
        ideFrame,
        updateNotification,
        true,
        false,
        BalloonLayoutData.fullContent(),
        Disposer.newDisposable()
      )
      balloon.show(notificationPosition, Balloon.Position.atLeft)
    } catch (e: Throwable) {
      updateNotification.notify(project)
    }
  }
}
