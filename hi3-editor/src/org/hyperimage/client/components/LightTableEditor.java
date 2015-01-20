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


package org.hyperimage.client.components;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.HIClientGUI;
import org.hyperimage.client.gui.views.LightTablePreViewerView;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiLightTable;

/**
 * @author Jens-Martin Loebel
 */
public class LightTableEditor extends HIComponent implements ActionListener {

	private HiLightTable lightTable;
	
	LightTablePreViewerView xmlView;
	boolean isValid = false;
	
	
	public LightTableEditor(HiLightTable lightTable) {
		super(Messages.getString("LightTableEditor.0")+" ("+
                        (lightTable.getUUID() == null ? "X"+lightTable.getId() : lightTable.getUUID())+
                        ")", Messages.getString("LightTableEditor.2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		this.lightTable = lightTable;

                // init views
		xmlView = new LightTablePreViewerView(lightTable);
		
		
		// register views
		views.add(xmlView);
		
		// attach listeners
                xmlView.getPreViewerButton().addActionListener(this);
//		xmlView.getSaveButton().addActionListener(this);
//		xmlView.getResetButton().addActionListener(this);
		
		
	}
	
	public void updateLanguage() {
		super.updateLanguage();
		
		setTitle(Messages.getString("LightTableEditor.0")+" X"+lightTable.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		getWindowMenuItem().setText(getTitle());
		HIRuntime.getGui().updateComponentTitle(this);

	}
	
	public boolean requestClose() {
		return askToSaveOrCancelChanges();
	}
	
	public HiBase getBaseElement() {
		return this.lightTable;
	}

	private void saveChanges() {
		if ( xmlView.hasChanges() && HIRuntime.getGui().checkEditAbility(false) ) {
			xmlView.syncChanges();

			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().updateLightTable(lightTable);
				HIRuntime.getGui().stopIndicatingServiceActivity();
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, lightTable, this);
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}
	}
	
	private void resetChanges() {
		xmlView.resetChanges();
	}
	
	private boolean askToSaveOrCancelChanges() {
		if ( xmlView.hasChanges() && HIRuntime.getGui().checkEditAbility(true) ) {
			int decision = HIRuntime.getGui().displayUserChoiceDialog(
					Messages.getString("LightTableEditor.3"), Messages.getString("LightTableEditor.4")); //$NON-NLS-1$ //$NON-NLS-2$
			
			if ( decision == JOptionPane.CANCEL_OPTION ) 
				return false;
			else if ( decision == JOptionPane.YES_OPTION )
				saveChanges();
		}
		return true;
	}

	
    // --------------------------------------------------------------------------------------------


    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equalsIgnoreCase("open")) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(HIRuntime.getManager().getServerURL() + "previewer/?session=" + HIRuntime.getManager().getEndpointSession() + "#X" + lightTable.getId()));
                } catch (IOException | URISyntaxException ex) {
                    Logger.getLogger(HIClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            // close component
            HIRuntime.getGui().deregisterComponent(this, false);
        }
        
        if (e.getActionCommand().equalsIgnoreCase("save")) { //$NON-NLS-1$
            // check user role
            if (!HIRuntime.getGui().checkEditAbility(false)) return;
            if (xmlView.hasChanges()) saveChanges();
        }

        if (e.getActionCommand().equalsIgnoreCase("reset")) //$NON-NLS-1$
        {
            resetChanges();
        }
    }

}
