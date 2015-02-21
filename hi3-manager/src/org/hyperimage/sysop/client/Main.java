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
package org.hyperimage.sysop.client;

import java.net.MalformedURLException;
import javax.swing.JOptionPane;

import org.hyperimage.client.HIWebServiceManager;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.sysop.client.gui.AuthDialog;
import org.hyperimage.sysop.client.gui.ServerSelectionDialog;
import org.hyperimage.sysop.client.gui.SysopFrame;

/**
 * @author Heinz-Guenter Kuper
 */
public class Main {

    // Constants
    private static final String SYSOP = "sysop";
    private static final boolean DEBUG = false;
    private static final String VERSION = "0.92 Alpha";

    // Member fields
    private static SysopFrame m_frame = null;
    private static String m_strServerURL = "";
    private static String m_strSysopPasswd = "";

    private static void chooseServer() {
        ServerSelectionDialog svrDlg = new ServerSelectionDialog(m_frame, true);
        svrDlg.setTitle("Choose Server");
        svrDlg.setLocationRelativeTo(m_frame);
        svrDlg.setVisible(true);
        m_strServerURL = svrDlg.getURL();
    }

    private static HISystemOperator handleLogin() {
        HIWebServiceManager manager = null;
        HISystemOperator sysOp = null;
        if (m_strServerURL.compareTo("") != 0) {
            // Login dialog
            boolean loginOk = false;
            while (!loginOk) {
                showAuthDialog();
                try {
                    manager = new HIWebServiceManager(m_strServerURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    if (manager.login(SYSOP, m_strSysopPasswd)) {
                        if (DEBUG) {
                            System.out.println("SysOp logged in with password \"" + m_strSysopPasswd + "\"");
                        }
                        sysOp = new HISystemOperator(manager);
                        m_frame.setSysOp(sysOp);
                        m_frame.setVisible(true);
                        loginOk = true;
                    } else {
                        if (DEBUG) {
                            System.out.println("Password incorrect.");
                        }
                        JOptionPane.showMessageDialog(null, "Password incorrect!", "HyperImage 3 System Manager", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (HIWebServiceException e) {
                    e.printStackTrace();
                }

            }
        }

        return sysOp;
    }

    private static void showAuthDialog() {
        AuthDialog authDlg = new AuthDialog(m_frame, true);
        authDlg.setTitle("Authenticate Administrator");
        authDlg.setLocationRelativeTo(m_frame);
        authDlg.setVisible(true);
        m_strSysopPasswd = authDlg.getPassword();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        m_frame = new SysopFrame();
        HISystemOperator sysOp = null;

        //TaskSelectionPanel taskPanel = new TaskSelectionPanel();
        m_frame.setTitle("HyperImage 3 System Manager Client " + VERSION);
        m_frame.pack();
        m_frame.setLocationRelativeTo(null);	// centre on screen
        m_frame.setVisible(false);

        // Determine server
        if ( args.length > 0  && args[0] != null ) m_strServerURL = args[0];
        else chooseServer();
        
        if (DEBUG) {
            System.out.println("Connecting to " + m_strServerURL);
        }

        sysOp = handleLogin();

    }

}
