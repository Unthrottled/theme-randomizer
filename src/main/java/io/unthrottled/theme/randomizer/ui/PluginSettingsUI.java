package io.unthrottled.theme.randomizer.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.ui.JBUI;
import io.unthrottled.theme.randomizer.config.ChangeIntervals;
import io.unthrottled.theme.randomizer.config.Config;
import io.unthrottled.theme.randomizer.config.ConfigListener;
import io.unthrottled.theme.randomizer.config.ConfigSettingsModel;
import io.unthrottled.theme.randomizer.config.IntervalTuple;
import io.unthrottled.theme.randomizer.config.SettingsHelper;
import io.unthrottled.theme.randomizer.config.actors.LafAnimationActor;
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
import java.util.Arrays;
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
  private JComboBox<IntervalTuple> changeIntervalWomboComboBox;
  private JCheckBox animationCheckbox;
  private JTabbedPane preferredThemesTabbo;
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
    return "Theme Randomizer";
  }

  @Override
  public @Nullable JComponent createComponent() {
    changeIntervalWomboComboBox.setModel(
      new DefaultComboBoxModel<>(
        new Vector<>(
          Arrays.stream(ChangeIntervals.values())
            .map(this::createIntervalTuple)
            .collect(Collectors.toList())
        )
      )
    );

    ChangeIntervals.Companion.getValue(initialSettings.getInterval())
      .ifPresent(value -> changeIntervalWomboComboBox.getModel().setSelectedItem(
        createIntervalTuple(value)
      ));

    changeIntervalWomboComboBox.addActionListener(e ->
      pluginSettingsModel.setInterval(
        ((IntervalTuple) changeIntervalWomboComboBox.getModel().getSelectedItem())
          .getInterval().toString()));


    changeThemeCheckbox.setSelected(initialSettings.isChangeTheme());
    changeThemeCheckbox.addActionListener(e ->
      pluginSettingsModel.setChangeTheme(changeThemeCheckbox.isSelected()));

    randomOrderCheckbox.setSelected(initialSettings.isRandomOrder());
    randomOrderCheckbox.addActionListener(e ->
      pluginSettingsModel.setRandomOrder(randomOrderCheckbox.isSelected()));

    animationCheckbox.setSelected(initialSettings.isThemeTransition());
    animationCheckbox.addActionListener(e ->
      pluginSettingsModel.setThemeTransition(animationCheckbox.isSelected()));

    return rootPane;
  }

  @NotNull
  private IntervalTuple createIntervalTuple(ChangeIntervals value) {
    return new IntervalTuple(value, SettingsHelper.getDisplayMapping(value));
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
    LafAnimationActor.INSTANCE.enableAnimation(pluginSettingsModel.isThemeTransition());
    ApplicationManager.getApplication().getMessageBus().syncPublisher(
      ConfigListener.Companion.getCONFIG_TOPIC()
    ).pluginConfigUpdated(config);
    initialSettings = pluginSettingsModel.duplicate();
  }

  @NotNull
  private String convertToStorageString(LAFListPanel lafListPanel) {
    return lafListPanel.getSelected().stream()
      .map(ThemeGatekeeper::getId)
      .collect(Collectors.joining(Config.DEFAULT_DELIMITER));
  }
}
