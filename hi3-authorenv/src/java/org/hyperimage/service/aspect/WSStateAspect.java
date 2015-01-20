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

package org.hyperimage.service.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hyperimage.service.exception.HIServiceException;
import org.hyperimage.service.ws.HIEditor;
import org.hyperimage.service.ws.HIEditor.WSstates;

/**
 * @author Jens-Martin Loebel
 */
@Aspect
public class WSStateAspect {

	@Before("execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.* (..)) && " +
			"this(service) &&" +
			"!execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.createProject (..)) &&" +
			"!execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.setProject (..)) &&" +
			"!execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.getVersionID (..)) &&" +
			"!execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.sysop* (..)) &&" +
			"!execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.admin* (..)) &&" +
			"!execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.updateUser (..)) &&" +
			"!execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.updateUserPassword (..)) &&" +
			"!execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.getProjects (..))")
	public void checkProjectSelected(HIEditor service) throws HIServiceException {
		if ( service.getState() != WSstates.PROJECT_SELECTED )
			throw new HIServiceException("<b>you need to select a project first!</b>");			
	}

}
