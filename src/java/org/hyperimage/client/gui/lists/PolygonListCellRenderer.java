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

package org.hyperimage.client.gui.lists;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import org.hyperimage.client.gui.PolygonEditorControl;
import org.hyperimage.client.model.RelativePolygon;


/**
 * @author Jens-Martin Loebel
 */

public class PolygonListCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = -820157524560699784L;

	
	private HashMap<String, Icon> polygonCache;
	private JLabel polygonLabel;
	private static Border regBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);
	private static Border selBorder;
	
	public PolygonListCellRenderer(JList list) {
		PolygonListCellRenderer.selBorder = BorderFactory.createLineBorder(list.getSelectionBackground(), 3);
		this.setSize(55, 55);
		this.setPreferredSize(new Dimension(62, 62));
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.polygonLabel = new JLabel();
		this.polygonLabel.setSize(50, 50);
		this.polygonLabel.setPreferredSize(new Dimension(55,55));
		this.polygonLabel.setOpaque(true);
		this.setBackground(Color.white);
		
		this.add(polygonLabel, BorderLayout.CENTER);
		
		polygonCache = new HashMap<String, Icon>();
		
	}
	
	public static Icon renderPolygonIcon(String polygonModel, Color color) {
		BufferedImage colorImage;
		Graphics2D g2d;
		int previewIconSize = 48;
		
		RelativePolygon tempPolygon = new RelativePolygon(polygonModel, previewIconSize, previewIconSize);
		tempPolygon.clipTo(previewIconSize, previewIconSize);
		
		colorImage = new BufferedImage(previewIconSize, previewIconSize, BufferedImage.TYPE_INT_RGB);
		g2d = colorImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.white);
		g2d.fill3DRect(0, 0, previewIconSize, previewIconSize, true);
		
		
		g2d.setColor(color);
		g2d.fill(tempPolygon.getPolygonPath());
		g2d.setStroke(PolygonEditorControl.selectStrokeSolid);
		g2d.setColor(color.darker());
		g2d.draw(tempPolygon.getPolygonPath());

		return new ImageIcon(colorImage);
	}
	
	
	public void clearCache() {
		this.polygonCache.clear();
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		// caching strategy
		Icon icon = polygonCache.get((String)value);		
		if ( icon == null ) {
			icon = renderPolygonIcon((String)value, Color.gray);
			polygonCache.put((String)value, icon);
		}
		
		polygonLabel.setIcon(icon);

		if ( isSelected ) polygonLabel.setBorder(selBorder);
		else polygonLabel.setBorder(regBorder);
		
		
		return this;
	}

}
