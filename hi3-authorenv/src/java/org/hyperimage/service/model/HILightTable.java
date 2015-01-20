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
 * Copyright 2014 Leuphana Universität Lüneburg
 * All rights reserved.  Use is subject to license terms.
 */

package org.hyperimage.service.model;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Jens-Martin Loebel
 */
@Entity
public class HILightTable extends HIBase {

	private String title;
	
	@Column(nullable=false, columnDefinition="TEXT", length=256000)
	private String xml;
	

	public HILightTable() {
            // default constructor for persistence
            this.uuid = UUID.randomUUID().toString();
            touchTimestamp();
	}

        public HILightTable(String title, String xml) {
            this(title, xml, null);
        }
        
	public HILightTable(String title, String xml, String uuid) {
            this.title = title;
            this.xml = xml;
            if ( uuid == null ) this.uuid = UUID.randomUUID().toString();
            else this.uuid = uuid;
            touchTimestamp();
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getXml() {
		return xml;
	}


	public void setXml(String xml) {
		this.xml = xml;
	}

	
	
}
