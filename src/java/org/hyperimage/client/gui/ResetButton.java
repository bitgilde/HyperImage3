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

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * @author Jens-Martin Loebel
 */
public class ResetButton extends JButton {

	private static final long serialVersionUID = 8054996819281090925L;

	
	public ResetButton() {
        setActionCommand("reset"); //$NON-NLS-1$
        setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        setPreferredSize(new Dimension(24, 24));
		setIcon(new ImageIcon(getClass().getResource("/resources/icons/reset.png"))); //$NON-NLS-1$
		setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/reset-active.png"))); //$NON-NLS-1$
		setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/reset-disabled.png"))); //$NON-NLS-1$

	}
}
