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

package org.hyperimage.client.gui.dialogs;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class AboutDialog extends JDialog {

	private static final long serialVersionUID = -3081768612310435589L;

	
	private JLabel authorLabel;
    private JLabel logoLabel;
    private JLabel versionLabel;
    
    
	public AboutDialog(JFrame parent) {
		super(parent);
		this.setModal(true);
				
		initComponents();
		this.logoLabel.setText("<html><b>HyperImage Editor 3.0<br>Community Edition</b><br>Version "+HIRuntime.getClientVersion()+"</html>");
		this.setTitle(Messages.getString("AboutDialog.ABOUTHIEDITOR"));
		
		this.setBounds(
				(parent.getWidth()/2) - (this.getWidth()/2), 
				(parent.getHeight()/2) - (this.getHeight()/2), 
				this.getWidth(), 
				this.getHeight());
	}
	
	
	private void initComponents() {

        logoLabel = new JLabel();
        versionLabel = new JLabel();
        authorLabel = new JLabel();
 
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        logoLabel.setIcon(new ImageIcon(getClass().getResource("/resources/hyperimage-logo.png"))); // NOI18N
        logoLabel.setText("<html><b>HyperImage Editor 3.0<br>Community Edition</b><br>(SourceForge Release Version)</html>");

        versionLabel.setText("<html>SourceForge community release.<br>Website: <a href=\"http://sf.net/projects/hyperimage\">sf.net/projects/hyperimage</a><br><br>Copyright &copy; 2006-2015 HyperImage contributors.<br>Licensed&nbsp;under&nbsp;the&nbsp;Apache&nbsp;2.0&nbsp;License.&nbsp;(Portions&nbsp;CDDL&nbsp;1.0)</html>");

        authorLabel.setText("<html>For&nbsp;more&nbsp;information&nbsp;on&nbsp;HyperImage&nbsp;visit&nbsp;<a href=\"http://hyperimage.ws/\">hyperimage.ws</a></html>");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, authorLabel, GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, versionLabel, GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, logoLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(logoLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(versionLabel)
                .add(29, 29, 29)
                .add(authorLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }
}
