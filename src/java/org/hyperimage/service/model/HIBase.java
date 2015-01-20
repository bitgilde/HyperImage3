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
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Jens-Martin Loebel
 */
@Entity(name="HIBase")
@Inheritance(strategy=InheritanceType.JOINED)
@XmlSeeAlso({HIGroup.class, HIInscription.class, HILayer.class, HILightTable.class, HIObject.class, HIText.class, HIURL.class, HIView.class})
public class HIBase implements Serializable {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	long id;
        
        @Column(length = 36, nullable = true, unique = false)
        String uuid;
	
        @Column(nullable = false)
        long timestamp;
        
	@JoinColumn(nullable=false)
	@ManyToOne(cascade=CascadeType.PERSIST,targetEntity=HIProject.class,optional=false)
	HIProject project;

	@OneToMany(mappedBy="owner", targetEntity=HIFlexMetadataRecord.class, cascade=CascadeType.ALL)
	List<HIFlexMetadataRecord> metadata;

/*        
    @Transient
    String elementType = this.getClass().getSimpleName();
    
    @Transient
    public String getElementType() {
        return this.elementType;
    }
    
    public void setElementType(String elementType) {
        this.elementType = elementType;
    }
*/        
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
        
        public String getUUID() {
            return this.uuid;
        }
        
        public void setUUID(String uuid) {
            this.uuid = uuid;
        }
        
	@XmlTransient
	public HIProject getProject() {
		return project;
	}
        
        public long getTimestamp() {
            return this.timestamp;
        }
        
        public void setTimestamp(long timestamp) {
           this.timestamp = timestamp;
        }

	public void setProject(HIProject project) {
		this.project = project;
	}
	
	public List<HIFlexMetadataRecord> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<HIFlexMetadataRecord> metadata) {
		this.metadata = metadata;
	}

        public void touchTimestamp() {
            this.timestamp = new Date().getTime();
        }


}
