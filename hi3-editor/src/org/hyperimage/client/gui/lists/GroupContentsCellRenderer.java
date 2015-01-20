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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.hyperimage.client.gui.lists.GroupContentsList.HI_ListDisplayStyles;
import org.hyperimage.client.ws.HiQuickInfo;

/**
 * @author Jens-Martin Loebel
 */
public class GroupContentsCellRenderer extends JPanel implements
		ListCellRenderer {
	
	private static final long serialVersionUID = -1965469051301621847L;

	
	private GroupContentsList.HI_ListDisplayStyles style = GroupContentsList.HI_ListDisplayStyles.ICON_STYLE;
	private HashMap<HiQuickInfo, QuickInfoCell> cellCache;
	
	public GroupContentsCellRenderer() {
		this.cellCache = new HashMap<HiQuickInfo, QuickInfoCell>();
	}
        
        
    public void updateLanguage() {
        for ( QuickInfoCell cell : this.cellCache.values() )
            cell.updateLanguage();
    }
	
	public QuickInfoCell getCellForContent(HiQuickInfo info) {
		if ( style != GroupContentsList.HI_ListDisplayStyles.ICON_STYLE )
			return null;
		QuickInfoCell cell = cellCache.get(info);
		
		// create cell
		if ( cell == null ) {
			cell = new QuickInfoCell( info );
			cellCache.put(info, cell);		
		}
		
		return cell;
	}
	
	public void removeContentFromCache(HiQuickInfo info) {
		cellCache.remove(info);
	}
	
	public void clearCache() {
		this.cellCache.clear();
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		this.removeAll();

		
		if ( style == GroupContentsList.HI_ListDisplayStyles.ICON_STYLE ) {
			this.setBackground(Color.LIGHT_GRAY);

			// caching strategy
			QuickInfoCell cell = cellCache.get((HiQuickInfo)list.getModel().getElementAt(index));
			if ( cell == null ) {
				cell = new QuickInfoCell( (HiQuickInfo)list.getModel().getElementAt(index) );
				cellCache.put((HiQuickInfo)list.getModel().getElementAt(index), cell);				
			}			
			cell.setOpaque(true);
			cell.setSelected(isSelected);
			this.add(cell);

		} else {
			// TODO: implement caching strategy
			QuickInfoListCell cell = new QuickInfoListCell( (HiQuickInfo)list.getModel().getElementAt(index) );
			if ( index % 2 != 0 ) cell.setBackground(new Color(200,200,255));
	        cell.setOpaque(true);
			cell.setSelected(isSelected);
			this.add(cell, BorderLayout.CENTER);
		}
		
		this.setOpaque(true);
	
		return this;
	}
	
	public HI_ListDisplayStyles getDisplayStyle() {
		return this.style;
	}
	
	public void setDisplayStyle(HI_ListDisplayStyles style) {
		this.style = style;
		
		// DEBUG
		if ( style == HI_ListDisplayStyles.LIST_STYLE )
			this.setLayout(new BorderLayout());
		else this.setLayout(new FlowLayout());
	}


}
