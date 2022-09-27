package io.unthrottled.theme.randomizer.config.ui

import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.ide.TreeExpander
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.laf.UIThemeBasedLookAndFeelInfo
import com.intellij.ide.ui.laf.darcula.DarculaLookAndFeelInfo
import com.intellij.ide.ui.search.SearchUtil
import com.intellij.ide.ui.search.SearchableOptionsRegistrar
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.packageDependencies.ui.TreeExpansionMonitor
import com.intellij.ui.CheckboxTree
import com.intellij.ui.CheckboxTree.CheckboxTreeCellRenderer
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.FilterComponent
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import io.unthrottled.theme.randomizer.MyBundle
import io.unthrottled.theme.randomizer.tools.toOptional
import java.awt.BorderLayout
import java.awt.EventQueue
import java.util.LinkedList
import java.util.function.Predicate
import java.util.stream.Stream
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

data class ThemeGroupData(
  val name: String,
  val lookAndFeels: List<UIManager.LookAndFeelInfo>
)

@Suppress("TooManyFunctions") // cuz I said so.
class PreferredLAFTree(
  private val selectionPredicate: Predicate<UIManager.LookAndFeelInfo>
) {
  private val themeCheckStatus: MutableMap<String, Boolean> = HashMap()
  val component: JComponent = JPanel(BorderLayout())
  private val myTree: CheckboxTree = createTree()
  private val myFilter: FilterComponent = MyFilterComponent()
  private val toolbarPanel: JPanel = JPanel(BorderLayout())
  private val myToggleAll = JBCheckBox()

  private fun initTree() {
    val scrollPane = ScrollPaneFactory.createScrollPane(myTree)
    toolbarPanel.add(myFilter, BorderLayout.CENTER)
    toolbarPanel.border = JBUI.Borders.emptyBottom(2)
    val group = DefaultActionGroup()
    val actionManager = CommonActionsManager.getInstance()
    val treeExpander: TreeExpander = DefaultTreeExpander(myTree)
    group.add(actionManager.createExpandAllAction(treeExpander, myTree))
    group.add(actionManager.createCollapseAllAction(treeExpander, myTree))
    toolbarPanel.add(
      ActionManager.getInstance().createActionToolbar("PreferredCharacterTree", group, true).component,
      BorderLayout.WEST
    )

    myToggleAll.isSelected = getAllNodes()
      .map { node: CheckedTreeNode -> node.isChecked }
      .reduce { a: Boolean, b: Boolean ->
        a && b
      }
      .orElse(false)
    myToggleAll.text = MyBundle.message("settings.general.preferred-themes.toggle-all")
    myToggleAll.addActionListener {
      ApplicationManager.getApplication().invokeLater {
        getAllNodes().forEach { node ->
          node.isChecked = myToggleAll.isSelected
        }
      }
    }
    toolbarPanel.add(myToggleAll, BorderLayout.EAST)

    component.add(toolbarPanel, BorderLayout.NORTH)
    component.add(scrollPane, BorderLayout.CENTER)

    if (EventQueue.isDispatchThread()) {
      myFilter.reset()
    }

    reset(copyAndSort(getThemeList()))
    myToggleAll.isSelected = getAllNodes()
      .map { node: CheckedTreeNode -> node.isChecked }
      .reduce { a: Boolean, b: Boolean ->
        a && b
      }
      .orElse(false)
  }

  private fun createTree() =
    CheckboxTree(
      object : CheckboxTreeCellRenderer(true) {
        override fun customizeRenderer(
          tree: JTree,
          value: Any,
          selected: Boolean,
          expanded: Boolean,
          leaf: Boolean,
          row: Int,
          hasFocus: Boolean
        ) {
          if (value !is CheckedTreeNode) return

          val attributes =
            if (value.userObject is ThemeGroupData) SimpleTextAttributes.REGULAR_ATTRIBUTES
            else SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES
          val text = getNodeText(value)
          val background = UIUtil.getTreeBackground(selected, true)
          UIUtil.changeBackGround(this, background)
          SearchUtil.appendFragments(
            myFilter.toOptional().map { it.filter }.orElse(null),
            text,
            attributes.style,
            attributes.fgColor,
            background,
            textRenderer
          )
        }
      },
      CheckedTreeNode(null)
    )

  fun filterModel(filter: String?, force: Boolean): List<ThemeGroupData> {
    val list: List<ThemeGroupData> = getThemeList()
    if (filter.isNullOrEmpty()) {
      return list
    }

    var result: List<ThemeGroupData> =
      getThemeList {
        it.name.contains(filter, ignoreCase = true)
      }

    val filters = SearchableOptionsRegistrar.getInstance().getProcessedWords(filter)
    if (force && result.isEmpty()) {
      if (filters.size > 1) {
        result = filterModel(filter, false)
      }
    }
    return result
  }

  fun filter(intentionsToShow: List<ThemeGroupData>) {
    refreshCheckStatus(myTree.model.root as CheckedTreeNode)
    reset(copyAndSort(intentionsToShow))
  }

  fun reset() {
    themeCheckStatus.clear()
    reset(copyAndSort(getThemeList()))
  }

  private fun getThemeList(predicate: (UIManager.LookAndFeelInfo) -> Boolean = { true }) =
    LafManager.getInstance().installedLookAndFeels
      .filter(predicate)
      .sortedBy { it.name }
      .groupBy {
        it.isDark()
      }
      .map {
        ThemeGroupData(if (it.key) "Dark Themes" else "Light Themes", it.value)
      }

  private fun reset(sortedThemeData: List<ThemeGroupData>) {
    if (!EventQueue.isDispatchThread()) {
      return
    }

    val root = CheckedTreeNode(null)
    val treeModel = myTree.model as DefaultTreeModel
    sortedThemeData.forEach { themeData ->
      val themeRoot = CheckedTreeNode(themeData.name)
      themeData.lookAndFeels.forEach { uiLookAndFeel ->
        val themeNode = CheckedTreeNode(uiLookAndFeel)
        themeNode.isChecked = selectionPredicate.test(uiLookAndFeel)
        treeModel.insertNodeInto(themeNode, themeRoot, themeRoot.childCount)
      }
      themeRoot.isChecked = themeData.lookAndFeels.all { lookAndFeelInfo ->
        selectionPredicate.test(lookAndFeelInfo)
      }
      treeModel.insertNodeInto(themeRoot, root, root.childCount)
    }
    treeModel.setRoot(root)
    treeModel.nodeChanged(root)
    TreeUtil.expandAll(myTree)
    myTree.setSelectionRow(0)
  }

  private val root: CheckedTreeNode
    get() = myTree.model.root as CheckedTreeNode

  fun getSelected(): List<UIManager.LookAndFeelInfo> =
    getSelectedThemes(root)

  private fun refreshCheckStatus(root: CheckedTreeNode) {
    when (val userObject = root.userObject) {
      is UIManager.LookAndFeelInfo -> themeCheckStatus[userObject.name] = root.isChecked
      else -> visitChildren(root) { refreshCheckStatus(it) }
    }
  }

  val isModified: Boolean
    get() = isModified(root, selectionPredicate)

  fun dispose() {
    myFilter.dispose()
  }

  private fun getAllNodes(): Stream<CheckedTreeNode> {
    val bob = Stream.builder<CheckedTreeNode>()
    traverseTree(root) {
      bob.add(it)
    }
    return bob.build()
  }

  var filter: String?
    get() = myFilter.filter
    set(filter) {
      myFilter.filter = filter
    }

  internal fun interface CheckedNodeVisitor {
    fun visit(node: CheckedTreeNode)
  }

  private inner class MyFilterComponent : FilterComponent(
    "CHARACTER_FILTER_HISTORY",
    HISTORY_LENGTH
  ) {
    private val myExpansionMonitor = TreeExpansionMonitor.install(myTree)
    override fun filter() {
      val filter = filter
      if (filter.isNullOrEmpty().not() && !myExpansionMonitor.isFreeze) {
        myExpansionMonitor.freeze()
      }
      this@PreferredLAFTree.filter(filterModel(filter, true))
      val expandedPaths = TreeUtil.collectExpandedPaths(
        myTree
      )
      (myTree.model as DefaultTreeModel).reload()
      TreeUtil.restoreExpandedPaths(myTree, expandedPaths)
      SwingUtilities.invokeLater {
        myTree.setSelectionRow(0)
        IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown {
          IdeFocusManager.getGlobalInstance().requestFocus(
            myTree,
            true
          )
        }
      }
      TreeUtil.expandAll(myTree)
      if (filter.isNullOrEmpty()) {
        TreeUtil.collapseAll(myTree, 0)
        myExpansionMonitor.restore()
      }
    }

    override fun onlineFilter() {
      val filter = filter
      if (filter != null && filter.isNotEmpty()) {
        if (!myExpansionMonitor.isFreeze) {
          myExpansionMonitor.freeze()
        }
      }
      this@PreferredLAFTree.filter(filterModel(filter, true))
      TreeUtil.expandAll(myTree)
      if (filter == null || filter.isEmpty()) {
        TreeUtil.collapseAll(myTree, 0)
        myExpansionMonitor.restore()
      }
    }
  }

  companion object {
    private const val HISTORY_LENGTH = 10
    private fun copyAndSort(intentionsToShow: List<ThemeGroupData>): List<ThemeGroupData> {
      val copy: MutableList<ThemeGroupData> = ArrayList(intentionsToShow)
      copy.sortWith { data1: ThemeGroupData, data2: ThemeGroupData ->
        data1.name.compareTo(data2.name)
      }
      return copy
    }

    private fun getNodeText(node: CheckedTreeNode): String =
      when (val userObject = node.userObject) {
        is UIManager.LookAndFeelInfo -> userObject.name
        is String -> userObject
        else -> "???"
      }

    private fun getSelectedThemes(root: CheckedTreeNode): List<UIManager.LookAndFeelInfo> {
      val selectedThemes = LinkedList<UIManager.LookAndFeelInfo>()
      traverseTree(root) {
        val userObject = it.userObject
        if (it.isChecked && userObject is UIManager.LookAndFeelInfo) {
          selectedThemes.push(userObject)
        }
      }
      return selectedThemes
    }

    private fun traverseTree(root: CheckedTreeNode, consumer: (CheckedTreeNode) -> Unit) {
      val visitQueue = LinkedList<CheckedTreeNode>()
      visitQueue.push(root)
      while (visitQueue.isNotEmpty()) {
        val current = visitQueue.pop()
        consumer(current)
        val currentChildren = current.children()
        while (currentChildren.hasMoreElements()) {
          val child = currentChildren.nextElement() as CheckedTreeNode
          visitQueue.push(child)
        }
      }
    }

    private fun isModified(
      root: CheckedTreeNode,
      selectionPredicate: Predicate<UIManager.LookAndFeelInfo>
    ): Boolean {
      val userObject = root.userObject
      return if (userObject is UIManager.LookAndFeelInfo) {
        val enabled = selectionPredicate.test(userObject)
        enabled != root.isChecked
      } else {
        val modified = booleanArrayOf(false)
        visitChildren(
          root
        ) { node: CheckedTreeNode -> modified[0] = modified[0] or isModified(node, selectionPredicate) }
        modified[0]
      }
    }

    private fun visitChildren(
      node: TreeNode,
      visitor: CheckedNodeVisitor
    ) {
      val children = node.children()
      while (children.hasMoreElements()) {
        val child = children.nextElement() as CheckedTreeNode
        visitor.visit(child)
      }
    }
  }

  init {
    initTree()
  }
}

fun UIManager.LookAndFeelInfo.isDark(): Boolean =
  if (this is UIThemeBasedLookAndFeelInfo) {
    this.theme.isDark
  } else {
    this is DarculaLookAndFeelInfo
  }
