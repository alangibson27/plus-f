package com.socialthingy.plusf.wos

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Frame
import java.util.Optional
import javax.swing.*
import javax.swing.event.*
import javax.swing.tree.*

import com.socialthingy.plusf.spectrum.ui.ProgressDialog
import java.util.concurrent.CompletableFuture

class WosTree(owner: Frame? = null) : JDialog(owner) {
    fun selectArchive(): Optional<Archive> {
        isVisible = true
        dispose()
        return selected
    }

    private val wosScraper = WosScrapers.new("www.worldofspectrum.org")
    private val rootNode = DefaultMutableTreeNode("Search Results")
    private val tree = JTree(rootNode, true)
    private val searchBox = JTextField()
    private val searchButton = JButton("Search")
    private val searchPanel = JPanel(BorderLayout())
    private val loadButton = JButton("Load")
    private val cancelButton = JButton("Cancel")
    private val loadPanel = JPanel(FlowLayout())

    private var selected: Optional<Archive> = Optional.empty()

    init {
        loadButton.isEnabled = false
        tree.showsRootHandles = true
        tree.addTreeWillExpandListener(ArchiveLazyLoader())
        tree.addTreeSelectionListener(LoadButtonActivator())

        searchPanel.add(searchBox, BorderLayout.CENTER)
        searchPanel.add(searchButton, BorderLayout.LINE_END)

        loadPanel.add(cancelButton)
        loadPanel.add(loadButton)

        isModal = true
        title = "Search WOS Archive"
        contentPane.layout = BorderLayout()
        contentPane.add(searchPanel, BorderLayout.PAGE_START)
        contentPane.add(JScrollPane(tree), BorderLayout.CENTER)
        contentPane.add(loadPanel, BorderLayout.PAGE_END)

        searchBox.addActionListener(Searcher())
        searchButton.addActionListener(Searcher())
        loadButton.addActionListener(LoadListener())
        cancelButton.addActionListener(CancelListener())
    }

    inner class LoadListener : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            selected = Optional.of(
                    (tree.selectionPath.lastPathComponent as DefaultMutableTreeNode).userObject as Archive
            )

            isVisible = false
        }
    }

    inner class CancelListener : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            isVisible = false
        }
    }

    inner class Searcher : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            if (searchBox.text.length >= 3) {
                rootNode.removeAllChildren()

                val progressDialog = ProgressDialog(
                        this@WosTree,
                        "Searching"
                ) {}

                CompletableFuture.supplyAsync { wosScraper.findTitles(searchBox.text) }
                        .thenAccept { result ->
                            if (!progressDialog.wasCancelled()) {
                                onEventThread {
                                    progressDialog.close()
                                    handleResult(result)
                                }
                            }
                        }

                progressDialog.setMessage(String.format("Searching WOS for \"%s\"...", searchBox.text))
                progressDialog.isVisible = true
            }
        }

        private fun handleResult(result: List<Title>) {
            if (result.isEmpty()) {
                rootNode.userObject = "No matches found"
            } else {
                rootNode.userObject = "Search results"
                result.forEach { rootNode.add(DefaultMutableTreeNode(it, true)) }
                (tree.model as DefaultTreeModel).reload(rootNode)
                tree.expandRow(0)
            }
        }
    }

    inner class ArchiveLazyLoader : TreeWillExpandListener {
        override fun treeWillExpand(event: TreeExpansionEvent) {
            val selected = event.path.lastPathComponent as DefaultMutableTreeNode
            if (selected.userObject is Title) {
                loadArchives(selected, selected.userObject as Title)
            }
        }

        override fun treeWillCollapse(event: TreeExpansionEvent) {}

        private fun loadArchives(selectedNode: DefaultMutableTreeNode, title: Title) {
//      val archives = Future { wosScraper.findArchives(title) }
            val progressDialog = ProgressDialog(
                    this@WosTree,
                    "Searching"
            ) {}

            CompletableFuture.supplyAsync { wosScraper.findArchives(title) }
                    .thenAccept { result ->
                        if (!progressDialog.wasCancelled()) {
                            onEventThread {
                                progressDialog.close()
                                handleResult(selectedNode, result)
                            }
                        }
                    }

            progressDialog.setMessage(String.format("Finding archives for \"%s\"...", title.name))
            progressDialog.isVisible = true

        }

        private fun handleResult(selectedNode: DefaultMutableTreeNode, result: List<Archive>) {
            selectedNode.removeAllChildren()
            if (result.isNotEmpty()) {
                result.forEach { selectedNode.add(DefaultMutableTreeNode(it, false)) }
                (tree.model as DefaultTreeModel).reload(selectedNode)
            } else {
                selectedNode.add(DefaultMutableTreeNode("No suitable archives found"))
                (tree.model as DefaultTreeModel).reload(selectedNode)
            }
        }
    }

    inner class LoadButtonActivator : TreeSelectionListener {
        override fun valueChanged(e: TreeSelectionEvent) {
            val selected = e.path.lastPathComponent as DefaultMutableTreeNode
            loadButton.isEnabled = selected.userObject is Archive
        }
    }

    private fun onEventThread(code: () -> Unit) = SwingUtilities.invokeLater(code)
}