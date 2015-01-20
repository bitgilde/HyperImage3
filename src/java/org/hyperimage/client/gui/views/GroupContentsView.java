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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.OptionButton;
import org.hyperimage.client.gui.lists.GroupContentsCellRenderer;
import org.hyperimage.client.gui.lists.GroupContentsList;
import org.hyperimage.client.ws.GroupTypes;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiQuickInfo;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Jens-Martin Loebel
 */
public class GroupContentsView extends GUIView implements MouseListener {

	private static final long serialVersionUID = -5222432024095462853L;

	
	private JPanel groupContentsPanel;
	private JPanel groupContentsInfoPanel;
	private JPanel controlPanel;
	private GroupContentsList groupContentsList;
	private JScrollPane listScrollPane;
	private JLabel elementCountLabel;
	private JButton listStyleButton;
	public OptionButton optionsButton;
	private JProgressBar loadingIndicator;
	private DefaultListModel model;
	private List<HiQuickInfo> contents;
	
	private HiGroup currentGroup = null;
	
	// popup menu options
	private JPopupMenu popupMenu;
	private JMenu elementCreationMenu;
	private JMenuItem createURLMenuItem;
	private JMenuItem createTextMenuItem;
	private JMenuItem createLightTableMenuItem;
	private JMenuItem createObjectMenuItem;
	private JMenuItem newGroupFromSelectionMenuItem;
	private JMenuItem removeSelectionMenuItem;
	private JMenuItem moveSelectionToTrashMenuItem;
	private ImageIcon trashIcon;
	private ImageIcon deleteIcon;
	public ImageIcon iconStyleIcon;
	public ImageIcon listStyleIcon;
	
	
	private int elementCount = 0;
	
	
	public GroupContentsView() {
		super(new Color(0x21, 0x49, 0x8A));
		groupContentsList = new GroupContentsList();
		model = new DefaultListModel();
		groupContentsList.setModel(model);

		initComponents();
		updateLanguage();
		
		populatePopupMenu(); // init popup menu
		
		// DnD
		groupContentsList.setDragEnabled(true);
		groupContentsList.setDropMode(DropMode.INSERT);

		// attach listeners
		groupContentsList.addMouseListener(this);
		
		setDisplayPanel(groupContentsPanel);
	}
	
	
	public void updateLanguage() {
		menuTitle.setText(Messages.getString("GroupContentsView.0")); //$NON-NLS-1$

		// popup menu
    	elementCreationMenu.setText(Messages.getString("GroupContentsView.12")); //$NON-NLS-1$
    	createURLMenuItem.setText(Messages.getString("GroupContentsView.13")); //$NON-NLS-1$
    	createTextMenuItem.setText(Messages.getString("GroupContentsView.15")); //$NON-NLS-1$
    	createLightTableMenuItem.setText(Messages.getString("GroupContentsView.17")); //$NON-NLS-1$
    	createObjectMenuItem.setText(Messages.getString("GroupContentsView.19")); //$NON-NLS-1$
    	newGroupFromSelectionMenuItem.setText(Messages.getString("GroupContentsView.21")); //$NON-NLS-1$
    	removeSelectionMenuItem.setText(Messages.getString("GroupContentsView.23")); //$NON-NLS-1$
    	moveSelectionToTrashMenuItem.setText(Messages.getString("GroupContentsView.25")); //$NON-NLS-1$
        optionsButton.updateLanguage();
		if ( currentGroup != null ) {
                    setPopupMenuState();
                    ((GroupContentsCellRenderer)groupContentsList.getCellRenderer()).updateLanguage();
                }
		
		// element count
		if ( loadingIndicator.isVisible() ) elementCountLabel.setText(Messages.getString("GroupContentsView.29")); //$NON-NLS-1$
		else {
			if ( elementCount >= 0 ) {
				if ( elementCount != 1 ) elementCountLabel.setText(elementCount+Messages.getString("GroupContentsView.30")); //$NON-NLS-1$
				else elementCountLabel.setText(Messages.getString("GroupContentsView.31")); //$NON-NLS-1$
			} else elementCountLabel.setText(Messages.getString("GroupContentsView.32")); //$NON-NLS-1$
		}
	}
	
	
	public List<HiQuickInfo> getContents() {
		if ( contents == null ) {
			contents = new ArrayList<HiQuickInfo>();
			for ( int i=0 ; i < model.getSize(); i++ )
				contents.add((HiQuickInfo) model.get(i));
		}
		return contents;
	}
	
