/* $Id: SysopFrame.java 18 2009-01-27 12:16:13Z hgkuper $ */

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

package org.hyperimage.sysop.client.gui;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.hyperimage.client.ws.HiProject;
import org.hyperimage.client.ws.HiUser;
import org.hyperimage.sysop.client.HISystemOperator;
import org.hyperimage.sysop.client.utility.DecoratedHIProject;
import org.hyperimage.sysop.client.utility.DecoratedHIUser;
import org.hyperimage.sysop.client.utility.TransferableHIUser;

// TODO [HGK] When resizing frame borders, resizing the content (jscrollpanes/jpanels)

/**
 * @author Heinz-Guenter Kuper
 */
public class SysopFrame extends JFrame implements WindowListener {
	public enum DeletionType {USER, PROJECT, ADMIN}
	
	private JButton m_buttAddUser;
	private JButton m_buttDelUser;
	private JButton m_buttAddProject;
	private JButton m_buttDelProject;
	private JMenu m_menuFile;
	private JMenu m_menuEdit;
	private JMenuBar m_menubar;
	private JMenuItem m_menuitemQuit;
	private JMenuItem m_menuitemChangeSysopPasswd;
	private JPanel m_panelUsers;
	private JPanel m_panelProjects;
	private JScrollPane m_scrollpaneUsers;
	private JScrollPane m_scrollpaneProjects;
	private JTree m_treeUsers;
	private JTree m_treeProjects;
	private JPopupMenu m_popupmenuUser;
	private JPopupMenu m_popupmenuProject;
	private JMenuItem m_menuitemEditUser;
	private JMenuItem m_menuitemRemoveAdmin;

	private static HISystemOperator m_hiSysOp = null;
	private static DefaultMutableTreeNode m_nodeUserRoot = null;
	private static DefaultMutableTreeNode m_nodeProjectRoot = null;

	private static final boolean DEBUG = true;

	/** Creates new form SysopFrame */
	public SysopFrame() {
		initComponents();
		pimpComponents();
	}

