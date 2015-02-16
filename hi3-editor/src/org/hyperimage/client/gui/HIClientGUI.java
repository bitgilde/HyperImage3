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

package org.hyperimage.client.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.HIWebServiceManager;
import org.hyperimage.client.Messages;
import org.hyperimage.client.components.AccountSettings;
import org.hyperimage.client.components.GenericMetadataEditor;
import org.hyperimage.client.components.GroupBrowser;
import org.hyperimage.client.components.HIComponent;
import org.hyperimage.client.components.HIComponent.HIMessageTypes;
import org.hyperimage.client.components.HIComponentFrame;
import org.hyperimage.client.components.LayerEditor;
import org.hyperimage.client.components.LightTableEditor;
import org.hyperimage.client.components.ObjectEditor;
import org.hyperimage.client.components.PreferenceManager;
import org.hyperimage.client.components.ProjectSettings;
import org.hyperimage.client.components.ProjectUsersManager;
import org.hyperimage.client.components.RepositoryImport;
import org.hyperimage.client.components.SearchModule;
import org.hyperimage.client.components.TemplateEditor;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.dialogs.AboutDialog;
import org.hyperimage.client.gui.dialogs.ExportProjectDialog;
import org.hyperimage.client.gui.dialogs.LoginDialog;
import org.hyperimage.client.gui.dialogs.ProjectChooser;
import org.hyperimage.client.gui.dialogs.XMLImportProjectDialog;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HIMaintenanceModeException_Exception;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiImageSizes;
import org.hyperimage.client.ws.HiLightTable;
import org.hyperimage.client.ws.HiObject;
import org.hyperimage.client.ws.HiProject;
import org.hyperimage.client.ws.HiQuickInfo;
import org.hyperimage.client.ws.HiRoles;
import org.hyperimage.client.ws.HiText;
import org.hyperimage.client.ws.HiView;
import org.hyperimage.client.ws.Hiurl;
import org.hyperimage.client.xmlimportexport.PeTAL2Importer;
import org.hyperimage.client.xmlimportexport.PeTAL3Exporter;
import org.hyperimage.client.xmlimportexport.PeTAL3Importer;
import org.hyperimage.client.xmlimportexport.PeTALExporter;
import org.hyperimage.client.xmlimportexport.VRACore4HeidelbergImporter;
import org.hyperimage.client.xmlimportexport.XMLImporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * Class: HIClientGUI Package: org.hyperimage.client.gui
 *
 * @author Jens-Martin Loebel
 *
 * Provides the HyperImage Client GUI
 */
public class HIClientGUI extends JFrame implements WindowListener, ActionListener, InternalFrameListener, HierarchyListener, ComponentListener {

    private static final long serialVersionUID = -4501582186237562342L;
    private static final String APPNAME = "HyperImage Editor";

    private HIWebServiceManager manager;

    // GUI central components and dialogs
    HiProject project;
    private JMenu debugMenu;
    private HIComponentFrame frontFrame = null;
    private ProjectChooser projectChooser;
    private LoginDialog loginDialog;
    private ExportProjectDialog exportDialog;
    private XMLImportProjectDialog xmlImportDialog;

    private HIWebServiceException lastWSError = null;

    // active Components
    Vector<HIComponentFrame> components;

    // indicates whether components are currently communicating with the web service or not
    private boolean serviceActivity = false;
    // indicates whether or not the GUI is currently displaying a service error modal dialog
    private boolean displayingError = false;

    // custom cursors
    Cursor waitCursor;

    // MDI mode
    public JDesktopPane mdiPane = new JDesktopPane();
    public ProgressInfoGlassPane infoGlassPane = new ProgressInfoGlassPane();
    // MDI Menus
    private JMenuItem aboutMenuItem;
    private JMenuItem administrateProjectPrefsMenuItem;
    private JMenuItem administrateProjectUsersMenuItem;
    private JMenuItem changeProjectMenuItem;
    private JMenuItem showLivePreviewMenuItem;
    private JMenuItem changeUserMenuItem;
    private JMenuItem contentMenuItem;
    private JMenuItem editUserMenuItem;
    private JMenuItem exitMenuItem;
    private JMenu fileMenu;
    private JMenuItem exportMenuItem;
    private JMenuItem publicationMenuItem;
    private JMenuItem xmlImportMenuItem;
    private JMenu guiLanguageMenu;
    private JMenuBar guiMenuBar;
    private JMenu helpMenu;
    private JMenuItem feedbackItem;
    private JMenuItem newGroupBrowserMenuItem;
    private JMenuItem searchMenuItem;
    private JMenuItem importMenuItem;
    private JMenuItem nextWindowItem;
    private JMenuItem prevWindowItem;
    private JMenu projectMenu;
    private JMenuItem projectSettingsMenuItem;
    private JMenuItem projectTemplatesMenuItem;
    private JMenuItem toggleMetadataViewMenuItem;
    private JMenu toolsMenu;
    private JMenuItem visitWebsiteMenuItem;
    private JMenu windowMenu;
    private JSeparator windowSeparator1;
    private JSeparator windowSeparator2;
    // GUI popup menu
    private JMenu popupToolsMenu;
    private JMenuItem popupNewGroupBrowserMenuItem;
    private JMenuItem popupSearchMenuItem;
    private JMenuItem popupImportMenuItem;

    public HIClientGUI() {
        // init
        this.manager = HIRuntime.getManager();
        HIRuntime.setGui(this);
        components = new Vector<HIComponentFrame>();

        // set initial GUI state
        mdiPane.setBackground(new Color(0x41, 0x69, 0xAA));

        // set close handler
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(this);

    }

    public LoginDialog getLoginDialog() {
        return loginDialog;
    }

