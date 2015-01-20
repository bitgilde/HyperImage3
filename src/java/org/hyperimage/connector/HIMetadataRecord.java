/* $Id: HIMetadataRecord.java 133 2009-08-25 12:05:53Z jmloebel $ */

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
 * Store metadata records in a key-value format.
 * 
 * @author Heinz-Günter Kuper
 */
public class HIMetadataRecord {
	private MetadataType m_metadataType = null;
	private String m_strKey = null;
	private String m_strValue = null;
	
	public enum MetadataType {
		/**
		 * Dublin Core
		 */
		DC,
		/**
		 * Anything else ...
		 */
		OTHER
	}
	
	public MetadataType getMetadataType() {
		return m_metadataType;
	}
	public void setMetadataType(MetadataType metadataType) {
		m_metadataType = metadataType;
	}
	public String getKey() {
		return m_strKey;
	}
	public void setKey(String key) {
		m_strKey = key;
	}
	public String getValue() {
		return m_strValue;
	}
	public void setValue(String value) {
		m_strValue = value;
	}

}
