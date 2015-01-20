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

package org.hyperimage.service.model.render;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

/**
 * Class: RelativePolygon
 * Package: org.hyperimage.service.model.render
 * @author Jens-Martin Loebel
 *
 * Provides additional functionality. Syncs changes back to model.
 */
public class RelativePolygon {
	public static enum HiPolygonTypes {HI_FREEDESIGN,HI_RECTANGLE, HI_USERDESIGN};
	
	public static enum PolygonResizeCorners {NONE, UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT};

	private int scaleX=0, scaleY=0;
	private Vector<Double> pointsX = new Vector<Double>();
	private Vector<Double> pointsY = new Vector<Double>();
	private Vector<Line2D.Double> lines = new Vector<Line2D.Double>();
	private HiPolygonTypes type = HiPolygonTypes.HI_FREEDESIGN;
	private String model;
	private GeneralPath polygonPath;
	private boolean closed = true;
	private boolean hasChanges = false;

	/**
	 * Construct new relative polygon from given scales.
	 * @param model Model implementation of polygon class
	 * @param scaleX scale for x-coordinates (e.g. width of image)
	 * @param scaleY scale for y-coordinates (e.g. height of image)
	 */
	public RelativePolygon(String model, int scaleX, int scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.model = model;
		this.polygonPath = new GeneralPath();
		getPointsFromModel();
		createDisplayablePath();
	}
	
	/**
	 * Constructs a new relative polygon of given type with given scale
	 * @param type the polygon type
	 * @param x x coordinate of first point
	 * @param y y coordinate of first point
	 * @param width width of rectangle (only needed for rectangle type polygons)
	 * @param height height of rectangle (only needed for rectangle type polygons)
	 * @param modelDesign - model of library design (only needed for user design type polygons)
	 * @param scaleX scale for x-coordinates (e.g. width of image)
	 * @param scaleY scale for y-coordinates (e.g. height of image)
	 */
	public RelativePolygon(HiPolygonTypes type, int x, int y, int width, int height, String modelDesign, int scaleX, int scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.type = type;
		this.polygonPath = new GeneralPath();
		
		if ( type == HiPolygonTypes.HI_FREEDESIGN ) {
			addPoint(x, y);
			commitChangesToModel();
		} else if ( type == HiPolygonTypes.HI_RECTANGLE ) {
			addPoint(x,y);
			addPoint(x+width,y);
			addPoint(x+width,y+height);
			addPoint(x,y+height);
			commitChangesToModel();
		} else
			this.model = modelDesign;
			
		getPointsFromModel();
		this.type = type;
		createDisplayablePath();
	}

	public String getChangedModel() {
		String points = getModelType()+";";

		for (int i=0; i < pointsX.size(); i++) {
			points = points + pointsX.get(i)+"#"+pointsY.get(i);
			if ( i < (pointsX.size()-1) ) points = points + ";";
		}
		if (pointsX.size() == 0) points = getModelType()+";0.0#0.0";
		
		return points;
	}
	
	public void commitChangesToModel() {
		model = getChangedModel();
	}
	
	public void discardChanges() {
		getPointsFromModel();
		createDisplayablePath();
	}


	public boolean hasChanges() {
		return hasChanges;
	}

	/**
	 * Appends the specified relative coordinates to this Polygon.
	 * @param x the specified relative x coordinate
	 * @param y the specified relative y coordinate
	 */
	public void addPoint(double x, double y) {
		pointsX.add(x);
		pointsY.add(y);

		hasChanges = true;
		createDisplayablePath();
	}

	/**
	 * overwrites method of Polygon superclass to store relative points in extra Vector
	 */
	public void addPoint(int x, int y) {
		pointsX.add(new Double( (double) x/scaleX));
		pointsY.add(new Double( (double) y/scaleY));

		hasChanges = true;
		createDisplayablePath();
	}
	public void insertPointAfter(int index, int x, int y) {
		int insertIndex = Math.max(index, 0);
		insertIndex = Math.min(insertIndex, size()-1);
		pointsX.insertElementAt((double)x/(double)scaleX, insertIndex+1);
		pointsY.insertElementAt((double)y/(double)scaleY, insertIndex+1);

		hasChanges = true;
		createDisplayablePath();
	}

