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

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.hyperimage.client.Messages;

import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiQuickInfo;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class QuickInfoListCell extends JPanel {

	private static final long serialVersionUID = 6255714357570789171L;


	private HiQuickInfo info;
	
    private JLabel idLabel, typeLabel, titleLabel, countLabel;

    private boolean isSelected = false;
	
	public QuickInfoListCell(HiQuickInfo info) {
		this.info = info;
		
		initComponents();
		initInfoText();
	}
	
	
	public boolean isSelected() {
		return isSelected;
	}
	
	public void setSelected(boolean selected) {
		if ( selected != isSelected ) {
			isSelected = selected;;
			if ( isSelected ) this.setBackground(Color.yellow);
			else this.setBackground(Color.gray);
				
		}
	}

	
	private void initInfoText() {
		String idText = null;
		String typeText = null;
		
		if ( info.getContentType() == HiBaseTypes.HI_GROUP ) {
			idText = "G";
			typeText = Messages.getString("QuickInfoCell.GROUP");
		} else if ( info.getContentType() == HiBaseTypes.HI_INSCRIPTION ) {
			idText = "I";
			typeText = Messages.getString("QuickInfoCell.INSCRIPTION");
		} if ( info.getContentType() == HiBaseTypes.HI_LAYER ) {
			idText = "L";
			typeText = Messages.getString("QuickInfoCell.LAYER");
		} if ( info.getContentType() == HiBaseTypes.HI_LIGHT_TABLE ) {
			idText = "X";
			typeText = Messages.getString("QuickInfoCell.LIGHTTABLE");
		} if ( info.getContentType() == HiBaseTypes.HI_OBJECT ) {
			idText = "O";
			typeText = Messages.getString("QuickInfoCell.OBJECT");
		} if ( info.getContentType() == HiBaseTypes.HI_TEXT ) {
			idText = "T";
			typeText = Messages.getString("QuickInfoCell.TEXT");
		} if ( info.getContentType() == HiBaseTypes.HI_VIEW ) {
			idText = "V";
			typeText = Messages.getString("QuickInfoCell.VIEW");
		} if ( info.getContentType() == HiBaseTypes.HIURL ) {
			idText = "U";
			typeText = Messages.getString("QuickInfoCell.EXTERNALURL");
		} 
                
		idLabel.setText("<html><b>ID: </b>"+idText+info.getBaseID()+"</html>");
		typeLabel.setText(typeText);

// DEBUG for artop - remove
//		if ( info.getContentType() != HiBaseTypes.HI_OBJECT ) 
			if ( info.getTitle().length() > 0 ) titleLabel.setText(info.getTitle());
			else titleLabel.setText("-");
//		else titleLabel.setText("(n. A.)");
		countLabel.setText("("+info.getCount()+")");
	}
	
	private void initComponents() {

        idLabel = new JLabel();
        typeLabel = new JLabel();
        titleLabel = new JLabel();
        countLabel = new JLabel();

        setBackground(new Color(242,242,255));

        countLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        countLabel.setText("(32)");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(idLabel)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(typeLabel)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(titleLabel, GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(countLabel, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createParallelGroup(GroupLayout.BASELINE)
                .add(idLabel)
                .add(typeLabel)
                .add(titleLabel)
                .add(countLabel))
        );
    }
}
