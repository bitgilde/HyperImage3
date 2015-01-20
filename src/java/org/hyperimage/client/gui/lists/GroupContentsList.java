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

package org.hyperimage.client.gui.lists;

import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

/**
 * @author Jens-Martin Loebel
 */
public class GroupContentsList extends JList {
	
	private static final long serialVersionUID = -1403753593827429188L;

	
	public static enum HI_ListDisplayStyles {ICON_STYLE, LIST_STYLE };	

	private DefaultListModel groupListModel;
	private GroupContentsCellRenderer cellRenderer;

	public HI_ListDisplayStyles style;
	
	
	public GroupContentsList() {
		super();
		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		this.setVisibleRowCount(-1);
		this.setBackground(Color.lightGray);

		this.groupListModel = new DefaultListModel();
		this.setModel(groupListModel);

		this.cellRenderer = new GroupContentsCellRenderer();

		// set icon style as default
		style = HI_ListDisplayStyles.ICON_STYLE;
		this.setCellRenderer(cellRenderer);
	}
	
	
	public HI_ListDisplayStyles getDisplayStyle() {
		return this.style;
	}
	
	public void setDisplayStyle(HI_ListDisplayStyles style) {
		if ( style != this.style ) {
			this.style = style;
			if ( style == HI_ListDisplayStyles.ICON_STYLE ) {
				this.setLayoutOrientation(JList.HORIZONTAL_WRAP);
				cellRenderer.setDisplayStyle(style);
			} else {
				this.setLayoutOrientation(JList.VERTICAL);
				cellRenderer.setDisplayStyle(style);
			}
		}
	}
	
	
}
