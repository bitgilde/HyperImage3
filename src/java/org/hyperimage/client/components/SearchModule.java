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

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.dnd.ContentTransfer;
import org.hyperimage.client.gui.dnd.QuickInfoTransferable;
import org.hyperimage.client.gui.lists.GroupContentsCellRenderer;
import org.hyperimage.client.gui.lists.GroupContentsList;
import org.hyperimage.client.gui.lists.QuickInfoCell;
import org.hyperimage.client.gui.lists.GroupContentsList.HI_ListDisplayStyles;
import org.hyperimage.client.gui.views.SearchResultsView;
import org.hyperimage.client.gui.views.SearchView;
import org.hyperimage.client.util.WSImageLoaderThread;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiImageSizes;
import org.hyperimage.client.ws.HiQuickInfo;

/**
 * @author Jens-Martin Loebel
 */
public class SearchModule extends HIComponent implements ActionListener, MouseListener {

	private HIComponent searchModule = this;

	private SearchView searchView;
	private SearchResultsView resultsView;
	private HI_ListDisplayStyles userSelectedStyle = HI_ListDisplayStyles.ICON_STYLE;
	private SearchResultLoader resultsLoader;

	private boolean isSimpleSearch = true;



	/**
	 * This class implements the Drag and Drop handler for search results.
	 * As per SDD: copying of search results to another group as well as linking is supported
	 * to another group or to/from the trash.
	 * 
	 * Class: SearchResultsTransferHandler
	 * @author Jens-Martin Loebel
	 *
	 */
	class SearchResultsTransferHandler extends TransferHandler {

		private static final long serialVersionUID = -5928024106665575189L;

		
		private SearchModule search;


		public SearchResultsTransferHandler(SearchModule search) {
			this.search = search;
		}


		// -------- export methods ---------


		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE+LINK;
		}

		protected Transferable createTransferable(JComponent c) {
			Vector<HiQuickInfo> elements = new Vector<HiQuickInfo>();

				// build quick info list
				for ( Object object : resultsView.getContentsList().getSelectedValues() )
					elements.add((HiQuickInfo) object);

			return new QuickInfoTransferable(
					new ContentTransfer(elements, null, search)
			);
		}

		protected void exportDone(JComponent c, Transferable t, int action) {
			// focus source component if drop failed
			if ( action == NONE ) HIRuntime.getGui().focusComponent(search);
		}


		// -------- import methods ---------
		
		// nothing can be imported into a search result view

		public boolean canImport(TransferSupport supp) {
			// focus target component if necessary
			HIRuntime.getGui().focusComponent(search);

			return false;
		}

