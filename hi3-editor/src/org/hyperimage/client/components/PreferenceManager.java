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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListModel;
import javax.swing.JColorChooser;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.views.PreferenceManagerView;
import org.hyperimage.client.ws.HiBase;

/**
 * 
 * Class: PreferenceManager
 * Package: org.hyperimage.client.components
 * @author Jens-Martin Loebel
 *
 */
public class PreferenceManager extends HIComponent implements ActionListener, MouseListener {

	PreferenceManagerView preferenceView;

	public PreferenceManager() {
		super(Messages.getString("PreferenceManager.0"), Messages.getString("PreferenceManager.1")); //$NON-NLS-1$ //$NON-NLS-2$

		// init views
		preferenceView = new PreferenceManagerView();

		// fill lists
		preferenceView.rebuildPreferenceLists(HIRuntime.getManager().getProjectPolygons(), HIRuntime.getManager().getProjectColors());

		// register views
		views.add(preferenceView);

		// attach listeners
		preferenceView.getEdiButton().addActionListener(this);
		preferenceView.getRemoveButton().addActionListener(this);
		preferenceView.getColorPrefList().addMouseListener(this);

	}
	
	
	public void updateLanguage() {
		super.updateLanguage();

		super.setTitle(Messages.getString("PreferenceManager.0")); //$NON-NLS-1$
		getWindowMenuItem().setText(Messages.getString("PreferenceManager.1")); //$NON-NLS-1$
		HIRuntime.getGui().updateComponentTitle(this);
	}


	public void receiveMessage(HIMessageTypes message, HiBase base ) {
		if ( message == HIMessageTypes.PREFERENCE_MODIFIED )
			preferenceView.rebuildPreferenceLists(HIRuntime.getManager().getProjectPolygons(), HIRuntime.getManager().getProjectColors());
	}


	// --------------------------------------------------------------------------------------------------


	private void editColorAction() {
		// check user role
		if ( !HIRuntime.getGui().checkEditAbility(false) )
			return;

		int index = preferenceView.getColorPrefList().getSelectedIndex();
		Color newColor = JColorChooser.showDialog(preferenceView, Messages.getString("PreferenceManager.2"),  //$NON-NLS-1$
				HIRuntime.getManager().getProjectColors().get(index));
		// update color
		if ( newColor != null ) {
			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().getProjectColors().setElementAt(newColor, index);
				HIRuntime.getManager().updateProjectColors();
				newColor = HIRuntime.getManager().getProjectColors().get(index);
				// update GUI
				((DefaultListModel)preferenceView.getColorPrefList().getModel()).setElementAt(newColor, index);
				preferenceView.getColorPrefList().repaint();
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, this);
				HIRuntime.getGui().stopIndicatingServiceActivity();
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}
	}


	// --------------------------------------------------------------------------------------------------


	@Override
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equalsIgnoreCase("edit")  //$NON-NLS-1$
				&& preferenceView.getColorPrefList().getSelectedIndex() >= 0)
			editColorAction();
		
		if ( e.getActionCommand().equalsIgnoreCase("remove")  //$NON-NLS-1$
				&& ( preferenceView.getPolygonPrefList().getSelectedIndex() >= 0
						|| preferenceView.getColorPrefList().getSelectedIndex() >= 0) ) {
			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(false) )
				return;

			if ( ! HIRuntime.getGui().displayUserYesNoDialog(Messages.getString("PreferenceManager.5"), Messages.getString("PreferenceManager.6")+"\n\n"+Messages.getString("PreferenceManager.8")) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				return;

			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				if ( preferenceView.getPolygonPrefList().getSelectedIndex() >= 0 ) 
					HIRuntime.getManager().removeProjectPolygon(
							HIRuntime.getManager().getProjectPolygons().get(
									preferenceView.getPolygonPrefList().getSelectedIndex()
							)
					);
				else if ( preferenceView.getColorPrefList().getSelectedIndex() >= 0 )
					HIRuntime.getManager().removeProjectColor(
							HIRuntime.getManager().getProjectColors().get(
									preferenceView.getColorPrefList().getSelectedIndex()
							)
					);
				// update GUI
				preferenceView.rebuildPreferenceLists(HIRuntime.getManager().getProjectPolygons(), HIRuntime.getManager().getProjectColors());
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, this);
				HIRuntime.getGui().stopIndicatingServiceActivity();
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}
	}


	// --------------------------------------------------------------------------------------------------


	public void mouseClicked(MouseEvent e) {
		if ( e.getClickCount() == 2 
				&& e.getButton() == MouseEvent.BUTTON1
				&& preferenceView.getColorPrefList().getSelectedIndex() >= 0 )
			editColorAction();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
}
