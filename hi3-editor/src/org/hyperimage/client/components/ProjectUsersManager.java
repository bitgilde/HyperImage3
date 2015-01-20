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

package org.hyperimage.client.components;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.dnd.UserTransferable;
import org.hyperimage.client.gui.views.UserManagerView;
import org.hyperimage.client.ws.HiRoles;
import org.hyperimage.client.ws.HiUser;

/**
 * @author Jens-Martin Loebel
 */
public class ProjectUsersManager extends HIComponent {

	private UserManagerView userManagerView;
	
	
	/**
	 * This class implements the Drag and Drop handler for User Lists.
	 * 
	 * Class: UserTransferHandler
	 * @author Jens-Martin Loebel
	 *
	 */
	public class UserTransferHandler extends TransferHandler {

		private static final long serialVersionUID = -876283873557001866L;

		
		private boolean userImported = false;
		private HIComponent userManager;
		
		
		public UserTransferHandler(HIComponent userManager) {
			this.userManager = userManager;
		}
		
		
		// -------- export methods ---------

		
		public int getSourceActions(JComponent c) {
			return MOVE;
		}

		protected Transferable createTransferable(JComponent c) {
			HiUser user;

			user = (HiUser) ((JList)c).getSelectedValue();

			return new UserTransferable(user);
		}

		protected void exportDone(JComponent c, Transferable t, int action) {
			if ( userImported && t.isDataFlavorSupported(UserTransferable.userFlavor) ) {
				HiUser user;
				try {
					user = (HiUser) t.getTransferData(UserTransferable.userFlavor);
				} catch (UnsupportedFlavorException e) {
					return; // error occurred or user empty
				} catch (IOException e) {
					return; // error occurred or user empty
				}
				
				// update GUI, remove from source list
				// server action is executed by import method
				DefaultListModel model = (DefaultListModel) ((JList)c).getModel();
				if ( model.contains(user) ) 
					model.removeElement(user);
			}
		}


		// -------- import methods ---------


		public boolean canImport(TransferSupport supp) {
			if ( supp.isDataFlavorSupported(UserTransferable.userFlavor) ) {
				HiUser user;
				try {
					user = (HiUser) supp.getTransferable().getTransferData(UserTransferable.userFlavor);
				} catch (UnsupportedFlavorException e) {
					return false; // error occurred or user empty
				} catch (IOException e) {
					return false; // error occurred or user empty
				}
				
				// can´t reorder a user list, only move users to different lists
				DefaultListModel model = (DefaultListModel) ((JList)supp.getComponent()).getModel();			
				if ( model.contains(user) ) return false;
								
				return true;
			}
			
			return false;
		}

		public boolean importData(TransferSupport supp) {
			userImported = false;
			if (!canImport(supp)) // check if we can import this
				return false;
			
			supp.setDropAction(MOVE);
			
			// DEBUG
			if ( supp.isDataFlavorSupported(UserTransferable.userFlavor) ) {
				HiUser user;
				try {
					user = (HiUser) supp.getTransferable().getTransferData(UserTransferable.userFlavor);
				} catch (UnsupportedFlavorException e) {
					return false; // error occurred or user empty
				} catch (IOException e) {
					return false; // error occurred or user empty
				}
				
				// can´t reorder a user list, only move users to different lists
				JList list = (JList)supp.getComponent();
				DefaultListModel model = (DefaultListModel) list.getModel();			
				if ( model.contains(user) ) return false;

				
				if ( list == userManagerView.getSystemUsersList() ) {
					/* ***********************************
					 * handle removing a user from project
					 * ***********************************
					 */
					try {
						HIRuntime.getGui().startIndicatingServiceActivity();
						HIRuntime.getManager().removeUserFromProject(user);
						HIRuntime.getGui().stopIndicatingServiceActivity();
					} catch (HIWebServiceException wse) {
						HIRuntime.getGui().reportError(wse, userManager);
						return false;
					}
				}

				if ( list == userManagerView.getProjectUsersList()
					|| list == userManagerView.getProjectGuestsList() ) {
					/* ***********************************
					 * handle adding a user to the project
					 * ***********************************
					 */
					try {
						HiRoles role = HiRoles.USER;
						// check if user should be added as a guest
						if ( list == userManagerView.getProjectGuestsList() )
							role = HiRoles.GUEST;
						
						HIRuntime.getGui().startIndicatingServiceActivity();
						HIRuntime.getManager().addUserToProject(user, role);
						HIRuntime.getGui().stopIndicatingServiceActivity();
					} catch (HIWebServiceException wse) {
						HIRuntime.getGui().reportError(wse, userManager);
						return false;
					}
				}

				
				
				// update GUI, add user to target list
				model.addElement(user);
				userImported = true; // set for export method, import succeeded
				return true;				
			}
			
			return false;
		}
	}
	
	
	// ---------------------------------------------------------------------------------------------------
	
	
	public ProjectUsersManager() {
		super(Messages.getString("ProjectUsersManager.0"), Messages.getString("ProjectUsersManager.1")); //$NON-NLS-1$ //$NON-NLS-2$
		
		boolean loadingErrorOccurred = false;
		
		
		List<HiUser> projectUsers;
		List<HiUser> projectGuests = new ArrayList<HiUser>();
		List<HiUser> filteredUsers = new ArrayList<HiUser>();
		
		try {
			HIRuntime.getGui().startIndicatingServiceActivity();
			projectUsers = HIRuntime.getManager().getProjectUsers();
			
			// filter lists, sort out project admins and put guests in separate list
			List<HiUser> users = new ArrayList<HiUser>(projectUsers.size());
			for ( HiUser user : projectUsers ) {
				HiRoles role = HIRuntime.getManager().getRoleInProject(user, HIRuntime.getManager().getProject());
				
				if ( role == HiRoles.USER )
					users.add(user);
				else if ( role == HiRoles.GUEST )
					projectGuests.add(user);
				else 
					filteredUsers.add(user); // remember filteted user
			}
			projectUsers = users; // replace with filtered list

			// remove filtered users from available system users
			List<HiUser> systemUsers = HIRuntime.getManager().getUsersAsAdmin();
			List<HiUser> cleanSystemUsers = new ArrayList<HiUser>(systemUsers.size());
			for ( HiUser user : systemUsers ) {
				boolean isFilteredUser = false;
				for ( HiUser filteredUser : filteredUsers )
					if ( filteredUser.getId() == user.getId() )
						isFilteredUser = true;

				if ( !isFilteredUser )
					cleanSystemUsers.add(user);
			}
			systemUsers = cleanSystemUsers;

			// init views
			userManagerView = new UserManagerView(systemUsers, projectUsers, projectGuests);

			HIRuntime.getGui().stopIndicatingServiceActivity();

		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
			loadingErrorOccurred = true;
		}
		
		if ( loadingErrorOccurred )
			userManagerView = new UserManagerView(null, null, null);
		
		// register views
		views.add(userManagerView);
		
		// init Drag and Drop
		UserTransferHandler handler = new UserTransferHandler(this);
		
		userManagerView.getSystemUsersList().setTransferHandler(handler);
		userManagerView.getProjectUsersList().setTransferHandler(handler);
		userManagerView.getProjectGuestsList().setTransferHandler(handler);
		
		// attach listeners
		
	}
	
	
	public void updateLanguage() {
		super.updateLanguage();

		super.setTitle(Messages.getString("ProjectUsersManager.0")); //$NON-NLS-1$
		getWindowMenuItem().setText(Messages.getString("ProjectUsersManager.1")); //$NON-NLS-1$
		HIRuntime.getGui().updateComponentTitle(this);
	}
	
}