		public boolean importData(TransferSupport supp) {
			return false;
		}
	}



	// ------------------------------------------------------------------------------------------------------



	// DEBUG refactor
	class SearchResultLoader implements Runnable 
	{		
		private Thread thread;
		
		private boolean isSimple;
		private String text;
		private List<String> fields, contents;


		public SearchResultLoader()
		{
			this.thread = new Thread(this); 
		}

		public void simpleSearch(String text) {
			this.text = text;
			this.isSimple = true;
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

		public void fieldSearch(List<String> fields, List<String> contents) {
			this.fields = fields;
			this.contents = contents;
			this.isSimple = false;
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
			final List<HiQuickInfo> infoList;
			try {
				// DEBUG
				if ( isSimple )
					infoList = HIRuntime.getManager().simpleSearch(text, searchView.getSelectedLanguage());
				else 
					infoList = HIRuntime.getManager().fieldSearch(fields, contents, searchView.getSelectedLanguage());
					
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, searchModule);
				resultsView.setContents(null);
				searchView.setSearchEnabled(true);
				return;
			}

			SwingUtilities.invokeLater(new Runnable()  {
				public void run() {
					if ( infoList != null ) {
						if ( infoList.size() >= HIRuntime.MAX_GROUP_ITEMS ) 
							setDisplayStyle(HI_ListDisplayStyles.LIST_STYLE);
						else
							setDisplayStyle(userSelectedStyle);
						resultsView.setContents(infoList);
					}
					searchView.setSearchEnabled(true);

					// scan items and load/attach previews from WS if necessary
					if ( resultsView.getContentsList().getDisplayStyle() == GroupContentsList.HI_ListDisplayStyles.ICON_STYLE)
						loadContentPreviews();

				}
			});



		}
	}


	// ------------------------------------------------------------------------------------------------------



	public SearchModule() {
		super(Messages.getString("SearchModule.0"), Messages.getString("SearchModule.1")); //$NON-NLS-1$ //$NON-NLS-2$

		resultsLoader = new SearchResultLoader();

		// init views
		searchView = new SearchView(HIRuntime.getManager().getProject().getLanguages(), HIRuntime.getManager().getProject().getDefaultLanguage());
		resultsView = new SearchResultsView();
		resultsView.setTitle(Messages.getString("SearchModule.2")); //$NON-NLS-1$

		// register views
		views.add(searchView);
		views.add(resultsView);

		// init Drag and Drop
		resultsView.getContentsList().setTransferHandler(new SearchResultsTransferHandler(this));


		// attach listeners
		searchView.attachListeners(this);
		resultsView.getContentsList().addMouseListener(this);
	}

	public void updateLanguage() {
		super.updateLanguage();
		
		if ( resultsView.getContentsList().getDisplayStyle() == HI_ListDisplayStyles.ICON_STYLE )
			resultsView.getListStyleButton().setToolTipText(Messages.getString("SearchModule.10")); //$NON-NLS-1$
		else
			resultsView.getListStyleButton().setToolTipText(Messages.getString("SearchModule.11")); //$NON-NLS-1$
		
		updateTitle(isSimpleSearch);
	}
	
	
	public void receiveMessage(HIMessageTypes message, HiBase base ) {
		if ( message == HIMessageTypes.LANGUAGE_ADDED || message == HIMessageTypes.LANGUAGE_REMOVED ) {	
			// rebuild language field
			searchView.rebuildLanguages(HIRuntime.getManager().getProject().getLanguages());
		}
		
		if ( message == HIMessageTypes.DEFAULT_LANGUAGE_CHANGED ) {
			// reload search contents
			if ( resultsView.getContents() == null ) return;
			HIRuntime.getGui().startIndicatingServiceActivity();
			try {
				for (int i=0 ; i < resultsView.getContents().size(); i++) {
					HiQuickInfo info = resultsView.getContents().get(i);
					info = HIRuntime.getManager().getBaseQuickInfo(info.getBaseID());
					resultsView.getContents().set(i, info);
				}
				resultsView.setContents(resultsView.getContents());
				loadContentPreviews();
			} catch( HIWebServiceException wse ) {
				HIRuntime.getGui().reportError(wse, this);
			}
			HIRuntime.getGui().stopIndicatingServiceActivity();
		}

	}

	private void updateTitle(boolean isSimple) {
		this.isSimpleSearch = isSimple;
		String title = searchView.getSearchText().trim();
		if ( title.length() == 0 ) {
			title = Messages.getString("SearchModule.3"); //$NON-NLS-1$
		} else if ( isSimple )
			title = Messages.getString("SearchModule.4")+" \""+title+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		else {
			title = searchView.getFieldSearchText().trim();
			title = Messages.getString("SearchModule.7")+" \""+title+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		setTitle(title);
		getWindowMenuItem().setText(getTitle());
		HIRuntime.getGui().updateComponentTitle(this);
	}


	private void setDisplayStyle ( HI_ListDisplayStyles style ) {
		if ( resultsView.getContentsList().getDisplayStyle() == style )
			return;
		if ( style == HI_ListDisplayStyles.ICON_STYLE ) {
			resultsView.getContentsList().setDisplayStyle(HI_ListDisplayStyles.ICON_STYLE);
			resultsView.getListStyleButton().setIcon(resultsView.iconStyleIcon);
			resultsView.getListStyleButton().setToolTipText(Messages.getString("SearchModule.10")); //$NON-NLS-1$
			// load/attach preview images if necessary
			loadContentPreviews();
		} else {
			resultsView.getContentsList().setDisplayStyle(HI_ListDisplayStyles.LIST_STYLE);
			resultsView.getListStyleButton().setIcon(resultsView.listStyleIcon);
			resultsView.getListStyleButton().setToolTipText(Messages.getString("SearchModule.11")); //$NON-NLS-1$
		}
	}



	private void loadContentPreviews() {
		for ( HiQuickInfo info : resultsView.getContents() )
			loadContentPreview(info);
	}

	private void loadContentPreview(HiQuickInfo info) {
		GroupContentsCellRenderer renderer = (GroupContentsCellRenderer) resultsView.getContentsList().getCellRenderer();
		QuickInfoCell cell = renderer.getCellForContent(info);
		if ( cell != null ) {
			if ( cell.needsPreview() ) {
				WSImageLoaderThread thumbLoader = new WSImageLoaderThread();
				long viewID = info.getBaseID();
				boolean cacheImage = true;
				if ( info.getContentType() == HiBaseTypes.HI_OBJECT )
					viewID = info.getRelatedID();
				if ( info.getContentType() == HiBaseTypes.HI_LAYER )
					cacheImage = false;

				thumbLoader.loadImage(viewID, cacheImage, HiImageSizes.HI_THUMBNAIL, cell, resultsView.getContentsList());
			}
		}
	}


	// ------------------------------------------------------------------------------------------------------


	@Override
	public void actionPerformed(ActionEvent e) {
			if ( searchView.getSelectedLanguage().compareTo("all") == 0 ) { //$NON-NLS-1$
				HIRuntime.getGui().displayInfoDialog(Messages.getString("SearchModule.13"), Messages.getString("SearchModule.14")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}			
			if ( searchView.getSearchText().length() == 0  && searchView.getFieldSearchText().length() == 0 ) return;

			searchView.setSearchEnabled(false);
			// DEBUG do search
			resultsView.prepareElementLoading();
			if ( e.getActionCommand().equalsIgnoreCase("simpleSearch") || (e.getActionCommand().equalsIgnoreCase("search") && searchView.getFieldSearchText().length() == 0) ){ //$NON-NLS-1$ //$NON-NLS-2$
				updateTitle(true);
				resultsLoader.simpleSearch(searchView.getSearchText());
			} if ( e.getActionCommand().equalsIgnoreCase("fieldSearch") || (e.getActionCommand().equalsIgnoreCase("search") && searchView.getFieldSearchText().length() > 0) ) { //$NON-NLS-1$ //$NON-NLS-2$
				updateTitle(false);
				List<String> fields = new ArrayList<String>();
				fields.add(searchView.getSearchField());
				List<String> contents = new ArrayList<String>();
				contents.add(searchView.getFieldSearchText());				
				resultsLoader.fieldSearch(fields, contents);
			}
	}


	// ------------------------------------------------------------------------------------------------------


	@Override
	public void mouseClicked(MouseEvent e) {
		if ( e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && resultsView.getContentsList().getSelectedIndex() >= 0 ) {
			HiQuickInfo info = (HiQuickInfo) resultsView.getContentsList().getSelectedValue();
			HIRuntime.getGui().openContentEditor(info, this);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mousePressed(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}

}

