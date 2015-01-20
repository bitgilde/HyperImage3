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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Jens-Martin Loebel
 */
@Entity
public class HIObject extends HIBase {

	@OneToMany(cascade=CascadeType.ALL, mappedBy="object", targetEntity=HIObjectContent.class)
	private List<HIObjectContent> views;
	
	@OneToOne(targetEntity=HIObjectContent.class, optional= true)
	private HIObjectContent defaultView;

	// sort property is interpreted and maintained by client
	@Column(columnDefinition="TEXT", length=128000, nullable=false)
	private String sortOrder; // object content (view/inscription) sort order

        
        public HIObject() {
            this(null);
        }

	public HIObject(String uuid) {
            metadata = new ArrayList<HIFlexMetadataRecord>();
            views = new ArrayList<HIObjectContent>();
            sortOrder = "";
            if ( uuid == null ) this.uuid = UUID.randomUUID().toString();
            else this.uuid = uuid;
            touchTimestamp();
	}
	
	public List<HIObjectContent> getViews() {
		return views;
	}

	public void setViews(List<HIObjectContent> views) {
		this.views = views;
	}

	
	public boolean containsView(HIObjectContent view) {
		if ( view == null )
			return false;
		
		if ( !(view instanceof HIView) && !(view instanceof HIInscription) )
			return false;
		
		for ( HIBase objView : views )
			if ( objView.getId() == view.getId() )
				return true;
		
		return false;
	}
	
	public void addView(HIObjectContent view) {
		if ( view == null )
			return;

		if ( containsView(view) )
			return;

		views.add(view);
		
		// set initial sort order
		this.sortOrder = this.sortOrder+","+view.getId();
		if ( this.sortOrder.startsWith(",") )
			this.sortOrder = this.sortOrder.substring(1);
		setSortOrder(this.sortOrder);
	}

	public void removeView(HIObjectContent view) {
		if ( view == null )
			return;

		if ( !containsView(view) )
			return;
		
		views.remove(view);

		if ( defaultView != null )
			if ( defaultView.getId() == view.getId() )
				defaultView = null;
		
		// remove view from sort order index if possible
		String newOrder = "";
		for ( String sortPos : this.getSortOrder().split(",") ) {
			sortPos = sortPos.replaceAll(",", "");
			try {
				long sortId = Long.parseLong(sortPos);
				if ( sortId != view.getId() )
					newOrder = newOrder+","+sortPos;
			} catch (NumberFormatException nfe) {}
		}
		if ( newOrder.startsWith(",") )
			newOrder = newOrder.substring(1);
		if ( newOrder.endsWith(",") )
			newOrder = newOrder.substring(0, newOrder.length()-1);

	}

	public HIObjectContent getDefaultView() {
		return defaultView;
	}

	public void setDefaultView(HIObjectContent defaultView) {
		if ( defaultView == null )
			this.defaultView = null;
		
		if ( containsView(defaultView) )
			this.defaultView = defaultView;
	}
	
	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	
	@Transient
	@XmlTransient
	public List<HIObjectContent> getSortedViews() {
		List<HIObjectContent> sortedViews = new ArrayList<HIObjectContent>(getViews().size());
		for ( HIObjectContent content : getViews() )
			sortedViews.add(content);
		
		long contentID;
		int index = 0;
		int contentIndex;

		// parse sort order string (don´t trust user input)
		for ( String contentIDString : getSortOrder().split(",") ) {
			try {
				contentID = Long.parseLong(contentIDString);
				// find content belonging to the parsed id
				contentIndex = -1;
				
				for ( int i=0; i<sortedViews.size(); i++ )
					if ( sortedViews.get(i).getId() == contentID )
						contentIndex = i;
				// if content was found, sort to new index
				if ( contentIndex >= 0 ) if ( contentIndex != index ) {
					HIObjectContent content = sortedViews.get(contentIndex);
					sortedViews.remove(contentIndex);
                                        if (index > sortedViews.size()) index=sortedViews.size();
					sortedViews.add(index, content);
					index = index+1;
				} else index = index + 1;
			} catch (NumberFormatException e) {}; // user messed with sort order string format, no problem
		}
		
		return sortedViews;
	}

	


	public boolean equals(Object object) {
		if (object != null ) 
			if ( object instanceof HIView )
				if ( ((HIView)object).getId() == this.id )
					return true;
		
		return false;
	}

	
	
	
}
