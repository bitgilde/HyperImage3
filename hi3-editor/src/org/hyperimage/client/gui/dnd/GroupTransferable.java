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
import org.hyperimage.client.ws.GroupTypes;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiProject;

/**
 * Drag and Drop support class for Groups.
 * Supports exporting a group as well as a String representation of the group.
 * Class: GroupTransferable
 * @author Jens-Martin Loebel
 *
 */
public class GroupTransferable implements Transferable {

	// the HiGroup custom Data Flavor, also supports exporting as String (group title)
	public static final DataFlavor groupFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";"+HiGroup.class.toString().replaceAll(" ", "="), "HyperImage Group");

	private HiGroup group;

	
	public GroupTransferable(HiGroup group) {
		this.group = group;
	}
	
	
	private String getId() {
		if ( group == null ) return "";
		return "Gruppe G"+group.getId();
	}

	
	public Object getTransferData(DataFlavor flavor)
	throws UnsupportedFlavorException, IOException {
		// we support the String flavor
		// export group title in the default project language. If group has no title export ID
		if ( flavor == DataFlavor.stringFlavor ) {
			String title;

			// check if this is a special group
			if ( group.getType() == GroupTypes.HIGROUP_IMPORT ) return "Import Gruppe";
			if ( group.getType() == GroupTypes.HIGROUP_TRASH ) return "Papierkorb";
			
			// check if we can get to the manager --> project and then the project´s default language to read out the correct title
			HiProject project = null;
			if ( HIRuntime.getManager() != null ) project = HIRuntime.getManager().getProject();
			if ( project == null ) return getId(); // not possible to get to project, just export ID

			// get group title in project´s default language
			title = MetadataHelper.findValue("HIBase", "title", MetadataHelper.getDefaultMetadataRecord(group, HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()));
			
			// title empty or not present --> return id
			if ( title == null || title.length() == 0 ) return getId();

			
			return title; // return group title
		}
		
		// export group itself
		if ( flavor == groupFlavor )
			return group;
		
		return null; // no supported data flavor found
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor flavors[] = new DataFlavor[2];
		flavors[0] = DataFlavor.stringFlavor;
		flavors[1] = groupFlavor;
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if ( flavor == DataFlavor.stringFlavor || flavor == groupFlavor )
			return true;

		return false;
	}


}
