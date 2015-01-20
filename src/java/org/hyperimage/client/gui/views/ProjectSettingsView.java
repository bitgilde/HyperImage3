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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.components.ProjectSettings.LinkTransferHandler;
import org.hyperimage.client.gui.ResetButton;
import org.hyperimage.client.gui.SaveButton;
import org.hyperimage.client.gui.lists.GroupContentsCellRenderer;
import org.hyperimage.client.gui.lists.LanguageListCellRenderer;
import org.hyperimage.client.gui.lists.QuickInfoCell;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiLanguage;
import org.hyperimage.client.ws.HiProjectMetadata;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class ProjectSettingsView extends GUIView implements ActionListener {

    private static final long serialVersionUID = -8962637707426813845L;

    private JLabel availableLangLabel;
    private JList availableLangList;
    private JScrollPane avaliableLangScroll;
    private JComboBox defaultLangComboBox;
    private JLabel defaultLangLabel;
    private JPanel languagePanel;
    private JComboBox previewFieldComboBox;
    private JPanel previewFieldPanel;
    private JLabel projectLangLabel;
    private JList projectLangList;
    private JScrollPane projectLangScroll;
    private JPanel settingsPanel;
    private JPanel startElementPanel;
    private JList linkTargetList;
    private JLabel noLinkLabel;
    private ResetButton resetButton;
    private SaveButton saveButton;
    private JLabel titleLabel;
    private JTextField titleTextField;
    private JPanel metadataPanel;
    private JLabel languageLabel;
    private JComboBox languageComboBox;
    private JPanel quotaPanel;
    private JProgressBar quotaProgressBar;

    private Vector<String> titleMetadata = new Vector<String>();
    private int curLangIndex = 0;
    private int defLangIndex = 0;
    private Vector<String> languageKeys;
    private boolean hasChanges = false;

    private static final Border linkBorder = new LineBorder(Color.black, 1);

    private DefaultListModel availLangModel;
    private DefaultListModel projectLangModel;
    private DefaultComboBoxModel defaultLangModel;
    private DefaultComboBoxModel previewFieldComboBoxModel;


	public ProjectSettingsView() {
		super();

		languageKeys = new Vector<String>();
		initComponents();
		updateLanguage();

		setDisplayPanel(settingsPanel);

		// Drag and Drop
		availableLangList.setDragEnabled(true);
		availableLangList.setDropMode(DropMode.INSERT);
		projectLangList.setDragEnabled(true);
		projectLangList.setDropMode(DropMode.INSERT);
		linkTargetList.setDragEnabled(true);
		linkTargetList.setDropMode(DropMode.ON);


		fillLanguageLists();
		updateLanguageOptions();
		buildMetadataLanguages();
	}

    public String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i") + "B";
        String output = String.format("%.1f", bytes / Math.pow(unit, exp));
        if ( output.endsWith(".0") || output.endsWith(",0") ) output = output.substring(0, output.length()-2);
        return output+" "+pre;
    }
    
    public void updateQuotaInfo() {
                // update quota info
        if (HIRuntime.getManager().getProject().getQuota() == 0) {
            quotaProgressBar.setValue(0);
            quotaProgressBar.setString(Messages.getString("ProjectSettingsView.used")+": "+humanReadableByteCount(HIRuntime.getManager().getProject().getUsed(), false));
        } else {
            quotaProgressBar.setValue(
                Math.min(
                        100,
                        (int) Math.round(100.0*HIRuntime.getManager().getProject().getUsed()/HIRuntime.getManager().getProject().getQuota()
                )));
            quotaProgressBar.setString(humanReadableByteCount(HIRuntime.getManager().getProject().getUsed(), false)+" / "+humanReadableByteCount(HIRuntime.getManager().getProject().getQuota(), false));
        }
    }
    
    public String getSelectedPreviewField() {
        return (String) previewFieldComboBoxModel.getSelectedItem();
    }
    
    public JComboBox getPreviewFieldComboBox() {
        return previewFieldComboBox;
    }

    public void updateLanguage() {
        menuTitle.setText(Messages.getString("ProjectSettingsView.0")); //$NON-NLS-1$
        noLinkLabel.setText(
                "<html><center><b>" + //$NON-NLS-1$
                Messages.getString("ProjectSettingsView.2") + //$NON-NLS-1$
                "</b><br><br>" + //$NON-NLS-1$
                Messages.getString("ProjectSettingsView.4") + //$NON-NLS-1$
                "</center></html>");         //$NON-NLS-1$
        ((TitledBorder) metadataPanel.getBorder()).setTitle(Messages.getString("ProjectSettingsView.1")); //$NON-NLS-1$
        metadataPanel.repaint();

        String quotaTitle = Messages.getString("ProjectSettingsView.noquota");
        if ( HIRuntime.getManager().getProject().getQuota() > 0 ) quotaTitle = Messages.getString("ProjectSettingsView.quota");
        ((TitledBorder) quotaPanel.getBorder()).setTitle(quotaTitle); //$NON-NLS-1$

        ((TitledBorder) languagePanel.getBorder()).setTitle(Messages.getString("ProjectSettingsView.6")); //$NON-NLS-1$
        languagePanel.repaint();
        ((TitledBorder) startElementPanel.getBorder()).setTitle(Messages.getString("ProjectSettingsView.7")); //$NON-NLS-1$
        startElementPanel.repaint();
        ((TitledBorder) previewFieldPanel.getBorder()).setTitle(Messages.getString("ProjectSettingsView.8")); //$NON-NLS-1$
        previewFieldPanel.repaint();

        availableLangLabel.setText(Messages.getString("ProjectSettingsView.9")); //$NON-NLS-1$
        projectLangLabel.setText(Messages.getString("ProjectSettingsView.10")); //$NON-NLS-1$
        defaultLangLabel.setText(Messages.getString("ProjectSettingsView.11")); //$NON-NLS-1$
        languageLabel.setText(Messages.getString("MetadataEditorControl.18")); //$NON-NLS-1$

        titleLabel.setText(Messages.getString("ProjectSettingsView.3")); //$NON-NLS-1$
        saveButton.setToolTipText(Messages.getString("ProjectSettingsView.5")); //$NON-NLS-1$
        resetButton.setToolTipText(Messages.getString("ProjectSettingsView.12")); //$NON-NLS-1$

        updateQuotaInfo();
    }


	public boolean hasChanges() {
		// check changes
		syncToBuffer();
		if ( !hasChanges )
			if ( entryModified() )
				hasChanges = true;

		return hasChanges;
	}

	public void syncChanges() {
		// sync changes back to model
		if ( hasChanges() ) {
			for ( int i=0; i<languageKeys.size(); i++ )
				MetadataHelper.setValue(HIRuntime.getManager().getProject(), languageKeys.get(i), titleMetadata.get(i));
			hasChanges = false;
		}
	}

	public void resetChanges() {
		for ( int i=0; i<languageKeys.size(); i++ )
			titleMetadata.setElementAt(
					MetadataHelper.findValue(HIRuntime.getManager().getProject(), languageKeys.get(i)), i
			);
		hasChanges = false;

		setMetadataFields();
	}


	public JButton getSaveButton() {
		return this.saveButton;
	}

	public JButton getResetButton() {
		return this.resetButton;
	}




	public JList getStartElementList() {
		return linkTargetList;
	}


	public QuickInfoCell getElementLinkPreview() {
		if ( HIRuntime.getManager().getProject().getStartObjectInfo() == null ) return null;

		GroupContentsCellRenderer renderer = (GroupContentsCellRenderer) linkTargetList.getCellRenderer();
		return renderer.getCellForContent(HIRuntime.getManager().getProject().getStartObjectInfo());
	}

	public void updateStartElementLink() {
		DefaultListModel model = (DefaultListModel) linkTargetList.getModel();
		model.removeAllElements();
		GroupContentsCellRenderer renderer = (GroupContentsCellRenderer) linkTargetList.getCellRenderer();
		renderer.clearCache();

		if ( HIRuntime.getManager().getProject().getStartObjectInfo() != null ) {
			model.addElement(HIRuntime.getManager().getProject().getStartObjectInfo());
			if ( getElementLinkPreview() != null ) getElementLinkPreview().setBorder(new EmptyBorder(0,0,0,0));
			linkTargetList.setVisible(true);
			noLinkLabel.setVisible(false);
		} else {
			linkTargetList.setVisible(false);
			noLinkLabel.setVisible(true);
		}

		linkTargetList.repaint();
	}

	public void setLinkTransferHandler(LinkTransferHandler linkTransferHandler) {
		linkTargetList.setTransferHandler(linkTransferHandler);
		noLinkLabel.setTransferHandler(linkTransferHandler);
	}


	public JComboBox getDefaultLangComboBox() {
		return defaultLangComboBox;
	}

	public JList getAvailableLangList() {
		return availableLangList;
	}

	public JList getProjectLangList() {
		return projectLangList;
	}

	public void updateLanguageOptions() {
		defaultLangModel.removeAllElements();

		for ( int i=0 ; i < projectLangModel.size(); i++)
			defaultLangModel.addElement(projectLangModel.get(i));

		defaultLangComboBox.setSelectedItem(MetadataHelper.langToLocale(HIRuntime.getManager().getProject().getDefaultLanguage()));

		buildMetadataLanguages();
	}


	private void setMetadataFields() {
		titleTextField.setText(titleMetadata.elementAt(curLangIndex));
	}

	private boolean entryModified() {
		for ( int i=0; i<languageKeys.size(); i++ )
			// compare title fields with model
			if ( titleMetadata.get(i).compareTo(
					MetadataHelper.findValue(HIRuntime.getManager().getProject(), languageKeys.get(i))
			) != 0 ) return true;

		return false;
	}


	private void syncToBuffer() {
		// sync current entry to the metadata buffer
		titleMetadata.setElementAt(titleTextField.getText(), curLangIndex);
	}



	private void buildMetadataLanguages() {
		languageComboBox.removeActionListener(this);
		languageKeys.removeAllElements();
		languageComboBox.removeAllItems();

		for ( HiProjectMetadata record : HIRuntime.getManager().getProject().getMetadata() ) {
			languageKeys.addElement(record.getLanguageID());
			if ( record.getLanguageID().compareTo(HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()) == 0 )
				defLangIndex = languageKeys.size()-1;
			languageComboBox.addItem(MetadataHelper.langToLocale(record.getLanguageID()).getDisplayLanguage());
		}
		if ( languageComboBox.getModel().getSize() > defLangIndex ) 
			languageComboBox.setSelectedIndex(defLangIndex);
		curLangIndex = defLangIndex;
		languageComboBox.addActionListener(this);
		
		// buffer model metadata
		titleMetadata.removeAllElements();
		// DEBUG
		for ( String lang : languageKeys )
			titleMetadata.addElement(
					MetadataHelper.findValue(HIRuntime.getManager().getProject(), lang));
		setMetadataFields();
		this.hasChanges = false;
	}




	// DEBUG
	private void fillLanguageLists() {
		availLangModel.removeAllElements();
		projectLangModel.removeAllElements();
		// sort languages, main langs first, variants second
		for ( Locale lang : Locale.getAvailableLocales() )
			if ( lang.getVariant().length() == 0 )
				if ( lang.getCountry().length() <= 0 || lang.getCountry().equalsIgnoreCase(lang.getLanguage()) )
					if ( !availLangModel.contains(new Locale(lang.getLanguage())) ) 
						availLangModel.addElement(new Locale(lang.getLanguage()));

		// DEBUG disable language variants for now
		/*
		for ( Locale lang : Locale.getAvailableLocales() )
			if ( lang.getVariant().length() == 0 )
				if ( lang.getCountry().length() > 0 && !lang.getCountry().equalsIgnoreCase(lang.getLanguage()) )
					availLangModel.addElement(lang);
		 */

		// remove project languages from list and add them to project languages list
		for ( HiLanguage hiLang : HIRuntime.getManager().getProject().getLanguages() ) {
			int removeIndex = availLangModel.indexOf(MetadataHelper.langToLocale(hiLang));
			if ( removeIndex >= 0 ) availLangModel.remove(removeIndex);
			projectLangModel.addElement(MetadataHelper.langToLocale(hiLang));
		}
	}

    private void initComponents() {

        noLinkLabel = new JLabel();
        noLinkLabel.setSize(136, 171);
        noLinkLabel.setPreferredSize(new Dimension(134, 169));
        noLinkLabel.setBackground(Color.white);
        noLinkLabel.setOpaque(true);
        noLinkLabel.setBorder(linkBorder);

        // -----
        settingsPanel = new JPanel();
        quotaPanel = new JPanel();
        quotaProgressBar = new JProgressBar();
        metadataPanel = new JPanel();
        languageLabel = new JLabel();
        languageComboBox = new JComboBox();
        titleTextField = new JTextField();
        titleLabel = new JLabel();
        saveButton = new SaveButton();
        resetButton = new ResetButton();
        languagePanel = new JPanel();
        availableLangLabel = new JLabel();
        projectLangLabel = new JLabel();
        avaliableLangScroll = new JScrollPane();
        availableLangList = new JList();
        projectLangScroll = new JScrollPane();
        projectLangList = new JList();
        defaultLangLabel = new JLabel();
        defaultLangComboBox = new JComboBox();
        startElementPanel = new JPanel();
        previewFieldPanel = new JPanel();
        previewFieldComboBox = new JComboBox();

        quotaPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.blue));
        quotaProgressBar.setMaximum(100);
        quotaProgressBar.setStringPainted(true);

        GroupLayout quotaPanelLayout = new GroupLayout(quotaPanel);
        quotaPanel.setLayout(quotaPanelLayout);
        quotaPanelLayout.setHorizontalGroup(
            quotaPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(quotaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(quotaProgressBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        quotaPanelLayout.setVerticalGroup(
            quotaPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(quotaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(quotaProgressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        metadataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue)); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
        languageLabel.setText(Messages.getString("MetadataEditorControl.18")); //$NON-NLS-1$

        GroupLayout metadataPanelLayout = new GroupLayout(metadataPanel);
        metadataPanel.setLayout(metadataPanelLayout);

       metadataPanelLayout.setHorizontalGroup(
            metadataPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(metadataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(metadataPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(metadataPanelLayout.createSequentialGroup()
                        .add(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .add(GroupLayout.LEADING, metadataPanelLayout.createSequentialGroup()
                        .add(titleLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(languageLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(languageComboBox, GroupLayout.PREFERRED_SIZE, 187, GroupLayout.PREFERRED_SIZE))
                    .add(titleTextField))
                .addContainerGap())
        );
        metadataPanelLayout.setVerticalGroup(
            metadataPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(metadataPanelLayout.createSequentialGroup()
                .add(metadataPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(metadataPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                        .add(languageComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(languageLabel))
                    .add(metadataPanelLayout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(titleLabel)))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(titleTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(metadataPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        languagePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue)); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$

        avaliableLangScroll.setViewportView(availableLangList);

        projectLangScroll.setViewportView(projectLangList);

		GroupLayout languagePanelLayout = new GroupLayout(languagePanel);
        languagePanel.setLayout(languagePanelLayout);
        languagePanelLayout.setHorizontalGroup(
            languagePanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, languagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(languagePanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(avaliableLangScroll, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .add(availableLangLabel, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                .add(30, 30, 30)
                .add(languagePanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, defaultLangComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, defaultLangLabel, GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, projectLangScroll, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, projectLangLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        languagePanelLayout.setVerticalGroup(
            languagePanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(languagePanelLayout.createSequentialGroup()
                .add(languagePanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(availableLangLabel)
                    .add(projectLangLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(languagePanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(languagePanelLayout.createSequentialGroup()
                        .add(projectLangScroll)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(defaultLangLabel, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(defaultLangComboBox, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
                    .add(avaliableLangScroll))
                .addContainerGap())
        );

        startElementPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue)); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
        startElementPanel.setPreferredSize(new Dimension(150, 210));

        previewFieldPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue)); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
        GroupLayout previewFieldPanelLayout = new GroupLayout(previewFieldPanel);
        previewFieldPanel.setLayout(previewFieldPanelLayout);
        previewFieldPanelLayout.setHorizontalGroup(
                previewFieldPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(previewFieldComboBox, 0, 144, Short.MAX_VALUE)
        );
        previewFieldPanelLayout.setVerticalGroup(
                previewFieldPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(previewFieldPanelLayout.createSequentialGroup()
                        .add(previewFieldComboBox, GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                        .addContainerGap())
        );

        GroupLayout settingsPanelLayout = new GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(settingsPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(metadataPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(settingsPanelLayout.createSequentialGroup()
                        .add(settingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                            .add(previewFieldPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(settingsPanelLayout.createSequentialGroup()
                                .add(startElementPanel, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, Short.MAX_VALUE))
                            .add(quotaPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.UNRELATED)
                        .add(languagePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(settingsPanelLayout.createSequentialGroup()
                .add(8, 8, 8)
                .add(metadataPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(settingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(settingsPanelLayout.createSequentialGroup()
                        .add(startElementPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.UNRELATED)
                        .add(previewFieldPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(quotaPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(0, 67, Short.MAX_VALUE))
                    .add(settingsPanelLayout.createSequentialGroup()
                        .add(languagePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

	// -----
        
        // init language lists model
        linkTargetList = new JList();
        availLangModel = new DefaultListModel();
        availableLangList.setModel(availLangModel);
        availableLangList.setCellRenderer(new LanguageListCellRenderer());
        availableLangList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectLangModel = new DefaultListModel();
        projectLangList.setModel(projectLangModel);
        projectLangList.setCellRenderer(new LanguageListCellRenderer());
        projectLangList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        defaultLangModel = new DefaultComboBoxModel();
        defaultLangComboBox.setModel(defaultLangModel);
        defaultLangComboBox.setRenderer(new LanguageListCellRenderer());

        // set up link target list display
        startElementPanel.add(linkTargetList);
        startElementPanel.add(noLinkLabel);
        GroupContentsCellRenderer renderer = new GroupContentsCellRenderer();
        renderer.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkTargetList.setCellRenderer(renderer);
        linkTargetList.setModel(new DefaultListModel());
        linkTargetList.setPreferredSize(new Dimension(132, 166));
        linkTargetList.setBackground(startElementPanel.getBackground());
        linkTargetList.setBorder(linkBorder);
        linkTargetList.setVisible(false);
        previewFieldComboBoxModel = new DefaultComboBoxModel();
        previewFieldComboBox.setModel(previewFieldComboBoxModel);
        
        
        // DEBUG
        // set up title preview field
        // TODO react to template changes (add / delete)
        // TODO display user friendly i18n field names
        for ( HiFlexMetadataTemplate template : HIRuntime.getManager().getProject().getTemplates() ) {
            if ( template.getNamespacePrefix().compareTo("HIBase") != 0 ) for ( HiFlexMetadataSet entry : template.getEntries() ) {
                if ( entry.isRichText() == false ) previewFieldComboBoxModel.addElement(template.getNamespacePrefix()+"."+entry.getTagname());
            }
        }
        int index = -1;
        if ( MetadataHelper.findPreference(HIRuntime.getManager().getProject(), "admin.preview.objectTitleField") != null ) 
            index  = previewFieldComboBoxModel.getIndexOf(MetadataHelper.findPreference(HIRuntime.getManager().getProject(), "admin.preview.objectTitleField").getValue());
        previewFieldComboBox.setSelectedIndex(index);
    }


    // -----------------------------------------------------------------------------------------------------


	public void actionPerformed(ActionEvent e) {
		if ( languageKeys.size() > 0 ) {

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
