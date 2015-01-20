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

package org.hyperimage.client.gui.dialogs;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import org.hyperimage.client.gui.ResetButton;
import org.hyperimage.client.gui.lists.ProjectListCellRenderer;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Jens-Martin Loebel
 */
//DEBUG - this and the dialog requires major cleanup and separation of MVC, only here because we needed it for the artop 2.0 release
class ProjectChooserDialog extends JDialog {
	
	private static final long serialVersionUID = -6788486174590741450L;

	
	private ResetButton cancelButton;
	private JPanel chooserPanel;
	private JScrollPane listScrollPane;
	private JButton okButton;
	private JList projectList;
	private ProjectListCellRenderer projectCellRenderer;
	

	public ProjectChooserDialog(JFrame parent) {
		super(parent, true);	
		
		initComponents();
		
	}
	
	public JList getProjectList() {
		return projectList;
	}

	public JButton getOkButton() {
		return okButton;
	}
	
	public JButton getCancelButton() {
		return cancelButton;
	}
	
	private void initComponents() {

        chooserPanel = new JPanel();
        listScrollPane = new JScrollPane();
        projectList = new JList();
        okButton = new JButton();
        cancelButton = new ResetButton();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        listScrollPane.setViewportView(projectList);

       GroupLayout chooserPanelLayout = new GroupLayout(chooserPanel);
        chooserPanel.setLayout(chooserPanelLayout);
        chooserPanelLayout.setHorizontalGroup(
            chooserPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(chooserPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(chooserPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, chooserPanelLayout.createSequentialGroup()
                        .add(cancelButton,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(okButton,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
                    .add(listScrollPane,GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                .addContainerGap())
        );
        chooserPanelLayout.setVerticalGroup(
            chooserPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, chooserPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(listScrollPane,GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chooserPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
                    .add(cancelButton,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

       GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(chooserPanel,GroupLayout.DEFAULT_SIZE,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(chooserPanel,GroupLayout.DEFAULT_SIZE,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
        
        // -----

        this.setTitle("Projekt Auswählen");
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectCellRenderer = new ProjectListCellRenderer();
        projectList.setCellRenderer(projectCellRenderer);

        // adjust buttons
        okButton.setToolTipText("Projekt auswählen");
        okButton.setActionCommand("selectProject");
        okButton.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        okButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/ok.png"))); //$NON-NLS-1$
        okButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/ok-active.png"))); //$NON-NLS-1$
        okButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/ok-disabled.png"))); //$NON-NLS-1$
        okButton.setPreferredSize(new Dimension(24, 24));
        
        cancelButton.setToolTipText("Abbruch");
        cancelButton.setActionCommand("cancel");
        
        okButton.requestFocus();
    }
	
}
