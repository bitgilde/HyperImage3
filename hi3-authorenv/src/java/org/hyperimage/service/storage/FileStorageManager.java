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

package org.hyperimage.service.storage;

import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.SeekableStream;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import org.hyperimage.service.model.HIProject;
import org.hyperimage.service.model.HIView;
import org.hyperimage.service.util.ImageHelper;

/**
 * 
 * Class: FileStorageManager
 * Package: org.hyperimage.service.storage
 * @author Jens-Martin Loebel
 *
 */
public class FileStorageManager {

	private String repositoryLocation;
	
	public FileStorageManager(String repositoryLocation) throws InstantiationException {
		this.repositoryLocation = repositoryLocation;
		
		if ( !this.repositoryLocation.endsWith(File.separator) )
			this.repositoryLocation = this.repositoryLocation+File.separator;
		
		File f = new File(repositoryLocation);
		if ( !f.isDirectory() )	
			throw new InstantiationException("Repository Location '"+repositoryLocation+"' is invalid!");
		if ( !f.canWrite() )
			throw new InstantiationException("Repository Location '"+repositoryLocation+"' is not writable!");
		
	}
	
	public boolean storeView(HIView view, byte[] data) {
		if ( !prepDir(view) )
			return false;
		
		// store original bitstream
		try {
			FileOutputStream fos = new FileOutputStream(getDir(view)+view.getHash()+".original");
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO handle file error
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO handle file error
			e.printStackTrace();
			return false;
		}
		
		// render previews if possible

		// only generate previews/thumbnails if file is an image
		byte[] hiresData = null;
		byte[] previewData = null;
		byte[] thumbnailData = null;
                if( view.getMimeType() == null ) view.setMimeType("application/octet-stream");
		if ( view.getMimeType().startsWith("image") )
		{
			// load byte array as image
                        try {
			PlanarImage viewImage = JAI.create("stream",SeekableStream.wrapInputStream(
						new ByteArrayInputStream(data), true));
			
			// set bitstream info
			view.setWidth(viewImage.getWidth());
			view.setHeight(viewImage.getHeight());
			
			// render preview and thumbnail image
			PlanarImage previewImage = ImageHelper.scaleImageTo(viewImage, new Dimension(400,400));
			PlanarImage thumbImage = ImageHelper.scaleImageTo(viewImage, new Dimension(128,128));
			
			// create jpeg files from PlanarImages		    
			ByteArrayOutputStream outHiRes = new ByteArrayOutputStream();
			ByteArrayOutputStream outPreview = new ByteArrayOutputStream();
			ByteArrayOutputStream outThumbnail = new ByteArrayOutputStream();

			JPEGEncodeParam jpegParam = new JPEGEncodeParam();
			// set encoding quality
			jpegParam.setQuality(0.8f);
			JAI.create("encode",viewImage, outHiRes, "JPEG", jpegParam);
			JAI.create("encode",previewImage, outPreview, "JPEG", jpegParam);
			JAI.create("encode", thumbImage, outThumbnail, "JPEG", jpegParam);
			hiresData = outHiRes.toByteArray();
			previewData = outPreview.toByteArray();
			thumbnailData = outThumbnail.toByteArray();
			
			FileOutputStream fos;
			try {
				// store working copy of image as jpeg
				fos = new FileOutputStream(getDir(view)+view.getHash()+"_full.jpg");
				fos.write(hiresData);
				fos.close();

				// store rendered preview image as jpeg
				fos = new FileOutputStream(getDir(view)+view.getHash()+"_preview.jpg");
				fos.write(previewData);
				fos.close();

				// store rendered thumbnail image as jpeg
				fos = new FileOutputStream(getDir(view)+view.getHash()+"_thumb.jpg");
				fos.write(thumbnailData);
				fos.close();
			} catch (FileNotFoundException e) {
				// TODO handle file error
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				// TODO handle file error
				e.printStackTrace();
				return false;
			}
			
			viewImage = null;
			previewImage = null;
			thumbImage = null;
			outHiRes = null;
			outPreview = null;
			outThumbnail = null;
			hiresData = null;
			previewData = null;
			thumbnailData = null;
                        } catch (Exception e) {
                            System.out.println("Image data creation failed: "+e.getMessage());
                            e.printStackTrace();
                        }
		} else
		{
			System.out.println("--> "+view.getMimeType());
			// TODO file is not an image -- use standard icon 
		
		}		
		
		
		
		return true;
	}
	
	
	public boolean removeView(HIView view) {
		if ( !prepDir(view) )
			return false;
		
		// remove original bitstream
		File original = new File(getDir(view)+view.getHash()+".original");
		if ( original.exists() ) original.delete();
		// remove rendered bitstreams
		File preview = new File(getDir(view)+view.getHash()+"_thumb.jpg");
		if ( preview.exists() ) preview.delete();
		preview = new File(getDir(view)+view.getHash()+"_preview.jpg");
		if ( preview.exists() ) preview.delete();
		preview = new File(getDir(view)+view.getHash()+"_full.jpg");
		if ( preview.exists() ) preview.delete();		
		// remove directory
		File dir = new File(getDir(view));
		if ( dir.isDirectory() && dir.exists() )
			dir.delete();
		
		return true;
	}

