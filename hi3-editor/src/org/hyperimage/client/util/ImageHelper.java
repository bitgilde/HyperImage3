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

import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.io.ByteArrayInputStream;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.ScaleDescriptor;
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

	public static RenderableImage convertByteArrayToRenderableImage(byte[] data) {
		if ( data == null )
			return null;
		PlanarImage originalImage = JAI.create("stream",SeekableStream.wrapInputStream(
				new ByteArrayInputStream(data), true));
		
		ParameterBlock pb = new ParameterBlock();
        pb.addSource(originalImage);
        pb.add(null).add(null).add(null).add(null).add(null);
        RenderableImage ren = JAI.createRenderable("renderable", pb);

        return ren;
	}
	
	/**
	 * Scales a given image to conform to given dimension
	 * @param originalImage
	 * @param scaleDimension
	 * @return
	 */
	public static PlanarImage scaleImageTo(PlanarImage originalImage, double relScale) 
	{
		PlanarImage thumbnailImage = null;

		// sanity check
		if ( originalImage == null ) return null;

		if ( relScale == 1d  || relScale == 0d )
			return originalImage;
		
		if ( relScale < 1 ) {
			// downsample image
			RenderedImage ren = (RenderedImage) originalImage;
			thumbnailImage = SubsampleAverageDescriptor.create(ren, relScale, relScale, null).getRendering();			
		} else {
			// upsample image
			thumbnailImage = ScaleDescriptor.create(originalImage, new Float(relScale), new Float(relScale),
	                    new Float(0.0f), new Float(0.0f), Interpolation.getInstance(Interpolation.INTERP_NEAREST), null);
		}
		
		return thumbnailImage;
	}

	public static PlanarImage scaleImageTo(RenderableImage originalImage, double relScale) 
	{
		PlanarImage thumbnailImage;

		// sanity check
		if ( originalImage == null ) return null;

		// downsample image
        thumbnailImage = (PlanarImage) originalImage.createScaledRendering((int)(originalImage.getWidth()*relScale), (int)(originalImage.getHeight()*relScale), null);

		return thumbnailImage;
	}
	


}
