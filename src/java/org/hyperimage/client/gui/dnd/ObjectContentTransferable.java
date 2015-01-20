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

package org.hyperimage.client.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiObjectContent;
import org.hyperimage.client.ws.HiProject;

/**
 * Drag and Drop support class for Object contents.
 * Supports exporting a content object as well as a String representation of the content object.
 * Class: ObjectContentTransferable
 * @author Jens-Martin Loebel
 *
 */
public class ObjectContentTransferable implements Transferable {

	private HiObjectContent content;

	// the HiObjectContent custom Data Flavor, also supports exporting as String (content title)
	public static final DataFlavor objecContentFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";"+HiObjectContent.class.toString().replaceAll(" ", "="), "HyperImage Object Content");

	
	
	public ObjectContentTransferable(HiObjectContent content) {
		this.content = content;
	}


	private String getId() {
		if ( content instanceof HiInscription ) return "Inschrift I"+content.getId(); 
		return "Ansicht V"+content.getId();
	}
	
	public Object getTransferData(DataFlavor flavor)
	throws UnsupportedFlavorException, IOException {
		// we support the String flavor
		// export content title in the default project language. If group has no title export ID
		if ( flavor == DataFlavor.stringFlavor ) {
			String title;

			// check if we can get to the manager --> project and then the project´s default language to read out the correct title
			HiProject project = null;
			if ( HIRuntime.getManager() != null ) project = HIRuntime.getManager().getProject();
			// not possible to get to project, just export ID
			if ( project == null ) return getId();

			// get group title in project´s default language
			title = MetadataHelper.findValue("HIBase", "title", MetadataHelper.getDefaultMetadataRecord(content, HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()));
			
			// title empty or not present --> return id
			if ( title == null || title.length() == 0 )
			return getId();
			
			
			return title; // return group title
		}
		
		// export object content itself
		if ( flavor == ObjectContentTransferable.objecContentFlavor )
			return content;
		
		return null; // no supported data flavor found
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor flavors[] = new DataFlavor[2];
		flavors[0] = DataFlavor.stringFlavor;
		flavors[1] = ObjectContentTransferable.objecContentFlavor;
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if ( flavor == DataFlavor.stringFlavor || flavor == ObjectContentTransferable.objecContentFlavor )
			return true;

		return false;
	}



}
