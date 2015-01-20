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

package org.hyperimage.client.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.w3c.dom.Document;

/**
 * @author Jens-Martin Loebel
 */
public class HIRichTextChunk {
	public static enum chunkTypes {REGULAR, BOLD, ITALIC, UNDERLINE, LINK };
	
	private chunkTypes chunkType;
	private String value, ref;
		
	public HIRichTextChunk(chunkTypes chunkType, String value) {
		this(chunkType, value, null);
	}
	
	public HIRichTextChunk(chunkTypes chunkType, String value, String ref) {
		this.chunkType = chunkType;
		this.value = value;
		this.value = value.replaceAll("\\{\\\\#\\}", "{#}");
        this.ref = null;
        if ( chunkType == chunkTypes.LINK )
            this.ref = ref;
	}

	
	public chunkTypes getChunkType() {
		return chunkType;
	}
	public void setChunkType(chunkTypes chunkType) {
		this.chunkType = chunkType;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public String getRef() {
		return ref;
	}
	public void setRef(String ref) {
		this.ref = ref;
	}
	
	public String getModel() {
		String key = "regular";
		
		if ( chunkType == chunkTypes.BOLD) key = "bold";
		else if ( chunkType == chunkTypes.ITALIC) key = "italic";
		else if ( chunkType == chunkTypes.UNDERLINE) key = "underline";
		else if ( chunkType == chunkTypes.LINK) key = "link:"+ref;
		
		return "{#}"+key+"{#}"+value.replaceAll("\\{#\\}", "{\\\\#}");
	}

	// TODO: complete
	public String getHTMLModel() {
            String key = "";

            if ( chunkType == chunkTypes.BOLD) key = "b";
            else if ( chunkType == chunkTypes.ITALIC) key = "i";
            else if ( chunkType == chunkTypes.UNDERLINE) key = "u";
		
            String text = value;
    		text = text.replaceAll("<", "&lt;");
    		text = text.replaceAll(">", "&gt;");    
            if ( key.length() > 0 )
                text = "<"+key+">"+text+"</"+key+">";
            else if ( chunkType == chunkTypes.LINK)
                text = "<a href=\"PeTAL://"+ref+"\">"+text+"</a>";

            return text;
	}
		

}
