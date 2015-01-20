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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.dnd.DropTarget;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JProgressBar;

/**
 * @author Jens-Martin Loebel
 */
// DEBUG - whole class unstable
public class ProgressInfoGlassPane extends JComponent {

	private static final long serialVersionUID = -4790025078584664916L;

	
	private JProgressBar progressBar;
	private String message = "";
	BufferedImage progressBarImage = null;
	BufferedImage panelImage = null;
	private int paintCount = 0;
	
	
	public ProgressInfoGlassPane() {
		// make sure drag enabled components don´t "bleed through" to other windows
		this.setDropTarget(new DropTarget());

		this.setFont(new Font("Default", Font.BOLD, 16));
		this.setLayout(null);
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setIndeterminate(true);
		this.add(progressBar);
		this.setOpaque(true);
	}
		
	public void setProgress(int progress) {
		if ( progress < 0 ) 
			progressBar.setIndeterminate(true);
		else {
			progressBar.setIndeterminate(false);
			progressBar.setValue(progress);
		}
	}
	
	public void setMessage(String message) {
		this.message = message;
		this.repaint();
	}
	
	public void setVisible(boolean visible) {
		if ( visible ) {
			progressBar.setIndeterminate(true);
			progressBar.setValue(0);
			paintCount = 0;
		}
		super.setVisible(visible);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		progressBar.setBounds( 
				(this.getWidth()/2)-175, 
				(this.getHeight()/2)-(progressBar.getPreferredSize().height/2),
				350,
				progressBar.getPreferredSize().height
		);
		Graphics2D g2d  = (Graphics2D)g;

		AlphaComposite newComposite;
		paintCount = paintCount + 1;
		if ( paintCount < 2 )
			newComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
		else
			newComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
		Composite orgComposite = g2d.getComposite();
		g2d.setComposite(newComposite);
		g2d.setColor(Color.white);
		g2d.fillRect(0,0, this.getWidth(), this.getHeight());
		g2d.setComposite(orgComposite);
		
		
		if (progressBarImage == null || progressBarImage.getWidth() != progressBar.getWidth() || progressBarImage.getHeight() != progressBar.getHeight())
			progressBarImage = getGraphicsConfiguration().createCompatibleImage(progressBar.getWidth(), progressBar.getHeight());

        Graphics gBar = progressBarImage.getGraphics();

        gBar.setClip(progressBar.getGraphics().getClip());
        gBar.setColor(Color.white);
        progressBar.paint(gBar);

		int x =  this.getWidth() / 2;
		int y = this.getHeight() / 2;

        Paint gradient = new GradientPaint(
        		x-5, 
        		y-5, 
        		Color.lightGray, 
        		x-5, 
        		y-5 + progressBar.getBounds().height+10, Color.darkGray);
        g2d.setPaint(gradient);
        g2d.fillRect(x-(progressBarImage.getWidth()/2)-5, y-(progressBarImage.getHeight()/2)-5, progressBarImage.getWidth()+10, progressBarImage.getHeight()+10);

        g2d.drawImage(progressBarImage, x-(progressBarImage.getWidth()/2), y-(progressBarImage.getHeight()/2), null);


        // draw info string
        g2d.setColor(Color.black);
        g2d.drawString(
        		this.message, 
        		x-(progressBarImage.getWidth()/2), 
        		y-g.getFontMetrics().getDescent()-20
        );
        
 	}

}
