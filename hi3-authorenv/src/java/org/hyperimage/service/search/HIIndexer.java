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

package org.hyperimage.service.search;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.hyperimage.service.model.HIBase;
import org.hyperimage.service.model.HIFlexMetadataSet;
import org.hyperimage.service.model.HIFlexMetadataTemplate;
import org.hyperimage.service.model.HILanguage;
import org.hyperimage.service.model.HILightTable;
import org.hyperimage.service.model.HIObject;
import org.hyperimage.service.model.HIProject;
import org.hyperimage.service.model.HIRichText;
import org.hyperimage.service.model.HIURL;
import org.hyperimage.service.util.MetadataHelper;

/**
 * DEBUG This class is experimental. NO exception handling is taking place.
 * This is a proof of concept to include Lucene search capability in HyperImage.
 * Class: HIIndexer
 * Package: org.hyperimage.service.search
 * @author Jens-Martin Loebel
 *
 */
public class HIIndexer {

	private IndexWriter hiWriter;
	private String directoryLocation;

	public HIIndexer(String directoryLocation) {

		this.directoryLocation = directoryLocation;
		if ( ! this.directoryLocation.endsWith("/") )
			this.directoryLocation = this.directoryLocation+"/";
	}
	
	public Vector<Long> simpleSearch(String text, HIProject project, String language) {
		Vector<Long> results = new Vector<Long>();
		
		HIHitCollector collector = new HIHitCollector();
		QueryParser parser = new QueryParser("all", new StandardAnalyzer());
		try {
			
			prepDir(project);
			Query query = parser.parse("all."+language+":\""+text+"\"");
			IndexSearcher searcher = new IndexSearcher(getDir(project));
			searcher.search(query, collector);
			
			IndexReader reader = IndexReader.open(getDir(project));
			for ( int doc : collector.getDocs() ) {
				long baseID = Long.parseLong(reader.document( doc ).get("id"));
				if ( ! results.contains(baseID) )
					results.addElement(baseID);
			}
			searcher.close();
			reader.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return results;
	}


	public Vector<Long> fieldSearch(List<String> fields, List<String> contents, HIProject project, String language) {
		Vector<Long> results = new Vector<Long>();
		
		HIHitCollector collector = new HIHitCollector();
		QueryParser parser = new QueryParser("all", new StandardAnalyzer());

		String queryString = "";
		for ( int i = 0; i < fields.size(); i++ ) {
			// sanitize user input
			String field = fields.get(i);
			String content = contents.get(i);
			field = field.replaceAll("[^a-zA-Z0-9.]", "");
			content = content.replaceAll("\"", "\\\\\"");
			// convert displayable ids to DB id for id search
			if ( field.equalsIgnoreCase("id") )
				content = content.replaceAll("[^0-9]", "");
			
			if ( !field.equalsIgnoreCase("project") ) {
				if ( queryString.length() > 0 ) queryString = queryString + " AND ";
				if ( field.equalsIgnoreCase("id") ) // don´t add language to id search
					queryString = queryString + field+":\""+content+"\"";
				else
					queryString = queryString + field+"."+language+":\""+content+"\"";
			}
		}
		queryString = queryString.trim();
		if ( queryString.length() == 0 ) return results;

		try {
			prepDir(project);
			Query query = parser.parse(queryString);
			IndexSearcher searcher = new IndexSearcher(getDir(project));
			searcher.search(query, collector);
			
			IndexReader reader = IndexReader.open(getDir(project));
			for ( int doc : collector.getDocs() ) {
				long baseID = Long.parseLong(reader.document( doc ).get("id"));
				if ( ! results.contains(baseID) )
					results.addElement(baseID);
			}
			searcher.close();
			reader.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return results;
	}
	
	

	public void storeElement(HIBase base, HIProject project) {
		if ( base == null ) return;

		Document baseDoc = getOrCreateDocument(base, project);
		String all;
		
		// store base id
		addOrUpdateField(baseDoc, "id", Long.toString(base.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED);			
		// store base type
		addOrUpdateField(baseDoc, "type", base.getClass().getName(), Field.Store.YES, Field.Index.NOT_ANALYZED);			

		for ( HILanguage lang : project.getLanguages() ) {
			all = "";
			if ( base instanceof HIURL ) {
				/* *****
				 * handle urls
				 * *****/
				HIURL url = (HIURL)base;

				addOrUpdateField(baseDoc, "url."+lang.getLanguageId(), url.getUrl(), Field.Store.YES, Field.Index.NOT_ANALYZED);			
				all = all + " " + url.getUrl();
				addOrUpdateField(baseDoc, "lastAccess."+lang.getLanguageId(), HIRichText.getPlainTextModel(url.getLastAccess()));
				all = all + " " + HIRichText.getPlainTextModel(url.getLastAccess());			
			} else if ( base instanceof HILightTable ) {
				/* *****
				 * handle light tables
				 * *****/
				HILightTable lightTable = (HILightTable)base;
				addOrUpdateField(baseDoc, "title."+lang.getLanguageId(), lightTable.getTitle());
				all = all + " " + lightTable.getTitle();
				addOrUpdateField(baseDoc, "lastAccess."+lang.getLanguageId(), lightTable.getXml());
				all = all + " " + lightTable.getXml();
			} else if ( ! (base instanceof HIObject) ) {
				/* *****
				 * handle base fields
				 * *****/
				HIFlexMetadataTemplate template = null;
				for ( HIFlexMetadataTemplate projTemplate : project.getTemplates() )
					if ( projTemplate.getNamespacePrefix().compareTo("HIBase") == 0 )
						template = projTemplate;

				if ( template != null ) {
					for ( HIFlexMetadataSet set : template.getEntries() ) {
						String value;
						value = MetadataHelper.findValue(template, set.getTagname(), 
								MetadataHelper.getDefaultMetadataRecord(base, lang)
						);
						if ( set.isRichText() )
							value = HIRichText.getPlainTextModel(value);					
						addOrUpdateField(baseDoc, "HIBase."+set.getTagname()+"."+lang.getLanguageId(), value);
						all = all + " " + value;
					}
				}
			} else {
				/* *****
				 * handle objects
				 * *****/
				for ( HIFlexMetadataTemplate template : project.getTemplates() )
					for ( HIFlexMetadataSet set : template.getEntries() ) {
						String value;
						value = MetadataHelper.findValue(template, set.getTagname(), 
								MetadataHelper.getDefaultMetadataRecord(base, lang)
						);
						if ( set.isRichText() )
							value = HIRichText.getPlainTextModel(value);					
						addOrUpdateField(baseDoc, template.getNamespacePrefix()+"."+set.getTagname()+"."+lang.getLanguageId(), value);
						all = all + " " + value;
					}
			}

			// store the all field
			all = all.trim();
			addOrUpdateField(baseDoc, "all."+lang.getLanguageId(), all);
		}

		// store document in index
		try {
			prepDir(project);
			
			hiWriter = new IndexWriter(
					getDir(project),
					new StandardAnalyzer(),
					IndexWriter.MaxFieldLength.UNLIMITED
			);
			
			hiWriter.updateDocument(new Term("id",Long.toString(base.getId())), baseDoc);
			hiWriter.expungeDeletes();
			hiWriter.commit();
			hiWriter.close(true);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

	}

	public void removeElement(HIBase base, HIProject project) {
		if ( base == null ) return;
		try {
			prepDir(project);
			
			hiWriter = new IndexWriter(
					getDir(project),
					new StandardAnalyzer(),
					IndexWriter.MaxFieldLength.UNLIMITED
			);

			hiWriter.deleteDocuments(new Term("id",Long.toString(base.getId())));
			hiWriter.commit();
			hiWriter.expungeDeletes();
			hiWriter.close(true);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void initIndex(HIProject project) {
		prepDir(project);
		
		try {
			if ( IndexWriter.isLocked(getDir(project)) ) 
				IndexWriter.unlock(FSDirectory.getDirectory(getDir(project)));
			hiWriter = new IndexWriter(
					getDir(project),
					new StandardAnalyzer(),
					true,
					IndexWriter.MaxFieldLength.UNLIMITED
			);
			hiWriter.commit();
			hiWriter.close(true);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	

	private Document getOrCreateDocument(HIBase base, HIProject project) {
		Document baseDoc = null;
		
		try {
			prepDir(project);
			IndexReader reader = IndexReader.open(getDir(project));
			
			TermDocs docs = reader.termDocs(new Term("id", Long.toString(base.getId())));
			if ( docs.next() )
				baseDoc = reader.document(docs.doc());
			else
				baseDoc = new Document();

			reader.close();
			
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
		return baseDoc;
	}



	private void addOrUpdateField(Document document, String key, String value, Store store, Index analyze) {
		if ( value == null ) value = "";
		
		Field field = document.getField(key);
		if ( field == null )
			document.add(new Field(key, value, store, analyze));
		else
			field.setValue(value);
	}

	private void addOrUpdateField(Document document, String key, String value) {
		addOrUpdateField(document, key, value, Field.Store.NO, Field.Index.ANALYZED);
	}

	
	
	private String getDir(HIProject project) {
		return directoryLocation+"P"+project.getId()+File.separator+"search";
	}
	
	private boolean prepDir(HIProject project) {
		File dir = new File(getDir(project));
		if ( !dir.exists() )
			dir.mkdir();
		
		if ( !dir.exists() )
			return false;
		
		if ( !IndexReader.indexExists(getDir(project)) ) {
			// create index
			try {
				hiWriter = new IndexWriter(
						getDir(project),
						new StandardAnalyzer(),
						true,
						IndexWriter.MaxFieldLength.UNLIMITED
				);
				hiWriter.commit();
				hiWriter.close(true);
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (LockObtainFailedException e2) {
				e2.printStackTrace();
			} catch (IOException e3) {
				e3.printStackTrace();
			}
		}
		
		return true;
	}

}
