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

import org.hyperimage.client.Messages;

/**
 * @author Jens-Martin Loebel
 */
public class RepositoryContentsView extends GenericContentsView {

	private static final long serialVersionUID = 9185945469255398465L;

	
	public RepositoryContentsView() {
		super(Messages.getString("RepositoryContentsView.0")); //$NON-NLS-1$
	}
	
	protected void setLoading(boolean loading) {	
		// DEBUG
		optionsButton.setEnabled(false);
		if ( loading ) elementCountLabel.setText("Lade Inhalt..."); //$NON-NLS-1$
		else {
			if ( elementCount >= 0 ) {
				if ( elementCount != 1 ) elementCountLabel.setText(elementCount+" Elemente gefunden"); //$NON-NLS-1$
				else elementCountLabel.setText("ein Element gefunden"); //$NON-NLS-1$
			} else elementCountLabel.setText("Ladefehler!"); //$NON-NLS-1$
		}
		loadingIndicator.setVisible(loading);
		
	}

}