	public void setScale(int scaleX, int scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;

		createDisplayablePath();
	}


	public String toString() {
		String polygonString = "";
	
		if ( pointsX.size() == 0 ) return "0.0#0.0";
		for (int i=0; i<pointsX.size(); i++)
			polygonString = polygonString + pointsX.get(i).toString()+"#"+pointsY.get(i).toString()+";";
		return polygonString.substring(0, polygonString.length()-1);
	}

	public void translate(int deltaX, int deltaY) {
		double dx,dy;
		// update internal Vector tables
		dx = (double) deltaX/scaleX;
		dy = (double) deltaY/scaleY;
		for (int i=0; i<pointsX.size(); i++)
		{
			pointsX.set(i, (double) pointsX.get(i)+dx);
			pointsY.set(i, (double) pointsY.get(i)+dy);

			/* disabled for 1.1 as per user request
			// respect precentage bounds
			if ( pointsX.get(i) > 1d ) pointsX.set(i, 1d);
			if ( pointsX.get(i) < 0d ) pointsX.set(i, 0d);
			if ( pointsY.get(i) > 1d ) pointsY.set(i, 1d);
			if ( pointsY.get(i) < 0d ) pointsY.set(i, 0d);
			 */

			hasChanges = true;
			createDisplayablePath();
		}
	}

	/**
	 * Translates the vertices of the Polygon by deltaX along the x axis and by deltaY along the y axis.
	 * @param deltaX the relative amount to translate along the x axis
	 * @param deltaY the relative amount to translate along the y axis
	 */
	public void translate(double deltaX, double deltaY)
	{
		// update relative point Vectors
		for ( int i=0; i<pointsX.size(); i++) {
			pointsX.set(i, (double) pointsX.get(i)+deltaX);
			pointsY.set(i, (double) pointsY.get(i)+deltaY);
		}
		translate(new Long(Math.round(deltaX*scaleX)).intValue(),new Long(Math.round(deltaY*scaleY)).intValue());
		hasChanges = true;
	}

	public void movePoint(int index, int deltaX, int deltaY) {
		double dx = (double) deltaX/scaleX;
		double dy = (double) deltaY/scaleY;

		if ( ( index >= 0 ) && ( index < pointsX.size() ) )
		{
			pointsX.set(index, (double) pointsX.get(index)+dx);
			// respect precentage bounds
			if ( pointsX.get(index) > 1d ) pointsX.set(index, 1d);
			if ( pointsX.get(index) < 0d ) pointsX.set(index, 0d);

			pointsY.set(index, (double) pointsY.get(index)+dy);
			// respect precentage bounds
			if ( pointsY.get(index) > 1d ) pointsY.set(index, 1d);
			if ( pointsY.get(index) < 0d ) pointsY.set(index, 0d);

			hasChanges = true;
			createDisplayablePath();
		}

	}


	public void movePointTo(int index, int x, int y) {
		double dx = (double) x/scaleX;
		double dy = (double) y/scaleY;

		if ( ( index >= 0 ) && ( index < pointsX.size() ) )
		{
			pointsX.set(index, dx);
			// respect precentage bounds
			if ( pointsX.get(index) > 1d ) pointsX.set(index, 1d);
			if ( pointsX.get(index) < 0d ) pointsX.set(index, 0d);

			pointsY.set(index, dy);
			// respect precentage bounds
			if ( pointsY.get(index) > 1d ) pointsY.set(index, 1d);
			if ( pointsY.get(index) < 0d ) pointsY.set(index, 0d);

			hasChanges = true;
			createDisplayablePath();
		}

	}

