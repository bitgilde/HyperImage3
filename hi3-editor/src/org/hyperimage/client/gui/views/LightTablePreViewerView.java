/*
 * Copyright 2014 Leuphana Universität Lüneburg. All rights reserved.
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

/*
 * Copyright 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
 * All rights reserved. Use is subject to license terms.
 * http://bitgilde.de/
 *
 * For further information on HyperImage visit http://hyperimage.ws/
 */

package org.hyperimage.client.gui.views;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.TagsButton;
import org.hyperimage.client.gui.dialogs.HIBaseTagsEditorDialog;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiLightTable;

/**
 * @author Jens-Martin Loebel
 */
public class LightTablePreViewerView extends GUIView implements ActionListener {

    private static final long serialVersionUID = -5239949927219184068L;

    // GUI components
    private JPanel editorPanel;
    private JLabel infoLabel;
    private JLabel titleLabel;
    private JPanel infoPanel;
    private JButton openPreViewerButton;
    private TagsButton tagsButton;
    private JLabel previewerNoteLabel;

    HiLightTable lightTable;
    String lightTableTitle;

    public LightTablePreViewerView(HiLightTable lightTable) {
        super(Messages.getString("LightTablePreViewerView.editTitle"), new Color(0x61, 0x89, 0xCA)); //$NON-NLS-1$

        this.lightTable = lightTable;
        initComponents();
        setDisplayPanel(editorPanel);
        
        // set up tag button
        tagsButton.setCount((int) (long) HIRuntime.getGui().getTagCountForElement(lightTable.getId()));
        tagsButton.addActionListener(this);
        
//        if ( lightTable.getUUID() == null ) infoLabel.setText("ID: X"+lightTable.getId());
//        else infoLabel.setText("ID: "+lightTable.getUUID());
        infoLabel.setText(Messages.getString("HIClientGUI.lastChanged")+": "+MetadataHelper.getFuzzyDate(lightTable.getTimestamp()));

    }
    
    public void updateContent() {
        infoLabel.setText(Messages.getString("HIClientGUI.lastChanged")+": "+MetadataHelper.getFuzzyDate(lightTable.getTimestamp()));
    }

    @Override
    public void updateLanguage() {
        super.setTitle(Messages.getString("LightTablePreViewerView.editTitle")); //$NON-NLS-1$

        openPreViewerButton.setText(Messages.getString("LightTablePreViewerView.openPreViewer"));
        tagsButton.setToolTipText(Messages.getString("MetadataEditorControl.tagButtonTooltip"));
        previewerNoteLabel.setText("<html><b>"+Messages.getString("LightTablePreViewerView.note")+"</b><br>"+
                Messages.getString("LightTablePreViewerView.info1")+"<br>"+
                Messages.getString("LightTablePreViewerView.info2")+"<br><ul><li>"+
                Messages.getString("LightTablePreViewerView.instruction1")+"</li><li>"+
                Messages.getString("LightTablePreViewerView.instruction2")+"</li><li>"+
                Messages.getString("LightTablePreViewerView.instruction3")+"</li></ul></html>");
       
        ((TitledBorder) infoPanel.getBorder()).setTitle(Messages.getString("LightTablePreViewerView.info"));

        setLightTableTitle(lightTable.getTitle());
    }

    public void resetChanges() {
    }

    public boolean hasChanges() {
        return false;
    }

    public void syncChanges() {
    }
    
    public JButton getPreViewerButton() {
        return this.openPreViewerButton;
    }

    private void setLightTableTitle(String title) {
        if (title.length() > 0) {
            titleLabel.setText(Messages.getString("LightTableXMLView.15") + ": " + title); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            titleLabel.setText(Messages.getString("LightTableXMLView.17") + ": -"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private void initComponents() {

        editorPanel = new JPanel();
        titleLabel = new JLabel();
        infoLabel = new JLabel();
        infoPanel = new JPanel();
        previewerNoteLabel = new JLabel();
        openPreViewerButton = new JButton();
        tagsButton = new TagsButton();
        openPreViewerButton.setActionCommand("open");

        titleLabel.setText(Messages.getString("LightTableXMLView.31") + ": "); //$NON-NLS-1$ //$NON-NLS-2$
        infoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("LightTablePreViewerView.info"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.blue));

        previewerNoteLabel.setVerticalAlignment(SwingConstants.TOP);

        GroupLayout infoPanelLayout = new GroupLayout(infoPanel);
        infoPanel.setLayout(infoPanelLayout);
        infoPanelLayout.setHorizontalGroup(
            infoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(infoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(openPreViewerButton, GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                    .addComponent(previewerNoteLabel, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(titleLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        infoPanelLayout.setVerticalGroup(
            infoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, infoPanelLayout.createSequentialGroup()
                .addComponent(titleLabel)
                .addGap(18, 18, 18)
                .addComponent(previewerNoteLabel, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openPreViewerButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        GroupLayout editorPanelLayout = new GroupLayout(editorPanel);
        editorPanel.setLayout(editorPanelLayout);
        editorPanelLayout.setHorizontalGroup(editorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(editorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(infoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(editorPanelLayout.createSequentialGroup()
                        .addComponent(infoLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tagsButton, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)))
                .addContainerGap())
        );
        editorPanelLayout.setVerticalGroup(editorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(editorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(editorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(infoLabel)
                    .addComponent(tagsButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        

        // -----

        if (System.getProperty("HI.feature.tagsDisabled") != null) {
            tagsButton.setVisible(false);
        }

        updateLanguage();
    }

    
    // --------------------------------------------------------------------------------------------------
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        tagsButton.setCount(new HIBaseTagsEditorDialog(HIRuntime.getGui(), lightTable.getId()).chooseTags());
    }

}
