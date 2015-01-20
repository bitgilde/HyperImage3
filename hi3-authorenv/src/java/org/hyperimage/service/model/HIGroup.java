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
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Jens-Martin Loebel
 */
@Entity
public class HIGroup extends HIBase {

	public static enum GroupTypes {HIGROUP_REGULAR, HIGROUP_IMPORT, HIGROUP_TRASH, HIGROUP_TAG};

	private boolean visible;
	
	private GroupTypes type;
	
	// sort property is interpreted and maintained by client
	@Column(columnDefinition="TEXT", length=128000, nullable=false)
	private String sortOrder;
	
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.REFRESH}, targetEntity=HIBase.class)
	private List<HIBase> contents;
	
	
	protected HIGroup() {
            // default constructor for persistence
            this.uuid = UUID.randomUUID().toString();
            touchTimestamp();
	}
	
	public HIGroup(GroupTypes type, boolean visible) {
            this( type, visible, null );
	}
        
	public HIGroup(GroupTypes type, boolean visible, String uuid) {
            this.type = type;
            this.visible = visible;
            this.contents = new ArrayList<HIBase>();
            this.metadata = new ArrayList<HIFlexMetadataRecord>();
            this.sortOrder = "";
            if ( uuid == null ) this.uuid = UUID.randomUUID().toString();
            else this.uuid = uuid;
            touchTimestamp();
        }

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public boolean isVisible() {
		return visible;
	}


	public void setVisible(boolean visible) {
		this.visible = visible;
	}


	public GroupTypes getType() {
		return type;
	}


	public void setType(GroupTypes type) {
		this.type = type;
	}


	@XmlTransient
	public List<HIBase> getContents() {
		return contents;
	}

	public void setContents(List<HIBase> contents) {
		this.contents = contents;
	}

	public int findContentPosition(HIBase content) {
		int pos = -1;
		
		for ( int i=0; i < contents.size() ; i++  )
			if ( contents.get(i).getId() == content.getId() )
				pos = i;
		
		return pos;
	}
	
	public boolean contains(HIBase base) {
		if ( base == null ) return false;
		for ( HIBase groupBase : contents  )
			if ( groupBase.getId() == base.getId() )
				return true;

		return false;
	}
	
	public boolean addContent(HIBase content) {
		if ( content == null ) return false;
	
		if ( contains(content) ) return false;

		contents.add(content);
		
		// set initial sort order
		this.sortOrder = this.sortOrder+","+content.getId();
		if ( this.sortOrder.startsWith(",") )
			this.sortOrder = this.sortOrder.substring(1);
		setSortOrder(this.sortOrder);
		
		return true;
	}
	public boolean removeContent(HIBase base) {
		boolean removed = false;
		int pos = findContentPosition(base);
		if ( pos >= 0 ) {
			this.contents.remove(pos);
			removed = true;

			// remove content from sort order index if possible
			String newOrder = "";
			for ( String sortPos : this.getSortOrder().split(",") ) {
				sortPos = sortPos.replaceAll(",", "");
				try {
					long sortId = Long.parseLong(sortPos);
					if ( sortId != base.getId() )
						newOrder = newOrder+","+sortPos;
				} catch (NumberFormatException nfe) {}
			}
			if ( newOrder.startsWith(",") )
				newOrder = newOrder.substring(1);
			if ( newOrder.endsWith(",") )
				newOrder = newOrder.substring(0, newOrder.length()-1);
			
			setSortOrder(newOrder);
		}

		return removed;
	}



	

	public boolean equals(Object object) {
		if (object != null ) 
			if ( object instanceof HIGroup )
				if ( ((HIGroup)object).getId() == this.id )
					return true;
		
		return false;
	}

	
}
