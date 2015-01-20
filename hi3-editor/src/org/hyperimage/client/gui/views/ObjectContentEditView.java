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

package org.hyperimage.client.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.InscriptionViewerControl;
import org.hyperimage.client.gui.LayerViewerControl;
import org.hyperimage.client.util.LoadableImage;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiObjectContent;
import org.hyperimage.client.ws.HiView;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import com.sun.media.jai.widget.DisplayJAI;
import java.net.URLConnection;
import javax.swing.JScrollPane;
import org.hyperimage.client.util.MetadataHelper;

/**
 * @author Jens-Martin Loebel
 */
public class ObjectContentEditView extends GUIView implements LoadableImage {

	private static final long serialVersionUID = -2851582974143718095L;

	
	HiObjectContent content = null;

	private JButton editLayersButton;
    private JLabel layerCountLabel;
    private JPanel objectContentsPreviewPanel;
    private JPanel previewPanel;
    private JPanel contentsPanel;
    private JTabbedPane contentsTabPane;
    private JPanel controlPanel;
    private JPanel statusPanel;
    private JPanel editPanel;
    private JProgressBar loadingIndicator;
    
    private PlanarImage noPreview;
    private DisplayJAI viewPanel;
    private InscriptionViewerControl inscriptionViewer;
    private LayerViewerControl layerViewer = null;
    private String defLang;
    
    private GenericMetadataEditorView metadataEditor;
    
    
	public ObjectContentEditView(String defLang) {
		super(new Color(0x61, 0x89, 0xCA));
		this.defLang = defLang;
		
		initComponents();
		updateLanguage();
		
		setDisplayPanel(objectContentsPreviewPanel);
	}
	
	
	public void updateLanguage() {
        layerCountLabel.setText(Messages.getString("ObjectContentEditView.0")); //$NON-NLS-1$
	updateStatusBar();
	if (content == null || content instanceof HiView) setTitle(Messages.getString("ObjectContentEditView.1")); //$NON-NLS-1$
        else setTitle(Messages.getString("ObjectContentEditView.inscriptiondetails"));
        contentsTabPane.addTab(Messages.getString("ObjectContentEditView.2"), previewPanel); //$NON-NLS-1$
        contentsTabPane.addTab(Messages.getString("ObjectContentEditView.3"), new JScrollPane(editPanel)); //$NON-NLS-1$
        editLayersButton.setToolTipText(Messages.getString("ObjectContentEditView.4"));		 //$NON-NLS-1$
        if ( metadataEditor != null ) metadataEditor.updateLanguage();
        
        layerViewer.updateLanguage();
	}
	
	public void updateContent() {
		resetChanges();
		if ( content instanceof HiView ) {
			layerViewer.initLayers(((HiView)content).getLayers(), ((HiView)content).getSortOrder());
			previewPanel.repaint();
		}
		this.repaint();
		updateStatusBar();
	}

	
	public void setContentView(HiObjectContent content) {
		this.content = content;
		setView();
	}
	
	public void updateMetadataLanguages() {
		metadataEditor.updateMetadata();
	}
	
	public JPanel getDisplayPanel() {
		return previewPanel;
	}
	
	public LayerViewerControl getLayerViewer() {
		return layerViewer;
	}
	
	public JButton getLayerEditorButton() {
		return editLayersButton;
	}
	
	public boolean hasChanges() {
		if ( metadataEditor != null )
			return metadataEditor.hasChanges();
		return false;
	}
	
	public void syncChanges() {
		if ( metadataEditor != null )
			metadataEditor.syncChanges();
	}

	public void resetChanges() {
		if ( metadataEditor != null ) {
			metadataEditor.resetChanges();
			editPanel.doLayout();
			editPanel.repaint();
		}
	}
	
