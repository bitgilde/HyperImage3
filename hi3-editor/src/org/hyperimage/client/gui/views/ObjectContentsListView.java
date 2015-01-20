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

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;

import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.OptionButton;
import org.hyperimage.client.gui.lists.ObjectContentsCellRenderer;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiObject;
import org.hyperimage.client.ws.HiObjectContent;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class ObjectContentsListView extends GUIView implements MouseListener {

	private static final long serialVersionUID = 4698307895559211977L;

	
	private HiObject object;
	private String defaultLang;
	
    private JScrollPane listScroll;
    private JPanel objectContentsControlPanel;
    private JPanel objectContentsPanel;
    private OptionButton optionsButton;
    private JLabel viewCountLabel;

    private JList objectContentsList;
	private DefaultListModel objectContentsListModel;
	private ObjectContentsCellRenderer objectContentsRenderer;
	
	// popup menu options
	private JPopupMenu popupMenu;
	private JCheckBoxMenuItem defaultViewToggleMenuItem;
	private JMenuItem newInscriptionMenuItem;
	private JMenuItem moveViewToTrashMenuItem;

	
	public ObjectContentsListView(String defaultLang, HiObject object) {
		super(new Color(0x61, 0x89, 0xCA));
		this.defaultLang = defaultLang;
		this.object = object;
		
		initComponents();
		updateLanguage();
		setDisplayPanel(objectContentsPanel);
		
		// DnD
		objectContentsList.setDragEnabled(true);
		objectContentsList.setDropMode(DropMode.INSERT);

		updateViewCount();
		populateList();
		populatePopupMenu();
		
		// sort contents
		sortObjectContent();
		
		// attach listeners
		objectContentsList.addMouseListener(this);
	}
	
	
	public void updateLanguage() {
            setTitle(Messages.getString("ObjectContentsListView.0")); //$NON-NLS-1$
            viewCountLabel.setText(Messages.getString("ObjectContentsListView.1")); //$NON-NLS-1$
            updateViewCount();
            defaultViewToggleMenuItem.setText(Messages.getString("ObjectContentsListView.2")); //$NON-NLS-1$
            newInscriptionMenuItem.setText(Messages.getString("ObjectContentsListView.3")); //$NON-NLS-1$
            moveViewToTrashMenuItem.setText(Messages.getString("ObjectContentsListView.4")); //$NON-NLS-1$
            optionsButton.updateLanguage();
	}
	
	
	public JList getContentsList() {
		return objectContentsList;
	}
		
	public void setSelectedIndex(int index) {
		if ( objectContentsListModel.getSize() <= 0 ) {
			optionsButton.setEnabled(false);
			return;
		} else optionsButton.setEnabled(true);
		
		if ( index < 0 || index >= objectContentsListModel.getSize() )
			return;
		
		if ( objectContentsList.getSelectedIndex() != index ) 
			objectContentsList.setSelectedIndex(index);
		
	}
	
	public JButton getOptionsButton() {
		return optionsButton;
	}

	
	public void updateRendering(HiInscription inscription) {
		objectContentsRenderer.refresh(inscription);
		objectContentsList.repaint();
		
	}

	public void refreshList() {
		int oldIndex = objectContentsList.getSelectedIndex();
		long oldId = 0;
		if ( oldIndex >= 0 ) oldId = ((HiObjectContent)objectContentsListModel.getElementAt(oldIndex)).getId();
		populateList(); // rebuild list
		// try to find the old id again
		int newIndex = -1;
		for ( int i=0; i < objectContentsListModel.size(); i++ )
			if ( ((HiObjectContent)objectContentsListModel.elementAt(i)).getId() == oldId )
				newIndex = i;
		
		if ( newIndex >= 0 ) // found the content again
			objectContentsList.setSelectedIndex(newIndex);
		else if ( objectContentsListModel.size() > 0 ) {
			// could not find content, it was probably deleted --> select a content object close to the old index
			newIndex = Math.min(oldIndex, objectContentsListModel.size()-1);
			objectContentsList.setSelectedIndex(newIndex);
		}		
		// scroll to selected index
		if ( objectContentsListModel.size() > 0 )
			listScroll.getViewport().setViewPosition(objectContentsList.indexToLocation(objectContentsList.getSelectedIndex()));
	}

	
	public String getSortOrder() {
		// build sort order string
		String sortOrder = ""; //$NON-NLS-1$
		for ( int index = 0; index < objectContentsListModel.size(); index ++ )
			sortOrder = sortOrder + ","+((HiObjectContent) objectContentsListModel.get(index)).getId(); //$NON-NLS-1$
		
		// remove leading ","
		if ( sortOrder.length() > 0 )
			sortOrder = sortOrder.substring(1);
		
		return sortOrder;
	}
	
	private void sortObjectContent() {
		long contentID;
		int index = 0;
		int contentIndex;

		// parse sort order string (don´t trust user input)
		for ( String contentIDString : object.getSortOrder().split(",") ) { //$NON-NLS-1$
			try {
				contentID = Long.parseLong(contentIDString);
				// find content belonging to the parsed id
				contentIndex = -1;
				
				for ( int i=0; i<objectContentsListModel.size(); i++ )
					if ( ((HiObjectContent) objectContentsListModel.get(i)).getId() == contentID )
						contentIndex = i;
				// if content was found, sort to new index
				if ( contentIndex >= 0 ) if ( contentIndex != index ) {
					HiObjectContent content = (HiObjectContent) objectContentsListModel.get(contentIndex);
					objectContentsListModel.remove(contentIndex);
					object.getViews().remove(contentIndex);
					objectContentsListModel.add(index, content);
					object.getViews().add(index, content);
					index = index+1;
				} else index = index + 1;
			} catch (NumberFormatException e) {}; // user messed with sort order string format, no problem
		}
	}


	/**
	 * Displays option popup menu in a default location (next to options button)
	 */
	public void showDefaultPopupMenu() {
		setPopupMenuState();
		popupMenu.show(this, objectContentsControlPanel.getLocation().x+30, objectContentsControlPanel.getLocation().y+20);		
	}

	
	public void setMenuActionListener(ActionListener listener) {
		defaultViewToggleMenuItem.addActionListener(listener);
		newInscriptionMenuItem.addActionListener(listener);
		moveViewToTrashMenuItem.addActionListener(listener);
	}
	

	
	public void disablePopup() {
		objectContentsList.removeMouseListener(this);		
	}

	
	private void populatePopupMenu() {
		popupMenu.add(defaultViewToggleMenuItem);
		popupMenu.add(newInscriptionMenuItem);
		popupMenu.add(new JSeparator());
		popupMenu.add(moveViewToTrashMenuItem);		
	}
	
	private void setPopupMenuState() {
		boolean optionsEnabled = true;
		if ( objectContentsListModel.size() == 0 ) optionsEnabled = false;
		defaultViewToggleMenuItem.setEnabled(optionsEnabled);
		moveViewToTrashMenuItem.setEnabled(optionsEnabled);
		
		if ( object.getDefaultView() == null )
			defaultViewToggleMenuItem.setSelected(false);
		else if ( object.getDefaultView().getId() == ((HiObjectContent)objectContentsListModel.get(objectContentsList.getSelectedIndex())).getId() )
			defaultViewToggleMenuItem.setSelected(true);
		else
			defaultViewToggleMenuItem.setSelected(false);
	}

	private void populateList() {
		objectContentsListModel.removeAllElements();
		for ( HiObjectContent content : object.getViews() )
			objectContentsListModel.addElement(content);

		// select the default view if possible
		if ( objectContentsListModel.size() > 0 ) {
			if ( object.getDefaultView() == null )
				setSelectedIndex(0);
			else { 
				HiObjectContent defView = object.getDefaultView();
				for ( HiObjectContent content : object.getViews() )
					if ( defView.getId() == content.getId() )
						defView = content;
				setSelectedIndex(objectContentsListModel.indexOf(defView));
				// scroll to selected element
				listScroll.getViewport().setViewPosition(objectContentsList.indexToLocation(objectContentsList.getSelectedIndex()));
			}
		}
	}
	
	public void updateViewCount() {
		switch (object.getViews().size()) {
		case 0:
			viewCountLabel.setText(Messages.getString("ObjectContentsListView.8")); //$NON-NLS-1$
			break;
		case 1:
			viewCountLabel.setText(Messages.getString("ObjectContentsListView.9")); //$NON-NLS-1$
			break;
		default:
			viewCountLabel.setText(object.getViews().size()+" "+Messages.getString("ObjectContentsListView.11")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void initComponents() {

        objectContentsPanel = new JPanel();
        listScroll = new JScrollPane();
        objectContentsList = new JList();
        objectContentsControlPanel = new JPanel();
        optionsButton = new OptionButton();
        viewCountLabel = new JLabel();

        objectContentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        objectContentsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        objectContentsList.setVisibleRowCount(-1);
        listScroll.setViewportView(objectContentsList);

        GroupLayout obectContentsControlPanelLayout = new GroupLayout(objectContentsControlPanel);
        objectContentsControlPanel.setLayout(obectContentsControlPanelLayout);
        obectContentsControlPanelLayout.setHorizontalGroup(
            obectContentsControlPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(obectContentsControlPanelLayout.createSequentialGroup()
                .add(optionsButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(viewCountLabel)
                .addContainerGap(42, Short.MAX_VALUE))
        );
        obectContentsControlPanelLayout.setVerticalGroup(
            obectContentsControlPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(obectContentsControlPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                .add(optionsButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(viewCountLabel))
        );

        GroupLayout objectContentsPanelLayout = new GroupLayout(objectContentsPanel);
        objectContentsPanel.setLayout(objectContentsPanelLayout);
        objectContentsPanelLayout.setHorizontalGroup(
            objectContentsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, objectContentsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(objectContentsPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, objectContentsControlPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, listScroll, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                .addContainerGap())
        );
        objectContentsPanelLayout.setVerticalGroup(
            objectContentsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, objectContentsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(listScroll, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(objectContentsControlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        
        // -----
        
        objectContentsListModel = new DefaultListModel();
        objectContentsList.setModel(objectContentsListModel);
        objectContentsRenderer = new ObjectContentsCellRenderer(object, defaultLang);
        objectContentsList.setCellRenderer(objectContentsRenderer);
        objectContentsList.setBackground(Color.lightGray);
        
        // popup menu
        popupMenu = new JPopupMenu();

    	defaultViewToggleMenuItem = new JCheckBoxMenuItem();
    	defaultViewToggleMenuItem.setActionCommand("defaultView"); //$NON-NLS-1$
    	newInscriptionMenuItem = new JMenuItem();
    	newInscriptionMenuItem.setActionCommand("newInscription"); //$NON-NLS-1$
    	newInscriptionMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/create-menu.png"))); //$NON-NLS-1$
    	moveViewToTrashMenuItem = new JMenuItem();
    	moveViewToTrashMenuItem.setActionCommand("moveToTrash"); //$NON-NLS-1$
    	moveViewToTrashMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/trashcan-icon.png"))); //$NON-NLS-1$

    }

	
	// --------------------------------------------------------------------------------------------
	
	
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

	/**
	 * mouse event: mouse pressed
	 * - display popup menu if necessary
	 */
	public void mousePressed(MouseEvent e) {
		// display popup menu
		if ( e.isPopupTrigger() ) {
			e.consume();
			setPopupMenuState();
			popupMenu.show(this, e.getX()-listScroll.getViewport().getViewPosition().x+20, e.getY()-listScroll.getViewport().getViewPosition().y+15);
		}
	}

	/**
	 * mouse event: mouse released
	 * - display popup menu if necessary
	 */
	public void mouseReleased(MouseEvent e) {
		if ( !e.isConsumed() ) if ( e.isPopupTrigger() ) {
			setPopupMenuState();
			popupMenu.show(this, e.getX()-listScroll.getViewport().getViewPosition().x+20, e.getY()-listScroll.getViewport().getViewPosition().y+15);
		}
	}


}
