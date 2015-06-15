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

/*
 * Copyright 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
 * All rights reserved. Use is subject to license terms.
 * http://bitgilde.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For further information on HyperImage visit http://hyperimage.ws/
 */

package org.hyperimage.client.gui.views;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.MetadataEditorControl;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiKeyValue;
import org.hyperimage.client.ws.HiText;
import org.hyperimage.client.ws.HiView;
import org.hyperimage.client.ws.Hiurl;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Jens-Martin Loebel
 */
public class GenericMetadataEditorView extends GUIView {

	private static final long serialVersionUID = -9211467123067610757L;

	
	protected JPanel editorPanel;
	protected MetadataEditorControl metadataEditorControl;
	private HiBase base;
	
	public GenericMetadataEditorView(HiText text, String defLang) {
		super(Messages.getString("GenericMetadataEditorView.0"), new Color(0x61, 0x89, 0xCA)); //$NON-NLS-1$
        
		base = text;
		metadataEditorControl = new MetadataEditorControl("title", Messages.getString("GenericMetadataEditorView.2"), null, null, "content", Messages.getString("GenericMetadataEditorView.4"));		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		metadataEditorControl.setHeading(Messages.getString("GenericMetadataEditorView.5")); //$NON-NLS-1$
		metadataEditorControl.setMetadata(text.getMetadata(), defLang);
		if ( text.getUUID() == null ) metadataEditorControl.setIdLabel("T"+text.getId()); //$NON-NLS-1$
                else metadataEditorControl.setIdLabel(text.getUUID());
                metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(text.getTimestamp()));
		
