/* $Id: HIWSUTF8EncodingException.java 133 2009-08-25 12:05:53Z jmloebel $ */

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

package org.hyperimage.connector.exception;

/**
 * Wraps an encoding exception in a HyperImage Web Service Exception.
 * 
 * @author Heinz-Günter Kuper
 */
public class HIWSUTF8EncodingException extends Exception {

	/**
	 * 03-09-2008
	 */
	private static final long serialVersionUID = 5227522399477377705L;

	public HIWSUTF8EncodingException(String message, Throwable cause) {
		super(message, cause);
	}

}
