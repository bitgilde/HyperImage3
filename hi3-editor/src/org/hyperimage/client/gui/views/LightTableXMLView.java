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
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.ResetButton;
import org.hyperimage.client.gui.SaveButton;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiLightTable;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Jens-Martin Loebel
 */
public class LightTableXMLView extends GUIView {

	private static final long serialVersionUID = -5239949927219184068L;

	// GUI components
	private JPanel editorPanel;
    private JLabel infoLabel;
    private ResetButton resetButton;
    private SaveButton saveButton;
    private JLabel titleLabel;
    private JScrollPane xmlScroll;
    private JTextArea xmlTextArea;
    
    // xml status
    boolean isValidLitaXML = false;
	
    // xml parser
    DocumentBuilderFactory factory;
	DocumentBuilder builder;
	Schema litaSchema = null;
	
	HiLightTable lightTable;
	String lightTableTitle, lightTableXML;
	
    
    /**
	 * Custom Document filter for HyperImage READER XML documents and constraints
	 * Class: HILightTableXMLDocumentFilter
	 * Package: org.hyperimage.client.gui
	 * @author Jens-Martin Loebel
	 */
	class HILightTableXMLDocumentFilter extends DocumentFilter {
		
		private void trimXML() {
			String xml = xmlTextArea.getText();
			
			if ( !xml.startsWith("<lita id=") && xml.indexOf("<lita id=") > 0 ) //$NON-NLS-1$ //$NON-NLS-2$
				// trim C.T.´s XML and find the "lita" element --> this is crude
				xml = xmlTextArea.getText().substring(xmlTextArea.getText().indexOf("<lita id=")); //$NON-NLS-1$

			if ( !xmlTextArea.getText().endsWith("</lita>") && xmlTextArea.getText().indexOf("</lita>") > 0 ) //$NON-NLS-1$ //$NON-NLS-2$
				// trim C.T.´s XML and find the "lita" element --> this is crude
				xml = xml.substring(0, xml.indexOf("</lita>")+7); //$NON-NLS-1$
			
			if ( xml.compareTo(xmlTextArea.getText()) != 0 ) {
				xmlTextArea.setText(xml);
			}
		}

		
		public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
			super.insertString(fb, offs, str, a);
			trimXML();
			parseXMLAndUpdateStatus();
			
		}

