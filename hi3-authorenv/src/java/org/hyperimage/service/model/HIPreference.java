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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;


/**
 * @author Jens-Martin Loebel
 */
@Entity(name="HIPreference")
public class HIPreference {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	long id;
	
	@Column(nullable=false)
	String key;

	@Column(nullable=false, columnDefinition="TEXT", length=128000)
	String value;

	@ManyToOne(targetEntity=HIProject.class, cascade=CascadeType.PERSIST,optional=false)
	@XmlTransient
	HIProject project;
	

	
	public HIPreference() {
		// default constructor for persistence
		this.value = "";
	}

	public HIPreference(String key, String value, HIProject project) {
		this.key = key;
		this.value = value;
		this.project = project;
		project.getPreferences().add(this);
	}

	public HIPreference(String key, HIProject project) {
		this(key,"",project);
	}	

	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@XmlTransient
	public HIProject getProject() {
		return project;
	}

	public void setProject(HIProject project) {
		this.project = project;
	}

	
}
