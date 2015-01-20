/* $Id: HITypedDatastream.java 133 2009-08-25 12:05:53Z jmloebel $*/

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

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * Package the binary data and the MIME type in an object.
 * 
 * @author Heinz-Günter Kuper
 *
 */
public class HITypedDatastream {
	private MimeType m_MIMEType = null;
	private byte[] m_byteArray = null;
	
	public HITypedDatastream() {
		// NOP
	}

	public boolean setMIMEType(String strMIMEType) {
		try {
			m_MIMEType = new MimeType(strMIMEType);
		} catch (MimeTypeParseException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void setByteArray(byte[] bytearray) {
		m_byteArray = bytearray;
	}
	
	public String getMIMEType() {
		return m_MIMEType.getPrimaryType() + "/" + m_MIMEType.getSubType();
	}
	
	public byte[] getByteArray() {
		return m_byteArray;
	}
}
