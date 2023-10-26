package io.unthrottled.theme.randomizer.listeners

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.theme.randomizer.PluginMaster
import io.unthrottled.theme.randomizer.config.Constants.PLUGIN_ID
import io.unthrottled.theme.randomizer.tools.Logging

class IDEPluginInstallListener : DynamicPluginListener, Logging {
  override fun beforePluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
  }

  override fun beforePluginUnload(
    pluginDescriptor: IdeaPluginDescriptor,
    isUpdate: Boolean
  ) {
  }

  override fun checkUnloadPlugin(pluginDescriptor: IdeaPluginDescriptor) {
  }

  override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
    if (pluginDescriptor.pluginId.idString == PLUGIN_ID) {
      ApplicationManager.getApplication().invokeLater {
        PluginMaster.instance.onUpdate()
      }
    }
  }

  override fun pluginUnloaded(
    pluginDescriptor: IdeaPluginDescriptor,
    isUpdate: Boolean
  ) {
  }
}
