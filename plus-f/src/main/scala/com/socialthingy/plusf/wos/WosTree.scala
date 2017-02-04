package com.socialthingy.plusf.wos

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{BorderLayout, Dimension, FlowLayout, Frame}
import java.util.Optional
import javax.swing._
import javax.swing.event.{TreeExpansionEvent, TreeSelectionEvent, TreeSelectionListener, TreeWillExpandListener}
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel}

import com.socialthingy.plusf.spectrum.ui.ProgressDialog

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object WosTree extends App {
  val dialog = new WosTree
  dialog.setSize(new Dimension(500, 600))
  dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
  println(dialog.selectArchive)
}

class WosTree(owner: Frame = null) extends JDialog(owner) {
  def selectArchive: Optional[Archive] = {
    setVisible(true)
    dispose()
    selected
  }

  private val wosScraper = WosScraper("www.worldofspectrum.org")
  private val rootNode = new DefaultMutableTreeNode("Search Results")
  private val tree = new JTree(rootNode, true)
  private val searchBox = new JTextField()
  private val searchButton = new JButton("Search")
  private val searchPanel = new JPanel(new BorderLayout())
  private val loadButton = new JButton("Load")
  loadButton.setEnabled(false)
  private val cancelButton = new JButton("Cancel")
  private val loadPanel = new JPanel(new FlowLayout())

  private var selected: Optional[Archive] = Optional.empty()

  tree.setShowsRootHandles(true)
  tree.addTreeWillExpandListener(ArchiveLazyLoader)
  tree.addTreeSelectionListener(LoadButtonActivator)

  searchPanel.add(searchBox, BorderLayout.CENTER)
  searchPanel.add(searchButton, BorderLayout.LINE_END)

  loadPanel.add(cancelButton)
  loadPanel.add(loadButton)

  setModal(true)
  setTitle("Search WOS Archive")
  getContentPane.setLayout(new BorderLayout())
  getContentPane.add(searchPanel, BorderLayout.PAGE_START)
  getContentPane.add(new JScrollPane(tree), BorderLayout.CENTER)
  getContentPane.add(loadPanel, BorderLayout.PAGE_END)

  searchBox.addActionListener(Searcher)
  searchButton.addActionListener(Searcher)
  loadButton.addActionListener(LoadListener)
  cancelButton.addActionListener(CancelListener)

  object LoadListener extends ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = {
      selected = Optional.of(
        tree.getSelectionPath.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode]
          .getUserObject
          .asInstanceOf[Archive]
      )

      setVisible(false)
    }
  }

  object CancelListener extends ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = {
      setVisible(false)
    }
  }

  object Searcher extends ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = {
      if (searchBox.getText.length >= 3) {
        rootNode.removeAllChildren()

        val titles = Future { wosScraper.findTitles(searchBox.getText) }
        val progressDialog = new ProgressDialog(
          WosTree.this,
          "Searching",
          () => ()
        )
        progressDialog.setMessage(s"""Searching WOS for "${searchBox.getText}..."""")
        progressDialog.setVisible(true)

        titles.onComplete { result =>
          if (!progressDialog.wasCancelled()) onEventThread {
            progressDialog.close()
            handleResult(result)
          }
        }
      }
    }

    private def handleResult(result: Try[Seq[Title]]) = result match {
        case Success(t) if t.isEmpty => rootNode.setUserObject("No matches found")

        case Success(t) =>
          rootNode.setUserObject("Search results")
          t.foreach(t => rootNode.add(new DefaultMutableTreeNode(t, true)))
          tree.getModel.asInstanceOf[DefaultTreeModel].reload(rootNode)
          tree.expandRow(0)

        case Failure(f) =>
          rootNode.setUserObject("Search error, please try again")
    }
  }

  object ArchiveLazyLoader extends TreeWillExpandListener {
    override def treeWillExpand(event: TreeExpansionEvent): Unit = {
      val selected = event.getPath.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode]
      selected.getUserObject match {
        case t: Title => loadArchives(selected, t)
        case _ =>
      }
    }

    override def treeWillCollapse(event: TreeExpansionEvent): Unit = {}

    private def loadArchives(selectedNode: DefaultMutableTreeNode, title: Title) = {
      val archives = Future { wosScraper.findArchives(title) }
      val progressDialog = new ProgressDialog(
        WosTree.this,
        "Searching",
        () => ()
      )
      progressDialog.setMessage(s"""Finding archives for "${title.name}..."""")
      progressDialog.setVisible(true)

      archives.onComplete { result =>
        if (!progressDialog.wasCancelled()) onEventThread {
          progressDialog.close()
          handleResult(selectedNode, result)
        }
      }
    }

    private def handleResult(selectedNode: DefaultMutableTreeNode, result: Try[Seq[Archive]]) = {
      selectedNode.removeAllChildren()
      result match {
        case Success(archives) =>
          archives.foreach(a => selectedNode.add(new DefaultMutableTreeNode(a, false)))
          tree.getModel.asInstanceOf[DefaultTreeModel].reload(selectedNode)

        case _ =>
          selectedNode.add(new DefaultMutableTreeNode("No suitable archives found"))
          tree.getModel.asInstanceOf[DefaultTreeModel].reload(selectedNode)
      }
    }
  }

  object LoadButtonActivator extends TreeSelectionListener {
    override def valueChanged(e: TreeSelectionEvent): Unit = {
      val selected = e.getPath.getLastPathComponent.asInstanceOf[DefaultMutableTreeNode]
      loadButton.setEnabled(selected.getUserObject.isInstanceOf[Archive])
    }
  }

  private def onEventThread(code: => Any) = SwingUtilities.invokeLater(() => code)
}