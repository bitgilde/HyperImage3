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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.hyperimage.client.Messages;

import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiLayer;

/**
 * @author Jens-Martin Loebel
 */
public class LayerListCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = -6994190696912376656L;

	
	private JLabel cellLabel = new JLabel();
	private String defaultLang;
	
	public LayerListCellRenderer(String defaultLang) {
            this.defaultLang = defaultLang;
		
            cellLabel.setFont(cellLabel.getFont().deriveFont((float)12));
		
            this.setLayout(new BorderLayout());
            this.add(cellLabel, BorderLayout.CENTER);
	}

	public void setDefaultLang(String defaultLang) {
            this.defaultLang = defaultLang;
	}
	
	public static ImageIcon renderColorBarIcon(Color color) 
	{
		BufferedImage colorImage;
		Graphics2D g2d;
		colorImage = new BufferedImage(15,10, BufferedImage.TYPE_INT_RGB);
		g2d = colorImage.createGraphics();
		g2d.setColor(color);
		g2d.fillRect(0,0,15,10);
		g2d.draw3DRect(0,0,15,10, true);
		return new ImageIcon(colorImage);
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		HiLayer layer = null;
		HILayer wrappedLayer = null;
		if ( value instanceof HILayer ) {
			layer = ((HILayer)value).getModel();
			wrappedLayer = (HILayer)value;
		} else if ( value instanceof HiLayer )
			layer = (HiLayer)value;

                String id = "L"+layer.getId();
                if ( layer.getUUID() != null ) id = layer.getUUID();
                
		HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(layer, defaultLang);
		if ( record != null ) {
			String title = MetadataHelper.findValue("HIBase", "title", record);
			if ( title == null )
				cellLabel.setText(Messages.getString("LayerListCellRenderer.layer")+" ("+id+")");
			else if ( title.length() == 0 )
				cellLabel.setText(Messages.getString("LayerListCellRenderer.layer")+" ("+id+")");
			else cellLabel.setText(title);
		} else cellLabel.setText(Messages.getString("LayerListCellRenderer.layer")+" ("+id+")");
		cellLabel.setIcon(null);
		
		if ( wrappedLayer == null )
			cellLabel.setIcon(renderColorBarIcon(new Color(layer.getRed(), layer.getGreen(), layer.getBlue())));
		else
			cellLabel.setIcon(renderColorBarIcon(wrappedLayer.getSolidColour()));
			
		if (isSelected) {
            this.setBackground(list.getSelectionBackground());
            cellLabel.setBackground(list.getSelectionBackground());
            cellLabel.setForeground(list.getSelectionForeground());
        } else {
            this.setBackground(list.getBackground());
            cellLabel.setBackground(list.getBackground());
            cellLabel.setForeground(list.getForeground());
        }
        this.setEnabled(list.isEnabled());
        cellLabel.setEnabled(list.isEnabled());
        cellLabel.setOpaque(true);
        this.setOpaque(true);

		
		return this;
	}

}