		initView();
	}

	public GenericMetadataEditorView(HiInscription inscription, String defLang) {
		super(Messages.getString("GenericMetadataEditorView.7")); //$NON-NLS-1$
        
		base = inscription;
		metadataEditorControl = new MetadataEditorControl(null, null, null, null, "content", Messages.getString("GenericMetadataEditorView.9"));		 //$NON-NLS-1$ //$NON-NLS-2$
		metadataEditorControl.setHeading(Messages.getString("GenericMetadataEditorView.10")); //$NON-NLS-1$
		metadataEditorControl.setMetadata(inscription.getMetadata(), defLang);
		if ( inscription.getUUID() == null ) metadataEditorControl.setIdLabel("I"+inscription.getId()); //$NON-NLS-1$
                else metadataEditorControl.setIdLabel(inscription.getUUID());
                metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(inscription.getTimestamp()));
		
		initView();
	}

	public GenericMetadataEditorView(Hiurl url, String defLang) {
		super(Messages.getString("GenericMetadataEditorView.12")); //$NON-NLS-1$
        
		base = url;
		metadataEditorControl = new MetadataEditorControl("url", Messages.getString("GenericMetadataEditorView.14"), "title", Messages.getString("GenericMetadataEditorView.16"), "lastaccess", Messages.getString("GenericMetadataEditorView.18"));		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		metadataEditorControl.setHeading(Messages.getString("GenericMetadataEditorView.19")); //$NON-NLS-1$
		metadataEditorControl.setMultiLanguage(false);
		// create dummy record for URLs
		HiFlexMetadataRecord record = new HiFlexMetadataRecord();
		record.setLanguage(defLang);
		HiKeyValue kvUrl = new HiKeyValue();
		HiKeyValue kvTitle = new HiKeyValue();
		HiKeyValue kvLastAccess = new HiKeyValue();
		kvUrl.setKey("HIBase.url"); //$NON-NLS-1$
		if ( url.getUrl() != null )
			kvUrl.setValue(url.getUrl());
		else kvUrl.setValue(""); //$NON-NLS-1$

		kvTitle.setKey("HIBase.title"); //$NON-NLS-1$
		if ( url.getTitle() != null )
			kvTitle.setValue(url.getTitle());
		else kvTitle.setValue(""); //$NON-NLS-1$

		kvLastAccess.setKey("HIBase.lastaccess"); //$NON-NLS-1$
		if ( url.getLastAccess() != null )
			kvLastAccess.setValue(url.getLastAccess());
		else kvLastAccess.setValue(""); //$NON-NLS-1$

		record.getContents().add(kvUrl);
		record.getContents().add(kvTitle);
		record.getContents().add(kvLastAccess);
		ArrayList<HiFlexMetadataRecord> records = new ArrayList<HiFlexMetadataRecord>();
		records.add(record);
		metadataEditorControl.setMetadata(records, defLang);
		if ( url.getUUID() == null ) metadataEditorControl.setIdLabel("U"+url.getId()); //$NON-NLS-1$
                else metadataEditorControl.setIdLabel(url.getUUID());
                metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(url.getTimestamp()));
		
		initView();
	}
	
	public GenericMetadataEditorView(HiView view, String defLang) {
		super(Messages.getString("GenericMetadataEditorView.27")); //$NON-NLS-1$
        
		base = view;
		metadataEditorControl = new MetadataEditorControl("title", Messages.getString("GenericMetadataEditorView.29"), "source", Messages.getString("GenericMetadataEditorView.31"), "comment", Messages.getString("GenericMetadataEditorView.33"));		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		metadataEditorControl.setMetadata(view.getMetadata(), defLang);
		if ( view.getUUID() == null ) metadataEditorControl.setIdLabel("V"+view.getId()); //$NON-NLS-1$
                else metadataEditorControl.setIdLabel(view.getUUID());
                metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(view.getTimestamp()));
		
		initView();
	}

    public void updateContent() {
        metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(base.getTimestamp()));
    }
        
    public void updateLanguage() {
        if (base instanceof HiText) {
            metadataEditorControl.updateLanguage(Messages.getString("GenericMetadataEditorView.2"), null, Messages.getString("GenericMetadataEditorView.4")); //$NON-NLS-1$ //$NON-NLS-2$
            metadataEditorControl.setHeading(Messages.getString("GenericMetadataEditorView.5")); //$NON-NLS-1$
            setTitle(Messages.getString("GenericMetadataEditorView.0")); //$NON-NLS-1$
        }
        if (base instanceof HiInscription) {
            metadataEditorControl.updateLanguage(null, null, Messages.getString("GenericMetadataEditorView.9")); //$NON-NLS-1$ 
            metadataEditorControl.setHeading(Messages.getString("GenericMetadataEditorView.10")); //$NON-NLS-1$
            setTitle(Messages.getString("GenericMetadataEditorView.7")); //$NON-NLS-1$
        }
        if (base instanceof Hiurl) {
            metadataEditorControl.updateLanguage(Messages.getString("GenericMetadataEditorView.14"), Messages.getString("GenericMetadataEditorView.16"), Messages.getString("GenericMetadataEditorView.18")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            metadataEditorControl.setHeading(Messages.getString("GenericMetadataEditorView.19")); //$NON-NLS-1$
            setTitle(Messages.getString("GenericMetadataEditorView.12")); //$NON-NLS-1$
        }
        if (base instanceof HiView) {
            metadataEditorControl.updateLanguage(Messages.getString("GenericMetadataEditorView.29"), Messages.getString("GenericMetadataEditorView.31"), Messages.getString("GenericMetadataEditorView.33")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            setTitle(Messages.getString("GenericMetadataEditorView.27")); //$NON-NLS-1$
        }
        
        metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(base.getTimestamp()));        
    }


    private void initView() {
        initComponents();
        setDisplayPanel(editorPanel);

        // set up tag button
        metadataEditorControl.setBaseID(base.getId());
        metadataEditorControl.setTagCount(HIRuntime.getGui().getTagCountForElement(base.getId()));
    }
	
	public JButton getSaveButton() {
		return metadataEditorControl.getSaveButton();
	}

	public JButton getResetButton() {
		return metadataEditorControl.getResetButton();
	}
	
    public void syncChanges() {
        metadataEditorControl.syncChanges();
        if (base instanceof Hiurl) {
            // sync changes back to url from dummy metadata record
            Hiurl url = (Hiurl) base;
            url.setUrl(MetadataHelper.findValue("HIBase", "url", metadataEditorControl.getMetadata().get(0))); //$NON-NLS-1$ //$NON-NLS-2$
            url.setTitle(MetadataHelper.findValue("HIBase", "title", metadataEditorControl.getMetadata().get(0))); //$NON-NLS-1$ //$NON-NLS-2$
            url.setLastAccess(MetadataHelper.findValue("HIBase", "lastaccess", metadataEditorControl.getMetadata().get(0))); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void resetChanges() {
        metadataEditorControl.resetChanges();
    }

    public boolean hasChanges() {
        return metadataEditorControl.hasChanges();
    }

    public JPanel getMetadataPanel() {
        return metadataEditorControl.getMetadataPanel();
    }

    public void updateMetadata() {
        metadataEditorControl.setMetadata(base.getMetadata(), HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
        updateContent();
    }
	
	protected void initComponents() {
        editorPanel = new JPanel();

        GroupLayout editorPanelLayout = new GroupLayout(editorPanel);
        editorPanel.setLayout(editorPanelLayout);
        editorPanelLayout.setHorizontalGroup(
            editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(editorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(metadataEditorControl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        editorPanelLayout.setVerticalGroup(
            editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(editorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(metadataEditorControl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }

}
