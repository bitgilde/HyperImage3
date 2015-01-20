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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Vector;
import javax.media.jai.PlanarImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.PolygonEditorControl;
import org.hyperimage.client.gui.lists.LayerListCellRenderer;
import org.hyperimage.client.gui.lists.PolygonListCellRenderer;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.model.RelativePolygon;
import org.hyperimage.client.model.RelativePolygon.HiPolygonTypes;
import org.hyperimage.client.util.MetadataHelper;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class LayerPolygonEditorView extends GUIView implements ChangeListener, MouseListener, MouseWheelListener, MouseMotionListener, ActionListener, KeyListener {

	private static final long serialVersionUID = -8566390743522453070L;

	public String polygonEditorViewCommand = "";
	private JPanel editorPanel;
	private JLabel zoomSliderLabel;
	private JLabel zoomInfoLabel;
        private JLabel minusLabel;
        private JLabel plusLabel;
        private PolygonEditorControl polygonEditorControl;
        private JScrollPane polygonScroll;
        private JSlider zoomSlider;

	private Vector<HILayer> layers;
	private HILayer userSelectedLayer = null;
	private PlanarImage image;
	private Vector<String> projectPolygons;

	private float scale = 1.0f;
	private float minimumScale = 0.25f;

      
        private int posX = 0, posY = 0;

	// popup menu items
	private int popupX = 0, popupY = 0; // coordinates of upper left corner of user popup menu
	private JMenu newPolygonMenu;
	private JMenuItem layerIDMenuItem;
	private JMenuItem switchToPolygonLayerMenuItem;
	private JMenuItem deletePolygonItem;
	private JMenuItem toFreeDesignItem;
	private JCheckBoxMenuItem openPolygonPathItem;
	private JMenuItem copyToClipboardItem;
	private JMenuItem pasteFromClipboardItem;
	private JMenuItem addToLibraryItem;
	private JMenuItem discardLayerPolygonChanges;
	private JMenuItem discardAllPolygonChanges;
	private JMenuItem newLayerItem;

        private JToggleButton isolationModeButton;
        private JPanel toolButtonsPanel;
        private JToggleButton polygonButton;
        private JToggleButton rectangleButton;
        private JToggleButton circleButton;
        private JToggleButton flowingFreeformButton;
        private JToggleButton arrowButton;

	public LayerPolygonEditorView(Vector<HILayer> layers, PlanarImage image, Vector<String> projectPolygons) {
		super(new Color(0x91, 0xB9, 0xFA));
		this.layers = layers;
		this.image = image;
		this.projectPolygons = projectPolygons;

		initComponents();
		updateLanguage();
                updateMenuOptions();
                setDisplayPanel(editorPanel);

                // calculate minimum scale and set correct value
		int max = Math.max(image.getWidth(), image.getHeight());
		minimumScale = 400f / max;
		float unit = (2f-minimumScale)/100f;
		zoomSlider.setValue((int) ((1.0f-minimumScale)/unit) );

		// populate popup menu
		populatePopupMenu();
		
		// attach listener
		zoomSlider.addChangeListener(this);
                polygonEditorControl.addMouseWheelListener(this);
		polygonEditorControl.addMouseListener(this);
                polygonEditorControl.addMouseMotionListener(this);
                isolationModeButton.addActionListener(this);
                polygonButton.addActionListener(this);
                flowingFreeformButton.addActionListener(this);
                rectangleButton.addActionListener(this);
                circleButton.addActionListener(this);
                arrowButton.addActionListener(this);
               
		// menu listeners
		deletePolygonItem.addActionListener(this);
		toFreeDesignItem.addActionListener(this);
		openPolygonPathItem.addActionListener(this);
		copyToClipboardItem.addActionListener(this);
		pasteFromClipboardItem.addActionListener(this);
		discardLayerPolygonChanges.addActionListener(this);
		discardAllPolygonChanges.addActionListener(this);

                // delete active polygon by hitting the keyboard delete key
                this.addKeyListener(this);               
	}
	
        public void setFocus() {
            this.setFocusable(true);
            this.requestFocus();
            this.requestFocusInWindow();
        }
	
	public void updateLanguage() {
		setTitle(Messages.getString("LayerPolygonEditorView.0")); //$NON-NLS-1$
                layerIDMenuItem.setText(Messages.getString("LayerPolygonEditorView.1")+": "); //$NON-NLS-1$ //$NON-NLS-2$
                zoomSliderLabel.setText(Messages.getString("LayerPolygonEditorView.3")); //$NON-NLS-1$
                zoomInfoLabel.setText((int)(scale*100)+" %"); //$NON-NLS-1$

                newPolygonMenu.setText(Messages.getString("LayerPolygonEditorView.5")); //$NON-NLS-1$
                deletePolygonItem.setText(Messages.getString("LayerPolygonEditorView.6")); //$NON-NLS-1$
                toFreeDesignItem.setText(Messages.getString("LayerPolygonEditorView.7")); //$NON-NLS-1$
                openPolygonPathItem.setText(Messages.getString("LayerPolygonEditorView.8")); //$NON-NLS-1$
                pasteFromClipboardItem.setText(Messages.getString("LayerPolygonEditorView.9")); //$NON-NLS-1$
                addToLibraryItem.setText(Messages.getString("LayerPolygonEditorView.10")); //$NON-NLS-1$
                discardLayerPolygonChanges.setText(Messages.getString("LayerPolygonEditorView.11")); //$NON-NLS-1$
                discardAllPolygonChanges.setText(Messages.getString("LayerPolygonEditorView.12")); //$NON-NLS-1$
                newLayerItem.setText(Messages.getString("LayerPolygonEditorView.13")); //$NON-NLS-1$
                
                isolationModeButton.setToolTipText(Messages.getString("LayerPolygonEditorView.isolationModeIcon"));
                rectangleButton.setToolTipText(Messages.getString("LayerPolygonEditorView.boxIcon"));
                circleButton.setToolTipText(Messages.getString("LayerPolygonEditorView.circleIcon"));
                arrowButton.setToolTipText(Messages.getString("LayerPolygonEditorView.arrowIcon"));
                polygonButton.setToolTipText(Messages.getString("LayerPolygonEditorView.freeformIcon"));
                flowingFreeformButton.setToolTipText(Messages.getString("LayerPolygonEditorView.freehandIcon"));
	}
	

	public int getScaleX() {
		return image.getWidth();
	}

	public int getScaleY() {
		return image.getHeight();
	}


	public void repaintShapes() {
		polygonEditorControl.repaintShapes();
	}

	public void updateScale() {
		polygonEditorControl.updateScale();		
	}


	public void setUserSelectedLayer(HILayer layer) {
		userSelectedLayer = layer;
		if ( layer != null )
			polygonEditorControl.setUserSelectedLayer(layer);
	}

	public void addFreeFormPolygon(HILayer layer) {
            if ( layer != null ) polygonEditorControl.addFreeFormPolygon(layer, posX, posY);
            polygonEditorControl.setPlaceFirstAnchorMode(false);
            polygonEditorViewCommand = "";
	}


        public void addFreePolygon(HILayer layer) {
            if ( layer != null ) polygonEditorControl.addFreePolygon(layer, posX, posY);
            polygonEditorControl.setPlaceFirstAnchorMode(false);
            polygonEditorViewCommand = "";
	}


	public void addRectanglePolygon(HILayer layer) {
            if ( layer != null ) polygonEditorControl.addRectanglePolygon(layer, posX, posY);
            polygonEditorControl.setPlaceFirstAnchorMode(false);
            polygonEditorViewCommand = "";
            updateToolbarButtons();
	}


        public void addCirclePolygon(HILayer layer) {
            if ( layer != null ) polygonEditorControl.addCirclePolygon(layer, posX, posY);
            polygonEditorControl.setPlaceFirstAnchorMode(false);
            polygonEditorViewCommand = "";
            updateToolbarButtons();
	}

        public void addArrowPolygon(HILayer layer) {
            polygonEditorControl.setPlaceFirstAnchorMode(false);
            if ( layer != null ) polygonEditorControl.addArrowPolygon(layer, posX, posY);
            polygonEditorViewCommand = "";
            updateToolbarButtons();
	}
	
	public void addLibraryPolygon(HILayer layer, String model) {
		if ( layer != null ) polygonEditorControl.addLibraryPolygon(layer, model, posX, posY);
	}

	public JMenuItem getSwitchToPolygonLayerItem() {
		return switchToPolygonLayerMenuItem;
	}

	public HILayer getActiveLayer() {
		// find layer for active polygon
		HILayer polygonLayer = null;
		for ( HILayer layer : layers )
			if ( layer.getRelativePolygons().contains(polygonEditorControl.getActivePolygon()) )
				polygonLayer = layer;
		return polygonLayer;
	}
	
	public HILayer getSelectedLayer() {
		return polygonEditorControl.getSelectedLayer();
	}

	public RelativePolygon getSelectedPolygon() {
		return polygonEditorControl.getSelectedPolygon();
	}
        
        public void setPlaceFirstAnchorMode(boolean mode) {
            polygonEditorControl.setPlaceFirstAnchorMode(mode);
            cleanupPolygons();
        }


	public void cleanupPolygons() {
		polygonEditorControl.closeAndCleanupPolygons();
                polygonEditorControl.repaint();
	}

        public void updateToolbarButtons() {
            polygonButton.removeActionListener(this);
            flowingFreeformButton.removeActionListener(this);
            rectangleButton.removeActionListener(this);
            circleButton.removeActionListener(this);
            arrowButton.removeActionListener(this);

            polygonButton.setSelected(polygonEditorViewCommand.startsWith("add_freeform"));
            flowingFreeformButton.setSelected(polygonEditorViewCommand.equalsIgnoreCase("add_free"));
            rectangleButton.setSelected(polygonEditorViewCommand.equalsIgnoreCase("add_rectangle"));
            circleButton.setSelected(polygonEditorViewCommand.equalsIgnoreCase("add_circle"));
            arrowButton.setSelected(polygonEditorViewCommand.equalsIgnoreCase("add_arrow"));
            
            polygonButton.addActionListener(this);
            flowingFreeformButton.addActionListener(this);
            rectangleButton.addActionListener(this);
            circleButton.addActionListener(this);
            arrowButton.addActionListener(this);
        }

	public void regenerateNewPolygonMenu(ActionListener listener) {
            // remove action listeners
            while ( newPolygonMenu.getActionListeners().length > 0 )
                newPolygonMenu.removeActionListener(newPolygonMenu.getActionListeners()[0]);

            newPolygonMenu.removeAll(); // clear menu
                
            polygonButton.removeActionListener(listener);
            flowingFreeformButton.removeActionListener(listener);
            rectangleButton.removeActionListener(listener);
            circleButton.removeActionListener(listener);
            arrowButton.removeActionListener(listener);

            polygonButton.addActionListener(listener);
            flowingFreeformButton.addActionListener(listener);
            rectangleButton.addActionListener(listener);
            circleButton.addActionListener(listener);
            arrowButton.addActionListener(listener);

                
		// generate the new Polygon Menu, updates added / removed library polygons
		JMenuItem item;
		item = new JMenuItem(Messages.getString("LayerPolygonEditorView.19")); 
		item.setActionCommand("ADD_FREEFORM"); 
		item.addActionListener(listener);
		newPolygonMenu.add(item);
	
		item = new JMenuItem(Messages.getString("LayerPolygonEditorView.21")); 
		item.setActionCommand("ADD_RECTANGLE"); 
		item.addActionListener(listener);
		newPolygonMenu.add(item);
		
                item = new JMenuItem( Messages.getString("LayerPolygonEditorView.circle") );
		item.setActionCommand("ADD_CIRCLE");
		item.addActionListener(listener);
		newPolygonMenu.add(item);

                item = new JMenuItem( Messages.getString("LayerPolygonEditorView.arrow") );
		item.setActionCommand("ADD_ARROW");
		item.addActionListener(listener);
		newPolygonMenu.add(item);

                item = new JMenuItem( Messages.getString("LayerPolygonEditorView.freehand") );
		item.setActionCommand("ADD_FREE");
		item.addActionListener(listener);
		newPolygonMenu.add(item);

                newPolygonMenu.add(new JSeparator());

		for ( int i=0; i < projectPolygons.size(); i++  ) {
			item = new JMenuItem(Integer.toString(i+1));
			item.setActionCommand("ADD_LIB_"+i); //$NON-NLS-1$
			item.addActionListener(listener);
			item.setIcon(PolygonListCellRenderer.renderPolygonIcon(projectPolygons.get(i), Color.GRAY));
			newPolygonMenu.add(item);
		}
		// inform user of empty library
		if ( projectPolygons.size() == 0 ) {
			item = new JMenuItem(Messages.getString("LayerPolygonEditorView.24")); //$NON-NLS-1$
			item.setEnabled(false);
			newPolygonMenu.add(item);
		}
		
	}
	

	public JMenuItem getAddToLibraryItem() {
		return this.addToLibraryItem;
	}
	
	public JMenuItem getAddLayerItem() {
		return this.newLayerItem;
	}
	
	public void updateMenuOptions() {
            populatePopupMenu();
            isolationModeButton.setEnabled(layers.size() > 0);
            if ( !isolationModeButton.isEnabled() ) isolationModeButton.setSelected(false);
	}

	
	private void populatePopupMenu() {
		JPopupMenu popupMenu = polygonEditorControl.getPopupMenu();
		popupMenu.removeAll();
		
		// check if view contains layers
		if ( layers.size() > 0 ) {
			popupMenu.add(layerIDMenuItem); // add layer info item
			popupMenu.add(switchToPolygonLayerMenuItem); // switch to polygon layer item
			popupMenu.add(new JSeparator());
			popupMenu.add(newPolygonMenu); // add new polygon Menu
			popupMenu.add(toFreeDesignItem); // convert to free design item
			popupMenu.add(openPolygonPathItem); // open / close Polygon path
			popupMenu.add(new JSeparator());
			popupMenu.add(copyToClipboardItem); // copy to clipboard item
			popupMenu.add(pasteFromClipboardItem); // paste from clipboard item
			popupMenu.add(new JSeparator());
			popupMenu.add(addToLibraryItem); // add polygon to library item
			popupMenu.add(new JSeparator());
			popupMenu.add(discardLayerPolygonChanges); // discard changes made to polygons on specific layer
			popupMenu.add(discardAllPolygonChanges); // discard all changes made to polygons
			popupMenu.add(new JSeparator());
			popupMenu.add(deletePolygonItem); // delete Polygon item
			
		} else {
			// no layers found --> display info
			JMenuItem item = new JMenuItem(Messages.getString("LayerPolygonEditorView.25")); //$NON-NLS-1$
			item.setEnabled(false);
			popupMenu.add(item);
			popupMenu.add(new JSeparator());
			popupMenu.add(newLayerItem);
		}
	}
	
	private void setPopupMenuState() {
		ImageIcon icon = null;
		addToLibraryItem.setEnabled(false);
		if ( layers.size() > 0 ) {
				pasteFromClipboardItem.setEnabled(!HIRuntime.isClipboardEmpty());
				icon = LayerListCellRenderer.renderColorBarIcon(userSelectedLayer.getSolidColour());
				newPolygonMenu.setIcon(icon);

				if ( userSelectedLayer != null ) {
					// extract user selected layer info / title
					String title = MetadataHelper.findValue(
							"HIBase",  //$NON-NLS-1$
							"title",  //$NON-NLS-1$
							MetadataHelper.getDefaultMetadataRecord(
									userSelectedLayer.getModel(), 
									HIRuntime.getManager().getProject().getDefaultLanguage())
					);
					if ( title == null || title.length() == 0 ) {
                                            if ( userSelectedLayer.getModel().getUUID() == null ) title = "L"+userSelectedLayer.getModel().getId(); //$NON-NLS-1$
                                            else title = userSelectedLayer.getModel().getUUID();
                                        }
					layerIDMenuItem.setText(Messages.getString("LayerPolygonEditorView.2")+": "+title); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				// extract polygon layer info / title
				switchToPolygonLayerMenuItem.setEnabled(false);
				HILayer selectedLayer = getActiveLayer();
				if ( selectedLayer == null ) {
					switchToPolygonLayerMenuItem.setText(Messages.getString("LayerPolygonEditorView.14")); //$NON-NLS-1$
				} else {
					if ( userSelectedLayer != null && selectedLayer.getModel().getId() != userSelectedLayer.getModel().getId() ) 
						switchToPolygonLayerMenuItem.setEnabled(true);
					String layerTitle = MetadataHelper.findValue("HIBase", "title", MetadataHelper.getDefaultMetadataRecord(selectedLayer.getModel(), HIRuntime.getManager().getProject().getDefaultLanguage())); //$NON-NLS-1$ //$NON-NLS-2$
					if ( layerTitle == null || layerTitle.length() == 0 ) layerTitle="L"+selectedLayer.getModel().getId(); //$NON-NLS-1$
					switchToPolygonLayerMenuItem.setText(Messages.getString("LayerPolygonEditorView.18")+" \""+layerTitle+"\" "+Messages.getString("LayerPolygonEditorView.23"));					 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}

				// options if no polygon is selected/active
			if ( polygonEditorControl.getActivePolygon() == null ) {
				switchToPolygonLayerMenuItem.setEnabled(false);
				deletePolygonItem.setEnabled(false);
				toFreeDesignItem.setEnabled(false);
				toFreeDesignItem.setVisible(false);
				openPolygonPathItem.setEnabled(false);
				openPolygonPathItem.setVisible(false);
				openPolygonPathItem.setSelected(false);
				copyToClipboardItem.setEnabled(false);
			} else { // options if a polygon is selected/active
				copyToClipboardItem.setEnabled(true);
				deletePolygonItem.setEnabled(true);
				if ( (polygonEditorControl.getActivePolygon().getType() == HiPolygonTypes.HI_FREEDESIGN) || (polygonEditorControl.getActivePolygon().getType() == HiPolygonTypes.HI_FREEHAND)) {
					addToLibraryItem.setEnabled(true && HIRuntime.getGui().checkEditAbility(true));
					openPolygonPathItem.setEnabled(true);
					openPolygonPathItem.setVisible(true);
					toFreeDesignItem.setEnabled(false);
					toFreeDesignItem.setVisible(false);
				} else {
					openPolygonPathItem.setEnabled(false);
					openPolygonPathItem.setVisible(false);
					toFreeDesignItem.setEnabled(true);
					toFreeDesignItem.setVisible(true);
				}
				if ( polygonEditorControl.getActivePolygon().isClosed() )
					openPolygonPathItem.setSelected(false);
				else
					openPolygonPathItem.setSelected(true);
			}
			discardLayerPolygonChanges.setIcon(icon);
		}
	}
	

	private void initComponents() {
            editorPanel = new JPanel();
            polygonEditorControl = new PolygonEditorControl(image, layers);
            polygonEditorControl.view = this; // DEBUG
            polygonScroll = new JScrollPane(polygonEditorControl);
            polygonScroll.setPreferredSize(new Dimension(400, 400));
            zoomSliderLabel = new JLabel();
            zoomSlider = new JSlider();
            zoomInfoLabel = new JLabel();
            minusLabel = new JLabel();
            plusLabel = new JLabel();
        
            zoomSlider.setSnapToTicks(true);
            zoomSlider.setPaintTicks(false);
            zoomSlider.setPaintLabels(false);
            zoomSlider.setOrientation(JSlider.VERTICAL);
            zoomSlider.setMinimum(1);
            zoomSlider.setMaximum(100);

            isolationModeButton = new JToggleButton();
            isolationModeButton.setBorder(BorderFactory.createEmptyBorder());
            isolationModeButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/isolationmode-icon.png"))); //$NON-NLS-1$
            isolationModeButton.setSelectedIcon(new ImageIcon(getClass().getResource("/resources/icons/isolationmode-icon-active.png")));
            isolationModeButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/isolationmode-icon-disabled.png")));
            isolationModeButton.setPreferredSize(new Dimension(24, 24));
            
            toolButtonsPanel = new JPanel();
            polygonButton = new JToggleButton();
            polygonButton.setBorder(BorderFactory.createEmptyBorder());
            polygonButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/layerEditor.png"))); //$NON-NLS-1$
            polygonButton.setSelectedIcon(new ImageIcon(getClass().getResource("/resources/icons/layerEditor-active.png")));
            polygonButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/layerEditor-disabled.png"))); //$NON-NLS-1$
            polygonButton.setPreferredSize(new Dimension(24, 24));
            polygonButton.setActionCommand("ADD_FREEFORM");

            flowingFreeformButton = new JToggleButton();
            flowingFreeformButton.setBorder(BorderFactory.createEmptyBorder());
            flowingFreeformButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/freeform-icon.png"))); //$NON-NLS-1$
            flowingFreeformButton.setSelectedIcon(new ImageIcon(getClass().getResource("/resources/icons/freeform-icon-active.png"))); //$NON-NLS-1$
            flowingFreeformButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/freeform-icon-disabled.png"))); //$NON-NLS-1$
            flowingFreeformButton.setPreferredSize(new Dimension(24, 24));
            flowingFreeformButton.setActionCommand("ADD_FREE");

            rectangleButton = new JToggleButton();
            rectangleButton.setBorder(BorderFactory.createEmptyBorder());
            rectangleButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/box-icon.png"))); //$NON-NLS-1$
            rectangleButton.setSelectedIcon(new ImageIcon(getClass().getResource("/resources/icons/box-icon-active.png"))); //$NON-NLS-1$
            rectangleButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/box-icon-disabled.png"))); //$NON-NLS-1$
            rectangleButton.setPreferredSize(new Dimension(24, 24));
            rectangleButton.setActionCommand("ADD_RECTANGLE");

            circleButton = new JToggleButton();
            circleButton.setBorder(BorderFactory.createEmptyBorder());
            circleButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/circle-icon.png"))); //$NON-NLS-1$
            circleButton.setSelectedIcon(new ImageIcon(getClass().getResource("/resources/icons/circle-icon-active.png"))); //$NON-NLS-1$
            circleButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/circle-icon-disabled.png"))); //$NON-NLS-1$
            circleButton.setPreferredSize(new Dimension(24, 24));
            circleButton.setActionCommand("ADD_CIRCLE");

            arrowButton = new JToggleButton();
            arrowButton.setBorder(BorderFactory.createEmptyBorder());
            arrowButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/arrow-icon.png"))); //$NON-NLS-1$
            arrowButton.setSelectedIcon(new ImageIcon(getClass().getResource("/resources/icons/arrow-icon-active.png"))); //$NON-NLS-1$
            arrowButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/arrow-icon-disabled.png"))); //$NON-NLS-1$
            arrowButton.setPreferredSize(new Dimension(24, 24));
            arrowButton.setActionCommand("ADD_ARROW");

            zoomSliderLabel.setLabelFor(zoomSlider);
            minusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            minusLabel.setText("-");
            minusLabel.setPreferredSize(new Dimension(24, 12));

            plusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            plusLabel.setText("+");
            plusLabel.setPreferredSize(new Dimension(24, 12));
        
            GroupLayout toolButtonsPanelLayout = new GroupLayout(toolButtonsPanel);
            toolButtonsPanel.setLayout(toolButtonsPanelLayout);
            toolButtonsPanelLayout.setHorizontalGroup(
                    toolButtonsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(toolButtonsPanelLayout.createParallelGroup(GroupLayout.TRAILING, false)
                            .add(GroupLayout.LEADING, zoomSlider, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .add(GroupLayout.LEADING, polygonButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(GroupLayout.LEADING, flowingFreeformButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(GroupLayout.LEADING, rectangleButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(GroupLayout.LEADING, circleButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(GroupLayout.LEADING, arrowButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(GroupLayout.LEADING, isolationModeButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(minusLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(plusLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            );
            toolButtonsPanelLayout.setVerticalGroup(
                    toolButtonsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(toolButtonsPanelLayout.createSequentialGroup()
                            .add(polygonButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(2, 2, 2)
                            .add(flowingFreeformButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(2, 2, 2)
                            .add(rectangleButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(2, 2, 2)
                            .add(circleButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(2, 2, 2)
                            .add(arrowButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(12, 12, 12)
                            .add(isolationModeButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .add(24, 24, 24)
                            .add(plusLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.RELATED)
                            .add(zoomSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.RELATED)
                            .add(minusLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            GroupLayout editorPanelLayout = new GroupLayout(editorPanel);
            editorPanel.setLayout(editorPanelLayout);
            editorPanelLayout.setHorizontalGroup(
                    editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(editorPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .add(toolButtonsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.RELATED)
                            .add(editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
                                    .add(editorPanelLayout.createSequentialGroup()
                                            .add(zoomSliderLabel)
                                            .addPreferredGap(LayoutStyle.RELATED)
                                            .add(zoomInfoLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
                                            .add(0, 0, Short.MAX_VALUE))
                                    .add(polygonScroll, GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE))
                            .addContainerGap())
            );
            editorPanelLayout.setVerticalGroup(
                    editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.TRAILING, editorPanelLayout.createSequentialGroup()
                            .add(20, 20, 20)
                            .add(editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
                                    .add(polygonScroll, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(toolButtonsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(LayoutStyle.RELATED)
                            .add(editorPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                                    .add(zoomSliderLabel)
                                    .add(zoomInfoLabel))
                            .add(19, 19, 19))
            );

            // -----

            newPolygonMenu = new JMenu();
            deletePolygonItem = new JMenuItem();
            toFreeDesignItem = new JMenuItem();
            openPolygonPathItem = new JCheckBoxMenuItem();
            pasteFromClipboardItem = new JMenuItem();
            addToLibraryItem = new JMenuItem();
            discardLayerPolygonChanges = new JMenuItem();
            discardAllPolygonChanges = new JMenuItem();
            newLayerItem = new JMenuItem();
    	
            layerIDMenuItem = new JMenuItem();
            layerIDMenuItem.setEnabled(false);
            switchToPolygonLayerMenuItem = new JMenuItem();
            switchToPolygonLayerMenuItem.setActionCommand("SWITCH_TO_ACTIVE_LAYER"); //$NON-NLS-1$
            deletePolygonItem.setActionCommand("DELETE_POLYGON"); //$NON-NLS-1$
            toFreeDesignItem.setActionCommand("TO_FREE_DESIGN"); //$NON-NLS-1$
            openPolygonPathItem.setActionCommand("OPEN_PATH"); //$NON-NLS-1$
            copyToClipboardItem = new JMenuItem(Messages.getString("LayerPolygonEditorView.4")); //$NON-NLS-1$
            copyToClipboardItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/copy-menu.png"))); //$NON-NLS-1$
            copyToClipboardItem.setActionCommand("COPY_CLIPBOARD"); //$NON-NLS-1$
            pasteFromClipboardItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/paste-menu.png"))); //$NON-NLS-1$
            pasteFromClipboardItem.setActionCommand("PASTE_CLIPBOARD");         //$NON-NLS-1$
            addToLibraryItem.setActionCommand("TO_LIBRARY"); //$NON-NLS-1$
            discardLayerPolygonChanges.setActionCommand("DISCARD_LAYER_POLYGON_CHANGES"); //$NON-NLS-1$
            discardAllPolygonChanges.setActionCommand("DISCARD_ALL_POLYGON_CHANGES"); //$NON-NLS-1$
            // options for empty layer popup menu
            newLayerItem.setActionCommand("add"); //$NON-NLS-1$

	}


	
	// ---------------------------------------------------------------------------------------------
	

	/**
	 * handle zoom level change events
	 */
	public void stateChanged(ChangeEvent e) {
            if (e.getSource() == zoomSlider) {
                // handle zoom event
                int value = zoomSlider.getValue();
                float unit = (2f - minimumScale) / 100f;
                float newScale = unit * ((float) value) + minimumScale;

                if (newScale != scale) {
                    // adjust viewport so the user view is centered on the same location
                    int centerX = polygonScroll.getViewport().getViewPosition().x + (polygonScroll.getViewport().getSize().width / 2);
                    int centerY = polygonScroll.getViewport().getViewPosition().y + (polygonScroll.getViewport().getSize().height / 2);

                    centerX = (int) (((image.getWidth() * newScale) * centerX) / (image.getWidth() * scale));
                    centerY = (int) (((image.getHeight() * newScale) * centerY) / (image.getHeight() * scale));

                    Point viewPoint = new Point(
                            centerX - (polygonScroll.getViewport().getSize().width / 2),
                            centerY - (polygonScroll.getViewport().getSize().height / 2));
                    // set info
                    zoomInfoLabel.setText((int) (newScale * 100) + " %"); //$NON-NLS-1$
                    // set view
                    scale = newScale;
                    polygonEditorControl.scaleView(scale);
                    // set viewport
                    polygonScroll.getViewport().setViewPosition(viewPoint);

                }
            }


	}

        // ---------------------------------------------------------------------------------------------

	
	public void mouseClicked(MouseEvent e) {
            setFocus();
	}

        public void mouseEntered(MouseEvent e) {
            setFocus();
	}

	public void mouseExited(MouseEvent e) {
		// not needed
	}
        
        public void mouseDragged(MouseEvent e) {
            setFocus();
            int divX, divY;
            // calculate distance of drag
            divX = e.getX()-posX;
            divY = e.getY()-posY;

            if ( polygonEditorViewCommand.length() == 0 && polygonEditorControl.getActivePolygon() == null && !polygonEditorControl.isResizing() ) {
                polygonScroll.getViewport().setViewPosition(new Point(
                    Math.min(polygonEditorControl.getWidth()-polygonScroll.getViewport().getWidth(), Math.max(0, polygonScroll.getViewport().getViewPosition().x-divX)), 
                    Math.min(polygonEditorControl.getHeight()-polygonScroll.getViewport().getHeight(), Math.max(0, polygonScroll.getViewport().getViewPosition().y-divY)))
                );
            }

        }
        public void mouseMoved(MouseEvent e) {
            // not needed
        }

    /**
     * mouse event: mouse pressed
     * - display popup menu if necessary
     */
    public void mousePressed(MouseEvent e) {
        setFocus();
        posX = e.getX();
        posY = e.getY();

        if (e.isPopupTrigger()) {
            e.consume();
            popupX = e.getX();
            popupY = e.getY();
            setPopupMenuState();
            polygonEditorControl.selectActivePolygon();
            polygonEditorControl.getPopupMenu().show(this, popupX - polygonScroll.getViewport().getViewPosition().x, popupY - polygonScroll.getViewport().getViewPosition().y + 15);
        } else {
            if (polygonEditorViewCommand.equalsIgnoreCase("add_freeform")) addFreeFormPolygon(userSelectedLayer);
            if (polygonEditorViewCommand.equalsIgnoreCase("add_rectangle")) addRectanglePolygon(userSelectedLayer);
            if (polygonEditorViewCommand.equalsIgnoreCase("add_free")) addFreePolygon(userSelectedLayer);
            if (polygonEditorViewCommand.equalsIgnoreCase("add_circle")) addCirclePolygon(userSelectedLayer);
            if (polygonEditorViewCommand.equalsIgnoreCase("add_arrow")) addArrowPolygon(userSelectedLayer);
        }
    }

	/**
	 * mouse event: mouse released
	 * - display popup menu if necessary
	 */
        @Override
	public void mouseReleased(MouseEvent e) {
            setFocus();
		if ( !e.isConsumed() ) if ( e.isPopupTrigger() ) {
			popupX = e.getX();
			popupY = e.getY();
			setPopupMenuState();
			polygonEditorControl.selectActivePolygon();
			polygonEditorControl.getPopupMenu().show(this, popupX-polygonScroll.getViewport().getViewPosition().x, popupY-polygonScroll.getViewport().getViewPosition().y+15);
		}
	}

	// ---------------------------------------------------------------------------------------------

        /**
         * mouse wheel event: scroll wheel moves
         * - adjust image zoom level
         */
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
                zoomSlider.setValue(Math.min(Math.max(1, zoomSlider.getValue()+e.getWheelRotation()), 100));                
        }
        
	// ---------------------------------------------------------------------------------------------
	
        /*
         * handle isolation mode and tool button events
         */
	public void actionPerformed(ActionEvent e) {

            if ( e.getSource() == isolationModeButton ) {
                polygonEditorControl.setIsolationMode(isolationModeButton.isSelected());
                return;
            }
            
            // add polygon
            if ( e.getSource() == polygonButton ) {
                if ( !polygonButton.isSelected() ) {
                    polygonEditorViewCommand = "deselect";
                    setPlaceFirstAnchorMode(false);
                    updateToolbarButtons();
                } else {
                    polygonEditorViewCommand = "add_freeform";
                    setPlaceFirstAnchorMode(true);
                    updateToolbarButtons();
                }
                return;
            }
            // add free flowing polygon
            if ( e.getSource() == flowingFreeformButton ) {
                if ( !flowingFreeformButton.isSelected() ) {
                    polygonEditorViewCommand = "deselect";
                    setPlaceFirstAnchorMode(false);
                    updateToolbarButtons();
                } else {
                    polygonEditorViewCommand = "add_free";
                    setPlaceFirstAnchorMode(true);
                    updateToolbarButtons();
                }
                return;
            }
            // add rectangle polygon
            if ( e.getSource() == rectangleButton ) {
                if ( !rectangleButton.isSelected() ) {
                    polygonEditorViewCommand = "deselect";
                    setPlaceFirstAnchorMode(false);
                    updateToolbarButtons();
                } else {
                    polygonEditorViewCommand = "add_rectangle";
                    setPlaceFirstAnchorMode(true);
                    updateToolbarButtons();
                }
                return;
            }
            // add circle polygon
            if ( e.getSource() == circleButton ) {
                if ( !circleButton.isSelected() ) {
                    polygonEditorViewCommand = "deselect";
                    setPlaceFirstAnchorMode(false);
                    updateToolbarButtons();
                } else {
                    polygonEditorViewCommand = "add_circle";
                    setPlaceFirstAnchorMode(true);
                    updateToolbarButtons();
                }
                return;
            }
            // add arrow polygon
            if ( e.getSource() == arrowButton ) {
                if ( !arrowButton.isSelected() ) {
                    polygonEditorViewCommand = "deselect";
                    setPlaceFirstAnchorMode(false);
                    updateToolbarButtons();
                } else {
                    polygonEditorViewCommand = "add_arrow";
                    setPlaceFirstAnchorMode(true);
                    updateToolbarButtons();
                }
                return;
            }

            if (e.getActionCommand().equalsIgnoreCase("DELETE_POLYGON")) {
                polygonEditorControl.deleteSelectedPolygon();
            }
            if (polygonEditorControl.getActivePolygon() != null) {
                if (e.getActionCommand().equalsIgnoreCase("TO_FREE_DESIGN")) //$NON-NLS-1$
                {
                    polygonEditorControl.convertActivePolygonToFreeDesign();
                } else if (e.getActionCommand().equalsIgnoreCase("OPEN_PATH")) { //$NON-NLS-1$
                    polygonEditorControl.setActivePolygonOpen(openPolygonPathItem.isSelected());
                    openPolygonPathItem.setSelected(openPolygonPathItem.isSelected());
                }
            }
            if (e.getActionCommand().equalsIgnoreCase("DISCARD_LAYER_POLYGON_CHANGES") ) { //$NON-NLS-1$
			if ( userSelectedLayer != null ) userSelectedLayer.resetPolygonChanges();
			polygonEditorControl.repaintShapes();
		} else  if ( e.getActionCommand().equalsIgnoreCase("DISCARD_ALL_POLYGON_CHANGES") ) { //$NON-NLS-1$
			for ( HILayer layer : layers )
				layer.resetPolygonChanges();
			polygonEditorControl.repaintShapes();
		} else if ( e.getActionCommand().equalsIgnoreCase("COPY_CLIPBOARD") ) {
                    HIRuntime.copyToClipboard(polygonEditorControl.getSelectedPolygon().getChangedModel());
                } else if ( e.getActionCommand().equalsIgnoreCase("PASTE_CLIPBOARD") && layers.size() > 0 && ! HIRuntime.isClipboardEmpty()) { //$NON-NLS-1$
			if ( polygonEditorControl.getActivePolygon() == null )
				polygonEditorControl.addPolygon(userSelectedLayer, HIRuntime.pasteFromClipboard(), popupX, popupY);
			else {
				HILayer polygonLayer = getActiveLayer();
				if ( polygonLayer != null ) 
					polygonEditorControl.addPolygon(polygonLayer, HIRuntime.pasteFromClipboard(), popupX, popupY);
			}
		}
	}

        
    // ---------------------------------------------------------------------------------------------


    @Override
    public void keyTyped(KeyEvent e) {
        // not needed
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ( e.getKeyCode() == KeyEvent.VK_BACK_SPACE ) polygonEditorControl.deleteSelectedPolygon();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // not needed
    }

   
}
