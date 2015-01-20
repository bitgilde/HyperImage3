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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Jens-Martin Loebel
 */
@Entity
public class HILayer extends HIBase {

	int red;
	int green;
	int blue;
	float opacity;
	
	@ManyToOne(targetEntity=HIView.class, cascade=CascadeType.PERSIST,optional=false)
	@XmlTransient
	HIView view;

	@JoinColumn(nullable=true)
	HIBase link;

	@Column(columnDefinition="TEXT", length=256000, nullable=false)
	String polygons;
	
	@Transient
	HIQuickInfo linkInfo; // link info property for client
	
	public HILayer() {
            this.uuid = UUID.randomUUID().toString();
            polygons = "";
            touchTimestamp();
	}
	
 	public HILayer(HIView view, int red, int green, int blue, float opacity) {
            this(view, red, green, blue, opacity, null);
        }
	
	public HILayer(HIView view, int red, int green, int blue, float opacity, String uuid) {
            polygons = "";
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.opacity = opacity;
		
            this.view = view;
            view.getLayers().add(this);
            if ( uuid == null ) this.uuid = UUID.randomUUID().toString();
            else this.uuid = uuid;
            touchTimestamp();
	}


	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	public String getPolygons() {
		return polygons;
	}

	public void setPolygons(String polygons) {
		this.polygons = polygons;
	}

	@XmlTransient
	public HIView getView() {
		return view;
	}

	public void setView(HIView view) {
		this.view = view;
	}

	@XmlTransient
	public HIBase getLink() {
		return link;
	}

	public void setLink(HIBase link) {
		this.link = link;
	}


	public HIQuickInfo getLinkInfo() {
		return this.linkInfo;
	}


	public void setLinkInfo(HIQuickInfo linkInfo) {
		this.linkInfo = linkInfo;
	}
	
	
	
	
}