	// DEBUG - work in progress
	/**
	 * 
	 * @param relX
	 * @param relY
	 * @param corner
	 */
	public void transformBy(double relX, double relY, PolygonResizeCorners corner) {
		// disable other corners for now -- TODO implement resizing on all 4 corners
		if ( corner != PolygonResizeCorners.LOWER_RIGHT ) return;

		GeneralPath transformPath = new GeneralPath();
		if ( pointsX.size() > 0 ) {
			transformPath.moveTo(pointsX.get(0).floatValue(), pointsY.get(0).floatValue());
			for ( int i=1; i < pointsX.size(); i++)
				transformPath.lineTo(pointsX.get(i).floatValue(), pointsY.get(i).floatValue());			
		}
		double oldX = transformPath.getBounds2D().getX();
		double oldY = transformPath.getBounds2D().getY();

		transformPath.transform(AffineTransform.getScaleInstance(relX, relY));


		PathIterator segments;
		double coords[] = new double[6];
		double transX = 0;
		double transY = 0;

		// lower right corner
		if ( corner == PolygonResizeCorners.LOWER_RIGHT ) {
			// translate back to old point of origin
			transX = oldX-transformPath.getBounds2D().getX();
			transY = oldY-transformPath.getBounds2D().getY();
		}

		segments = transformPath.getPathIterator(AffineTransform.getTranslateInstance(transX, transY));
		for (int i=0 ; i<pointsX.size(); i++) {
			segments.currentSegment(coords);
			pointsX.set(i, coords[0]);
			pointsY.set(i, coords[1]);
			segments.next();				
		}
		createDisplayablePath();
	}
	
	// DEBUG - work in progress
	/**
	 * 
	 * @param divX
	 * @param divY
	 * @param corner
	 */
	public void transformBy(int divX, int divY, PolygonResizeCorners corner) {
		// disable other corners for now -- TODO implement resizing on all 4 corners
		if ( corner != PolygonResizeCorners.LOWER_RIGHT ) return;
		
		// don�´t scale lower than 10x10 pixels
		if ( (polygonPath.getBounds().width+divX) < 10 || (polygonPath.getBounds().height+divY) < 10 )
			return;

		double relX = (double)(polygonPath.getBounds().width+divX) / (double)polygonPath.getBounds().width;
		double relY = (double)(polygonPath.getBounds().height+divY) / (double)polygonPath.getBounds().height;

		transformBy(relX, relY, corner);
	}

	// DEBUG - work in progress
	/**
	 * Scales and translates polygon to fit inside a defined rectangle size
	 * @param width
	 * @param height
	 */
	public void clipTo(int width, int height) {
		// translate polygon to 0,0 coordinate origin
		translate(-getPolygonPath().getBounds().x, -getPolygonPath().getBounds().y);
				
		// transform polygon to fit requested size
		GeneralPath transformPath = new GeneralPath();
		if ( pointsX.size() > 0 ) {
			transformPath.moveTo(pointsX.get(0).floatValue(), pointsY.get(0).floatValue());
			for ( int i=1; i < pointsX.size(); i++)
				transformPath.lineTo(pointsX.get(i).floatValue(), pointsY.get(i).floatValue());
		}
		double scale = Math.max(transformPath.getBounds2D().getWidth(), transformPath.getBounds2D().getHeight());
		if ( scale > 0 ) transformBy(1.0d/scale, 1.0d/scale, PolygonResizeCorners.LOWER_RIGHT);
		
		// set display scale
		setScale(width, height);
	}

	public void removePoint(int index)
	{
		if ( pointsX.size() > 1 ) {
			pointsX.remove(index);
			pointsY.remove(index);
			if ( pointsX.size() == 1 ) this.closed=false;
			hasChanges = true;
			createDisplayablePath();
		}
	}

	/**
	 * Returns the number of anchor points in this polygon
	 * @return the number of anchor points in this polygon
	 */
	public int size() {
		return pointsX.size();
	}

	public Point2D.Double getRelativePoint(int index) {
		return new Point2D.Double(pointsX.get(index), pointsY.get(index));
	}

	public Point getPoint(int index) {
		int insertIndex = index;
		if ( ( insertIndex < 0 ) || ( insertIndex >= this.size() )  ) insertIndex = 0;
		return new Point((int) (pointsX.get(insertIndex)*scaleX), (int) (pointsY.get(insertIndex)*scaleY));
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		if ( this.closed != closed ) {
			this.closed = closed;
			createDisplayablePath();
		}
	}

	public String getModel() {
		return this.model;
	}

	public GeneralPath getPolygonPath() {
		return this.polygonPath;
	}

	public boolean isOnLine(int x, int y) {
		if ( getLinePoint(x, y) >= 0 ) return true;
		return false;
	}

	public int getLinePoint(int x, int y) {
		Rectangle2D.Double rect = new Rectangle2D.Double(x-2,y-2,4,4);
		int count = 0;

		for ( Line2D.Double line : lines ) {
			if ( line.intersects(rect) )
				return count;
			count = count + 1;
		}		
		return -1;
	}


