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
public class HIURL extends HIBase {


	@Column(nullable=false, length=65535)
	String url;
	
	@Column(nullable=false, length=65535)
	String title;
	
	@Column(nullable=false, columnDefinition="TEXT", length=128000)
	String lastAccess;
	
	
	public HIURL() {
            // default constructor for persistence
            this.uuid = UUID.randomUUID().toString();
            touchTimestamp();
	}
	
	public HIURL(String url, String title, String lastAccess) {
            this(url, title, lastAccess, null);
        }
        
	public HIURL(String url, String title, String lastAccess, String uuid) {
            this.url = url;
            this.title = title;
            this.lastAccess = lastAccess;
            if ( uuid == null ) this.uuid = UUID.randomUUID().toString();
            else this.uuid = uuid;
            touchTimestamp();
	}
	

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(String lastAccess) {
		this.lastAccess = lastAccess;
	}
	
	
	
}
