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

package org.hyperimage.client.components;

import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.TooManyListenersException;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.hyperimage.client.HIRuntime;

/**
 * @author Jens-Martin Loebel
 */
public class HIComponentFrame extends JInternalFrame implements DropTargetListener {

	private static final long serialVersionUID = 1L;
	
	private JSplitPane split2 = null;

	
	private HIComponent component;


	public HIComponentFrame(HIComponent component) {
		this.component = component;

		initFrame();

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	public HIComponent getHIComponent() {
		return this.component;
	}

	public void updateTitle() {
		this.setTitle(component.getTitle());
	}
	
	private void initFrame() {
		// make sure drag enabled components don´t "bleed through" to other windows
		this.setDropTarget(new DropTarget());
		try {
			this.getDropTarget().addDropTargetListener(this);
		} catch (TooManyListenersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if ( component.countViews() == 1 ) {
			// single view in component
			this.getContentPane().add(new JScrollPane(component.getViews().get(0)));			

		} else { 
			// SplitPane for 2 and 3 column layout
			JSplitPane split = new JSplitPane();
			split.setContinuousLayout(true);
			this.getContentPane().add(split);
			split.setLeftComponent(new JScrollPane(component.getViews().get(0)));
			split.setDividerLocation((int)component.getViews().get(0).getPreferredSize().getWidth()+10);
			split.setBorder(null);
			if ( component.countViews() == 2 ) {
				// 2 column layout
				split.setRightComponent(new JScrollPane(component.getViews().get(1)));

			} else if ( component.countViews() == 3 ) {
				// 3 column layout
				split2 = new JSplitPane();
				split2.setBorder(null);
				split2.setContinuousLayout(true);
				split2.setOneTouchExpandable(true);
				split.setRightComponent(split2);

				split2.setLeftComponent(new JScrollPane(component.getViews().get(1)));
				split2.setDividerLocation((int)component.getViews().get(1).getPreferredSize().getWidth()+10);
				split2.setResizeWeight(1d);
				split2.setRightComponent(new JScrollPane(component.getViews().get(2)));
				
			} else {
				System.out.println("GUI ERROR: Components with "+component.countViews()+" views are not supported!");
			}		
		}

		// set view properties
		this.getContentPane().setFocusable(true);
		component.getViews().get(0).setFocusable(true);
		component.getViews().get(0).setFocusCycleRoot(true);

		// set frame properties
		this.setTitle(component.getTitle());
		this.setResizable(true);
		this.setClosable(true);
		this.setIconifiable(true);
		this.setMaximizable(true);
		int width = 0;
		int height = 0;
		for ( int i=0; i < component.countViews(); i++ )
			width = (int) (width + component.getViews().get(i).getPreferredSize().getWidth());
		for ( int i=0; i < component.countViews(); i++ )
			height = Math.max(height, (int)component.getViews().get(i).getPreferredSize().getHeight());
		this.pack();
		this.setPreferredSize(new Dimension(width+90,height+90));
		this.setSize(this.getPreferredSize());
	}

	// DEBUG: refactor
	public void setMetadataVisible(boolean visible) {
		if ( split2 != null )
			if ( visible ) {
				split2.setDividerLocation(-1);
			} else {
				split2.setDividerLocation(1.0d);
			}
	}
	
	public boolean isMetadataVisible() {
		if ( split2 == null ) return false;
		return ( split2.getDividerLocation() >= split2.getMaximumDividerLocation() ) ? false : true;
	}
	
	public boolean hasMetadataView() {
		return ( split2 == null ) ? false : true;
	}

	
	
	// ------------------------------------------------------------------------------------------------------
	
	
	
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
		if ( !this.isSelected() )
			HIRuntime.getGui().focusComponent(this);
	}

	public void drop(DropTargetDropEvent dtde) {
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}
}
