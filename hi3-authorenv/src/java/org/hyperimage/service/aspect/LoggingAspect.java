/* $Id: LoggingAspect.java 4 2009-01-13 16:14:56Z jmloebel $ */

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

import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.hyperimage.service.exception.HIEntityNotFoundException;
import org.hyperimage.service.exception.HIMaintenanceModeException;
import org.hyperimage.service.exception.HIPrivilegeException;
import org.hyperimage.service.model.HIProject;
import org.hyperimage.service.model.HIQuickInfo;
import org.hyperimage.service.ws.HIEditor;

/**
 * @author Jens-Martin Loebel
 * @author Heinz-Guenter Kuper
 */
@Aspect
public class LoggingAspect {
	Logger logger = Logger.getLogger("org.hyperimage.service.ws.HIEditor");
	/*
	@Before("this(service) && args(id) && " +
			"( (execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.deleteFromProject (long)))" +
			"|| (execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.deleteGroup (long) ))" +
			")")
	*/
	public void log(JoinPoint thisJoinPoint, HIEditor service, long id) {
		Signature sig = thisJoinPoint.getSignature();
		HIProject proj = null;
		HIQuickInfo info = null;
		try {
			proj = service.getProject();
			info = service.getBaseQuickInfo(id);
			
			
		} catch (HIMaintenanceModeException e) {
			e.printStackTrace();
		} catch (HIEntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HIPrivilegeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if( proj == null ) {
			// do something useful, would you?
		}
		
		// how can I determine what exactly is being deleted, e.g. group id or object id?
		String infoString = "";
		if ( info != null ) // check if exception occurred
			infoString = "ID: "+info.getBaseID()+" - Type: "+info.getContentType()+" - Title: "+info.getTitle();
		
		logger.info(sig.getDeclaringTypeName() + "." + sig.getName() + ": User=" + service.getCurrentUser() + ", ProjectID="+ proj.getId()
				+" "+infoString);
	}
}