	public void updateStatusBar() {
		// update status bar
		if ( content instanceof HiView ) {
			String id="V"+content.getId(); //$NON-NLS-1$
                        if ( content.getUUID() != null ) id = content.getUUID();
			String count = Messages.getString("ObjectContentEditView.6"); //$NON-NLS-1$
			String layerString = Messages.getString("ObjectContentEditView.7"); //$NON-NLS-1$
			if ( ((HiView)content).getLayers().size() == 1 ) {
				layerString=Messages.getString("ObjectContentEditView.8"); //$NON-NLS-1$
				count = Messages.getString("ObjectContentEditView.9"); //$NON-NLS-1$
			} else if ( ((HiView)content).getLayers().size() > 1 )
				count = Integer.toString( ((HiView)content).getLayers().size() );
			String mimetype = URLConnection.guessContentTypeFromName(((HiView)content).getFilename());
			if ( mimetype.startsWith("image/") )  //$NON-NLS-1$
				layerCountLabel.setText("<html>"
                                        +Messages.getString("ObjectContentEditView.viewLastChanged")+":&nbsp;"
                                        +MetadataHelper.getFuzzyDate(content.getTimestamp())+"<br>"
                                        +((HiView)content).getWidth()+"*"+((HiView)content).getHeight()+" px - "+count + " "+layerString+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			else
				layerCountLabel.setText("<html>"
                                        +Messages.getString("ObjectContentEditView.viewLastChanged")+":&nbsp;"
                                        +MetadataHelper.getFuzzyDate(content.getTimestamp())+"<br>"
                                        +Messages.getString("ObjectContentEditView.18")+" ("+mimetype+")</html>");		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			
			if ( ! needsPreview() )
				loadingIndicator.setVisible(false);
		} else if ( content instanceof HiInscription ){
                    layerCountLabel.setText(Messages.getString("ObjectContentEditView.inscriptionLastChanged")+": "+MetadataHelper.getFuzzyDate(content.getTimestamp()));
                }
		if ( content == null ) layerCountLabel.setText(Messages.getString("ObjectContentEditView.25")); //$NON-NLS-1$
	}

	private void setView() {
            if (content == null || content instanceof HiView) setTitle(Messages.getString("ObjectContentEditView.1")); //$NON-NLS-1$
            else setTitle(Messages.getString("ObjectContentEditView.inscriptiondetails"));
            
            if ( this.content == null ) {
			setNoPreview();
			return;
		} else {
			previewPanel.removeAll();
			if ( content instanceof HiView ) {
				// update status bar
				loadingIndicator.setVisible(true);
				layerCountLabel.setText(Messages.getString("ObjectContentEditView.21")); //$NON-NLS-1$
				editLayersButton.setEnabled(true);
				layerViewer.setPreviewLoading();

				layerViewer.initLayers(((HiView)content).getLayers(), ((HiView)content).getSortOrder());
				previewPanel.add(layerViewer);

				metadataEditor = new GenericMetadataEditorView((HiView)content, defLang);
			} else {
				inscriptionViewer.setInscription((HiInscription)content);
				previewPanel.add(inscriptionViewer);
				metadataEditor = new GenericMetadataEditorView((HiInscription)content, defLang);
				// update status bar
				loadingIndicator.setVisible(false);
                                String id = "I"+content.getId();
                                if ( content.getUUID() != null ) id = content.getUUID();
			//	layerCountLabel.setText("ID: "+id+" - "+Messages.getString("ObjectContentEditView.24")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                layerCountLabel.setText(Messages.getString("ObjectContentEditView.inscriptionLastChanged")+" "
                                    +MetadataHelper.getFuzzyDate(content.getTimestamp()));
				editLayersButton.setEnabled(false);
			}
		}
		editPanel.removeAll();
		editPanel.add(metadataEditor.getMetadataPanel());
		editPanel.doLayout();
		editPanel.repaint();

		previewPanel.doLayout();
		previewPanel.repaint();
	}
	
	private void setNoPreview() {
		editPanel.removeAll();
		editPanel.doLayout();
		editPanel.repaint();
		previewPanel.removeAll();
        viewPanel.set(noPreview);
        previewPanel.add(viewPanel);
        layerCountLabel.setText(Messages.getString("ObjectContentEditView.25")); //$NON-NLS-1$
        loadingIndicator.setVisible(false);
	}
	
	private void initComponents() {

        objectContentsPreviewPanel = new JPanel();
        previewPanel = new JPanel();
        editLayersButton = new JButton();
        layerCountLabel = new JLabel();
        contentsPanel = new JPanel();
        contentsTabPane = new JTabbedPane();
        editPanel = new JPanel();
        controlPanel = new JPanel();
        statusPanel = new JPanel();
        loadingIndicator = new JProgressBar();

        
        GroupLayout contentsPanelLayout = new GroupLayout(contentsPanel);
        contentsPanel.setLayout(contentsPanelLayout);
        contentsPanelLayout.setHorizontalGroup(
            contentsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(0, 394, Short.MAX_VALUE)
            .add(contentsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(GroupLayout.TRAILING, contentsTabPane, GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE))
        );
        contentsPanelLayout.setVerticalGroup(
            contentsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(0, 252, Short.MAX_VALUE)
            .add(contentsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(contentsTabPane, GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE))
        );

        editLayersButton.setActionCommand("editLayers"); //$NON-NLS-1$
        editLayersButton.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        editLayersButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/layerEditor.png"))); //$NON-NLS-1$
        editLayersButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/layerEditor-active.png"))); //$NON-NLS-1$
        editLayersButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/layerEditor-disabled.png"))); //$NON-NLS-1$
        editLayersButton.setPreferredSize(new Dimension(24, 24));

        statusPanel.setLayout(new BorderLayout(5, 0));

        statusPanel.add(layerCountLabel, BorderLayout.CENTER);
        statusPanel.add(loadingIndicator, BorderLayout.EAST);

        GroupLayout controlPanelLayout = new GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(controlPanelLayout.createSequentialGroup()
                .add(editLayersButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(statusPanel, GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, statusPanel, GroupLayout.PREFERRED_SIZE, 24, Short.MAX_VALUE)
            .add(GroupLayout.TRAILING, editLayersButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        GroupLayout objectContentsPreviewPanelLayout = new GroupLayout(objectContentsPreviewPanel);
        objectContentsPreviewPanel.setLayout(objectContentsPreviewPanelLayout);
        objectContentsPreviewPanelLayout.setHorizontalGroup(
            objectContentsPreviewPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, objectContentsPreviewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(objectContentsPreviewPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, contentsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, controlPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        objectContentsPreviewPanelLayout.setVerticalGroup(
            objectContentsPreviewPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, objectContentsPreviewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(contentsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(controlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        
        
        // -----
        
        
        loadingIndicator.setIndeterminate(true);
        previewPanel.setLayout(new BorderLayout());
        editPanel.setLayout(new BorderLayout());

        // no view panel (empty)
        viewPanel = new DisplayJAI();
        noPreview  = 
            JAI.create("url", getClass().getResource("/resources/hyperimage-nopreview.png")); //$NON-NLS-1$ //$NON-NLS-2$
        // inscription panel
        inscriptionViewer = new InscriptionViewerControl(defLang);
        // view panel (layer viewer)
	layerViewer = new LayerViewerControl();

        setNoPreview();        
    }

	
	public boolean needsPreview() {
		return layerViewer.needsPreview();
	}

	public void setPreviewImage(PlanarImage image) {
		layerViewer.setPreviewImage(image);
		
		updateStatusBar();
		loadingIndicator.setVisible(false);

	}


}
