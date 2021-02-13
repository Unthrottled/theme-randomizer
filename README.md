# Theme Randomizer

![Build](https://github.com/Unthrottled/theme-randomizer/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/16107.svg)](https://plugins.jetbrains.com/plugin/16107)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/16107.svg)](https://plugins.jetbrains.com/plugin/16107)

<!-- Plugin description -->
Do you have many themes installed? Do you have many favorite themes? Would you like to be able randomly set your theme?

Well look no further! The Theme Randomizer is exactly that. It can pick a random theme or cycle to the next theme in
line. You can also customize the interval when your theme changes as well!
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Theme Randomizer"</kbd> >
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

If enabled this allows the randomizer plugin to change the theme at the specified interval.

Note: if you set a theme manually, that will reset the countdown to changing the theme.
Such as if you change a theme,and your interval is a day, then the next theme change will happen 24 hours after your manual theme change.

**Random Order**

Well this puts the randomizer in `Theme Randomizer`.
This will evenly and randomly distribute all themes, so you don't see the same theme too many times!

If it is not set, then the plugin will run through the themes sequentially by name in ascending order.

**Theme Transition Animation**

Toggles the built-in theme change fade animation.

_The animations will remain in your IDE after uninstalling the plugin._

To remove them, un-check this action or toggle the action at
<kbd>Help</kbd> > <kbd>Find Action</kbd> > <kbd>ide.intellij.laf.enable.animation</kbd>


#### Preferred Themes

This is the complete list of all the installed themes on your ide.

When **no themes** are preferred the randomizer will pick from all the themes. However, once one or many themes become
preferred the randomizer will choose from the preferred list. This will happen the next time a theme change interval is
emitted.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
