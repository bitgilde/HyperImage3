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

package org.hyperimage.client.gui.views;

import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.lists.UserListCellRenderer;
import org.hyperimage.client.ws.HiUser;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class UserManagerView extends GUIView {

	private static final long serialVersionUID = 5632733075676981394L;

	
	private JPanel assignPanel;
    private JLabel infoLabel;
    private JList projectGuestsList;
    private JPanel projectGuestsPanel;
    private JScrollPane projectGuestsScroll;
    private JList projectUsersList;
    private JPanel projectUsersPanel;
    private JScrollPane projectUsersScroll;
    private JList systemUsersList;
    private JPanel systemUsersPanel;
    private JScrollPane systemUsersScroll;
    private TitledBorder systemUsersBorder;
    private TitledBorder projectUsersBorder;
    private TitledBorder projectGuestsBorder;
    
    
    private DefaultListModel systemUsersModel, projectUsersModel, guestUsersModel;
    
    
	public UserManagerView(List<HiUser> users, List<HiUser> projectUsers, List<HiUser> projectGuests) {
		super(Messages.getString("UserManagerView.0")); //$NON-NLS-1$
		
		initComponents();
		
		// remove project users and guests from system user list
		Vector<HiUser> systemUsers = new Vector<HiUser>();
		for ( HiUser user : users ) {
			boolean isInProject = false;
			
			for ( HiUser projUser : projectUsers )
				if ( projUser.getId() == user.getId() )
					isInProject = true;
			for ( HiUser projUser : projectGuests )
				if ( projUser.getId() == user.getId() )
					isInProject = true;
			
			if ( !isInProject ) systemUsers.addElement(user);
		}
		
		// pre-fill lists, removing sysop user as possible option
		if ( users != null && projectUsers != null && projectGuests != null ) {
			for ( HiUser user : systemUsers )
				if ( user.getUserName().compareTo("sysop") != 0 ) systemUsersModel.addElement(user); //$NON-NLS-1$
			for ( HiUser user : projectUsers )
				if ( user.getUserName().compareTo("sysop") != 0 ) projectUsersModel.addElement(user); //$NON-NLS-1$
			for ( HiUser user : projectGuests )
				if ( user.getUserName().compareTo("sysop") != 0 ) guestUsersModel.addElement(user); //$NON-NLS-1$
		}
		
		// init Drag and Drop
		systemUsersList.setDragEnabled(true);
		systemUsersList.setDropMode(DropMode.INSERT);
		projectUsersList.setDragEnabled(true);
		projectUsersList.setDropMode(DropMode.INSERT);
		projectGuestsList.setDragEnabled(true);
		projectGuestsList.setDropMode(DropMode.INSERT);
		
		setDisplayPanel(assignPanel);
	}
	
	
	public void updateLanguage() {
        infoLabel.setText(Messages.getString("UserManagerView.6")); //$NON-NLS-1$
        systemUsersBorder.setTitle(Messages.getString("UserManagerView.4")); //$NON-NLS-1$
        projectUsersBorder.setTitle(Messages.getString("UserManagerView.7")); //$NON-NLS-1$
        projectGuestsBorder.setTitle(Messages.getString("UserManagerView.9")); //$NON-NLS-1$
        
        super.setTitle(Messages.getString("UserManagerView.0")); //$NON-NLS-1$
	}
	
	
	public JList getSystemUsersList() {
		return systemUsersList;
	}
	
	public JList getProjectUsersList() {
		return projectUsersList;
	}

	public JList getProjectGuestsList() {
		return projectGuestsList;
	}
	
	
	private void initComponents() {

        assignPanel = new JPanel();
        systemUsersPanel = new JPanel();
        systemUsersScroll = new JScrollPane();
        systemUsersList = new JList();
        infoLabel = new JLabel();
        projectUsersPanel = new JPanel();
        projectUsersScroll = new JScrollPane();
        projectUsersList = new JList();
        projectGuestsPanel = new JPanel();
        projectGuestsScroll = new JScrollPane();
        projectGuestsList = new JList();

        systemUsersBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("UserManagerView.4"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
        systemUsersPanel.setBorder(systemUsersBorder);

        systemUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        systemUsersScroll.setViewportView(systemUsersList);

        GroupLayout systemUsersPanelLayout = new GroupLayout(systemUsersPanel);
        systemUsersPanel.setLayout(systemUsersPanelLayout);
        systemUsersPanelLayout.setHorizontalGroup(
            systemUsersPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(systemUsersScroll, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
        );
        systemUsersPanelLayout.setVerticalGroup(
            systemUsersPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(systemUsersScroll, GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
        );

        projectUsersBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("UserManagerView.7"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
        projectUsersPanel.setBorder(projectUsersBorder);

        projectUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectUsersScroll.setViewportView(projectUsersList);

        GroupLayout projectUsersPanelLayout = new GroupLayout(projectUsersPanel);
        projectUsersPanel.setLayout(projectUsersPanelLayout);
        projectUsersPanelLayout.setHorizontalGroup(
            projectUsersPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(projectUsersScroll, GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
        );
        projectUsersPanelLayout.setVerticalGroup(
            projectUsersPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(projectUsersScroll, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
        );

        projectGuestsBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("UserManagerView.9"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
        projectGuestsPanel.setBorder(projectGuestsBorder);

        projectGuestsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectGuestsScroll.setViewportView(projectGuestsList);

        GroupLayout projectGuestsPanelLayout = new GroupLayout(projectGuestsPanel);
        projectGuestsPanel.setLayout(projectGuestsPanelLayout);
        projectGuestsPanelLayout.setHorizontalGroup(
            projectGuestsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(projectGuestsScroll, GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
        );
        projectGuestsPanelLayout.setVerticalGroup(
            projectGuestsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(projectGuestsScroll, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
        );

        GroupLayout assignPanelLayout = new GroupLayout(assignPanel);
        assignPanel.setLayout(assignPanelLayout);
        assignPanelLayout.setHorizontalGroup(
            assignPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, assignPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(assignPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, infoLabel, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
                    .add(assignPanelLayout.createSequentialGroup()
                        .add(systemUsersPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.UNRELATED)
                        .add(assignPanelLayout.createParallelGroup(GroupLayout.LEADING)
                            .add(projectGuestsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(projectUsersPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        assignPanelLayout.setVerticalGroup(
            assignPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(assignPanelLayout.createSequentialGroup()
                .add(20, 20, 20)
                .add(assignPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(assignPanelLayout.createSequentialGroup()
                        .add(projectUsersPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.UNRELATED)
                        .add(projectGuestsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(systemUsersPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(infoLabel, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

       
        // -----
        UserListCellRenderer renderer = new UserListCellRenderer();
        systemUsersModel = new DefaultListModel();
        projectUsersModel = new DefaultListModel();
        guestUsersModel = new DefaultListModel();
        
        systemUsersList.setCellRenderer(renderer);
        systemUsersList.setModel(systemUsersModel);
        projectUsersList.setCellRenderer(renderer);
        projectUsersList.setModel(projectUsersModel);
        projectGuestsList.setCellRenderer(renderer);
        projectGuestsList.setModel(guestUsersModel);
        
        
        updateLanguage();
        
    }	

	
}
