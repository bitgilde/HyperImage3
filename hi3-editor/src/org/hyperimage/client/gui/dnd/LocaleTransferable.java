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
import java.util.Locale;

import org.hyperimage.client.util.MetadataHelper;

/**
 * 
 * Class: LocaleTransferable
 * Package: org.hyperimage.client.gui.dnd
 * @author Jens-Martin Loebel
 *
 */
public class LocaleTransferable implements Transferable {
	
	// the Locale custom Data Flavor, also supports exporting as String (language ID)
	public static final DataFlavor localeFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";"+Locale.class.toString().replaceAll(" ", "="), "HyperImage Locale - Project Language");

	private Locale lang;
	

	public LocaleTransferable(Locale lang) {
		this.lang = lang;
	}


	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {

		// we support the String flavor
		// export user name.
		if ( flavor == DataFlavor.stringFlavor ) {
			String title;
			title = MetadataHelper.localeToLangID(lang);
			
			return title; // return user name
		}
		
		// export Locale itself
		if ( flavor == localeFlavor )
			return lang;
		
		return null; // no supported data flavor found

	}

	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor flavors[] = new DataFlavor[2];
		flavors[0] = DataFlavor.stringFlavor;
		flavors[1] = localeFlavor;
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if ( flavor == DataFlavor.stringFlavor || flavor == localeFlavor )
			return true;

		return false;
	}

}
