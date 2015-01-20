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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.views.AccountSettingsView;
import org.hyperimage.client.ws.HiUser;


/**
 * 
 * Class: AccountSettings
 * Package: org.hyperimage.client.components
 * @author Jens-Martin Loebel
 *
 */
public class AccountSettings extends HIComponent implements ActionListener {

	private AccountSettingsView settingsView;
	
	private HiUser user;
		
        
        @Override
        public void updateLanguage() {
            super.updateLanguage();
            
        setTitle(Messages.getString("AccountSettings.ACCOUNTSETTINGS"));
        getWindowMenuItem().setText(getTitle());
	HIRuntime.getGui().updateComponentTitle(this);
            
        }
	
	public boolean requestClose() {
		return askToSaveOrCancelChanges();
	}
	
	
	public AccountSettings(HiUser user) {
		super(Messages.getString("AccountSettings.ACCOUNTSETTINGS"), Messages.getString("AccountSettings.ACCOUNT"));	
		this.user = user;
		
		// init views
		settingsView = new AccountSettingsView(user);
		
		
		// register views
		views.add(settingsView);
		
		// attach listeners
		settingsView.getUpdateButton().addActionListener(this);
		settingsView.getCancelButton().addActionListener(this);

	}
	
	private void saveNewPassword() {
		if ( settingsView.hasPasswordChanges() ) {
			String password = settingsView.getNewPassword();
			if ( password.length() < 6 ) {
				HIRuntime.getGui().displayInfoDialog(Messages.getString("AccountSettings.CHANGEPASSWORD"), Messages.getString("AccountSettings.NEWPWD6CHARS"));
				settingsView.resetPassword();
			} else {
				try {
					HIRuntime.getManager().updateUserPassword(user, password);
					// inform user
					HIRuntime.getGui().displayInfoDialog(Messages.getString("AccountSettings.CHANGEPASSWORD"), Messages.getString("AccountSettings.PWDCHANGESUCCESSFUL"));
					settingsView.resetPassword();
				} catch (HIWebServiceException wse) {
					settingsView.resetPassword();
					HIRuntime.getGui().reportError(wse, this);
					return;
				}
			}
			
		}
	}
	
	private void saveMetadataChanges() {
		if ( settingsView.hasMetadataChanges() ) {
			settingsView.syncMetadataChanges();
			try {
				HIRuntime.getManager().updateUser(user);
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
			}
		}
	}
	
	private boolean askToSaveOrCancelChanges() {
		if ( (settingsView.hasMetadataChanges() || settingsView.hasMetadataChanges())
				&& HIRuntime.getGui().checkEditAbility(true)) {
			int decision = JOptionPane.showConfirmDialog(
					HIRuntime.getGui(), 
			Messages.getString("AccountSettings.SAVEACCOUNTCHANGES"));

			if ( decision == JOptionPane.CANCEL_OPTION ) 
				return false;
			else if ( decision == JOptionPane.YES_OPTION ) {
				saveMetadataChanges();
				saveNewPassword();
			}
		}		
		return true;
	}

	
	// -------------------------------------------------------------------------------------------------
	

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if ( e.getActionCommand().equalsIgnoreCase("update") ) {
			if ( settingsView.hasMetadataChanges() || settingsView.hasPasswordChanges() ) {
				// check user role
				if ( !HIRuntime.getGui().checkEditAbility(false) )
					return;
				saveMetadataChanges();
				saveNewPassword();
			}
		}
		
		if ( e.getActionCommand().equalsIgnoreCase("cancel") ) {
			// remove action listeners and ask GUI to de-register settings editor
			settingsView.getUpdateButton().removeActionListener(this);
			settingsView.getCancelButton().removeActionListener(this);
			HIRuntime.getGui().deregisterComponent(this, true);
		}
	}



	
}
