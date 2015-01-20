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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JTextField;

/**
 * @author Jens-Martin Loebel
 */
public class JEnrichedTextField extends JTextField {

	private static final long serialVersionUID = 5612588545633088154L;

	
	private String emptyText = "";


	public void setEmptyText(String emptyText) {
		this.emptyText = emptyText;
		this.repaint();
	}
	
	public String getEmptyTitle() {
		return this.emptyText;
	}

	
	public void paint(Graphics g) {
		super.paint(g);
		
		if ( this.getText().length() == 0 && !this.isFocusOwner() ) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setColor(Color.darkGray);
			FontMetrics metrics = g.getFontMetrics();
			g2d.drawString(emptyText, 7, metrics.getStringBounds(emptyText, g).getBounds().height+3);
		}
	}
}