	public boolean isOnAnchor(int x, int y) {
		if ( getDragAnchor(x, y) >= 0 ) return true;
		return false;
	}

	public int getDragAnchor(int x, int y) {
		PathIterator segments;
		Rectangle2D.Double rect = new Rectangle2D.Double(x-3,y-3,6,6);
		double coords[] = new double[6];
		int count = 0;

		segments = polygonPath.getPathIterator(null);

		while ( !segments.isDone() ) {
			switch ( segments.currentSegment(coords) ) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				if ( rect.contains(coords[0], coords[1]) )
					return count;
				segments.next();
				count = count + 1;
				break;
			default:
				segments.next();
			break;
			}
		}		
		return -1;
	}

	private void createDisplayablePath() {
		polygonPath.reset();
		lines.removeAllElements();
		polygonPath.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		for (int i=0; i < pointsX.size(); i++) {
			if ( i==0 ) polygonPath.moveTo((int) (pointsX.get(i)*scaleX), (int) (pointsY.get(i)*scaleY));
			else { 
				polygonPath.lineTo((int) (pointsX.get(i)*scaleX), (int) (pointsY.get(i)*scaleY));
				lines.addElement(new Line2D.Double((pointsX.get(i-1)*scaleX),(pointsY.get(i-1)*scaleY),(pointsX.get(i)*scaleX),(pointsY.get(i)*scaleY)));
			}
		}
		if ( closed ) {
			polygonPath.closePath();
			if ( size() > 1 ) 
				lines.addElement(new Line2D.Double((pointsX.get(size()-1)*scaleX),(pointsY.get(size()-1)*scaleY),(pointsX.get(0)*scaleX),(pointsY.get(0)*scaleY)));
		}
	}

	public HiPolygonTypes getType() {
		return this.type;
	}
	
	private String getModelType() {
		if ( type == HiPolygonTypes.HI_RECTANGLE )
			return "R";
		else if ( type == HiPolygonTypes.HI_USERDESIGN )
			return "U";
		else
			return "F";
	}

	public static String getModelType(HiPolygonTypes type) {
		if ( type == HiPolygonTypes.HI_RECTANGLE )
			return "R";
		else if ( type == HiPolygonTypes.HI_USERDESIGN )
			return "U";
		else
			return "F";
	}

	private HiPolygonTypes getTypeFromModel(String modelType) {
		HiPolygonTypes type;

		if ( modelType == null ) type = HiPolygonTypes.HI_FREEDESIGN;
		else if ( modelType.length() == 0 ) type = HiPolygonTypes.HI_FREEDESIGN;
		else if ( modelType.startsWith("U") )
			type = HiPolygonTypes.HI_USERDESIGN;
		else if ( modelType.startsWith("R") )
			type = HiPolygonTypes.HI_RECTANGLE;
		else
			type =HiPolygonTypes.HI_FREEDESIGN;

		return type;
	}

	private void getPointsFromModel() {
		String[] point;

		pointsX.removeAllElements();
		pointsY.removeAllElements();

		if ( model == null ) model = getModelType()+";";
		if ( model.compareTo("") == 0 ) model = getModelType()+";0.0#0.0";

		// extract type
		this.type = getTypeFromModel(model.split(";",2)[0]);
		// extract vertices
		String vertices;
		if ( model.split(";",2).length > 1 )
			vertices = model.split(";",2)[1];
		else
			vertices = "0.0#0.0";

		for (String coords : vertices.split(";") ) {
			point = coords.split("#");
			addPoint(Double.parseDouble(point[0]), Double.parseDouble(point[1]));
		}
		hasChanges = false;
	}

	/**
	 * Returns the current horizontal scale for this polygon
	 * @return X scale of polygon
	 */
	public int getScaleX() {
		return scaleX;
	}

	/**
	 * Returns the current vertical scale for this polygon
	 * @return Y scale of polygon
	 */
	public int getScaleY() {
		return scaleY;
	}

	/**
	 * Converts this polygon to a free form, is possible
	 */
	public void convertToFreeDesign() {
		this.type = HiPolygonTypes.HI_FREEDESIGN;
	}
	
	
	

}
