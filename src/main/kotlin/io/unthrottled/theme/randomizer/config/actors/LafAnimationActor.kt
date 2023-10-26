package io.unthrottled.theme.randomizer.config.actors

import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.util.registry.RegistryValue
import io.unthrottled.theme.randomizer.onboarding.UpdateNotification
import io.unthrottled.theme.randomizer.tools.toOptional

object LafAnimationActor {
  fun enableAnimation(enabled: Boolean) {
    if (enabled == getAnimationEnabled()) return

    getAnimationRegistryValue().setValue(enabled)
    if (!enabled) return

    UpdateNotification.sendMessage(
      "Theme Transition Animation Enabled",
      """The animations will remain in your IDE after uninstalling the plugin.
      |To remove them, un-check this action or toggle the action at
      |"Help -> Find Action -> ide.intellij.laf.enable.animation".
      """.trimMargin()
    )
  }

  private fun getAnimationRegistryValue(): RegistryValue = Registry.get("ide.intellij.laf.enable.animation")

  fun getAnimationEnabled(): Boolean =
    getAnimationRegistryValue()
      .toOptional()
      .filter { it.isBoolean }
      .map { it.asBoolean() }
      .orElse(false)
}
