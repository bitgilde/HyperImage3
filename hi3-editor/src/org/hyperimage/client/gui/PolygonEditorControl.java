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

package org.hyperimage.client.gui;

import com.sun.media.jai.widget.DisplayJAI;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.gui.views.LayerPolygonEditorView;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.model.HIRichText;
import org.hyperimage.client.model.RelativePolygon;
import org.hyperimage.client.model.RelativePolygon.HiPolygonTypes;
import org.hyperimage.client.model.RelativePolygon.PolygonResizeCorners;
import org.hyperimage.client.util.ImageHelper;
import org.hyperimage.client.util.MetadataHelper;


/**
* Class: PolygonEditorControl
* Package: org.hyperimage.client.gui
* @author Jens-Martin Loebel & Uf Schoeneberg
*
* GUI implementation of the Polygon Editor
* Takes a List of HiLayer model and wraps it using the @see org.hyperimage.client.model.HILayer elemtents
* 
* This class can display all layer polygons on top of a given JAI image. In addition the user can interact
* with the polygons:
* - add/remove points
* - move polygon (translation, rotation)
* - open/close the polygon path
* - write changes back to the model or discard changes
* - scale the image
* 
* Polygons/layer properties and creation/destruction of layers and polygons is handled by the @see LayerPolygonEditorView class.
* 
*/
public class PolygonEditorControl extends DisplayJAI implements PopupMenuListener {
	
	private static final long serialVersionUID = 584425494646670381L;

	
	private PlanarImage image;
	private float scale = 1.0f;
	private Vector<HILayer> layers;
	
