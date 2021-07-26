package io.unthrottled.theme.randomizer.ui;

import com.intellij.ui.GuiUtils;
import io.unthrottled.theme.randomizer.config.ui.PreferredLAFTree;

import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.util.List;
import java.util.function.Predicate;

public final class LAFListPanel {
  private final PreferredLAFTree myLAFTree;
  private JPanel myPanel;
  private JPanel myTreePanel;

  public LAFListPanel(
    Predicate<UIManager.LookAndFeelInfo> selectionPredicate
  ) {
    myLAFTree = new PreferredLAFTree(selectionPredicate);
    myTreePanel.setLayout(new BorderLayout());
    myTreePanel.add(myLAFTree.getComponent(), BorderLayout.CENTER);

    GuiUtils.replaceJSplitPaneWithIDEASplitter(myPanel);
  }

  public void reset() {
    myLAFTree.reset();
  }

  public List<UIManager.LookAndFeelInfo> getSelected() {
    return myLAFTree.getSelected();
  }

  public JPanel getComponent() {
    return myPanel;
  }

  public boolean isModified() {
    return myLAFTree.isModified();
  }

  public void dispose() {
    myLAFTree.dispose();
  }

  public Runnable showOption(final String option) {
    return () -> {
      myLAFTree.filter(myLAFTree.filterModel(option, true));
      myLAFTree.setFilter(option);
    };
  }
}
