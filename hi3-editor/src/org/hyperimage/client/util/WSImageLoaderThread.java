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

import javax.media.jai.PlanarImage;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.ws.HiImageSizes;

/**
 * Class: WSImageLoaderThread
 * Package: org.hyperimage.client.components
 * @author Jens-Martin Loebel
 *
 */
public class WSImageLoaderThread extends Thread {
	private Thread thread;
	private long viewID;
	private LoadableImage imageComponent;
	private JComponent container;
	private boolean cacheImage = true;
	private HiImageSizes imgSize;
	

	public WSImageLoaderThread()
	{
		this.thread = new Thread(this); 
	}

	public void loadImage(long viewID, boolean cacheImage, HiImageSizes imgSize, LoadableImage imageComponent, JComponent container) {
		this.viewID = viewID;
		this.cacheImage = cacheImage;
		this.imgSize = imgSize;
		this.imageComponent = imageComponent;
		this.container = container;
		thread.start();
	}

	public void loadImage(long viewID, HiImageSizes imgSize, LoadableImage imageComponent, JComponent container) {
		this.viewID = viewID;
		this.cacheImage = true;
		this.imgSize = imgSize;
		this.imageComponent = imageComponent;
		this.container = container;
		thread.start();
	}

	public void run() 
	{
		try {
			// caching is done by the web service manager
			final PlanarImage thumbnail = HIRuntime.getManager().getImage(viewID, imgSize, cacheImage);

			SwingUtilities.invokeLater(new Runnable()  {
				public void run() {
					// set content here
					imageComponent.setPreviewImage(thumbnail);
					container.repaint();
					container.doLayout();
				}
			});
		} catch (HIWebServiceException wse) {

		}
	}
}
