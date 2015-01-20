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

import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiQuickInfo;

/**
 * 
 * Class: QuickInfoTransferable
 * Package: org.hyperimage.client.gui.dnd
 * @author Jens-Martin Loebel
 *
 */
public class QuickInfoTransferable implements Transferable {

	
	// the HiQuickInfo custom Data Flavor, also supports exporting as String (number of elements or single element title)
	public static final DataFlavor quickInfoFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";"+HiQuickInfo.class.toString().replaceAll(" ", "="), "HyperImage Quick Info");

	private ContentTransfer transfer;
	
	
	public QuickInfoTransferable(ContentTransfer transfer) {
		this.transfer = transfer;
	}
	
	
	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if ( flavor == DataFlavor.stringFlavor ) {
			if ( transfer.getContents().size() == 1 ) {
				String title = transfer.getContents().get(0).getTitle();
				if ( title.length() > 0 ) return title;
				return "ID "+MetadataHelper.getDisplayableID(transfer.getContents().get(0));
			}
			else return transfer.getContents().size()+" Elemente";
		}
		if ( flavor == quickInfoFlavor )
			return transfer;
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor flavors[] = new DataFlavor[2];
		flavors[0] = DataFlavor.stringFlavor;
		flavors[1] = quickInfoFlavor;
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if ( flavor == DataFlavor.stringFlavor || flavor == quickInfoFlavor )
			return true;
		
		return false;
	}

}
