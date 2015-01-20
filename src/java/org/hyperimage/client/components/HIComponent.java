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

import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.hyperimage.client.gui.views.GUIView;
import org.hyperimage.client.ws.HiBase;

/**
 * @author Jens-Martin Loebel
 */
public abstract class HIComponent {

	public static enum HIMessageTypes {
		ENTITY_CHANGED, 
		ENTITY_ADDED, 
		ENTITY_REMOVED, 
		GROUP_SORTORDER_CHANGED,
		GROUP_CONTENTS_CHANGED,
		GROUPCONTENTS_SORTORDER_CHANGED,
		CHILD_ADDED, 
		CHILD_REMOVED,
		CONTENT_MOVED_TO_TRASH,
		MOVED_TO_TRASH, 
		PREFERENCE_MODIFIED,
		LANGUAGE_ADDED,
		LANGUAGE_REMOVED,
		DEFAULT_LANGUAGE_CHANGED,
                TEMPLATE_CHANGED};
	
	protected String title;
	protected JMenu contextMenu;
	protected JMenuItem windowMenuItem;

	protected ArrayList<GUIView> views;
	
	public HIComponent(String title, String menuTitle) {
		views = new ArrayList<GUIView>();
		contextMenu = new JMenu(menuTitle);
		windowMenuItem = new JMenuItem(title);
		this.title = title;
	}
	
	public ArrayList<GUIView> getViews() {
		return views;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public JMenu getContextMenu() {
		return this.contextMenu;
	}
	
	public JMenuItem getWindowMenuItem() {
		return this.windowMenuItem;
	}
	
	public void updateLanguage() {
		for ( GUIView view : views )
			view.updateLanguage();
	}
	
	public HiBase getBaseElement() {
		// returns the base element this component is editing, if applicable
		return null; // null if this component does not deal with HiBase elements
	}
	
	public void receiveMessage(HIMessageTypes message, HiBase base ) {
		// do nothing
	}
	
	public int countViews() {
		return views.size();
	}
	
	public boolean requestClose() {
		return true;
	}
}
