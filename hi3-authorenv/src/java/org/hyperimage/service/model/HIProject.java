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
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Jens-Martin Loebel
 */
@Entity(name = "HIProject")
public class HIProject implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    
    @Column(nullable = false, name="quota")        
    long quota = 0;
    
    @Transient
    long used = 0;

    @JoinColumn(nullable = false)
    @OneToOne(cascade = CascadeType.ALL, targetEntity = HILanguage.class)
    HILanguage defaultLanguage;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "project", targetEntity = HILanguage.class)
    List<HILanguage> languages;

    @JoinColumn(nullable = true)
    @OneToOne
    HIBase startObject;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "project", targetEntity = HIFlexMetadataTemplate.class)
    List<HIFlexMetadataTemplate> templates;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, targetEntity = HIPreference.class)
    List<HIPreference> preferences;

    @OneToMany(mappedBy = "project", targetEntity = HIProjectMetadata.class, cascade = CascadeType.ALL)
    List<HIProjectMetadata> metadata;

    @Transient
    HIQuickInfo startObjectInfo; // link info property for client

    public HIProject() {
        this.languages = new ArrayList<HILanguage>();
        this.templates = new ArrayList<HIFlexMetadataTemplate>();
        this.metadata = new ArrayList<HIProjectMetadata>();
    }
    

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public long getQuota() {
        return quota;
    }
    
    public void setQuota(long quota) {
        this.quota = quota;
    }
    
    public long getUsed() {
        return used;
    }
    
    public void setUsed(long used) {
        this.used = used;
    }
 
    public HILanguage getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(HILanguage defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
        this.defaultLanguage.setProject(this);
    }

    public List<HILanguage> getLanguages() {
        return languages;
    }

    public void setLanguages(List<HILanguage> languages) {
        this.languages = languages;
    }

    @XmlTransient
    public HIBase getStartObject() {
        return startObject;
    }

    public void setStartObject(HIBase startObject) {
        this.startObject = startObject;
    }

    public List<HIFlexMetadataTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<HIFlexMetadataTemplate> templates) {
        this.templates = templates;
    }

    public List<HIPreference> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<HIPreference> preferences) {
        this.preferences = preferences;
    }

    public List<HIProjectMetadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<HIProjectMetadata> metadata) {
        this.metadata = metadata;
    }

    public HIQuickInfo getStartObjectInfo() {
        return startObjectInfo;
    }

    public void setStartObjectInfo(HIQuickInfo startObjectInfo) {
        this.startObjectInfo = startObjectInfo;
    }

    @Override
    public boolean equals(Object object) {
        if (object != null) {
            if (object instanceof HIProject) {
                if (((HIProject) object).getId() == this.id) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 29 * hash + (this.defaultLanguage != null ? this.defaultLanguage.hashCode() : 0);
        hash = 29 * hash + (this.languages != null ? this.languages.hashCode() : 0);
        hash = 29 * hash + (this.startObject != null ? this.startObject.hashCode() : 0);
        hash = 29 * hash + (this.templates != null ? this.templates.hashCode() : 0);
        hash = 29 * hash + (this.preferences != null ? this.preferences.hashCode() : 0);
        hash = 29 * hash + (this.metadata != null ? this.metadata.hashCode() : 0);
        hash = 29 * hash + (this.startObjectInfo != null ? this.startObjectInfo.hashCode() : 0);
        return hash;
    }

}
