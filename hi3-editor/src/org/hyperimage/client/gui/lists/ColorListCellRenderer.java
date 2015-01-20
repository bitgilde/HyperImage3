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
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import org.hyperimage.client.Messages;

/**
 * @author Jens-Martin Loebel
 */
public class ColorListCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 7949103832494614959L;

	
	private JLabel cellLabel = new JLabel();
	private Vector<Color> projectColors;
	private JComboBox box;

	
	public ColorListCellRenderer(Vector<Color> projectColors, JComboBox box) {
		this.box = box;
        cellLabel.setFont(cellLabel.getFont().deriveFont((float)12));

		this.setBorder(new EmptyBorder(3,5,3,5));
		this.setLayout(new BorderLayout());
		this.add(cellLabel, BorderLayout.CENTER);
		this.projectColors = projectColors;
	}
	
	
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		Color color = (Color)value;
		// strip transparency
		color = new Color(color.getRed(), color.getGreen(), color.getBlue());
		
		if (index == -1 && box != null ) 
			index = box.getSelectedIndex();
		cellLabel.setIcon(LayerListCellRenderer.renderColorBarIcon(color));
		
		if ( index < projectColors.size() || box == null )
			cellLabel.setText((index+1)+" ("+color.getRed()+", "+color.getGreen()+", "+color.getBlue()+")");
		else cellLabel.setText(Messages.getString("LayerPropertyEditorView.50"));
		
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
        setOpaque(true);
        cellLabel.setOpaque(true);

		
		return this;
	}

}
