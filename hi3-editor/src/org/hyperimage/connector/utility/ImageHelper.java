/* $Id: ImageHelper.java 16 2009-01-16 16:36:16Z hgkuper $ */

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

/** 
 * This class implemented by Jens-Martin Loebel, originally to be found in package org.hyperimage.hieditor.util
 */
package org.hyperimage.connector.utility;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.SubsampleAverageDescriptor;

import com.sun.media.jai.codec.SeekableStream;

public class ImageHelper {

	public static PlanarImage convertByteArrayToPlanarImage(byte[] data) {
		if ( data == null )
			return null;
		return JAI.create("stream",SeekableStream.wrapInputStream(
				new ByteArrayInputStream(data), true));
	}

	/**
	 * Scales a given image to conform to given dimension
	 * @param originalImage
	 * @param scaleDimension
	 * @return
	 */
	public static PlanarImage scaleImageTo(PlanarImage originalImage, Dimension scaleDimension) 
	{
		PlanarImage thumbnailImage;
		Double scaleX, scaleY, scale; // scale factors

		// sanity check
		if ( originalImage == null ) return null;

		// determine scale factor
		scaleX = (originalImage.getWidth()/new Double(scaleDimension.width));
		scaleY = (originalImage.getHeight()/new Double(scaleDimension.height));
		scale = 1 / Math.max(scaleX, scaleY);

		// scale asset only if size > HI_DIM_THUMBSIZE
		if ( (scaleX > 1) || (scaleY > 1 ) )
		{
			// downsample image
			RenderedImage ren = (RenderedImage) originalImage;
			thumbnailImage = SubsampleAverageDescriptor.create(ren, scale, scale, null).getRendering();
		} else thumbnailImage = originalImage;

		return thumbnailImage;
	}

}
