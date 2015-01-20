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

import java.util.Vector;

import org.hyperimage.client.components.HIComponent;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiQuickInfo;

/**
 * @author Jens-Martin Loebel
 */
public class ContentTransfer {
	private Vector<HiQuickInfo> contents;
	private HiGroup sourceGroup;
	private HIComponent source;
	private HIComponent target = null;
	
	
	public ContentTransfer(Vector<HiQuickInfo> contents, HiGroup sourceGroup, HIComponent source) {
		this.contents = contents;
		this.sourceGroup = sourceGroup;
		this.source = source;
	}
	
	
	public Vector<HiQuickInfo> getContents() {
		return this.contents;
	}
	
	public HiGroup getSourceGroup() {
		return this.sourceGroup;
	}
	
	public HIComponent getSource() {
		return this.source;
	}

	public HIComponent getTarget() {
		return this.target;
	}
	
	public void setTarget(HIComponent target) {
		this.target = target;
	}

}