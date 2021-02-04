# theme-randomizer

![Build](https://github.com/Unthrottled/theme-randomizer/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

## Template ToDo list

- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [ ] Verify the [pluginGroup](/gradle.properties), [plugin ID](/src/main/resources/META-INF/plugin.xml)
  and [sources package](/src/main/kotlin).
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate)
  for the first time.
- [ ] Set the Plugin ID in the above README badges.
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified
  about releases containing new features and fixes.

<!-- Plugin description -->
Do you have many themes installed? Do you have many favorite themes? Would you like to be able randomly set your theme?

Well look no further! The Theme Randomizer is exactly that. It can pick a random theme or cycle to the next theme in
line. You can also customize the interval when your theme changes as well!
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "
  theme-randomizer"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/Unthrottled/theme-randomizer/releases/latest) and install it manually
  using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Configuration

<kbd>Settings/Preferences</kbd> > <kbd>Appearance & Behavior</kbd> > <kbd>Theme Randomizer</kbd>

### General Settings
This section is dedicated to describing the general configurable functionality  tab.

#### Settings

**Change Theme**

**Random Order**

**Theme Transition Animation**

Toggles the built-in theme change fade animation.

_The animations will remain in your IDE after uninstalling the plugin._

To remove them, un-check this action or toggle the action at
<kbd>Help<kbd> > <kbd>Find Action<kbd> > ide.intellij.laf.enable.animation


#### Preferred Themes

This is the complete list of all the installed themes on your ide.

When **no themes** are preferred the randomizer will pick from all the themes. However, once one or many themes become
preferred the randomizer will choose from the preferred list. This will happen the next time a theme change interval is
emitted.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