	public byte[] getThumbnail(HIView view) {
		byte[] bitstream = null;
		
		if ( !prepDir(view.getProject()) )
			return null;
		
		try {
			bitstream = getBytesFromFile(getDir(view)+view.getHash()+"_thumb.jpg");
		} catch (IOException e) {
			// TODO handle file error			
                        System.out.println("File Error: "+e.getMessage());
//			e.printStackTrace();
		}
		
		return bitstream;
		
	}

	public byte[] getPreview(HIView view) {
		byte[] bitstream = null;
		
		if ( !prepDir(view.getProject()) )
			return null;
		
		try {
			bitstream = getBytesFromFile(getDir(view)+view.getHash()+"_preview.jpg");
		} catch (IOException e) {
			// TODO handle file error
                        System.out.println("File Error: "+e.getMessage());
//			e.printStackTrace();
		}
		
		return bitstream;
		
	}

	public byte[] getHiRes(HIView view) {
		byte[] bitstream = null;
		
		if ( !prepDir(view.getProject()) )
			return null;
		
		try {
			bitstream = getBytesFromFile(getDir(view)+view.getHash()+"_full.jpg");
		} catch (IOException e) {
			// TODO handle file error
                        System.out.println("File Error: "+e.getMessage());
//			e.printStackTrace();
		}
		
		return bitstream;
		
	}


    	public byte[] getOriginal(HIView view) {
		byte[] bitstream = null;

		if ( !prepDir(view.getProject()) )
			return null;

		try {
			bitstream = getBytesFromFile(getDir(view)+view.getHash()+".original");
		} catch (IOException e) {
			// TODO handle file error
                        System.out.println("File Error: "+e.getMessage());
//			e.printStackTrace();
		}

		return bitstream;

	}
        
        private long getDirSize(File dir) {
            long size = 0;
          
            for (File file : dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) return true;
                    else {
                      return file.getAbsolutePath().toLowerCase().endsWith(".original");
                    }
                }
            })) {
                if (file.isFile()) size += file.length(); else size += getDirSize(file);
            }
            return size;
        }
                
        public long getUsedSpaceProject(HIProject project) {
            try {
                return getDirSize(new File(getDir(project)));
            } catch (Exception e) {
                System.out.println("QUOTA CALC ERROR!");
                e.printStackTrace();
                return 0;
            }
        }


	
	// http://www.java-tips.org/java-se-tips/java.io/reading-a-file-into-a-byte-array.html
	// TODO replace with own code
	public static byte[] getBytesFromFile(String filename) throws IOException {
		
		File file = new File(filename);
		InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
        
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file);
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
	
	private String getDir(HIProject project) {
		return repositoryLocation+"P"+project.getId()+File.separator;
	}
	private String getDir(HIView view) {
		return repositoryLocation+"P"+view.getProject().getId()+File.separator+"V"+view.getId()+File.separator;
	}
	
	private boolean prepDir(HIProject project) {
		File dir = new File(getDir(project));
		if ( !dir.exists() )
			dir.mkdir();
		
		if ( !dir.exists() )
			return false;
		
		return true;
	}
	
	private boolean prepDir(HIView view) {
		File dir = new File(getDir(view));
		if ( !dir.exists() )
			dir.mkdirs();
		
		if ( !dir.exists() )
			return false;
		
		return true;
	}
	
	
}
