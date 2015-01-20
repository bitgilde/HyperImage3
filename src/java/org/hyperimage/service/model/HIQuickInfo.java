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

/**
 * @author Jens-Martin Loebel
 */
public class HIQuickInfo {

	public static enum HI_BaseTypes {HIGroup, HIInscription, HILayer, HIObject, HIText, HIURL, HILightTable, HIView, HIRepositoryItem};

	private long baseID;
	private HI_BaseTypes contentType;
	private String title;
	private String preview;
	private long relatedID;
	private int count;
        private String uuid;
	

	public HIQuickInfo () {
		// default constructor
	}

        public HIQuickInfo(HIBase base, String title, String preview, int count, long relatedID) {
		this.baseID = base.getId();
		this.contentType = baseToContentType(base);
		this.title = title;
		this.preview = preview;
		this.count = count;
		this.relatedID = relatedID;
                this.uuid = base.getUUID();
	}

	public HIQuickInfo(HIBase base) {
		this(base, "", null, 0, 0);
        }

	public static HI_BaseTypes baseToContentType(HIBase base) {
		HI_BaseTypes type = null;

		if ( base instanceof HIGroup )
			type = HI_BaseTypes.HIGroup;
		else if ( base instanceof HIInscription )
			type = HI_BaseTypes.HIInscription;
		else if ( base instanceof HILayer )
			type = HI_BaseTypes.HILayer;
		else if ( base instanceof HILightTable )
			type = HI_BaseTypes.HILightTable;
		else if ( base instanceof HIObject )
			type = HI_BaseTypes.HIObject;
		else if ( base instanceof HIText )
			type = HI_BaseTypes.HIText;
		else if ( base instanceof HIURL )
			type = HI_BaseTypes.HIURL;
		else if ( base instanceof HIView )
			type = HI_BaseTypes.HIView;
		
		return type;
	}

	public long getBaseID() {
		return baseID;
	}

	public void setBaseID(long baseID) {
		this.baseID = baseID;
	}

	public HI_BaseTypes getContentType() {
		return contentType;
	}

	public void setContentType(HI_BaseTypes contentType) {
		this.contentType = contentType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

	public long getRelatedID() {
		return relatedID;
	}

	public void setRelatedID(long relatedID) {
		this.relatedID = relatedID;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
        
        public String getUUID() {
            return this.uuid;
        }

        public void setUUID(String uuid) {
            this.uuid = uuid;
        }
	
	
}
