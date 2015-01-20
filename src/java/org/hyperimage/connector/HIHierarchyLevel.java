/* $Id: HIHierarchyLevel.java 133 2009-08-25 12:05:53Z jmloebel $ */

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

package org.hyperimage.connector;

/**
 * <p>This class organises information regarding the repository structure.</p>
 * 
 * <p>There are three possible cases:
 * <ol>
 * <li>Element in hierarchy is a level, i.e. a branch in the tree
 * <ul><li>m_isLevel==true, m_hasChildren==true, m_hasPreview==false</li></ul></li>
 * <li>Element in hierarchy is a level, but is a leaf in the tree
 * <ul><li>m_isLevel==true, m_hasChildren==false, m_hasPreview==false</li></ul></li>
 * <li>Element in hierarchy is a leaf and contains binary data (e.g. an image)
 * <ul><li>m_isLevel==false, m_hasChildren==false, m_hasPreview=={true,false}</li></ul></li>
 * </ol>
 * </p>
 * 
 * <p>m_hasPreview is only relevant when dealing with binary data, e.g. an image, and when
 * a preview image exists for the image in question.</p>
 * 
 * @author Heinz-Günter Kuper
 */
public class HIHierarchyLevel {
	private String m_strURN;
	private String m_strDisplayName;
	private boolean m_isLevel;
	private boolean m_hasChildren;
	private boolean m_hasPreview;

	public HIHierarchyLevel() {
		m_strURN = "";
		m_strDisplayName = "";
		m_isLevel = false;
		m_hasChildren = false;
		m_hasPreview = false;
	}

	public String getURN() {
		return m_strURN;
	}

	public void setURN(String strURN) {
		m_strURN = strURN;
	}

	public String getDisplayName() {
		return m_strDisplayName;
	}

	public void setDisplayName(String strDisplayName) {
		m_strDisplayName = strDisplayName;
	}

	public boolean isLevel() {
		return m_isLevel;
	}

	public void setLevel(boolean isLevel) {
		this.m_isLevel = isLevel;
	}

	public boolean hasChildren() {
		return m_hasChildren;
	}

	public void setChildren(boolean hasChildren) {
		this.m_hasChildren = hasChildren;
	}

	public boolean hasPreview() {
		return m_hasPreview;
	}

	public void setPreview(boolean hasPreview) {
		this.m_hasPreview = hasPreview;
	}
}
