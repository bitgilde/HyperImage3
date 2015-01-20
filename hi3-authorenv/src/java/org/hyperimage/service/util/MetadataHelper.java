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

package org.hyperimage.service.util;

import java.util.ArrayList;
import java.util.List;

import org.hyperimage.service.model.HIBase;
import org.hyperimage.service.model.HIFlexMetadataName;
import org.hyperimage.service.model.HIFlexMetadataRecord;
import org.hyperimage.service.model.HIFlexMetadataSet;
import org.hyperimage.service.model.HIFlexMetadataTemplate;
import org.hyperimage.service.model.HIKeyValue;
import org.hyperimage.service.model.HILanguage;
import org.hyperimage.service.model.HIPreference;
import org.hyperimage.service.model.HIProject;
import org.hyperimage.service.model.HIURL;

/**
 * @author Jens-Martin Loebel
 */
public class MetadataHelper {
	
	public static List<HIFlexMetadataRecord> resolveMetadataRecords(HIBase base) {
		List<HIFlexMetadataRecord> records = null;

		if ( ! (base instanceof HIURL) ) 
			records = base.getMetadata();
		else 
			records = new ArrayList<HIFlexMetadataRecord>();
		
		return records;
	}

	public static HIFlexMetadataRecord getDefaultMetadataRecord(HIBase base, HILanguage language) {
		return getDefaultMetadataRecord(base, language.getLanguageId());
	}

	public static HIFlexMetadataRecord getDefaultMetadataRecord(HIBase base, String language) {
		HIFlexMetadataRecord record = null;
		
		if ( base == null ) return record;
		if ( language == null ) return record;
		
		List<HIFlexMetadataRecord> records = resolveMetadataRecords(base);
		
		for ( HIFlexMetadataRecord rec : records )
			if ( rec.getLanguage().compareTo(language) == 0 )
				record = rec;
		
		return record;
	}
	
	public static String getTemplateKeyDisplayName(HIFlexMetadataTemplate template, String key, String guiLang) {
		String displayName = null;
		HIFlexMetadataSet keySet = null;

		// find key
		for ( HIFlexMetadataSet entry : template.getEntries())
			if ( entry.getTagname().compareTo(key) == 0 )
				keySet = entry;
		// key not found in template --> return
		if ( keySet == null )
			return key;

		// if no display name can be found use the tag name
		displayName = keySet.getTagname();

		// check known sets
		if ( template.getNamespacePrefix().compareTo("HIInternal") == 0 ) {
			// TODO - clean up for easier internationalization
			if ( guiLang.compareTo("de") == 0 ) {
				if ( key.compareTo("catchall") == 0 ) displayName="Zusatz";
				if ( key.compareTo("note") == 0 ) displayName="Notiz";
			}
		} else {		
			// search for correct display name
			for ( HIFlexMetadataName name : keySet.getDisplayNames() )
				if ( name.getLanguage().compareTo(guiLang) == 0 )
					displayName = name.getDisplayName();
		}

		return displayName;
	}
	
	public static String findValue(HIFlexMetadataTemplate template, String key, HIFlexMetadataRecord record) {
		return findValue(template.getNamespacePrefix(), key, record);
	}

	public static String findValue(String template, String key, HIFlexMetadataRecord record) {
		String value = null;

		if ( record == null )
			return null;

		for (HIKeyValue kvPair : record.getContents() )
			if ( kvPair.getKey().compareTo(template+"."+key) == 0 )
				value = kvPair.getValue();

		return value;
	}

	public static boolean setValue(HIFlexMetadataTemplate template, String key, String value, HIFlexMetadataRecord record) {		
		return setValue(template.getNamespacePrefix(), key, value, record);
	}

	public static boolean setValue(String template, String key, String value, HIFlexMetadataRecord record) {		
		for (HIKeyValue kvPair : record.getContents() )
			if ( kvPair.getKey().compareTo(template+"."+key) == 0 ) {
				kvPair.setValue(value);
				return true;
			}

		return false;
	}

	public static HIPreference findPreference(HIProject project, String key) {
		for ( HIPreference pref : project.getPreferences() )
			if ( pref.getKey().compareTo(key) == 0 )
				return pref;
		
		return null;
	}

	public static String findPreferenceValue(HIProject project, String key) {
		for ( HIPreference pref : project.getPreferences() )
			if ( pref.getKey().compareTo(key) == 0 )
				return pref.getValue();
		
		return null;
	}

	public static boolean setPreferenceValue(HIProject project, String key, String value) {
		for ( HIPreference pref : project.getPreferences() )
			if ( pref.getKey().compareTo(key) == 0 ) {
				pref.setValue(value);
				return true;
			}
		
		return false;
	}

}
