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
import org.hyperimage.client.ws.HiFlexMetadataSet;

/**
 * Drag and Drop support class for Template Sets (fields).
 * Supports exporting a template set as well as a String representation of the template set.
 * Class: TemplateFieldTransferable
 * @author Jens-Martin Loebel
 *
 */
public class TemplateFieldTransferable implements Transferable {
	// the HiUser custom Data Flavor, also supports exporting as String (user first/last and short name)
	public static final DataFlavor fieldFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";"+HiFlexMetadataSet.class.toString().replaceAll(" ", "="), "HyperImage Template Field");

	private HiFlexMetadataSet set;


	public TemplateFieldTransferable(HiFlexMetadataSet set) {
		this.set = set;
	}

        @Override
	public Object getTransferData(DataFlavor flavor)
	throws UnsupportedFlavorException, IOException {
		// we support the String flavor
		// export template field display name in current GUI language.
		if ( flavor == DataFlavor.stringFlavor ) {
			String title;
			title = MetadataHelper.getTemplateKeyDisplayName(null, set, MetadataHelper.localeToLangID(HIRuntime.getGUILanguage()));

			return title; // return display name
		}

		// export user itself
		if ( flavor == fieldFlavor )
			return set;

		return null; // no supported data flavor found
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor flavors[] = new DataFlavor[2];
		flavors[0] = DataFlavor.stringFlavor;
		flavors[1] = fieldFlavor;
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if ( flavor == DataFlavor.stringFlavor || flavor == fieldFlavor )
			return true;

		return false;
	}

}
