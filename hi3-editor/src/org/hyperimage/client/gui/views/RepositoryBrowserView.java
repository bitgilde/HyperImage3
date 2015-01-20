/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/HYPERIMAGE.LICENSE
 * or http://www.sun.com/cddl/cddl.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/HYPERIMAGE.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2009 Humboldt-Universitaet zu Berlin
 * All rights reserved.  Use is subject to license terms.
 */

package org.hyperimage.client.gui.views;

import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.hyperimage.client.Messages;
import org.hyperimage.client.model.HIRepository;
import org.hyperimage.client.ws.HiRepository;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class RepositoryBrowserView extends GUIView {

	private static final long serialVersionUID = 3456992248005430602L;

	
	private JPanel browserPanel;
	private JButton addButton;
    private JPanel browserTreePanel;
    private JPanel controlPanel;
    private JButton removeButton;
    private JScrollPane repoTreeScroll;
    private JTree repositoryTree;	
    
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
		
	private Vector<HIRepository> repositories;
	

    
	public RepositoryBrowserView(List<HiRepository> modelRepositories) {
		super(Messages.getString("RepositoryBrowserView.0")); //$NON-NLS-1$

		initComponents();

		setDisplayPanel(browserPanel);

		// init repositories
		repositories = new Vector<HIRepository>();
		
		for ( HiRepository repository : modelRepositories ) {
			HIRepository rep = new HIRepository(repository);
			repositories.addElement(rep);
			
			treeModel.insertNodeInto(
					new DefaultMutableTreeNode(rep), 
					rootNode, 
					0);
		}
		
	}

    public void addRepository(HiRepository repos) {
        HIRepository rep = new HIRepository(repos);
        repositories.addElement(rep);
		treeModel.insertNodeInto(
				new DefaultMutableTreeNode(rep),
				rootNode, repositories.size()-1
		);

    }
    
    public void removeRepository(HIRepository repos) {
    	int index = repositories.indexOf(repos);
    	if ( index < 0 ) return;

    	// remove all children
    	((DefaultMutableTreeNode)treeModel.getRoot()).removeAllChildren();
    	repositories.remove(repos);
    	// re-populate tree
    	for ( HIRepository rep : repositories ) {
			treeModel.insertNodeInto(
					new DefaultMutableTreeNode(rep), 
					rootNode, 
					0);
		}
    	
    	treeModel.reload();
    	repositoryTree.repaint();
    }
    
    
	public JTree getRepositoryTree() {
		return repositoryTree;
	}
	
	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}
	
	public Vector<HIRepository> getRepositories() {
		return this.repositories;
	}

	

	public void setSelectedIndex(int index) {
		// sanity check
		if ( index >= repositories.size() )
			return;
		
		if ( index < 0 ) {
			repositoryTree.setSelectionPath(null);
			return;
		}
		
		DefaultMutableTreeNode[] pathToRep = new DefaultMutableTreeNode[2];		
		pathToRep[0] = rootNode;
		pathToRep[1] = (DefaultMutableTreeNode) treeModel.getChild(null, index);
		TreePath path = new TreePath(pathToRep);
		
		repositoryTree.setSelectionPath(path);
	}
	
	public int getSelectedIndex() {
		if ( repositoryTree.getSelectionPath() == null )
			return -1;
		
		for ( int i = 0; i < repositories.size() ; i++ ) {
			if ( ((DefaultMutableTreeNode)(repositoryTree.getSelectionPath().getPath()[1])).getUserObject() == repositories.get(i) )
				return i;
		}
		
		return -1;
	}


    public JButton getAddButton() {
        return this.addButton;
    }
	
    public JButton getRemoveButton() {
        return this.removeButton;
    }
	
	
	private void initComponents() {
		browserPanel = new JPanel();
        browserTreePanel = new JPanel();
        repoTreeScroll = new JScrollPane();
        repositoryTree = new JTree();
        controlPanel = new JPanel();
        addButton = new JButton();
        removeButton = new JButton();

        repoTreeScroll.setViewportView(repositoryTree);

        GroupLayout browserTreePanelLayout = new GroupLayout(browserTreePanel);
        browserTreePanel.setLayout(browserTreePanelLayout);
        browserTreePanelLayout.setHorizontalGroup(
            browserTreePanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(repoTreeScroll, GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
        );
        browserTreePanelLayout.setVerticalGroup(
            browserTreePanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(repoTreeScroll, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        addButton.setActionCommand("addRep"); //$NON-NLS-1$
        addButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        addButton.setPreferredSize(new java.awt.Dimension(24, 24));

        removeButton.setActionCommand("removeRep"); //$NON-NLS-1$
        removeButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        removeButton.setPreferredSize(new java.awt.Dimension(24, 24));

        GroupLayout controlPanelLayout = new GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
                controlPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(controlPanelLayout.createSequentialGroup()
                    .add(addButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(removeButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(150, Short.MAX_VALUE))
            );
            controlPanelLayout.setVerticalGroup(
                controlPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(controlPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(addButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(removeButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            );
            
            
        GroupLayout browserPanelLayout = new GroupLayout(browserPanel);
        browserPanel.setLayout(browserPanelLayout);
        browserPanelLayout.setHorizontalGroup(
            browserPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(browserPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(browserPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.TRAILING, browserTreePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.TRAILING, controlPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        browserPanelLayout.setVerticalGroup(
            browserPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, browserPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(browserTreePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(controlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        
        // -----
        
		addButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add.png"))); //$NON-NLS-1$
		addButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-active.png"))); //$NON-NLS-1$
		addButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-disabled.png"))); //$NON-NLS-1$
		addButton.setToolTipText(Messages.getString("RepositoryBrowserView.3")); //$NON-NLS-1$
		addButton.setEnabled(true);

		removeButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove.png"))); //$NON-NLS-1$
		removeButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-active.png"))); //$NON-NLS-1$
		removeButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-disabled.png"))); //$NON-NLS-1$
		removeButton.setToolTipText(Messages.getString("RepositoryBrowserView.4")); //$NON-NLS-1$
		removeButton.setEnabled(false);
		
		// init tree
		rootNode = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(rootNode, true);
		repositoryTree.setModel(treeModel);
		repositoryTree.setRootVisible(false);
		repositoryTree.setScrollsOnExpand(true);
		repositoryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		

	}


}
