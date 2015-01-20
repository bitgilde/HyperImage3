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

import java.util.Scanner;
import java.util.Vector;

import org.hyperimage.service.model.HIRichTextChunk.chunkTypes;

/**
 * @author Jens-Martin Loebel
 */
public class HIRichText {
		
	private static Vector<HIRichTextChunk> chunks = new Vector<HIRichTextChunk>();
	
	protected HIRichText() {
	}

	protected HIRichText(String model) {
		modelToWrapper(model);
	}

	public Vector<HIRichTextChunk> getChunks() {
		return chunks;
	}
	
	public String getModel() {
		return wrapperToModel();
	}
	
	public static String getPlainTextModel(String model) {
		String output = "";
		modelToWrapper(model);

		for ( HIRichTextChunk chunk : chunks ) {
			output = output + chunk.getValue();
			if ( chunk.getChunkType() == chunkTypes.LINK )
				output = output + " "+chunk.getRef()+" ";
		}

		return output;
	}

	public String getHTMLModel() {
		return wrapperToHTMLModel();
	}

	public static void addChunk(HIRichTextChunk.chunkTypes chunkType, String value) {
		chunks.add(new HIRichTextChunk( chunkType, value ));
	}

	public static void addChunk(HIRichTextChunk.chunkTypes chunkType, String value, String ref) {
		chunks.add(new HIRichTextChunk( chunkType, value, ref ));
	}

	public static void addChunk(HIRichTextChunk chunk) {
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
	
	private static void modelToWrapper(String model) {
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
					else if ( key.startsWith("link") ) addChunk(HIRichTextChunk.chunkTypes.LINK,value, ref);
					else addChunk(HIRichTextChunk.chunkTypes.REGULAR,value);
				}
			}

	}


}
