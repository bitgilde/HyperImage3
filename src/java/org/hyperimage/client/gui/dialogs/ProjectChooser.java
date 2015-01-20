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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.util.MetadataHelper;

import org.hyperimage.client.ws.HiProject;

/**
 * @author Jens-Martin Loebel
 */
// DEBUG - this and the dialog requires major cleanup and separation of MVC, only here because we needed it for the 2.0 release
public class ProjectChooser implements MouseListener, ActionListener, ListSelectionListener {

	private DefaultListModel projectListModel;
	private ProjectChooserDialog projChooserDialog;
	private JFrame parent;
	private HiProject selectedProject;
	private int selectedIndex;
	
	private List<HiProject> projects;
	

	
	public ProjectChooser(JFrame parent) {
        projectListModel = new DefaultListModel();
        this.parent = parent;
        
        projChooserDialog = new ProjectChooserDialog(parent);
        projChooserDialog.getProjectList().setModel(projectListModel);  
        
        // attach listeners
        projChooserDialog.getProjectList().addMouseListener(this);
        projChooserDialog.getProjectList().addListSelectionListener(this);
        projChooserDialog.getOkButton().addActionListener(this);
        projChooserDialog.getCancelButton().addActionListener(this);
        
	}
	
	public HiProject selectProject(List<HiProject> projects) {
		this.projects = projects;
		selectedIndex = 0;
		
		projectListModel.removeAllElements();
		for ( HiProject project : projects )
			projectListModel.addElement(project);
		if ( projects.size() > 0 ) projChooserDialog.getProjectList().setSelectedIndex(0);
		selectedProject = projects.get(selectedIndex);

		projChooserDialog.setBounds(
				(parent.getWidth()/2) - (projChooserDialog.getWidth()/2), 
				(parent.getHeight()/2) - (projChooserDialog.getHeight()/2), 
				projChooserDialog.getWidth(), 
				projChooserDialog.getHeight());

		// only show dialog, if user is registered in more than one project
		if ( projects.size() > 1 ) projChooserDialog.setVisible(true);
		else return projects.get(0);
	
		return selectedProject;
	}
	
	
	// -----------------------------------------------------------------------------------
	

	public void mouseClicked(MouseEvent e) {
		if ( e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 ) {
			projChooserDialog.setVisible(false);
		}
	}

	public void mouseEntered(MouseEvent e) {
		// not needed
	}

	public void mouseExited(MouseEvent e) {
		// not needed
	}

	public void mousePressed(MouseEvent e) {
		// not needed
	}

	public void mouseReleased(MouseEvent e) {
		// not needed
	}
	
	
	// -----------------------------------------------------------------------------------
	

	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equalsIgnoreCase("selectProject") )
			selectedProject = projects.get(selectedIndex);
		else 
			selectedProject = null;

			projChooserDialog.setVisible(false);
	}
	
	
	// -----------------------------------------------------------------------------------
	

	public void valueChanged(ListSelectionEvent e) {
		// don´t allow user to deselect the list
		if ( projChooserDialog.getProjectList().getSelectedIndex() == -1 ) {
			projChooserDialog.getProjectList().setSelectedIndex(selectedIndex);
			return;
		}
		if ( projChooserDialog.getProjectList().getSelectedIndex() != selectedIndex ) {
			selectedIndex = projChooserDialog.getProjectList().getSelectedIndex();
			selectedProject = projects.get(selectedIndex);
		}

	}
}