	private void initComponents() {
		m_panelUsers = new JPanel();
		m_scrollpaneUsers = new JScrollPane();
		m_treeUsers = new javax.swing.JTree();
		m_buttAddUser = new JButton();
		m_buttDelUser = new JButton();
		m_panelProjects = new JPanel();
		m_scrollpaneProjects = new JScrollPane();
		m_treeProjects = new JTree();
		m_buttAddProject = new JButton();
		m_buttDelProject = new JButton();
		m_menubar = new JMenuBar();
		m_menuFile = new JMenu();
		m_menuitemQuit = new JMenuItem();
		m_menuEdit = new JMenu();
		m_menuitemChangeSysopPasswd = new JMenuItem();
		m_popupmenuUser = new JPopupMenu();
		m_popupmenuProject = new JPopupMenu();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		// User Display
		m_panelUsers.setBorder(javax.swing.BorderFactory.createTitledBorder("Users"));

		m_scrollpaneUsers.setViewportView(m_treeUsers);

		m_buttAddUser.setText("<html><font size=\"+2\">+</font></html>");
		m_buttAddUser.setToolTipText("Add user");
		m_buttAddUser.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		m_buttAddUser.setMargin(new java.awt.Insets(5, 5, 5, 5));
		m_buttAddUser.setMaximumSize(new java.awt.Dimension(32, 32));
		m_buttAddUser.setMinimumSize(new java.awt.Dimension(32, 32));
		m_buttAddUser.setPreferredSize(new java.awt.Dimension(32, 32));

		m_buttDelUser.setText("<html><font size=\"+2\">-</font><html>");
		m_buttDelUser.setToolTipText("Remove user");
		m_buttDelUser.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		m_buttDelUser.setMargin(new java.awt.Insets(5, 5, 5, 5));
		m_buttDelUser.setMaximumSize(new java.awt.Dimension(32, 32));
		m_buttDelUser.setMinimumSize(new java.awt.Dimension(32, 32));
		m_buttDelUser.setPreferredSize(new java.awt.Dimension(32, 32));

		org.jdesktop.layout.GroupLayout panellayoutUsers = new org.jdesktop.layout.GroupLayout(m_panelUsers);
		m_panelUsers.setLayout(panellayoutUsers);
		panellayoutUsers.setHorizontalGroup(
				panellayoutUsers.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(panellayoutUsers.createSequentialGroup()
						.addContainerGap()
						.add(panellayoutUsers.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								.add(m_scrollpaneUsers, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
								.add(panellayoutUsers.createSequentialGroup()
										.add(m_buttAddUser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 312, Short.MAX_VALUE)
										.add(m_buttDelUser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
										.addContainerGap())
		);
		panellayoutUsers.setVerticalGroup(
				panellayoutUsers.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(panellayoutUsers.createSequentialGroup()
						.addContainerGap()
						.add(m_scrollpaneUsers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 387, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(panellayoutUsers.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								.add(m_buttAddUser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.add(m_buttDelUser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
								.addContainerGap(35, Short.MAX_VALUE))
		);

		// Project Display
		m_panelProjects.setBorder(javax.swing.BorderFactory.createTitledBorder("Projects"));

		m_scrollpaneProjects.setViewportView(m_treeProjects);

		m_buttAddProject.setText("<html><font size=\"+2\">+</font></html>");
		m_buttAddProject.setToolTipText("Add project");
		m_buttAddProject.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		m_buttAddProject.setMargin(new java.awt.Insets(5, 5, 5, 5));
		m_buttAddProject.setMaximumSize(new java.awt.Dimension(32, 32));
		m_buttAddProject.setMinimumSize(new java.awt.Dimension(32, 32));
		m_buttAddProject.setPreferredSize(new java.awt.Dimension(32, 32));

		m_buttDelProject.setText("<html><font size=\"+2\">-</font></html>");
		m_buttDelProject.setToolTipText("Remove project");
		m_buttDelProject.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		m_buttDelProject.setMargin(new java.awt.Insets(5, 5, 5, 5));
		m_buttDelProject.setMaximumSize(new java.awt.Dimension(32, 32));
		m_buttDelProject.setMinimumSize(new java.awt.Dimension(32, 32));
		m_buttDelProject.setPreferredSize(new java.awt.Dimension(32, 32));

		org.jdesktop.layout.GroupLayout panellayoutProjects = new org.jdesktop.layout.GroupLayout(m_panelProjects);
		m_panelProjects.setLayout(panellayoutProjects);
		panellayoutProjects.setHorizontalGroup(
				panellayoutProjects.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(panellayoutProjects.createSequentialGroup()
						.addContainerGap()
						.add(panellayoutProjects.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								.add(m_scrollpaneProjects, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
								.add(panellayoutProjects.createSequentialGroup()
										.add(m_buttAddProject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 312, Short.MAX_VALUE)
										.add(m_buttDelProject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
										.addContainerGap())
		);
		panellayoutProjects.setVerticalGroup(
				panellayoutProjects.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(panellayoutProjects.createSequentialGroup()
						.addContainerGap()
						.add(m_scrollpaneProjects, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 387, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(panellayoutProjects.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
								.add(m_buttAddProject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.add(m_buttDelProject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
								.addContainerGap(35, Short.MAX_VALUE))
		);

		// Menus
		m_menuFile.setText("File");
		m_menuitemQuit.setText("Quit");
		m_menuitemQuit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if( m_hiSysOp.logout() )
					System.out.println("Logged out");
				System.exit(NORMAL);
			}
			
		});
		m_menuFile.add(m_menuitemQuit);
		m_menubar.add(m_menuFile);

		m_menuEdit.setText("Edit");
		m_menuitemChangeSysopPasswd.setText("Change Sysop Password");
		m_menuitemChangeSysopPasswd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Change the sysop passwd");
				
			}
			
		});
		m_menuEdit.add(m_menuitemChangeSysopPasswd);
		m_menubar.add(m_menuEdit);

		setJMenuBar(m_menubar);

		// Popup Menus
		m_menuitemEditUser = new JMenuItem("Edit user");
		m_menuitemEditUser.setEnabled(false);
		m_popupmenuUser.add(m_menuitemEditUser);
		
		m_menuitemRemoveAdmin = new JMenuItem("Remove admin from project");
		m_menuitemRemoveAdmin.setEnabled(false);
		m_popupmenuProject.add(m_menuitemRemoveAdmin);

		// Layout
		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
						.addContainerGap()
						.add(m_panelUsers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.add(18, 18, 18)
						.add(m_panelProjects, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
						.addContainerGap()
						.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								.add(m_panelProjects, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.add(m_panelUsers, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap())
		);

		pack();
	}

	private void pimpComponents() {
		addWindowListener(this);
		m_treeUsers.setRootVisible(false);
		m_treeUsers.setShowsRootHandles(true);
		m_treeProjects.setRootVisible(false);
		m_treeProjects.setShowsRootHandles(true);
		
		m_nodeUserRoot = new DefaultMutableTreeNode("Users"); // root should be invisible
		m_nodeProjectRoot = new DefaultMutableTreeNode("Projects"); // root should be invisible

		setupButtons();
		setupPopupMenus();
		try {
			setupDnD();
		} catch (ClassNotFoundException e) {
			// TODO [HGK] Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setupButtons() {
		m_buttAddUser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				addNewUser();
			}
		});

		m_buttDelUser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				deleteUser();
			}	
		});

		m_buttAddProject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addNewProject();
			}
		});

		m_buttDelProject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteProject();
			}
		});
	}
	
	private void setupPopupMenus() {
		// User Popup Menu
		m_menuitemEditUser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				editUser();
			}	
		});

		m_treeUsers.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// NOP
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				// NOP
			}
			@Override
			public void mouseExited(MouseEvent e) {
				// NOP
			}
			@Override
			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}
			private void showPopup(MouseEvent e) {
				TreePath currentSelection = m_treeUsers.getSelectionPath();
				if( e.isPopupTrigger() && (currentSelection != null) ) {
					DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
					if( currentNode.getUserObject() instanceof org.hyperimage.sysop.client.utility.DecoratedHIUser )
						m_menuitemEditUser.setEnabled(true);
					else
						m_menuitemEditUser.setEnabled(false);
					m_popupmenuUser.show(e.getComponent(),e.getX(), e.getY());
				}
			}
		});
		
		// Project Popup Menu
		m_menuitemRemoveAdmin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				removeAdminFromProject();
			}	
		});

		m_treeProjects.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// NOP
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				// NOP
			}
			@Override
			public void mouseExited(MouseEvent e) {
				// NOP
			}
			@Override
			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}
			private void showPopup(MouseEvent e) {
				TreePath currentSelection = m_treeProjects.getSelectionPath();
				if( e.isPopupTrigger() && (currentSelection != null) ) {
					DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
					if( currentNode.getUserObject() instanceof org.hyperimage.sysop.client.utility.DecoratedHIUser )
						m_menuitemRemoveAdmin.setEnabled(true);
					else
						m_menuitemRemoveAdmin.setEnabled(false);
					m_popupmenuProject.show(e.getComponent(),e.getX(), e.getY());
				}
			}
		});
	}

	
	private void setupDnD() throws ClassNotFoundException {
		m_treeUsers.setDragEnabled(true);
		m_treeUsers.setTransferHandler(new SysOpTransferHandler());
		m_treeProjects.setDropMode(DropMode.ON_OR_INSERT);
		m_treeProjects.setTransferHandler(new SysOpTransferHandler());
	}
	
	protected boolean assignAdminToProject(DecoratedHIUser newAdmin, DecoratedHIProject targetProj) {
		return m_hiSysOp.assignAdminToProject(newAdmin.getID(), targetProj.getID());
	}
	
	protected void removeAdminFromProject() {
		TreePath currentSelection = m_treeProjects.getSelectionPath();
		DefaultMutableTreeNode parentNode = null;
		DefaultMutableTreeNode currentNode = null;
		if( currentSelection != null ) {
			currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
			if (currentNode != null && (currentNode.getUserObject() instanceof org.hyperimage.sysop.client.utility.DecoratedHIUser)) {
				DecoratedHIUser user = (DecoratedHIUser) currentNode.getUserObject();
				// get parent to find project id
				parentNode = (DefaultMutableTreeNode) currentNode.getParent();
				DecoratedHIProject proj = (DecoratedHIProject) parentNode.getUserObject();
				if( confirmDelete(DeletionType.ADMIN, user.toString(), proj.toString()) && m_hiSysOp.removeAdminFromProject(user.getID(), proj.getID()) ) {
					// TODO [HGK] Log removal of admin.
					((DefaultTreeModel) m_treeProjects.getModel()).removeNodeFromParent(currentNode);
				}
			}
		}

		// Refresh the user tree to reflect the fact that a particular user is now no longer admin in a particular project.
		refreshUserTree();
	}

	protected void editUser() {
		TreePath currentSelection = m_treeUsers.getSelectionPath();
		if( currentSelection != null ) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
			if (currentNode != null) {
				EditUserDialog dlg = new EditUserDialog(this, true, currentNode.getUserObject(), m_hiSysOp);
				dlg.setLocationRelativeTo(this);
				dlg.setVisible(true);
			}
		}
		
		refreshTrees();
	}

	protected void addNewUser() {
		NewUserDialog dlg = new NewUserDialog(this, true, m_hiSysOp);
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);

		if( dlg.getNewlyAddedUser() != null ) {
			DefaultMutableTreeNode nodeNew = new DefaultMutableTreeNode(new DecoratedHIUser(dlg.getNewlyAddedUser()));
			// TODO [HGK] Make sure new user is inserted at lexicographically appropriate location.
			((DefaultTreeModel) m_treeUsers.getModel()).insertNodeInto(nodeNew, m_nodeUserRoot, m_nodeUserRoot.getChildCount());
			refreshTrees();
			m_treeUsers.scrollPathToVisible(new TreePath(nodeNew.getPath()));
		}
	}

	protected void deleteUser() {
		TreePath currentSelection = m_treeUsers.getSelectionPath();
		if( currentSelection != null ) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
			MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
			if (parent != null) {
				// Is the selection really a project?
				if( !(currentNode.getUserObject() instanceof org.hyperimage.sysop.client.utility.DecoratedHIUser) )
					return;
				// Confirm deletion before actually removing user.
				DecoratedHIUser user = (DecoratedHIUser) currentNode.getUserObject();
				if( confirmDelete(DeletionType.USER, user.toString()) && m_hiSysOp.delUser(user.getUserName()) ) {
					// TODO [HGK] Log deletion.
					((DefaultTreeModel) m_treeUsers.getModel()).removeNodeFromParent(currentNode);
				}
			}
		} else {
			System.out.println("Select the user to be deleted!");
		}
		
		refreshTrees();
	}

	protected void addNewProject() {
		NewProjectDialog dlg = new NewProjectDialog(this, true, m_hiSysOp);
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);

		if( dlg.getNewlyAddedProject() != null ) {
			DefaultMutableTreeNode nodeNew = new DefaultMutableTreeNode(new DecoratedHIProject(dlg.getNewlyAddedProject()));
			// TODO [HGK] Make sure new user is inserted at lexicographically appropriate location.
			((DefaultTreeModel) m_treeProjects.getModel()).insertNodeInto(nodeNew, m_nodeProjectRoot, m_nodeProjectRoot.getChildCount());
			refreshTrees();
			m_treeProjects.scrollPathToVisible(new TreePath(nodeNew.getPath()));
		}
	}

	protected void deleteProject() {
		TreePath currentSelection = m_treeProjects.getSelectionPath();
		if( currentSelection != null ) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
			MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
			if (parent != null) {
				// Is the selection really a project?
				if( !(currentNode.getUserObject() instanceof org.hyperimage.sysop.client.utility.DecoratedHIProject) )
					return;
				// Confirm deletion before actually removing project.
				DecoratedHIProject proj = (DecoratedHIProject) currentNode.getUserObject();
				if( confirmDelete(DeletionType.PROJECT, proj.toString()) && m_hiSysOp.delProject(proj.getID()) ) {
					// TODO [HGK] Log deletion.
					((DefaultTreeModel) m_treeProjects.getModel()).removeNodeFromParent(currentNode);
				}
			}
		} else {
			System.out.println("Select the project to be deleted!");
		}
		
		refreshTrees();
	}
	
	/**
	 * Returns true if the deletion has been confirmed.
	 * @param type
	 * @param strTarget
	 * @return
	 */
	private boolean confirmDelete(DeletionType type, String... strTarget) {
		String msg = "Are you sure you want to ";
		int nResult = -1;
		
		if( type.compareTo(DeletionType.PROJECT) == 0 ) {
			msg += "delete project \"" + strTarget[0] + "\"?";
		} else if( type.compareTo(DeletionType.USER) == 0 ) {
			msg += "delete user \"" + strTarget[0] + "\"?";
		} else if( type.compareTo(DeletionType.ADMIN) == 0 ) {
			msg += "remove project admin \"" + strTarget[0] + "\" from project \"" + strTarget[1] + "\"?";
		}
		
		nResult = JOptionPane.showConfirmDialog(null, msg, "Confirm Delete", JOptionPane.YES_NO_OPTION);
		return nResult == JOptionPane.YES_OPTION;
	}

	/**
	 * Must be called after SysOp for this frame has been set.
	 */
	public void populateTrees() {
		populateUserTree();
		populateProjectTree();
	}

	/**
	 * Populates tree without sysop user.
	 */
	private void populateUserTree() {
		DefaultMutableTreeNode nodeTree = null;

		List<HiUser> listUsers = m_hiSysOp.getAllUsersAlphabetically();

		for( HiUser user : listUsers ) {
			if( user.getUserName().compareTo("sysop") == 0 ) continue;	// don't add sysop to the tree

			nodeTree = new DefaultMutableTreeNode(new DecoratedHIUser(user));
			m_nodeUserRoot.add(nodeTree);
			// Get list of all projects to which this user belongs
			List<HiProject> listProjects = m_hiSysOp.getProjectsForUser(user.getId());
			for( HiProject proj: listProjects ) {
				DefaultMutableTreeNode nodeUserProject = new DefaultMutableTreeNode(new DecoratedHIProject(proj));
				nodeTree.add(nodeUserProject);
			}
		}
		m_treeUsers.setModel(new DefaultTreeModel(m_nodeUserRoot));
	}

	private void populateProjectTree() {
		DefaultMutableTreeNode nodeTree = null;

		List<HiProject> listProj = m_hiSysOp.getAllProjects();

		for( HiProject proj : listProj ) {
			nodeTree = new DefaultMutableTreeNode(new DecoratedHIProject(proj));
			m_nodeProjectRoot.add(nodeTree);
			// Get list of all users in project
			List<HiUser> listUsers = m_hiSysOp.getProjectAdminsForProject(proj.getId());
			for( HiUser user: listUsers) {
				DefaultMutableTreeNode nodeUserProject = new DefaultMutableTreeNode(new DecoratedHIUser(user));
				nodeTree.add(nodeUserProject);
			}
		}
		m_treeProjects.setModel(new DefaultTreeModel(m_nodeProjectRoot));
	}
	
	/**
	 * Better to refresh tree by getting fresh data from the service, in case e.g. a user has edited his data in the interim.
	 */
	private void refreshUserTree() {
		m_nodeUserRoot.removeAllChildren();
		populateUserTree();
	}
	
	/**
	 * Better to refresh tree by getting fresh data from the service, in case e.g. a user has edited his data in the interim.
	 */
	private void refreshProjectTree() {
		m_nodeProjectRoot.removeAllChildren();
		populateProjectTree();
	}
	
	/**
	 * Refresh all trees in frame.
	 */
	public void refreshTrees() {
		refreshUserTree();
		refreshProjectTree();
	}

	public void setSysOp(HISystemOperator hiSysOp) {
		m_hiSysOp = hiSysOp;
		populateTrees();
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// NOP
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// NOP
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if( m_hiSysOp.logout() )
			System.out.println("Logged out");	// TODO [HGK] write to log
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// NOP
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// NOP
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// NOP
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// NOP
	}
	
	/*For DnD*/
	class SysOpTransferHandler extends TransferHandler {
		private DefaultTreeModel model = (DefaultTreeModel) m_treeProjects.getModel();
		
		public int getSourceActions(JComponent c) {
			return COPY;
		}
		
		public boolean canImport(TransferHandler.TransferSupport support) {
			if( !support.isDataFlavorSupported(TransferableHIUser.userFlavour) ) {
				return false;
			}

			JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
			if( dropLocation.getPath() != null ) {
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) dropLocation.getPath().getLastPathComponent();
				return parentNode.getUserObject() instanceof org.hyperimage.sysop.client.utility.DecoratedHIProject;
			}

			return false;
		}

		public boolean importData(TransferHandler.TransferSupport support) {
			if( !canImport(support) ) {
				return false;
			}

			JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();

			TreePath path = dropLocation.getPath();

			Transferable transferable = support.getTransferable();
			
			DecoratedHIUser transferData;
			try {
				transferData = (DecoratedHIUser) transferable.getTransferData(TransferableHIUser.userFlavour);
			} catch (IOException e) {
				System.out.println("IOException");
				return false;
			} catch (UnsupportedFlavorException e) {
				System.out.println("UnsupportedFlavorException");
				return false;
			}

			int childIndex = dropLocation.getChildIndex();
			if (childIndex == -1) {
				childIndex = model.getChildCount(path.getLastPathComponent());
			}

			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(transferData);
			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)path.getLastPathComponent();
			// Add the new admin to the tree model and also to the project via Web service
			if( parentNode.getUserObject() instanceof org.hyperimage.sysop.client.utility.DecoratedHIProject ) {
				if( assignAdminToProject((DecoratedHIUser) newNode.getUserObject(), (DecoratedHIProject) parentNode.getUserObject()) )
					model.insertNodeInto(newNode, parentNode, childIndex);
				else
					return false;
			}

			// Ensure the new addition is visible.
			TreePath newPath = null;
			if( newNode != null )
				newPath = path.pathByAddingChild(newNode);
			if( newPath != null ) {
				m_treeProjects.makeVisible(newPath);
				m_treeProjects.scrollRectToVisible(m_treeProjects.getPathBounds(newPath));
			}

			return true;
		}

		protected Transferable createTransferable(JComponent c) {
			if( c == m_treeUsers ) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) m_treeUsers .getSelectionPath().getLastPathComponent();
				DecoratedHIUser selectedUser = (DecoratedHIUser) selectedNode.getUserObject();
				return new TransferableHIUser(selectedUser);
			}
			return null;
		}
	}
}
