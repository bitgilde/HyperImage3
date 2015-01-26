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

package org.hyperimage.service.ws;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import org.hyperimage.service.aspect.MaintenanceModeAspect;
import org.hyperimage.service.exception.HIMaintenanceModeException;
import org.hyperimage.service.exception.HIParameterException;
import org.hyperimage.service.model.HIUser;
import org.hyperimage.service.util.PRrest;
import org.hyperimage.service.util.ServerPreferences;
import org.scribe.oauth.Scribe;
import org.scribe.oauth.Token;

/**
 * 
 * Class: HILogin
 * Package: org.hyperimage.service.ws
 * @author Jens-Martin Loebel
 * 
 * Provides the HyperImage Login WebService via Glassfish
 *
 */

@WebService(serviceName = "HILoginService")
public class HILogin {

    @PersistenceUnit(unitName = "HIEditor_ORM")
    @PersistenceContext(unitName = "HIEditor_ORM")
    EntityManagerFactory emf;

    private String m_strPPGLocation = "";
    ServerPreferences prefs = new ServerPreferences();
	
	/**
	 * 
	 */
    public HILogin() {
        m_strPPGLocation = prefs.getPPGPref();
        HIEditor.m_logger.log(Level.INFO, "PPG Location: {0}", m_strPPGLocation);
        HIEditor.m_logger.log(Level.INFO, "HIStore Location: {0}", prefs.getHIStorePref());
        HIEditor.m_logger.log(Level.INFO, "Prometheus API URL: {0}", prefs.getPrometheusAPIPref());
    }

    /**
     * WebService method: Returns the URL of PostPeTAL generator JNLP launch
     * file
     *
     * @since 2.0.86
     * @return URL of PostPeTAL generator JNLP launch file
     */
    @WebMethod
    public String getPPGLocation() {
        return m_strPPGLocation;
    }

