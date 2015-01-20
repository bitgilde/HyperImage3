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

package org.hyperimage.client.components;

import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.jai.PlanarImage;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.dialogs.LoginDialog;
import org.hyperimage.client.gui.dialogs.RepositoryDataDialog;
import org.hyperimage.client.gui.dnd.RepositoryTransferable;
import org.hyperimage.client.gui.lists.GroupContentsCellRenderer;
import org.hyperimage.client.gui.lists.GroupContentsList;
import org.hyperimage.client.gui.lists.QuickInfoCell;
import org.hyperimage.client.gui.lists.GroupContentsList.HI_ListDisplayStyles;
import org.hyperimage.client.gui.views.RepositoryBrowserView;
import org.hyperimage.client.gui.views.RepositoryContentsView;
import org.hyperimage.client.gui.views.RepositoryMetadataView;
import org.hyperimage.client.model.HIRepository;
import org.hyperimage.client.util.WSRepositoryImageLoaderThread;
import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiQuickInfo;
import org.hyperimage.client.ws.HiRepository;
import org.hyperimage.connector.fedora3.ws.HIFedora3Connector;
import org.hyperimage.connector.fedora3.ws.HIFedora3ConnectorService;
import org.hyperimage.connector.fedora3.ws.HIWSAssetNotFoundException_Exception;
import org.hyperimage.connector.fedora3.ws.HIWSAuthException_Exception;
import org.hyperimage.connector.fedora3.ws.HIWSDCMetadataException_Exception;
import org.hyperimage.connector.fedora3.ws.HIWSLoggedException_Exception;
import org.hyperimage.connector.fedora3.ws.HIWSNotBinaryException_Exception;
import org.hyperimage.connector.fedora3.ws.HIWSUTF8EncodingException_Exception;
import org.hyperimage.connector.fedora3.ws.HIWSXMLParserException_Exception;
import org.hyperimage.connector.fedora3.ws.HiHierarchyLevel;
import org.hyperimage.connector.fedora3.ws.HiMetadataRecord;
import org.hyperimage.connector.fedora3.ws.HiTypedDatastream;

/**
 * @author Jens-Martin Loebel
 */
public class RepositoryImport extends HIComponent implements TreeSelectionListener, ListSelectionListener, ActionListener {

	private RepositoryBrowserView browserView;
	private RepositoryContentsView contentsView;
	private RepositoryMetadataView metadataView;

	private int curIndex = -1;
	private int lastElementIndex = -1;
	private int lastElementSelectionCount = 0;
	private TreePath lastPath = null;
	private HI_ListDisplayStyles userSelectedStyle = HI_ListDisplayStyles.ICON_STYLE;
	private HIRepositoryLevelLoader levelLoader;
	private HashMap<String, PlanarImage> previewCache;

    private RepositoryDataDialog dataDialog = new RepositoryDataDialog(HIRuntime.getGui(), true);

	private HIFedora3Connector proxy = null;


	
	/**
	 * This class implements the Drag and Drop handler for repository items content.
 	 * 
	 * Class: GroupContentsTransferHandler
	 * @author Jens-Martin Loebel
	 *
	 */
	public class ImportTransferHandler extends TransferHandler {

		private static final long serialVersionUID = -8788292008094843014L;
		
		private RepositoryImport sender;
		
		
		public ImportTransferHandler(RepositoryImport sender) {
			this.sender = sender;
		}

		
		// -------- export methods ---------

		
		public int getSourceActions(JComponent c) {
			return COPY;
		}

		protected Transferable createTransferable(JComponent c) {
			ArrayList<HiQuickInfo> importElements = new ArrayList<HiQuickInfo>(contentsView.getContentsList().getSelectedIndices().length);
			for ( int index : contentsView.getContentsList().getSelectedIndices() )
				importElements.add(contentsView.getContents().get(index));
	
			return new RepositoryTransferable(importElements, sender);
		}

