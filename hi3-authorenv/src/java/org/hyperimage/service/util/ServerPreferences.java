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

package org.hyperimage.service.util;

import java.io.BufferedInputStream;
import java.io.File;
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
public class ServerPreferences {
	private static Preferences m_prefs = null;
	private static boolean m_bPrefsLoaded = false;
	
	// Constants
	private static final String NODENAME = "/org/hyperimage/service";
	private static final String PREF_FILENAME = "hiserverprefs.xml";
	private static InputStream m_PrefStream = null;
	
	// Preference keys
	private static final String HISTORE_LOC_KEY = "historeLocation";
	private static final String PPG_LOC_KEY = "ppgLocation";
	private static final String PROMETHEUS_API_LOC_KEY = "prometheusAPILocation";
	private static final String PROMETHEUS_CONSUMER_KEY = "OAUTHConsumerKey";
	private static final String PROMETHEUS_CONSUMER_SECRET = "OAUTHConsumerSecret";
	
	// Default preference values
	private static final String DEF_HISTORE_LOC = "/Users/Shared/HIStore/";
	private static final String DEF_PPG_LOC="http://hyperimage.ws/PostPetalGenerator/PostPetalGenerator.jnlp";
        private static final String DEF_PROMETHEUS_API_LOC="http://prometheus-test.uni-koeln.de/pandora-devel";
        private static final String DEF_PROMETHEUS_CONSUMER_KEY_VAL="";
        private static final String DEF_PROMETHEUS_CONSUMER_SECRET_VAL="";

        
	public ServerPreferences() {
		m_prefs = Preferences.userRoot().node(NODENAME);
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		m_PrefStream = classloader.getResourceAsStream("./conf/" + PREF_FILENAME);
		loadPrefs();
	}

	/**
         * 
	 * @return
	 */
	public String getHIStorePref() {
		return m_prefs.get(HISTORE_LOC_KEY, DEF_HISTORE_LOC);
	}
	
	public String getPPGPref() {
		return m_prefs.get(PPG_LOC_KEY, DEF_PPG_LOC);
	}
        
        public String getPrometheusAPIPref() {
            return m_prefs.get(PROMETHEUS_API_LOC_KEY, DEF_PROMETHEUS_API_LOC);
        }
        
        public String getPrometheusOAUTHConsumerKey() {
            return m_prefs.get(PROMETHEUS_CONSUMER_KEY, DEF_PROMETHEUS_CONSUMER_KEY_VAL);
        }

        public String getPrometheusOAUTHConsumerSecret() {
            return m_prefs.get(PROMETHEUS_CONSUMER_SECRET, DEF_PROMETHEUS_CONSUMER_SECRET_VAL);
        }

    private void loadPrefs() {
        if ( m_PrefStream == null ) return;

        // Import preference data
        try {
            Preferences.importPreferences(m_PrefStream);

        } catch (InvalidPreferencesFormatException | IOException ipfe) {
            ipfe.printStackTrace();
        }

    }
}
