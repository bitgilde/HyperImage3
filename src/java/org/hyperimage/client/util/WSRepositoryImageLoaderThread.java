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

package org.hyperimage.client.util;

import java.util.HashMap;

import javax.media.jai.PlanarImage;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.hyperimage.connector.fedora3.ws.HIFedora3Connector;

/**
 * DEBUG
 * Class: WSRepositoryImageLoaderthread
 * Package: org.hyperimage.client.components
 * @author Jens-Martin Loebel
 *
 */
public class WSRepositoryImageLoaderThread extends Thread {

		private Thread thread;
		private String urn;
		private HIFedora3Connector proxy;
		private LoadableImage imageComponent;
		private JComponent container;
		private HashMap<String, PlanarImage> previewCache;
		

		public WSRepositoryImageLoaderThread()
		{
			this.thread = new Thread(this); 
		}

		public void loadImage(HIFedora3Connector proxy, String urn, LoadableImage imageComponent, JComponent container, HashMap<String, PlanarImage> previewCache) {
			this.proxy = proxy;
			this.urn = urn;
			this.imageComponent = imageComponent;
			this.container = container;
			this.previewCache = previewCache;
			thread.start();
		}

		public void run() 
		{
			try {
				final PlanarImage thumbnail;
				// DEBUG caching strategy for repositories
				PlanarImage cachedImage = previewCache.get(urn);
				if ( cachedImage == null ) {
					thumbnail = ImageHelper.convertByteArrayToPlanarImage(proxy.getAssetPreviewData(null, urn));
					previewCache.put(urn, thumbnail);
				} else
					thumbnail = cachedImage;
				
				SwingUtilities.invokeLater(new Runnable()  {
					public void run() {
						// set content here
						imageComponent.setPreviewImage(thumbnail);
						container.repaint();
						container.doLayout();
					}
				});
			} catch (Exception e) {
				imageComponent.setPreviewImage(null);
				container.repaint();
				container.doLayout();
			}
		}

}
