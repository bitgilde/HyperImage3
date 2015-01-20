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

import java.net.URLConnection;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Jens-Martin Loebel
 */
@Entity
@Table(name="hiview")
public class HIView extends HIObjectContent {

	@OneToMany(mappedBy="view", cascade=CascadeType.ALL, targetEntity=HILayer.class)
	List<HILayer> layers;

	@Column(nullable=false)
	String filename;

	@Column(nullable=false)
	String hash;
		
	// sort property is interpreted and maintained by client
	@Column(columnDefinition="TEXT", length=128000, nullable=false)
	String sortOrder; // layer sort order (z-order)

	@Transient
	String mimeType;
	
	int width = 0;
	
	int height = 0;
	
	@Column(nullable=false)
	String repositoryID; // source repository identifier string

	
	public HIView() {
            // default constructor for persistence
            sortOrder = "";
            repositoryID = "";
            this.uuid = UUID.randomUUID().toString();
            touchTimestamp();
	}

        public HIView(String filename, String hash, String repositoryID, HIObject object) {
            this(filename, hash, repositoryID, object, null);
        }
        
	public HIView(String filename, String hash, String repositoryID, HIObject object, String uuid) {
            this.filename = filename;
            this.hash = hash;
            this.repositoryID = repositoryID;
            this.object = object;
            this.mimeType = URLConnection.guessContentTypeFromName(filename);
            // MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename);
            this.sortOrder = "";
            if ( uuid == null ) this.uuid = UUID.randomUUID().toString();
            else this.uuid = uuid;
            touchTimestamp();
	}
	
	
	public List<HILayer> getLayers() {
		return layers;
	}

	public void setLayers(List<HILayer> layers) {
		this.layers = layers;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getRepositoryID() {
		return repositoryID;
	}

	public void setRepositoryID(String repositoryID) {
		this.repositoryID = repositoryID;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	

	public boolean equals(Object object) {
		if (object != null ) 
			if ( object instanceof HIView )
				if ( ((HIView)object).getId() == this.id )
					return true;
		
		return false;
	}

	
}