    public boolean handleLogin() {
        boolean loginSelected;

        HIRuntime.getManager().logout(); // log out

        // try to login using supplied credentials
        try {

            // handle login for OAUTH / Prometheus users
            if (HIRuntime.OAUTHMode) {
                boolean success = HIRuntime.getManager().loginPR(System.getProperty("jnlp.user"),
                        System.getProperty("jnlp.oauth_token"),
                        System.getProperty("jnlp.oauth_token_secret"),
                        System.getProperty("jnlp.oauth_verifier"));

                if (!success) {
                    HIRuntime.displayFatalErrorAndExit("ERROR: Could not authenticate user using suplied OAUTH / Prometheus credentials!");
                }

                return success;
            }

            // prompt for user name / password
            this.setTitle(APPNAME + " " + HIRuntime.getClientVersion()); //$NON-NLS-1$
            loginDialog.setInfoLabel(Messages.getString("HIClientGUI.0"), Color.blue); //$NON-NLS-1$
            loginSelected = loginDialog.promptLogin(loginDialog.getUserName());

            if (!loginSelected) {
                return false;
            }

            while (!HIRuntime.getManager().login(loginDialog.getUserName(), loginDialog.getPassword())) {
                loginDialog.setInfoLabel(Messages.getString("HIClientGUI.1"), Color.red); //$NON-NLS-1$
                loginSelected = loginDialog.promptLogin(loginDialog.getUserName());
                if (!loginSelected) {
                    return false;
                }
            }
        } catch (HIWebServiceException e) {
            System.out.println("SERVER SIDE SERVICE EXCEPTION:"); //$NON-NLS-1$
            e.printStackTrace();
            // scan for maintenance mode and display message
            if (e.getCause() instanceof HIMaintenanceModeException_Exception) {
                displayMaintenanceDialog(e);
                return handleLogin();
            } else {
                HIRuntime.displayFatalErrorAndExit("WebService Initialization Failed!\n\nReason:" + e.getMessage() + "\n\n" + e.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            // TODO: analyze ws error
            return false;
        }

        return true;
    }

    public void tryLogoutAndExit() {
        if (deregisterAllComponents()) {
            System.out.print("Logout: "); //$NON-NLS-1$
            System.out.println(HIRuntime.getManager().logout());
            this.setVisible(false);
            this.dispose();
            System.exit(0);
        }
    }

    public void chooseProject() {
        this.setTitle(APPNAME + " " + HIRuntime.getClientVersion()); //$NON-NLS-1$
        List<HiProject> projects = null;
        try {
            startIndicatingServiceActivity();
            projects = HIRuntime.getManager().getProjects();
            stopIndicatingServiceActivity();
        } catch (HIWebServiceException e) {
            HIRuntime.displayFatalErrorAndExit("Could not get user projects!"); //$NON-NLS-1$
        }
        if (projects.size() == 0) {
            HIRuntime.displayFatalErrorAndExit("User does not belong to any projects!"); //$NON-NLS-1$
        }

        // let the user choose a project (if only one project exists it automatically will be selected by the project chooser)
        project = projectChooser.selectProject(projects);

        // The user declined to select a project, nothing we can do --> exit gracefully
        if (project == null) {
            if (HIRuntime.getManager().getProject() == null) {
                tryLogoutAndExit();
                System.exit(1);
            } else {
                setMenuState();
                return;
            }
        }

        try {
            startIndicatingServiceActivity(true);
            System.out.println("Setting Project ID: " + project.getId());
            manager.setProject(project);
            stopIndicatingServiceActivity();
            // display project title in gui title bar
            updateProjectTitle();

            setMenuState();
        } catch (HIWebServiceException wse) {
            reportError(wse, null);
            return;
        }
    }

    public void updateProjectTitle() {
        this.project = HIRuntime.getManager().getProject();
        String projectTitle = MetadataHelper.findValue(project, project.getDefaultLanguage().getLanguageId());
        if (projectTitle == null || projectTitle.length() == 0) {
            projectTitle = "Project P" + project.getId(); //$NON-NLS-1$
        }
        this.setTitle(APPNAME + " " + HIRuntime.getClientVersion() + " - " + projectTitle); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void triggerProjectUpdate() {
        sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, null);
        sendMessage(HIMessageTypes.LANGUAGE_ADDED, null, null);
        sendMessage(HIMessageTypes.DEFAULT_LANGUAGE_CHANGED, null, null);
        sendMessage(HIMessageTypes.GROUP_SORTORDER_CHANGED, null, null);
        updateProjectTitle();
    }

    public void handleXMLImport() {
        // check user role
        if (!checkAdminAbility(false)) {
            return;
        }

        // check requirements --> empty project --> no groups and no elements
        boolean projectIsEmpty = false;
        try {
            if (HIRuntime.getManager().getGroups().size() == 0) {
                if (HIRuntime.getManager().getGroupContents(HIRuntime.getManager().getImportGroup()).size() == 0
                        && HIRuntime.getManager().getGroupContents(HIRuntime.getManager().getTrashGroup()).size() == 0) {
                    projectIsEmpty = true;
                }
            }
        } catch (HIWebServiceException wse) {
            HIRuntime.getGui().reportError(wse, null);
            return;
        }

        // show import dialog
        if (xmlImportDialog.displayImportDialog()) {

        } else {
            displayInfoDialog(Messages.getString("HIClientGUI.150"), Messages.getString("HIClientGUI.151"));
            return;
        }

        File importFile = xmlImportDialog.getImportXMLFile();

        // check import xml
        if (importFile == null || !importFile.isFile() || !importFile.exists() || !importFile.canRead()) {
            displayInfoDialog(Messages.getString("HIClientGUI.152"), Messages.getString("HIClientGUI.153"));
            return;
        }

        XMLImporter importer = new XMLImporter();

        if (!importer.loadAndValidateXMLFile(importFile)) {
            displayInfoDialog(Messages.getString("HIClientGUI.154"), Messages.getString("HIClientGUI.155"));
            return;
        }

        if (importer.isPeTAL2_0()) {
            if (!projectIsEmpty) {
                displayInfoDialog(Messages.getString("HIClientGUI.159"), Messages.getString("HIClientGUI.160") + "\n\n" + Messages.getString("HIClientGUI.161"));
                return;
            }
            if (!xmlImportDialog.isPeTAL_Expected()) {
                displayInfoDialog(Messages.getString("HIClientGUI.154"), Messages.getString("HIClientGUI.vraCoreExtendedExpected"));
                return;
            } else { // I know the "else" is redundant after the return, but I'm leaving it in anyway. ~HGK
                PeTAL2Importer petalImporter = new PeTAL2Importer(importFile, importer.getXMLDocument());
                petalImporter.importXMLToProject();
            }
        } else if (importer.isPeTAL3_0()) {
            if (!xmlImportDialog.isPeTAL_Expected()) {
                displayInfoDialog(Messages.getString("HIClientGUI.154"), Messages.getString("HIClientGUI.vraCoreExtendedExpected"));
                return;
            }
            PeTAL3Importer petalImporter = new PeTAL3Importer(importFile, importer.getXMLDocument());
            petalImporter.importXMLToProject();
        }       /*else if( importer.isVRA4() ){
         if( !xmlImportDialog.isVRA4_Expected() ) {
         displayInfoDialog("XML Import Error", "Expected file to be in PeTAL 2.0 format.");
         return;
         } else {
         VRACore4Importer vraImporter = new VRACore4Importer(importFile, importer.getXMLDocument());
         vraImporter.importXMLToProject();
         }
         }*/ else if (importer.isVRA4Hdlbg()) {
            if (!xmlImportDialog.isVRA4Hdlbg_Expected()) {
                displayInfoDialog(Messages.getString("HIClientGUI.154"), Messages.getString("HIClientGUI.petalExpected"));
                return;
            } else {
                VRACore4HeidelbergImporter vrahdlbgImporter = new VRACore4HeidelbergImporter(importFile, importer.getXMLDocument());
                vrahdlbgImporter.importXMLToProject();
            }
        } else if( importer.isTamboti() ){
            if( !xmlImportDialog.isVRA4Hdlbg_Expected()) {
                displayInfoDialog(Messages.getString("HIClientGUI.154"), Messages.getString("HIClientGUI.petalExpected"));
                return;
            } else {
                // Tamboti VRA export is currently (as of 2014-06-06) wrapped in a <my-list-export> tag.
                // Extract the document below this tag and pass it to the importer.
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = null;
                try {
                    docBuilder = docFactory.newDocumentBuilder();
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(HIClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                Document vraDoc = docBuilder.newDocument();
                Document tambotiDoc = importer.getXMLDocument();
                Node tambotiDocRoot = tambotiDoc.getFirstChild(); // returns <my-list-export>
                NodeList childNodes = tambotiDocRoot.getChildNodes();
                
                // Iterate through the nodes looking for the first (and only?) element, namely <vra>.
                int i = 0;
                Node vraNode = childNodes.item(i);
                while( !(vraNode instanceof Element) )
                    vraNode = childNodes.item(++i);
                
                Node node = vraDoc.importNode(vraNode, true);
                vraDoc.appendChild(node);
                VRACore4HeidelbergImporter vrahdlbgImporter = new VRACore4HeidelbergImporter(importFile, vraDoc);
                vrahdlbgImporter.importXMLToProject();
            }
        }

    }

    public void handleExport() {
        // check user role
        if (!checkEditAbility(false)) {
            return;
        }

        // show export dialog
        if (exportDialog.displayExportDialog()) {
            // sanity check
            if (exportDialog.getExportDir() == null) {
                return;
            }

            if (!exportDialog.getExportDir().canWrite()) {
                displayInfoDialog(Messages.getString("HIClientGUI.8"), Messages.getString("HIClientGUI.9")); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }

            final File exportDir = new File(exportDialog.getExportDir().getAbsolutePath() + File.separatorChar + "PeTAL"); //$NON-NLS-1$
            if (!exportDir.exists()) {
                exportDir.mkdir();
            }

            /* **************
             * Export Project
             * **************
             */
            // DEBUG
            startIndicatingServiceActivity(true);
            setMessage(Messages.getString("HIClientGUI.5")); //$NON-NLS-1$

            // execute export in separate thread
            new Thread() {
                public void run() {
                    // check export type, still support Legacy mode (PeTAL 1.2) for now
                    Document xml;
                    if (exportDialog.isPeTAL3Mode()) {
                        xml = PeTAL3Exporter.getProjectToPeTALXML(HIRuntime.getManager().getProject());
                    } else {
                        xml = PeTALExporter.getProjectToPeTALXML(HIRuntime.getManager().getProject());
                    }

                    if (xml != null) {
                        File outputFile = new File(exportDir.getAbsolutePath() + File.separatorChar + "project.xml"); //$NON-NLS-1$
                        try {
                            outputFile.createNewFile();
                        } catch (IOException ioe) {
                            return;
                        }
                        if (outputFile.canWrite() == false) {
                            return;
                        }

                        if (exportDialog.isPeTAL3Mode()) {
                            PeTAL3Exporter.serializeXMLDocumentToFile(xml, outputFile);
                        } else {
                            PeTALExporter.serializeXMLDocumentToFile(xml, outputFile);
                        }

                    }

                    if (exportDialog.isExportingBinaries()) {
                        /* ********************
                         * Export view binaries
                         * ********************
                         */
                        // DEBUG
                        FileOutputStream binWriter;
                        File binaryDir = new File(exportDir.getAbsolutePath() + File.separatorChar + "img"); //$NON-NLS-1$
                        if (!binaryDir.exists()) {
                            binaryDir.mkdir();
                        }

                        int progress = -1;
                        float percentage = 0;

                        Vector<HiView> projectViews;
                        if (exportDialog.isPeTAL3Mode()) {
                            projectViews = PeTAL3Exporter.projectViews;
                        } else {
                            projectViews = PeTALExporter.projectViews;
                        }

                        if (projectViews.size() > 0) {
                            percentage = 100f / (float) projectViews.size();
                        }
                        for (int index = 0; index < projectViews.size(); index++) {
                            if (progress != (int) (percentage * (float) index)) {
                                progress = (int) (percentage * (float) index);
                                setProgress(progress);
                            }
                            HiView view = projectViews.get(index);
                            setMessage(Messages.getString("HIClientGUI.6") + " " + (index + 1) + " " + Messages.getString("HIClientGUI.11") + " " + projectViews.size()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                            if (view.getFilename() != null && view.getFilename().length() > 0) {
                                view.setMimeType(URLConnection.guessContentTypeFromName((view.getFilename())));
                            }
                            if (view.getMimeType() == null) {
                                view.setMimeType("");
                            }
                            try {
                                // export full size
                                byte[] bitstream;
                                /*
                                 if ( exportDialog.isLegacyMode()) {
                                 // support Legacy mode PeTAL 1.2
                                 bitstream = HIRuntime.getManager().getImageAsBitstream(view, HiImageSizes.HI_FULL);
                                 if (bitstream != null) {
                                 binWriter = new FileOutputStream(binaryDir.getAbsolutePath() + File.separatorChar + "V" + view.getId() + ".jpg"); //$NON-NLS-1$ //$NON-NLS-2$
                                 binWriter.write(bitstream);
                                 binWriter.close();
                                 } else if (!view.getMimeType().startsWith("image/")) //$NON-NLS-1$
                                 HIRuntime.getGui().displayInfoDialog(Messages.getString("HIClientGUI.12"), Messages.getString("HIClientGUI.13") + " V" + view.getId() + " " + Messages.getString("HIClientGUI.16") + "\n\n" + Messages.getString("HIClientGUI.18")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                                 else
                                 System.out.println("View not exported because the image is broken or mime detection failed" + " (" + view.getMimeType() + ") - " + view.getFilename() + "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

                                 // export preview size
                                 bitstream = HIRuntime.getManager().getImageAsBitstream(view, HiImageSizes.HI_PREVIEW);
                                 if (bitstream != null) {
                                 binWriter = new FileOutputStream(binaryDir.getAbsolutePath() + File.separatorChar + "V" + view.getId() + "_prev.jpg"); //$NON-NLS-1$ //$NON-NLS-2$
                                 binWriter.write(bitstream);
                                 binWriter.close();
                                 }

                                 // export thumbnail size
                                 bitstream = HIRuntime.getManager().getImageAsBitstream(view, HiImageSizes.HI_THUMBNAIL);
                                 if (bitstream != null) {
                                 binWriter = new FileOutputStream(binaryDir.getAbsolutePath() + File.separatorChar + "V" + view.getId() + "_thumb.jpg"); //$NON-NLS-1$ //$NON-NLS-2$
                                 binWriter.write(bitstream);
                                 binWriter.close();
                                 }
                                 } else {
                                 */
                                /*
                                 * standard: PeTAL 3.0 / 2.0
                                 */

                                // allow smart binary export. Check if file already at destination and has correct hash
                                File destFile = new File(binaryDir.getAbsolutePath() + File.separatorChar + "V" + view.getId() + ".original");
                                boolean exportBinary = true;
                                if (destFile.exists() && destFile.canRead()) {
                                    bitstream = HIRuntime.getBytesFromFile(destFile);
                                    String hash = HIRuntime.getMD5HashString(bitstream);
                                    if (hash.compareTo(view.getHash()) == 0) {
                                        exportBinary = false;
                                    }
                                    bitstream = null;
                                }

                                if (exportBinary) {
                                    bitstream = HIRuntime.getManager().getImageAsBitstream(view, HiImageSizes.HI_ORIGINAL);
                                    if (bitstream != null) {
                                        String extension = ".original";
                                        if ( view.getMimeType().equalsIgnoreCase("image/jpeg") ) extension = ".jpg";
                                        if ( view.getMimeType().equalsIgnoreCase("image/png") ) extension = ".png";
                                        if ( exportDialog.isPeTAL3Mode() ) binWriter = new FileOutputStream(binaryDir.getAbsolutePath() + File.separatorChar + view.getUUID() + extension);
                                        else binWriter = new FileOutputStream(binaryDir.getAbsolutePath() + File.separatorChar + "V" + view.getId() + ".original");
                                        binWriter.write(bitstream);
                                        binWriter.close();
                                    } else {
                                        HIRuntime.getGui().displayInfoDialog(Messages.getString("HIClientGUI.12"), Messages.getString("HIClientGUI.13") + " V" + view.getId() + " " + Messages.getString("HIClientGUI.16") + "\n\n" + Messages.getString("HIClientGUI.18"));
                                        System.out.println("View not exported because the image is broken or mime detection failed" + " (" + view.getMimeType() + ") - " + view.getFilename() + "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                    }
                                }

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                HIRuntime.getGui().displayInfoDialog(Messages.getString("HIClientGUI.23"), Messages.getString("HIClientGUI.24")); //$NON-NLS-1$ //$NON-NLS-2$
                                break;
                            } catch (IOException e) {
                                e.printStackTrace();
                                HIRuntime.getGui().displayInfoDialog(Messages.getString("HIClientGUI.25"), Messages.getString("HIClientGUI.26")); //$NON-NLS-1$ //$NON-NLS-2$
                                break;
                            } catch (HIWebServiceException wse) {
                                HIRuntime.getGui().reportError(wse, null);
                                break;
                            }

                        }
                    }
                    stopIndicatingServiceActivity();

                }
            }.start();

        } else {
            displayInfoDialog(Messages.getString("HIClientGUI.27"), Messages.getString("HIClientGUI.28")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void createAndShowGUI() {
        // scan and set appropriate on screen language
        for (Locale guiLang : HIRuntime.supportedLanguages) {
            if (Locale.getDefault().getLanguage().compareTo(guiLang.getLanguage()) == 0) {
                HIRuntime.setGUILanguage(guiLang);
            }
        }

        // init central components / dialogs
        projectChooser = new ProjectChooser(this);
        loginDialog = new LoginDialog(this);
        exportDialog = new ExportProjectDialog(this);
        xmlImportDialog = new XMLImportProjectDialog(this);

        // init main menu
        initMenus();

        // style GUI
        this.setTitle(APPNAME + " " + HIRuntime.getClientVersion()); //$NON-NLS-1$
        this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        this.setMinimumSize(new Dimension(200, 460));
        this.setContentPane(mdiPane);
        this.setGlassPane(infoGlassPane);
        infoGlassPane.setVisible(false);

        this.setVisible(true);

        // create custom cursors
        Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
        waitCursor = null;
        try {
            waitCursor = tk.createCustomCursor(
                    ImageIO.read(
                            getClass().getResource("/resources/cursors/wait-cursor.png")), //$NON-NLS-1$
                    new Point(16, 16), "cross-Add"); //$NON-NLS-1$
        } catch (Exception e) {
            // error occurred --> use system default cursor
            waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        }
        infoGlassPane.setCursor(waitCursor);

        // set main GUI popup menu handler
        mdiPane.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && !e.isConsumed()) {
                    e.consume();
                    popupToolsMenu.getPopupMenu().show(mdiPane, e.getX() + 10, e.getY());
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && !e.isConsumed()) {
                    e.consume();
                    popupToolsMenu.getPopupMenu().show(mdiPane, e.getX() + 10, e.getY());
                }
            }
        });

        // prompt for login credentials
        if (!handleLogin()) {
            tryLogoutAndExit();
        }
        
        // display beta version warning on first startup if necessary
        if ( HIRuntime.getClientVersion().contains("beta") && !HIRuntime.betaWarningDisplayed ) {
            HIRuntime.betaWarningDisplayed = true;
            JOptionPane.showMessageDialog(this, Messages.getString("HIClientGUI.BetaWarning"), Messages.getString("HIClientGUI.BetaWarningTitle"), JOptionPane.INFORMATION_MESSAGE);
        }

        // set active project
        chooseProject();

        // show standard layout
        showDefaultProjectLayout();

    }

    private void initMenus() {
        guiMenuBar = new JMenuBar();

        fileMenu = new JMenu();
        exportMenuItem = new JMenuItem();
        publicationMenuItem = new JMenuItem();
        xmlImportMenuItem = new JMenuItem();
        guiLanguageMenu = new JMenu();
        editUserMenuItem = new JMenuItem();
        changeProjectMenuItem = new JMenuItem();
        showLivePreviewMenuItem = new JMenuItem();
        changeUserMenuItem = new JMenuItem();
        exitMenuItem = new JMenuItem();
        projectMenu = new JMenu();
        administrateProjectPrefsMenuItem = new JMenuItem();
        projectSettingsMenuItem = new JMenuItem();
        projectTemplatesMenuItem = new JMenuItem();
        administrateProjectUsersMenuItem = new JMenuItem();
        toolsMenu = new JMenu();
        newGroupBrowserMenuItem = new JMenuItem();
        searchMenuItem = new JMenuItem();
        importMenuItem = new JMenuItem();
        windowMenu = new JMenu();
        nextWindowItem = new JMenuItem();
        prevWindowItem = new JMenuItem();
        windowSeparator1 = new JSeparator();
        toggleMetadataViewMenuItem = new JMenuItem();
        windowSeparator2 = new JSeparator();
        helpMenu = new JMenu();
        aboutMenuItem = new JMenuItem();
        contentMenuItem = new JMenuItem();
        visitWebsiteMenuItem = new JMenuItem();

        editUserMenuItem.setActionCommand("accountSettings"); //$NON-NLS-1$
        // check if editor account settings feature is disabled
        if (System.getProperty("HI.feature.accountSettingsDisabled") == null) {
            fileMenu.add(editUserMenuItem);
        }
        fileMenu.add(new JSeparator());

        fileMenu.add(guiLanguageMenu);

        fileMenu.add(new JSeparator());

        changeUserMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, HIRuntime.getModifierKey() + ActionEvent.SHIFT_MASK));
        changeUserMenuItem.setActionCommand("changeUser"); //$NON-NLS-1$
        fileMenu.add(changeUserMenuItem);

        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, HIRuntime.getModifierKey()));
        exitMenuItem.setActionCommand("exit"); //$NON-NLS-1$

        fileMenu.add(exitMenuItem);

        guiMenuBar.add(fileMenu);

        exportMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/export-menu.png"))); //$NON-NLS-1$
        exportMenuItem.setActionCommand("exportProject"); //$NON-NLS-1$
        exportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, HIRuntime.getModifierKey()));
        projectMenu.add(exportMenuItem);

        publicationMenuItem.setActionCommand("startPubTool"); //$NON-NLS-1$
        projectMenu.add(publicationMenuItem);

        projectMenu.add(new JSeparator());

        projectSettingsMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/preferences-menu.png"))); //$NON-NLS-1$
        projectSettingsMenuItem.setActionCommand("projectSettings"); //$NON-NLS-1$
        projectSettingsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, HIRuntime.getModifierKey()));
        projectMenu.add(projectSettingsMenuItem);

        projectMenu.add(new JSeparator());

        administrateProjectPrefsMenuItem.setActionCommand("librarySettings"); //$NON-NLS-1$
        projectMenu.add(administrateProjectPrefsMenuItem);

        projectTemplatesMenuItem.setActionCommand("templateEditor"); //$NON-NLS-1$
        // check if editor project templates feature is disabled
        if (System.getProperty("HI.feature.templateEditorDisabled") == null) {
            projectMenu.add(projectTemplatesMenuItem);
        }

        administrateProjectUsersMenuItem.setActionCommand("manageProjectUsers"); //$NON-NLS-1$
        // check if editor user management feature is disabled
        if (System.getProperty("HI.feature.manageUsersDisabled") == null) {
            projectMenu.add(administrateProjectUsersMenuItem);
        }

        xmlImportMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/import-menu.png"))); //$NON-NLS-1$
        xmlImportMenuItem.setActionCommand("xmlImportProject"); //$NON-NLS-1$
        xmlImportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, HIRuntime.getModifierKey()));

        // check if editor feature disabled
        if (System.getProperty("HI.feature.importDisabled") == null) {
            projectMenu.add(xmlImportMenuItem);
        }

        projectMenu.add(new JSeparator());
        showLivePreviewMenuItem.setActionCommand("livePreview"); //$NON-NLS-1$
        projectMenu.add(showLivePreviewMenuItem);

        projectMenu.add(new JSeparator());
        changeProjectMenuItem.setActionCommand("changeProject"); //$NON-NLS-1$
        projectMenu.add(changeProjectMenuItem);

        guiMenuBar.add(projectMenu);

        newGroupBrowserMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, HIRuntime.getModifierKey()));
        newGroupBrowserMenuItem.setActionCommand("newBrowser"); //$NON-NLS-1$
        toolsMenu.add(newGroupBrowserMenuItem);

        searchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, HIRuntime.getModifierKey()));
        searchMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/search-menu.png"))); //$NON-NLS-1$
        searchMenuItem.setActionCommand("newSearch"); //$NON-NLS-1$
        toolsMenu.add(searchMenuItem);
