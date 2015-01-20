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

package org.hyperimage.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.hyperimage.client.util.LoadableImage;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiObjectContent;
import org.hyperimage.client.ws.HiView;

import com.sun.media.jai.widget.DisplayJAI;

/**
 * @author Jens-Martin Loebel
 */
public class ObjectContentCell extends JPanel implements LoadableImage {

	private static final long serialVersionUID = -3487286832530501690L;

	
	private boolean isSelected = false;
	private boolean needsPreview = false;
	private DisplayJAI viewPanel;
	private static LineBorder defaultBorder = (LineBorder) BorderFactory.createLineBorder(Color.GRAY, 3);
	private static LineBorder selectedBorder = (LineBorder) BorderFactory.createLineBorder(Color.YELLOW, 3);

	private HiObjectContent content;
	private String defLang;
	
	public ObjectContentCell(HiObjectContent content, String defLang) {
		this.content = content;
		this.defLang = defLang;
		
		viewPanel = new DisplayJAI();
		viewPanel.setPreferredSize(new Dimension(128, 128));
		viewPanel.setBorder(null);
		viewPanel.setBackground(Color.WHITE);
		this.setLayout(new BorderLayout());
		this.setSize(128,128);
		this.setPreferredSize(new Dimension(128, 128));
		this.setBackground(Color.LIGHT_GRAY);
		this.setBorder(defaultBorder);
		this.add(viewPanel, BorderLayout.CENTER);
		
		attachPreview();
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
    				viewPanel.removeAll();
    				viewPanel.set(image, x, y);
    				needsPreview = false;
    			}
    		} else {
    			// preview not found or load error
				PlanarImage errorImage = 
					JAI.create("url", getClass().getResource("/resources/icons/preview-loaderror.png")); 
				viewPanel.removeAll();
				viewPanel.set(errorImage);
				needsPreview = false;
    		}
    }

	public boolean isSelected() {
		return isSelected;
	}
	
	public void setSelected(boolean selected) {
		if ( selected != isSelected ) {
			isSelected = selected;

			if ( isSelected ) this.setBorder(selectedBorder);
			else this.setBorder(defaultBorder);
		}
	}
	
	public boolean attachPreview() {
		boolean found = false;
		
		// TODO caching strategy and handling of views
		
		if ( content instanceof HiView ) {
	        needsPreview = true;
		} else {
    		JLabel area = new JLabel();
    		area.setPreferredSize(new Dimension(128, 128));
    		area.setFont(area.getFont().deriveFont((float)10));
    		area.setBorder(BorderFactory.createLineBorder(Color.black, 1));
    		area.setBackground(Color.WHITE);
    		area.setOpaque(true);
    		area.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    		
    		String inscription = MetadataHelper.findValue("HIBase", "content", 
    			MetadataHelper.getDefaultMetadataRecord(content, defLang)
    		);
    		inscription = MetadataHelper.richTextToHTML(inscription);
    		// limit amout of data displayed
    		if ( inscription.length() > 128 ) {
    			inscription = inscription.substring(0, 128);
    			if ( inscription.lastIndexOf("<") > inscription.lastIndexOf(">") )
    				inscription = inscription.substring(0, inscription.lastIndexOf("<")-1);
    			inscription = inscription+"...";
    		}
    		area.setText(inscription);

    		this.removeAll();
    		this.add(area, BorderLayout.CENTER);
			needsPreview = false;
		}

		// attach temp picture
		if ( needsPreview ) {
	        PlanarImage image = 
	        	JAI.create("url", getClass().getResource("/resources/icons/preview-loading.png")); 
            viewPanel.set(image);
		}
		
		return found;
	}
    
}
