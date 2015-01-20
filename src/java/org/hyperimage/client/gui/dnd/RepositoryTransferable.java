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
import java.util.List;

import org.hyperimage.client.components.RepositoryImport;
import org.hyperimage.client.ws.HiQuickInfo;

/**
 * Drag and Drop support for external repository elements
 * Class: RepositoryTransferable
 * Package: org.hyperimage.client.gui.dnd
 * @author Jens-Martin Loebel
 *
 */
public class RepositoryTransferable implements Transferable {

	// the HILayer custom Data Flavor, also supports exporting as String (layer title)
	public static final DataFlavor repositoryFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";"+RepositoryTransferable.class.toString().replaceAll(" ", "="), "HyperImage External Repository Element");

	private RepositoryTransfer repTransfer;


	public RepositoryTransferable(List<HiQuickInfo> repElements, RepositoryImport source) {
		this.repTransfer = new RepositoryTransfer(repElements, source);
	}


	public Object getTransferData(DataFlavor flavor) {
		// we support the String flavor
		// export element URN 
		if ( flavor == DataFlavor.stringFlavor ) {
			if ( repTransfer.getRepElements().size() == 1 )
				return repTransfer.getRepElements().get(0).getPreview();
			else return repTransfer.getRepElements().size()+" Elemente";				
		}

		// export element itself
		if ( flavor == repositoryFlavor )
			return repTransfer;

		return null; // no supported data flavor found
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor flavors[] = new DataFlavor[2];
		flavors[0] = DataFlavor.stringFlavor;
		flavors[1] = repositoryFlavor;
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if ( flavor == DataFlavor.stringFlavor || flavor == repositoryFlavor )
			return true;

		return false;
	}
}
