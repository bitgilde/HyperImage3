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
import org.hyperimage.service.util.ServerPreferences;

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
