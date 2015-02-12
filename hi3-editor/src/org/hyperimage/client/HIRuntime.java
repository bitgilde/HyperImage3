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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.hyperimage.client.gui.HIClientGUI;

/**
 * 
 * Class: HIRuntime
 * Package: org.hyperimage.client
 * @author Jens-Martin Loebel
 *
 */
public abstract class HIRuntime {

	private static final String clientVersion = "3.0";
	private static final String minorRev = "a2";
	
	// DEBUG replace with local preference file
	public static final int MAX_GROUP_ITEMS = 100; // number of items in a group after which the group browser switches to list-display-style
	public static final long MINIMUM_FREE_MEMORY = (1024*1024*5); 	// minimum free memory for GUI / layer editor in bytes --> currently 5 MB
        
        public static boolean OAUTHMode = false; // set when Prometheus user logged in via OAUTH
        
	// available gui languages
	public static final Locale[] supportedLanguages = {
		new Locale("de"),
		new Locale("en")
	};
	
	// set default gui language
	private static Locale guiLanguage = supportedLanguages[0];
	
	private static HIWebServiceManager manager;
	private static HIClientGUI gui;
	private static String clipboard;
	
	
	
	/**
	 * Report all fatal errors to the user.
	 * Since we probably don´t have a GUI (anymore) at this stage
	 * display errors to user in a standard JOption pane. All errors reported
	 * are fatal. As the client cannot recover from those --> exit gracefully. 
	 * 
	 * @param message error message to display
	 */
	public static void displayFatalErrorAndExit(String message) {
		JOptionPane.showMessageDialog(null,
				"FATAL ERROR!\n"+message,
				"HyperImage Client: Fatal Error",
				JOptionPane.ERROR_MESSAGE);
		System.out.println(message);
		System.exit(1);
	}
	

	public static HIClientGUI getGui() {
		return gui;
	}

	public static void setGui(HIClientGUI gui) {
		HIRuntime.gui = gui;
	}

	public static HIWebServiceManager getManager() {
		return manager;
	}

	public static void setManager(HIWebServiceManager manager) {
		HIRuntime.manager = manager;
	}

	// clipboard functions
	
	public static void copyToClipboard(String contents) {
		clipboard = contents;
	}
	
	public static String pasteFromClipboard() {
		return clipboard;
	}
	
	public static void emptyClipboard() {
		clipboard = null;
	}
	
	public static boolean isClipboardEmpty() {
		if ( clipboard == null ) return true;
		else if ( clipboard.length() == 0 ) return true;
		return false;
	}


	public static String getClientVersion() {
		return clientVersion+"."+minorRev;
	}


    // file helper functions

    	// http://www.java-tips.org/java-se-tips/java.io/reading-a-file-into-a-byte-array.html
	// TODO replace with own code
	public static byte[] getBytesFromFile(File file) throws IOException {

		InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file);
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public static String getMD5HashString(byte[] inputData) {
        String hashString = "";
        MessageDigest md5;
        byte[] digest;
        int curNum;

        final char[] hexTable = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.reset();

            md5.update(inputData);
            digest = md5.digest();

            for (int i = 0; i < digest.length; i++) {
                curNum = digest[i]; // make sure current byte of digest hex is unsigned
                if (curNum < 0) {
                    curNum = curNum + 256;
                }
                hashString = hashString + hexTable[curNum / 16] + hexTable[curNum % 16];
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("MD5 not supported by current Java VM!");
            System.exit(1);
        }
        return hashString;
    }


	// system dependent function
	
	public static int getModifierKey() {
		// get os-dependent modifier key
		if ( System.getProperty("os.name").toLowerCase().indexOf("mac") != -1 )
			return ActionEvent.META_MASK;

		return ActionEvent.CTRL_MASK;
	}


	// GUI internationalization
	
	public static Locale getGUILanguage() {
		return guiLanguage;
	}

	public static void setGUILanguage(Locale language) {
		guiLanguage = language;
		Messages.updateDefaultLanguage(language);
	}
	
}