    //-------------------------
    // Authentication Methods
    //-------------------------
    /**
     * Authenticate the user against the HIEditor webservice.
     *
     * @param username
     * @param password
     * @return <code>true</code> if the user corresponding to the username was
     * found in the DB and the password matches; <code>false</code> otherwise
     * @throws HIParameterException
     * @throws HIMaintenanceModeException
     */
    @WebMethod
    public W3CEndpointReference authenticate(
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password) throws HIParameterException, HIMaintenanceModeException {

        // needed for JAI headless
        System.setProperty("java.awt.headless", "true");

        // check Parameter
        HIEditor.checkParam(username, false);
        HIEditor.checkParam(password, false);


        // init database if necessary
        HIUser sysop;

        EntityManager em = emf.createEntityManager();
        try {
            sysop = (HIUser) em.createQuery("SELECT u FROM HIUser u WHERE u.userName=:username")
                    .setParameter("username", "sysop")
                    .getSingleResult();
                        
        } catch (NoResultException e) {
            try {
                sysop = new HIUser("System", "Operator", "sysop@hyperimage.eu", "sysop", "secret");
                UserTransaction utx = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
                utx.begin();
                em.joinTransaction();
                em.persist(sysop);
                utx.commit();
                System.out.println("NOTE: HI DB sysop-user created. PLEASE CHANGE SYSOP PASSWORD IMMEDIATELY!");
            } catch (NamingException | NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HILogin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        HIUser user = null;

        try {
            user = (HIUser) em.createQuery("SELECT u FROM HIUser u WHERE u.userName=:username AND u.passwordHash=:hash")
                    .setParameter("username", username)
                    .setParameter("hash", HIUser.getMD5HashString(password))
                    .getSingleResult();
        } catch (NoResultException e) {
            // user not found in db
        }


        if (user != null) {
            HIEditor editor;
            try {
                editor = new HIEditor(user);

                // check if we are in maintenance mode
                MaintenanceModeAspect.checkMaintenanceMode(user);

                // set timeout to 60 minutes (1000*60*60)
                HIEditor.manager.setTimeout((long) (1000 * 60 * 60), null);

                Logger.getLogger(HILogin.class.getName()).log(Level.INFO, "user ''{0}'' logged in...", user.getUserName());
                // return endpoint reference to manager service                
                return HIEditor.manager.export(editor);
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            return null;
        }

        return null;
    }
    
    
    /**
     * Authenticate a Prometheus user against the HIEditor webservice using OAuth.
     *
     * @param token
     * @param tokenSecret
     * @param tokenVerifier
     * @param userID
     * @return 
     */
    @WebMethod
    @SuppressWarnings("null")
    public W3CEndpointReference authenticatePR (
            @WebParam(name = "token") String token, 
            @WebParam(name = "tokenSecret") String tokenSecret, 
            @WebParam(name = "tokenVerifier") String tokenVerifier, 
            @WebParam(name = "userID") String userID) {
        
        
        HIUser user = null;
        Properties props = new Properties();
        props.setProperty("request.token.verb", "POST");
        props.setProperty("request.token.url",  "http://prometheus-test.uni-koeln.de/pandora-devel/oauth/request_token");
        props.setProperty("access.token.verb",  "POST");
        props.setProperty("access.token.url",   "http://prometheus-test.uni-koeln.de/pandora-devel/oauth/access_token");
        props.setProperty("callback.url",       "oob");
        props.setProperty("consumer.key",       prefs.getPrometheusOAUTHConsumerKey());
        props.setProperty("consumer.secret",    prefs.getPrometheusOAUTHConsumerSecret());
            
        Token accessToken;
        PRrest pr = null;
        try {
            Scribe scribe = new Scribe(props);
            Token requestToken = new Token(token, tokenSecret);
            accessToken  = scribe.getAccessToken(requestToken, tokenVerifier);
//            System.out.println("DEBUG sec: " + accessToken.getSecret() + " tok: " + accessToken.getToken() );
            pr = new PRrest(prefs, scribe, accessToken);
            System.out.println("user login @ Prometheus successful");
            
            // verify Prometheus user with HI
            HIUser prUser = pr.getPRUserInfo(); // get user info from Prometheus API
            // try to find user in HI database
            EntityManager em = emf.createEntityManager();
            try {
                user = (HIUser) em.createQuery("SELECT u FROM HIUser u WHERE u.userName=:username")
                    .setParameter("username", prUser.getUserName())
                    .getSingleResult();
                System.out.println("User '"+prUser.getUserName()+"' found!");
            } catch (NoResultException e) {
            // user not found in db, create HI user with Prometheus info
                System.out.println("User '"+prUser.getUserName()+"' not found!");
                try {
                    user = prUser;
                    UserTransaction utx = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
                    utx.begin();
                    em.joinTransaction();
                    em.persist(user);
                    utx.commit();
                } catch (NamingException | NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                    Logger.getLogger(HILogin.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        System.out.println("HI User ID: "+user.getId());
            
        } catch (Exception e) {
            System.out.println("OAUTH EXCEPTION FOR PROMETHEUS USER ID '"+userID+"': "+e.getMessage());
            return null;
        }
        
        /*
                    project = MI_port.sysopCreateProject(user_str, "de");
                    MI_port.setProject(project);

                    MI_port.adminAddTemplateToProject(MetadataHelper.getPRTemplateBlueprint());
                    title = "MI : " + user_str + " : " + user_name;
                    MI_port.updateProjectMetadata("de", title);
                    MI_port.adminAddLanguageToProject("en");

        */
        
        
        HIEditor editor = null;
        if ( user == null || pr == null ) return null;
        try {
            editor = new HIEditor(user, pr);
        } catch (InstantiationException ex) {
            Logger.getLogger(HILogin.class.getName()).log(Level.SEVERE, null, ex);
        }

        // set timeout to 60 minutes (1000*60*60)
        HIEditor.manager.setTimeout((long) (1000 * 60 * 60), null);

        // return endpoint reference to manager service
        return HIEditor.manager.export(editor);
     
    }
    

    /**
     *
     * @param editorRef
     * @return
     */
    @WebMethod
    public boolean logout(
            @WebParam(name = "editorRef") W3CEndpointReference editorRef) {
        boolean wasLoggedIn = true;

        HIEditor editor = HIEditor.manager.resolve(editorRef);
        if (editor == null) {
            wasLoggedIn = false;
        } else {
            Logger.getLogger(HILogin.class.getName()).log(Level.INFO, "user ''{0}'' logout", editor.getCurrentUser());
            HIEditor.manager.unexport(editor);
        }

        return wasLoggedIn;
    }

    @WebMethod
    public String getVersionID() {
        return HIEditor.getServiceVersion();
    }



}
