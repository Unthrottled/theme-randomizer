package io.unthrottled.theme.randomizer

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.util.Disposer
import com.intellij.ui.BalloonLayoutData
import icons.ThemeRandomizerIcons.PLUGIN_ICON
import io.unthrottled.theme.randomizer.config.Constants.PLUGIN_NAME
import io.unthrottled.theme.randomizer.tools.BalloonTools.fetchBalloonParameters
import org.intellij.lang.annotations.Language

@Suppress("MaxLineLength")
@Language("HTML")
private val UPDATE_MESSAGE: String =
  """
      What's New?<br>
      <ul>
        <li>Initial Release! See the <a href="https://github.com/Unthrottled/AMII#documentation">
      documentation</a> for features, usages, and configurations.</li>
      </ul>
      <br>Please see the <a href="https://github.com/Unthrottled/AMII/blob/master/CHANGELOG.md">changelog</a> for more details.
      Thanks for downloading!
      </div>
  """.trimIndent()

object UpdateNotification {

  private const val UPDATE_CHANNEL_NAME = "$PLUGIN_NAME Updates"
  private val notificationGroup = NotificationGroup(
    UPDATE_CHANNEL_NAME,
    NotificationDisplayType.STICKY_BALLOON,
    false,
    UPDATE_CHANNEL_NAME
  )

  fun display(
    project: Project,
    newVersion: String
  ) {
    val updateNotification = notificationGroup.createNotification(
      "$PLUGIN_NAME updated to v$newVersion",
      UPDATE_MESSAGE,
      NotificationType.INFORMATION
    )
      .setIcon(PLUGIN_ICON)
      .setListener(NotificationListener.UrlOpeningListener(false))

    showNotification(project, updateNotification)
  }

  fun sendMessage(
    title: String,
    message: String,
    project: Project? = null
  ) {
    showRegularNotification(
      title,
      message,
      project = project,
      listener = defaultListener
    )
  }

  private val defaultListener = NotificationListener.UrlOpeningListener(false)

  private fun showRegularNotification(
    title: String = "",
    content: String,
    project: Project? = null,
    listener: NotificationListener? = defaultListener
  ) {
    notificationGroup.createNotification(
      title,
      content,
      listener = listener
    ).setIcon(PLUGIN_ICON)
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
