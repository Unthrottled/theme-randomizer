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

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Theme
  Randomizer"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/Unthrottled/theme-randomizer/releases/latest) and install it manually
  using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Configuration

<kbd>Settings/Preferences</kbd> > <kbd>Appearance & Behavior</kbd> > <kbd>Theme Randomizer</kbd>

### General Settings

This section is dedicated to describing the general configurable functionality tab.

#### Settings

**Plugin Mode**

- **Timed**: Configures the plugin to randomly choose a theme to be used in a fixed time interval.
- **Match OS**: Configures the plugin to randomly choose at theme when the plugin observes your system changes from
  light to dark or dark to light.

**Change Theme**

If enabled this allows the randomizer plugin to change the theme at the specified interval.

Note: if you set a theme manually, that will reset the countdown to changing the theme. Such as if you change a
theme,and your interval is a day, then the next theme change will happen 24 hours after your manual theme change.

__Switches Before Change__

Allows you to specify how many times a theme will be associated with a system switch.

For example, if you choose `0`, you will get a random theme everytime your system switches to a different mode.

For clarity, if you choose `1`, and you have these themes:

```
Light Themes: Light A, Light B, Light C
Dark Themes: Dark A, Dark B, Dark C
```

Here is a table as to how this would work:

<details>
  <summary>Theme Table</summary>

| Day of Week | System Mode | Theme   |
|-------------|-------------|---------|
| Monday      | Dark AM     | Dark  C |
| Monday      | Light       | Light B |
| Monday      | Dark PM     | Dark  C |
| Tuesday     | Light       | Light B |
| Tuesday     | Dark        | Dark  A |
| Wednesday   | Light       | Light C |
| Wednesday   | Dark        | Dark  A |
| Thursday    | Light       | Light C |
| Thursday    | Dark        | Dark  B |
| Friday      | Light       | Light A |
| Friday      | Dark        | Dark  B |
| Saturday    | Light       | Light A |

**Note**: This is provided you are using your IDE any time before & after a system switch.
EG, if you use your IDE monday morning and quit using it before Monday Night. Then you open your IDE up Thursday
Morning, you'll still have the same theme.
Themes only change when the plugin observes a system change.

</details>

**Random Order**

Well this puts the randomizer in `Theme Randomizer`. This will evenly and randomly distribute all themes, so you don't
see the same theme too many times!

If it is not set, then the plugin will run through the themes sequentially by name in ascending order.

**Locally Auto-Sync Settings**
Allows you to _only_ synchronize the list of preferred/blacklisted themes across IDEs.

The first IDE that you enable Locally Auto-Sync Settings in the settings & save will set the initial list.
Then when you go to another IDE & enable Locally Auto-Sync Settings, then the settings from the other IDE will overwrite the current settings (e.g. keeps it in sync with the first one).
As long as there is a centralized setting, that will overwrite the IDE settings, when Locally Auto-Sync Settings. When you disable the action, it will go back to the IDE specific setting.

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

#### Blacklisted Themes

Is really handy if you are using a plugin whose author does not know how to stop adding themes. If you leave all the **
preferred themes** unchecked, and just exclude specific themes in the black list. That way when new themes are added,
you do not have to remember to add the new theme to your preferred list.

**Note**: blacklist takes priority of preference. If you have a theme preferred and excluded, then the theme will not
show up as a next chosen theme.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
