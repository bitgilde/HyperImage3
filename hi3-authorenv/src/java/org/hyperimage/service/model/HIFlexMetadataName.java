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

package org.hyperimage.service.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Jens-Martin Loebel
 */
@Entity
public class HIFlexMetadataName {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	long id;
	
	
	String displayName;
	
	String language;
	
	@ManyToOne(cascade=CascadeType.PERSIST,optional=false,targetEntity=HIFlexMetadataSet.class)
	HIFlexMetadataSet set;


	public HIFlexMetadataName() {
		// default constructor for persistence
	}
	
	public HIFlexMetadataName(String language, String displayName, HIFlexMetadataSet set) {
		this.language = language;
		this.displayName = displayName;
		this.set = set;
	}

	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@XmlTransient
	public HIFlexMetadataSet getSet() {
		return set;
	}

	public void setSet(HIFlexMetadataSet set) {
		this.set = set;
	}
}
