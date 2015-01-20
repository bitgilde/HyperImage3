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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Jens-Martin Loebel
 */
@Entity
public class HIFlexMetadataSet {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	long id;
	
	boolean richText;
	
	String tagname;
	
	@ManyToOne(cascade=CascadeType.PERSIST,optional=false,targetEntity=HIFlexMetadataTemplate.class)
	HIFlexMetadataTemplate template;
	
	@OneToMany(cascade=CascadeType.ALL,mappedBy="set",targetEntity=HIFlexMetadataName.class)
	List<HIFlexMetadataName> displayNames;

	
	protected HIFlexMetadataSet() {
		// default constructor for persistence
	}
	
	public HIFlexMetadataSet(String tagname, HIFlexMetadataTemplate template, boolean richText) {
		this.tagname = tagname;
		this.template = template;
		this.richText = richText;
		this.displayNames = new ArrayList<HIFlexMetadataName>();
	}


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTagname() {
		return tagname;
	}

	public void setTagname(String tagname) {
		this.tagname = tagname;
	}
	
	@XmlTransient
	public HIFlexMetadataTemplate getTemplate() {
		return template;
	}

	public void setTemplate(HIFlexMetadataTemplate template) {
		this.template = template;
	}

	public List<HIFlexMetadataName> getDisplayNames() {
		return displayNames;
	}

	public void setDisplayNames(List<HIFlexMetadataName> displayNames) {
		this.displayNames = displayNames;
	}

	public boolean isRichText() {
		return richText;
	}

	public void setRichText(boolean richText) {
		this.richText = richText;
	}
	
	
}
