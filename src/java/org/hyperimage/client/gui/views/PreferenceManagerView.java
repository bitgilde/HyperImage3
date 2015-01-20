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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.lists.ColorListCellRenderer;
import org.hyperimage.client.gui.lists.PolygonListCellRenderer;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class PreferenceManagerView extends GUIView implements ListSelectionListener {

	private static final long serialVersionUID = -3293942365001635163L;

	
	private JLabel addInfoLabel;
    private JPanel buttonPanel;
    private JList colorPrefList;
    private JPanel colorPrefPanel;
    private JScrollPane colorPrefScroll;
    private JButton editButton;
    private JPanel managerPanel;
    private JList polygonPrefList;
    private JPanel polygonPrefPanel;
    private JScrollPane polygonPrefScroll;
    private JButton removeButton;
    private JLabel removeInfoLabel;
    private TitledBorder polygonPrefBorder;
    private TitledBorder colorPrefBorder;
    
    
    private DefaultListModel polygonModel, colorModel;
    private int[] emptySelection = new int[0];
    
    
    public PreferenceManagerView() {
    	super(Messages.getString("PreferenceManagerView.0")); //$NON-NLS-1$

		initComponents();
		
		setDisplayPanel(managerPanel);
		
		// attach listeners
		polygonPrefList.addListSelectionListener(this);
		colorPrefList.addListSelectionListener(this);
		
    }
    
    public void updateLanguage() {
    	super.setTitle(Messages.getString("PreferenceManagerView.0")); //$NON-NLS-1$
    	polygonPrefBorder.setTitle(Messages.getString("PreferenceManagerView.1")); //$NON-NLS-1$
    	colorPrefBorder.setTitle(Messages.getString("PreferenceManagerView.3")); //$NON-NLS-1$
        addInfoLabel.setText("<html>"+Messages.getString("PreferenceManagerView.6")+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        editButton.setText(Messages.getString("PreferenceManagerView.8")); //$NON-NLS-1$
        removeInfoLabel.setText("<html><b>"+Messages.getString("PreferenceManagerView.11")+":</b><br><br>"+Messages.getString("PreferenceManagerView.13")+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        removeButton.setText(Messages.getString("PreferenceManagerView.15")); //$NON-NLS-1$

    }
    
    
    public JButton getRemoveButton() {
    	return removeButton;
    }
    
    public JButton getEdiButton() {
    	return editButton;
    }
    
    public JList getPolygonPrefList() {
    	return polygonPrefList;
    }

    public JList getColorPrefList() {
    	return colorPrefList;
    }

    
	public void rebuildPreferenceLists(Vector<String> projectPolygons, Vector<Color> projectColors) {
		polygonModel.removeAllElements();
		colorModel.removeAllElements();
		((PolygonListCellRenderer)polygonPrefList.getCellRenderer()).clearCache();
		
		for ( String model : projectPolygons )
			polygonModel.addElement(model);
		
		for ( Color color : projectColors )
			colorModel.addElement(color);
	}

    
    
    private void initComponents() {

        managerPanel = new JPanel();
        polygonPrefPanel = new JPanel();
        polygonPrefScroll = new JScrollPane();
        polygonPrefList = new JList();
        colorPrefPanel = new JPanel();
        colorPrefScroll = new JScrollPane();
        colorPrefList = new JList();
        addInfoLabel = new JLabel();
        buttonPanel = new JPanel();
        editButton = new JButton();
        removeInfoLabel = new JLabel();
        removeButton = new JButton();

        polygonPrefBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("PreferenceManagerView.1"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), Color.blue); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
        polygonPrefPanel.setBorder(polygonPrefBorder);

        polygonPrefList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        polygonPrefList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        polygonPrefList.setVisibleRowCount(-1);
        polygonPrefScroll.setViewportView(polygonPrefList);

        GroupLayout polygonPrefPanelLayout = new GroupLayout(polygonPrefPanel);
        polygonPrefPanel.setLayout(polygonPrefPanelLayout);
        polygonPrefPanelLayout.setHorizontalGroup(
            polygonPrefPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(polygonPrefScroll, GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
        );
        polygonPrefPanelLayout.setVerticalGroup(
            polygonPrefPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(polygonPrefScroll, GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
        );

        colorPrefBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("PreferenceManagerView.3"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), Color.blue); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
        colorPrefPanel.setBorder(colorPrefBorder);

        colorPrefList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        colorPrefScroll.setViewportView(colorPrefList);

        GroupLayout colorPrefPanelLayout = new GroupLayout(colorPrefPanel);
        colorPrefPanel.setLayout(colorPrefPanelLayout);
        colorPrefPanelLayout.setHorizontalGroup(
            colorPrefPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(colorPrefScroll, GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
        );
        colorPrefPanelLayout.setVerticalGroup(
            colorPrefPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(colorPrefScroll, GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
        );

        editButton.setActionCommand("edit"); //$NON-NLS-1$
        editButton.setEnabled(false);

        removeInfoLabel.setVerticalAlignment(SwingConstants.BOTTOM);

        removeButton.setActionCommand("remove"); //$NON-NLS-1$
        removeButton.setEnabled(false);

        GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(buttonPanelLayout.createSequentialGroup()
                .add(editButton, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addContainerGap())
            .add(GroupLayout.TRAILING, buttonPanelLayout.createSequentialGroup()
                .add(removeButton, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addContainerGap())
            .add(removeInfoLabel, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(buttonPanelLayout.createSequentialGroup()
                .add(editButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(removeInfoLabel, GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(removeButton))
        );

        GroupLayout managerPanelLayout = new GroupLayout(managerPanel);
        managerPanel.setLayout(managerPanelLayout);
        managerPanelLayout.setHorizontalGroup(
            managerPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(managerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(managerPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, addInfoLabel, 0, 0, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, managerPanelLayout.createSequentialGroup()
                        .add(polygonPrefPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.UNRELATED)
                        .add(colorPrefPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        managerPanelLayout.setVerticalGroup(
            managerPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, managerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(managerPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, colorPrefPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, polygonPrefPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(addInfoLabel)
                .addContainerGap())
        );
        
        // -----
        
        polygonModel = new DefaultListModel();
        polygonPrefList.setModel(polygonModel);
        polygonPrefList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        polygonPrefList.setCellRenderer(new PolygonListCellRenderer(polygonPrefList));
        colorModel = new DefaultListModel();
        colorPrefList.setModel(colorModel);
        colorPrefList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		colorPrefList.setCellRenderer(new ColorListCellRenderer(HIRuntime.getManager().getProjectColors(), null));
        
		updateLanguage();


    }

    
    // -------------------------------------------------------------------------------------------------


	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (polygonPrefList.getSelectedIndex() >= 0 && colorPrefList.getSelectedIndex() >= 0 )
			if ( e.getSource() == polygonPrefList ) 
				colorPrefList.setSelectedIndices(emptySelection);
			else 
				polygonPrefList.setSelectedIndices(emptySelection);
		
		editButton.setEnabled(colorPrefList.getSelectedIndex() >= 0 ? true : false);
		removeButton.setEnabled(polygonPrefList.getSelectedIndex() >= 0 || colorPrefList.getSelectedIndex() >= 0 ? true : false);
	}


}
