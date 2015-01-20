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
/* $Id: ConnectorPreferences.java 95 2009-03-06 15:28:38Z hgkuper $ */
package org.hyperimage.connector.utility;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

/**
 * 
 * @author Heinz-Guenter Kuper
 *
 */
public class ConnectorPreferences {
	private static Preferences m_prefs = null;
	
	// Constants
	private static final String NODENAME = "/org/hyperimage/connector/fedora3";
	private static final String PREF_FILENAME = "hif3connprefs.xml";
	private static URL m_PrefURL = null;
	
	// Preference keys
	private static final String FEDORA_URL_KEY = "fedoraURL";
	
	// Default preference values
	private static final String DEF_FEDORA_URL="http://hyperimage.cms.hu-berlin.de:8080/fedora/wsdl?api=API-A";

	public ConnectorPreferences() {
		m_prefs = Preferences.userRoot().node(NODENAME);
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		m_PrefURL = classloader.getResource("./conf/" + PREF_FILENAME);
		loadPrefs();
	}
	
	public String getFedoraURLPref() {
		return m_prefs.get(FEDORA_URL_KEY, DEF_FEDORA_URL);
	}

	private void loadPrefs() {
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(m_PrefURL.getFile()));
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}

		// Import preference data
		try {
			Preferences.importPreferences(is);
		} catch (InvalidPreferencesFormatException ipfe) {
			ipfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
