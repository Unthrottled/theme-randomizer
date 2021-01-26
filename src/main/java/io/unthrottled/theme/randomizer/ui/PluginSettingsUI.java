package io.unthrottled.theme.randomizer.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class PluginSettingsUI implements SearchableConfigurable, Configurable.NoScroll, DumbAware {
  private JTabbedPane tabbedPane1;
  private JPanel panel1;
  private JPanel lafList;

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }

  @Override
  public @NotNull
  @NonNls
  String getId() {
    return "io.unthrottled.theme.randomizer.PluginSettings";
  }

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return "Theme Randomizer Settings";
  }

  @Override
  public @Nullable JComponent createComponent() {
    return panel1;
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {

  }
}
