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
 * Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
 * All rights reserved. Use is subject to license terms.
 * http://bitgilde.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For further information on HyperImage visit http://hyperimage.ws/
 */

package org.hyperimage.client.model;

import java.util.Scanner;
import java.util.Vector;

/**
 * @author Jens-Martin Loebel
 */
public class HIRichText {
		
	private Vector<HIRichTextChunk> chunks;
	
	public HIRichText() {
		chunks = new Vector<HIRichTextChunk>();
	}

	public HIRichText(String model) {
		chunks = new Vector<HIRichTextChunk>();
		modelToWrapper(model);
	}

	public Vector<HIRichTextChunk> getChunks() {
		return chunks;
	}
	
	public String getModel() {
		return wrapperToModel();
	}

	public String getHTMLModel() {
		return wrapperToHTMLModel();
	}

	public void addChunk(HIRichTextChunk.chunkTypes chunkType, String value) {
		chunks.add(new HIRichTextChunk( chunkType, value ));
	}

	public void addChunk(HIRichTextChunk.chunkTypes chunkType, String value, String ref) {
		chunks.add(new HIRichTextChunk( chunkType, value, ref ));
	}

	public void addChunk(HIRichTextChunk chunk) {
		chunks.add(chunk);
	}

	private String wrapperToModel() {
		String model = "";

		for ( HIRichTextChunk chunk : chunks )
			model = model + chunk.getModel();

		return model;
	}

	private String wrapperToHTMLModel() {
		String model = "";

		for ( HIRichTextChunk chunk : chunks )
			model = model + chunk.getHTMLModel();

		model = model.replaceAll("\n", "<br>");
		return "<html>"+model+"</html>";
	}
	
	private void modelToWrapper(String model) {
		chunks.removeAllElements();
		String key, ref=null, value;
	
		if ( model == null )
			return;
		
			Scanner scanner = new Scanner(model).useDelimiter("\\{#\\}");
			while ( scanner.hasNext() ) {
				key = scanner.next();
				if ( key.indexOf(":") != -1 ) ref = key.split(":",2)[1];
				if ( scanner.hasNext() ) { 
					value = scanner.next();
					if ( key.compareTo("bold") == 0 ) addChunk(HIRichTextChunk.chunkTypes.BOLD, value);
					else if ( key.compareTo("italic") == 0 ) addChunk(HIRichTextChunk.chunkTypes.ITALIC, value);
					else if ( key.compareTo("underline") == 0 ) addChunk(HIRichTextChunk.chunkTypes.UNDERLINE, value);
					else if ( key.compareTo("subscript") == 0 ) addChunk(HIRichTextChunk.chunkTypes.SUBSCRIPT, value);
					else if ( key.compareTo("superscript") == 0 ) addChunk(HIRichTextChunk.chunkTypes.SUPERSCRIPT, value);
					else if ( key.compareTo("literal") == 0 ) addChunk(HIRichTextChunk.chunkTypes.LITERAL, value);
					else if ( key.startsWith("link") ) addChunk(HIRichTextChunk.chunkTypes.LINK,value, ref);
					else addChunk(HIRichTextChunk.chunkTypes.REGULAR,value);
				}
			}

	}


}
