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
import org.hyperimage.service.exception.HIPrivilegeException;
import org.hyperimage.service.model.HIRole.HI_Roles;
import org.hyperimage.service.ws.HIEditor;

/**
 * @author Jens-Martin Loebel
 */
@Aspect
public class PrivilegesAspect {

	@Before("this(service) &&" +
			"(execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.create* (..)) || " +
			"execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.update* (..)) || " +
			"execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.delete* (..)) || " +
			"execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.copy* (..)) || " +
			"execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.move* (..)) || " +
			"execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.add* (..)) || " +
	"execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.remove* (..)))")
	public void checkEditingPrivileges(HIEditor service) throws HIPrivilegeException {
		if ( service.getRole() != null )
			if ( service.getRole() == HI_Roles.GUEST )
				throw new HIPrivilegeException("<b>Access denied! Guests may not modify project content.</b>");
	}

	@Before("this(service) &&" +
	"(execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.sysop* (..)))")
	public void checkSysopPrivileges(HIEditor service) throws HIPrivilegeException {
		if ( service.getRole() != null )
			if ( !service.isSysop() )
				throw new HIPrivilegeException("<b>Access denied! This method may only be invoked by the sysop user.</b>");
	}

	@Before("this(service) &&" +
	"(execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.admin* (..)))")
	public void checkAdminPrivileges(HIEditor service) throws HIPrivilegeException {
		if ( service.getRole() != null )
			if ( service.getRole() != HI_Roles.ADMIN && !service.isSysop() )
				throw new HIPrivilegeException("<b>Access denied! Only Project Admins may invoke this method.</b>");
	}

}
