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

package org.hyperimage.client.gui.lists;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.hyperimage.client.util.LoadableImage;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiQuickInfo;

import com.sun.media.jai.widget.DisplayJAI;
import org.hyperimage.client.Messages;

/**
 * @author Jens-Martin Loebel
 */
public class QuickInfoCell extends JPanel implements LoadableImage {
	
	private static final long serialVersionUID = 8068619766383530438L;

	
	private JPanel infoPanel;
    private JLabel idInfoLabel;
    private JPanel idInfoPanel;
    private DisplayJAI previewPanel;
    private JLabel titleLabel;
    private JPanel titlePanel;
    
    private HiQuickInfo info;
    private boolean isSelected = false;
    private boolean needsPreview = false;
    
    public QuickInfoCell(HiQuickInfo info) {
    	this.info = info;
    	
    	initComponents();
    	initInfoText();    	
    	attachPreview();
    	    	
    }
    
    void updateLanguage() {
        this.setInfoHeader();
    }
    
    public boolean needsPreview() {
    	return needsPreview;
    }
    
    public void setPreviewImage(PlanarImage image) {
    	if ( needsPreview ) 
    		if ( image != null ) {
    			if ( image.getWidth() <= 128 && image.getHeight() <= 128 ) {
    				int x = (128-image.getWidth()) / 2;
    				int y = (128-image.getHeight()) / 2;
    				previewPanel.removeAll();
    				previewPanel.set(image, x, y);
    				needsPreview = false;
    			}
    		} else {
    			// preview not found or load error
				PlanarImage errorImage = 
					JAI.create("url", getClass().getResource("/resources/icons/preview-loaderror.png")); 
				previewPanel.removeAll();
				previewPanel.set(errorImage);
				needsPreview = false;
    		}
    }

	public boolean isSelected() {
		return isSelected;
	}
	
	public void setSelected(boolean selected) {
		if ( selected != isSelected ) {
			isSelected = selected;

			if ( isSelected ) this.setBackground(Color.yellow);
			else this.setBackground(Color.gray);
		}
	}
	
	public boolean attachPreview() {
		boolean found = false;
		
		// TODO caching strategy and handling of views
		
		if ( info.getContentType() == HiBaseTypes.HI_GROUP ) {
	        PlanarImage image = 
	            JAI.create("url", getClass().getResource("/resources/icons/preview-group.png")); 
	        previewPanel.set(image);
	        found = true;
		} else if ( info.getContentType() == HiBaseTypes.HIURL ) {
	        PlanarImage image = 
	            JAI.create("url", getClass().getResource("/resources/icons/preview-url.png")); 
	        previewPanel.set(image);
	        found = true;
		} else if ( info.getContentType() == HiBaseTypes.HI_LIGHT_TABLE ) {
	        PlanarImage image = 
	            JAI.create("url", getClass().getResource("/resources/icons/preview-lighttable.png")); 
	        previewPanel.set(image);
	        found = true;
		} else if ( info.getContentType() == HiBaseTypes.HI_LAYER ) {
	        needsPreview = true;
		} else if ( info.getContentType() == HiBaseTypes.HI_VIEW ) {
	        needsPreview = true;
		} else if ( info.getContentType() == HiBaseTypes.HI_OBJECT  && info.getPreview() == null ) {
			if ( info.getCount() > 0  )				
				needsPreview = true;
			else {
				// object has no views attached
				PlanarImage image = 
					JAI.create("url", getClass().getResource("/resources/icons/preview-noview.png")); 
				previewPanel.set(image);
				found = true;
			}
		} else if ( info.getContentType() == HiBaseTypes.HI_REPOSITORY_ITEM ) {
			needsPreview = true;
		}
		// attach temp picture
		if ( needsPreview ) {
	        PlanarImage image = 
	        	JAI.create("url", getClass().getResource("/resources/icons/preview-loading.png")); 
            previewPanel.set(image);
		}
		
		return found;
	}
    
   private void setInfoHeader() {
        if ( info.getContentType() == HiBaseTypes.HI_GROUP ) {
    		if (info.getCount() != 1 ) 
    			idInfoLabel.setText(Messages.getString("QuickInfoCell.GROUP")+" ("+info.getCount()+" "+Messages.getString("QuickInfoCell.ELEMENTS")+")");
    		else idInfoLabel.setText(Messages.getString("QuickInfoCell.GROUP")+" ("+Messages.getString("QuickInfoCell.ONEELEMENT")+")");
    	} else if ( info.getContentType() == HiBaseTypes.HI_INSCRIPTION ) {
    		idInfoLabel.setText(Messages.getString("QuickInfoCell.INSCRIPTION"));
    	} else if ( info.getContentType() == HiBaseTypes.HI_LAYER ) {
    		idInfoLabel.setText(Messages.getString("QuickInfoCell.LAYER"));
    	} else if ( info.getContentType() == HiBaseTypes.HI_LIGHT_TABLE ) {
    		idInfoLabel.setText(Messages.getString("QuickInfoCell.LIGHTTABLE"));
    	} else if ( info.getContentType() == HiBaseTypes.HI_OBJECT ) {
    		if (info.getCount() != 1 ) 
    			idInfoLabel.setText(Messages.getString("QuickInfoCell.OBJECT")+" ("+info.getCount()+" "+Messages.getString("QuickInfoCell.VIEWS")+")");
    		else idInfoLabel.setText(Messages.getString("QuickInfoCell.OBJECT")+" ("+Messages.getString("QuickInfoCell.ONEVIEW")+")");
    	} else if ( info.getContentType() == HiBaseTypes.HI_TEXT ) {
    		idInfoLabel.setText(Messages.getString("QuickInfoCell.TEXT"));
    	} else if ( info.getContentType() == HiBaseTypes.HI_VIEW ) {
    		idInfoLabel.setText(Messages.getString("QuickInfoCell.VIEW"));
    	} else if ( info.getContentType() == HiBaseTypes.HIURL ) {
    		idInfoLabel.setText(Messages.getString("QuickInfoCell.EXTERNALURL"));
    	} else ;
   }
        
