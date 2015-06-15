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
 * Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.HIRichTextFieldControl;
import org.hyperimage.client.gui.HISimpleTextFieldControl;
import org.hyperimage.client.gui.HITemplateSeperatorControl;
import org.hyperimage.client.gui.HITextFieldControl;
import org.hyperimage.client.gui.ResetButton;
import org.hyperimage.client.gui.SaveButton;
import org.hyperimage.client.gui.TagsButton;
import org.hyperimage.client.gui.dialogs.HIBaseTagsEditorDialog;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiKeyValue;
import org.hyperimage.client.ws.HiObject;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class FlexMetadataEditorView extends GUIView implements ActionListener {

	private static final long serialVersionUID = -7426122593174332781L;


	private GridBagConstraints gridBagConstraints = new GridBagConstraints();

	private List<HiFlexMetadataTemplate> templates;
	private HiBase base;
	private List<HiFlexMetadataRecord> metadata;
	private String defLang;

	private HashMap <String, HITextFieldControl> modelToView;
	private HashMap <String, Vector<String>> metadataBuffer;
	private Vector<String> languageKeys;
	private int curLangIndex = 0;
	private int defLangIndex = 0;
	private boolean hasChanges = false;
	

	private JPanel controlPanel;
	private JPanel editorPanel;
	private JLabel idLabel;
	private JComboBox languageComboBox;
	private JLabel languageLabel;
	private JPanel metadataElementsPanel;
	private JPanel metadataPanel;
	private ResetButton resetButton;
	private SaveButton saveButton;
        private TagsButton tagsButton;

	private HashMap<String, HITemplateSeperatorControl> prefixStore = new HashMap<String, HITemplateSeperatorControl>();



	public FlexMetadataEditorView(List<HiFlexMetadataTemplate> templates, HiBase base, String defLang) {
		super(Messages.getString("FlexMetadataEditorView.0"), new Color(0x61, 0x89, 0xCA)); //$NON-NLS-1$

		this.templates = templates;
		
		this.base = base;
		this.metadata = MetadataHelper.resolveMetadataRecords(base);
		this.defLang = defLang;
		this.languageKeys = new Vector<String>();

		this.modelToView = new HashMap<String, HITextFieldControl>();
		this.metadataBuffer = new HashMap<String, Vector<String>>();

		initComponents();
		initFields();
		buildLanguages();
                
                // attach listeners
                tagsButton.addActionListener(this);
                tagsButton.setCount((int) (long) HIRuntime.getGui().getTagCountForElement(base.getId()));

		initBuffer();
		setMetadataFields();

		setDisplayPanel(editorPanel);


	}

        @Override
	public void updateLanguage() {
		languageLabel.setText(Messages.getString("FlexMetadataEditorView.17")); //$NON-NLS-1$
		saveButton.setToolTipText(Messages.getString("FlexMetadataEditorView.19")); //$NON-NLS-1$
                resetButton.setToolTipText(Messages.getString("FlexMetadataEditorView.20")); //$NON-NLS-1$
                tagsButton.setToolTipText(Messages.getString("MetadataEditorControl.tagButtonTooltip"));

		setTitle(Messages.getString("FlexMetadataEditorView.0")); //$NON-NLS-1$
		if ( base instanceof HiObject ) {
                    idLabel.setText(Messages.getString("HIClientGUI.lastChanged")+": "+MetadataHelper.getFuzzyDate(base.getTimestamp()));
//                    if ( base.getUUID() == null ) setIdLabel("O"+base.getId());
//                    else setIdLabel(base.getUUID());
                }
		buildLanguages();
		
		// DEBUG DRY
		// set display titles for known namespaces
		for (String prefix : prefixStore.keySet()) {
                    String displayPrefix = processDisplayFields(prefix);
                    prefixStore.get(prefix).updateTitle(displayPrefix);
                }
		
		for ( HiFlexMetadataTemplate template : templates ) {
			if (template.getNamespacePrefix().compareTo("HIBase") != 0) {  //$NON-NLS-1$
				// DEBUG - make this pretty
				for ( HiFlexMetadataSet fmSet : template.getEntries() ) {
					HITextFieldControl textControl;
					textControl = modelToView.get(template.getNamespacePrefix()+"."+fmSet.getTagname());  //$NON-NLS-1$
					if ( textControl != null )
						textControl.updateTitle(MetadataHelper.getTemplateKeyDisplayName(template, fmSet.getTagname(), 
							HIRuntime.getGUILanguage().getLanguage()));
				}
			}
		}
	}
	
	
	public void updateContent() {
		resetChanges();
	}

	public void setIdLabel(String id) {
		idLabel.setText(Messages.getString("FlexMetadataEditorView.2")+" "+id); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public JButton getSaveButton() {
		return saveButton;
	}

	public JButton getResetButton() {
		return resetButton;
	}
	
	public void updateMetadataLanguages() {
		buildLanguages();
		initBuffer();
		curLangIndex = languageComboBox.getSelectedIndex();
		setMetadataFields();
	}
	
	public void syncChanges() {
		for ( String lang : languageKeys ) {
			HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(metadata, lang);
			Vector<String> values = metadataBuffer.get(lang);
			
			int count = 0;
			if ( record != null )
				for ( HiKeyValue kvPair : record.getContents() ) {
					kvPair.setValue(values.get(count));
					count = count + 1;
				}
		}
		hasChanges = false;
	}

	public void resetChanges() {
		initBuffer();
		setMetadataFields();
		hasChanges = false;
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

	private boolean entryModified() {
		for ( String lang : languageKeys ) {
			HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(metadata, lang);
			Vector<String> values = metadataBuffer.get(lang);
			
			int count = 0;
			if ( record != null )
				for ( HiKeyValue kvPair : record.getContents() ) {
					if ( kvPair.getValue().compareTo(values.get(count)) != 0 )
						return true;
					count = count + 1;
				}
		}
		
		return false;
	}
	
	private void syncToBuffer() {
		if ( metadata != null ) {
			// sync current entry to the metadata buffer
			HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(metadata, languageKeys.get(curLangIndex));

			Vector<String> values = metadataBuffer.get(languageKeys.get(curLangIndex));
			int count = 0;
			if ( record != null )
				for ( HiKeyValue kvPair : record.getContents() ) {
					values.setElementAt(modelToView.get(kvPair.getKey()).getText(), count);
					count = count + 1;
				}

		}
	}
	
	private void initBuffer() {
		metadataBuffer.clear();
		
		for ( String lang : languageKeys ) {
			HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(metadata, lang);
			Vector<String> values = new Vector<String>();
			
			if ( record != null ) {
				for ( HiKeyValue kvPair : record.getContents() )
					values.addElement(kvPair.getValue());
				metadataBuffer.put(lang, values);
			}
		}
		
	}

	private void setMetadataFields() {
		if ( metadata != null ) {
			HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(metadata, languageKeys.get(curLangIndex));

			int count = 0;
			Vector <String> values = metadataBuffer.get(record.getLanguage());
			for ( HiKeyValue kvPair : record.getContents() ) {
				HITextFieldControl textControl = modelToView.get(kvPair.getKey());
				if (values == null || values.get(count) == null) textControl.setText("");
                                else textControl.setText(values.get(count));
				count = count + 1;
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
				languageComboBox.addItem(new Locale(record.getLanguage()).getDisplayLanguage());
			}
			languageComboBox.setSelectedIndex(defLangIndex);
			curLangIndex = defLangIndex;
		} else languageComboBox.addItem(Messages.getString("FlexMetadataEditorView.4")); //$NON-NLS-1$
		languageComboBox.addActionListener(this);
	}
        
    private String processDisplayFields(String strPrefix) {
        String strDisplayPrefix = strPrefix;

        if (strPrefix.compareTo("HIInternal") == 0) {
            strDisplayPrefix = Messages.getString("FlexMetadataEditorView.8"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (strPrefix.compareTo("dc") == 0) {
            strDisplayPrefix = Messages.getString("FlexMetadataEditorView.10"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (strPrefix.compareTo("cdwalite") == 0) {
            strDisplayPrefix = Messages.getString("FlexMetadataEditorView.25"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (strPrefix.compareTo("vra4") == 0) {
            strDisplayPrefix = Messages.getString("FlexMetadataEditorView.VRA4"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (strPrefix.compareTo("vra4hdlbg") == 0) {
            strDisplayPrefix = Messages.getString("FlexMetadataEditorView.VRA4_Hdlbg"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (strPrefix.equalsIgnoreCase("custom")) {
            strDisplayPrefix = Messages.getString("FlexMetadataEditorView.26"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (strPrefix.compareTo("HIClassic") == 0) {
            strDisplayPrefix = Messages.getString("FlexMetadataEditorView.12"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        return strDisplayPrefix;
    }

	// TODO: clean up GUI set up
	private void initFields() {
		int mainCount = 0;
		HITextFieldControl textControl;
		
		prefixStore.clear();
		for ( HiFlexMetadataTemplate template : templates ) {
			Vector<HITextFieldControl> items = new Vector<HITextFieldControl>();

			if (template.getNamespacePrefix().compareTo("HIBase") != 0) {  //$NON-NLS-1$
				boolean collapsed = false;
				// auto hide system default template
				if ( template.getNamespacePrefix().compareTo("HIInternal") == 0 ) //$NON-NLS-1$
					collapsed = true;
				
				// DEBUG - make this pretty
				JPanel panel = new JPanel();
				panel.setLayout(new GridBagLayout());
				String prefix = template.getNamespacePrefix();                                
				// set display titles for known namespaces
                                prefix = processDisplayFields(prefix); // horrible, I know, but it will just have to do for now. ~HGK				
				prefixStore.put(template.getNamespacePrefix(), addTemplateSeperator(prefix, collapsed, mainCount, metadataElementsPanel, panel));
				mainCount = mainCount + 1;
				
				int count = 0;
				for ( HiFlexMetadataSet fmSet : template.getEntries() ) {
					if ( fmSet.isRichText() ) {
						textControl = addMultiLineField(
								MetadataHelper.getTemplateKeyDisplayName(template, fmSet.getTagname(), 
										HIRuntime.getGUILanguage().getLanguage()), 
								count, 
								panel);
					} else
						textControl = addSingleLineField(
								MetadataHelper.getTemplateKeyDisplayName(template, fmSet.getTagname(), 
										HIRuntime.getGUILanguage().getLanguage()), 
								count, 
								panel);

					modelToView.put(template.getNamespacePrefix()+"."+fmSet.getTagname(), textControl); //$NON-NLS-1$
					items.add(textControl);
					
					count = count + 1;
				}
				
				// DEBUG add filler
				// addFiller(count, panel);
				
				// DEBUG - add Panel
				gridBagConstraints.fill = GridBagConstraints.BOTH;
				gridBagConstraints.gridwidth = 1;
				gridBagConstraints.gridheight = 1;
				gridBagConstraints.gridx = 0;
				gridBagConstraints.gridy = mainCount;
				gridBagConstraints.weightx = 1.0d;
				gridBagConstraints.weighty = 1.0d;
				metadataElementsPanel.add(panel, gridBagConstraints);
				mainCount = mainCount + 1;

			}
			
		}
	
		// DEBUG - add filler
		addFiller(mainCount, metadataElementsPanel);

		
		if ( base instanceof HiObject ) {
//                    if ( base.getUUID() == null ) setIdLabel("O"+base.getId()); //$NON-NLS-1$
//                    else setIdLabel(base.getUUID());
                    idLabel.setText(Messages.getString("HIClientGUI.lastChanged")+": "+MetadataHelper.getFuzzyDate(base.getTimestamp()));
                } 
	}

	private void initComponents() {

		editorPanel = new JPanel();
		metadataPanel = new JPanel();
		languageLabel = new JLabel();
		languageComboBox = new JComboBox();
		metadataElementsPanel = new JPanel();
		controlPanel = new JPanel();
		idLabel = new JLabel();
		resetButton = new ResetButton();
		saveButton = new SaveButton();
                tagsButton = new TagsButton();

		JScrollPane metadataElementsScroll = new JScrollPane(metadataElementsPanel);
		metadataElementsScroll.setPreferredSize(new Dimension(200,300));

		metadataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				Messages.getString("FlexMetadataEditorView.15"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue)); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$

		GroupLayout metadataPanelLayout = new GroupLayout(metadataPanel);
		metadataPanel.setLayout(metadataPanelLayout);
		metadataPanelLayout.setHorizontalGroup(
				metadataPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(GroupLayout.TRAILING, metadataPanelLayout.createSequentialGroup()
						.addContainerGap(192, Short.MAX_VALUE)
						.add(languageLabel)
						.addPreferredGap(LayoutStyle.RELATED)
						.add(languageComboBox, GroupLayout.PREFERRED_SIZE, 187, GroupLayout.PREFERRED_SIZE))
						.add(metadataElementsScroll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		metadataPanelLayout.setVerticalGroup(
				metadataPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(metadataPanelLayout.createSequentialGroup()
						.add(metadataPanelLayout.createParallelGroup(GroupLayout.BASELINE)
								.add(languageComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.add(languageLabel))
								.addPreferredGap(LayoutStyle.RELATED)
								.add(metadataElementsScroll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);

		idLabel.setText(Messages.getString("FlexMetadataEditorView.18")); //$NON-NLS-1$


        GroupLayout controlPanelLayout = new GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(controlPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(controlPanelLayout.createSequentialGroup()
                .add(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(idLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(tagsButton, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE))
        );
        controlPanelLayout.setVerticalGroup(controlPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(controlPanelLayout.createSequentialGroup()
                .add(0, 0, Short.MAX_VALUE)
                .add(controlPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.TRAILING, controlPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                        .add(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(idLabel, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
                    .add(GroupLayout.TRAILING, tagsButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0))
        );


		GroupLayout editorPanelLayout = new GroupLayout(editorPanel);
		editorPanel.setLayout(editorPanelLayout);
		editorPanelLayout.setHorizontalGroup(
				editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(GroupLayout.TRAILING, editorPanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(editorPanelLayout.createParallelGroup(GroupLayout.TRAILING)
								.add(GroupLayout.LEADING, metadataPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.add(GroupLayout.LEADING, controlPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap())
		);
		editorPanelLayout.setVerticalGroup(
				editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(GroupLayout.TRAILING, editorPanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(metadataPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.RELATED, 6, 6)
						.add(controlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap())
		);

		// -----
		
		metadataElementsPanel.setLayout(new GridBagLayout());
		languageComboBox.addActionListener(this);
                if (System.getProperty("HI.feature.tagsDisabled") != null) {
                    tagsButton.setVisible(false);
                }

                updateLanguage();
	}

	private void addFiller(int index, JPanel panel) {
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = index;
		gridBagConstraints.weightx = 1.0d;
		gridBagConstraints.weighty = 0.0000000001d;
		panel.add(new JPanel(), gridBagConstraints);
	}
	private HITextFieldControl addSingleLineField(String title, int index, JPanel panel) {
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 1.0d;
		gridBagConstraints.weighty = 0.0d;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = index;

		HISimpleTextFieldControl slPanel = new HISimpleTextFieldControl(title);
		panel.add(slPanel,gridBagConstraints);
		return slPanel;
	}

	private HITextFieldControl addMultiLineField(String title, int index, JPanel panel) {
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = index;
		gridBagConstraints.weightx = 1.0d;
		gridBagConstraints.weighty = 1.0d;

		HIRichTextFieldControl mlPanel = new HIRichTextFieldControl(title);
		panel.add(mlPanel,gridBagConstraints);	

		return mlPanel;
	}
	
	private HITemplateSeperatorControl addTemplateSeperator(String title, boolean collapsed, int index, JPanel panel, JPanel itemsPanel) {
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 1.0d;
		gridBagConstraints.weighty = 0.0d;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = index;

		HITemplateSeperatorControl control = new HITemplateSeperatorControl(title, itemsPanel, collapsed);
		panel.add(control, gridBagConstraints);
		
		return control;
	}


	// ----------------------------------------------

        @Override
	public void actionPerformed(ActionEvent e) {
            if ( e.getSource() == tagsButton ) {
                tagsButton.setCount(new HIBaseTagsEditorDialog(HIRuntime.getGui(), base.getId()).chooseTags());
            } else {
		if ( metadata != null ) if ( languageKeys.size() > 0 ) {

			// trigger change state update
			hasChanges();

			// save changes to buffer
			syncToBuffer();

			curLangIndex = languageComboBox.getSelectedIndex();
			setMetadataFields();
		}		
            }
	}

}
