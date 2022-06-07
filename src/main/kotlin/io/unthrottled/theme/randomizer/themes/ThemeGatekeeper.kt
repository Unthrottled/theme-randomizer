package io.unthrottled.theme.randomizer.themes

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import javax.swing.UIManager

class ThemeGatekeeper : Disposable {
  companion object {
    val instance: ThemeGatekeeper
      get() = ApplicationManager.getApplication().getService(ThemeGatekeeper::class.java)

    @JvmStatic
    fun getId(lookAndFeelInfo: UIManager.LookAndFeelInfo): String =
      lookAndFeelInfo.getId()
  }

  fun isLegit(lookAndFeelInfo: UIManager.LookAndFeelInfo): Boolean =
    !isBlackListed(lookAndFeelInfo) &&
      (ThemeSelectionService.instance.preferredThemeIds.isEmpty() || isPreferred(lookAndFeelInfo))

  fun isPreferred(lookAndFeelInfo: UIManager.LookAndFeelInfo): Boolean =
    ThemeSelectionService.instance.preferredThemeIds.contains(
      lookAndFeelInfo.getId()
    )

  fun isBlackListed(lookAndFeelInfo: UIManager.LookAndFeelInfo): Boolean =
    ThemeSelectionService.instance.blackListedThemeIds.contains(
      lookAndFeelInfo.getId()
    )

  override fun dispose() {
  }
}