    private void initInfoText() {
        setInfoHeader();
    	// set icon
    	if ( info.getContentType() == HiBaseTypes.HI_GROUP ) {
    		idInfoLabel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-icon.png"))); // NOI18N
    	} else if ( info.getContentType() == HiBaseTypes.HI_INSCRIPTION ) {
    		idInfoLabel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/inscription-icon.png"))); // NOI18N
    	} else if ( info.getContentType() == HiBaseTypes.HI_LAYER ) {
    		idInfoLabel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/layer-icon.png"))); // NOI18N
    	} else if ( info.getContentType() == HiBaseTypes.HI_LIGHT_TABLE ) {
    		idInfoLabel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/lighttable-icon.png"))); // NOI18N
    	} else if ( info.getContentType() == HiBaseTypes.HI_OBJECT ) {
    		idInfoLabel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/object-icon.png"))); // NOI18N
    	} else if ( info.getContentType() == HiBaseTypes.HI_TEXT ) {
    		idInfoLabel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/text-icon.png"))); // NOI18N
    	} else if ( info.getContentType() == HiBaseTypes.HI_VIEW ) {
    		idInfoLabel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/view-icon.png"))); // NOI18N
    	} else if ( info.getContentType() == HiBaseTypes.HIURL ) {
    		idInfoLabel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/url-icon.png"))); // NOI18N
    	} else if ( info.getContentType() == HiBaseTypes.HI_REPOSITORY_ITEM ) {
    		idInfoLabel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/repository-icon.png"))); // NOI18N
    		idInfoLabel.setText(info.getPreview()); 		
    	} else ;
    	
    	// set title
    	String title = null;
    	if ( info.getTitle() != null )
    		if ( info.getTitle().length() > 0 )
    			title = info.getTitle();
    		else if ( info.getContentType() == HiBaseTypes.HI_OBJECT )
    			title= "-";
    	if ( title != null ) titleLabel.setText(title);
    	
    	// DEBUG - clean up
    	if ( info.getPreview() != null && info.getContentType() != HiBaseTypes.HI_REPOSITORY_ITEM ) {
    		previewPanel.setLayout(new BorderLayout());
    		JLabel area = new JLabel();
    		area.setFont(area.getFont().deriveFont((float)10));
    		area.setBorder(BorderFactory.createLineBorder(Color.black, 1));
    		previewPanel.add(area);
    		area.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    		area.setText(MetadataHelper.richTextToHTML(info.getPreview()));
    	}
    }
    
	private void initComponents() {
		infoPanel = new JPanel();
        idInfoPanel = new JPanel();
        idInfoLabel = new JLabel();
        
        // DEBUG
        previewPanel = new DisplayJAI();
        
        titlePanel = new JPanel();
        titleLabel = new JLabel();

        setBackground(Color.gray);
        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        infoPanel.setLayout(new BorderLayout());

        idInfoPanel.setBorder(BorderFactory.createEtchedBorder());
        idInfoPanel.setLayout(new BorderLayout());

        idInfoLabel.setFont(idInfoLabel.getFont().deriveFont((float)10));
        idInfoLabel.setText("ID: ");
        idInfoPanel.add(idInfoLabel, BorderLayout.CENTER);

        infoPanel.add(idInfoPanel, BorderLayout.NORTH);

        titlePanel.setBorder(BorderFactory.createEtchedBorder());
        titlePanel.setLayout(new BorderLayout());

        titleLabel.setFont(titleLabel.getFont().deriveFont((float)10));
        titleLabel.setText("-");
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        infoPanel.add(titlePanel, BorderLayout.SOUTH);

        previewPanel.setBackground(Color.white);
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        previewPanel.setPreferredSize(new Dimension(128, 128));

        org.jdesktop.layout.GroupLayout previewPanelLayout = new org.jdesktop.layout.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 126, Short.MAX_VALUE)
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 126, Short.MAX_VALUE)
        );

        infoPanel.add(previewPanel, BorderLayout.CENTER);
        this.setLayout(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(130,165));
        this.add(infoPanel);
    }


}
