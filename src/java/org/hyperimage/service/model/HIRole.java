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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * @author Jens-Martin Loebel
 */
@Entity
public class HIRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    public static enum HI_Roles {

        GUEST, USER, ADMIN
    };

    HI_Roles type;

    @OneToOne
    HIUser user;

    @OneToOne
    HIProject project;

    public HIRole() {
        // default constructor for persistence
        this.type = HI_Roles.GUEST;
    }

    public HIRole(HIUser user, HIProject project, HI_Roles type) {
        this.user = user;
        this.project = project;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public HI_Roles getType() {
        return type;
    }

    public void setType(HI_Roles type) {
        this.type = type;
    }

    public HIUser getUser() {
        return user;
    }

    public void setUser(HIUser user) {
        this.user = user;
    }

    public HIProject getProject() {
        return project;
    }

    public void setProject(HIProject project) {
        this.project = project;
    }

}
