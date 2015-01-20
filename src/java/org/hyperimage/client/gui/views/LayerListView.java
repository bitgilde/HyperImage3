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
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Vector;

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

import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.lists.LayerListCellRenderer;
import org.hyperimage.client.model.HILayer;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class LayerListView extends GUIView {

	private static final long serialVersionUID = 4440600497916240667L;

	
	private JButton createLayerButton;
    private JList layerList;
    private DefaultListModel layerListModel;
    private JScrollPane layerListScroll;
    private JPanel listPanel;
    private JButton removeLayerButton;
    
    // popup menu
    private JMenuItem titleItem;
    private JPopupMenu popupMenu;
	private JMenuItem createLayerMenuItem;
    private JMenuItem removeLayerMenuItem;
    
    
    private Vector<HILayer> layers;
	private String defLang;
	
	
	public LayerListView(Vector<HILayer> layers, String defLang) {
		super(new Color(0x91, 0xB9, 0xFA));
		
		this.layers = layers;
		this.defLang = defLang;		

		initComponents();
		updateLanguage();
		
		for ( HILayer layer : layers )
			layerListModel.addElement(layer);		
		if ( layers.size() > 0 ) layerList.setSelectedIndex(0);
	
		// DnD
		layerList.setDragEnabled(true);
		layerList.setDropMode(DropMode.INSERT);
		
		setDisplayPanel(listPanel);
		
	}
	
	
	public void updateLanguage() {
		setTitle(Messages.getString("LayerListView.0")); //$NON-NLS-1$
		createLayerMenuItem.setText(Messages.getString("LayerListView.1")); //$NON-NLS-1$
		removeLayerMenuItem.setText(Messages.getString("LayerListView.2")); //$NON-NLS-1$
        createLayerButton.setToolTipText(Messages.getString("LayerListView.3")); //$NON-NLS-1$
		removeLayerButton.setToolTipText(Messages.getString("LayerListView.4")); //$NON-NLS-1$
		titleItem.setText(Messages.getString("LayerListView.5")); //$NON-NLS-1$
	}

	public String getSortOrder() {
		// build sort order string
		String sortOrder = ""; //$NON-NLS-1$
		for ( HILayer layer : layers )
			sortOrder = sortOrder + ","+layer.getModel().getId(); //$NON-NLS-1$
		
		// remove leading ","
		if ( sortOrder.length() > 0 )
			sortOrder = sortOrder.substring(1);
		
		return sortOrder;
	}
	
	public void updateLayerList() {
		int oldIndex = layerList.getSelectedIndex();

		layerListModel.removeAllElements();
		for ( HILayer layer : layers )
			layerListModel.addElement(layer);
		
		if ( layers.size() > 0 ) {
			if ( oldIndex < layerListModel.size() )
				layerList.setSelectedIndex(oldIndex);
			else layerList.setSelectedIndex(layers.size()-1);
		}
		
		removeLayerButton.setEnabled(layers.size() > 0);
		removeLayerMenuItem.setEnabled(layers.size() > 0);
	}

	public JList getLayerList() {
		return this.layerList;
	}


	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	public void showPopupMenu(int x, int y) {
		// TODO: adjust popup menu location to scrollpane viewport
		popupMenu.show(this, x, y+10);
	}

	public void attachActionListeners(ActionListener listener) {
		createLayerButton.addActionListener(listener);
		removeLayerButton.addActionListener(listener);
		createLayerMenuItem.addActionListener(listener);
		removeLayerMenuItem.addActionListener(listener);
	}


	
	private void initComponents() {

        listPanel = new JPanel();
        layerListScroll = new JScrollPane();
        layerList = new JList();
        createLayerButton = new JButton();
        removeLayerButton = new JButton();

        layerListScroll.setViewportView(layerList);

        createLayerButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add.png"))); //$NON-NLS-1$
        createLayerButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-active.png"))); //$NON-NLS-1$
        createLayerButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-disabled.png"))); //$NON-NLS-1$
        createLayerButton.setPreferredSize(new Dimension(24, 24));

        removeLayerButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove.png"))); //$NON-NLS-1$
        removeLayerButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-active.png"))); //$NON-NLS-1$
        removeLayerButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-disabled.png"))); //$NON-NLS-1$
        removeLayerButton.setPreferredSize(new Dimension(24, 24));

        GroupLayout listPanelLayout = new GroupLayout(listPanel);
        listPanel.setLayout(listPanelLayout);
        listPanelLayout.setHorizontalGroup(
            listPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(listPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(listPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(layerListScroll, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                    .add(listPanelLayout.createSequentialGroup()
                        .add(createLayerButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(removeLayerButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        listPanelLayout.setVerticalGroup(
            listPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, listPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(layerListScroll, GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(listPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(createLayerButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(removeLayerButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        
        // ------
		
        layerListModel = new DefaultListModel();
        layerList.setModel(layerListModel);
        layerList.setCellRenderer(new LayerListCellRenderer(defLang));
        layerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        createLayerButton.setActionCommand("add"); //$NON-NLS-1$
        removeLayerButton.setActionCommand("remove"); //$NON-NLS-1$
		removeLayerButton.setEnabled(layers.size() > 0);
		
		// popup menu
		popupMenu = new JPopupMenu();
		titleItem = new JMenuItem();
		titleItem.setEnabled(false);
		popupMenu.add(titleItem);
		popupMenu.add(new JSeparator());
		createLayerMenuItem = new JMenuItem();
		createLayerMenuItem.setActionCommand("add"); //$NON-NLS-1$
		popupMenu.add(createLayerMenuItem);
		createLayerMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/create-menu.png"))); //$NON-NLS-1$
		removeLayerMenuItem = new JMenuItem();
		removeLayerMenuItem.setEnabled(layers.size() > 0);
		removeLayerMenuItem.setActionCommand("remove"); //$NON-NLS-1$
		popupMenu.add(removeLayerMenuItem);

        
    }

}
