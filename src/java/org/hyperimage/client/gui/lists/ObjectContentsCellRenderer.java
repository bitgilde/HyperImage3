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
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;

import org.hyperimage.client.gui.ObjectContentCell;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiObject;
import org.hyperimage.client.ws.HiObjectContent;

/**
 * @author Jens-Martin Loebel
 */
public class ObjectContentsCellRenderer extends JPanel implements
		ListCellRenderer {

	private static final long serialVersionUID = -7813916969496340115L;

	
	private HashMap<HiObjectContent, ObjectContentCell> cellCache;
	private String defLang;
	private HiObject object = null;
	
	private static LineBorder standardBorder;
	private static LineBorder defContentBorder = new LineBorder(Color.darkGray, 2);


	public ObjectContentsCellRenderer(HiObject object, String defLang) {
		this.cellCache = new HashMap<HiObjectContent, ObjectContentCell>();
		this.defLang = defLang;
		this.object = object;
		
		this.setLayout(new FlowLayout());
		this.setBackground(Color.LIGHT_GRAY);
	}
	
	public ObjectContentCell getCellForContent(HiObjectContent content) {
		ObjectContentCell cell = cellCache.get(content);
		
		// create cell
		if ( cell == null ) {
			cell = new ObjectContentCell( content, defLang );
			cellCache.put(content, cell);		
		}
		
		return cell;
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		// caching strategy
		ObjectContentCell cell = cellCache.get((HiObjectContent)list.getModel().getElementAt(index));
		if ( cell == null ) {
			cell = new ObjectContentCell( (HiObjectContent)list.getModel().getElementAt(index), defLang );
			cellCache.put((HiObjectContent)list.getModel().getElementAt(index), cell);				
		}			
		
		cell.setSelected(isSelected);
		cell.setOpaque(true);
		this.setOpaque(true);
		
		// highlight default view
		if ( standardBorder == null ) standardBorder = new LineBorder(list.getBackground(), 2);
		LineBorder border = standardBorder;
		
		if ( object.getDefaultView() == null && index == 0 )
			border = defContentBorder;
		
		if ( object.getDefaultView() != null && object.getDefaultView().getId() == ((HiObjectContent)value).getId() )
			border = defContentBorder;
		
		this.setBorder(border);
		
		this.removeAll();
		this.add(cell);
		
		return this;
	}

	public void refresh(HiInscription inscription) {
		cellCache.remove(inscription);
		ObjectContentCell cell = new ObjectContentCell( (HiObjectContent)inscription, defLang );
			cellCache.put((HiObjectContent)inscription, cell);				
	}

}
