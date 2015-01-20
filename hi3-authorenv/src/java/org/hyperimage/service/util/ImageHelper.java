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

package org.hyperimage.service.util;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.ByteArrayInputStream;

import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.SubsampleAverageDescriptor;

import com.sun.media.jai.codec.SeekableStream;

/**
 * @author Jens-Martin Loebel
 */
public class ImageHelper {

	public static PlanarImage convertByteArrayToPlanarImage(byte[] data) {
		if ( data == null )
			return null;
		return JAI.create("stream",SeekableStream.wrapInputStream(
				new ByteArrayInputStream(data), true));
	}
	
	public static BufferedImage convertByteArrayToBufferedImage(byte[] data) {
		if ( data == null )
			return null;
		return JAI.create("stream",SeekableStream.wrapInputStream(
				new ByteArrayInputStream(data), true)).getAsBufferedImage();
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
		Float scaleXf, scaleYf, scalef; // scale factors as float
		
		// sanity check
		if ( originalImage == null ) return null;

		// determine scale factor
		scaleX = (originalImage.getWidth()/new Double(scaleDimension.width));
		scaleY = (originalImage.getHeight()/new Double(scaleDimension.height));
		scaleXf = (originalImage.getWidth()/new Float(scaleDimension.width));
		scaleYf = (originalImage.getHeight()/new Float(scaleDimension.height));
		scale = 1 / Math.max(scaleX, scaleY);
		scalef = 1 / Math.max(scaleXf, scaleYf);


		
		if ( (scaleX > 1) || (scaleY > 1 ) )
		{
			// downsample image
			RenderedImage ren = (RenderedImage) originalImage;
			thumbnailImage = SubsampleAverageDescriptor.create(ren, scale, scale, null).getRendering();
		} else {
                    // upsample image
                    ParameterBlock pb = new ParameterBlock();
                    pb.addSource(originalImage);
                    pb.add(scalef);
                    pb.add(scalef);
                    pb.add(0.0F);
                    pb.add(0.0F);
                    pb.add(new InterpolationBicubic2(8));
                    thumbnailImage = JAI.create("scale", pb, null);
		}

		return thumbnailImage;
	}
	public static PlanarImage scaleImageTo(BufferedImage originalImage, Dimension scaleDimension) 
	{
		return scaleImageTo(PlanarImage.wrapRenderedImage(originalImage), scaleDimension);
	}
	

}