		public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
			super.remove(fb, offset, length);
			trimXML();
			parseXMLAndUpdateStatus();
		}

		public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			super.replace(fb, offset, length, text, attrs);
			trimXML();
			parseXMLAndUpdateStatus();
		}

	}

    
    
	public LightTableXMLView(HiLightTable lightTable) {
		super(Messages.getString("LightTableXMLView.6"), new Color(0x61, 0x89, 0xCA)); //$NON-NLS-1$

		this.lightTable = lightTable;
		
		initComponents();
		setDisplayPanel(editorPanel);
		
		lightTableTitle = lightTable.getTitle();
		lightTableXML = lightTable.getXml();
		xmlTextArea.setText(lightTableXML);
		setLightTableTitle(lightTableTitle);

		// init xml parser
		factory = DocumentBuilderFactory.newInstance();
		builder = null;
		try {
			factory.setNamespaceAware(false);
			builder = factory.newDocumentBuilder();
			if ( builder != null )
				builder.setErrorHandler(null);
			litaSchema = SchemaFactory.newInstance(
					XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(
							getClass().getResource("/resources/schema/lita.xsd")); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			// error could not create xml parser
			builder = null;
			System.out.println("Client Error: could not init light table xml parser\nReason: "+e.getMessage()); //$NON-NLS-1$
		} catch (SAXException e) {
			litaSchema = null;
			System.out.println("Client Error: could not init light table xml parser schema\nReason: "+e.getMessage()); //$NON-NLS-1$
		}
		
		// set initial status
		parseXMLAndUpdateStatus();

	}

	public void updateLanguage() {	
		saveButton.setToolTipText(Messages.getString("LightTableXMLView.34")); //$NON-NLS-1$
        resetButton.setToolTipText(Messages.getString("LightTableXMLView.35")); //$NON-NLS-1$

        super.setTitle(Messages.getString("LightTableXMLView.6")); //$NON-NLS-1$
        
        parseXMLAndUpdateStatus();
	}
	
	
	public void resetChanges() {
		lightTableTitle = lightTable.getTitle();
		lightTableXML = lightTable.getXml();
		xmlTextArea.setText(lightTableXML);
		setLightTableTitle(lightTableTitle);
		parseXMLAndUpdateStatus();
	}

	public boolean hasChanges() {
		return lightTable.getXml().compareTo(lightTableXML) != 0 ? true : false;
	}
	
	public void syncChanges() {
		lightTable.setTitle(lightTableTitle);
		lightTable.setXml(lightTableXML);
		
		super.setTitle(Messages.getString("LightTableXMLView.6")); //$NON-NLS-1$
	}
	

	public JButton getSaveButton() {
		return saveButton;
	}

	public JButton getResetButton() {
		return resetButton;
	}
	
	
	// DEBUG

	private void parseXMLAndUpdateStatus() {
		isValidLitaXML = false;
		
		Document doc = null;
		// prepare xml input
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+xmlTextArea.getText(); //$NON-NLS-1$
		// sanitize user XML input
		xml = MetadataHelper.convertToUTF8(xml);
		
		ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());

		try {
			if ( builder != null ) {
				// parse document using DOM, if possible
				builder.reset();
				doc = builder.parse(input);

				// validate against lita schema, if possible
				if ( litaSchema != null ) litaSchema.newValidator().validate(new DOMSource(doc), new DOMResult());

				// passed xml and schema validation
				isValidLitaXML = true;
			}
		} catch (SAXException e) {
			// invalid xml
			isValidLitaXML = false;
		} catch (IOException e) {
			// invalid xml
			isValidLitaXML = false;
		}
		
		if ( isValidLitaXML ) {
			lightTableXML = xmlTextArea.getText();
			
			NodeList nodes = doc.getElementsByTagName("lita"); //$NON-NLS-1$
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);

				NodeList title = element.getElementsByTagName("title"); //$NON-NLS-1$
				Element line = (Element) title.item(0);
				String lang = line.getAttribute("xml:lang"); //$NON-NLS-1$
				if ( lang != null && lang.compareTo(HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()) == 0 )
					lightTableTitle = line.getTextContent();
			}
			setLightTableTitle(lightTableTitle);
		} else
			setLightTableTitle("-"); //$NON-NLS-1$

		updateXMLGUI();
	}
	
	
	private void setLightTableTitle(String title) {
		if ( title.length() > 0 ) 
			titleLabel.setText(Messages.getString("LightTableXMLView.15")+": "+title); //$NON-NLS-1$ //$NON-NLS-2$
		else
			titleLabel.setText(Messages.getString("LightTableXMLView.17")+": -"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void updateXMLGUI() {
		saveButton.setEnabled(isValidLitaXML);

		if ( isValidLitaXML ) {
 	        infoLabel.setText("<html>"+Messages.getString("LightTableXMLView.20")+": X"+lightTable.getId()+" - <font color=\"#009900\">"+Messages.getString("LightTableXMLView.23")+"</font></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 	        xmlTextArea.setForeground(Color.blue);
		} else {
	        infoLabel.setText("<html>"+Messages.getString("LightTableXMLView.26")+": X"+lightTable.getId()+" - <font color=\"#FF0000\">"+Messages.getString("LightTableXMLView.29")+"</font></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 	        xmlTextArea.setForeground(Color.black);
		}
	}

	
	private void initComponents() {

        editorPanel = new JPanel();
        xmlScroll = new JScrollPane();
        xmlTextArea = new JTextArea();
        titleLabel = new JLabel();
        infoLabel = new JLabel();
        resetButton = new ResetButton();
        saveButton = new SaveButton();

        xmlTextArea.setColumns(20);
        xmlTextArea.setRows(5);
        xmlScroll.setViewportView(xmlTextArea);

        titleLabel.setText(Messages.getString("LightTableXMLView.31")+": "); //$NON-NLS-1$ //$NON-NLS-2$

        infoLabel.setText("<html>xxx</html>"); //$NON-NLS-1$

        GroupLayout editorPanelLayout = new GroupLayout(editorPanel);
        editorPanel.setLayout(editorPanelLayout);
        editorPanelLayout.setHorizontalGroup(
                editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(editorPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
                        .add(xmlScroll, GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                        .add(titleLabel, GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                        .add(editorPanelLayout.createSequentialGroup()
                            .add(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.RELATED)
                            .add(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.UNRELATED)
                            .add(infoLabel, GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)))
                    .addContainerGap())
            );
            editorPanelLayout.setVerticalGroup(
                editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
                .add(editorPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(titleLabel)
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(xmlScroll, GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                    .add(8, 8, 8)
                    .add(editorPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                        .add(infoLabel, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                        .add(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
            );

        
        // -----
        
		((AbstractDocument) xmlTextArea.getDocument()).setDocumentFilter(new HILightTableXMLDocumentFilter());
		
		updateLanguage();
    }

}
