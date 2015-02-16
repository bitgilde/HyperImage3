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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hyperimage.service.exception.HIMaintenanceModeException;
import org.hyperimage.service.model.HIUser;
import org.hyperimage.service.util.ServerPreferences;
import org.hyperimage.service.ws.HIEditor;

/**
 * @author Jens-Martin Loebel
 */
@Aspect
public class MaintenanceModeAspect {

        private static ServerPreferences prefs = new ServerPreferences();
        
	@Before("this(service) &&" +
			"(execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.* (..))" +
			"&& !execution(@javax.jws.WebMethod * org.hyperimage.service.ws.HIEditor.getVersionID (..)))" +
			")")
	public void checkMaintenanceLoginPrivileges(HIEditor service) throws HIMaintenanceModeException {
		checkMaintenanceMode(service.getCurrentUser());
	}
	
	public static void checkMaintenanceMode(HIUser user) throws HIMaintenanceModeException {
		checkMaintenanceMode(user.getUserName());
	}

	public static void checkMaintenanceMode(String userName) throws HIMaintenanceModeException {
		/* *****************************************
		 * check if service is in maintenance mode
		 * if so, only allow the sysop user to login
		 * *****************************************/
		                
                File maintenance;
                if ( prefs == null ) maintenance = new File("/Users/Shared/HIStore/hi3_maintenance.txt");
                else {
                    String historeDir = prefs.getHIStorePref();
                    if ( !historeDir.endsWith("/") ) {
                        historeDir = historeDir+"/";
                    }
                    maintenance = new File(historeDir + "hi3_maintenance.txt");
                }
		if ( maintenance.exists() && maintenance.canRead() ) {
			String explanation = "";
			try {
				BufferedReader reader = new BufferedReader(new FileReader(maintenance));
				explanation = reader.readLine();
			} catch (Exception e) {
				explanation = "Maintenance Mode active! Only sysop user may login.";
			}
			// only permit the sysop user to proceed
			if ( userName.compareTo("sysop") != 0 ) 
				throw new HIMaintenanceModeException(explanation);
		}
	}
}
