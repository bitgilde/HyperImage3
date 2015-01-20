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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.views.FeedbackView;
import org.hyperimage.client.util.MetadataHelper;

/**
 * @author Jens-Martin Loebel
 */
// DEBUG - whole class
public class FeedbackModule extends HIComponent implements ActionListener {
	
	private FeedbackView feedbackView;
	
	private String feedback;
	

	public FeedbackModule(boolean isBug) {
		super("Feedback / Bug Report", "Feedback");	
		
		// init views
		feedbackView = new FeedbackView();
		
		if ( isBug ) feedbackView.setFeedbackOption(1);
		
		
		// register views
		views.add(feedbackView);
		
		// attach listeners
		feedbackView.getFeedbackButton().addActionListener(this);
	}
	
	public FeedbackModule() {
		this(false);
	}

	
	// ---------------------------------------------------------------------------------------------


	private void addLine(String line) {
		feedback = feedback+line+"\n";
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if ( feedbackView.getText().length() <= 0 )
			return;
		
		try {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {  
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
	                return null;  
	            }  
	            public void checkClientTrusted(  
	                    java.security.cert.X509Certificate[] certs, String authType) {  
	            }  		  
	            public void checkServerTrusted(  
	                    java.security.cert.X509Certificate[] certs, String authType) {  
	            }  
	        } };  
	  
	        try {  
	            SSLContext sc = SSLContext.getInstance("SSL");  
	            sc.init(null, trustAllCerts, new java.security.SecureRandom());  
	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());  
	        } catch (Exception e3) {  
	            e3.printStackTrace(); 
	        }  
	        // construct feedback
	        feedback = "";
	        
	        addLine("Type            : "+feedbackView.getType());
	        addLine("User            : "+HIRuntime.getManager().getCurrentUser().getLastName()+", "+HIRuntime.getManager().getCurrentUser().getFirstName()+" ("+HIRuntime.getManager().getCurrentUser().getUserName()+") "+HIRuntime.getManager().getCurrentUser().getEmail());
	        addLine("Project         : P"+HIRuntime.getManager().getProject().getId()+" - "+MetadataHelper.findValue(HIRuntime.getManager().getProject(), HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()));
	        addLine("Client Version  : "+HIRuntime.getClientVersion());
			addLine("Service URL     : "+HIRuntime.getManager().getServerURL());
	        try {
				addLine("Service Version : "+HIRuntime.getManager().getVersionID());
			} catch (HIWebServiceException e1) {
			}
	        addLine("Client OS       : "+System.getProperty("os.name")+" "+System.getProperty("os.version")+" ("+System.getProperty("os.arch")+")");
	        addLine("Java Version    : "+System.getProperty("java.version")+" ("+System.getProperty("java.vendor")+")");
	        if ( HIRuntime.getGui().getLastWSError() == null )  addLine("Last WS Error   : -none-");
	        else {
	        	HIWebServiceException lastWSError = HIRuntime.getGui().getLastWSError();
	        	addLine("Last WS Error   : "+lastWSError.getCause().getMessage()+" ("+lastWSError.getErrorType()+")");
	        	addLine("");
	        	addLine("Stack Trace");
	        	addLine("-----------");
	        	ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	        	PrintWriter writer = new PrintWriter(byteStream);
	        	lastWSError.getCause().printStackTrace(writer);
	        	writer.close();
	        	addLine(new String(byteStream.toByteArray()));
	        }
	        
	        addLine("");
	        addLine("Message");
	        addLine("-------");
	        feedback=feedback+feedbackView.getText();
	        feedback=feedback+"\n\n";
	        
	        // convert to url format
	        feedback = URLEncoder.encode(feedback, "UTF-8");
			URL url = new URL("https://hyperimage.cms.hu-berlin.de/2.0/wstart/provideFeedback.php");
	        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			PrintWriter writer = new PrintWriter(connection.getOutputStream());
			writer.write("raw_fb="+feedback);
			writer.close();
			connection.connect();
			
			// send feedback
			String response = connection.getResponseMessage();
			if ( response.compareTo("OK") == 0 ) {
				HIRuntime.getGui().clearLastWSError();
				HIRuntime.getGui().displayInfoDialog("Feedback gesendet", "Vielen Dank für Ihr Feedback!\n\nIhre Nachricht wurde an die HyperImage Entwickler gesendet.");
			} else
				HIRuntime.getGui().displayInfoDialog("Feedback Fehlgeschlagen", "Leider konnte Ihr Feedback aufgrund eines Systemfehlers nicht gesendet werden.\n"
						+"Bitte versuchen Sie es später erneut.\n\n"
						+"Sollte das Problem bestehen bleiben wenden Sie sich bitte per mail an die Entwickler.\n\nVielen Dank!");
			connection.disconnect();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			HIRuntime.getGui().displayInfoDialog("Feedback Fehlgeschlagen", "Leider konnte Ihr Feedback aufgrund eines Systemfehlers nicht gesendet werden.\n"
					+"Bitte versuchen Sie es später erneut.\n\n"
					+"Sollte das Problem bestehen bleiben wenden Sie sich bitte per mail an die Entwickler.\n\nVielen Dank!");
		} catch (IOException e2) {
			e2.printStackTrace();
			HIRuntime.getGui().displayInfoDialog("Feedback Fehlgeschlagen", "Leider konnte Ihr Feedback aufgrund eines Systemfehlers nicht gesendet werden.\n"
					+"Bitte versuchen Sie es später erneut.\n\n"
					+"Sollte das Problem bestehen bleiben wenden Sie sich bitte per mail an die Entwickler.\n\nVielen Dank!");
		}
		
		HIRuntime.getGui().deregisterComponent(this, false);
		
	}

}
