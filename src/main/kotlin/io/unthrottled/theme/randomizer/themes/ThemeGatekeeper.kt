package io.unthrottled.theme.randomizer.themes

import com.intellij.ide.ui.laf.UIThemeLookAndFeelInfo
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager

@Suppress("UnstableApiUsage")
class ThemeGatekeeper : Disposable {

  fun isLegit(lookAndFeelInfo: UIThemeLookAndFeelInfo): Boolean =
    !isBlackListed(lookAndFeelInfo) &&
      (ThemeSelectionService.instance.preferredThemeIds.isEmpty() || isPreferred(lookAndFeelInfo))

  fun isPreferred(lookAndFeelInfo: UIThemeLookAndFeelInfo): Boolean =
    ThemeSelectionService.instance.preferredThemeIds.contains(lookAndFeelInfo.id)

  fun isBlackListed(lookAndFeelInfo: UIThemeLookAndFeelInfo): Boolean =
    ThemeSelectionService.instance.blackListedThemeIds.contains(lookAndFeelInfo.id)

  override fun dispose() = Unit

  companion object {
    val instance: ThemeGatekeeper
      get() = ApplicationManager.getApplication().getService(ThemeGatekeeper::class.java)

    @JvmStatic
    fun getId(lookAndFeelInfo: UIThemeLookAndFeelInfo): String = lookAndFeelInfo.id
  }

}
