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

package org.hyperimage.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiInscription;

/**
 * @author Jens-Martin Loebel
 */
public class InscriptionViewerControl extends JPanel {

	private static final long serialVersionUID = -1037643810535292908L;

	
	private HiInscription inscription = null;
	private JTextPane insPane;
	private String defLang;
	
	public InscriptionViewerControl(String defLang) {
		this.defLang = defLang;

		this.setLayout(new BorderLayout());
		insPane = new JTextPane();
		insPane.setBorder(new EmptyBorder(5,5,5,5));
		insPane.setBackground(Color.WHITE);
		insPane.setEditable(false);
		insPane.setContentType("text/html");

		this.add(new JScrollPane(insPane), BorderLayout.CENTER);
	}
	
	public void setInscription(HiInscription inscription) {
		this.inscription = inscription;
		
		if ( this.inscription != null ) {
			String html = MetadataHelper.richTextToHTML(
					MetadataHelper.findValue("HIBase", "content", 
							MetadataHelper.getDefaultMetadataRecord(inscription, defLang))
					);
			
			insPane.setText(html);
		}
			
	}
}
