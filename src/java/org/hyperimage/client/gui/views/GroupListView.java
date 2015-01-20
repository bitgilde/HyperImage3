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

/*
 * Copyright 2014 Leuphana Universität Lüneburg
 * All rights reserved.  Use is subject to license terms.
 */

package org.hyperimage.client.gui.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.lists.GroupListCellRenderer;
import org.hyperimage.client.ws.HiGroup;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class GroupListView extends GUIView implements ListSelectionListener {
	
	private static final long serialVersionUID = 3913008821465269712L;


	private JButton createGroupButton;
	private JList groupList;
	private DefaultListModel groupListModel;
	private JScrollPane groupListScrollPane;
	private JButton removeGroupButton;
	private JPanel groupListPanel;	
	private HiGroup importGroup;
	private HiGroup trashGroup;
	private Vector<HiGroup> groups;

	// popup menu
	private JPopupMenu popupMenu;
	private JMenuItem titleItem;
	private JMenuItem createGroupMenuItem;
	private JMenuItem removeGroupMenuItem;
	

	public GroupListView(HiGroup importGroup, HiGroup trashGroup, String defaultLang) {
		super(new Color(0x21, 0x49, 0x8A));
		
		this.importGroup = importGroup;
		this.trashGroup = trashGroup;
		this.groups = new Vector<HiGroup>();
		
		initComponents();
		updateLanguage();
		
		groupList.setCellRenderer(new GroupListCellRenderer(importGroup, trashGroup, defaultLang));

		// DnD
		groupList.setDragEnabled(true);
		groupList.setDropMode(DropMode.ON_OR_INSERT);

		// attach listeners
		groupList.addListSelectionListener(this);

		// fill groups list
		setGroups();
		
		this.setDisplayPanel(groupListPanel);
	}
	
	public void updateContent() {
		groupList.repaint();
	}
	
	
	public void updateLanguage() {
		menuTitle.setText(Messages.getString("GroupListView.0")); //$NON-NLS-1$
		
		createGroupButton.setToolTipText(Messages.getString("GroupListView.1")); //$NON-NLS-1$
		removeGroupButton.setToolTipText(Messages.getString("GroupListView.2")); //$NON-NLS-1$
		titleItem.setText(Messages.getString("GroupListView.3")); //$NON-NLS-1$
		createGroupMenuItem.setText(Messages.getString("GroupListView.4")); //$NON-NLS-1$
		removeGroupMenuItem.setText(Messages.getString("GroupListView.5")); //$NON-NLS-1$		
	}
	
	
	public void setGroups(List<HiGroup> groups)  {
		this.groups.removeAllElements();
		for ( HiGroup group : groups )
			this.groups.addElement(group);
		
		setGroups();
	}
	
	public void resetAllGroups(HiGroup importGroup, HiGroup trashGroup, List<HiGroup> groups) {
		this.importGroup = importGroup;
		this.trashGroup = trashGroup;
		((GroupListCellRenderer)this.groupList.getCellRenderer()).resetGroups(importGroup, trashGroup);
		setGroups(groups);
		this.groupList.setSelectedIndex(0);
	}
	
	private void setGroups() {
		// preserve selected group
		int selectedIndex = -1;
		HiGroup selGroup = null;
		if ( groupListModel.size() > 0 ) { 
			selectedIndex = groupList.getSelectedIndex();
			if ( selectedIndex >= 0 ) selGroup = (HiGroup) groupListModel.get(selectedIndex);
		}
		
		// temporarily remove list listeners
		ListSelectionListener listeners[] = this.groupList.getListeners(ListSelectionListener.class);
		for ( ListSelectionListener listener : listeners )
			this.groupList.removeListSelectionListener(listener);
		
		groupListModel.removeAllElements();
		// first group is always the import group
		groupListModel.addElement(importGroup);
		// add project groups to GUI list
		for ( HiGroup group : groups )
			groupListModel.addElement(group);
		// last group is always the trash group
		if ( System.getProperty("HI.feature.trashDisabled") == null ) groupListModel.addElement(trashGroup);
		
		// try to find selected group again
		if ( selectedIndex >=0 ) for ( int i=0; i < groupListModel.size(); i++ )
			if ( ((HiGroup)groupListModel.get(i)).getId() == selGroup.getId() )
				selectedIndex = i;

		groupList.setSelectedIndex(-1);
		// reinstate listeners
		for ( ListSelectionListener listener : listeners )
			this.groupList.addListSelectionListener(listener);

		if ( selectedIndex >= 0 && selectedIndex < groupListModel.size() ) 
			groupList.setSelectedIndex(selectedIndex);
		else groupList.setSelectedIndex(0);
		
		if ( groupList.getSelectedIndex() == 0 || groupList.getSelectedIndex() == (groupListModel.size()-1) )
			removeGroupButton.setEnabled(false);
		else removeGroupButton.setEnabled(true);

	}
	

	public JList getList() {
		return this.groupList;
	}
	
	private void initComponents() {

		groupListPanel = new JPanel();
		createGroupButton = new JButton();
		removeGroupButton = new JButton();
		groupListScrollPane = new JScrollPane();
		groupList = new JList();
		// set model
		groupListModel = new DefaultListModel();
		groupList.setModel(groupListModel);
		groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		createGroupButton.setBorder(BorderFactory.createEmptyBorder());
		createGroupButton.setPreferredSize(new Dimension(24, 24));

		removeGroupButton.setBorder(BorderFactory.createEmptyBorder());
		removeGroupButton.setOpaque(true);
		removeGroupButton.setPreferredSize(new Dimension(24, 24));

		groupListScrollPane.setViewportView(groupList);

		GroupLayout groupListPanelLayout = new GroupLayout(groupListPanel);
		groupListPanel.setLayout(groupListPanelLayout);
		groupListPanelLayout.setHorizontalGroup(
				groupListPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(groupListPanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(groupListPanelLayout.createParallelGroup(GroupLayout.LEADING)
								.add(groupListPanelLayout.createSequentialGroup()
										.add(createGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.RELATED)
										.add(removeGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.add(GroupLayout.TRAILING, groupListScrollPane, GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE))
										.addContainerGap())
		);
		groupListPanelLayout.setVerticalGroup(
				groupListPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(GroupLayout.TRAILING, groupListPanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(groupListScrollPane, GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.RELATED)
						.add(groupListPanelLayout.createParallelGroup(GroupLayout.BASELINE)
								.add(createGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.add(removeGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addContainerGap())
		);
		
		// ------

		createGroupButton.setActionCommand("create"); //$NON-NLS-1$
		createGroupButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add.png"))); //$NON-NLS-1$
		createGroupButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-active.png"))); //$NON-NLS-1$
		createGroupButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-disabled.png"))); //$NON-NLS-1$
		removeGroupButton.setActionCommand("remove"); //$NON-NLS-1$
		removeGroupButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove.png"))); //$NON-NLS-1$
		removeGroupButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-active.png"))); //$NON-NLS-1$
		removeGroupButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-disabled.png"))); //$NON-NLS-1$

		// popup menu
		popupMenu = new JPopupMenu();
		titleItem = new JMenuItem();
		titleItem.setEnabled(false);
		popupMenu.add(titleItem);
		popupMenu.add(new JSeparator());
		createGroupMenuItem = new JMenuItem();
		createGroupMenuItem.setActionCommand("create"); //$NON-NLS-1$
		popupMenu.add(createGroupMenuItem);
		createGroupMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/create-menu.png"))); //$NON-NLS-1$
		removeGroupMenuItem = new JMenuItem();
		removeGroupMenuItem.setActionCommand("remove"); //$NON-NLS-1$
		popupMenu.add(removeGroupMenuItem);
	}

	public HiGroup getCurrentGroup() {
		if ( groupList.getSelectedIndex() == -1 )
			return null;
		if ( groupList.getSelectedIndex() == 0 )
			return importGroup;
		else if ( groupList.getSelectedIndex() == (groupListModel.size()-1) )
			return trashGroup;
		else return (HiGroup) groupListModel.get(groupList.getSelectedIndex());
	}
	
	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	public void showPopupMenu(int x, int y) {
		// TODO: adjust popup menu location to scrollpane viewport
		popupMenu.show(this, x, y+10);
	}

	public void attachActionListeners(ActionListener listener) {
		createGroupButton.addActionListener(listener);
		removeGroupButton.addActionListener(listener);
		createGroupMenuItem.addActionListener(listener);
		removeGroupMenuItem.addActionListener(listener);
	}
	
	public HiGroup getGroup(int index) {
		return (HiGroup) groupListModel.get(index);
	}

	public HiGroup getGroup(HiGroup refGroup) {
		HiGroup foundGroup = refGroup;
		
		for (int i = 0; i < groupListModel.getSize(); i++)
			if ( ((HiGroup)groupListModel.get(i)).getId() == refGroup.getId() )
				foundGroup = (HiGroup)groupListModel.get(i);
		
		return foundGroup;
	}

	public void setSelectedGroup(HiGroup group) {
		for ( int i=0; i < groupListModel.getSize(); i++ )
			if ( ((HiGroup)groupListModel.get(i)).getId() == group.getId() )
				groupList.setSelectedIndex(i);		
	}

	public void updateGroup(HiGroup group) {
		for ( int i=0; i < groupListModel.size() ; i++ )
			if ( ((HiGroup)groupListModel.getElementAt(i)).getId() == group.getId() )
				groupListModel.setElementAt(group, i);
		updateContent();
	}
	
	
	// ------------------------------------------------------------------------------------------
	

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if ( groupList.getSelectedIndex() == 0 || groupList.getSelectedIndex() == (groupListModel.size()-1) ) {
			removeGroupButton.setEnabled(false);
			removeGroupMenuItem.setEnabled(false);
		} else {
			removeGroupButton.setEnabled(true);
			removeGroupMenuItem.setEnabled(true);
		}
	}

}
