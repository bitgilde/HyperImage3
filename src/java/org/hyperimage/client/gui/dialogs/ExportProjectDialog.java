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

package org.hyperimage.client.gui.dialogs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class ExportProjectDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = -5157327724136384906L;

    private ButtonGroup exportFormatButtonGroup;
    private JLabel exportFormatLabel;
    private JRadioButton petal2RadioButton;
    private JRadioButton petal3RadioButton;
    private JButton cancelButton;
    private JCheckBox exportBinariesCheckBox;
    private JButton exportButton;
    private JPanel exportOptionsPanel;
    private JFrame parent;
    private TitledBorder exportOptionsBorder;
    private JLabel exportNoteLabel;

    private JFileChooser exportDirChooser;
    private File exportDir;

    public ExportProjectDialog(JFrame owner) {
        super(owner);
        this.parent = owner;

        initComponents();

        // attach listeners
        exportButton.addActionListener(this);
        cancelButton.addActionListener(this);
    }

    private void updateLanguage() {
        exportOptionsBorder.setTitle(Messages.getString("ExportProjectDialog.1")); //$NON-NLS-1$
        exportBinariesCheckBox.setText(Messages.getString("ExportProjectDialog.3")); //$NON-NLS-1$

        exportButton.setText(Messages.getString("ExportProjectDialog.8")); //$NON-NLS-1$

        cancelButton.setText(Messages.getString("ExportProjectDialog.9")); //$NON-NLS-1$
        this.setTitle(Messages.getString("ExportProjectDialog.12")); //$NON-NLS-1$

        exportNoteLabel.setText("<html>" + Messages.getString("ExportProjectDialog.17") + "</html>"); //$NON-NLS-1$

        exportFormatLabel.setText("Ausgabeformat:");

        exportDirChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return Messages.getString("ExportProjectDialog.13"); //$NON-NLS-1$
            }
        });
    }

    public boolean displayExportDialog() {

        // set dialog size, center on screen
        this.setBounds(
                (parent.getWidth() / 2) - (this.getWidth() / 2),
                (parent.getHeight() / 2) - (this.getHeight() / 2),
                this.getWidth(),
                this.getHeight());

        // init export dir
        this.exportDir = null;

        // update on-screen language
        updateLanguage();

        // display dialog
        this.setVisible(true);

        // check if user selected a valid export dir
        if (exportDir != null) {
            return true;
        }

        return false;
    }

    public File getExportDir() {
        return this.exportDir;
    }
    
    public boolean isPeTAL3Mode() {
        return petal3RadioButton.isSelected();
    }

    public boolean isExportingBinaries() {
        return exportBinariesCheckBox.isSelected();
    }

    private void initComponents() {

        exportOptionsPanel = new JPanel();
        exportBinariesCheckBox = new JCheckBox();
        exportButton = new JButton();
        cancelButton = new JButton();
        exportNoteLabel = new JLabel();
        exportFormatButtonGroup = new ButtonGroup();
        petal3RadioButton = new JRadioButton();
        petal2RadioButton = new JRadioButton();
        exportFormatLabel = new JLabel();

        exportBinariesCheckBox.setSelected(true);
        exportFormatButtonGroup.add(petal3RadioButton);
        petal3RadioButton.setSelected(true);
        petal3RadioButton.setText("PeTAL 3.0");

        exportFormatButtonGroup.add(petal2RadioButton);
        petal2RadioButton.setText("PeTAL 2.0");

        setModal(true);

        exportOptionsBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("ExportProjectDialog.1"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), Color.blue); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$
        exportOptionsPanel.setBorder(exportOptionsBorder);

        GroupLayout exportOptionsPanelLayout = new GroupLayout(exportOptionsPanel);
        exportOptionsPanel.setLayout(exportOptionsPanelLayout);
        exportOptionsPanelLayout.setHorizontalGroup(
            exportOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(exportOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(exportOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(exportOptionsPanelLayout.createSequentialGroup()
                        .add(exportFormatLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(petal3RadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(petal2RadioButton)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(exportNoteLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, exportBinariesCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        exportOptionsPanelLayout.setVerticalGroup(
            exportOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(exportOptionsPanelLayout.createSequentialGroup()
                .add(exportOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(petal3RadioButton)
                    .add(petal2RadioButton)
                    .add(exportFormatLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(exportBinariesCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(exportNoteLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, exportOptionsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(cancelButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(exportButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(exportOptionsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(exportButton)
                    .add(cancelButton))
                .addContainerGap())
        );        

        // -----
        exportButton.setActionCommand("export"); //$NON-NLS-1$
        cancelButton.setActionCommand("cancel"); //$NON-NLS-1$

        // setup file chooser
        exportDirChooser = new JFileChooser();
        exportDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        exportDirChooser.setMultiSelectionEnabled(false);
        exportDirChooser.setFileHidingEnabled(true);
        exportDirChooser.setAcceptAllFileFilterUsed(false);

        updateLanguage();
        pack();

		// -----
    }

	// -------------------------------------------------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equalsIgnoreCase("export")) { //$NON-NLS-1$
            // display file chooser dialog
            exportDirChooser.showDialog(parent, Messages.getString("ExportProjectDialog.15")); //$NON-NLS-1$
            File exportDir = exportDirChooser.getSelectedFile();
            // fix target directory (probably only needed for MacOS X)
            if (exportDir != null && !exportDir.exists()) {
                if (exportDir.getAbsolutePath().lastIndexOf(File.separatorChar) >= 0) {
                    exportDir = new File(
                            exportDir.getAbsolutePath().substring(0,
                                    exportDir.getAbsolutePath().lastIndexOf(File.separatorChar)
                            )
                    );
                }
            }

            // validate export dir
            if (exportDir != null && exportDir.exists()) {
                this.exportDir = exportDir;
            } else {
                this.exportDir = null;
            }

            // if dir was chosen close export dialog
            if (this.exportDir != null && this.exportDir.exists()) {
                this.setVisible(false);
            }
        }

        if (e.getActionCommand().equalsIgnoreCase("cancel")) { //$NON-NLS-1$
            this.exportDir = null;
            this.setVisible(false);
        }

    }

}
