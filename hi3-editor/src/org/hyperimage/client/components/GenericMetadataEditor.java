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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.views.GenericMetadataEditorView;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiLightTable;
import org.hyperimage.client.ws.HiText;
import org.hyperimage.client.ws.HiView;
import org.hyperimage.client.ws.Hiurl;

/**
 * @author Jens-Martin Loebel
 */
public class GenericMetadataEditor extends HIComponent implements ActionListener {

	protected GenericMetadataEditorView editorView;
	private HiBase base;

	
	public GenericMetadataEditor(HiText text) {
		super(Messages.getString("GenericMetadataEditor.0")+" ("+
                        (text.getUUID() == null ? "T"+text.getId() : text.getUUID())
                        +")", Messages.getString("GenericMetadataEditor.3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		this.base = text;
		editorView = new GenericMetadataEditorView(text, HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
		
		initEditor();
	}

	public GenericMetadataEditor(HiInscription inscription) {
		super(Messages.getString("GenericMetadataEditor.4")+" ("+
                        (inscription.getUUID() == null ? "I"+inscription.getId() : inscription.getUUID())
                        +")", Messages.getString("GenericMetadataEditor.7")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		this.base = inscription;
		editorView = new GenericMetadataEditorView(inscription, HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
		
		initEditor();
	}
	
	public GenericMetadataEditor(Hiurl url) {
		super(Messages.getString("GenericMetadataEditor.8")+" ("+
                        (url.getUUID() == null ? "U"+url.getId() : url.getUUID())
                        +")", Messages.getString("GenericMetadataEditor.11")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		this.base = url;
		editorView = new GenericMetadataEditorView(url, HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
		
		initEditor();
	}
	
	public GenericMetadataEditor(HiView view) {
		super(Messages.getString("GenericMetadataEditor.12")+" ("+
                        (view.getUUID() == null ? "V"+view.getId() : view.getUUID())
                        +")", Messages.getString("GenericMetadataEditor.15")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		this.base = view;
		editorView = new GenericMetadataEditorView(view, HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
		
		initEditor();
	}

	public void updateLanguage() {
		super.updateLanguage();
		if ( this.base instanceof HiText ) setTitle(Messages.getString("GenericMetadataEditor.0")+" (T"+base.getId()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if ( this.base instanceof HiInscription ) setTitle(Messages.getString("GenericMetadataEditor.4")+" (I"+base.getId()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if ( this.base instanceof Hiurl ) setTitle(Messages.getString("GenericMetadataEditor.8")+" (U"+base.getId()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if ( this.base instanceof HiView ) setTitle(Messages.getString("GenericMetadataEditor.12")+" (V"+base.getId()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		getWindowMenuItem().setText(getTitle());
		HIRuntime.getGui().updateComponentTitle(this);
	}
	
	
	public void receiveMessage(HIMessageTypes message, HiBase base ) {
		if ( message == HIMessageTypes.ENTITY_CHANGED ) 
			if ( this.base.getId() == base.getId() )
				editorView.resetChanges();
		
		if ( message == HIMessageTypes.LANGUAGE_ADDED || message == HIMessageTypes.LANGUAGE_REMOVED ) {	
			if ( this.base == null ) return;
			if ( this.base instanceof Hiurl || this.base instanceof HiLightTable ) return;
			// reload all base metadata
			HIRuntime.getGui().startIndicatingServiceActivity();
			try {
				List<HiFlexMetadataRecord> records = HIRuntime.getManager().getFlexMetadataRecords(this.base);
				while ( this.base.getMetadata().size() > 0 ) this.base.getMetadata().remove(0);
				for ( HiFlexMetadataRecord record : records )
					this.base.getMetadata().add(record);
                                this.base.setTimestamp(new Date().getTime());
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
			HIRuntime.getGui().stopIndicatingServiceActivity();

			// update GUI
			editorView.updateMetadata();
		}

	}	
	
	
	private void initEditor() {
		// attach listeners
		editorView.getSaveButton().addActionListener(this);
		editorView.getResetButton().addActionListener(this);

		// register views
		this.views.add(editorView);
	}

	
	
	public HiBase getBaseElement() {
		return this.base;
	}

	/**
	 * respond to GUI close request, prompt user to save metadata or cancel operation
	 */
	public boolean requestClose() {
		return askToSaveOrCancelChanges();
	}
	
	private void saveMetadataChanges() {
		if ( editorView.hasChanges() && HIRuntime.getGui().checkEditAbility(false) ) {
			editorView.syncChanges();
			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				if ( ! (base instanceof Hiurl) ) 
					HIRuntime.getManager().updateFlexMetadataRecords(MetadataHelper.resolveMetadataRecords(base));
				else
					HIRuntime.getManager().updateURL((Hiurl)base);

                                base.setTimestamp(new Date().getTime());
				HIRuntime.getGui().stopIndicatingServiceActivity();

				// propagate changes				
                                editorView.updateContent();
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, base, this);
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
			}
		}
	}
	
	private boolean askToSaveOrCancelChanges() {
		if ( editorView.hasChanges() && HIRuntime.getGui().checkEditAbility(true) ) {
			int decision = HIRuntime.getGui().displayUserChoiceDialog(
					Messages.getString("GenericMetadataEditor.16"), Messages.getString("GenericMetadataEditor.17")); //$NON-NLS-1$ //$NON-NLS-2$
			
			if ( decision == JOptionPane.CANCEL_OPTION ) 
				return false;
			else if ( decision == JOptionPane.YES_OPTION )
				saveMetadataChanges();
		}
		return true;
	}


	//	-------------------------------------------

	public void actionPerformed(ActionEvent e) {

		if ( e.getActionCommand().equalsIgnoreCase("save") ) { //$NON-NLS-1$
			if ( editorView.hasChanges() && HIRuntime.getGui().checkEditAbility(false) )
				saveMetadataChanges();
		}

		if ( e.getActionCommand().equalsIgnoreCase("reset") ) //$NON-NLS-1$
			editorView.resetChanges();

	}

}
