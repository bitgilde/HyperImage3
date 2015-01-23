/* $Id: DecoratedHIProject.java 18 2009-01-27 12:16:13Z hgkuper $ */

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

/*
 * Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
 * All rights reserved. Use is subject to license terms.
 * http://bitgilde.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For further information on HyperImage visit http://hyperimage.ws/
 */

package org.hyperimage.sysop.client.utility;

import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiProject;

/**
 * @author Heinz-Guenter Kuper
 */
public class DecoratedHIProject {
	private HiProject m_project = null;
	
	public DecoratedHIProject(HiProject project) {
		m_project = project;
	}
	
	@Override
	public String toString() {
		String strReturn = MetadataHelper.findValue(m_project, m_project.getDefaultLanguage().getLanguageId());
		
                if( strReturn == null )
                    strReturn = "null";
		
		if( strReturn.compareTo("") == 0 )
			strReturn = "Project " + m_project.getId();
		
		return strReturn;
	}
	
	public long getID() {
		return m_project.getId();
	}
}
