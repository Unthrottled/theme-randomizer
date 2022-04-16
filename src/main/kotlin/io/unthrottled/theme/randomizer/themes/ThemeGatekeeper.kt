package io.unthrottled.theme.randomizer.themes

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ConfigListener
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
        ConfigListener.CONFIG_TOPIC,
      ConfigListener { newPluginState, _ ->
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
