package io.unthrottled.theme.randomizer.config.ui

import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.ide.TreeExpander
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.search.SearchUtil
import com.intellij.ide.ui.search.SearchableOptionsRegistrar
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.packageDependencies.ui.TreeExpansionMonitor
import com.intellij.ui.CheckboxTree
import com.intellij.ui.CheckboxTree.CheckboxTreeCellRenderer
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.FilterComponent
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import io.unthrottled.theme.randomizer.tools.toOptional
import java.awt.BorderLayout
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedList
import java.util.function.Predicate
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

data class CharacterData(
  val lookAndFeelInfo: UIManager.LookAndFeelInfo
)

class PreferredCharacterTree(
  private val selectionPredicate: Predicate<UIManager.LookAndFeelInfo>
) {
  private val characterCheckStatus: MutableMap<String, Boolean> = HashMap()
  val component: JComponent = JPanel(BorderLayout())
  private val myTree: CheckboxTree = createTree()
  private val myFilter: FilterComponent = MyFilterComponent()
  private val toolbarPanel: JPanel = JPanel(BorderLayout())

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
    component.add(toolbarPanel, BorderLayout.NORTH)
    component.add(scrollPane, BorderLayout.CENTER)
    myFilter.reset()
    reset(copyAndSort(getCharacterList()))
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
            if (value.userObject is CharacterData) SimpleTextAttributes.REGULAR_ATTRIBUTES
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

  fun filterModel(filter: String?, force: Boolean): List<CharacterData> {
    val list: List<CharacterData> = getCharacterList()
    if (filter.isNullOrEmpty()) {
      return list
    }

    var result: List<CharacterData> =
      getCharacterList {
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

  fun filter(intentionsToShow: List<CharacterData>) {
    refreshCheckStatus(myTree.model.root as CheckedTreeNode)
    reset(copyAndSort(intentionsToShow))
  }

  fun reset() {
    characterCheckStatus.clear()
    reset(copyAndSort(getCharacterList()))
  }

  private fun getCharacterList(predicate: (UIManager.LookAndFeelInfo) -> Boolean = { true }) =
    LafManager.getInstance().installedLookAndFeels
      .filter(predicate)
      .map { CharacterData(it) }
      .sortedBy { it.lookAndFeelInfo.name }

  private fun reset(sortedCharacterData: List<CharacterData>) {
    val root = CheckedTreeNode(null)
    val treeModel = myTree.model as DefaultTreeModel
    sortedCharacterData.forEach { characterData ->
      val animeRoot = CheckedTreeNode(characterData.lookAndFeelInfo)
      treeModel.insertNodeInto(animeRoot, root, root.childCount)
    }
    treeModel.setRoot(root)
    treeModel.nodeChanged(root)
    TreeUtil.expandAll(myTree)
    myTree.setSelectionRow(0)
  }

  private val root: CheckedTreeNode
    get() = myTree.model.root as CheckedTreeNode

  fun getSelected(): List<UIManager.LookAndFeelInfo> =
    getSelectedCharacters(root)

  private fun refreshCheckStatus(root: CheckedTreeNode) {
    when (val userObject = root.userObject) {
      is UIManager.LookAndFeelInfo -> characterCheckStatus[userObject.name] = root.isChecked
      else -> visitChildren(root) { refreshCheckStatus(it) }
    }
  }

  val isModified: Boolean
    get() = isModified(root, selectionPredicate)

  fun dispose() {
    myFilter.dispose()
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
      this@PreferredCharacterTree.filter(filterModel(filter, true))
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
      this@PreferredCharacterTree.filter(filterModel(filter, true))
      TreeUtil.expandAll(myTree)
      if (filter == null || filter.isEmpty()) {
        TreeUtil.collapseAll(myTree, 0)
        myExpansionMonitor.restore()
      }
    }
  }

  companion object {
    private const val HISTORY_LENGTH = 10
    private fun copyAndSort(intentionsToShow: List<CharacterData>): List<CharacterData> {
      val copy: MutableList<CharacterData> = ArrayList(intentionsToShow)
      copy.sortWith { data1: CharacterData, data2: CharacterData ->
        data1.lookAndFeelInfo.name.compareTo(data2.lookAndFeelInfo.name)
      }
      return copy
    }

    private fun getNodeText(node: CheckedTreeNode): String =
      when (val userObject = node.userObject) {
        is UIManager.LookAndFeelInfo -> userObject.name
        else -> "???"
      }

    private fun getSelectedCharacters(root: CheckedTreeNode): List<UIManager.LookAndFeelInfo> {
      val visitQueue = LinkedList<CheckedTreeNode>()
      visitQueue.push(root)
      val selectedCharacters = LinkedList<UIManager.LookAndFeelInfo>()
      while (visitQueue.isNotEmpty()) {
        val current = visitQueue.pop()
        val userObject = current.userObject
        if (current.isChecked && userObject is UIManager.LookAndFeelInfo) {
          selectedCharacters.push(userObject)
        }
        val currentChildren = current.children()
        while (currentChildren.hasMoreElements()) {
          val child = currentChildren.nextElement() as CheckedTreeNode
          visitQueue.push(child)
        }
      }
      return selectedCharacters
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
