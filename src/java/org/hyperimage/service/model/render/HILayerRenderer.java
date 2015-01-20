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

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Vector;

import org.hyperimage.service.model.HILayer;

/**
 * @author Jens-Martin Loebel
 */
public class HILayerRenderer {

	// static constants, used to draw layer outline
	private static final float polygonDash[] = {4.0f,4.0f};
	public static final BasicStroke selectStrokeWhite = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, polygonDash, 0.0f);
	public static final BasicStroke selectStrokeBlack = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, polygonDash, 4.0f);
	
	private Vector<RelativePolygon> polygons = new Vector<RelativePolygon>();
	private Color layerColour;
	private HILayer model;

	
	public HILayerRenderer(HILayer model, int scaleX, int scaleY) {
		this.model = model;
		this.layerColour = new Color(model.getRed(), 
				model.getGreen(),
				model.getBlue(),
				(int)(model.getOpacity()*255));

		// split model polygon string into separate polygons
		if ( model.getPolygons().length() > 0 ) {
			for (String polygon : model.getPolygons().split("\\|"))
				polygons.addElement(new RelativePolygon(polygon, scaleX, scaleY));
		}
	}

	public void setScale(int scaleX, int scaleY) {
		for (RelativePolygon polygon : polygons)
			polygon.setScale(scaleX, scaleY);
	}

	public Color getColour() {
		return this.layerColour;
	}
	
	public Color getSolidColour() {
		return new Color (layerColour.getRed(), layerColour.getGreen(), layerColour.getBlue());
	}
	
	public void setColour(Color layerColour) {
		this.layerColour = new Color (layerColour.getRed(), layerColour.getGreen(), layerColour.getBlue(), this.layerColour.getAlpha());
	}

	public void setOpacity(int opacity) {
		this.layerColour = new Color(this.layerColour.getRed(), this.layerColour.getGreen(), this.layerColour.getBlue(), opacity);
	}
	
	public int getOpacity() {
		return this.layerColour.getAlpha();
	}

	public void setOpacity(float relOpacity) {
		int opacity = (int)relOpacity*255;
		this.layerColour = new Color(this.layerColour.getRed(), this.layerColour.getGreen(), this.layerColour.getBlue(), opacity);
	}

	public Vector<RelativePolygon> getRelativePolygons() {
		return this.polygons;
	}
	
	public HILayer getModel() {
		return this.model;
	}
}
