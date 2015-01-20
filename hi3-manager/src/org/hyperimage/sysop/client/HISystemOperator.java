/* $Id: HISystemOperator.java 154 2009-11-19 09:40:45Z jmloebel $ */

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

package org.hyperimage.sysop.client;

import java.util.ArrayList;
import java.util.List;

import org.hyperimage.client.HIWebServiceManager;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HIEditor;
import org.hyperimage.client.ws.HIEntityException_Exception;
import org.hyperimage.client.ws.HIEntityNotFoundException_Exception;
import org.hyperimage.client.ws.HIMaintenanceModeException_Exception;
import org.hyperimage.client.ws.HIParameterException_Exception;
import org.hyperimage.client.ws.HIPrivilegeException_Exception;
import org.hyperimage.client.ws.HIServiceException_Exception;
import org.hyperimage.client.ws.HiProject;
import org.hyperimage.client.ws.HiRoles;
import org.hyperimage.client.ws.HiUser;

/**
 * This class exposes the administrative functions to the GUI.
 * 
 * @author Heinz-Guenter Kuper
 *
 */
public class HISystemOperator {
	private static HIWebServiceManager m_manager = null;
	private static HIEditor m_port = null;
	
	@SuppressWarnings("deprecation")
	public HISystemOperator(HIWebServiceManager manager) {
		m_manager = manager;
		m_port = m_manager.getWSPort();
	}
	
	public boolean logout() {
		return m_manager.logout();
	}
	
	/**
	 * List all the currently available projects.
	 * @return List of projects
	 */
	public List<HiProject> getAllProjects() {
		try {
			return m_port.getProjects();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Return list of all users sorted by last name.
	 * @return
	 */
	public  List<HiUser> getAllUsersAlphabetically() {
		try {
			return m_port.sysopGetUsers();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * List all the projects for which a particular user is the project admin.
	 * @param lUserID
	 * @return
	 */
	public List<HiProject> getProjectsForUser(long lUserID) {
		List<HiProject> projects = null;
		try {
			projects = m_port.sysopGetProjectsForUser(lUserID);
		} catch (HIEntityNotFoundException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		
		return projects;
	}
	
	public List<HiUser> getUsersInProject(long lProjectID) {
		List<HiUser> users = null;
		try {
			users = m_port.sysopGetUsersForProject(lProjectID);
		} catch (HIEntityNotFoundException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		
		return users;
	}
	
	public HiUser addUser(String strUsername, String strUserFirstName, String strUserLastname,
						String strPassword, String strUserEmail) {
		HiUser user = null;
		try {
			user = m_port.sysopCreateUser(strUserFirstName, strUserLastname, strUsername, strPassword, strUserEmail);
		} catch (HIEntityException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIParameterException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		
		return user;
	}
	
	public boolean delUser(String strUsername) {
		HiUser user = getUser(strUsername);
		boolean result = false;
		
		try {
			result = m_port.sysopDeleteUser(user.getId());
		} catch (HIEntityNotFoundException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIPrivilegeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		
		return result;
	}
	
	public void modUser(final String strUsername, String strNewFirstName, String strNewLastName, String strNewEmail) {
		HiUser user = getUser(strUsername);
		
		try {
			m_port.updateUser(user.getId(), strNewFirstName, strNewLastName, strNewEmail);
		} catch (HIEntityNotFoundException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIParameterException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIPrivilegeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
	}
	
	public void modUserPasswd(final String strUsername, String strNewPassword) {
		HiUser user = getUser(strUsername);
		
		try {
			m_port.updateUserPassword(user.getId(), strNewPassword);
		} catch (HIEntityNotFoundException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIParameterException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIPrivilegeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
	}
	
	public HiProject addProject(String strAdminUsername, String strDefaultLang, String title) {
		HiProject project = null;
		try {
			project = m_port.sysopCreateProject(strAdminUsername, strDefaultLang);
			// JML: add Dublin Core Metadata to newly created projects
			m_port.setProject(project);
			m_port.adminAddTemplateToProject(MetadataHelper.getDCTemplateBlueprint());
			// JML: add project title
			if ( title != null && title.length() > 0 )
				m_port.updateProjectMetadata(strDefaultLang, title);

		} catch (HIEntityNotFoundException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIParameterException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIServiceException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIPrivilegeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		
		return project;
	}
	
	public boolean delProject(long lProjectID) {
		boolean result = false;
		
		try {
			result = m_port.sysopDeleteProject(lProjectID);
		} catch (HIPrivilegeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIEntityNotFoundException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIParameterException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	public boolean assignAdminToProject(long lUserID, long lProjectID) {
		boolean result = false;
		try {
			result = m_port.sysopAddUserToProject(lUserID, lProjectID, HiRoles.ADMIN);
		} catch (HIEntityNotFoundException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIPrivilegeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean removeAdminFromProject(long lUserID, long lProjectID) {
		boolean result = false;
		try {
			result = m_port.sysopRemoveUserFromProject(lUserID, lProjectID);
		} catch (HIEntityNotFoundException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIPrivilegeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		
		return result;
	}
	
	public List<HiUser> getProjectAdminsForProject(long lProjectID) {
		List<HiUser> users = null;
		List<HiUser> adminUsers = new ArrayList<HiUser>();
		try {
			users = m_port.sysopGetUsersForProject(lProjectID);
		} catch (HIEntityNotFoundException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		
		for( HiUser usr : users ) {
			try {
				if( m_port.adminGetRoleInProject(usr.getId(), lProjectID) == HiRoles.ADMIN )
					adminUsers.add(usr);
			} catch (HIEntityNotFoundException_Exception e) {
				// Stack trace gets written to server log.
				e.printStackTrace();
			} catch (HIPrivilegeException_Exception e) {
				// Stack trace gets written to server log.
				e.printStackTrace();
			} catch (HIServiceException_Exception e) {
				// Stack trace gets written to server log.
				e.printStackTrace();
			} catch (HIMaintenanceModeException_Exception e) {
				// Stack trace gets written to server log.
				e.printStackTrace();
			}
		}
		
		return adminUsers;
	}
	

	/////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// UTILITY METHODS ///////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////
	
	private HiUser getUser(String strUsername) {
		HiUser user = null;
		try {
			user = m_port.adminGetUserByUserName(strUsername);
		} catch (HIEntityNotFoundException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIParameterException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// Stack trace gets written to server log.
			e.printStackTrace();
		}
		
		return user;
	}
}