	public String getSortOrder() {
		// build sort order string
		String sortOrder = ""; //$NON-NLS-1$

		if ( contents != null ) { // sanity check
			for ( HiQuickInfo content : contents )
				sortOrder = sortOrder + ","+content.getBaseID(); //$NON-NLS-1$
			
			// remove leading ","
			if ( sortOrder.length() > 0 )
				sortOrder = sortOrder.substring(1);	
		}
		
		return sortOrder;
	}


	public void setMenuActionListener(ActionListener listener) {
		createURLMenuItem.addActionListener(listener);
		createTextMenuItem.addActionListener(listener);
		createLightTableMenuItem.addActionListener(listener);
		createObjectMenuItem.addActionListener(listener);
    	newGroupFromSelectionMenuItem.addActionListener(listener);
    	removeSelectionMenuItem.addActionListener(listener);
    	moveSelectionToTrashMenuItem.addActionListener(listener);
	}
	
	public JButton getListStyleButton() {
		return listStyleButton;
	}
	
	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}
	
	/**
	 * Displays option popup menu in a default location (next to options button)
	 */
	public void showDefaultPopupMenu() {
		setPopupMenuState();
		popupMenu.show(this, controlPanel.getLocation().x+30, controlPanel.getLocation().y+20);		
	}

	private Point getRelativeMenuLocation(int x, int y) {
		Point menuLoc = new Point(x-listScrollPane.getViewport().getViewPosition().x, y-listScrollPane.getViewport().getViewPosition().y+30);
		
		return menuLoc;
	}

	private void populatePopupMenu() {
		elementCreationMenu.add(createTextMenuItem);
		elementCreationMenu.add(createURLMenuItem);
		elementCreationMenu.add(createLightTableMenuItem);
		elementCreationMenu.add(createObjectMenuItem);

		popupMenu.add(elementCreationMenu);
		popupMenu.add(new JSeparator());
		popupMenu.add(newGroupFromSelectionMenuItem);
		popupMenu.add(removeSelectionMenuItem);
		popupMenu.add(new JSeparator());
		popupMenu.add(moveSelectionToTrashMenuItem);
	}
	
	private void setPopupMenuState() {
		boolean creationEnabled = true;
		if ( currentGroup.getType() == GroupTypes.HIGROUP_TRASH ) creationEnabled = false;
    	createURLMenuItem.setEnabled(creationEnabled);
    	createTextMenuItem.setEnabled(creationEnabled);
    	createLightTableMenuItem.setEnabled(creationEnabled);
    	createObjectMenuItem.setEnabled(creationEnabled);
    	boolean modifyingEnabled = creationEnabled;
    	if ( modifyingEnabled ) if ( groupContentsList.getSelectedIndex() < 0 )
    		modifyingEnabled = false;
    	newGroupFromSelectionMenuItem.setEnabled(modifyingEnabled);
    	if ( currentGroup.getType() != GroupTypes.HIGROUP_IMPORT) 
    		removeSelectionMenuItem.setEnabled(modifyingEnabled);
    	else
    		removeSelectionMenuItem.setEnabled(false);
    	moveSelectionToTrashMenuItem.setEnabled(modifyingEnabled);
		if ( currentGroup.getType() == GroupTypes.HIGROUP_TRASH ) {
			creationEnabled = false;
			moveSelectionToTrashMenuItem.setText(Messages.getString("GroupContentsView.3")); //$NON-NLS-1$
			moveSelectionToTrashMenuItem.setIcon(deleteIcon);
	    	if ( groupContentsList.getSelectedIndex() >=0 ) moveSelectionToTrashMenuItem.setEnabled(true);
		} else {
			moveSelectionToTrashMenuItem.setText(Messages.getString("GroupContentsView.4")); //$NON-NLS-1$
			moveSelectionToTrashMenuItem.setIcon(trashIcon);
		}
		
		
	}

	private void initComponents() {

		groupContentsPanel = new JPanel();
		controlPanel = new JPanel();
		listScrollPane = new JScrollPane();
        elementCountLabel = new JLabel();
        groupContentsInfoPanel = new JPanel();
        listStyleButton = new JButton();
        optionsButton = new OptionButton();
        loadingIndicator = new JProgressBar();
    
        loadingIndicator.setIndeterminate(true);
        listScrollPane.setViewportView(groupContentsList);
        groupContentsInfoPanel.setLayout(new BorderLayout(10,0));
        groupContentsInfoPanel.add(elementCountLabel, BorderLayout.WEST);
        groupContentsInfoPanel.add(loadingIndicator, BorderLayout.CENTER);
        prepareElementLoading();
        
        // -----

        controlPanel.setLayout(new BorderLayout(10, 0));
        
        listStyleButton.setActionCommand("changeListStyle"); //$NON-NLS-1$
        listStyleButton.setBorder(BorderFactory.createEmptyBorder());
        listStyleButton.setPreferredSize(new Dimension(24, 24));
        controlPanel.add(listStyleButton, BorderLayout.EAST);

        controlPanel.add(optionsButton, BorderLayout.WEST);

        elementCountLabel.setText(Messages.getString("GroupContentsView.8")); //$NON-NLS-1$
        groupContentsInfoPanel.add(elementCountLabel, BorderLayout.WEST);
        controlPanel.add(groupContentsInfoPanel, java.awt.BorderLayout.CENTER);


        GroupLayout groupContentsPanelLayout = new GroupLayout(groupContentsPanel);
        groupContentsPanel.setLayout(groupContentsPanelLayout);
        groupContentsPanelLayout.setHorizontalGroup(
            groupContentsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(groupContentsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(groupContentsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.TRAILING, controlPanel, GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .add(GroupLayout.TRAILING, listScrollPane, GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE))
                .addContainerGap())
        );
        groupContentsPanelLayout.setVerticalGroup(
            groupContentsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, groupContentsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(listScrollPane, GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .add(7, 7, 7)
                .add(controlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        
        // -----
                
        groupContentsPanel.setPreferredSize(new Dimension(200,300));
        // DEBUG refactor
        iconStyleIcon = new ImageIcon(getClass().getResource("/resources/icons/group-iconstyle.png")); //$NON-NLS-1$
        listStyleIcon = new ImageIcon(getClass().getResource("/resources/icons/group-liststyle.png")); //$NON-NLS-1$
        listStyleButton.setIcon(listStyleIcon);
        listStyleButton.setToolTipText(Messages.getString("GroupContentsView.11")); //$NON-NLS-1$

        // init popup menu
    	popupMenu = new JPopupMenu();
    	elementCreationMenu = new JMenu(); 
    	elementCreationMenu.setIcon(new ImageIcon(getClass().getResource("/resources/icons/create-menu.png"))); //$NON-NLS-1$
    	createURLMenuItem = new JMenuItem(); 
    	createURLMenuItem.setActionCommand("newURL"); //$NON-NLS-1$
    	createTextMenuItem = new JMenuItem(); 
    	createTextMenuItem.setActionCommand("newText"); //$NON-NLS-1$
    	createLightTableMenuItem = new JMenuItem(); 
    	createLightTableMenuItem.setActionCommand("newLighttable"); //$NON-NLS-1$
    	createObjectMenuItem = new JMenuItem();
    	createObjectMenuItem.setActionCommand("newObject"); //$NON-NLS-1$
    	newGroupFromSelectionMenuItem = new JMenuItem(); 
    	newGroupFromSelectionMenuItem.setActionCommand("groupFromSelection"); //$NON-NLS-1$
    	removeSelectionMenuItem = new JMenuItem();
    	removeSelectionMenuItem.setActionCommand("removeSelection"); //$NON-NLS-1$
    	moveSelectionToTrashMenuItem = new JMenuItem();
    	moveSelectionToTrashMenuItem.setActionCommand("moveSelectionToTrash"); //$NON-NLS-1$
    	// DEBUG
    	trashIcon = new ImageIcon(getClass().getResource("/resources/icons/trashcan-icon.png")); //$NON-NLS-1$
    	deleteIcon = new ImageIcon(getClass().getResource("/resources/icons/link-remove.png")); //$NON-NLS-1$
    	moveSelectionToTrashMenuItem.setIcon(trashIcon);			
    }

	private void setLoading(boolean loading) {
		optionsButton.setEnabled(!loading);
		if ( loading ) elementCountLabel.setText(Messages.getString("GroupContentsView.29")); //$NON-NLS-1$
		else {
			if ( elementCount >= 0 ) {
				if ( elementCount != 1 ) elementCountLabel.setText(elementCount+Messages.getString("GroupContentsView.30")); //$NON-NLS-1$
				else elementCountLabel.setText(Messages.getString("GroupContentsView.31")); //$NON-NLS-1$
			} else elementCountLabel.setText(Messages.getString("GroupContentsView.32")); //$NON-NLS-1$
		}
		loadingIndicator.setVisible(loading);
		
	}

		
	public void setCurrentGroup(HiGroup group) {
		this.currentGroup = group;
	}

	public HiGroup getCurrentGroup() {
		return this.currentGroup;
	}

	public GroupContentsList getContentsList() {
		return groupContentsList;
	}
	
	public HiQuickInfo getSelectedElement() {
		if ( groupContentsList.getSelectedIndex() > -1 )
			return (HiQuickInfo) model.getElementAt(groupContentsList.getSelectedIndex());
		else return null;
	}

	// DEBUG
	public void prepareElementLoading() {
		model.removeAllElements();
		elementCount = 0;
		setLoading(true);
	}
	
	public void setContents(List<HiQuickInfo> infoList) {
		model.removeAllElements();

		if ( infoList != null ) {
			this.contents = infoList;
			((GroupContentsCellRenderer)groupContentsList.getCellRenderer()).clearCache();

			elementCount = 0;
			for ( HiQuickInfo info : contents )
				model.addElement(info);
			elementCount = infoList.size();
			
			// sort contents
			sortContents();
			
			setLoading(false);
		} else {
			elementCount = -1;
			this.contents = new ArrayList<HiQuickInfo>();
			setLoading(false);
		}
	}

	public void sortContents() {
		if ( contents == null || currentGroup == null ) return;
		
		long contentID;
		int index = 0;
		int contentIndex;

		// parse sort order string (don´t trust user input)
		for ( String contentIDString : currentGroup.getSortOrder().split(",") ) { //$NON-NLS-1$
			try {
				contentID = Long.parseLong(contentIDString);
				// find content belonging to the parsed id
				contentIndex = -1;
				
				for ( int i=0; i<model.size(); i++ )
					if ( contents.get(i).getBaseID() == contentID )
						contentIndex = i;
				// if content was found, sort to new index
				if ( contentIndex >= 0 ) if ( contentIndex != index ) {
					HiQuickInfo content = contents.get(contentIndex);
					model.remove(contentIndex);
					contents.remove(contentIndex);
					model.add(index, content);
					contents.add(index, content);
					index = index+1;
				} else index = index + 1;
			} catch (NumberFormatException e) {}; // user messed with sort order string format, no problem
		}	
	}


	public void updateQuickInfo(HiQuickInfo newInfo) {
		int index = -1;
		HiQuickInfo oldInfo = null;
		// find the index for this content
		for ( int i=0; i < model.size(); i++ )
			if ( ((HiQuickInfo)model.getElementAt(i)).getBaseID() == newInfo.getBaseID() ) {
				index = i;
				oldInfo = (HiQuickInfo)model.getElementAt(i);
			}
 		((GroupContentsCellRenderer)groupContentsList.getCellRenderer()).removeContentFromCache(oldInfo);

		// update GUI
		if (index >= 0) model.setElementAt(newInfo, index);
		groupContentsList.repaint();
	}


	// DEBUG: refactor
	public void addContent(HiQuickInfo info) {
		boolean contains = false;
		for ( HiQuickInfo content : contents )
			if ( content.getBaseID() == info.getBaseID() )
				contains = true;
		if ( !contains ) this.contents.add(info);

		model.addElement(info);
		elementCount = contents.size();
		setLoading(false);
		groupContentsList.setSelectedIndex(model.getSize()-1);
		// TODO: scroll to selected index
	}
	
	// DEBUG refactor
	public void addContent(HiQuickInfo info, int index) {
		index = Math.max(0, index);
		
		boolean contains = false;
		for ( HiQuickInfo content : contents )
			if ( content.getBaseID() == info.getBaseID() )
				contains = true;
		if ( !contains ) this.contents.add(index,info);

		model.add(index,info);
		elementCount = contents.size();
		setLoading(false);
		groupContentsList.setSelectedIndex(index);
		// TODO: scroll to selected index
	}


	// DEBUG: refactor
	public void removeContent(HiQuickInfo info) {
		int index = -1;
		// find content index
		for ( int i=0; i < model.getSize(); i++ )
			if ( ((HiQuickInfo)model.get(i)).getBaseID() == info.getBaseID() )
				index = i;

		// only remove if content index was found
		if ( index >= 0 ) {
			this.contents.remove(index);
			model.remove(index);
			elementCount = contents.size();
			setLoading(false);
		}
	}



	
	
	// --------------------------------------------------------------------------------------------
	
	
	

	/**
	 * mouse event: mouse pressed
	 * - display popup menu if necessary
	 */
	public void mousePressed(MouseEvent e) {
		// display popup menu
		if ( e.isPopupTrigger() ) {
			e.consume();
			setPopupMenuState();
			Point menuLoc = getRelativeMenuLocation(e.getX(), e.getY());
			popupMenu.show(this, menuLoc.x, menuLoc.y);
		}
	}

	/**
	 * mouse event: mouse released
	 * - display popup menu if necessary
	 */
	public void mouseReleased(MouseEvent e) {
		if ( !e.isConsumed() ) if ( e.isPopupTrigger() ) {
			setPopupMenuState();
			Point menuLoc = getRelativeMenuLocation(e.getX(), e.getY());
			popupMenu.show(this, menuLoc.x, menuLoc.y);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
