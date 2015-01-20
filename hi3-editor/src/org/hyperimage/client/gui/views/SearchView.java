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

package org.hyperimage.client.gui.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiLanguage;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class SearchView extends GUIView implements DocumentListener {

	private static final long serialVersionUID = 1955083116920356788L;


	private JComboBox fieldComboBox;
	private JPanel fieldSearchPanel;
	private JTextField fieldSearchTextField;
	private JComboBox languageComboBox;
	private JLabel languageLabel;
	private JLabel notImplementedLabel;
	private JButton searchButton;
	private JPanel searchFieldPanel;
	private JPanel searchPanel;
	private TitledBorder searchFieldBorder;
	private TitledBorder fieldSearchBorder;
	private JTextField searchTextField;
	private int defLangIndex = 0;
	private String defLang;
	private Vector<String> languageKeys;
	private Vector<String> fieldKeys;
	private List<HiLanguage> languages;


	public SearchView(List<HiLanguage> languages, HiLanguage defLanguage) {
		super(Messages.getString("SearchView.0")); //$NON-NLS-1$

		initComponents();

		setDisplayPanel(searchPanel);
		this.defLang = defLanguage.getLanguageId();
		this.languages = languages;
		languageKeys = new Vector<String>();
		fieldKeys = new Vector<String>();
		buildLanguages();
		buildFields();
		
		// attach listeners
		searchTextField.getDocument().addDocumentListener(this);
		fieldSearchTextField.getDocument().addDocumentListener(this);
	}
	
	public void updateLanguage() {
		searchFieldBorder.setTitle(Messages.getString("SearchView.22")); //$NON-NLS-1$
		fieldSearchBorder.setTitle(Messages.getString("SearchView.28")); //$NON-NLS-1$
		languageLabel.setText(Messages.getString("SearchView.26")); //$NON-NLS-1$
		notImplementedLabel.setText(Messages.getString("SearchView.27")); //$NON-NLS-1$
		
		buildLanguages();
		buildFields();
		super.setTitle(Messages.getString("SearchView.0")); //$NON-NLS-1$
		

	}
	

	public String getSearchText() {
		return searchTextField.getText();
	}
	
	public String getFieldSearchText() {
		return fieldSearchTextField.getText();
	}
	
	public String getSearchField() {
		return fieldKeys.get(fieldComboBox.getSelectedIndex());
	}
	
	public JComboBox getFieldComboBox() {
		return fieldComboBox;
	}

	public String getSelectedLanguage() {
		if ( languageComboBox.getSelectedIndex() < (languageKeys.size()-1) )
			return languageKeys.get(languageComboBox.getSelectedIndex());
		else return "all"; //$NON-NLS-1$
	}
	
	public void setSearchEnabled(boolean enabled) {
		searchTextField.setEnabled(enabled);
		searchTextField.setEditable(enabled);
		fieldSearchTextField.setEnabled(enabled);
		fieldSearchTextField.setEditable(enabled);
		searchButton.setEnabled(enabled);
	}

	public void attachListeners(ActionListener listener) {
		searchTextField.addActionListener(listener);
		fieldSearchTextField.addActionListener(listener);
		searchButton.addActionListener(listener);
	}

	public void rebuildLanguages(List<HiLanguage> languages) {
		this.languages = languages;
		buildLanguages();
	}

	private void buildLanguages() {
		// build language index
		languageKeys.removeAllElements();
		for ( HiLanguage lang : languages )
			languageKeys.addElement(lang.getLanguageId());
		languageKeys.addElement("all"); //$NON-NLS-1$

		languageComboBox.removeAllItems();

		int index = 0;
		for ( String lang: languageKeys ) {
			if ( lang.compareTo(defLang) == 0 )
				defLangIndex = index;
			languageComboBox.addItem(new Locale(lang).getDisplayLanguage());
			index = index + 1;
		}
		if ( languageComboBox.getModel().getSize() > defLangIndex ) 
			languageComboBox.setSelectedIndex(defLangIndex);
	}
	
	private void buildFields() {
		fieldKeys.removeAllElements();
		fieldComboBox.removeAllItems();

		// add id search
		fieldKeys.add("id"); //$NON-NLS-1$
		fieldComboBox.addItem(Messages.getString("SearchView.4")); //$NON-NLS-1$
		// add HIBase search
		fieldKeys.add("HIBase.title"); //$NON-NLS-1$
		fieldComboBox.addItem(Messages.getString("SearchView.6")); //$NON-NLS-1$
		fieldKeys.add("HIBase.content"); //$NON-NLS-1$
		fieldComboBox.addItem(Messages.getString("SearchView.8")); //$NON-NLS-1$
		fieldKeys.add("HIBase.source"); //$NON-NLS-1$
		fieldComboBox.addItem(Messages.getString("SearchView.10")); //$NON-NLS-1$
		fieldKeys.add("HIBase.comment"); //$NON-NLS-1$
		fieldComboBox.addItem(Messages.getString("SearchView.12")); //$NON-NLS-1$
		// add object templates
		
		for ( HiFlexMetadataTemplate template : HIRuntime.getManager().getProject().getTemplates() ) {
			if ( template.getNamespacePrefix().equalsIgnoreCase("HIBase") ) continue; // skip base template //$NON-NLS-1$
			
			String templateString = template.getNamespacePrefix();
			// DEBUG refactor
			// id common template names
			if ( templateString.equalsIgnoreCase("dc") ) templateString = Messages.getString("SearchView.15"); //$NON-NLS-1$ //$NON-NLS-2$
			if ( templateString.equalsIgnoreCase("HIClassic") ) templateString = Messages.getString("SearchView.17"); //$NON-NLS-1$ //$NON-NLS-2$
			if ( templateString.equalsIgnoreCase("HIInternal") ) templateString = Messages.getString("SearchView.19"); //$NON-NLS-1$ //$NON-NLS-2$
			
			for ( HiFlexMetadataSet set : template.getEntries() ) {
				fieldKeys.add(template.getNamespacePrefix()+"."+set.getTagname()); //$NON-NLS-1$
				String fieldTitle = MetadataHelper.getTemplateKeyDisplayName(template, set.getTagname(), HIRuntime.getGUILanguage().getLanguage());
				if ( fieldTitle == null || fieldTitle.length() == 0 ) fieldTitle = set.getTagname();
				fieldComboBox.addItem(templateString+": "+fieldTitle); //$NON-NLS-1$
			}
		}
	}

	private void initComponents() {

		searchPanel = new JPanel();
		searchFieldPanel = new JPanel();
		searchTextField = new JTextField();
		searchButton = new JButton();
		languageComboBox = new JComboBox();
		languageLabel = new JLabel();
        fieldSearchPanel = new JPanel();
        fieldComboBox = new JComboBox();
        fieldSearchTextField = new JTextField();
 		notImplementedLabel = new JLabel();

 		searchFieldBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("SearchView.22"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), java.awt.Color.blue);  // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
		searchFieldPanel.setBorder(searchFieldBorder);

		GroupLayout searchFieldPanelLayout = new GroupLayout(searchFieldPanel);
		searchFieldPanel.setLayout(searchFieldPanelLayout);
		searchFieldPanelLayout.setHorizontalGroup(
				searchFieldPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(GroupLayout.TRAILING, searchFieldPanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(searchTextField, GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
						.addContainerGap())
		);
		searchFieldPanelLayout.setVerticalGroup(
				searchFieldPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(searchTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		);

		languageComboBox.setModel(new DefaultComboBoxModel(new String[] { Messages.getString("SearchView.24"), Messages.getString("SearchView.25") })); //$NON-NLS-1$ //$NON-NLS-2$

		languageLabel.setText(Messages.getString("SearchView.26")); //$NON-NLS-1$


		notImplementedLabel.setText(Messages.getString("SearchView.27")); //$NON-NLS-1$

		fieldSearchBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("SearchView.28"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
		fieldSearchPanel.setBorder(fieldSearchBorder);
        GroupLayout fieldSearchPanelLayout = new GroupLayout(fieldSearchPanel);
        fieldSearchPanel.setLayout(fieldSearchPanelLayout);
        fieldSearchPanelLayout.setHorizontalGroup(
            fieldSearchPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(fieldSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fieldSearchPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.TRAILING, fieldSearchTextField, GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .add(GroupLayout.TRAILING, fieldComboBox, 0, 271, Short.MAX_VALUE))
                .addContainerGap())
        );
        fieldSearchPanelLayout.setVerticalGroup(
            fieldSearchPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(fieldSearchPanelLayout.createSequentialGroup()
                .add(fieldComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(fieldSearchTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        
        GroupLayout searchPanelLayout = new GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, notImplementedLabel, GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, fieldSearchPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(searchPanelLayout.createSequentialGroup()
                        .add(languageLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(languageComboBox, GroupLayout.PREFERRED_SIZE, 187, GroupLayout.PREFERRED_SIZE))
                    .add(searchFieldPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(searchButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(languageComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(languageLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(searchFieldPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(fieldSearchPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(notImplementedLabel)
                .addPreferredGap(LayoutStyle.RELATED, 44, Short.MAX_VALUE)
                .add(searchButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

		// -----

		searchButton.setPreferredSize(new Dimension(24, 24));
		searchButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		searchButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/find.png"))); //$NON-NLS-1$
		searchButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/find-active.png"))); //$NON-NLS-1$
		searchButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/find-disabled.png"))); //$NON-NLS-1$
		searchButton.setToolTipText(Messages.getString("SearchView.30")); //$NON-NLS-1$
		searchButton.setActionCommand("search"); //$NON-NLS-1$
		searchButton.setEnabled(false);

		searchTextField.setActionCommand("simpleSearch"); //$NON-NLS-1$
		fieldSearchTextField.setActionCommand("fieldSearch"); //$NON-NLS-1$

	}


	// ----------------------------------------------------------------------------------------------------


	private void updateSearchStatus() {
		if ( searchTextField.getText().length() > 0 || fieldSearchTextField.getText().length() >0 )
			searchButton.setEnabled(true);
		else 
			searchButton.setEnabled(false);		
	}


	@Override
	public void changedUpdate(DocumentEvent e) {
		updateSearchStatus();
	}


	@Override
	public void insertUpdate(DocumentEvent e) {
		updateSearchStatus();
	}


	@Override
	public void removeUpdate(DocumentEvent e) {
		updateSearchStatus();
	}




}
