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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class FeedbackView extends GUIView implements DocumentListener {

	private static final long serialVersionUID = 3725404431826887185L;

	private JPanel feedbackPanel;
    private JScrollPane feedbackScroll;
    private JTextArea feedbackTextArea;
    private JComboBox feedbackTypeComboBox;
    private JLabel infoLabel;
    private JButton sendFeedbackButton;
	
	public FeedbackView() {
		super("Feedback geben");
		
		initComponents();
		
		setDisplayPanel(feedbackPanel);

		feedbackTextArea.getDocument().addDocumentListener(this);
		
	}
	
	
	public JButton getFeedbackButton() {
		return sendFeedbackButton;
	}
	
	public String getText() {
		return feedbackTextArea.getText();
	}
	
	public String getType() {
		return (String) feedbackTypeComboBox.getSelectedItem();
	}
	
	
	private void initComponents() {

        feedbackPanel = new JPanel();
        feedbackScroll = new JScrollPane();
        feedbackTextArea = new JTextArea();
        feedbackTypeComboBox = new JComboBox();
        sendFeedbackButton = new JButton();
        infoLabel = new JLabel();
        
        feedbackTextArea.setColumns(20);
        feedbackTextArea.setRows(5);
        feedbackScroll.setViewportView(feedbackTextArea);

//        feedbackTypeComboBox.setModel(new DefaultComboBoxModel(new String[] { "Anmerkung / Anregung", "Bug Report", "Feature Request" }));
        // REMOVED feature request and comment from list ad feedback function is now solely used to report bugs
      feedbackTypeComboBox.setModel(new DefaultComboBoxModel(new String[] { "Bug Report" }));

        sendFeedbackButton.setText("Feedback abschicken");

        infoLabel.setText("<html><b>Ihre Meinung ist uns wichtig!</b><br>Bitte wählen Sie eine Kategorie und geben Sie Ihre Nachricht ein. Ihr Feedback wird an die HyperImage Entwickler gesendet.</html>");

        GroupLayout feedbackPanelLayout = new GroupLayout(feedbackPanel);
        feedbackPanel.setLayout(feedbackPanelLayout);
        feedbackPanelLayout.setHorizontalGroup(
            feedbackPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(feedbackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(feedbackPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(feedbackScroll, GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .add(GroupLayout.TRAILING, sendFeedbackButton, GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .add(feedbackTypeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(infoLabel, GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
                .addContainerGap())
        );
        feedbackPanelLayout.setVerticalGroup(
            feedbackPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, feedbackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(infoLabel)
                .add(14, 14, 14)
                .add(feedbackTypeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(feedbackScroll, GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(sendFeedbackButton)
                .addContainerGap())
        );
            
            // -----
            
            sendFeedbackButton.setEnabled(false);
        
    }
	
	
	public void setFeedbackOption(int option) {
		option = Math.max(0, option);
		option = Math.min(option, feedbackTypeComboBox.getModel().getSize()-1);
		
		feedbackTypeComboBox.setSelectedIndex(option);
	}
	
	
	// ---------------------------------------------------------------------------------------------------


	@Override
	public void changedUpdate(DocumentEvent e) {
		sendFeedbackButton.setEnabled( getText().length() > 0 ? true : false );
	}


	@Override
	public void insertUpdate(DocumentEvent e) {
		sendFeedbackButton.setEnabled( getText().length() > 0 ? true : false );
	}


	@Override
	public void removeUpdate(DocumentEvent e) {
		sendFeedbackButton.setEnabled( getText().length() > 0 ? true : false );
	}
	
}
