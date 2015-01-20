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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class HITemplateSeperatorControl extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1258919684779203462L;

	
	private JToggleButton collapseToggleButton;
	private JSeparator seperator;
	private JLabel titleLabel;
	private String title;
	private JPanel items;
	private boolean collapsed = false;
	

	public HITemplateSeperatorControl(String title, JPanel items, boolean collapsed) {
		super();
		
		this.title = title;
		this.items = items;
		this.collapsed = collapsed;
		
		
		initComponents();
		collapseToggleButton.setSelected(!collapsed);
		updateItems();
		
		// attach listener
		collapseToggleButton.addActionListener(this);
		
	}
	
	public void updateTitle(String title) {
		this.title = title;
        titleLabel.setText(title);
        this.repaint();
        this.doLayout();
	}
	
	
	private void updateItems() {
		if ( items != null )
				items.setVisible(!collapsed);
	}
	
	private void initComponents() {

        collapseToggleButton = new JToggleButton();
        titleLabel = new JLabel();
        seperator = new JSeparator();

        collapseToggleButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/seperator-closed.png"))); // NOI18N
        collapseToggleButton.setSelected(true);
        collapseToggleButton.setBorder(BorderFactory.createEmptyBorder(3,5,5,3));
        collapseToggleButton.setSelectedIcon(new ImageIcon(getClass().getResource("/resources/icons/seperator-open.png"))); // NOI18N

        titleLabel.setForeground(Color.blue);
        titleLabel.setText(title);

        seperator.setBackground(Color.black);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(collapseToggleButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(titleLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(seperator, GroupLayout.DEFAULT_SIZE, 10, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createParallelGroup(GroupLayout.BASELINE)
                .add(collapseToggleButton)
                .add(titleLabel))
            .add(seperator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
    }


	// -------------------------------------------
	
	
	public void actionPerformed(ActionEvent e) {
		if ( collapseToggleButton.isSelected() ) 
			collapsed = false;
		else collapsed = true;
		
		updateItems();	
	}
	
}