		protected void exportDone(JComponent c, Transferable t, int action) {
			/*
			if ( action == COPY )
				HIRuntime.getGui().displayInfoDialog(
						"Nicht Implementiert", 
						"Der Import aus externen Repositories ist in dieser Version noch nicht implementiert."
				);
			*/
		}


		// -------- import methods ---------


		public boolean canImport(TransferSupport supp) {
			// cannot import things into an external repository
			return false;
		}

		public boolean importData(TransferSupport supp) {
			if ( !canImport(supp) )
				return false;
			
			return false;
		}
	}
	
	
	// ----------------------------------------------------------------------------------------------------



	/**
	 * DEBUG
	 * Class: HIRepositoryLevelLoader
	 * Package: org.hyperimage.client.components
	 * @author Jens-Martin Loebel
	 *
	 */
	class HIRepositoryLevelLoader implements Runnable 
	{		
		private Thread thread;
		private String urn;


		public HIRepositoryLevelLoader()
		{
			this.thread = new Thread(this); 
		}

		public void loadLevel(String urn) {
			this.urn = urn;
			if ( thread.getState() == Thread.State.NEW )
				thread.start();
			else if ( thread.getState() == Thread.State.TERMINATED ) {
				thread = new Thread(this);
				thread.start();
			} else { 
				while ( thread.getState() == Thread.State.RUNNABLE ) ;
				thread = new Thread(this);
				thread.start();
			}
		}

		public void run() 
		{
			final List<HiHierarchyLevel> infoList;
			try {
				// DEBUG
				infoList = proxy.getHierarchyLevel(null, urn);

			} catch (Exception e) {
				contentsView.setContents(null);
				browserView.getRepositoryTree().setEnabled(true);
				return;
			}

			SwingUtilities.invokeLater(new Runnable()  {
				public void run() {
					if ( infoList != null ) {
						if ( infoList.size() >= HIRuntime.MAX_GROUP_ITEMS ) 
							setDisplayStyle(HI_ListDisplayStyles.LIST_STYLE);
						else
							setDisplayStyle(userSelectedStyle);
						contentsView.setContents(createQuickInfoForRepositoryContents(infoList));
					}
					browserView.getRepositoryTree().setEnabled(true);

					// scan items and load/attach previews from WS if necessary
					if ( contentsView.getContentsList().getDisplayStyle() == GroupContentsList.HI_ListDisplayStyles.ICON_STYLE)
						loadContentPreviews();

				}
			});



		}
	}


	// -------------------------------------------------------------------------------------------------


	public RepositoryImport() {
		super(Messages.getString("RepositoryImport.0"), Messages.getString("RepositoryImport.1")); //$NON-NLS-1$ //$NON-NLS-2$

		levelLoader = new HIRepositoryLevelLoader();
		previewCache = new HashMap<String, PlanarImage>();

		// init views
		try {
			browserView = new RepositoryBrowserView(HIRuntime.getManager().getRepositories());
		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
			browserView = new RepositoryBrowserView(new ArrayList<HiRepository>());			
		}
		contentsView = new RepositoryContentsView();
		metadataView = new RepositoryMetadataView();
		closeRepository();
		
		// init Drag and Drop
		contentsView.getContentsList().setDragEnabled(true);
		
		// register views
		views.add(browserView);
		lastPath = browserView.getRepositoryTree().getSelectionPath();
		views.add(contentsView);
		views.add(metadataView);

		// attach listeners
		browserView.getRepositoryTree().addTreeSelectionListener(this);
        browserView.getAddButton().addActionListener(this);
        browserView.getRemoveButton().addActionListener(this);
		contentsView.getContentsList().addListSelectionListener(this);
		contentsView.getContentsList().setTransferHandler(new ImportTransferHandler(this));

        // check privileges
        browserView.getAddButton().setEnabled(HIRuntime.getGui().checkAdminAbility(true));

	}

