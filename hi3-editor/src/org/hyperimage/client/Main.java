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

package org.hyperimage.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;

import org.hyperimage.client.gui.HIClientGUI;

/**
 *
 * Class: Main
 *
 * @author Jens-Martin Loebel
 *
 */
public class Main {

    public static boolean SECURE = false; // if true, connect to service over HTTPS. Be sure to choose appropriate cert.
    public static final File HYPERDIR = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".hyperimage");
    public static final String CACERTS = "hyper_cacerts.jks";
    //public static final String CACERTS = "medi2_cacerts.jks";
    public static final File KEYSTORE_TGT = new File(HYPERDIR.getAbsoluteFile() + System.getProperty("file.separator") + CACERTS);
    public static final String KEYSTORE_SRC = new String("/resources/security/" + CACERTS);
    public static final String KEYSTORE_PASSWD = new String("changeit");

    public static void main(String args[]) {
        // fallback URL if no server URL was specified via command line / webstart xml
        String serverURL = "";

        // use user specified server URL if provided
        if (args.length == 0) {
            // error message
            HIRuntime.displayFatalErrorAndExit("Please specify a URL for the server.\n"
                    + "Usage: java -jar HIEditor_2.0.jar <URL to Service>\n"
                    + "e.g. java -jar HIEditor_2.0.jar \"http://your.server.com/HIEditor_2.0-Service/\"");
        } else {
            serverURL = args[0];
        }

        // check if service is secure (HTTPS)
        // try to auto-detect
        if (serverURL.toLowerCase().startsWith("https://") && SECURE == false) {
            SECURE = true;
        } else {
            SECURE = false;
        }

        if (SECURE) {
            // If HyperImage dir already exists, then skip this.
            if (!HYPERDIR.exists()) {
                HYPERDIR.mkdir();
            }

            // If keystore already exists, then skip this.
            if (!KEYSTORE_TGT.exists()) {
                InputStream input = Main.class.getResourceAsStream(KEYSTORE_SRC);

                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(KEYSTORE_TGT);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int ch = 0;

                do {
                    try {
                        ch = input.read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // In case the file is empty on the first run
                    if (ch == -1) {
                        continue;
                    } else {
                        try {
                            out.write(ch);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } while (ch != -1);

                try {
                    input.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Set system properties for keystore.
            System.setProperty("javax.net.ssl.trustStore", KEYSTORE_TGT.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStorePassword", KEYSTORE_PASSWD);
        }

        // Display console info
        System.out.println("Client Version: " + HIRuntime.getClientVersion());
        System.out.println("Server URL: " + serverURL);
        System.out.println("Secure Connection: " + SECURE);

        if (System.getProperty("jnlp.user") != null) {
            HIRuntime.OAUTHMode = true;
        }
        System.out.println("OAUTH / Prometheus Mode: " + HIRuntime.OAUTHMode);

        // DEBUG remove
        if (HIRuntime.OAUTHMode) {
            System.setProperty("HI.feature.importDisabled", "true");
//            System.setProperty("HI.feature.trashDisabled", "true");
            System.setProperty("HI.feature.templateEditorDisabled", "true");
            System.setProperty("HI.feature.accountSettingsDisabled", "true");
            System.setProperty("HI.feature.manageUsersDisabled", "true");
        }

        try {
            // append trailing slash if necessary
            if (!serverURL.endsWith("/")) {
                serverURL = serverURL + "/";
            }

            // init connection to web service
            HIWebServiceManager manager = new HIWebServiceManager(serverURL);
            HIRuntime.setManager(manager);

            // init MDI GUI and add to event queue
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    HIClientGUI gui = new HIClientGUI();
                    gui.createAndShowGUI(); // at this point the GUI takes over
                }
            });

            /*
             * Catch all fatal errors and report to user.
             */
        } catch (MalformedURLException mfue) {
            // catch and report bad URLs
            HIRuntime.displayFatalErrorAndExit("Supplied URL is invalid or malformed!\n\nReason: " + mfue.getMessage());
        } catch (Exception e) {
            if (e.getCause() instanceof ConnectException) {
                // catch "network down" situation and report to user if server is unreachable
                HIRuntime.displayFatalErrorAndExit("Network connection failed!\n\nCould not reach server at:\n" + serverURL);
            } else {
                // report all other fatal errors
                HIRuntime.displayFatalErrorAndExit("Client Initialization Failed!\n\nReason:" + e.getMessage() + "\n\n" + e.getClass());
            }
        }
    }
}
