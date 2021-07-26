package io.unthrottled.theme.randomizer.services

import com.intellij.ide.ui.laf.UIThemeBasedLookAndFeelInfo
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.config.ConfigListener.Companion.CONFIG_TOPIC
import javax.swing.UIManager

class ThemeGatekeeper : Disposable {
  companion object {
    val instance: ThemeGatekeeper
      get() = ServiceManager.getService(ThemeGatekeeper::class.java)

    @JvmStatic
    fun getId(lookAndFeelInfo: UIManager.LookAndFeelInfo): String =
      lookAndFeelInfo.getId()
  }

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  private var preferredThemeIds: Set<String> =
    extractAllowedCharactersFromState(Config.instance.selectedThemes)

  private var blackListedThemeIds: Set<String> =
    extractAllowedCharactersFromState(Config.instance.blacklistedThemes)

  private fun extractAllowedCharactersFromState(characterConfig: String): Set<String> =
    characterConfig.split(Config.DEFAULT_DELIMITER)
      .filter { it.isNotEmpty() }
      .toSet()

  init {
    connection.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        preferredThemeIds = extractAllowedCharactersFromState(newPluginState.selectedThemes)
        blackListedThemeIds = extractAllowedCharactersFromState(newPluginState.blacklistedThemes)
      }
    )
  }

  fun isLegit(lookAndFeelInfo: UIManager.LookAndFeelInfo): Boolean =
    !isBlackListed(lookAndFeelInfo) &&
      (preferredThemeIds.isEmpty() || isPreferred(lookAndFeelInfo))

  fun isPreferred(lookAndFeelInfo: UIManager.LookAndFeelInfo): Boolean =
    preferredThemeIds.contains(
      lookAndFeelInfo.getId()
    )

  fun isBlackListed(lookAndFeelInfo: UIManager.LookAndFeelInfo): Boolean =
    blackListedThemeIds.contains(
      lookAndFeelInfo.getId()
    )

  override fun dispose() {
    connection.dispose()
  }
}

fun UIManager.LookAndFeelInfo.getId(): String =
  when (this) {
    is UIThemeBasedLookAndFeelInfo -> this.theme.id
    else -> this.name
  }
