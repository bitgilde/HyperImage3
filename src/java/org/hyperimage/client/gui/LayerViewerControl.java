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

package org.hyperimage.client.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.Vector;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.model.HIRichText;
import org.hyperimage.client.model.RelativePolygon;
import org.hyperimage.client.util.ImageHelper;
import org.hyperimage.client.util.LoadableImage;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiLayer;

import com.sun.media.jai.widget.DisplayJAI;

/**
 * @author Jens-Martin Loebel
 */
public class LayerViewerControl extends DisplayJAI implements LoadableImage, HierarchyBoundsListener {

	private static final long serialVersionUID = -8628438088116965512L;

	
	private boolean needsPreview = true;
	private boolean displayLayers = false;

	private List<HiLayer> modelLayers;
	private Vector<HILayer> layers;
	private HILayer activeLayer = null;
	private HILayer selectedLayer = null;
	private PlanarImage image = null;

	// static constants, used to draw the outline of a polygon
	private static final BasicStroke solidStroke = new BasicStroke(1);
	// constant for polygon filling texture
	private GeneralPath combinedPath; // combined path of all polygons in a layer (except open polygons)

	private float curScale = 1.0f;
	private boolean isDragAndDrop = false;
	
	// popup menu
	JPopupMenu popupMenu;
	JMenuItem titleMenuItem;
	JMenuItem visitLinkMenuItem;
	JMenuItem editLayerMenuItem;
	JSeparator separator;
	JMenuItem removeLinkMenuItem;
		
	
	public LayerViewerControl() {
		this.layers = new Vector<HILayer>();
		this.addMouseMotionListener(this);
		this.set(JAI.create("url", getClass().getResource("/resources/hyperimage-preview_loading.png"))); //$NON-NLS-1$ //$NON-NLS-2$
		
		initMenus();
		updateLanguage();
		
		// attach listeners
		this.addHierarchyBoundsListener(this);
		this.addMouseListener(this);
	}

	public void setPreviewLoading() {
		needsPreview = true;
	}
	
	public void updateLanguage() {
		visitLinkMenuItem.setText(Messages.getString("LayerViewerControl.18")); //$NON-NLS-1$
		editLayerMenuItem.setText(Messages.getString("LayerViewerControl.20")); //$NON-NLS-1$
		removeLinkMenuItem.setText(Messages.getString("LayerViewerControl.22")); //$NON-NLS-1$
	}
	
	
	public void initLayers(List<HiLayer> modelLayers, String sortOrder) {
		// default preview size
		int scaleX = 400;
		int scaleY = 400;

		this.modelLayers = modelLayers;
		layers.removeAllElements();
		
		// wrap model
		this.combinedPath = new GeneralPath();
		combinedPath.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		for ( HiLayer modelLayer : this.modelLayers )
			layers.add(new HILayer(modelLayer, scaleX, scaleY));
		
		sortLayers(sortOrder); // sort layers
		
		if ( needsPreview() ) {
			displayLayers = false;
			this.set(JAI.create("url", getClass().getResource("/resources/hyperimage-preview_loading.png"))); //$NON-NLS-1$ //$NON-NLS-2$
		} else scalePreviewImage();
	}

	public HILayer getActiveLayer() {
		if ( displayLayers ) return activeLayer;
		else return null;
	}

	public HILayer getSelectedLayer() {
		if ( displayLayers ) return selectedLayer;
		else return null;
	}

	public void paint(Graphics g) {
		super.paint(g);

		if ( displayLayers ) {
			Graphics2D g2d = (Graphics2D)g;

			// search all layers for polygons
			// do this in reverse order so layers at the top of the list will be drawn on top of other layers
			for (int count=layers.size()-1; count>=0; count--) {
				HILayer layer = layers.get(count);

				// construct combined polygon path of all polygons on current layer
				combinedPath.reset();
				for (RelativePolygon polygon : layer.getRelativePolygons())
					if ( polygon.isClosed() )  // only add closed polygon paths
						combinedPath.append(polygon.getPolygonPath(), false);
				// draw inside of all closed polygons on this layer

				// fill area for currently active layer (mouse over) if any
				if ( layer == activeLayer ) {
					int opacity = layer.getColour().getAlpha() + 100;
					if ( opacity > 255 ) opacity = 255;
					
					// DEBUG 
					opacity = 255;
					
					Color selColor = new Color(layer.getColour().getRed(), layer.getColour().getGreen(), layer.getColour().getBlue(), opacity);
					g2d.setColor(selColor);
				} else g2d.setColor(layer.getColour());
				g2d.fill(combinedPath);

				// draw outline of all polygons on this layer, anchors for active polygon, open polygons
				for (RelativePolygon polygon : layer.getRelativePolygons()) {

					// draw polygon outline (solid line)
					g2d.setStroke(solidStroke);
					
					if ( layer == activeLayer ) {
						if ( isDragAndDrop ) g2d.setColor(Color.white);
						else g2d.setColor(Color.black);
					}
					else g2d.setColor(layer.getColour().darker());
					g2d.draw(polygon.getPolygonPath());

				}
			}
		}
	}


