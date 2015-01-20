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

package org.hyperimage.client.exception;

import org.hyperimage.client.ws.HIMaintenanceModeException_Exception;

/**
 * @author Jens-Martin Loebel
 */
// TODO: scan all error types, give more feedback for client/GUI
public class HIWebServiceException extends Exception {

	private static final long serialVersionUID = -3421914798761957500L;

	
	public static enum HIerrorTypes { UNKNOWN, RECONNECT, PRIVILEGE, INTERNAL, ENTITY, MAINTENANCE };
	
	private HIerrorTypes errorType = HIerrorTypes.UNKNOWN;
	
	
	public HIWebServiceException(Exception exception) {
		super(exception);

		errorType = HIerrorTypes.UNKNOWN;

		if ( exception.getMessage() != null && exception.getMessage().indexOf("http://jax-ws.dev.java.net/xml/ns/") >= 0 )
			errorType = HIerrorTypes.RECONNECT;
		
		if ( exception instanceof HIMaintenanceModeException_Exception )
			errorType = HIerrorTypes.MAINTENANCE;
	}
	
	public HIerrorTypes getErrorType() {
		return this.errorType;
	}

}
