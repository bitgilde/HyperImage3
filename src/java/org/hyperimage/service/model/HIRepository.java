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

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Jens-Martin Loebel
 */
@Entity
public class HIRepository implements Serializable {

	public static enum CheckoutPermissions {
		CREATOR_ADMINS_USERS,
		CREATOR_ADMINS,
		CREATOR_ONLY,
	};
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	long id;
	
	@JoinColumn(nullable=false)
	@ManyToOne(cascade=CascadeType.PERSIST,targetEntity=HIProject.class,optional=false)
	private HIProject project;

	@JoinColumn(nullable=false)
	@ManyToOne(cascade=CascadeType.PERSIST,targetEntity=HIUser.class,optional=false)
	private HIUser creator;
	
	private String displayTitle;
	
	@Column(nullable=false)
	private String userName;

	@Column(nullable=false)
	private String password;

	@Column(nullable=false)
	private String url;

	@Column(nullable=false)
	private String repoType;

	@Column(nullable=false)
	private CheckoutPermissions checkoutPermission;

	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@XmlTransient
	public HIProject getProject() {
		return project;
	}

	public void setProject(HIProject project) {
		this.project = project;
	}

	public HIUser getCreator() {
		return creator;
	}

	public void setCreator(HIUser creator) {
		this.creator = creator;
	}

	public String getDisplayTitle() {
		return displayTitle;
	}

	public void setDisplayTitle(String displayTitle) {
		this.displayTitle = displayTitle;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRepoType() {
		return repoType;
	}

	public void setRepoType(String repoType) {
		this.repoType = repoType;
	}

	public CheckoutPermissions getCheckoutPermission() {
		return checkoutPermission;
	}

	public void setCheckoutPermission(CheckoutPermissions checkoutPermission) {
		this.checkoutPermission = checkoutPermission;
	}

	
	

}
