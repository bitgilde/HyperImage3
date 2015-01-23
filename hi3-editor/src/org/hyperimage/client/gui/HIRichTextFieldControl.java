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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.components.HIComponentFrame;
import org.hyperimage.client.gui.dnd.ContentTransfer;
import org.hyperimage.client.gui.dnd.GroupTransferable;
import org.hyperimage.client.gui.dnd.LayerTransferable;
import org.hyperimage.client.gui.dnd.ObjectContentTransferable;
import org.hyperimage.client.gui.dnd.QuickInfoTransferable;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.model.HIRichText;
import org.hyperimage.client.model.HIRichTextChunk;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.GroupTypes;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiObjectContent;
import org.hyperimage.client.ws.HiView;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class HIRichTextFieldControl extends HITextFieldControl
implements ActionListener, CaretListener, MouseListener, MouseMotionListener {

	private static final long serialVersionUID = -2129200508769763069L;

	// undo/redo manager
	final UndoManager undo = new UndoManager();

	private JToggleButton boldButton, italicButton, underlineButton, regularButton, subscriptButton, superscriptButton, literalButton;
	private ButtonGroup styleButtonGroup;
	private JButton linkButton;
	private JTextField linkField;
	private JPanel linkPanel;
	private JTextPane textPane;
	private JScrollPane textPaneScroll;	
	private ImageIcon linkIcon;
	private String fieldTitle;
	private TitledBorder fieldBorder;

	private StyledDocument document;
	private Style regularStyle, boldStyle, italicStyle, underlineStyle, linkStyle, subscriptStyle, superscriptStyle, literalStyle;
	private static final String LINK_TARGET_ATTR = "target";

	private int stylePos = 0;
	private JToggleButton buttonState;
	private Style styleState;
	private boolean isOnLink = false;

	

	/**
	 * This class implements the Drag and Drop handler for rich text field links.
	 * Only droping a link is supported.
 	 * 
	 * Class: LinkTransferHandler
	 * @author Jens-Martin Loebel
	 *
	 */
	public class LinkTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 1392755235239308382L;


//		-------- export methods ---------
		

		public int getSourceActions(JComponent c) {
			// dragging is supported as text
		    return LINK+COPY_OR_MOVE;
		}

		protected Transferable createTransferable(JComponent c) {
			// dragging is supported as text only
			
			return new StringSelection(textPane.getSelectedText());
		}

		protected void exportDone(JComponent c, Transferable t, int action) {
			// focus source component if drop failed
			if ( action == NONE )
				if ( HIRichTextFieldControl.this.getRootPane().getParent() instanceof HIComponentFrame )
					HIRuntime.getGui().focusComponent((HIComponentFrame) HIRichTextFieldControl.this.getRootPane().getParent());

			if ( action == MOVE )
				textPane.replaceSelection("");
		}
		
		
		// -------- import methods ---------

		
		public boolean canImport(TransferSupport supp) {
			// focus target component if necessary
			if ( HIRichTextFieldControl.this.getRootPane().getParent() instanceof HIComponentFrame )
				HIRuntime.getGui().focusComponent((HIComponentFrame) HIRichTextFieldControl.this.getRootPane().getParent());

			boolean isLink = false;
			
			if ( supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor)
					|| supp.isDataFlavorSupported(LayerTransferable.layerFlavor)
					|| supp.isDataFlavorSupported(GroupTransferable.groupFlavor)
					|| supp.isDataFlavorSupported(ObjectContentTransferable.objecContentFlavor) )
				isLink = true;

			if ( ! isLink && !supp.isDataFlavorSupported(DataFlavor.stringFlavor) )
		        return false;

		    if ( !textPane.isEditable() || !textPane.isEnabled() ) // check if editing is enabled
		    	return false;
	
		    // check transfer data
		    if ( supp.isDataFlavorSupported(GroupTransferable.groupFlavor) ) {
		    	try {
					HiGroup group = (HiGroup) supp.getTransferable().getTransferData(GroupTransferable.groupFlavor);
				    // cannot create a link to import or trash
					if ( group.getType() != GroupTypes.HIGROUP_REGULAR )
						return false;
				} catch (UnsupportedFlavorException e) {
					return false; // group empty or error occurred
				} catch (IOException e) {
					return false; // group empty or error occurred
				}
		    }

		    if ( isLink ) supp.setDropAction(LINK); // can only link elements to a metadata field

		    return true;
		}

		public boolean importData(TransferSupport supp) {
		    if (!canImport(supp)) // check if we support transfer elements
		        return false;
		    if ( !textPane.isEditable() || !textPane.isEnabled() ) // check if editing is enabled
		    	return false;
		    
		    String targetID = null;
		    String altTitle = null;
		    
		    // try to extract link target ID
		    if ( supp.isDataFlavorSupported(GroupTransferable.groupFlavor) ) {
		    	// link object was a group
		    	try {
					HiGroup group = (HiGroup) supp.getTransferable().getTransferData(GroupTransferable.groupFlavor);
					if ( group == null ) return false;
					targetID = group.getUUID(); // support UUIDs - OLD: "G"+group.getId();
					altTitle = Messages.getString("HIRichTextFieldControl.LINKTO")+(String) supp.getTransferable().getTransferData(DataFlavor.stringFlavor);
					supp.setDropAction(LINK);
		    	} catch (UnsupportedFlavorException e) {
					return false; // group empty or error occurred
				} catch (IOException e) {
					return false; // group empty or error occurred
				}
		    }

		    if ( supp.isDataFlavorSupported(LayerTransferable.layerFlavor) ) {
		    	// link object was a layer
		    	try {
					HILayer layer = (HILayer) supp.getTransferable().getTransferData(LayerTransferable.layerFlavor);
					if ( layer == null ) return false;
					targetID = layer.getModel().getUUID(); // support UUIDs - OLD: "L"+layer.getModel().getId();
					altTitle = Messages.getString("HIRichTextFieldControl.LINKTO")+(String) supp.getTransferable().getTransferData(DataFlavor.stringFlavor);
					supp.setDropAction(LINK);
				} catch (UnsupportedFlavorException e) {
					return false; // layer empty or error occurred
				} catch (IOException e) {
					return false; // layer empty or error occurred
				}
		    }
		    
		    if ( supp.isDataFlavorSupported(ObjectContentTransferable.objecContentFlavor) ) {
		    	// link object was object content
		    	try {
					HiObjectContent content  = (HiObjectContent) supp.getTransferable().getTransferData(ObjectContentTransferable.objecContentFlavor);
					if ( content == null ) return false;
					if ( content instanceof HiView ) targetID = content.getUUID(); // support UUIDs - OLD: "V"+content.getId();
					else targetID = content.getUUID(); // support UUIDs - OLD: "I"+content.getId();
					altTitle = Messages.getString("HIRichTextFieldControl.LINKTO")+(String) supp.getTransferable().getTransferData(DataFlavor.stringFlavor);
					supp.setDropAction(LINK);
				} catch (UnsupportedFlavorException e) {
					return false; // object content empty or error occurred
				} catch (IOException e) {
					return false; // object content empty or error occurred
				}
		    }

		    if ( supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor) ) {
		    	// link object was group content or search result
		    	try {
					ContentTransfer transfer = (ContentTransfer) supp.getTransferable().getTransferData(QuickInfoTransferable.quickInfoFlavor);
					if ( transfer == null ) return false;					
					if ( transfer.getContents().size() != 1 ) return false; // can´t create a link to multiple objects
					
					// create link id
					targetID = transfer.getContents().get(0).getUUID(); // support UUIDs - OLD: MetadataHelper.getDisplayableID(transfer.getContents().get(0));
					
					altTitle = Messages.getString("HIRichTextFieldControl.LINKTO")+(String) supp.getTransferable().getTransferData(DataFlavor.stringFlavor);
					supp.setDropAction(LINK);
				} catch (UnsupportedFlavorException e) {
					return false; // object content empty or error occurred
				} catch (IOException e) {
					return false; // object content empty or error occurred
				}
		    }

		    
		    if ( targetID != null )
			    supp.setDropAction(LINK); // can only link elements to a metadata field

		    
		    // if data was not something we can turn into a link, import a plain text
		    if ( targetID == null  && supp.isDataFlavorSupported(DataFlavor.stringFlavor) ) {
		    	try {
					altTitle = (String)supp.getTransferable().getTransferData(DataFlavor.stringFlavor);
				} catch (UnsupportedFlavorException e) {
					return false; // group empty or error occurred
				} catch (IOException e) {
					return false; // group empty or error occurred
				}
		    }

		    
		    /* ***************************
		     * handle links and plain text
		     * ***************************
		     */
		    if ( targetID == null && altTitle == null ) return false; // could not find any useable link target or text to import

		    if ( targetID != null ) {
		    	if ( altTitle == null ) altTitle = Messages.getString("HIRichTextFieldControl.LINK");		    	
		    	// create link in text field
		    	createLink(altTitle, targetID);
		    	// update GUI selection
		    	int selection = Math.abs(textPane.getSelectionEnd()-textPane.getSelectionStart());
		    	int startPos = Math.min(textPane.getSelectionStart(), textPane.getSelectionEnd());
		    	if ( selection == 0 ) {
		    		selection = altTitle.length();
		    		startPos = startPos - selection;
		    	}
		    	textPane.setSelectionStart(startPos);
		    	textPane.setSelectionEnd(startPos+selection);
		    	// update GUI
		    	updateCaretGUI(startPos, startPos+selection);

		    } else {
		    	// just import text
		    	int startPos = Math.min(textPane.getSelectionStart(), textPane.getSelectionEnd());
				try {
					document.insertString(startPos, altTitle, regularStyle);
				} catch (BadLocationException e) {
					// ignore
				}
		    }
		    
		    return true;
		}
	}
	
	
	
	// ------------------------------------------------------------------------------------------------

	

	/**
	 * Custom Document filter for HyperImage rich text field style attributes and constraints
	 * Class: HIRichDocumentFilter
	 * Package: org.hyperimage.client.gui
	 * @author Jens-Martin Loebel
	 */
	class HIRichDocumentFilter extends DocumentFilter {
		public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
			super.insertString(fb, offs, str, a);
		}

		public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
			super.remove(fb, offset, length);
		}

		public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			AttributeSet as = document.getCharacterElement(offset-1).getAttributes();

			if ( ( getCharacterStyleName(offset-1).compareTo("linkEmpty") == 0 ) && 
					( as.containsAttribute(StyleConstants.IconAttribute, linkIcon) ) && 
					( offset > 0 ) ) {
				linkStyle.addAttribute(LINK_TARGET_ATTR, getCharacterLinkTarget(offset-1));
				document.remove(offset-1, 1);
				super.replace(fb, offset-1, length, text, linkStyle);
				return;
			} else {
				if ( ((textPane.getSelectionEnd()-textPane.getSelectionStart()) == 0) && 
						!getCharacterStyleName(offset-1).startsWith("link") ) 
					super.replace(fb, offset, length, text, styleState);
				else 
					super.replace(fb, offset, length, text, attrs);
			}
		}
	}


	public HIRichTextFieldControl(String fieldTitle) {
		super();
		this.fieldTitle = fieldTitle;
		
		initComponents();
                updateLanguage();
		
		// init Drag and Drop
		LinkTransferHandler handler = new LinkTransferHandler();
		this.linkField.setTransferHandler(handler);
		this.textPane.setTransferHandler(handler);
		
		// attach listeners
		this.textPane.addMouseListener(this);
		this.textPane.addMouseMotionListener(this);
	}

	public String getText() {
		HIRichText richText = new HIRichText();

		String text;
		String linkTarget = null;
		String styleName = getCharacterStyleName(0);
		int startPos = 0;
		int endPos = 1;

		while (startPos < document.getLength()) {
			styleName = getCharacterStyleName(startPos);
			linkTarget = getCharacterLinkTarget(startPos);

			while ( (styleName.compareTo(getCharacterStyleName(endPos)) == 0) && (linkTarget.compareTo(getCharacterLinkTarget(endPos)) == 0) ) {
				endPos = endPos + 1;
				if ( endPos >= document.getLength() ) { 
					endPos = document.getLength();
					break;
				}
			}

			try {
				styleName = getCharacterStyleName(startPos);
				linkTarget = getCharacterLinkTarget(startPos);
				text = document.getText(startPos, endPos-startPos);
				if ( styleName.compareTo("bold") == 0 ) 
					richText.addChunk(HIRichTextChunk.chunkTypes.BOLD, text);
				else if ( styleName.compareTo("italic") == 0 )
					richText.addChunk(HIRichTextChunk.chunkTypes.ITALIC, text);
				else if ( styleName.compareTo("underline") == 0 )
					richText.addChunk(HIRichTextChunk.chunkTypes.UNDERLINE, text);
				else if ( styleName.compareTo("subscript") == 0 )
					richText.addChunk(HIRichTextChunk.chunkTypes.SUBSCRIPT, text);
				else if ( styleName.compareTo("superscript") == 0 )
					richText.addChunk(HIRichTextChunk.chunkTypes.SUPERSCRIPT, text);
				else if ( styleName.compareTo("literal") == 0 )
					richText.addChunk(HIRichTextChunk.chunkTypes.LITERAL, text);
				else if ( styleName.compareTo("link") == 0 )
					richText.addChunk(HIRichTextChunk.chunkTypes.LINK, text, linkTarget);
				else richText.addChunk(HIRichTextChunk.chunkTypes.REGULAR, text);


			} catch (BadLocationException e) {
				// ignore
			}
			startPos = endPos; 

		}
		return richText.getModel();	
	}

	public void setText(String modelText) {
		HIRichText richText = new HIRichText(modelText);
		try {
			document.remove(0, document.getLength());

			for ( HIRichTextChunk chunk : richText.getChunks() ) {
				if ( chunk.getChunkType() == HIRichTextChunk.chunkTypes.BOLD )
					document.insertString(document.getLength(), chunk.getValue(), boldStyle);
				else if ( chunk.getChunkType() == HIRichTextChunk.chunkTypes.ITALIC )
					document.insertString(document.getLength(), chunk.getValue(), italicStyle);
				else if ( chunk.getChunkType() == HIRichTextChunk.chunkTypes.UNDERLINE )
					document.insertString(document.getLength(), chunk.getValue(), underlineStyle);
				else if ( chunk.getChunkType() == HIRichTextChunk.chunkTypes.SUBSCRIPT )
					document.insertString(document.getLength(), chunk.getValue(), subscriptStyle);
				else if ( chunk.getChunkType() == HIRichTextChunk.chunkTypes.SUPERSCRIPT )
					document.insertString(document.getLength(), chunk.getValue(), superscriptStyle);
				else if ( chunk.getChunkType() == HIRichTextChunk.chunkTypes.LITERAL )
					document.insertString(document.getLength(), chunk.getValue(), literalStyle);
				else if ( chunk.getChunkType() == HIRichTextChunk.chunkTypes.LINK ) {
					int startPos = document.getLength();
					document.insertString(document.getLength(), chunk.getValue(), regularStyle);
					markAsLink(startPos, chunk.getValue().length(), chunk.getRef());
				} else document.insertString(document.getLength(), chunk.getValue(), regularStyle);
			}
		} catch (BadLocationException e) {
			// ignore
		}
		textPane.setCaretPosition(document.getLength());
	}

	public void setEditable(boolean editable) {
		textPane.setEditable(editable);
		textPane.setEnabled(editable);
		linkField.setEnabled(editable);
		
		boldButton.setEnabled(editable);
		italicButton.setEnabled(editable);
		underlineButton.setEnabled(editable);
                subscriptButton.setEnabled(editable);
                superscriptButton.setEnabled(editable);
                literalButton.setEnabled(editable);
		
		if ( editable ) linkField.setBorder(BorderFactory.createLineBorder(Color.black));
		else linkField.setBorder(BorderFactory.createLineBorder(Color.gray));

		if ( !editable ) {
			textPane.setText("");
			linkField.setText("");
			linkButton.setEnabled(false);
		}
	}

	
	public void updateTitle(String title) {
		fieldBorder.setTitle(title);
		repaint();
		doLayout();
	}



	private void addUndoRedoAbility(JTextPane textPane, int modifierMask) 
	{
		textPane.getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent evt) {
				undo.addEdit(evt.getEdit());
			}
		});

		// Create an undo action and add it to the text component
		textPane.getActionMap().put("Undo",
				new AbstractAction("Undo") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {
				try {
					if (undo.canUndo()) {
						undo.undo();
					} else Toolkit.getDefaultToolkit().beep();
				} catch (CannotUndoException e) {;}
			}
		});
		// TODO Bind the undo action to key specified in HIConfig
		textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifierMask), "Undo");

		// Create a redo action and add it to the text component
		textPane.getActionMap().put("Redo",
				new AbstractAction("Redo") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {
				try {
					if (undo.canRedo()) {
						undo.redo();
					} else Toolkit.getDefaultToolkit().beep();
				} catch (CannotRedoException e) {;}
			}
		});

		// TODO Bind the redo action to key specified in HIConfig
		textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifierMask+ActionEvent.SHIFT_MASK), "Redo");
	}



	private void updateStyleStateByName(String styleName) {
		buttonState = regularButton;
		styleState = regularStyle;

		if ( styleName.compareTo("bold") == 0 ) {
			styleState = boldStyle;
			buttonState = boldButton;
		} else if ( styleName.compareTo("italic") == 0 ) {
			styleState = italicStyle;
			buttonState = italicButton;
		} else if ( styleName.compareTo("underline") == 0 ) {
			styleState = underlineStyle;
			buttonState = underlineButton;
		} else if ( styleName.compareTo("subscript") == 0 ) {
			styleState = subscriptStyle;
			buttonState = subscriptButton;
		} else if ( styleName.compareTo("superscript") == 0 ) {
			styleState = superscriptStyle;
			buttonState = superscriptButton;
		} else if ( styleName.compareTo("literal") == 0 ) {
			styleState = literalStyle;
			buttonState = literalButton;
		} else {
			styleState = regularStyle;
			buttonState = regularButton;
		}

		buttonState.setSelected(true);
	}

	private void markAsLink(int startPos, int length, String linkTarget) {
		// remove empty links in between
		for ( int i=startPos; i<=startPos+length; i++ ) {
			if ( getCharacterStyleName(i).compareTo("linkEmpty") == 0 ) {
				try {
					document.remove(i, 1);
					length = length - 1;
				} catch (BadLocationException e) {
					// ignore
				}

			}
		}
		// mark region
		linkStyle.addAttribute(LINK_TARGET_ATTR, linkTarget);
		document.setCharacterAttributes(startPos, length, linkStyle, true);
	}


	private void createLink(String altTitle, String targetID) {
		int selection = Math.abs(textPane.getSelectionEnd()-textPane.getSelectionStart());
		int startPos = Math.min(textPane.getSelectionStart(), textPane.getSelectionEnd());
	
		if ( selection != 0 ) markAsLink(startPos, selection, targetID);
		else
			try {
				document.insertString(startPos, altTitle, regularStyle);
				markAsLink(startPos, altTitle.length(), targetID);
			} catch (BadLocationException e) {
				// ignore
			}
			textPane.requestFocus();
		
	}

	private String getCharacterStyleName(int pos) {
		return (String) document.getCharacterElement(pos).getAttributes().getAttribute(StyleConstants.NameAttribute);
	}

	private String getCharacterLinkTarget(int pos) {
		String linkTarget = (String) document.getCharacterElement(pos).getAttributes().getAttribute(LINK_TARGET_ATTR);
		if ( linkTarget == null ) return "";
		return linkTarget;
	}

	private int getLinkStartPos(int pos) {
		return getLinkStartPos(pos, getCharacterLinkTarget(pos));
	}
	private int getLinkStartPos(int pos, String linkTarget) {
		int startPos = pos;
		Element element = document.getCharacterElement(pos);

		if ( ( getCharacterStyleName(pos).compareTo("link") == 0 ) && ( linkTarget.compareTo(getCharacterLinkTarget(pos)) == 0 ) )
			if (startPos > 0 ) startPos = getLinkStartPos(element.getStartOffset()-1);
			else startPos = element.getStartOffset();
		else startPos = startPos + 1;

		return startPos;
	}

	private int getLinkEndPos(int pos) {
		return getLinkEndPos(pos, getCharacterLinkTarget(pos));
	}
	private int getLinkEndPos(int pos, String linkTarget) {
		int endPos = pos;		
		Element element = document.getCharacterElement(pos);

		if ( ( getCharacterStyleName(pos).compareTo("link") == 0 ) && ( linkTarget.compareTo(getCharacterLinkTarget(pos)) == 0 ) )
			if (endPos < document.getLength() ) endPos = getLinkEndPos(element.getEndOffset()+1);
			else endPos = element.getEndOffset();
		else endPos = endPos - 1;

		return endPos;
	}

    public void updateLanguage() {
        boldButton.setToolTipText(Messages.getString("HIRichTextFieldControl.BOLDTEXT"));
        italicButton.setToolTipText(Messages.getString("HIRichTextFieldControl.ITALICSTEXT"));
        underlineButton.setToolTipText(Messages.getString("HIRichTextFieldControl.UNDERLINEDTEXT"));
        subscriptButton.setToolTipText(Messages.getString("HIRichTextFieldControl.SUBSCRIPTTEXT"));
        superscriptButton.setToolTipText(Messages.getString("HIRichTextFieldControl.SUPERSCRIPTTEXT"));
        literalButton.setToolTipText(Messages.getString("HIRichTextFieldControl.LITERALTEXT"));
        linkButton.setToolTipText(Messages.getString("HIRichTextFieldControl.REMOVELINK"));
    }

    @SuppressWarnings("serial")
    private void initComponents() {

        styleButtonGroup = new ButtonGroup();
        boldButton = new JToggleButton();
        italicButton = new JToggleButton();
        underlineButton = new JToggleButton();
        subscriptButton = new JToggleButton();
        superscriptButton = new JToggleButton();
        literalButton = new JToggleButton();
        regularButton = new JToggleButton();
        textPaneScroll = new JScrollPane();
        textPane = new JTextPane();
        linkPanel = new JPanel();
        linkField = new JTextField();
        linkButton = new JButton();

        // -----
        linkIcon = new ImageIcon(getClass().getResource("/resources/icons/link.png"));
        buttonState = regularButton;

        ((AbstractDocument) textPane.getDocument()).setDocumentFilter(new HIRichDocumentFilter());
        // add button actions to text pane
        InputMap inputMap = textPane.getInputMap();
        ActionMap actionMap = textPane.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, HIRuntime.getModifierKey()), "SET_BOLD");
        actionMap.put("SET_BOLD", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doStyleAction(boldButton);
            }
        });
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, HIRuntime.getModifierKey()), "SET_ITALIC");
        actionMap.put("SET_ITALIC", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doStyleAction(italicButton);
            }
        });
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, HIRuntime.getModifierKey()), "SET_UNDERLINE");
        actionMap.put("SET_UNDERLINE", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doStyleAction(underlineButton);
            }
        });
        // add undo/redo manager
        addUndoRedoAbility(textPane, HIRuntime.getModifierKey());

        boldButton.setActionCommand("group_boldButton");
        boldButton.addActionListener(this);
        italicButton.setActionCommand("group_italicButton");
        italicButton.addActionListener(this);
        underlineButton.setActionCommand("group_underlineButton");
        underlineButton.addActionListener(this);
        subscriptButton.setActionCommand("group_subscriptButton");
        subscriptButton.addActionListener(this);
        superscriptButton.setActionCommand("group_superscriptButton");
        superscriptButton.addActionListener(this);
        literalButton.setActionCommand("group_literalButton");
        literalButton.addActionListener(this);
        linkButton.addActionListener(this);
        linkButton.setActionCommand("removeLink");
        regularButton.setActionCommand("group_regularButton");

        textPane.addCaretListener(this);

		// -----
        fieldBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), fieldTitle);
        setBorder(fieldBorder);

        styleButtonGroup.add(boldButton);
        boldButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/bold.png"))); // NOI18N
        boldButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/bold-active.png"))); // NOI18N
        boldButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/bold-disabled.png"))); // NOI18N
        boldButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        boldButton.setPreferredSize(new Dimension(24, 24));

        styleButtonGroup.add(italicButton);
        italicButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/italic.png"))); // NOI18N
        italicButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/italic-active.png"))); // NOI18N
        italicButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/italic-disabled.png"))); // NOI18N
        italicButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        italicButton.setPreferredSize(new Dimension(24, 24));

        styleButtonGroup.add(underlineButton);
        underlineButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/underline.png"))); // NOI18N
        underlineButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/underline-active.png"))); // NOI18N
        underlineButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/underline-disabled.png"))); // NOI18N
        underlineButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        underlineButton.setPreferredSize(new Dimension(24, 24));

        styleButtonGroup.add(subscriptButton);
        subscriptButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/subscript.png"))); // NOI18N
        subscriptButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/subscript-active.png"))); // NOI18N
        subscriptButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/subscript-disabled.png"))); // NOI18N
        subscriptButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        subscriptButton.setPreferredSize(new Dimension(24, 24));

        styleButtonGroup.add(superscriptButton);
        superscriptButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/superscript.png"))); // NOI18N
        superscriptButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/superscript-active.png"))); // NOI18N
        superscriptButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/superscript-disabled.png"))); // NOI18N
        superscriptButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        superscriptButton.setPreferredSize(new Dimension(24, 24));

        styleButtonGroup.add(literalButton);
        literalButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/literal.png"))); // NOI18N
        literalButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/literal-active.png"))); // NOI18N
        literalButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/literal-disabled.png"))); // NOI18N
        literalButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        literalButton.setPreferredSize(new Dimension(24, 24));

        styleButtonGroup.add(regularButton);

        textPane.setBackground(Color.white);
        textPaneScroll.setViewportView(textPane);

        linkField.setEditable(false);
        linkField.setBorder(BorderFactory.createLineBorder(Color.black));
        linkField.setPreferredSize(new Dimension(200, 24));

        linkButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/removeLink.png"))); // NOI18N
        linkButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/removeLink-active.png"))); // NOI18N
        linkButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/removeLink-disabled.png"))); // NOI18N
        linkButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        linkButton.setPreferredSize(new Dimension(24, 24));
        linkButton.setEnabled(false);

        GroupLayout linkPanelLayout = new GroupLayout(linkPanel);
        linkPanel.setLayout(linkPanelLayout);
        linkPanelLayout.setHorizontalGroup(
            linkPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(linkPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(linkButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(linkField, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                .addContainerGap())
        );
        linkPanelLayout.setVerticalGroup(
            linkPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(linkPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                .add(linkButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(linkField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(6, 6, 6)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(boldButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(italicButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(subscriptButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(superscriptButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(underlineButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(literalButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(textPaneScroll)
                .addContainerGap())
            .add(GroupLayout.TRAILING, linkPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(boldButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(italicButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(underlineButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(4, 4, 4)
                        .add(superscriptButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(subscriptButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(4, 4, 4)
                        .add(literalButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(textPaneScroll))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(linkPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );


	// ------
        if ( System.getProperty("HI.feature.advancedEditorDisabled") != null ) {
            subscriptButton.setVisible(false);
            superscriptButton.setVisible(false);
            literalButton.setVisible(false);
        }
        
        initStyles();

    }

	private void initStyles() {
		document = textPane.getStyledDocument();
		StyleContext styleContext = new StyleContext();
		regularStyle = document.addStyle("regular", styleContext.getStyle(StyleContext.DEFAULT_STYLE));
		document.setLogicalStyle(0, regularStyle);
		StyleConstants.setFontFamily(regularStyle, "SansSerif");
		StyleConstants.setBold(regularStyle, false);
		StyleConstants.setItalic(regularStyle, false);
		StyleConstants.setUnderline(regularStyle, false);
		StyleConstants.setSubscript(regularStyle, false);
		StyleConstants.setSuperscript(regularStyle, false);

		boldStyle = document.addStyle("bold", null);
		StyleConstants.setBold(boldStyle, true);
		StyleConstants.setItalic(boldStyle, false);
		StyleConstants.setUnderline(boldStyle, false);
		StyleConstants.setSubscript(boldStyle, false);
		StyleConstants.setSuperscript(boldStyle, false);

		italicStyle = document.addStyle("italic", null);
		StyleConstants.setItalic(italicStyle, true);
		StyleConstants.setBold(italicStyle, false);
		StyleConstants.setUnderline(italicStyle, false);
		StyleConstants.setSubscript(italicStyle, false);
		StyleConstants.setSuperscript(italicStyle, false);

		underlineStyle = document.addStyle("underline", null);
		StyleConstants.setItalic(underlineStyle, false);
		StyleConstants.setBold(underlineStyle, false);
		StyleConstants.setUnderline(underlineStyle, true);
		StyleConstants.setSubscript(underlineStyle, false);
		StyleConstants.setSuperscript(underlineStyle, false);

		subscriptStyle = document.addStyle("subscript", null);
		StyleConstants.setItalic(subscriptStyle, false);
		StyleConstants.setBold(subscriptStyle, false);
		StyleConstants.setUnderline(subscriptStyle, false);
		StyleConstants.setSubscript(subscriptStyle, true);
		StyleConstants.setSuperscript(subscriptStyle, false);

		superscriptStyle = document.addStyle("superscript", null);
		StyleConstants.setItalic(superscriptStyle, false);
		StyleConstants.setBold(superscriptStyle, false);
		StyleConstants.setUnderline(superscriptStyle, false);
		StyleConstants.setSubscript(superscriptStyle, false);
		StyleConstants.setSuperscript(superscriptStyle, true);

		literalStyle = document.addStyle("literal", null);
		StyleConstants.setItalic(literalStyle, false);
		StyleConstants.setBold(literalStyle, true);
		StyleConstants.setUnderline(literalStyle, false);
		StyleConstants.setSubscript(literalStyle, false);
		StyleConstants.setSuperscript(literalStyle, false);
                StyleConstants.setBackground(literalStyle, new Color(240,240,240));
                StyleConstants.setFontFamily(literalStyle, "monospaced");
                StyleConstants.setForeground(literalStyle, new Color(38,115,4));
                
		linkStyle = document.addStyle("link", null);
		StyleConstants.setUnderline(linkStyle, true);
		StyleConstants.setForeground(linkStyle, Color.BLUE);
		StyleConstants.setSubscript(linkStyle, false);
		StyleConstants.setSuperscript(linkStyle, false);

		styleState = regularStyle;

	}




	// -------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e) {
		// TODO cleanup state logic
		int selection = Math.abs(textPane.getSelectionEnd()-textPane.getSelectionStart());
		int startPos = Math.min(textPane.getSelectionStart(), textPane.getSelectionEnd());

		if ( e.getActionCommand().startsWith("group_") ) 
			doStyleAction((JToggleButton) e.getSource());
		else if ( e.getActionCommand().equalsIgnoreCase("removeLink") && linkField.getText().length() > 0 ) {
			// remove link
			startPos = getLinkStartPos(stylePos);
			selection = getLinkEndPos(stylePos)-startPos;
			document.setCharacterAttributes(startPos, selection, regularStyle, true);
			textPane.setSelectionStart(startPos);
			textPane.setSelectionEnd(startPos+selection);
			textPane.requestFocus();
		}
	}

	private void doStyleAction(JToggleButton source) {
		int selection = Math.abs(textPane.getSelectionEnd()-textPane.getSelectionStart());
		int startPos = Math.min(textPane.getSelectionStart(), textPane.getSelectionEnd());

		if ( source == buttonState )
			updateStyleStateByName("regular");
		else {
			if ( source == boldButton ) updateStyleStateByName("bold");
			else if ( source == italicButton ) updateStyleStateByName("italic");
			else if ( source == underlineButton ) updateStyleStateByName("underline");
			else if ( source == subscriptButton ) updateStyleStateByName("subscript");
			else if ( source == superscriptButton ) updateStyleStateByName("superscript");
			else if ( source == literalButton ) updateStyleStateByName("literal");
			else updateStyleStateByName("regular");
		}
		if ( selection != 0 ) document.setCharacterAttributes(startPos, selection, styleState, true);

		// continue in regular style after a link, if user chooses to break link chain
		if ( textPane.getCaretPosition() == textPane.getText().length() 
			&& (textPane.getSelectionEnd()-textPane.getSelectionStart())==0 
			&& getCharacterStyleName(textPane.getCaretPosition()-1).compareTo("link") == 0
			) {
			updateStyleStateByName("regular");
			try {
				document.insertString(textPane.getCaretPosition(), " ", regularStyle);
			} catch (BadLocationException e) {
				// ignore, can´t happen
			}
		}

		textPane.requestFocus();
	}

	
	// -------------------------------------------------------------------------------


	private void updateCaretGUI(int dot, int mark) {
		int selection = Math.abs(mark-dot);
		int startPos = Math.min(dot, mark);
		int stopPos = startPos + selection;
		String styleName;

		if ( selection == 0 ) {
			styleName = getCharacterStyleName(startPos-1);
			stylePos = startPos-1;
			updateStyleStateByName(styleName);
		}
		else {
			styleName = getCharacterStyleName(startPos);
			stylePos = startPos;
			for ( int i=startPos; i < stopPos; i++ )
				if ( getCharacterStyleName(i) != styleName ) {
					updateStyleStateByName("regular");
					return;
				}
			updateStyleStateByName(styleName);
		}
		// update link field
		isOnLink = false;
		if ( styleName.startsWith("link") ) {
			isOnLink = true;
			if ( selection == 0 ) startPos = startPos - 1;
			linkField.setText(getCharacterLinkTarget(startPos));
		}
		else linkField.setText("");

		setLinkState();
	}
	
	public void caretUpdate(CaretEvent e) {
		updateCaretGUI(e.getDot(), e.getMark());
	}


	private void setLinkState() {
		boolean enabled = true;

		if ( linkField.getText().length() == 0 )
			if ( !isOnLink ) enabled = false;
		else if ( (!isOnLink) && ((textPane.getSelectionEnd()-textPane.getSelectionStart()) == 0) ) 
			enabled = false;

		linkButton.setEnabled(enabled);
	}

	// -------------------------------------------------------------------------------

	
	// Handle follow link
	@Override
	public void mouseClicked(MouseEvent e) {
		int pos = textPane.viewToModel(e.getPoint());
		if ( e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && linkButton.isEnabled() )
			if ( getCharacterStyleName(pos).startsWith("link") ) {
				try {
					long linkID = Long.parseLong(getCharacterLinkTarget(pos).substring(1));
					HIRuntime.getGui().openContentEditor(linkID, null);
				} catch (NumberFormatException nfe ) {}
			}
	}


	// -------------------------------------------------------------------------------


	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	// DEBUG refactor
	public void mouseMoved(MouseEvent e) {
		if ( getCharacterStyleName(textPane.viewToModel(e.getPoint())).startsWith("link") )
			textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		else
			textPane.setCursor(Cursor.getDefaultCursor());
	}
	
}