	public void updateLanguage() {
		super.updateLanguage();

		dataDialog.updateLanguage();
	}

	public HIRepository getActiveRepository() {
		if ( browserView.getSelectedIndex() < 0 ) return null;
		return browserView.getRepositories().get(browserView.getSelectedIndex());
	}
	
	// DEBUG
	public List<HiMetadataRecord> getRepositoryElementMetadata(HiQuickInfo info) {
		try {
			String urn = info.getPreview();
			if ( urn.endsWith(",IMAGE") ) //$NON-NLS-1$
				urn = urn.substring(0, urn.indexOf(",IMAGE")); //$NON-NLS-1$
			return proxy.getMetadataRecord(null, urn);
		} catch (HIWSAssetNotFoundException_Exception e) {
			return null;
		} catch (HIWSDCMetadataException_Exception e) {
			return null;
		} catch (HIWSLoggedException_Exception e) {
			return null;
		} catch (HIWSUTF8EncodingException_Exception e) {
			return null;
		} catch (HIWSXMLParserException_Exception e) {
			return null;
		}
	}

	// DEBUG
	public HiTypedDatastream getRepositoryElementData(HiQuickInfo info) {
		if ( getActiveRepository() == null ) return null;
		
		try {
                        HiTypedDatastream stream = proxy.getAssetData(null, info.getPreview());
			return stream;

		} catch (HIWSAssetNotFoundException_Exception e) {
			e.printStackTrace();
			return null;
		} catch (HIWSAuthException_Exception e) {
			e.printStackTrace();
			return null;
		} catch (HIWSLoggedException_Exception e) {
			e.printStackTrace();
			return null;
		} catch (HIWSNotBinaryException_Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	private List<HiQuickInfo> createQuickInfoForRepositoryContents( List<HiHierarchyLevel> levelList) {
		ArrayList<HiQuickInfo> infoList = new ArrayList<HiQuickInfo>();
		for ( HiHierarchyLevel level : levelList ) {
			if ( ! level.isLevel() ) {
				HiQuickInfo info = new HiQuickInfo();
				info.setContentType(HiBaseTypes.HI_REPOSITORY_ITEM);
				info.setBaseID(0);
				info.setCount(0);
				info.setRelatedID(0);
				info.setTitle(level.getDisplayName());
				info.setPreview(level.getURN());
				infoList.add(info);
			}
		}

		return infoList;
	}


	private void setDisplayStyle ( HI_ListDisplayStyles style ) {
		if ( contentsView.getContentsList().getDisplayStyle() == style )
			return;
		if ( style == HI_ListDisplayStyles.ICON_STYLE ) {
			contentsView.getContentsList().setDisplayStyle(HI_ListDisplayStyles.ICON_STYLE);
			contentsView.getListStyleButton().setIcon(contentsView.iconStyleIcon);
			contentsView.getListStyleButton().setToolTipText(Messages.getString("RepositoryImport.82")); //$NON-NLS-1$
			// load/attach preview images if necessary
			loadContentPreviews();
		} else {
			contentsView.getContentsList().setDisplayStyle(HI_ListDisplayStyles.LIST_STYLE);
			contentsView.getListStyleButton().setIcon(contentsView.listStyleIcon);
			contentsView.getListStyleButton().setToolTipText(Messages.getString("RepositoryImport.83")); //$NON-NLS-1$
		}
	}


	private boolean openRepository() {
		// sanity check
		if ( curIndex < 0 ) return false;

		HIRepository rep = browserView.getRepositories().get(curIndex);

		if ( ! rep.isPasswordStored() ) {
			// DEBUG check repo type
			if ( rep.getModel().getRepoType().compareTo("Fedora3") != 0 ) { //$NON-NLS-1$
				HIRuntime.getGui().displayInfoDialog(Messages.getString("RepositoryImport.85"), Messages.getString("RepositoryImport.86")); //$NON-NLS-1$ //$NON-NLS-2$
				closeRepository();
				return false;
			}

			// prompt user to enter credentials		
			LoginDialog loginDialog = HIRuntime.getGui().getLoginDialog();
			loginDialog.setInfoLabel(Messages.getString("RepositoryImport.87"), Color.blue); //$NON-NLS-1$
			loginDialog.pack();

			if ( loginDialog.promptLogin(rep.getModel().getUserName()) ) {
				rep.getModel().setUserName(loginDialog.getUserName());
				rep.getModel().setPassword(loginDialog.getPassword());
			} else {
				closeRepository();
				return false;
			}
		}

		// DEBUG
		HIFedora3ConnectorService service = null;
		HIRuntime.getGui().startIndicatingServiceActivity();
		try {
			service = new HIFedora3ConnectorService(
					new URL(rep.getModel().getUrl()),
					new javax.xml.namespace.QName("http://fedora3.connector.hyperimage.org/", "HIFedora3ConnectorService") //$NON-NLS-1$ //$NON-NLS-2$
			);
		} catch (MalformedURLException e1) {
			HIRuntime.getGui().stopIndicatingServiceActivity();
			e1.printStackTrace();
		} catch (Exception se) {
			HIRuntime.getGui().stopIndicatingServiceActivity();
			se.printStackTrace();
		}
		// check connection
		if ( service == null ) {
			HIRuntime.getGui().displayInfoDialog(Messages.getString("RepositoryImport.90"), Messages.getString("RepositoryImport.91")); //$NON-NLS-1$ //$NON-NLS-2$
			closeRepository();
			HIRuntime.getGui().stopIndicatingServiceActivity();
			return false;
		}				
		// create proxy
		proxy = service.getHIFedora3ConnectorPort();
		// try to login
		proxy.authenticate(rep.getModel().getUserName(), rep.getModel().getPassword());

		try {
			// check if login succeeded
			if ( proxy.getReposInfo() != null) {
				HIRuntime.getGui().stopIndicatingServiceActivity();
				return true;
			}

		} catch (Exception e) {
			// handle login exception
			HIRuntime.getGui().displayInfoDialog(Messages.getString("RepositoryImport.92"), Messages.getString("RepositoryImport.93")); //$NON-NLS-1$ //$NON-NLS-2$
			rep.getModel().setUserName(""); //$NON-NLS-1$
			rep.getModel().setPassword(""); //$NON-NLS-1$
			proxy = null;
			closeRepository();
			HIRuntime.getGui().stopIndicatingServiceActivity();
			return false;
		}		

		HIRuntime.getGui().stopIndicatingServiceActivity();
		return false;
	}

	private void closeRepository() {
		proxy = null;
		previewCache.clear();
		browserView.setSelectedIndex(-1);
		contentsView.setContents(new ArrayList<HiQuickInfo>());
		metadataView.setMetadataForElements(0, null);
		lastElementIndex = -1;
		lastElementSelectionCount = -1;
		}

	// DEBUG
	private void loadCurrentLevel() {
		if ( ! openRepository() ) return;

		browserView.getRepositoryTree().setEnabled(false);
		contentsView.prepareElementLoading();
		lastElementSelectionCount = 0;
		lastElementIndex = -1;
		metadataView.setMetadataForElements(0, null);
		// DEBUG levels / hierarchy not supported
		levelLoader.loadLevel(null);
	}


	private void loadContentPreviews() {
		if ( !openRepository() ) return;

		for ( HiQuickInfo info : contentsView.getContents() )
			loadContentPreview(info);
	}

	private void loadContentPreview(HiQuickInfo info) {
		GroupContentsCellRenderer renderer = (GroupContentsCellRenderer) contentsView.getContentsList().getCellRenderer();
		QuickInfoCell cell = renderer.getCellForContent(info);
		if ( cell != null ) {
			if ( cell.needsPreview() ) {
				WSRepositoryImageLoaderThread thumbLoader = new WSRepositoryImageLoaderThread();
				thumbLoader.loadImage(proxy, info.getPreview(), cell, contentsView.getContentsList(), previewCache);
			}
		}
	}



	// ----------------------------------------------------------------------------------------------------


	@Override
	public void valueChanged(TreeSelectionEvent e) {

		if ( browserView.getSelectedIndex() != curIndex ) {
			if ( curIndex != -1 ) previewCache.clear();

			// set active repository
			curIndex = browserView.getSelectedIndex();
			if ( curIndex >= 0 ) {
				loadCurrentLevel();
			} else closeRepository();
			
            browserView.getRemoveButton().setEnabled(HIRuntime.getGui().checkAdminAbility(true) && (curIndex >= 0 ? true: false));
		}

		if ( lastPath != browserView.getRepositoryTree().getSelectionPath() ) {
			lastPath = browserView.getRepositoryTree().getSelectionPath();


		}


	}

	
	// ----------------------------------------------------------------------------------------------------


	@Override
	public void valueChanged(ListSelectionEvent e) {
		if ( contentsView.getContentsList().getSelectedIndex() != lastElementIndex 
				|| contentsView.getContentsList().getSelectedIndices().length != lastElementSelectionCount ) {
			lastElementIndex = contentsView.getContentsList().getSelectedIndex();
			lastElementSelectionCount = contentsView.getContentsList().getSelectedIndices().length;
				
			List<HiMetadataRecord> records = null;
			if ( contentsView.getContentsList().getSelectedIndex() >= 0 ) {
				try {
					HIRuntime.getGui().startIndicatingServiceActivity();
					// DEBUG filter ", IMAGE" for Fedora 3 connector
					String urn = contentsView.getContents().get(contentsView.getContentsList().getSelectedIndex()).getPreview();
					if ( urn.toUpperCase().indexOf(",IMAGE") >= 0 ) //$NON-NLS-1$
						urn = urn.substring(0, urn.toUpperCase().indexOf(",IMAGE")); //$NON-NLS-1$

					records = proxy.getMetadataRecord(null, urn);
					HIRuntime.getGui().stopIndicatingServiceActivity();
						
				} catch (Exception rse) {
					HIRuntime.getGui().reportError(new HIWebServiceException(rse), this);
					metadataView.setMetadataForElements(0, null);		
					return;
				}
			}
			metadataView.setMetadataForElements(contentsView.getContentsList().getSelectedIndices().length, records);
		}
		
	}

	// ----------------------------------------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        // check privileges
        if ( !HIRuntime.getGui().checkAdminAbility(true) ) return;

        // add repository
        if ( e.getSource() == browserView.getAddButton() ) {
            HiRepository newRepos = dataDialog.showAddRepositoryDialog();

            if ( newRepos != null && HIRuntime.getGui().checkAdminAbility(true) ) {
                try {
                    // store new repository info
                    newRepos = HIRuntime.getManager().createRepository(newRepos);

                    // update GUI
                    if ( newRepos != null )
                        browserView.addRepository(newRepos);

                } catch (HIWebServiceException wse) {
                    HIRuntime.getGui().reportError(wse, this);
                    return;
                }
            }
        }
        
        // remove repository --> check permissions
        if ( e.getSource() == browserView.getRemoveButton() 
        		&& HIRuntime.getGui().checkAdminAbility(false)
        		&& getActiveRepository() != null ) {
        	
        	HIRepository repos = getActiveRepository();
        	
        	// ask user if he/she really wants to do this
			if ( ! HIRuntime.getGui().displayUserYesNoDialog(
					Messages.getString("RepositoryImport.2"), //$NON-NLS-1$
					Messages.getString("RepositoryImport.3") )) //$NON-NLS-1$
				return;

			try {
				// remove repository
				if ( HIRuntime.getManager().deleteRepository(repos.getModel()) ) {
					// TODO update GUI
					browserView.removeRepository(repos);
				}

				
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
			
        	
        }

    }


}