//        toolsMenu.add(new JSeparator());

//        importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, HIRuntime.getModifierKey()));
        importMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/import-menu.png"))); //$NON-NLS-1$
        importMenuItem.setActionCommand("repositoryImport"); //$NON-NLS-1$
//        toolsMenu.add(importMenuItem);

        guiMenuBar.add(toolsMenu);

        nextWindowItem.setActionCommand("nextWindow"); //$NON-NLS-1$
        nextWindowItem.setEnabled(false);
        nextWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_GREATER, HIRuntime.getModifierKey()));
        windowMenu.add(nextWindowItem);

        prevWindowItem.setActionCommand("previousWindow"); //$NON-NLS-1$
        prevWindowItem.setEnabled(false);
        prevWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LESS, HIRuntime.getModifierKey()));
        windowMenu.add(prevWindowItem);
        windowMenu.add(windowSeparator1);

        toggleMetadataViewMenuItem.setActionCommand("toggleMetadata"); //$NON-NLS-1$
        toggleMetadataViewMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, HIRuntime.getModifierKey()));
        toggleMetadataViewMenuItem.setEnabled(false);
        windowMenu.add(toggleMetadataViewMenuItem);
        windowMenu.add(windowSeparator2);

        guiMenuBar.add(windowMenu);

        aboutMenuItem.setActionCommand("about"); //$NON-NLS-1$
        helpMenu.add(aboutMenuItem);
        helpMenu.add(new JSeparator());

        contentMenuItem.setActionCommand("openHelp"); //$NON-NLS-1$
        helpMenu.add(contentMenuItem);

        visitWebsiteMenuItem.setActionCommand("visitWebsite"); //$NON-NLS-1$
        helpMenu.add(visitWebsiteMenuItem);
