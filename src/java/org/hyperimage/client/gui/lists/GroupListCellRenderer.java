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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;

import org.hyperimage.client.Messages;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiGroup;

/**
 * @author Jens-Martin Loebel
 */
public class GroupListCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 7602833818020929120L;

	
	private JLabel cellLabel = new JLabel();
	private HiGroup importGroup, trashGroup;
	private String defaultLang;
	private JSeparator separator = new JSeparator();
	
	private int dropIndex = -1;
	
	
	public GroupListCellRenderer(HiGroup importGroup, HiGroup trashGroup, String defaultLang) {
		this.importGroup = importGroup;
		this.trashGroup = trashGroup;
		this.defaultLang = defaultLang;
		
        cellLabel.setFont(cellLabel.getFont().deriveFont((float)12));
		
		this.setLayout(new BorderLayout());
		this.add(cellLabel, BorderLayout.CENTER);
	}
	
	public void setDefaultLang(String defaultLang) {
		this.defaultLang = defaultLang;
	}
	
	public void resetGroups(HiGroup importGroup, HiGroup trashGroup) {
		this.importGroup = importGroup;
		this.trashGroup = trashGroup;
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		HiGroup group = (HiGroup)value;

		this.remove(separator);
		if ( group.getId() == importGroup.getId() ) {
			this.add(separator, BorderLayout.SOUTH);
			cellLabel.setText("<html>"+Messages.getString("GroupListCellRenderer.1")+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			cellLabel.setIcon(null);
			cellLabel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/import-icon.png")));			
		} else if ( group.getId() == trashGroup.getId() ) {
			this.add(separator, BorderLayout.NORTH);
			cellLabel.setText("<html>"+Messages.getString("GroupListCellRenderer.4")+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			cellLabel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/trashcan-icon.png")));			 //$NON-NLS-1$
		} else {
                    String id = "G"+group.getId();
                    if ( group.getUUID() != null ) id = group.getUUID();
			HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(group, defaultLang);
			if ( record != null ) {
				String title = MetadataHelper.findValue("HIBase", "title", record); //$NON-NLS-1$ //$NON-NLS-2$
				if ( title.length() > 0 ) cellLabel.setText(title);
				else cellLabel.setText(Messages.getString("GroupListCellRenderer.9")+" ("+id+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else cellLabel.setText(Messages.getString("GroupListCellRenderer.12")+" ("+id+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			cellLabel.setIcon(null);
		}
				
		// DEBUG
		
		if ( isSelected ) {
            this.setBackground(list.getBackground());
            cellLabel.setBackground(list.getSelectionBackground());
            cellLabel.setForeground(list.getSelectionForeground());
        } else {
        	cellLabel.setBackground(list.getBackground());
        	cellLabel.setForeground(list.getForeground());
        	this.setBackground(list.getBackground());
        }
		
		if ( list.getDropLocation() != null 
			&& index == list.getDropLocation().getIndex()
			&& !list.getDropLocation().isInsert()) {
            cellLabel.setBackground(Color.green);
            cellLabel.setForeground(list.getSelectionForeground());
            if ( list.getDropLocation() != null && dropIndex != list.getDropLocation().getIndex() ) {
                dropIndex = list.getDropLocation().getIndex();
            	list.repaint();
            }
		}
		
		

		if ( list.getDropLocation() == null && dropIndex >= 0 ) {
			dropIndex = -1;
			list.repaint();
		}

        this.setEnabled(list.isEnabled());
        cellLabel.setEnabled(list.isEnabled());
        cellLabel.setOpaque(true);
        this.setOpaque(true);
   
		
		return this;
	}
}