	// static constants, used to draw the outline of a polygon
	private static final float polygonDash[] = {4.0f,4.0f};
	public static final BasicStroke selectStrokeWhite = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, polygonDash, 0.0f);
	public static final BasicStroke selectStrokeBlack = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, polygonDash, 4.0f);
	public static final BasicStroke activeStrokeWhite = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, polygonDash, 0.0f);
	public static final BasicStroke activeStrokeBlack = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, polygonDash, 4.0f);
	public static final BasicStroke selectStrokeSolid = new BasicStroke(1);
	public static final BasicStroke activeStrokeSolid = new BasicStroke(2);
	// constant for polygon filling texture
	private static TexturePaint openPolygonTexturePaint;
	// custom GUI mouse cursors
	private Cursor crossAddCursor, crossRemoveMoveCursor, crossClosePathCursor, crossMoveCursor;

	// mouse and editing state variables
	private int dragX = 0, dragY = 0; // coordinates used to calculate how far the mouse cursor was dragged
	private int dragAnchor = -1; // is mouse on a polygon anchor (no == -1, yes == anchor index)
	private PolygonResizeCorners resizeAnchor = PolygonResizeCorners.NONE; // is mouse on selected polygon resize anchor
	private int dragLinePoint = -1; // is mouse on line between 2 polygon anchor points (s. a.)
	private boolean anchorCreated = false; // anchor created during last mouse pressed event?
	private boolean foundOpenPolygon = false; // open polygon path found in set of polygons
	private RelativePolygon activePolygon; // currently active polygon (mouse over)
        private RelativePolygon lastPolygon;
	private RelativePolygon selectedPolygon; // user selected active polygon (mouse click) - has resize frame
	private GeneralPath combinedPath; // combined path of all polygons in a layer (except open polygons)
	private Cursor cursorState; // saved cursor state
	private Image closePathIcon; // last anchor in an open polygon path
	private HILayer userSelectedLayer = null; // user selected layer (in layer list)
	private boolean highlightLayer = false; // should the view highlight polygons on the user selected layer?
	// context menu which is exposed to and filled by the controller object
	private JPopupMenu popupMenu;
        public LayerPolygonEditorView view = null; // DEBUG
        
        private boolean isolationMode; // if true, only display polygons of user selected layer
	private boolean placeFirstAnchorMode;
		
	public PolygonEditorControl(PlanarImage image, Vector<HILayer> layers) {
		this.image = image;
		this.layers = layers;
		this.selectedPolygon = null;
		combinedPath = new GeneralPath();
		combinedPath.setWindingRule(GeneralPath.WIND_EVEN_ODD);
                this.isolationMode = false;
                this.placeFirstAnchorMode = false;


		// load polygon texture
		Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
		try {
			BufferedImage openPolygonTexture = ImageIO.read(
					getClass().getResource("/resources/OpenPolygonTexture.gif"));
			openPolygonTexturePaint = new TexturePaint( openPolygonTexture, new Rectangle2D.Double(0,0,16,16) );

			// load cursor images
			crossMoveCursor = tk.createCustomCursor(
					ImageIO.read(
							getClass().getResource("/resources/cursors/cursorMove.png")), 
							new Point(11,11), "cross-Move");
			crossAddCursor = tk.createCustomCursor(
					ImageIO.read(
							getClass().getResource("/resources/cursors/crossAdd.png")), 
							new Point(13,13), "cross-Add");
			crossRemoveMoveCursor = tk.createCustomCursor(
					ImageIO.read(
							getClass().getResource("/resources/cursors/crossRemoveOrMove.png")), 
							new Point(13,13), "cross-RemoveOrMove");
			crossClosePathCursor = tk.createCustomCursor(
					ImageIO.read(
							getClass().getResource("/resources/cursors/crossClosePath.gif")), 
							new Point(12,12), "cross-ClosePath");
			
			closePathIcon = ImageIO.read(getClass().getResource("/resources/icons/closePath.png"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// init context menu
		popupMenu=new JPopupMenu();

		// add mouse event listeners
		addMouseMotionListener(this);
		addMouseListener(this);	
		popupMenu.addPopupMenuListener(this);
		
		// set display
		this.set(image);
	}
        
        public void setIsolationMode(boolean mode) {
            this.isolationMode = mode;
            if ( getSelectedLayer() != userSelectedLayer ) selectedPolygon = null;
            repaint();
        }
	public void setPlaceFirstAnchorMode(boolean mode) {
            this.placeFirstAnchorMode = mode;
        }
        
        public boolean isResizing() {
            if ( resizeAnchor == PolygonResizeCorners.LOWER_RIGHT || resizeAnchor == PolygonResizeCorners.UPPER_RIGHT )
                return true;
            return false;
        }
	
	/**
	 * Sets a new background image and adjusts the scale of all polygons accordingly.
	 * @param image PlanarImage
	 */
	public void scaleView(float scale) {
		if ( this.scale != scale ) {
			this.scale = scale;

			// scale layers
			PlanarImage scaledImg = ImageHelper.scaleImageTo(image, scale);
			for (HILayer layer : layers)
				layer.setScale(scaledImg.getWidth(),scaledImg.getHeight());
			// display image
			this.set(scaledImg);
			scaledImg.dispose();
		}
	}
	
	
	/**
	 * exposes state of polygons to the @see LayerPolygonEditorView wrapper class
	 * @return {@code true} is an open polygon path was found in any polygon on all layer, {@code false} otherwise
	 */
	public boolean containsOpenPolygon() {
		return foundOpenPolygon;
	}

	/**
	 * 
	 * @param layer
	 */
	public void addFreeFormPolygon(HILayer layer, int x, int y) {
            // close all other open polygons (if any)
            closeAndCleanupPolygons();
		
            RelativePolygon polygon;
            polygon = new RelativePolygon(HiPolygonTypes.HI_FREEDESIGN, x, y, 0,0, null, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale));
            polygon.setClosed(false);
            layer.getRelativePolygons().add(polygon);
		
            setViewingState(x, y, false);
            repaint();
	}

        /**
	 *
	 * @param layer
	 */
	public void addFreePolygon(HILayer layer, int x, int y) {
		// close all other open polygons (if any)
		closeAndCleanupPolygons();

		RelativePolygon polygon;
		polygon = new RelativePolygon(HiPolygonTypes.HI_FREEHAND, x, y, 0,0, null, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale));
		polygon.setClosed(false);
		layer.getRelativePolygons().add(polygon);

		setViewingState(x, y, false);
		repaint();
	}


	/**
	 * 
	 * @param layer
	 */
	public void addRectanglePolygon(HILayer layer, int x, int y) {
		// close all other open polygons (if any)
		closeAndCleanupPolygons();
		
		RelativePolygon polygon;
		polygon = new RelativePolygon(HiPolygonTypes.HI_RECTANGLE, x, y, 120, 50, null, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale));
		polygon.setClosed(true);
		layer.getRelativePolygons().add(polygon);
		
		selectedPolygon = polygon;
		setViewingState(x, y, false);
		repaint();
	}

        /**
	 *
	 * @param layer
	 */
	public void addCirclePolygon(HILayer layer, int x, int y) {
		// close all other open polygons (if any)
		closeAndCleanupPolygons();

		RelativePolygon polygon;
		polygon = new RelativePolygon(HiPolygonTypes.HI_CIRCLE, x, y, 60, 120, null, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale));
		polygon.setClosed(true);
		layer.getRelativePolygons().add(polygon);

		selectedPolygon = polygon;
		setViewingState(x, y, false);
		repaint();
	}


         /**
	 *
	 * @param layer
	 */
	public void addArrowPolygon(HILayer layer, int x, int y) {
		// close all other open polygons (if any)
		closeAndCleanupPolygons();

		RelativePolygon polygon;
		polygon = new RelativePolygon(HiPolygonTypes.HI_ARROW, x, y, 160, 120, null, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale));
		polygon.setClosed(true);
		layer.getRelativePolygons().add(polygon);

		selectedPolygon = polygon;
		setViewingState(x, y, false);
		repaint();
	}


	/**
	 * 
	 * @param layer
	 * @param model
	 */
	public void addLibraryPolygon(HILayer layer, String model, int x, int y) {
		// close all other open polygons (if any)
		closeAndCleanupPolygons();
		
		RelativePolygon polygon;
		polygon = new RelativePolygon(HiPolygonTypes.HI_USERDESIGN, 0, 0, 0, 0, model, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale));
		polygon.setClosed(true);
		// translate library polygon to user specified point
		polygon.translate(x-polygon.getPolygonPath().getBounds().x, y-polygon.getPolygonPath().getBounds().y);
		layer.getRelativePolygons().add(polygon);
		
		selectedPolygon = polygon;
		setViewingState(x, y, false);
		repaint();
	}

	public void addPolygon(HILayer layer, String model, int x, int y) {
		// close all other open polygons (if any)
		closeAndCleanupPolygons();
		
		RelativePolygon polygon;
		polygon = new RelativePolygon(model, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale));
		polygon.setClosed(true);
		// translate library polygon to user specified point
		polygon.translate(x-polygon.getPolygonPath().getBounds().x, y-polygon.getPolygonPath().getBounds().y);
		layer.getRelativePolygons().add(polygon);
		
		selectedPolygon = polygon;
		setViewingState(x, y, false);
		repaint();
	}

    public void deleteActivePolygon() {
        if (activePolygon != null) {
            // find polygon layer
            HILayer polygonLayer = null;
            for (HILayer layer : layers) {
                if (layer.getRelativePolygons().contains(activePolygon)) {
                    polygonLayer = layer;
                }
            }
            if (polygonLayer != null) {
                // remove polygon
                polygonLayer.getRelativePolygons().removeElement(activePolygon);
                if (selectedPolygon == activePolygon) {
                    selectedPolygon = null;
                }
                activePolygon = null;
                repaint();
            }
        }
    }

    public void deleteSelectedPolygon() {
        if (selectedPolygon != null) {
            // find polygon layer
            HILayer polygonLayer = null;
            for (HILayer layer : layers) {
                if (layer.getRelativePolygons().contains(selectedPolygon)) {
                    polygonLayer = layer;
                }
            }
            if (polygonLayer != null) {
                // remove polygon
                polygonLayer.getRelativePolygons().removeElement(selectedPolygon);
                activePolygon = null;
                selectedPolygon = null;
                repaint();
            }
        }
    }

    public void convertActivePolygonToFreeDesign() {
        if (activePolygon != null) {
            activePolygon.convertToFreeDesign();
            repaint();
        }
    }

    public void setActivePolygonOpen(boolean open) {
        if (activePolygon != null) {
            closeAndCleanupPolygons();
            if ( open ) {
                view.polygonEditorViewCommand = "add_freeform_open";
                placeFirstAnchorMode = true;
            } else {
                view.polygonEditorViewCommand = "";
                placeFirstAnchorMode = false;
            }
            view.updateToolbarButtons();
            
            activePolygon.setClosed(!open);
            // DEBUG
            repaint();
        }

    }


	/**
	 * closes all open polygons and removes polygons with only one or 2 anchor points
	 */
	public void closeAndCleanupPolygons() {
            Vector<RelativePolygon> polygonsToRemove = new Vector<RelativePolygon>();
            for (HILayer dispLayer : layers)
                for ( RelativePolygon dispPolygon : dispLayer.getRelativePolygons() ) {
                    if ( ! dispPolygon.isClosed() ) dispPolygon.setClosed(true);
                    if ( dispPolygon.size() < 3 ) polygonsToRemove.addElement(dispPolygon);
		}
		
            // now remove all found polygons with < 3 anchor points
            for (HILayer dispLayer : layers)
                for ( RelativePolygon removePolygon : polygonsToRemove )
                    if ( dispLayer.getRelativePolygons().contains(removePolygon) )
                        dispLayer.getRelativePolygons().removeElement(removePolygon);
	}


	/**
	 * exposes the editor popup menu to the @see LayerPolygonEditorView wrapper class
	 * @return the popup menu for this polygon editor
	 */
	public JPopupMenu getPopupMenu() {
		return this.popupMenu;
	}

	
	public HILayer getSelectedLayer() {
		if ( selectedPolygon == null ) return null;
		
		for ( HILayer layer : layers )
			if ( layer.getRelativePolygons().contains(selectedPolygon) )
				return layer;
		
		return null;
	}
	


	public RelativePolygon getActivePolygon() {
		return activePolygon;
	}

	public RelativePolygon getSelectedPolygon() {
		return selectedPolygon;
	}


	public void selectActivePolygon() {
		selectedPolygon = activePolygon;
		repaint();
	}

	public void setUserSelectedLayer(HILayer layer) {
            userSelectedLayer = layer;
            if ( isolationMode && getSelectedLayer() != userSelectedLayer ) selectedPolygon = null;
            repaintShapes();
	}



	public void repaintShapes() {
		setViewingState(0, 0, false);
		this.repaint();
	}

	public void updateScale() {
		for (HILayer layer : layers)
			layer.setScale(source.getWidth(),source.getHeight());
	}

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public Point[] simplifyRadialDist(Point[] points, double epsilon) {
        Point prevPoint = points[0];
        ArrayList<Point> newPoints = new ArrayList<>();
        newPoints.add(prevPoint);
        Point point = null;

        for (int i = 1; i < points.length; i++) {
            point = points[i];

            if (point.distanceSq(prevPoint) > epsilon) {
                newPoints.add(point);
                prevPoint = point;
            }
        }

        if (!prevPoint.equals(point)) {
            newPoints.add(point);
        }

        return newPoints.toArray(new Point[newPoints.size()]);
    }

    public Point[] simplifyPath(Point[] points, double epsilon) {
        if (activePolygon == null || activePolygon.getType() != HiPolygonTypes.HI_FREEHAND) {
            return points;
        }

        Point firstPoint = points[0];
        Point lastPoint = points[points.length - 1];
        if (points.length < 3) {
            return points;
        }

        int index = -1;
        double dist = 0;

        for (int i = 1; i < points.length - 1; i++) {
            double cDist = findPerpendicularDistance(points[i], firstPoint, lastPoint);
            if (cDist > dist) {
                dist = cDist;
                index = i;
            }
        }

        if (dist > epsilon) {
            // iterate
            Point[] l1 = Arrays.copyOfRange(points, 0, index + 1);
            Point[] l2 = Arrays.copyOfRange(points, index, points.length);
            Point[] r1 = simplifyPath(l1, epsilon);
            Point[] r2 = simplifyPath(l2, epsilon);
            // concat r2 to r1 minus the end/startpoint that will be the same
            Point[] rs = concat(Arrays.copyOfRange(r1, 0, r1.length - 1), r2);
            return rs;
        } else {
            return new Point[] {firstPoint, lastPoint};
        }
    }
    
    public double findPerpendicularDistance(Point p, Point p1, Point p2) {
        double x = p1.x;
        double y = p1.y;
        double dx = p2.x - x;
        double dy = p2.y - y;

        if (dx != 0 || dy != 0) {
            double t = ((p.x - x) * dx + (p.y - y) * dy) / (dx * dx + dy * dy);

            if ( t > 1 ) {
                x = p2.x;
                y = p2.y;

            } else if ( t > 0 ) {
                x += dx * t;
                y += dy * t;
            }
        }
        dx = p.x - x;
        dy = p.y - y;

        return dx * dx + dy * dy;
    }
	
	// DEBUG
	private void highlightPolygon(Graphics2D g2d, Shape clipShape, Color layerColor, int glowWidth) {
		Composite orgComposite = g2d.getComposite();

	    for (int i=glowWidth; i >= 2; i = i - 2) {
	        float opacity = (1f/(float)i);

	        if ( i % 4 != 0 ) g2d.setColor(Color.white);
	        else g2d.setColor(Color.white);
	        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, opacity));
	        
	        g2d.setStroke(new BasicStroke(i));
	        g2d.draw(clipShape);
	    }
		g2d.setComposite(orgComposite);
	}

	
	
	
	
	
	
	
	
	/**
	 * Overrides @see DisplayJAI paint method. Adds the ability to display semi-transparent polygons on top of
	 * a background image.
	 */
	public void paint(Graphics g) {
		super.paint(g);		
		Graphics2D g2d = (Graphics2D) g;

		// search all layers for polygons
		// do this in reverse order so layers at the top of the list will be drawn on top of other layers
		for (int count=layers.size()-1; count>=0; count--) {
			HILayer layer = layers.get(count);

                        // only draw user selected layer in isolation mode
//                        if ( isolationMode && layer != this.userSelectedLayer ) continue;
                        
			// construct combined polygon path of all polygons on current layer
			combinedPath.reset();
			for (RelativePolygon polygon : layer.getRelativePolygons())
				if ( polygon.isClosed() )  // only add closed polygon paths
					combinedPath.append(polygon.getPolygonPath(), false);

			// DEBUG highlight user selected layer (not needed if in isolation mode)
			if ( layer == userSelectedLayer && highlightLayer && !isolationMode )
				highlightPolygon(g2d, combinedPath, layer.getSolidColour(), 10);
			
			// draw inside of all closed polygons on this layer
			g2d.setColor(layer.getColour());
			if ( !isolationMode || layer == this.userSelectedLayer ) g2d.fill(combinedPath);

			
			// draw outline of all polygons on this layer, anchors for active polygon, open polygons
			for (RelativePolygon polygon : layer.getRelativePolygons()) {

                            // if this is an open polygon path: composite open polygon over image and other polygons
                            if ( !polygon.isClosed() ) {
                                Composite orgComposite = g2d.getComposite();
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
				if (openPolygonTexturePaint!= null ) { // sanity check: is custom texture available?
                                    g2d.setPaint(openPolygonTexturePaint);
                                    g2d.fill(polygon.getPolygonPath());
				}
                                    g2d.setComposite(orgComposite);
                            }

                            if ( !isolationMode || layer == userSelectedLayer) {
				if ( (polygon.getType() == HiPolygonTypes.HI_FREEDESIGN) || (polygon.getType() == HiPolygonTypes.HI_FREEHAND) ) {
                                    // draw polygon outline (dashed strokes)
                                    g2d.setStroke(selectStrokeWhite);
                                    g2d.setColor(Color.WHITE);
                                    g2d.draw(polygon.getPolygonPath());
                                    g2d.setStroke(selectStrokeBlack);
                                    g2d.setColor(layer.getColour().darker());
                                    g2d.draw(polygon.getPolygonPath());
				} else {
                                    // draw solid outline for fixed polygons
                                    g2d.setStroke(selectStrokeSolid);
                                    g2d.setColor(layer.getColour().darker());
                                    g2d.draw(polygon.getPolygonPath());                                        
				}
                            } else {
                                // draw black / layer colour outline for non-selected layers in isolation mode
                                g2d.setStroke(selectStrokeSolid);
				g2d.setColor(layer.getColour().darker());
				g2d.draw(polygon.getPolygonPath());                                        
                                g2d.setStroke(selectStrokeBlack);
                                g2d.setColor(layer.getColour());
                                g2d.draw(polygon.getPolygonPath());
                            }

				// draw resize frame for user selected polygon if any
				if ( polygon == selectedPolygon )
					drawResizeFrame(g2d, polygon.getPolygonPath());
				
				// draw anchors for currently active polygon (mouse over) if any
				if ( polygon == activePolygon ) {
					
					// draw anchors
					g2d.setStroke(selectStrokeBlack);
					g2d.setColor(Color.BLACK);
					g2d.draw(polygon.getPolygonPath());
					if ( (polygon.getType() == HiPolygonTypes.HI_FREEDESIGN) || (polygon.getType() == HiPolygonTypes.HI_FREEHAND) )  // only draw drag anchors for free design polygons
						for (int i=0;i<polygon.size();i++) 
							drawAnchor(g2d, layer.getColour(), polygon.getPoint(i), 6);
					if ( !polygon.isClosed() && polygon.size() > 0 ) {
						// for open polygon paths: highlight the first and last anchor point
							g2d.drawImage(closePathIcon, 
									polygon.getPoint(0).x-13, polygon.getPoint(0).y-3, 26, 17, null);
						if ( polygon.size() > 1 ) 
							drawAnchor(g2d, Color.WHITE, polygon.getPoint(polygon.size()-1), 6);
					}
				}

			}
		}
	}
	
	/**
	 * helper Method: draws a resize for given polygon path
	 * 
	 * @param polygonPath
	 */
	private void drawResizeFrame(Graphics2D g2d, GeneralPath polygonPath) {
		Rectangle bounds = polygonPath.getBounds();
		
		g2d.setStroke(selectStrokeSolid);
		// resize frame
		g2d.setColor(Color.BLACK);
		g2d.drawRect(bounds.x-10, bounds.y-10, bounds.width+20, bounds.height+20);
		g2d.setColor(Color.WHITE);
		g2d.drawRect(bounds.x-11, bounds.y-11, bounds.width+20, bounds.height+20);

		// resize anchors
//		g2d.drawRect(bounds.x-15, bounds.y+bounds.height+4, 10, 10);  // left lower
		g2d.drawArc(bounds.x+bounds.width+4, bounds.y-15, 10, 10, 0, 360); // right upper
//		g2d.drawRect(bounds.x-15, bounds.y-15, 10, 10);  // left upper
		g2d.drawRect(bounds.x+bounds.width+4, bounds.y+bounds.height+4, 10, 10); // right lower
		g2d.setColor(Color.BLACK);
//		g2d.drawRect(bounds.x-16, bounds.y+bounds.height+3, 10, 10); // left lower
		g2d.drawArc(bounds.x+bounds.width+3, bounds.y-16, 10, 10, 0, 360); // right upper
//		g2d.drawRect(bounds.x-16, bounds.y-16, 10, 10); // left upper
		g2d.drawRect(bounds.x+bounds.width+3,bounds.y+bounds.height+3, 10, 10); // right lower
	}


	/**
	 * helper Method: draws an anchor point for given coordinates
	 * @param g2d graphics object
	 * @param color
	 * @param p x and y coordinates
	 * @param size
	 */
	private void drawAnchor(Graphics2D g2d, Color color, Point p, int size)
	{
		// draw rectangle anchor of given size
		g2d.setColor(Color.WHITE);
		g2d.fillRect(p.x-(size/2),p.y-(size/2) , size, size);
		g2d.setColor(color);
		g2d.draw3DRect(p.x-(size/2), p.y-(size/2), size, size, true);
		g2d.draw3DRect(p.x-(size/2)+1, p.y-(size/2)+1, size-2, size-2, true);
	}

	/**
	 * helper method: called by mouse event handlers
	 * sets the GUI mouse cursor image for the current mouse (x,y) position according to the GUI state model:
	 * - if mouse is on anchor point: show move/remove anchor cursor
	 * 		- if polygon is open: show close polygon cursor
	 * - if mouse is on polygon line: show add anchor cursor
	 * - if mouse is inside a polygon: show move polygon cursor
	 * 		- if open polygon exists and mouse is not on an anchor or line: show add anchor cursor
	 * - else show system cursor
	 * @param x mouse x coordinate
	 * @param y mouse y coordinates
	 */
	private void setCursorState(int x, int y) {
		if ( dragAnchor >= 0 ) {
			cursorState = crossRemoveMoveCursor;
			if ( activePolygon!= null )
				if ( ( dragAnchor == 0 ) && !activePolygon.isClosed() )
					cursorState = crossClosePathCursor;
		} else if ( dragLinePoint >= 0 ) {
			cursorState = crossAddCursor;
		} else if ( activePolygon != null ) {
			cursorState = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		} else if ( resizeAnchor != PolygonResizeCorners.NONE ) cursorState = Cursor.getDefaultCursor();
                  else if ( placeFirstAnchorMode ) cursorState = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
                  else cursorState = crossMoveCursor;
		
                if ( activePolygon != null ) if ( !activePolygon.isClosed() && ( dragAnchor < 0 ) )
			cursorState = crossAddCursor;
		// set new cursor state if necessary
		if ( cursorState != this.getCursor() ) this.setCursor(cursorState);
	}


	/**
	 * internal method: sets the logical GUI state of the polygon editor (which polygon is active, etc...)
	 * depending on current mouse position (x, y)
	 * called by mouse event handler
	 * 
	 * @param x mouse x coordinate
	 * @param y mouse y coordinate
	 */
	private void setViewingState(int x, int y, boolean forceUpdate) {
		if ( popupMenu.isVisible() &&  ! forceUpdate ) return; // don´t update state, if user is browsing the context menu
		RelativePolygon oldPolygonState = activePolygon; // preserve previous state

		// reset state
		this.setToolTipText("");
		activePolygon = null;
		dragAnchor = -1;
		resizeAnchor = PolygonResizeCorners.NONE;
		dragLinePoint = -1;
		foundOpenPolygon = false;

		// search all layers for polygons
		// do this in reverse order so layers at the top of the list will be drawn on top of other layers
		for (int count=layers.size()-1; count>=0; count--) {
			HILayer layer = layers.get(count);
                        
                        // only observe polygons of selected layer in isolation mode
                        if ( isolationMode && layer != userSelectedLayer ) continue; 
			
                        for (RelativePolygon polygon : layer.getRelativePolygons())
				if ( !polygon.isClosed() ) {
					activePolygon = polygon; // if open polygon found, make it active, override "inside polygon" state
					selectedPolygon = null; // when an open polygon is detected, don´t give the option to resize
					foundOpenPolygon = true;
				}

			// check if mouse is on resize anchor of selected frame
			if ( selectedPolygon != null ) {
				Rectangle bounds = selectedPolygon.getPolygonPath().getBounds();
				Rectangle upperLeft =  new Rectangle(bounds.x-15, bounds.y-15, 10, 10);
				Rectangle lowerLeft =  new Rectangle(bounds.x-15, bounds.y+bounds.height+4, 10, 10);
				Rectangle upperRight = new Rectangle(bounds.x+bounds.width+4, bounds.y-15, 10, 10);
				Rectangle lowerRight = new Rectangle(bounds.x+bounds.width+4, bounds.y+bounds.height+4, 10, 10);

				if ( upperLeft.contains(x, y) )
					resizeAnchor = PolygonResizeCorners.UPPER_LEFT;
				else if ( upperRight.contains(x, y) )
					resizeAnchor = PolygonResizeCorners.UPPER_RIGHT;
				else if ( lowerLeft.contains(x, y) )
					resizeAnchor = PolygonResizeCorners.LOWER_LEFT;
				else if ( lowerRight.contains(x, y) )
					resizeAnchor = PolygonResizeCorners.LOWER_RIGHT;

			}

			// scan all polygons of current layer
			for (RelativePolygon polygon : layer.getRelativePolygons()) {
				boolean isActive = false;
				if ( polygon.isOnAnchor(x, y) ) { 
					// is mouse on an anchor point of the current polygon?
					if ( (polygon.getType() == HiPolygonTypes.HI_FREEDESIGN) || (polygon.getType() == HiPolygonTypes.HI_FREEHAND) ) dragAnchor = polygon.getDragAnchor(x , y);
					activePolygon = polygon;
					isActive = true;
				} else if ( polygon.isOnLine(x, y) ) {
					// is mouse on an outer line of the current polygon?
                                if ( (polygon.getType() == HiPolygonTypes.HI_FREEDESIGN) || (polygon.getType() == HiPolygonTypes.HI_FREEHAND) ) dragLinePoint = polygon.getLinePoint(x, y);
					activePolygon = polygon;
					this.setCursor(crossAddCursor);
					isActive = true;
				} else if ( polygon.getPolygonPath().contains(x, y) ) {
					// is polygon inside the current polygon?
					activePolygon = polygon;
					isActive = true;
				}
				
				// update tooltip
				if ( isActive ) {
					String title = MetadataHelper.findValue(
							"HIBase", 
							"title", 
							MetadataHelper.getDefaultMetadataRecord(
									layer.getModel(), 
									HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()
							)
						);
					if ( title == null || title.length() == 0 )
						title = MetadataHelper.getDisplayableID(layer.getModel());
					
					String comment = new HIRichText(
							MetadataHelper.findValue(
									"HIBase", "comment", 
									MetadataHelper.getDefaultMetadataRecord(
											layer.getModel(), 
											HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()
									)
							)).getHTMLModel();
					comment = comment.replaceAll("<[/]?html>", "");
					String tooltip = "<html><b>"+"Ebene"+": "+title+"</b><br><br>"+comment+"</html>";
					this.setToolTipText(tooltip);
				}

			}
		}
		if ( oldPolygonState != activePolygon ) repaint(); // repaint polygons if state has changed
		setCursorState(x, y); // update GUI cursor image
	}


	/**
	 * internal method: updates state of user selected polygon
	 * @param polygon - new polygon to be selected or null to remove selection
	 */
	private void setSelectedPolygon(RelativePolygon polygon) {
		RelativePolygon oldState = selectedPolygon;

		if ( polygon != selectedPolygon )
			if ( ! foundOpenPolygon ) selectedPolygon = polygon; // only select closed polygons
		
		if ( oldState != selectedPolygon ) // update viewport if necessary
			repaint();
	}


	/**
	 * helper method: adds a new anchor to the active polygon and updates the GUI state accordingly
	 * @param insertIndex index after which the anchor will be added
	 * @param x x coordinate of new anchor
	 * @param y y coordinate of new anchor
	 */
	private void addAnchorPointToActivePolygon(int insertIndex, int x, int y) {
		// sanity check
		if ( activePolygon == null ) return;

		activePolygon.insertPointAfter(insertIndex, x, y ); // add point
		// set new GUI state
		anchorCreated = true;
		dragAnchor = insertIndex+1;
		// repaint polygons
		setCursorState(x, y);
		repaint();		
	}

	// -------- mouse event handler ----------

	/**
	 * mouse handler: mouse move --> update logical GUI state and repaint polygons if necessary
	 * - display popup menu on windows systems if necessary
	 */
	public void mouseMoved(MouseEvent e) {
		setViewingState(e.getX(), e.getY(), false);
	}

	/**
	 * mouse handler: left mouse button pressed
	 *  - adds/removes anchor to polygon according to state
	 */
	public void mousePressed(MouseEvent e) {
		// save initial coordinates to calculate drag distance
		dragX = e.getX();
		dragY = e.getY();

		if ( e.getButton() == MouseEvent.BUTTON1 && ! e.isPopupTrigger()) {
			if ( (dragAnchor < 0) && activePolygon != null ) {
				// add a new anchor to the active polygon (add after last anchor if polygon path is open)
				if (dragLinePoint >= 0) addAnchorPointToActivePolygon(dragLinePoint, e.getX(), e.getY());
				else if ( !activePolygon.isClosed() ) addAnchorPointToActivePolygon(activePolygon.size()-1, e.getX(), e.getY());
			}
		}
	}

	/**
	 * mouse event: mouse dragged
	 * - move active polygon anchor if any
	 * - move active polygon (translate) if mouse is inside
	 */
	public void mouseDragged(MouseEvent e) {
		int divX, divY;
		// calculate distance of drag
		divX = e.getX()-dragX;
		divY = e.getY()-dragY;
		dragX = e.getX();
		dragY = e.getY();
		
		if ( resizeAnchor != PolygonResizeCorners.NONE ) {
			// resize polygon
			if ( resizeAnchor == PolygonResizeCorners.LOWER_RIGHT ) selectedPolygon.transformBy(divX, divY, resizeAnchor);

			if ( resizeAnchor == PolygonResizeCorners.UPPER_RIGHT ) selectedPolygon.transformByAngle(divX/100.0, resizeAnchor);
                        
                        repaint();
		} else { 
                    setSelectedPolygon(activePolygon); // update user selected polygon
                    if ( dragAnchor >= 0 ) {
                        // move anchor to new coordinates
                        activePolygon.movePointTo(dragAnchor, e.getX(), e.getY());
			repaint();
                    } else if ( activePolygon != null ) {
                        if ( activePolygon.isClosed() ) {
                            activePolygon.translate(divX, divY);
                            repaint();
			} else if (activePolygon.getPolygonPath().contains(e.getX(), e.getY())) {
                            // move polygon to new coordinates
                            activePolygon.translate(divX, divY);
                            repaint();				
			}
                    }
		}

                // freihand polygon
                if ( activePolygon != null && activePolygon.getType() == HiPolygonTypes.HI_FREEHAND ) {
                    if (!activePolygon.isClosed()) {
                        if ( Math.abs(divX)>0 || Math.abs(divY)>0 ) {
                            addAnchorPointToActivePolygon(activePolygon.size()-1, e.getX(), e.getY());
                        }
                    }
                }


	}

	/**
	 * mouse event: mouse clicked --> left mouse button
	 *  - adds/removes anchor to polygon according to state
	 *  - closes open polygon if mouse over last anchor
	 */
	public void mouseClicked(MouseEvent e) {
		if ( ( e.getButton() == MouseEvent.BUTTON1 ) && !popupMenu.isVisible() ) {

                        setSelectedPolygon(activePolygon); // update user selected polygon

                  if ( lastPolygon != null ) {
                    if(lastPolygon.getType() == HiPolygonTypes.HI_FREEDESIGN && lastPolygon.size() == 2) activePolygon = lastPolygon;
                  }

                  if ( activePolygon != null ) {

                       if (activePolygon.getType() == HiPolygonTypes.HI_FREEDESIGN && activePolygon.size()==1 ) {
                            addAnchorPointToActivePolygon(activePolygon.size()-1, e.getX(), e.getY());
                            lastPolygon = activePolygon;

                        }

				if ( (!activePolygon.isClosed()) && (dragAnchor < 0) ) 
					addAnchorPointToActivePolygon(activePolygon.size()-1, e.getX(), e.getY());
				else if ( dragAnchor >= 0 ) {
					if ( (!activePolygon.isClosed()) && (dragAnchor == 0) ) {
						// close open polygon and update GUI state
						activePolygon.setClosed(true);
						dragAnchor = -1;
						setCursorState(e.getX(),e.getY());
                                                // DEBUG
                                                view.polygonEditorViewCommand = "";
                                                placeFirstAnchorMode = false;
                                                view.updateToolbarButtons();
						this.repaint();
					} else if ( ( anchorCreated == false ) && ( activePolygon.size() > 2 ) ) {
						// remove active anchor from polygon and update GUI state
						activePolygon.removePoint(dragAnchor);
						dragAnchor = -1;
						setCursorState(e.getX(),e.getY());
						this.repaint();
					}
				}
			}
			anchorCreated = false;
		}
	}

	public void mouseReleased(MouseEvent e) {
            if ( activePolygon != null && activePolygon.getType() == HiPolygonTypes.HI_FREEHAND ) {
                if (!activePolygon.isClosed()) {
                    activePolygon.setPoints(simplifyPath(simplifyRadialDist(activePolygon.getPoints(), 6.0), 6.0));
                    this.repaint();
                }
            }
		// not needed
	}

	public void mouseEntered(MouseEvent e) {
		highlightLayer = false;
		setViewingState(e.getX(), e.getY(), false);
		this.repaint();
	}
	
	public void mouseExited(MouseEvent e) {
		highlightLayer = true;
		repaintShapes();
	}

	// -------- popup menu event handler ----------


	public void popupMenuCanceled(PopupMenuEvent e) {
		// not needed
	}


	/**
	 * event handler: resumes updates of GUI information at new mouse location, after popup menu was exited
	 */
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		if ( this.getMousePosition() != null ) 
			setViewingState(this.getMousePosition().x, this.getMousePosition().y, true);
	}


	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// not needed
	}


}
