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

/*
 * Copyright 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
 * All rights reserved. Use is subject to license terms.
 * http://bitgilde.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For further information on HyperImage visit http://hyperimage.ws/
 */

package org.hyperimage.client.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.components.HIComponent;
import org.hyperimage.client.components.LayerEditor.LinkTransferHandler;
import org.hyperimage.client.gui.MetadataEditorControl;
import org.hyperimage.client.gui.lists.ColorListCellRenderer;
import org.hyperimage.client.gui.lists.GroupContentsCellRenderer;
import org.hyperimage.client.gui.lists.QuickInfoCell;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.util.MetadataHelper;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class LayerPropertyEditorView extends GUIView 
implements ChangeListener, PopupMenuListener, ActionListener, MouseListener {

	private static final long serialVersionUID = -3949801824506408714L;

	
    private JComboBox layerColorComboBox;
    private DefaultComboBoxModel layerColorListModel;
    private JPanel layerColorPanel;
    private JTextField layerOpacityField;
    private JLabel layerOpacityLabel;
    private JPanel layerOpacityPanel;
    private JSlider layerOpacitySlider;
    private JPanel layerPanel;
    private JList linkTargetList;
    private JPanel linkTargetPanel;
    private MetadataEditorControl metadataEditorControl;
    private JPanel propertyPanel;
    private JButton addColorToLibButton;
    private JLabel noLinkLabel;
    private TitledBorder linkTargetBorder;
    private TitledBorder layerBorder;
    private TitledBorder layerColorBorder;
    private TitledBorder layerOpacityBorder;
    private static final Border linkBorder = new LineBorder(Color.black, 1);
    
    
    private HILayer layer;
	private boolean enabled = true;
	private LayerPolygonEditorView syncView = null;
	private String defLang;
	private Vector<Color> projectColors;
	private Color customColor;
	private HIComponent owner;
	
	// popup menu options
	private JPopupMenu popupMenu;
	private JMenuItem linkInfoMenuItem;
	private JMenuItem visitLinkMenuItem;
	private JMenuItem removeLinkMenuItem;
	
	private JComponent colorUpdateComponent = null;

	public LayerPropertyEditorView(HIComponent owner, Vector<Color> projectColors, String defLang) {
		this(owner, projectColors, defLang, null);
	}
	public LayerPropertyEditorView(HIComponent owner, Vector<Color> projectColors, String defLang, JComponent colorUpdateComponent) {
		super(Messages.getString("LayerPropertyEditorView.0"), new Color(0x91, 0xB9, 0xFA)); //$NON-NLS-1$
		this.defLang = defLang;
		this.projectColors = projectColors;
		this.customColor = new Color (0,0,0,127);
		this.owner = owner;
		this.colorUpdateComponent = colorUpdateComponent;
		
		initComponents();
		setDisplayPanel(propertyPanel);
                
                // set up tags
                metadataEditorControl.setBaseID(0);
                metadataEditorControl.setTagCount(0);
		
		// Drag and Drop
		linkTargetList.setDragEnabled(true);
		linkTargetList.setDropMode(DropMode.ON);
		
		// attach listeners
		layerOpacitySlider.addChangeListener(this);
		layerOpacityField.addActionListener(this);
		layerColorComboBox.addPopupMenuListener(this);
		linkTargetList.addMouseListener(this);
	}
	
	public void updateLanguage() {
		super.setTitle(Messages.getString("LayerPropertyEditorView.0")); //$NON-NLS-1$
		
        noLinkLabel.setText(
        		"<html><center><b>"+ //$NON-NLS-1$
        		Messages.getString("LayerPropertyEditorView.10")+ //$NON-NLS-1$
        		"</b><br><br>"+ //$NON-NLS-1$
        		Messages.getString("LayerPropertyEditorView.12")+ //$NON-NLS-1$
        		"</center></html>"); //$NON-NLS-1$

		addColorToLibButton.setText(Messages.getString("LayerPropertyEditorView.14")); //$NON-NLS-1$
		metadataEditorControl.updateLanguage(Messages.getString("LayerPropertyEditorView.17"), null, Messages.getString("LayerPropertyEditorView.19")); //$NON-NLS-1$ //$NON-NLS-2$
		
        linkTargetBorder.setTitle(Messages.getString("LayerPropertyEditorView.22"));  //$NON-NLS-1$
		layerBorder.setTitle(Messages.getString("LayerPropertyEditorView.20")); //$NON-NLS-1$
        layerColorBorder.setTitle(Messages.getString("LayerPropertyEditorView.23")); //$NON-NLS-1$
        layerOpacityBorder.setTitle(Messages.getString("LayerPropertyEditorView.24")); //$NON-NLS-1$

    	visitLinkMenuItem.setText(Messages.getString("LayerPropertyEditorView.27")); //$NON-NLS-1$
    	removeLinkMenuItem.setText(Messages.getString("LayerPropertyEditorView.29")); //$NON-NLS-1$
	}
	
	
	public void updateMetadataLanguages() {
		if ( this.layer == null ) return;
		metadataEditorControl.setMetadata(this.layer.getModel().getMetadata(), defLang);
	}

        public void updateContent() {
            if ( layer != null && layer.getModel() != null ) 
                metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(layer.getModel().getTimestamp()));
        }
	
	public void setLayer(HILayer layer) {
		this.layer = layer;
		
		if ( layer == null ) {
			metadataEditorControl.setEmptyText(""); //$NON-NLS-1$
			setEditingEnabled(false);
		} else { 
			setEditingEnabled(true);
                        metadataEditorControl.setBaseID(layer.getModel().getId());
                        metadataEditorControl.setTagCount(HIRuntime.getGui().getTagCountForElement(layer.getModel().getId()));
			layerOpacitySlider.setValue(layer.getColour().getAlpha());
			float relOpacity = (float)layerOpacitySlider.getValue() / 255f;
			layerOpacityField.setText(String.valueOf((int)(relOpacity*100)));
			metadataEditorControl.setMetadata(layer.getModel().getMetadata(), defLang);
			if ( layer.getModel().getUUID() == null ) metadataEditorControl.setIdLabel("L"+layer.getModel().getId()); //$NON-NLS-1$
                        else metadataEditorControl.setIdLabel(layer.getModel().getUUID());
                        metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(layer.getModel().getTimestamp()));

			if ( layer.getModel().getUUID() == null ) metadataEditorControl.setEmptyText(Messages.getString("LayerPropertyEditorView.3")+" (L"+layer.getModel().getId()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        else metadataEditorControl.setEmptyText(Messages.getString("LayerPropertyEditorView.3")+" ("+layer.getModel().getUUID()+")");

			
			int projColorIndex = -1;
			for ( int i=0; i < projectColors.size(); i++ ) {
				Color color = projectColors.get(i);
				if ( color.getRed() == layer.getColour().getRed() &&
					 color.getGreen() == layer.getColour().getGreen() &&
					 color.getBlue() == layer.getColour().getBlue() )
					projColorIndex = i;
			}
			if ( HIRuntime.getGui().checkEditAbility(true) ) addColorToLibButton.setEnabled( projColorIndex >= 0 ? false : true );
			else addColorToLibButton.setEnabled(false);

			if ( projColorIndex >= 0 )
				layerColorComboBox.setSelectedIndex(projColorIndex);
			else { 
				layerColorComboBox.setSelectedIndex(projectColors.size());
				customColor = new Color(layer.getColour().getRed(), layer.getColour().getGreen(), layer.getColour().getBlue());
				updateCustomColor();
			}
		}		
		updateLayerLink();
	}
	
	public boolean hasMetadataChanges() {
		return metadataEditorControl.hasChanges();
	}

	public void syncMetadataChanges() {
		metadataEditorControl.syncChanges();
	}

	public void resetMetadataChanges() {
		metadataEditorControl.resetChanges();
	}


	// Helper Method: sync view used to update/display layer property changes in the polygon editor view
	public void setSyncView(LayerPolygonEditorView polygonEditorView) {
		this.syncView = polygonEditorView;
	}

	

	public void setLinkTransferHandler(LinkTransferHandler linkTransferHandler) {
		linkTargetList.setTransferHandler(linkTransferHandler);
		noLinkLabel.setTransferHandler(linkTransferHandler);
	}
	
	// DEBUG
	public void updateLayerLink() {
		DefaultListModel model = (DefaultListModel) linkTargetList.getModel();
		model.removeAllElements();
		GroupContentsCellRenderer renderer = (GroupContentsCellRenderer) linkTargetList.getCellRenderer();
		renderer.clearCache();
		
		if ( layer != null ) {			
			if ( layer.getModel().getLinkInfo() != null ) {
				model.addElement(layer.getModel().getLinkInfo());
				if ( getLayerLinkPreview() != null ) getLayerLinkPreview().setBorder(new EmptyBorder(0,0,0,0));
				linkTargetList.setVisible(true);
				noLinkLabel.setVisible(false);
			} else {
				linkTargetList.setVisible(false);
				noLinkLabel.setVisible(true);
			}
			
		} else {
			linkTargetList.setVisible(false);
			noLinkLabel.setVisible(true);
		}
		
		linkTargetList.repaint();
	}

	public QuickInfoCell getLayerLinkPreview() {
		if ( layer.getModel().getLinkInfo() == null ) return null;
		
		GroupContentsCellRenderer renderer = (GroupContentsCellRenderer) linkTargetList.getCellRenderer();
		return renderer.getCellForContent(layer.getModel().getLinkInfo());
	}


	public JList getLinkTargetList() {
		return linkTargetList;
	}

	public JButton getSaveButton() {
		return metadataEditorControl.getSaveButton();
	}
	
	public JButton getResetButton() {
		return metadataEditorControl.getResetButton();
	}
	

	public JButton getAddToLibraryButton() {
		return addColorToLibButton;
	}
	
	public void setPopupMenuListeners(ActionListener listener) {
		visitLinkMenuItem.addActionListener(listener);
		removeLinkMenuItem.addActionListener(listener);
	}
		

	public void regenerateProjectColors() {
		layerColorListModel.removeAllElements();
        int index = 0;
        boolean indexSet = false;
        for ( Color color : projectColors ) {
        	layerColorListModel.addElement(color);
        	if ( layer != null 
        			&& color.getRed() == layer.getColour().getRed()
        			&& color.getGreen() == layer.getColour().getGreen()
        			&& color.getBlue() == layer.getColour().getBlue() 
        	) {
        		indexSet = true;
        		layerColorComboBox.setSelectedIndex(index);
        	}
        	index = index + 1;
        }

        if ( layer != null ) 
			customColor = new Color (layer.getColour().getRed(), layer.getColour().getGreen(), layer.getColour().getBlue(), 127);

		layerColorListModel.addElement(customColor);
		if ( layer == null ) {
			layerColorComboBox.setSelectedIndex(0);
			indexSet = true;
		}

		if ( !indexSet ) {
        	layerColorComboBox.setSelectedIndex(layerColorListModel.getSize()-1);
        	addColorToLibButton.setEnabled(true && HIRuntime.getGui().checkEditAbility(true) );
		} else addColorToLibButton.setEnabled(false);

	}

	
	private void setEditingEnabled(boolean enabled) {
		if ( this.enabled != enabled ) {
			this.enabled = enabled;
			layerColorComboBox.setEnabled(enabled);
			layerOpacityField.setEnabled(enabled);
			layerOpacityField.setEditable(enabled);
			layerOpacitySlider.setEnabled(enabled);
			linkTargetList.setEnabled(enabled);
			if ( !enabled ) {
				metadataEditorControl.setMetadata(null, defLang);
				metadataEditorControl.setIdLabel("-"); //$NON-NLS-1$
				addColorToLibButton.setEnabled(false);
			}
		}
	}
	
	private void updateCustomColor() {
		layerColorComboBox.removePopupMenuListener(this);
		layerColorListModel.removeElementAt(projectColors.size());
		layerColorListModel.addElement(customColor);
		layerColorComboBox.setSelectedIndex(projectColors.size());
		layerColorComboBox.addPopupMenuListener(this);
	}
	
	private void setPopupMenuState() {
		boolean hasLink = false;
		if ( layer != null && layer.getModel().getLinkInfo() != null )
			hasLink = true;

		if ( hasLink )
			linkInfoMenuItem.setText(Messages.getString("LayerPropertyEditorView.7")+" "+MetadataHelper.getDisplayableID(layer.getModel().getLinkInfo())); //$NON-NLS-1$ //$NON-NLS-2$
		
		linkInfoMenuItem.setVisible(hasLink);
		visitLinkMenuItem.setEnabled(hasLink);
		removeLinkMenuItem.setEnabled(hasLink);
	}



	private void initComponents() {
		
        noLinkLabel = new JLabel();
        noLinkLabel.setVisible(false);
        noLinkLabel.setSize(136, 171);
        noLinkLabel.setPreferredSize(new Dimension(134, 169));
        noLinkLabel.setBackground(Color.white);
        noLinkLabel.setOpaque(true);
        noLinkLabel.setBorder(linkBorder);

		addColorToLibButton = new JButton();
		addColorToLibButton.setActionCommand("colorToLibrary"); //$NON-NLS-1$

		// -----
		
        propertyPanel = new JPanel();
        layerPanel = new JPanel();
        linkTargetPanel = new JPanel();
        linkTargetList = new JList();
        layerColorPanel = new JPanel();
        layerColorComboBox = new JComboBox();
        layerOpacityPanel = new JPanel();
        layerOpacitySlider = new JSlider();
        layerOpacityField = new JTextField();
        layerOpacityLabel = new JLabel();
        
		metadataEditorControl = new MetadataEditorControl("title", Messages.getString("LayerPropertyEditorView.17"), null, null, "comment", Messages.getString("LayerPropertyEditorView.19")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		layerBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("LayerPropertyEditorView.20"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.BLUE); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
        layerPanel.setBorder(layerBorder);

        linkTargetBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("LayerPropertyEditorView.22"));  //$NON-NLS-1$
        linkTargetPanel.setBorder(linkTargetBorder);
        linkTargetPanel.add(linkTargetList);
        linkTargetPanel.add(noLinkLabel);

        layerColorBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("LayerPropertyEditorView.23")); //$NON-NLS-1$
        layerColorPanel.setBorder(layerColorBorder);

        layerColorPanel.setLayout(new BorderLayout());
        layerColorPanel.add(layerColorComboBox, BorderLayout.CENTER);
        layerColorPanel.add(addColorToLibButton, BorderLayout.SOUTH);

        layerOpacityBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("LayerPropertyEditorView.24")); //$NON-NLS-1$
        layerOpacityPanel.setBorder(layerOpacityBorder);

        layerOpacitySlider.setMinorTickSpacing(1);
        layerOpacitySlider.setSnapToTicks(true);
        layerOpacitySlider.setPaintLabels(false);
        layerOpacitySlider.setPaintTicks(false);

        layerOpacityField.setText("100"); //$NON-NLS-1$

        layerOpacityLabel.setLabelFor(layerOpacityField);
        layerOpacityLabel.setText("%"); //$NON-NLS-1$

        GroupLayout layerOpacityPanelLayout = new GroupLayout(layerOpacityPanel);
        layerOpacityPanel.setLayout(layerOpacityPanelLayout);
        layerOpacityPanelLayout.setHorizontalGroup(
            layerOpacityPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, layerOpacityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(layerOpacitySlider, GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layerOpacityField, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layerOpacityLabel)
                .add(20, 20, 20))
        );
        layerOpacityPanelLayout.setVerticalGroup(
            layerOpacityPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(layerOpacityPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                .add(layerOpacityLabel)
                .add(layerOpacityField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .add(layerOpacitySlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
        
        GroupLayout layerPanelLayout = new GroupLayout(layerPanel);
        layerPanel.setLayout(layerPanelLayout);
        layerPanelLayout.setHorizontalGroup(
            layerPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(layerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(linkTargetPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(layerPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(layerColorPanel, GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                    .add(layerOpacityPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layerPanelLayout.setVerticalGroup(
            layerPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(layerPanelLayout.createSequentialGroup()
                .add(layerPanelLayout.createParallelGroup(GroupLayout.LEADING, false)
                    .add(GroupLayout.TRAILING, layerPanelLayout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(layerColorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(layerOpacityPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .add(linkTargetPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout propertyPanelLayout = new GroupLayout(propertyPanel);
        propertyPanel.setLayout(propertyPanelLayout);
        propertyPanelLayout.setHorizontalGroup(
            propertyPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, propertyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(propertyPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, metadataEditorControl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, layerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        propertyPanelLayout.setVerticalGroup(
            propertyPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(propertyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(layerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(metadataEditorControl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        
        // -----
        
        // set up popup menu
    	popupMenu = new JPopupMenu();
    	linkInfoMenuItem = new JMenuItem();
    	linkInfoMenuItem.setEnabled(false);
    	popupMenu.add(linkInfoMenuItem);
    	visitLinkMenuItem = new JMenuItem();
    	visitLinkMenuItem.setActionCommand("visitLink"); //$NON-NLS-1$
    	popupMenu.add(visitLinkMenuItem);
    	popupMenu.add(new JSeparator());
    	removeLinkMenuItem = new JMenuItem();
    	removeLinkMenuItem.setActionCommand("removeLink"); //$NON-NLS-1$
    	popupMenu.add(removeLinkMenuItem);


        // set up link target list display
        GroupContentsCellRenderer renderer = new GroupContentsCellRenderer();
        renderer.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkTargetList.setCellRenderer(renderer);
        linkTargetList.setModel(new DefaultListModel());
        linkTargetList.setPreferredSize(new Dimension(132,166));
        linkTargetList.setBackground(linkTargetPanel.getBackground());
		linkTargetList.setBorder(linkBorder);
        
        layerOpacitySlider.setMinimum(0);
        layerOpacitySlider.setMaximum(255);
        
        // init color chooser and add project colors
        layerColorListModel = new DefaultComboBoxModel();
        layerColorComboBox.setModel(layerColorListModel);
        layerColorComboBox.setRenderer(new ColorListCellRenderer(projectColors, layerColorComboBox));
        for ( Color color : projectColors )
        	layerColorListModel.addElement(color);
        layerColorListModel.addElement(customColor);
        
        updateLanguage();
    }
	
	
	
	// ---------------------------------------------------------------


	public void stateChanged(ChangeEvent e) {
		layer.setOpacity(layerOpacitySlider.getValue());
		float relOpacity = (float)layerOpacitySlider.getValue() / 255f;
		layerOpacityField.setText(String.valueOf((int)(relOpacity*100)));
		
		// show changes if possible
		if ( syncView != null )
			syncView.repaintShapes();
	}

	
	// ---------------------------------------------------------------



	public void popupMenuCanceled(PopupMenuEvent e) {		
	}
	
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		if ( layerColorComboBox.getSelectedIndex() == projectColors.size() ) {
			// let the user select a new color
			final LayerPropertyEditorView propView = this;
			SwingUtilities.invokeLater(new Runnable()  {
				public void run() {
					Color newColor = JColorChooser.showDialog(propView, Messages.getString("LayerPropertyEditorView.31"), customColor); //$NON-NLS-1$
					if ( newColor != null ) {
						// check if new color is a project color
						boolean indexSet = false;
						for ( int index = 0; index < projectColors.size(); index ++ ) {
							Color color = projectColors.get(index);
				        	if ( color.getRed() == newColor.getRed()
				        		&& color.getGreen() == newColor.getGreen()
				        		&& color.getBlue() == newColor.getBlue() 
				        		) {
				        		indexSet = true;
				        		layerColorComboBox.setSelectedIndex(index);
				        	}
				        }
						
						if ( !indexSet ) {
							customColor = new Color (newColor.getRed(), newColor.getGreen(), newColor.getBlue(), 127);
							updateCustomColor();
							layer.setColour(customColor);
							addColorToLibButton.setEnabled(true && HIRuntime.getGui().checkEditAbility(true));
						}
						if ( syncView != null ) syncView.repaintShapes();
					} else {
						layer.setColour(customColor);
						addColorToLibButton.setEnabled(true && HIRuntime.getGui().checkEditAbility(true));
						if ( syncView != null ) syncView.repaintShapes();
					}
					if ( colorUpdateComponent != null )
						colorUpdateComponent.repaint();
				}
			});
		} else {
			layer.setColour(projectColors.get(layerColorComboBox.getSelectedIndex()));
			addColorToLibButton.setEnabled(false);
			if ( syncView != null ) syncView.repaintShapes();
		}
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	}

	
	// ---------------------------------------------------------------


	// DEBUG: clean this up, avoid rounding errors
	public void actionPerformed(ActionEvent e) {
		try {
			int opacity = Integer.parseInt(layerOpacityField.getText());
			opacity = Math.max(0, opacity);
			opacity = Math.min(100, opacity);
			int layerOpacity = (int) ((255f/100f)*opacity);
			if ( layerOpacity > 0 && layerOpacity < 255 )
				layerOpacity = layerOpacity + 1;
			layer.setOpacity(layerOpacity);
			layerOpacitySlider.setValue(layerOpacity);
			
			// show changes if possible
			if ( syncView != null )
				syncView.repaintShapes();

		} catch (NumberFormatException nfe) {
			layerOpacityField.setText( Integer.toString(layer.getOpacity()*100/255));
		}
	}



	
	// ---------------------------------------------------------------




	@Override
	public void mouseClicked(MouseEvent e) {
	
		// user double clicked on link, try to open
		if ( e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 && !e.isPopupTrigger()  ) {
			if ( layer == null ) return;
			
			if ( layer.getModel().getLinkInfo() != null )
				HIRuntime.getGui().openContentEditor(layer.getModel().getLinkInfo(), owner);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * mouse event: mouse pressed
	 * - display popup menu if necessary
	 */
	public void mousePressed(MouseEvent e) {
		// display popup menu
		if ( e.isPopupTrigger() && layer != null ) {
			e.consume();
			setPopupMenuState();
			popupMenu.show(linkTargetPanel, e.getX(), e.getY()+15);
		}
	}

	/**
	 * mouse event: mouse released
	 * - display popup menu if necessary
	 */
	public void mouseReleased(MouseEvent e) {
		if ( !e.isConsumed() ) if ( e.isPopupTrigger() && layer != null ) {
			setPopupMenuState();
			popupMenu.show(linkTargetPanel, e.getX(), e.getY()+15);
		}
	}






	}

	
	
