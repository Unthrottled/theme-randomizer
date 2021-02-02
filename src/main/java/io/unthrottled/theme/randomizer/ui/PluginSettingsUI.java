package io.unthrottled.theme.randomizer.ui;

import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.ui.JBUI;
import io.unthrottled.theme.randomizer.config.Config;
import io.unthrottled.theme.randomizer.config.ConfigListener;
import io.unthrottled.theme.randomizer.config.ConfigSettingsModel;
import io.unthrottled.theme.randomizer.services.ThemeGatekeeper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.util.Vector;
import java.util.stream.Collectors;

public class PluginSettingsUI implements SearchableConfigurable, Configurable.NoScroll, DumbAware {
  private final ConfigSettingsModel pluginSettingsModel = Config.getInitialConfigSettingsModel();
  private ConfigSettingsModel initialSettings = Config.getInitialConfigSettingsModel();
  private JTabbedPane tabbedPane1;
  private JPanel rootPane;
  private JPanel lafListPane;
  private JCheckBox changeThemeCheckbox;
  private JCheckBox randomOrderCheckbox;
  private JComboBox<String> changeIntervalWomboComboBox;
  private LAFListPanel lafListPanelModel;

  private void createUIComponents() {
    lafListPanelModel = new LAFListPanel(
      ThemeGatekeeper.Companion.getInstance()::isPreferred
    );
    lafListPane = lafListPanelModel.getComponent();
    lafListPane.setPreferredSize(JBUI.size(800, 600));
    lafListPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
    changeIntervalWomboComboBox.setModel(
      new DefaultComboBoxModel<>(
        new Vector<>(
          Lists.newArrayList(
            "Ayy", "lmao"
          )
        )
      )
    );
    changeIntervalWomboComboBox.getModel().setSelectedItem(
      initialSettings.getInterval()
    );
    changeThemeCheckbox.addActionListener(e ->
      pluginSettingsModel.setInterval((String) changeIntervalWomboComboBox.getModel().getSelectedItem()));

    changeThemeCheckbox.setSelected(initialSettings.isChangeTheme());
    randomOrderCheckbox.setSelected(initialSettings.isRandomOrder());

    return rootPane;
  }

  @Override
  public boolean isModified() {
    return !initialSettings.equals(pluginSettingsModel) ||
      lafListPanelModel.isModified();
  }

  @Override
  public void apply() {
    Config config = Config.getInstance();
    config.setInterval(pluginSettingsModel.getInterval());
    config.setChangeTheme(pluginSettingsModel.isChangeTheme());
    config.setRandomOrder(pluginSettingsModel.isRandomOrder());
    config.setSelectedThemes(convertToStorageString(lafListPanelModel));
    ApplicationManager.getApplication().getMessageBus().syncPublisher(
      ConfigListener.Companion.getCONFIG_TOPIC()
    ).pluginConfigUpdated(config);
    initialSettings = pluginSettingsModel.duplicate();
  }

  @NotNull
  private String convertToStorageString(LAFListPanel lafListPanel) {
    return lafListPanel.getSelected().stream()
      .map(ThemeGatekeeper.Companion.getInstance()::getId)
      .collect(Collectors.joining(Config.DEFAULT_DELIMITER));
  }
}
