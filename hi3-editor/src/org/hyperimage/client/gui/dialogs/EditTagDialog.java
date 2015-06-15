/*
 * Copyright 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hyperimage.client.gui.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.ResetButton;
import org.hyperimage.client.gui.SaveButton;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiProjectMetadata;

/**
 *
 * @author Jens-Martin Loebel <loebel@bitgilde.de>
 */
public class EditTagDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    private ResetButton cancelButton;
    private JPanel editTagPanel;
    private JPanel tagNamePanel;
    private JComboBox languageComboBox;
    private JLabel languageLabel;
    private SaveButton saveButton;
    private JTextField tagnameTextField;
    private TitledBorder tagNameBorder;
    
    private HiGroup tag = null;
    private ArrayList<String> languageKeys = new ArrayList<String>();
    private int curLangIndex = 0;
    private int defLangIndex = 0;
    private ArrayList<String> tagMetadata = new ArrayList<String>();
    private boolean saveSelected = false;

    
    public EditTagDialog(JFrame parent) {
        super(parent, true);

        initComponents();
        updateLanguage();
        buildMetadataLanguages();

        this.setBounds(
                (parent.getWidth() / 2) - (this.getWidth() / 2),
                (parent.getHeight() / 2) - (this.getHeight() / 2),
                this.getWidth(),
                this.getHeight());

        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);
    }
    
    public void setTag(HiGroup tag) {
        this.tag = tag;
        loadTagData();
    }
    
    private void buildMetadataLanguages() {
        languageComboBox.removeActionListener(this);
        languageKeys.clear();
        languageComboBox.removeAllItems();

        for (HiProjectMetadata record : HIRuntime.getManager().getProject().getMetadata()) {
            languageKeys.add(record.getLanguageID());
            if (record.getLanguageID().compareTo(HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()) == 0) {
                defLangIndex = languageKeys.size() - 1;
            }
            languageComboBox.addItem(MetadataHelper.langToLocale(record.getLanguageID()).getDisplayLanguage());
        }
        if (languageComboBox.getModel().getSize() > defLangIndex) {
            languageComboBox.setSelectedIndex(defLangIndex);
        }
        curLangIndex = defLangIndex;
        languageComboBox.addActionListener(this);

        // buffer model metadata
        loadTagData();
    }

    public void updateLanguage() {
        this.setTitle(Messages.getString("EditTagDialog.edittagname"));
        languageLabel.setText(Messages.getString("EditTagDialog.language"));
        tagNameBorder.setTitle(Messages.getString("EditTagDialog.tagname"));
        tagNamePanel.repaint();

        saveButton.setToolTipText(Messages.getString("EditTemplateFieldDialog.AENDERUNGEN_SPEICHERN"));
        cancelButton.setToolTipText(Messages.getString("EditTemplateFieldDialog.ABBRUCH"));
    }
    
    public boolean saveSelected() {
        return saveSelected;
    }
    
    private void loadTagData() {
        if (tag != null) {
            tagMetadata.clear();
            for ( String lang : languageKeys ) {
                for ( HiFlexMetadataRecord rec : tag.getMetadata() ) {
                    if ( rec.getLanguage().compareTo(lang) == 0 ) {
                        tagMetadata.add(MetadataHelper.findValue("HIBase", "title", rec));
                    }
                }
            }
            setTagNameField();
        }
    }
    
    private void setTagNameField() {
        String name = tagMetadata.get(curLangIndex);
        if ( name == null || name.length() == 0 ) {
            name = "Tag ("+tag.getUUID().split("-")[4]+")";
            tagMetadata.set(curLangIndex, name);
        }
        tagnameTextField.setText(name);
    }

    private void syncToBuffer() {
        // sync current entry to the metadata buffer
        tagMetadata.set(curLangIndex, tagnameTextField.getText());
    }
    
    private void syncToModel() {
        for (int i=0; i < languageKeys.size(); i++) {
            String lang = languageKeys.get(i);
            for (HiFlexMetadataRecord rec : tag.getMetadata()) {
                if (rec.getLanguage().compareTo(lang) == 0) {
                    MetadataHelper.setValue("HIBase", "title", tagMetadata.get(i), rec);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        editTagPanel = new JPanel();
        languageLabel = new JLabel();
        languageComboBox = new JComboBox();
        saveButton = new SaveButton();
        cancelButton = new ResetButton();
        tagNamePanel = new JPanel();
        tagnameTextField = new JTextField();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        languageLabel.setHorizontalAlignment(SwingConstants.TRAILING);

        saveButton.setPreferredSize(new Dimension(24, 24));

        cancelButton.setPreferredSize(new Dimension(24, 24));

        tagNameBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Tagname", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue);
        tagNamePanel.setBorder(tagNameBorder); // NOI18N

        GroupLayout jPanel1Layout = new GroupLayout(tagNamePanel);
        tagNamePanel.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tagnameTextField)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tagnameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout editTagPanelLayout = new GroupLayout(editTagPanel);
        editTagPanel.setLayout(editTagPanelLayout);
        editTagPanelLayout.setHorizontalGroup(editTagPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(editTagPanelLayout.createSequentialGroup()
                .addGroup(editTagPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(editTagPanelLayout.createSequentialGroup()
                        .addComponent(languageLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(languageComboBox, GroupLayout.PREFERRED_SIZE, 199, GroupLayout.PREFERRED_SIZE))
                    .addGroup(editTagPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(editTagPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(editTagPanelLayout.createSequentialGroup()
                                .addGap(0, 372, Short.MAX_VALUE)
                                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addComponent(tagNamePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        editTagPanelLayout.setVerticalGroup(editTagPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, editTagPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editTagPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(languageComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(languageLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tagNamePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(editTagPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(editTagPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(editTagPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }              

    // -------------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if ( e.getSource() == languageComboBox ) {
            if ( languageKeys.size() > 0 ) {
			// save changes to buffer
			syncToBuffer();

			curLangIndex = languageComboBox.getSelectedIndex();

			setTagNameField();
		}
        } else {
            if ( e.getSource() == saveButton ) {
                // sync changes to model
                syncToBuffer();
                syncToModel();
                // TODO indicate save action
                this.saveSelected = true;
            } else this.saveSelected = false;
            setVisible(false); // close dialog
        }
    }

}