//		helpMenu.add(new JSeparator());

        feedbackItem = new JMenuItem();
        feedbackItem.setActionCommand("FEEDBACK"); //$NON-NLS-1$
        feedbackItem.addActionListener(this);
		// DISABLED USER FEEDBACK
//		helpMenu.add(feedbackItem);

        guiMenuBar.add(helpMenu);

        setJMenuBar(guiMenuBar);

		// -----
        // DEBUG remove
        debugMenu = new JMenu("Debug"); //$NON-NLS-1$
        JMenuItem debugItem = new JMenuItem("GTK Look and Feel"); //$NON-NLS-1$
        debugItem.setActionCommand("GTK"); //$NON-NLS-1$
        debugItem.addActionListener(this);
        debugMenu.add(debugItem);
        debugItem = new JMenuItem("System Look and Feel"); //$NON-NLS-1$
        debugItem.setActionCommand("SYSTEM"); //$NON-NLS-1$
        debugItem.addActionListener(this);
        debugMenu.add(debugItem);
        debugMenu.add(new JSeparator());

        debugItem = new JMenuItem("Perform WS Tests..."); //$NON-NLS-1$
        debugItem.setActionCommand("WS_TEST"); //$NON-NLS-1$
        debugItem.addActionListener(this);
        debugMenu.add(debugItem);

        // DEBUG
        if (HIRuntime.getManager().getServerURL().indexOf("localhost") >= 0) {
            guiMenuBar.add(debugMenu); //$NON-NLS-1$
        }

        // GUI popup menu
        popupToolsMenu = new JMenu();
        popupNewGroupBrowserMenuItem = new JMenuItem();
        popupNewGroupBrowserMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, HIRuntime.getModifierKey()));
        popupNewGroupBrowserMenuItem.setActionCommand("newBrowser"); //$NON-NLS-1$
        popupToolsMenu.add(popupNewGroupBrowserMenuItem);

        popupSearchMenuItem = new JMenuItem();
        popupSearchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, HIRuntime.getModifierKey()));
        popupSearchMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/search-menu.png"))); //$NON-NLS-1$
        popupSearchMenuItem.setActionCommand("notImplemented"); //$NON-NLS-1$
        popupToolsMenu.add(popupSearchMenuItem);
        popupToolsMenu.add(new JSeparator());

        popupImportMenuItem = new JMenuItem();
        popupImportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, HIRuntime.getModifierKey()));
        popupImportMenuItem.setIcon(new ImageIcon(getClass().getResource("/resources/icons/import-menu.png"))); //$NON-NLS-1$
        popupImportMenuItem.setActionCommand("notImplemented"); //$NON-NLS-1$
        popupToolsMenu.add(popupImportMenuItem);

		// -----
        // attach listeners
        aboutMenuItem.addActionListener(this);
        editUserMenuItem.addActionListener(this);
        changeUserMenuItem.addActionListener(this);
        changeProjectMenuItem.addActionListener(this);
        showLivePreviewMenuItem.addActionListener(this);
        exitMenuItem.addActionListener(this);
        newGroupBrowserMenuItem.addActionListener(this);
        searchMenuItem.addActionListener(this);
        importMenuItem.addActionListener(this);
        exportMenuItem.addActionListener(this);
        publicationMenuItem.addActionListener(this);
        xmlImportMenuItem.addActionListener(this);
        projectSettingsMenuItem.addActionListener(this);
        projectTemplatesMenuItem.addActionListener(this);
        administrateProjectUsersMenuItem.addActionListener(this);
        administrateProjectPrefsMenuItem.addActionListener(this);
        prevWindowItem.addActionListener(this);
        nextWindowItem.addActionListener(this);
        toggleMetadataViewMenuItem.addActionListener(this);
        contentMenuItem.addActionListener(this);
        visitWebsiteMenuItem.addActionListener(this);

        popupNewGroupBrowserMenuItem.addActionListener(this);
        popupSearchMenuItem.addActionListener(this);
        popupImportMenuItem.addActionListener(this);

        // build on screen language menu
        for (int i = 0; i < HIRuntime.supportedLanguages.length; i++) {
            JCheckBoxMenuItem langItem = new JCheckBoxMenuItem();
            langItem.setActionCommand("setLang_" + i); //$NON-NLS-1$
            langItem.addActionListener(this);
            guiLanguageMenu.add(langItem);
        }

        // set titles for all menus and menu items
        setMenuText();

    }

    /**
     * (Re)sets menu / menu item titles in current GUI language. This will be
     * called after a language change
     */
    private void setMenuText() {
        fileMenu.setText(Messages.getString("HIClientGUI.64")); //$NON-NLS-1$
        editUserMenuItem.setText(Messages.getString("HIClientGUI.65")); //$NON-NLS-1$
        guiLanguageMenu.setText(Messages.getString("HIClientGUI.66")); //$NON-NLS-1$
        changeUserMenuItem.setText(Messages.getString("HIClientGUI.67")); //$NON-NLS-1$
        exitMenuItem.setText(Messages.getString("HIClientGUI.68")); //$NON-NLS-1$
        projectMenu.setText(Messages.getString("HIClientGUI.69")); //$NON-NLS-1$
        exportMenuItem.setText(Messages.getString("HIClientGUI.70")); //$NON-NLS-1$
        publicationMenuItem.setText(Messages.getString("HIClientGUI.publicationmenuitem")); //$NON-NLS-1$
        xmlImportMenuItem.setText(Messages.getString("HIClientGUI.93")); //$NON-NLS-1$
        administrateProjectPrefsMenuItem.setText(Messages.getString("HIClientGUI.71")); //$NON-NLS-1$
        projectSettingsMenuItem.setText(Messages.getString("HIClientGUI.72")); //$NON-NLS-1$
        projectTemplatesMenuItem.setText(Messages.getString("HIClientGUI.73")); //$NON-NLS-1$
        administrateProjectUsersMenuItem.setText(Messages.getString("HIClientGUI.74")); //$NON-NLS-1$
        changeProjectMenuItem.setText(Messages.getString("HIClientGUI.75")); //$NON-NLS-1$
        showLivePreviewMenuItem.setText(Messages.getString("HIClientGUI.203")); //$NON-NLS-1$
        toolsMenu.setText(Messages.getString("HIClientGUI.76")); //$NON-NLS-1$
        newGroupBrowserMenuItem.setText(Messages.getString("HIClientGUI.77")); //$NON-NLS-1$
        searchMenuItem.setText(Messages.getString("HIClientGUI.78")); //$NON-NLS-1$
        importMenuItem.setText(Messages.getString("HIClientGUI.79")); //$NON-NLS-1$
        windowMenu.setText(Messages.getString("HIClientGUI.80")); //$NON-NLS-1$
        nextWindowItem.setText(Messages.getString("HIClientGUI.81")); //$NON-NLS-1$
        prevWindowItem.setText(Messages.getString("HIClientGUI.82")); //$NON-NLS-1$
        toggleMetadataViewMenuItem.setText(Messages.getString("HIClientGUI.83")); //$NON-NLS-1$
        helpMenu.setText(Messages.getString("HIClientGUI.84")); //$NON-NLS-1$
        aboutMenuItem.setText(Messages.getString("HIClientGUI.85")); //$NON-NLS-1$
        contentMenuItem.setText(Messages.getString("HIClientGUI.86")); //$NON-NLS-1$
        visitWebsiteMenuItem.setText(Messages.getString("HIClientGUI.87")); //$NON-NLS-1$
        feedbackItem.setText(Messages.getString("HIClientGUI.88")); //$NON-NLS-1$
        // popup menu
        popupNewGroupBrowserMenuItem.setText(newGroupBrowserMenuItem.getText());
        popupSearchMenuItem.setText(searchMenuItem.getText());
        popupImportMenuItem.setText(importMenuItem.getText());

        // build on screen language menu text
        for (int i = 0; i < HIRuntime.supportedLanguages.length; i++) {
            Locale guiLang = HIRuntime.supportedLanguages[i];
            JCheckBoxMenuItem langItem = (JCheckBoxMenuItem) guiLanguageMenu.getMenuComponent(i);
            langItem.setText(guiLang.getDisplayLanguage());
            langItem.setSelected(guiLang.getLanguage().compareTo(HIRuntime.getGUILanguage().getLanguage()) == 0 ? true : false);
        }

    }

    /**
     * Sets the state of all GUI menus after a change occurred (e.g. a window
     * was closed or got focus)
     */
    private void setMenuState() {
        if (frontFrame != null) {
            toggleMetadataViewMenuItem.setEnabled(frontFrame.hasMetadataView());
            if (frontFrame.isMetadataVisible()) {
                toggleMetadataViewMenuItem.setText(Messages.getString("HIClientGUI.89")); //$NON-NLS-1$
            } else {
                toggleMetadataViewMenuItem.setText(Messages.getString("HIClientGUI.90")); //$NON-NLS-1$
            }
        } else {
            toggleMetadataViewMenuItem.setEnabled(false);
        }

        boolean hasWindows = false;
        if (components.size() > 0) {
            hasWindows = true;
        }
        nextWindowItem.setEnabled(hasWindows);
        prevWindowItem.setEnabled(hasWindows);

        // update admin / user items
        exportMenuItem.setEnabled(checkEditAbility(true));
        xmlImportMenuItem.setEnabled(checkAdminAbility(true));
        administrateProjectUsersMenuItem.setEnabled(checkAdminAbility(true));
        projectSettingsMenuItem.setEnabled(checkAdminAbility(true));
        projectTemplatesMenuItem.setEnabled(checkAdminAbility(true));
        administrateProjectPrefsMenuItem.setEnabled(checkEditAbility(true));
    }

    // DEBUG
    private HIComponentFrame getFrontMostFrame() {
        return frontFrame;
    }

    // DEBUG remove
    public void displayNotImplementedDialog() {
        displayInfoDialog(Messages.getString("HIClientGUI.91"), Messages.getString("HIClientGUI.92")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // DEBUG
    private void showDefaultProjectLayout() {
        registerComponent(new GroupBrowser());
    }

    @SuppressWarnings("unchecked")
    public int getComponentTypeCount(Class componentClass) {
        int count = 0;
        for (HIComponentFrame frame : components) {
            if (frame.getHIComponent().getClass() == componentClass) {
                count = count + 1;
            }
        }

        return count;
    }

    public HIComponentFrame getEditingComponentForElement(HiQuickInfo info) {
        if (info.getContentType() == HiBaseTypes.HI_OBJECT) {
            for (HIComponentFrame frame : components) {
                if (frame.getHIComponent() instanceof ObjectEditor) {
                    if (frame.getHIComponent().getBaseElement().getId() == info.getBaseID()) {
                        return frame;
                    }
                }
            }
        }
        if (info.getContentType() == HiBaseTypes.HI_VIEW || info.getContentType() == HiBaseTypes.HI_INSCRIPTION) {
            for (HIComponentFrame frame : components) {
                if (frame.getHIComponent() instanceof ObjectEditor) {
                    if (frame.getHIComponent().getBaseElement().getId() == info.getRelatedID()) {
                        return frame;
                    }
                }
            }
        }
        if (info.getContentType() == HiBaseTypes.HI_LAYER) {
            for (HIComponentFrame frame : components) {
                if (frame.getHIComponent() instanceof LayerEditor) {
                    if (frame.getHIComponent().getBaseElement().getId() == info.getRelatedID()) {
                        return frame;
                    }
                }
            }
        }
        if (info.getContentType() == HiBaseTypes.HI_GROUP) {
            for (HIComponentFrame frame : components) {
                if (frame.getHIComponent() instanceof GroupBrowser) {
                    if (frame.getHIComponent().getBaseElement().getId() == info.getBaseID()) {
                        return frame;
                    }
                }
            }
        }
        if (info.getContentType() == HiBaseTypes.HI_TEXT || info.getContentType() == HiBaseTypes.HIURL) {
            for (HIComponentFrame frame : components) {
                if (frame.getHIComponent() instanceof GenericMetadataEditor) {
                    if (frame.getHIComponent().getBaseElement().getId() == info.getBaseID()) {
                        return frame;
                    }
                }
            }
        }
        if (info.getContentType() == HiBaseTypes.HI_LIGHT_TABLE) {
            for (HIComponentFrame frame : components) {
                if (frame.getHIComponent() instanceof LightTableEditor) {
                    if (frame.getHIComponent().getBaseElement().getId() == info.getBaseID()) {
                        return frame;
                    }
                }
            }
        }

        return null; // no component was found
    }

    public boolean focusComponent(HIComponentFrame frame) {
        if (frame == frontFrame) {
            return true;
        }

        // bring frame to front
        frame.moveToFront();
        try {
            // try to activate frame focus
            frame.setSelected(true);
            frontFrame = frame;
            setMenuState();
        } catch (PropertyVetoException e1) {
            return false;
        }
        return true;
    }

    public boolean focusComponent(HIComponent component) {
        for (HIComponentFrame frame : components) {
            if (frame.getHIComponent() == component) {
                return focusComponent(frame);
            }
        }

        return false;
    }

    public void updateComponentTitle(HIComponent sender) {
        for (HIComponentFrame frame : components) {
            if (frame.getHIComponent() == sender) {
                frame.updateTitle();
            }
        }
    }

    public void registerComponent(final HIComponent component) {
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("serial")
            public void run() {

                // check if component is already registered
                HIComponentFrame componentFrame = null;

                for (HIComponentFrame regFrame : components) {
                    if (regFrame.getHIComponent() == component) {
                        componentFrame = regFrame;
                    }
                }

                // focus existing component on screen
                if (componentFrame != null) {
                    focusComponent(componentFrame);
                    return;
                }

                // register new component
                componentFrame = new HIComponentFrame(component);
                componentFrame.setFocusable(true);

                mdiPane.add(componentFrame, JLayeredPane.DEFAULT_LAYER);
                // attach close listener
                componentFrame.addInternalFrameListener(HIRuntime.getGui());
                // attach hierarchy listener (to update main GUI menus)
                componentFrame.addHierarchyListener(HIRuntime.getGui());
                // attach window move listener
                componentFrame.addComponentListener(HIRuntime.getGui());
                // attach window menu listener
                componentFrame.getHIComponent().getWindowMenuItem().addActionListener(HIRuntime.getGui());

                // DEBUG style frames and set initial size
                if (component instanceof LayerEditor || component instanceof GroupBrowser) {
                    componentFrame.setSize(HIRuntime.getGui().getContentPane().getSize().width, componentFrame.getSize().height);
                    if (component instanceof GroupBrowser) {
                        componentFrame.setSize(HIRuntime.getGui().getContentPane().getSize().width, componentFrame.getSize().height);
                        componentFrame.setLocation(
                                0,
                                0 + ((1 + getComponentTypeCount(GroupBrowser.class) * 50)));
                    }
                    componentFrame.setMetadataVisible(true);
                } else // center window on screen
                // TODO replace with better location system
                {
                    componentFrame.setLocation(
                            (HIRuntime.getGui().getContentPane().getSize().width / 2) - (componentFrame.getSize().width / 2) + ((getComponentTypeCount(component.getClass()) * 30)),
                            (HIRuntime.getGui().getContentPane().getSize().height / 2) - (componentFrame.getSize().height / 2) + ((getComponentTypeCount(component.getClass()) * 30)));
                }

                componentFrame.setVisible(true);
                focusComponent(componentFrame);

                // set default close hotkey for mac users
                if (System.getProperty("os.name").toLowerCase().indexOf("mac") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
                    componentFrame.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK), "closeOnMac"); //$NON-NLS-1$
                    componentFrame.getActionMap().put("closeOnMac", new AbstractAction() { //$NON-NLS-1$
                        public void actionPerformed(ActionEvent e) {
                            ((HIComponentFrame) e.getSource()).doDefaultCloseAction();
                        }
                    });
                }

                // add component to list of active GUI components
                components.add(componentFrame);

                // DEBUG remove
                windowMenu.add(componentFrame.getHIComponent().getWindowMenuItem());
                for (int i = 0; i < components.size(); i++) {
                    components.get(i).getHIComponent().getWindowMenuItem().setActionCommand("showComponent_" + i); //$NON-NLS-1$
                }
                // update window menu
                frontFrame = componentFrame;
                setMenuState();

            }
        });
    }

    public boolean deregisterComponent(HIComponentFrame componentFrame, boolean force) {
        // bring frame to front 
        focusComponent(componentFrame);
        setMenuState();

        if (!force) {
            // prompt the user if there are unsaved changes
            if (!componentFrame.getHIComponent().requestClose()) {
                return false;
            }
        }

        // remove listeners
        componentFrame.removeInternalFrameListener(this);
        componentFrame.removeHierarchyListener(this);
        componentFrame.getHIComponent().getWindowMenuItem().removeActionListener(this);
        componentFrame.removeComponentListener(HIRuntime.getGui());

        // DEBUG focus on last window in queue
        if (components.size() > 1) {
            int index = components.indexOf(componentFrame);
            index = index - 1;
            if (index < 0) {
                index = components.size() - 1;
            }
            focusComponent(components.get(index));
        }

        // de-register the component and destroy the GUI window
        componentFrame.setVisible(false);
        components.remove(componentFrame); // remove from list of active GUI components
        componentFrame.dispose();

        setMenuState();

        // DEBUG remove
        windowMenu.remove(componentFrame.getHIComponent().getWindowMenuItem());
        for (int i = 0; i < components.size(); i++) {
            components.get(i).getHIComponent().getWindowMenuItem().setActionCommand("showComponent_" + i); //$NON-NLS-1$
        }
        return true;
    }

    public boolean deregisterComponent(HIComponent component, boolean force) {
        HIComponentFrame compFrame = null;
        for (HIComponentFrame frame : components) {
            if (frame.getHIComponent() == component) {
                compFrame = frame;
            }
        }
        if (compFrame != null) {
            return deregisterComponent(compFrame, force);
        }

        return false;
    }

    /**
     * Tries to close all active components and their GUI windows, prompting the
     * user if there are unsaved changes
     *
     * @return <code>true</code> if all active component windows have been
     * registered; <code>false</code> otherwise, e.g. the user canceled the
     * operation
     */
    public boolean deregisterAllComponents() {
        while (components.size() > 0) {
            if (!deregisterComponent(components.get(components.size() - 1), false)) {
                displayInfoDialog(APPNAME, Messages.getString("HIClientGUI.100")); //$NON-NLS-1$ //$NON-NLS-2$
                return false;
            }
        }

        return true;
    }

    /**
     * Asks to user to save all open / edited elements without actually closing
     * the GUI component frames
     *
     * @return <code>true</code> if all changed elements have been saved;
     * <code>false</code> otherwise, e.g. the user canceled the operation
     */
    public boolean saveAllOpenWork() {
        for (HIComponentFrame frame : components) {
            if (!frame.getHIComponent().requestClose()) {
                return false;
            }
        }

        return true;
    }

    /**
     * called by components to notify GUI that an element has been moved to the
     * trash The GUI will check if element is currently being edited by another
     * component and force-close that component
     *
     * @param baseID id of item in trash
     */
    public void notifyItemSentToTrash(long baseID) {
        Vector<HIComponentFrame> componentsToDelete = new Vector<HIComponentFrame>();
        // find components that edit this item
        for (HIComponentFrame frame : components) {
            if (frame.getHIComponent().getBaseElement() != null && frame.getHIComponent().getBaseElement().getId() == baseID) {
                componentsToDelete.addElement(frame);
            }
        }
        // force-close found components
        for (HIComponentFrame frame : componentsToDelete) {
            deregisterComponent(frame, true);
        }
    }

    /**
     * propagates changes of a base element (group, object, layer, etc.) to all
     * other GUI components / frames so all frames and views are in sync and
     * display the updated data This method should be called by a HIComponent
     * after a "save" operation.
     *
     * @param message the type of update (add, delete, change, ...) specified by
     * HIComponent.HIMessageTypes enum
     * @param base HI Editor element that was updated
     * @param sender component that updated this element
     */
    public void sendMessage(HIMessageTypes message, HiBase base, HIComponent sender) {
        for (HIComponentFrame frame : components) {
            if (frame.getHIComponent() != sender) // don´t send message back to sender
            {
                frame.getHIComponent().receiveMessage(message, base);
            }
        }
    }

    public void openContentEditor(long baseID, HIComponent sender) {
        HiQuickInfo info = null;
        try {
            info = HIRuntime.getManager().getBaseQuickInfo(baseID);
            if (info != null) {
                openContentEditor(info, sender);
            }
        } catch (HIWebServiceException wse) {
            reportError(wse, sender);
            return;
        }
    }

    public void openContentEditor(final HiQuickInfo content, HIComponent sender) {
		// check if this element is already being edited by a component, if so bring that window to front
        // instead of opening a new window
        HIComponentFrame editingFrame = getEditingComponentForElement(content);
        if (editingFrame != null) {
            focusComponent(editingFrame);

            if (content.getContentType() == HiBaseTypes.HI_VIEW || content.getContentType() == HiBaseTypes.HI_INSCRIPTION) {
                ((ObjectEditor) editingFrame.getHIComponent()).displayContentByID(content.getBaseID());
            }

            if (content.getContentType() == HiBaseTypes.HI_LAYER) {
                ((LayerEditor) editingFrame.getHIComponent()).displayLayerByID(content.getBaseID());
            }

            return;
        }

        // open new editor for user requested content
        new Thread() {

            public void run() {

                startIndicatingServiceActivity();
                GenericMetadataEditor editor = null;
                if (content.getContentType() == HiBaseTypes.HI_TEXT) {
                    try {
                        editor = new GenericMetadataEditor((HiText) manager.getBaseElement(content.getBaseID()));
                        registerComponent(editor);
                    } catch (HIWebServiceException wse) {
                        reportError(wse, null);
                        return;
                    }
                } else if (content.getContentType() == HiBaseTypes.HI_VIEW || content.getContentType() == HiBaseTypes.HI_INSCRIPTION) {
                    try {
                        HiObject object = (HiObject) manager.getBaseElement(content.getRelatedID());
                        ObjectEditor objEditor = new ObjectEditor(object, content.getBaseID());
                        registerComponent(objEditor);
                    } catch (HIWebServiceException wse) {
                        reportError(wse, null);
                        return;
                    }
                } else if (content.getContentType() == HiBaseTypes.HI_LAYER) {
                    try {
                        HiView view = (HiView) manager.getBaseElement(content.getRelatedID());
                        openLayerEditor(view, content.getBaseID());
                    } catch (HIWebServiceException wse) {
                        reportError(wse, null);
                        return;
                    }
                } else if (content.getContentType() == HiBaseTypes.HIURL) {
                    try {
                        editor = new GenericMetadataEditor((Hiurl) manager.getBaseElement(content.getBaseID()));
                        registerComponent(editor);
                    } catch (HIWebServiceException wse) {
                        reportError(wse, null);
                        return;
                    }
                } else if (content.getContentType() == HiBaseTypes.HI_LIGHT_TABLE) {
                    try {
                        HiLightTable lightTable = (HiLightTable) manager.getBaseElement(content.getBaseID());
                        LightTableEditor ltEditor = new LightTableEditor(lightTable);
                        registerComponent(ltEditor);
                    } catch (HIWebServiceException wse) {
                        reportError(wse, null);
                        return;
                    }
                } else if (content.getContentType() == HiBaseTypes.HI_OBJECT) {
                    try {
                        HiObject object = (HiObject) manager.getBaseElement(content.getBaseID());
                        ObjectEditor objEditor = new ObjectEditor(object);
                        registerComponent(objEditor);
                    } catch (HIWebServiceException wse) {
                        reportError(wse, null);
                        return;
                    }
                } else if (content.getContentType() == HiBaseTypes.HI_GROUP) {
                    try {
                        HiGroup group = (HiGroup) manager.getBaseElement(content.getBaseID());
                        GroupBrowser browser = new GroupBrowser(group);
                        registerComponent(browser);
                    } catch (HIWebServiceException wse) {
                        reportError(wse, null);
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(HIRuntime.getGui(), "GUI ERROR: Don´t know how to handle " + content.getContentType() + " (" + content.getBaseID() + ") yet!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                stopIndicatingServiceActivity();

            }

        }.start();

    }

    public void openLayerEditor(HiView view) {
        openLayerEditor(view, -1);
    }

    private void openLayerEditor(final HiView view, final long layerID) {
        // scan available memory and offer user the choice of using a lower res version of his image
        System.gc(); // garbage collect to get more accurate free memory reading
        long neededMem = (view.getWidth() // image width at 100%
                * view.getHeight() // image height at 100%
                * 3);	// RGB - model (3 bytes per pixel)
        System.out.println("total: " + Runtime.getRuntime().totalMemory());
        System.out.println("free: " + Runtime.getRuntime().freeMemory());

        if ((Runtime.getRuntime().freeMemory() - neededMem) < HIRuntime.MINIMUM_FREE_MEMORY) {
            boolean choice = displayUserYesNoDialog(Messages.getString("HIClientGUI.104"), //$NON-NLS-1$
                    Messages.getString("HIClientGUI.105") + "\n" //$NON-NLS-1$ //$NON-NLS-2$
                    + Messages.getString("HIClientGUI.107") + " (" //$NON-NLS-1$ //$NON-NLS-2$
                    + view.getWidth() + "*" + view.getHeight() + " px) " + Messages.getString("HIClientGUI.111") + "\n\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    + Messages.getString("HIClientGUI.113")); //$NON-NLS-1$
            if (choice) {
                displayNotImplementedDialog();
                return;
            } else {
                return;
            }
        }

        // execute in separate thread
        new Thread() {
            public void run() {
                startIndicatingServiceActivity();
                PlanarImage image;
                try {
                    image = HIRuntime.getManager().getImage(view.getId(), HiImageSizes.HI_FULL, true);
                    stopIndicatingServiceActivity();
                    if (image == null) { // catch the unlikely event that the server did not throw an error but also did not send an image
                        displayInfoDialog(Messages.getString("HIClientGUI.114"), Messages.getString("HIClientGUI.115")); //$NON-NLS-1$ //$NON-NLS-2$
                        return;
                    }

                    // pre-select user requested layer if possible
                    if (layerID > 0) {
                        registerComponent(new LayerEditor(view, image, layerID));
                    } else {
                        registerComponent(new LayerEditor(view, image));
                    }

                    System.gc();

                } catch (HIWebServiceException wse) {
                    reportError(wse, null);
                    return;
                }
            }
        }.start();

    }

    public void setGUIEnabled(boolean enabled) {
        fileMenu.setEnabled(enabled);
        projectMenu.setEnabled(enabled);
        toolsMenu.setEnabled(enabled);
        windowMenu.setEnabled(enabled);
        helpMenu.setEnabled(enabled);
    }

    public void startIndicatingServiceActivity() {
        startIndicatingServiceActivity(false);
    }

    public void startIndicatingServiceActivity(boolean showImmediately) {
        if (serviceActivity) {
            return;
        }

        serviceActivity = true;

        mdiPane.setCursor(waitCursor);
        HIRuntime.getGui().setGUIEnabled(false);
        setMessage(Messages.getString("HIClientGUI.20")); //$NON-NLS-1$

        if (showImmediately) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (serviceActivity) {
                        infoGlassPane.setVisible(true);
                    }
                }
            });
        } else {
            // DEBUG
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        return;
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (serviceActivity) {
                                infoGlassPane.setVisible(true);
                            }
                        }
                    });
                }
            }.start();
        }
    }

    public void setProgress(int progress) {
        infoGlassPane.setProgress(progress);
    }

    public void setMessage(String message) {
        infoGlassPane.setMessage(message);
    }

    public void stopIndicatingServiceActivity() {
        if (!serviceActivity) {
            return;
        }

        mdiPane.setCursor(Cursor.getDefaultCursor());
        serviceActivity = false;
        HIRuntime.getGui().setGUIEnabled(true);

        // DEBUG
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                infoGlassPane.setVisible(false);
            }
        });

    }

    // TODO: style
    public int displayUserChoiceDialog(String title, String message) {
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_CANCEL_OPTION);
    }

    // TODO: style
    public boolean displayUserYesNoDialog(String title, String message) {
        int answer = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }

    // TODO: style
    public void displayInfoDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // DEBUG
    public void displayReconnectDialog() {
        if (HIRuntime.getManager().getState() != HIWebServiceManager.WSStates.RECONNECT) {
            HIRuntime.getManager().setReconnect();
            int answer = 0;
            if (HIRuntime.OAUTHMode) {
                // cannot reconnect OAUTH / Prometheus user as token is only valid for single login...
                displayInfoDialog(Messages.getString("HIClientGUI.117"), Messages.getString("HIClientGUI.116"));
                tryLogoutAndExit();
            } else {
                answer = JOptionPane.showConfirmDialog(this, Messages.getString("HIClientGUI.116"), Messages.getString("HIClientGUI.117"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (answer == JOptionPane.OK_OPTION) {
                try {
                    if (HIRuntime.getManager().reconnect() == false) {
                        if (deregisterAllComponents()) {
                            if (!handleLogin()) {
                                tryLogoutAndExit();
                            } else {
                                chooseProject();
                                showDefaultProjectLayout();
                            }
                        } else {
                            System.exit(1);
                        }
                    }
                } catch (HIWebServiceException wse) {
                    reportError(wse, null);
                    if (deregisterAllComponents()) {
                        if (!handleLogin()) {
                            tryLogoutAndExit();
                        } else {
                            chooseProject();
                            showDefaultProjectLayout();
                        }
                    } else {
                        System.exit(1);
                    }
                }
            } else {
                if (deregisterAllComponents()) {
                    if (!handleLogin()) {
                        tryLogoutAndExit();
                    } else {
                        chooseProject();
                        showDefaultProjectLayout();
                    }
                } else {
                    System.exit(1);
                }
            }
        }
    }

    public void displayMaintenanceDialog(HIWebServiceException wse) {
        HIRuntime.getGui().displayInfoDialog(
                Messages.getString("HIClientGUI.118"), //$NON-NLS-1$
                "<html>" + Messages.getString("HIClientGUI.120") + "<br><br>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                "<b>" + Messages.getString("HIClientGUI.123") + "</b><br>" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                wse.getCause().getMessage()
                + "</html>"); //$NON-NLS-1$
    }

    public void clearLastWSError() {
        lastWSError = null;
    }

    public HIWebServiceException getLastWSError() {
        return lastWSError;
    }

    public void reportError(HIWebServiceException wse, HIComponent sender) {
		// TODO: implement user notification
        // differentiate between fatal and recoverable error
        // close offending component
        serviceActivity = true;
        stopIndicatingServiceActivity();
        lastWSError = wse;

        if (!displayingError) {
            if (wse.getErrorType() == HIWebServiceException.HIerrorTypes.RECONNECT) {
                displayReconnectDialog();
            } else if (wse.getErrorType() == HIWebServiceException.HIerrorTypes.MAINTENANCE) {
                // handle maintenance mode
                displayMaintenanceDialog(wse);
                displayingError = false;
                // de-register all components and logout
                while (components.size() > 0) {
                    deregisterComponent(components.get(0), true);
                }
                if (!handleLogin()) {
                    tryLogoutAndExit();
                    System.exit(1);
                } else {
                    chooseProject();
                    showDefaultProjectLayout();
                }
            } else {
                wse.getCause().printStackTrace();
                // process unknown / server side error
                JOptionPane.showMessageDialog(this, "Web Service Error: " + wse.getCause().getClass() + "\n\nReason: " + wse.getCause().getMessage() + //$NON-NLS-1$ //$NON-NLS-2$
                        "\n"); //$NON-NLS-1$

				// DISABLED open feedback for user
                // registerComponent(new FeedbackModule(true));
            }
        }
        displayingError = false;
    }

    public boolean checkEditAbility(boolean silent) {
        if (HIRuntime.getManager().getCurrentRole() == HiRoles.GUEST) {
            if (!silent) {
                JOptionPane.showMessageDialog(
                        HIRuntime.getGui(),
                        Messages.getString("HIClientGUI.128"), //$NON-NLS-1$
                        Messages.getString("HIClientGUI.129"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            }
            return false;
        }
        return true;
    }

    public boolean checkAdminAbility(boolean silent) {
        if (HIRuntime.getManager().getCurrentRole() != HiRoles.ADMIN) {
            if (!silent) {
                JOptionPane.showMessageDialog(
                        HIRuntime.getGui(),
                        Messages.getString("HIClientGUI.130"), //$NON-NLS-1$
                        Messages.getString("HIClientGUI.131"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            }
            return false;
        }
        return true;
    }

    /* ************************************************
     * GUI Listerners for Mouse, Window state and Menus
     * ************************************************
     */
	// ---------------------------------------------------
    public void actionPerformed(ActionEvent e) {

        // DEBUG
        if (e.getActionCommand().equalsIgnoreCase("notImplemented")) //$NON-NLS-1$
        {
            displayNotImplementedDialog();
        }

        /* *****************
         * File Menu actions
         * *****************
         */
        if (e.getActionCommand().equalsIgnoreCase("about")) //$NON-NLS-1$
        {
            new AboutDialog(this).setVisible(true);
        }

        if (e.getActionCommand().equalsIgnoreCase("accountSettings")) { //$NON-NLS-1$
            // check if account settings are already open
            if (getComponentTypeCount(AccountSettings.class) > 0) {
                // extract and focus frame
                for (HIComponentFrame frame : components) {
                    if (frame.getHIComponent() instanceof AccountSettings) {
                        focusComponent(frame);
                    }
                }
            } else {
                // open new account settings
                registerComponent(new AccountSettings(HIRuntime.getManager().getCurrentUser()));
            }
        }

        if (e.getActionCommand().startsWith("setLang_")) { //$NON-NLS-1$
            int index = Integer.parseInt(e.getActionCommand().substring(8));
            HIRuntime.setGUILanguage(HIRuntime.supportedLanguages[index]);
            setMenuText();
            for (HIComponentFrame frame : components) {
                frame.getHIComponent().updateLanguage();
            }
            setMenuState();
        }

        if (e.getActionCommand().equalsIgnoreCase("exit")) //$NON-NLS-1$
        // warn user
        {
            if (displayUserYesNoDialog(Messages.getString("HIClientGUI.2"), Messages.getString("HIClientGUI.138"))) //$NON-NLS-1$ //$NON-NLS-2$
            {
                tryLogoutAndExit();
            }
        }

        if (e.getActionCommand().equalsIgnoreCase("changeUser")) //$NON-NLS-1$
        // warn user
        {
            if (displayUserYesNoDialog(Messages.getString("HIClientGUI.140"), Messages.getString("HIClientGUI.141"))) //$NON-NLS-1$ //$NON-NLS-2$
            {
                if (deregisterAllComponents()) {
                    if (!handleLogin()) {
                        tryLogoutAndExit();
                    } else {
                        chooseProject();
                        showDefaultProjectLayout();
                    }
                }
            }
        }

        /* *********************
         * Project Menu actions
         * *********************
         */
        if (e.getActionCommand().equalsIgnoreCase("xmlImportProject")) //$NON-NLS-1$
        {
            handleXMLImport();
        }

        if (e.getActionCommand().equalsIgnoreCase("exportProject")) //$NON-NLS-1$
        {
            handleExport();
        }

        if (e.getActionCommand().equalsIgnoreCase("projectSettings")) { //$NON-NLS-1$
            // check if project settings component is already open
            if (getComponentTypeCount(ProjectSettings.class) > 0) {
                // extract and focus frame
                for (HIComponentFrame frame : components) {
                    if (frame.getHIComponent() instanceof ProjectSettings) {
                        focusComponent(frame);
                    }
                }
            } else {
                // open new account settings
                registerComponent(new ProjectSettings());
            }
        }

        if (e.getActionCommand().equalsIgnoreCase("templateEditor")) { //$NON-NLS-1$
            // check if template editor is already open
            if (getComponentTypeCount(TemplateEditor.class) > 0) {
                // extract and focus frame
                for (HIComponentFrame frame : components) {
                    if (frame.getHIComponent() instanceof TemplateEditor) {
                        focusComponent(frame);
                    }
                }
            } else {
                // check for open object editors
                if (getComponentTypeCount(ObjectEditor.class) > 0) {
                    // display warning dialog
                    boolean closeObjectEditors = displayUserYesNoDialog(Messages.getString("HIClientGUI.200"), Messages.getString("HIClientGUI.201")
                            + "\n\n" + Messages.getString("HIClientGUI.202"));

                    if (closeObjectEditors) {
                        Vector<HIComponentFrame> objectEditorFrames = new Vector<HIComponentFrame>();
                        for (HIComponentFrame frame : components) {
                            if (frame.getHIComponent() instanceof ObjectEditor) {
                                objectEditorFrames.addElement(frame);
                            }
                        }
                        while (objectEditorFrames.size() > 0) {
                            if (!deregisterComponent(objectEditorFrames.get(0), false)) {
                                return;
                            } else {
                                objectEditorFrames.remove(0);
                            }
                        }

                    } else {
                        return;
                    }
                }

                // open new account settings
                registerComponent(new TemplateEditor());
            }
        }

        if (e.getActionCommand().equalsIgnoreCase("librarySettings")) { //$NON-NLS-1$
            // check if account settings are already open
            if (getComponentTypeCount(PreferenceManager.class) > 0) {
                // extract and focus frame
                for (HIComponentFrame frame : components) {
                    if (frame.getHIComponent() instanceof PreferenceManager) {
                        focusComponent(frame);
                    }
                }
            } else {
                // open new account settings
                registerComponent(new PreferenceManager());
            }
        }

        if (e.getActionCommand().equalsIgnoreCase("manageProjectUsers")) { //$NON-NLS-1$
            HIComponentFrame userManagerFrame = null;
            for (HIComponentFrame frame : components) {
                if (frame.getHIComponent() instanceof ProjectUsersManager) {
                    userManagerFrame = frame;
                }
            }

            if (userManagerFrame == null) {
                registerComponent(new ProjectUsersManager());
            } else {
                focusComponent(userManagerFrame);
            }
        }

        if (e.getActionCommand().equalsIgnoreCase("startPubTool")) //$NON-NLS-1$
        {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(manager.getServerURL() + "publication/?session=" + manager.getEndpointSession() + "&lang=" + HIRuntime.getGUILanguage().getLanguage().toLowerCase()));
                } catch (IOException | URISyntaxException ex) {
                    Logger.getLogger(HIClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (e.getActionCommand().equalsIgnoreCase("livePreview")) //$NON-NLS-1$
        {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(manager.getServerURL() + "previewer/?session=" + manager.getEndpointSession()));
                } catch (IOException | URISyntaxException ex) {
                    Logger.getLogger(HIClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (e.getActionCommand().equalsIgnoreCase("changeProject")) //$NON-NLS-1$
        // warn user
        {
            if (displayUserYesNoDialog(Messages.getString("HIClientGUI.3"), Messages.getString("HIClientGUI.147"))) //$NON-NLS-1$ //$NON-NLS-2$
            {
                if (deregisterAllComponents()) {
                    chooseProject();
                    showDefaultProjectLayout();
                }
            }
        }


        /* *****************
         * Tool Menu actions
         * *****************
         */
        if (e.getActionCommand().equalsIgnoreCase("newBrowser")) //$NON-NLS-1$
        {
            registerComponent(new GroupBrowser());
        }

        if (e.getActionCommand().equalsIgnoreCase("newSearch")) //$NON-NLS-1$
        {
            registerComponent(new SearchModule());
        }

        if (e.getActionCommand().equalsIgnoreCase("repositoryImport")) { //$NON-NLS-1$
            // check if repository import is already open
            if (getComponentTypeCount(RepositoryImport.class) > 0) {
                // extract and focus frame
                for (HIComponentFrame frame : components) {
                    if (frame.getHIComponent() instanceof RepositoryImport) {
                        focusComponent(frame);
                    }
                }
            } else {
                // open new repository import
                registerComponent(new RepositoryImport());
            }
        }

        /* *******************
         * Window Menu actions
         * *******************
         */
        if (e.getActionCommand().equalsIgnoreCase("nextWindow")) //$NON-NLS-1$
        {
            if (getFrontMostFrame() != null && components.size() > 0) {
                int index = components.indexOf(getFrontMostFrame());
                index = index + 1;
                if (index >= components.size()) {
                    index = 0;
                }
                HIComponentFrame frame = components.get(index);
                focusComponent(frame);
            }
        }

        if (e.getActionCommand().equalsIgnoreCase("previousWindow")) //$NON-NLS-1$
        {
            if (getFrontMostFrame() != null && components.size() > 0) {
                int index = components.indexOf(getFrontMostFrame());
                index = index - 1;
                if (index < 0) {
                    index = components.size() - 1;
                }
                HIComponentFrame frame = components.get(index);
                focusComponent(frame);
            }
        }

        if (e.getActionCommand().equalsIgnoreCase("toggleMetadata")) { //$NON-NLS-1$
            if (getFrontMostFrame() != null) {
                getFrontMostFrame().setMetadataVisible(!getFrontMostFrame().isMetadataVisible());
                setMenuState();
            }
        }

        if (e.getActionCommand().startsWith("showComponent_")) { //$NON-NLS-1$
            int index = Integer.parseInt(e.getActionCommand().substring(14));
            HIComponentFrame frame = components.get(index);
            focusComponent(frame);
            setMenuState();
        }

        /* *****************
         * Help Menu actions
         * *****************
         */
        if (e.getActionCommand().equalsIgnoreCase("visitWebsite")) { //$NON-NLS-1$
            try {
                Desktop.getDesktop().browse(new URI("http://hyperimage.sourceforge.net/")); //$NON-NLS-1$
            } catch (Exception e1) {
                displayInfoDialog(Messages.getString("HIClientGUI.10"), Messages.getString("HIClientGUI.14") + "\n\n" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        Messages.getString("HIClientGUI.21") + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                        "http://hyperimage.sourceforge.net/"); //$NON-NLS-1$
            }
        }
        if (e.getActionCommand().equalsIgnoreCase("openHelp")) { //$NON-NLS-1$
            try {
                Desktop.getDesktop().browse(new URI("http://manual.hyperimage.ws/")); //$NON-NLS-1$
            } catch (Exception e1) {
                displayInfoDialog(Messages.getString("HIClientGUI.10"), Messages.getString("HIClientGUI.14") + "\n\n" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        Messages.getString("HIClientGUI.21") + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                        "http://hyperimage.sourceforge.net/"); //$NON-NLS-1$
            }
        }
		// DISABLED USER FEEDBACK
		/*
         if ( e.getActionCommand().equalsIgnoreCase("FEEDBACK") ) { //$NON-NLS-1$
         // check if feedback view is already open
         if ( getComponentTypeCount(FeedbackModule.class) > 0 ) {
         // extract and focus frame
         for ( HIComponentFrame frame : components )
         if ( frame.getHIComponent() instanceof FeedbackModule )
         focusComponent(frame);
         } else {
         // open new feedback form
         registerComponent(new FeedbackModule());
         }
         } 
         */

        // DEBUG remove debug actions
        if (e.getActionCommand().equalsIgnoreCase("WS_TEST")) { //$NON-NLS-1$
            // do nothing
            System.out.println("starting tests..."); //$NON-NLS-1$

            startIndicatingServiceActivity();
            try {

                HiFlexMetadataTemplate template = null;
                for (HiFlexMetadataTemplate temp : HIRuntime.getManager().getProject().getTemplates()) {
                    if (temp.getNamespacePrefix().startsWith("dc")) {
                        template = temp;
                    }
                }
                if (template != null) {
                    System.out.println("removing...");
                    HiFlexMetadataSet set = null;
                    for (HiFlexMetadataSet tempSet : template.getEntries()) {
                        if (tempSet.getTagname().equalsIgnoreCase("media")) {
                            set = tempSet;
                        }
                    }
                    if (set != null) {
                        HIRuntime.getManager().removeSetFromTemplate(template, set);
                    }
//                                      HIRuntime.getManager().addSetToTemplate(template, "media", false);
//                                    System.out.println("removing...");
//                                    HIRuntime.getManager().removeTemplateFromProject(template);
                }
//				HIRuntime.getManager().rebuildSearchIndex();

            } catch (HIWebServiceException wse) {
                reportError(wse, null);
            }

            stopIndicatingServiceActivity();
            System.out.println("finished tests"); //$NON-NLS-1$
        }

        if (e.getActionCommand().equalsIgnoreCase("GTK")) { //$NON-NLS-1$
            try {
                UIManager.setLookAndFeel(
                        "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); //$NON-NLS-1$
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            } catch (InstantiationException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (UnsupportedLookAndFeelException e1) {
                e1.printStackTrace();
            }
            SwingUtilities.updateComponentTreeUI(this);
        } else if (e.getActionCommand().equalsIgnoreCase("SYSTEM")) { //$NON-NLS-1$
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.updateComponentTreeUI(this);
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (InstantiationException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (UnsupportedLookAndFeelException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

	// ----------------------------------------------------------------
    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        // warn user
        if (displayUserYesNoDialog(Messages.getString("HIClientGUI.4"), Messages.getString("HIClientGUI.163"))) //$NON-NLS-1$ //$NON-NLS-2$
        {
            tryLogoutAndExit();
        }
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

	// ---------------------------------------------------
    public void internalFrameActivated(InternalFrameEvent e) {
        // TODO Auto-generated method stub

    }

    public void internalFrameClosed(InternalFrameEvent e) {
        // TODO Auto-generated method stub

    }

    public void internalFrameClosing(InternalFrameEvent e) {
        // DEBUG
        HIComponentFrame compFrame = (HIComponentFrame) e.getSource();

        if (!deregisterComponent(compFrame, false)) {
            return;
        }

    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
        // TODO Auto-generated method stub

    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
        // TODO Auto-generated method stub

    }

    public void internalFrameIconified(InternalFrameEvent e) {
        // TODO Auto-generated method stub

    }

    public void internalFrameOpened(InternalFrameEvent e) {
        // TODO Auto-generated method stub

    }

	// ---------------------------------------------------
    // DEBUG
    public void hierarchyChanged(HierarchyEvent e) {
        HIComponentFrame frame = (HIComponentFrame) e.getSource();
        frontFrame = frame;
        setMenuState();
    }

	// ---------------------------------------------------
    @Override
    public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

    }

    @Override
    public void componentMoved(ComponentEvent e) {
        if (e.getComponent().getBounds().y < 0) {
            e.getComponent().setBounds(e.getComponent().getBounds().x,
                    0,
                    e.getComponent().getBounds().width,
                    e.getComponent().getBounds().height);
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub

    }

    @Override
    public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub

    }

}