	public boolean needsPreview() {
		return needsPreview;
	}

	public void setPreviewImage(PlanarImage image) {
		if ( image != null ) {
			this.set(image);
			this.image = image;
			curScale = 1.0f;
			displayLayers = true;
			needsPreview = false;
			updateScale();
		} else {
			// preview not found or load error
			PlanarImage errorImage = 
				JAI.create("url", getClass().getResource("/resources/icons/preview-loaderror.png"));  //$NON-NLS-1$ //$NON-NLS-2$
			this.set(errorImage);
			needsPreview = false;
		}	
	}
	
	public void attachActionListener(ActionListener listener) {
		visitLinkMenuItem.addActionListener(listener);
		editLayerMenuItem.addActionListener(listener);
		removeLinkMenuItem.addActionListener(listener);
	}

	private void showPopupMenu(int x, int y) {
		if ( displayLayers ) {
			setMenuState();
			popupMenu.show(this, x, y);
		}
	}
	
	private void setMenuState() {
		String title = ""; //$NON-NLS-1$
		if ( activeLayer != null ) {
			title = MetadataHelper.findValue("HIBase", "title",  //$NON-NLS-1$ //$NON-NLS-2$
					MetadataHelper.getDefaultMetadataRecord(activeLayer.getModel(), HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()));
			if ( title == null || title.length() == 0 ) title = "L"+activeLayer.getModel().getId(); //$NON-NLS-1$
		}

		if ( activeLayer == null || activeLayer.getModel().getLinkInfo() == null ) {
			visitLinkMenuItem.setVisible(false);
			editLayerMenuItem.setVisible(activeLayer != null ? true : false);
			editLayerMenuItem.setEnabled(HIRuntime.getGui().checkEditAbility(true));
			removeLinkMenuItem.setVisible(false);
			separator.setVisible(false);
			if ( activeLayer == null )
				titleMenuItem.setText(Messages.getString("LayerViewerControl.10")); //$NON-NLS-1$
			else
				titleMenuItem.setText(Messages.getString("LayerViewerControl.11")+" \""+title+"\" "+Messages.getString("LayerViewerControl.14"));				 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		} else {
			titleMenuItem.setText(Messages.getString("LayerViewerControl.15")+" \""+title+"\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			visitLinkMenuItem.setVisible(true);
			editLayerMenuItem.setEnabled(true);
			editLayerMenuItem.setVisible(activeLayer != null ? true : false);
			editLayerMenuItem.setEnabled(HIRuntime.getGui().checkEditAbility(true));
			removeLinkMenuItem.setVisible(true);
			removeLinkMenuItem.setEnabled(HIRuntime.getGui().checkEditAbility(true));
			separator.setVisible(true);
		}
	}
	
	private void initMenus() {
		popupMenu = new JPopupMenu();
		titleMenuItem = new JMenuItem();
		titleMenuItem.setEnabled(false);
		popupMenu.add(titleMenuItem);
		popupMenu.add(new JSeparator());
		visitLinkMenuItem = new JMenuItem();
		visitLinkMenuItem.setActionCommand("openLinkTarget"); //$NON-NLS-1$
		popupMenu.add(visitLinkMenuItem);
		editLayerMenuItem = new JMenuItem();
		editLayerMenuItem.setActionCommand("editLayer"); //$NON-NLS-1$
		popupMenu.add(editLayerMenuItem);
		separator = new JSeparator();
		popupMenu.add(separator);
		removeLinkMenuItem = new JMenuItem();
		removeLinkMenuItem.setActionCommand("removeLink"); //$NON-NLS-1$
		popupMenu.add(removeLinkMenuItem);
	}
	
	private void sortLayers(String sortOrder) {
		long layerID;
		int index = 0;
		int contentIndex;

		// parse sort order string (don´t trust user input)
		for ( String contentIDString : sortOrder.split(",") ) { //$NON-NLS-1$
			try {
				layerID = Long.parseLong(contentIDString);
				// find content belonging to the parsed id
				contentIndex = -1;
				
				for ( int i=0; i<layers.size(); i++ )
					if ( layers.get(i).getModel().getId() == layerID )
						contentIndex = i;
				// if content was found, sort to new index
				if ( contentIndex >= 0 ) if ( contentIndex != index ) {
					HILayer layer = layers.get(contentIndex);
					layers.remove(contentIndex);
					layers.add(index, layer);
					index = index+1;
				} else index = index + 1;
			} catch (NumberFormatException e) {}; // user messed with sort order string format, no problem
		}
	}
	
	private void scalePreviewImage() {
		if ( displayLayers ) {
			PlanarImage scaledImage = ImageHelper.scaleImageTo(image, curScale);
			this.set(scaledImage);
			for ( HILayer layer : layers )
				layer.setScale(scaledImage.getWidth(), scaledImage.getHeight());
		}
	}
	
	/**
	 * internal method: sets the logical GUI state of the polygon editor (which polygon is active, etc...)
	 * in dependence of current mouse position (x, y)
	 * called by mouse event handler
	 * 
	 * @param x mouse x coordinate
	 * @param y mouse y coordinate
	 */
	private void setViewingState(int x, int y, boolean isDragAndDrop) {
		// don´t update state if the user is choosing options
		if ( popupMenu.isVisible() ) return;
		
		HILayer oldLayerState = activeLayer; // preserve previous state
		this.isDragAndDrop = isDragAndDrop;

		// reset state
		activeLayer = null;

		// search all layers for polygons
		// do this in reverse order so layers at the top of the list will be drawn on top of other layers
		for (int count=layers.size()-1; count>=0; count--) {
			HILayer layer = layers.get(count);
			// scan all polygons of current layer
			for (RelativePolygon polygon : layer.getRelativePolygons()) {
				if ( polygon.getPolygonPath().contains(x, y) ) {
					// is polygon inside the current polygon?
					activeLayer = layer;
				}
			}
		}
		if ( oldLayerState != activeLayer ) {
			// set cursor
			if ( ! isDragAndDrop ) {
				if ( activeLayer != null && activeLayer.getModel().getLinkInfo() != null) 
					this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				else 
					this.setCursor(Cursor.getDefaultCursor());
			}
			// set tooltip text
			if ( activeLayer == null ) this.setToolTipText(""); //$NON-NLS-1$
			else {
				String infoText = ""; //$NON-NLS-1$
				infoText = MetadataHelper.findValue("HIBase", "title",  //$NON-NLS-1$ //$NON-NLS-2$
						MetadataHelper.getDefaultMetadataRecord(activeLayer.getModel(), HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()));
				if ( infoText == null || infoText.length() == 0 ) infoText = "L"+activeLayer.getModel().getId(); //$NON-NLS-1$
				
				String comment = new HIRichText(
						MetadataHelper.findValue(
								"HIBase", "comment",  //$NON-NLS-1$ //$NON-NLS-2$
								MetadataHelper.getDefaultMetadataRecord(
										activeLayer.getModel(), 
										HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()
								)
						)).getHTMLModel();
				comment = comment.replaceAll("<[/]?html>", ""); //$NON-NLS-1$ //$NON-NLS-2$
				
				infoText = "<html><b>"+Messages.getString("LayerViewerControl.0")+": "+infoText+"</b><br><br>"+comment+"</html>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				this.setToolTipText(infoText);
			}
			
			repaint(); // repaint polygons if state has changed
		}
	}

	public void setState(int x, int y) {
		setViewingState(x, y, true);
	}

	
	
	private void updateScale() {
		int width = this.getSize().width;
		int height = this.getSize().height;
		int max = Math.max(image.getWidth(), image.getHeight());
		int scale = Math.min(width-image.getWidth(), height-image.getHeight());
		float relScale = (float)(max+scale)/(float)max;
		if ( relScale != curScale ) {
			curScale = relScale;
			scalePreviewImage();
		}
	}
	
	// -------- mouse event handler ----------

	
	/**
	 * mouse handler: mouse move --> update logical GUI state and repaint polygons if necessary
	 */
	public void mouseMoved(MouseEvent e) {
		setViewingState(e.getX(), e.getY(), false);
	}
	
	/**
	 * mouse handler: mouse drag --> update logical GUI state and repaint polygons if necessary
	 */
	public void mouseDragged(MouseEvent e) {
		setViewingState(e.getX(), e.getY(), false);
	}
	
	public void mouseExited(MouseEvent e) {
		if ( popupMenu.isVisible() ) return;
		activeLayer = null;
		this.setCursor(Cursor.getDefaultCursor());
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
		setViewingState(e.getX(), e.getY(), false);
	}
	
	public void mousePressed(MouseEvent e) {
		setViewingState(e.getX(), e.getY(), false);
		if ( e.getSource() == this && e.isPopupTrigger() && !e.isConsumed() ) {
			e.consume();
			selectedLayer = activeLayer;
			showPopupMenu(e.getX(), e.getY());
		}
	}	

	public void mouseReleased(MouseEvent e) {
		setViewingState(e.getX(), e.getY(), false);
		if ( e.getSource() == this && e.isPopupTrigger() && !e.isConsumed() ) {
			e.consume();
			selectedLayer = activeLayer;
			showPopupMenu(e.getX(), e.getY());
		}
	}	


	// -------- hiearchy bounds (resize) event handler ----------
	
	public void ancestorMoved(HierarchyEvent e) {
	}

	public void ancestorResized(HierarchyEvent e) {
		if ( displayLayers )
			updateScale();
	}
}
