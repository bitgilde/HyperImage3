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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Jens-Martin Loebel
 */
@Entity
@Table(name="hiflexmetadatarecord")
public class HIFlexMetadataRecord implements Serializable {
		
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	long id;
	
	@Column(nullable=false)
	String language;
		
	@OneToMany(targetEntity=HIKeyValue.class, mappedBy="parent", cascade=CascadeType.ALL)
	List<HIKeyValue> contents;

	@ManyToOne(optional=false, targetEntity=HIBase.class, cascade=CascadeType.PERSIST)
	HIBase owner;
	

	public HIFlexMetadataRecord() {
		// default constructor for persistence
	}
	
	public HIFlexMetadataRecord(String language, HIBase owner) {
		this.language = language;
		this.contents = new ArrayList<HIKeyValue>();
		this.owner = owner;
	}
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public List<HIKeyValue> getContents() {
		return contents;
	}

	public void setContents(List<HIKeyValue> contents) {
		this.contents = contents;
	}
		
	@XmlTransient
	public HIBase getOwner() {
		return owner;
	}

	public void setOwner(HIBase owner) {
		this.owner = owner;
	}
	
	
	public void addEntry(HIKeyValue entry) {
		entry.setParent(this);
		this.contents.add(entry);
	}

	public String findValue(String template, String key) {
		String value = null;
		
		for (HIKeyValue kvPair : this.contents )
			if ( kvPair.getKey().compareTo(template+"."+key) == 0 )
				value = kvPair.getValue();
		
		return value;
	}

	
}
