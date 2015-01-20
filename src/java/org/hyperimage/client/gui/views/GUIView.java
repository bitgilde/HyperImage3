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

package org.hyperimage.client.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

/**
 * @author Jens-Martin Loebel
 */
public abstract class GUIView extends JPanel {
	
	private static final long serialVersionUID = -8914666534420252209L;

	
	protected JMenuBar menuBar;
	protected JMenu menuTitle;
	
	public GUIView(String title) {
		initView(title);
	}
	
	public GUIView(String title, Color menuColor) {
		initView(title);
		menuBar.setBackground(menuColor);
		menuTitle.setBackground(menuColor);
	}
	
	public GUIView() {
		this("Component");
	}

	public GUIView(Color menuColor) {
		this("Component", menuColor);
	}

	
	public void updateContent() {
		// do nothing
	}
	
	protected void initView(String title) {
		menuBar = new JMenuBar();
		menuTitle = new JMenu(title);
		menuBar.add(menuTitle);
		menuBar.setBackground(new Color (0xAA, 0xAA, 0xAA));
		menuTitle.setBackground(menuBar.getBackground());
		menuTitle.setForeground(Color.WHITE);
		this.setLayout(new BorderLayout());
		this.setSize(400, 400);
		this.setPreferredSize(new Dimension(400,400));
		this.add(menuBar, BorderLayout.NORTH);
	}
	
	public JMenu getMenu() {
		return menuTitle;
	}
	
	public String getTitle() {
		return menuTitle.getText();
	}
	
	public void setTitle(String title) {
		menuTitle.setText(title);
	}
	
	public void updateLanguage() {
		// implemented in subclasses
	}
	
	
	
	protected void setDisplayPanel(JPanel content) {
		this.add(content, BorderLayout.CENTER);
		this.setSize(content.getSize());
		this.setPreferredSize(content.getPreferredSize());
	}

}
