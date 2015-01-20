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

package org.hyperimage.client.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.hyperimage.client.Messages;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class MetadataEditorControl extends JPanel implements ActionListener {

	private static final long serialVersionUID = 5224350099908280006L;

	
    private HIRichTextFieldControl annotationRichTextControl;
    private JComboBox languageComboBox;
    private JLabel languageLabel;
    private JTextField sourceField;
    private JPanel sourcePanel;
    private JEnrichedTextField titleField;
    private JPanel titlePanel;
    private JPanel metadataPanel;
    private TitledBorder metadataBorder = null;
    private TitledBorder titleBorder = null;
    private TitledBorder sourceBorder = null;
    
    private JPanel controlPanel;
    private ResetButton resetButton;
    private SaveButton saveButton;
    private JLabel idLabel;
    
    private String titleKey, sourceKey, contentKey;
    private String titleName, sourceName, contentName;
    private boolean titleEnabled = true;
    private boolean sourceEnabled = true;
    private List<HiFlexMetadataRecord> metadata;
    private String defLang;
    private Vector<String> languageKeys;
    
    private Vector<String> titleMetadata = new Vector<String>();
    private Vector<String> sourceMetadata = new Vector<String>();
    private Vector<String> contentMetadata = new Vector<String>();
    private int curLangIndex = 0;
    private int defLangIndex = 0;
    
    private boolean hasChanges = false;

    
	public MetadataEditorControl(String titleKey, String titleName, String sourceKey, String sourceName, String contentKey, String contentName) {
		this.titleKey = titleKey;
		this.titleName = titleName;
		this.sourceKey = sourceKey;
		this.sourceName = sourceName;
		this.contentKey = contentKey;
		this.contentName = contentName;
		languageKeys = new Vector<String>();
		
		initComponents();
		
		if ( titleKey == null )
			titleEnabled = false;
		titlePanel.setVisible(titleEnabled);

		if ( sourceKey == null )
			sourceEnabled = false;
		sourcePanel.setVisible(sourceEnabled);
		
	}
	
	
	public void updateLanguage(String titleName, String sourceName, String contentName ) {
		metadataBorder.setTitle(Messages.getString("MetadataEditorControl.16")); //$NON-NLS-1$
		metadataPanel.repaint();
        languageLabel.setText(Messages.getString("MetadataEditorControl.18")); //$NON-NLS-1$

        if ( titleEnabled ) titleBorder.setTitle(titleName);
        if ( sourceEnabled ) sourceBorder.setTitle(sourceName);
        annotationRichTextControl.updateTitle(contentName);
        annotationRichTextControl.updateLanguage();
		saveButton.setToolTipText(Messages.getString("MetadataEditorControl.1")); //$NON-NLS-1$
        resetButton.setToolTipText(Messages.getString("MetadataEditorControl.2"));         //$NON-NLS-1$
		buildLanguages();
	}
	
	public void setMetadata(List<HiFlexMetadataRecord> metadata, String defLang) {
		this.metadata = metadata;
		this.defLang = defLang;
		buildLanguages();
		
		// buffer model metadata
		titleMetadata.removeAllElements();
		sourceMetadata.removeAllElements();
		contentMetadata.removeAllElements();
		// DEBUG
		for ( String lang : languageKeys ) {
			if ( titleEnabled )
				titleMetadata.addElement(MetadataHelper.findValue("HIBase", titleKey, MetadataHelper.getDefaultMetadataRecord(metadata, lang))); //$NON-NLS-1$
			if ( sourceEnabled )
				sourceMetadata.addElement(MetadataHelper.findValue("HIBase", sourceKey, MetadataHelper.getDefaultMetadataRecord(metadata, lang))); //$NON-NLS-1$
			contentMetadata.addElement(MetadataHelper.findValue("HIBase", contentKey, MetadataHelper.getDefaultMetadataRecord(metadata, lang))); //$NON-NLS-1$
		}

		// set fields to project default language
		if ( metadata != null ) {
			setEditable(true);
			setMetadataFields();
		} else
			setEditable(false);
		hasChanges = false;
	}
	
	public void setIdLabel(String id) {
            idLabel.setText("ID: "+id); //$NON-NLS-1$
	}
        
        public void setTimeLabel(String time) {
            idLabel.setText(Messages.getString("HIClientGUI.lastChanged")+": "+time);
        }
	
	public void setHeading(String heading) {
		TitledBorder border = (TitledBorder) metadataPanel.getBorder();
		border.setTitle(heading);
	}

	public void setMultiLanguage(boolean multiLanguage) {
		languageComboBox.setVisible(multiLanguage);
		languageLabel.setVisible(multiLanguage);
	}
	
	public List<HiFlexMetadataRecord> getMetadata() {
		return metadata;
	}
	
	public void setEmptyText(String emptyText) {
		titleField.setEmptyText(emptyText);
	}
	
	public boolean hasChanges() {
		if ( metadata == null )
			return false;

		// check changes
		syncToBuffer();
		if ( !hasChanges )
			if ( entryModified() )
				hasChanges = true;

		return hasChanges;
	}
	
	public void syncChanges() {
		if ( metadata != null ) {

			// sync changes back to model
			if ( hasChanges() ) {
				for ( int i=0; i<languageKeys.size(); i++ ) {
					if ( titleEnabled )
						MetadataHelper.setValue(
								"HIBase",  //$NON-NLS-1$
								titleKey, 
								titleMetadata.get(i),
								MetadataHelper.getDefaultMetadataRecord(metadata, languageKeys.get(i)) );

					if ( sourceEnabled )
						MetadataHelper.setValue(
								"HIBase",  //$NON-NLS-1$
								sourceKey, 
								sourceMetadata.get(i),
								MetadataHelper.getDefaultMetadataRecord(metadata, languageKeys.get(i)) );

					MetadataHelper.setValue(
							"HIBase",  //$NON-NLS-1$
							contentKey, 
							contentMetadata.get(i),
							MetadataHelper.getDefaultMetadataRecord(metadata, languageKeys.get(i)) );
				}				
				hasChanges = false;
			}

		}
	}
	
	public void resetChanges() {
		if ( metadata != null ) {
			for ( int i=0; i<languageKeys.size(); i++ ) {
				if ( titleEnabled )
					titleMetadata.setElementAt(
							MetadataHelper.findValue(
									"HIBase",  //$NON-NLS-1$
									titleKey, 
									MetadataHelper.getDefaultMetadataRecord(metadata, languageKeys.get(i))
							)  ,i );

				if ( sourceEnabled )
					sourceMetadata.setElementAt(
							MetadataHelper.findValue(
									"HIBase",  //$NON-NLS-1$
									sourceKey, 
									MetadataHelper.getDefaultMetadataRecord(metadata, languageKeys.get(i))
							)  ,i );

				contentMetadata.setElementAt(
						MetadataHelper.findValue(
								"HIBase",  //$NON-NLS-1$
								contentKey, 
								MetadataHelper.getDefaultMetadataRecord(metadata, languageKeys.get(i))
						)  ,i );
			}
			hasChanges = false;

			setMetadataFields();
		}
	}
	

	public JButton getSaveButton() {
		return this.saveButton;
	}

    public JButton getResetButton() {
        return this.resetButton;
    }

    public JPanel getMetadataPanel() {
        return metadataPanel;
    }

    private void setMetadataFields() {
        if (metadata != null) {
            if (metadata.size() > 0) {
                if (titleEnabled) {
                    titleField.setText(titleMetadata.elementAt(curLangIndex));
                }
                if (sourceEnabled) {
                    sourceField.setText(sourceMetadata.elementAt(curLangIndex));
                }
                annotationRichTextControl.setText(contentMetadata.elementAt(curLangIndex));
            }
        }
    }

	private void buildLanguages() {
		languageComboBox.removeActionListener(this);
		languageKeys.removeAllElements();
		languageComboBox.removeAllItems();

		if ( metadata != null ) {
			for ( HiFlexMetadataRecord record : metadata ) {
				languageKeys.addElement(record.getLanguage());
				if ( record.getLanguage().compareTo(defLang) == 0 )
					defLangIndex = languageKeys.size()-1;
				languageComboBox.addItem(MetadataHelper.langToLocale(record.getLanguage()).getDisplayLanguage());
			}
			if ( languageComboBox.getModel().getSize() > defLangIndex ) 
				languageComboBox.setSelectedIndex(defLangIndex);
			curLangIndex = defLangIndex;
		} else languageComboBox.addItem(Messages.getString("MetadataEditorControl.10")); //$NON-NLS-1$
		languageComboBox.addActionListener(this);
	}
	
	private void setEditable(boolean editable) {
		titleField.setEnabled(editable);
		titleField.setEditable(editable);
		sourceField.setEnabled(editable);
		sourceField.setEditable(editable);
		annotationRichTextControl.setEditable(editable);
		saveButton.setEnabled(editable);
		resetButton.setEnabled(editable);
		if ( !editable ) {
			titleField.setText(""); //$NON-NLS-1$
			sourceField.setText(""); //$NON-NLS-1$
		}
	}
	
	private boolean entryModified() {

		if ( metadata != null )
			for ( int i=0; i<languageKeys.size(); i++ ) {
				// compare title fields with model if enabled
				if ( titleEnabled ) if ( titleMetadata.get(i).compareTo(
						MetadataHelper.findValue("HIBase", titleKey,  //$NON-NLS-1$
								MetadataHelper.getDefaultMetadataRecord(
										metadata, 
										languageKeys.get(i)))
				) != 0 ) return true;

				// compare source field with model if enabled
				if ( sourceEnabled ) if ( sourceMetadata.get(i).compareTo(
						MetadataHelper.findValue("HIBase", sourceKey,  //$NON-NLS-1$
								MetadataHelper.getDefaultMetadataRecord(
										metadata, 
										languageKeys.get(i)))
								) != 0 )
					return true;

				// compare content field with model
				if ( contentMetadata.get(i).compareTo(
						MetadataHelper.findValue("HIBase", contentKey,  //$NON-NLS-1$
								MetadataHelper.getDefaultMetadataRecord(
										metadata, 
										languageKeys.get(i)))
				) != 0 ) return true;

			}

		return false;
	}
	
	
	private void syncToBuffer() {
		if ( metadata != null ) {
			// sync current entry to the metadata buffer
			if ( titleEnabled )
				titleMetadata.setElementAt(titleField.getText(), curLangIndex);
			if ( sourceEnabled )
				sourceMetadata.setElementAt(sourceField.getText(), curLangIndex);
			contentMetadata.setElementAt(annotationRichTextControl.getText(), curLangIndex);
		}
	}
	
    private void initComponents() {
        metadataPanel = new JPanel();
        titlePanel = new JPanel();
        titleField = new JEnrichedTextField();
        sourcePanel = new JPanel();
        sourceField = new JTextField();
        annotationRichTextControl = new HIRichTextFieldControl(contentName);
        languageLabel = new JLabel();
        languageComboBox = new JComboBox();
        controlPanel = new JPanel();
        idLabel = new JLabel();
        resetButton = new ResetButton();
        saveButton = new SaveButton();

        metadataBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("MetadataEditorControl.16"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue);  //$NON-NLS-1$ //$NON-NLS-2$
        metadataPanel.setBorder(metadataBorder);

        titleBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), titleName);
        titlePanel.setBorder(titleBorder);

        GroupLayout titlePanelLayout = new GroupLayout(titlePanel);
        titlePanel.setLayout(titlePanelLayout);
        titlePanelLayout.setHorizontalGroup(
                titlePanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(titlePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(titleField, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                        .addContainerGap())
        );
        titlePanelLayout.setVerticalGroup(
                titlePanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(titlePanelLayout.createSequentialGroup()
                        .add(titleField, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sourceBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), sourceName);
        sourcePanel.setBorder(sourceBorder);

        GroupLayout sourcePanelLayout = new GroupLayout(sourcePanel);
        sourcePanel.setLayout(sourcePanelLayout);
        sourcePanelLayout.setHorizontalGroup(
                sourcePanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(sourcePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(sourceField, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                        .addContainerGap())
        );
        sourcePanelLayout.setVerticalGroup(
                sourcePanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(sourceField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        languageLabel.setText(Messages.getString("MetadataEditorControl.18")); //$NON-NLS-1$

        GroupLayout metadataPanelLayout = new GroupLayout(metadataPanel);
        metadataPanel.setLayout(metadataPanelLayout);
        metadataPanelLayout.setHorizontalGroup(
                metadataPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(titlePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(GroupLayout.TRAILING, metadataPanelLayout.createSequentialGroup()
                        .addContainerGap(90, Short.MAX_VALUE)
                        .add(languageLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(languageComboBox, GroupLayout.PREFERRED_SIZE, 187, GroupLayout.PREFERRED_SIZE))
                .add(sourcePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(GroupLayout.TRAILING, annotationRichTextControl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        metadataPanelLayout.setVerticalGroup(
                metadataPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(metadataPanelLayout.createSequentialGroup()
                        .add(metadataPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                                .add(languageComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .add(languageLabel))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(titlePanel, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(sourcePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(annotationRichTextControl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        idLabel.setText("ID:"); //$NON-NLS-1$

        GroupLayout controlPanelLayout = new GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
                controlPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(controlPanelLayout.createSequentialGroup()
                        .add(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.UNRELATED)
                        .add(idLabel, GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                        .addContainerGap())
        );
        controlPanelLayout.setVerticalGroup(
                controlPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(controlPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                        .add(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(idLabel, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(controlPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(GroupLayout.TRAILING, metadataPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(metadataPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(controlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        // -----
        languageComboBox.addActionListener(this);
    }


	
	// ---------------------------------------------------------------
	
	public void actionPerformed(ActionEvent e) {
		if ( metadata != null ) if ( languageKeys.size() > 0 ) {
			
			// trigger change state update
			hasChanges();
			
			// save changes to buffer
			syncToBuffer();

			curLangIndex = languageComboBox.getSelectedIndex();
			
			// DEBUG
			setMetadataFields();
		}
	}

}
