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

import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.xml.ws.developer.StatefulWebServiceManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.media.jai.JAI;
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
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;
import org.hyperimage.service.exception.HIEntityException;
import org.hyperimage.service.exception.HIEntityNotFoundException;
import org.hyperimage.service.exception.HIMaintenanceModeException;
import org.hyperimage.service.exception.HIParameterException;
import org.hyperimage.service.exception.HIPrivilegeException;
import org.hyperimage.service.exception.HIServiceException;
import org.hyperimage.service.model.HIBase;
import org.hyperimage.service.model.HIFlexMetadataName;
import org.hyperimage.service.model.HIFlexMetadataRecord;
import org.hyperimage.service.model.HIFlexMetadataSet;
import org.hyperimage.service.model.HIFlexMetadataTemplate;
import org.hyperimage.service.model.HIGroup;
import org.hyperimage.service.model.HIInscription;
import org.hyperimage.service.model.HIKeyValue;
import org.hyperimage.service.model.HILanguage;
import org.hyperimage.service.model.HILayer;
import org.hyperimage.service.model.HILightTable;
import org.hyperimage.service.model.HIObject;
import org.hyperimage.service.model.HIObjectContent;
import org.hyperimage.service.model.HIPreference;
import org.hyperimage.service.model.HIProject;
import org.hyperimage.service.model.HIProjectMetadata;
import org.hyperimage.service.model.HIQuickInfo;
import org.hyperimage.service.model.HIRepository;
import org.hyperimage.service.model.HIRole;
import org.hyperimage.service.model.HIText;
import org.hyperimage.service.model.HIURL;
import org.hyperimage.service.model.HIUser;
import org.hyperimage.service.model.HIView;
import org.hyperimage.service.model.render.HILayerRenderer;
import org.hyperimage.service.model.render.RelativePolygon;
import org.hyperimage.service.search.HIIndexer;
import org.hyperimage.service.storage.FileStorageManager;
import org.hyperimage.service.util.ImageHelper;
import org.hyperimage.service.util.MetadataHelper;
import org.hyperimage.service.util.ServerPreferences;

/**
 *
 * Class: HIEditor Package: org.hyperimage.service.ws
 *
 * @author Jens-Martin Loebel
 *
 * Provides the HyperImage WebService Interface via Glassfish
 */

@com.sun.xml.ws.developer.Stateful
@Stateful
@WebService(serviceName = "HIEditorService")
@Addressing
@BindingType(value = javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_MTOM_BINDING)
public class HIEditor {

    public static StatefulWebServiceManager<HIEditor> manager;
    public static final Logger m_logger = Logger.getLogger(HIEditor.class.getName());

    // lucene DB indexer for HI Elements 
    private HIIndexer indexer;

    // HI Web Service Version
    private static final String serviceVersion = "3.0";
    private static final String minorRev = "1";

    public static String getServiceVersion() {
        return serviceVersion + "." + minorRev;
    }

    @PersistenceContext(unitName = "HIEditor_ORM")
    @PersistenceUnit(unitName = "HIEditor_ORM")
    EntityManagerFactory emf;

    @Resource
    WebServiceContext wsContext;
    
    @Resource
    UserTransaction utx;

    public static enum WSstates {

        AUTHENTICATED, PROJECT_SELECTED
    };
    private WSstates state;

    private HIUser curUser;
    private HIRole curRole = null;
    private HIProject curProject = null;
    private HIGroup importGroup = null;
    private HIGroup trashGroup = null;

    private String repositoryLocation;
    private FileStorageManager storageManager;

    public static enum HI_ImageSizes {

        HI_THUMBNAIL, HI_PREVIEW, HI_FULL, HI_ORIGINAL
    };

    
    /**
     * Constructor - should only be instantiated by HILogin WebService
     *
     * @param curUser logged in user, authenticated and passed on by the HILogin
     * WebService
     * @throws InstantiationException
     */
    public HIEditor(HIUser curUser) throws InstantiationException {
        state = WSstates.AUTHENTICATED;
        this.curUser = curUser;

        ServerPreferences prefs = new ServerPreferences();
        this.repositoryLocation = prefs.getHIStorePref();
        m_logger.info("HIStore Location: " + repositoryLocation);
        this.storageManager = new FileStorageManager(repositoryLocation);
        indexer = new HIIndexer(repositoryLocation);
    }
   
    public HIEditor() throws InstantiationException {
        throw new InstantiationException("HIEditorService initialized without user");
    }

    
    /**
     * Gives the current implementation version ID for the HyperImage Service
     * components
     *
     * @return version ID of HIEditor WebService
     */
    @WebMethod
    public String getVersionID() {
        
        return getServiceVersion();
    }

    //-------------------------
    // User Methods
    //-------------------------
    /**
     * WebService method: Returns the role for the current user in the current
     * project. If no project is selected <code>null</code> is returned.
     *
     * @return role of current user in current project or <code>null</code> if
     * no project is selected.
     */
    @WebMethod
    public HIRole.HI_Roles getCurrentRole() throws HIMaintenanceModeException {
        
        if (curRole == null) {
            return null;
        }
        return curRole.getType();
    }

    /**
     * WebService method: Returns the role for a given user ID in a given
     * project ID. A project admin may only call this function with the current
     * project ID (where he or she is the admin). A regular user or guest cannot
     * invoke this function. The sysop user may use any registered user and
     * project ID.
     *
     * @param userID user ID for role lookup
     * @param projectID project ID for role lookup
     * @return role for given user in given project
     * @throws HIServiceException thrown if no project is selected (when invoked
     * by admin) or the given user does not belong to the given project
     * @throws HIPrivilegeException thrown if invoked by admin with different
     * project ID than the current project or if invoked by regular user or
     * guest.
     * @throws HIEntityNotFoundException thrown if user ID or project ID could
     * not be found
     * @throws HIMaintenanceModeException thrown if HIEditor WebService is in
     * maintenance mode
     */
    @WebMethod
    public HIRole.HI_Roles adminGetRoleInProject(
            @WebParam(name = "userID") long userID,
            @WebParam(name = "projectID") long projectID)
            throws HIServiceException, HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        HIRole role = null;

        if (!isSysop() && curProject == null) {
            throw new HIServiceException("You need to select a project first!");
        }

        EntityManager em = emf.createEntityManager();
        if (curProject != null) {
            curProject = em.find(HIProject.class, curProject.getId());
        }

        HIUser user = em.find(HIUser.class, userID);
        if (user == null) {
            throw new HIEntityNotFoundException("User not found!");
        }
        HIProject project = em.find(HIProject.class, projectID);
        if (project == null) {
            throw new HIEntityNotFoundException("Project not found!");
        }

        if (!isSysop() && curProject.getId() != project.getId()) {
            throw new HIPrivilegeException("User does not belong to current project!");
        }

        role = getRole(user, project);
        if (role == null) {
            throw new HIServiceException("User does not belong to project!");
        }

        return role.getType();
    }

    /**
     *
     * @param project
     * @return
     * @throws HIServiceException
     * @throws HIMaintenanceModeException
     */
    @SuppressWarnings("unchecked")
    @WebMethod
    public boolean setProject(
            @WebParam(name = "project") HIProject project)
            throws HIServiceException, HIMaintenanceModeException {

        
        checkParam(project);

        // find and refresh project from db
        HIProject dbProject = null;
        EntityManager em = emf.createEntityManager();
        dbProject = em.find(HIProject.class, project.getId());
        // if project cannot be found --> abort
        if (dbProject == null) return false;

        // check if user is a member of desired project, if so retrieve role, if not --> abort
        HIRole role = null;
        if (!isSysop()) {
            role = getRole(curUser, dbProject);
            if (role == null) {
                return false;
            }
        }
        // update WS state, set project and role
        state = WSstates.PROJECT_SELECTED;
        curProject = dbProject;
        
        curProject.setUsed(storageManager.getUsedSpaceProject(curProject));

        if (!isSysop()) curRole = role; else curRole = null;

        try {
            // find the import group for this project
            importGroup = (HIGroup) em.createQuery("SELECT g from HIGroup g WHERE g.project=:project AND g.type=:type")
                    .setParameter("project", curProject)
                    .setParameter("type", HIGroup.GroupTypes.HIGROUP_IMPORT)
                    .getSingleResult();

            // find the trash group for this project
            trashGroup = (HIGroup) em.createQuery("SELECT g from HIGroup g WHERE g.project=:project AND g.type=:type")
                    .setParameter("project", curProject)
                    .setParameter("type", HIGroup.GroupTypes.HIGROUP_TRASH)
                    .getSingleResult();

        } catch (NoResultException e) {
            curProject = null;
            curRole = null;
            importGroup = null;
            trashGroup = null;
            state = WSstates.AUTHENTICATED;
            throw new HIServiceException("Mandatory project groups not found!");
        }
        Logger.getLogger(HIEditor.class.getName()).log(Level.INFO, "set project id: "+curProject.getId()+" for user: "+curUser.getUserName());


        // DEBUG find orphaned content and add it to the import group
        // log all actions
        /*		List<HIBase> contents = em.createQuery("SELECT b from HIBase b WHERE b.project=:project")
         .setParameter("project", curProject)
         .getResultList();
         m_logger.info("Project P"+curProject.getId()+": contains " + contents.size() + " elements\n");

         int lostContent = 0;
         for ( HIBase content : contents ) {
         // filter out object content
         if ( !(content instanceof HILayer) && !(content instanceof HIView) && !(content instanceof HIInscription) && !(content instanceof HIGroup) ) {
				
         // count groups for base
         long size = (Long) em.createQuery("SELECT count(g) FROM HIGroup g WHERE g.contents=:base")
         .setParameter("base", content)
         .getSingleResult();
				
         // found orphaned content
         if ( size < 1 ) {	
         lostContent = lostContent + 1;
         m_logger.info("Project P"+curProject.getId()+": found orphaned content ID:"+content.getId()+" Type:"+content.getClass());
         // add content back to import group
         if ( addToImportGroup(content) )
         m_logger.info("Project P"+curProject.getId()+": content successfully added to import group!");
         else
         m_logger.info("Project P"+curProject.getId()+": FAILED to add content to import group!");
         }
         }
         }
         if ( lostContent == 0 )
         m_logger.info("Project P"+curProject.getId()+": No orphaned content found.");
         else
         m_logger.info("Project P"+curProject.getId()+": found "+lostContent+" lost elements.");
         */
        return true;
    }
    
    public static String findPreferenceValue(List<HIPreference> prefs, String key) {
        for (HIPreference pref : prefs) {
            if (pref.getKey().compareTo(key) == 0) {
                return pref.getValue();
            }
        }

        return null;
    }
	    
    // -- SYSOP METHOD, a user gets a list of all his projects, the sysop gets all projects in the database --
    @SuppressWarnings("unchecked")
    @WebMethod
    public List<HIProject> getProjects()
            throws HIMaintenanceModeException {
        
        List<HIProject> projects;

        String query = "SELECT r.project FROM HIRole r WHERE r.user=:user";
        // the sysop user gets all projects currently in db
        EntityManager em = emf.createEntityManager();
        if (isSysop()) {
            query = "SELECT p FROM HIProject p";
            projects = (List<HIProject>) em.createQuery(query)
                    .getResultList();
        } else {
            projects = (List<HIProject>) em.createQuery(query)
                    .setParameter("user", curUser)
                    .getResultList();
        }

        for (HIProject project : projects) {
            project.setUsed(storageManager.getUsedSpaceProject(project));
            if (project.getStartObject() != null) {
                project.setStartObjectInfo(createContentQuickInfo(project.getStartObject()));
            }
        }

        return projects;
    }

    // -- SYSOP METHOD --
    @WebMethod
    public synchronized HIUser sysopCreateUser(
            @WebParam(name = "firstName") String firstName,
            @WebParam(name = "lastName") String lastName,
            @WebParam(name = "userName") String userName,
            @WebParam(name = "password") String password,
            @WebParam(name = "email") String email)
            throws HIParameterException, HIEntityException, HIMaintenanceModeException {

        
        HIUser user = null;

        checkParam(firstName, true);
        checkParam(lastName, false);
        checkParam(userName, false);
        checkParam(password, false);
        checkParam(email, true);

        // check for duplicate user names
        boolean userFound = true;
        EntityManager em = emf.createEntityManager();
        try {
            user = (HIUser) em.createQuery("SELECT u FROM HIUser u WHERE u.userName=:userName")
                    .setParameter("userName", userName)
                    .getSingleResult();
        } catch (NoResultException e) {
            userFound = false;
        }
        if (userFound) {
            throw new HIEntityException("Username already taken!");
        }

        // create user
        user = new HIUser(firstName, lastName, email, userName, password);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(user);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return user;
    }

    // -- ADMIN METHOD --
    @WebMethod
    public HIUser adminGetUserByID(
            @WebParam(name = "userID") long userID)
            throws HIEntityNotFoundException, HIMaintenanceModeException {

        
        HIUser user = null;
        // retrieve user record
        EntityManager em = emf.createEntityManager();
        user = em.find(HIUser.class, userID);
        if (user == null) {
            throw new HIEntityNotFoundException("User not found!");
        }

        return user;
    }

    // -- ADMIN METHOD --
    @WebMethod
    public HIUser adminGetUserByUserName(
            @WebParam(name = "userName") String userName)
            throws HIParameterException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        HIUser user = null;
        checkParam(userName, false);

        // retrieve user record
        EntityManager em = emf.createEntityManager();
        try {
            user = (HIUser) em.createQuery("SELECT u FROM HIUser u WHERE u.userName=:userName")
                    .setParameter("userName", userName).getSingleResult();
        } catch (NoResultException e) {
            throw new HIEntityNotFoundException("User not found!");
        }

        return user;
    }

    // -- SYSOP METHOD, however a user may edit his own profile --
    @WebMethod
    public synchronized boolean updateUser(
            @WebParam(name = "userID") long userID,
            @WebParam(name = "firstName") String firstName,
            @WebParam(name = "lastName") String lastName,
            @WebParam(name = "email") String email)
            throws HIParameterException, HIEntityNotFoundException, HIPrivilegeException, HIMaintenanceModeException {

        
        HIUser user;
        EntityManager em = emf.createEntityManager();
        curUser = em.find(HIUser.class, curUser.getId());

        checkParam(firstName, true);
        checkParam(lastName, false);
        checkParam(email, true);

        // find user record
        user = em.find(HIUser.class, userID);
        if (user == null) {
            throw new HIEntityNotFoundException("User not found!");
        }

        // a user may only edit his own record, only the sysop can edit every user�s profile
        if (user.getId() != curUser.getId() && !curUser.getUserName().equalsIgnoreCase("sysop")) {
            throw new HIPrivilegeException("You can�t edit another user�s profile!");
        }

        // update and persist record
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(user);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    // -- SYSOP METHOD, however a user may edit his own profile --
    @WebMethod
    public synchronized boolean updateUserPassword(
            @WebParam(name = "userID") long userID,
            @WebParam(name = "password") String password)
            throws HIParameterException, HIEntityNotFoundException, HIPrivilegeException, HIMaintenanceModeException {

        
        HIUser user;
        EntityManager em = emf.createEntityManager();
        curUser = em.find(HIUser.class, curUser.getId());

        checkParam(password, false);

        // find user record
        user = em.find(HIUser.class, userID);
        if (user == null) {
            throw new HIEntityNotFoundException("User not found!");
        }

        // a user may only edit his own record, only the sysop can edit every user�s profile
        if (user.getId() != curUser.getId() && !curUser.getUserName().equalsIgnoreCase("sysop")) {
            throw new HIPrivilegeException("You can�t edit another user�s profile!");
        }

        // update and persist record
        user.setPassword(password);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(user);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    // -- SYSOP METHOD --
    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized boolean sysopDeleteUser(
            @WebParam(name = "userID") long userID)
            throws HIEntityNotFoundException, HIPrivilegeException, HIMaintenanceModeException {

        
        HIUser user;

        EntityManager em = emf.createEntityManager();
        curUser = em.find(HIUser.class, curUser.getId());

        // check integrity (sanity check)
        user = em.find(HIUser.class, userID);
        if (user == null) {
            throw new HIEntityNotFoundException("User not found!");
        }
        if (user.getUserName().equalsIgnoreCase("sysop")) {
            throw new HIPrivilegeException("can�t delete the sysop user!");
        }
        if (curUser.getId() == userID) {
            throw new HIPrivilegeException("can�t delete current (logged in) user!");
        }
        if (user.getUserName().equalsIgnoreCase("sysop")) {
            throw new HIPrivilegeException("can�t delete the sysop user!");
        }

        // remove user from all projects
        List<HIRole> roles = em.createQuery("SELECT r FROM HIRole r WHERE r.user=:user")
                .setParameter("user", user).getResultList();
        try {
            utx.begin();
            em.joinTransaction();

            for (HIRole role : roles) {
                System.out.println("Role ID: " + role.getId());
                em.remove(role);
                em.flush();
            }

            // TODO: remove all repositories this user may have created
            // remove user
            em.remove(user);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    // -- SYSOP METHOD --
    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized List<HIProject> sysopGetProjectsForUser(
            @WebParam(name = "userID") long userID)
            throws HIEntityNotFoundException, HIMaintenanceModeException {

        
        List<HIProject> projects = null;
        // retrieve user
        HIUser user = adminGetUserByID(userID);
        // find all user projects
        EntityManager em = emf.createEntityManager();
        if (user != null) {
            projects = (List<HIProject>) em.createQuery("SELECT r.project from HIRole r WHERE r.user=:user")
                    .setParameter("user", user)
                    .getResultList();
        }

        for (HIProject project : projects) {
            if (project.getStartObject() != null) {
                project.setStartObjectInfo(createContentQuickInfo(project.getStartObject()));
            }
        }

        return projects;
    }

    // -- ADMIN METHOD --
    @WebMethod
    public synchronized boolean adminSetProjectRole(
            @WebParam(name = "userID") long userID,
            @WebParam(name = "role") HIRole.HI_Roles role)
            throws HIPrivilegeException, HIServiceException, HIMaintenanceModeException {

        
        // retrieve user record
        HIUser user = adminGetUserByID(userID);

        if (user.getId() == curUser.getId()) {
            throw new HIPrivilegeException("You can�t take away your admin status!");
        }
        if (getState() != WSstates.PROJECT_SELECTED) {
            throw new HIServiceException("You need to select a project first!");
        }

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // retrieve role for this user in the current project
        HIRole projectRole = null;

        try {
            projectRole = (HIRole) em.createQuery("SELECT r from HIRole r WHERE r.user=:user AND r.project=:project")
                    .setParameter("user", user)
                    .setParameter("project", curProject)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new HIPrivilegeException("User does not belong to current project!");
        }

        // update role
        projectRole.setType(role);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(projectRole);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    // -- ADMIN METHOD, admins may add/remove users to/from the current project --
    @WebMethod
    public synchronized boolean adminAddUserToProject(
            @WebParam(name = "userID") long userID,
            @WebParam(name = "role") HIRole.HI_Roles role)
            throws HIServiceException, HIMaintenanceModeException {

        
        // retrieve user record
        HIUser user = adminGetUserByID(userID);
        if (getState() != WSstates.PROJECT_SELECTED) {
            throw new HIServiceException("You need to select a project first!");
        }

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check if user is already in project
        HIRole projectRole = null;
        try {
            projectRole = (HIRole) em.createQuery("SELECT r from HIRole r WHERE r.user=:user AND r.project=:project")
                    .setParameter("user", user)
                    .setParameter("project", curProject)
                    .getSingleResult();
        } catch (NoResultException e) {
        }
        try {
            utx.begin();
            em.joinTransaction();
            if (projectRole == null) {
                // add user to project
                projectRole = new HIRole(user, curProject, role);
                em.persist(projectRole);
                em.flush();
            } else {
                // user already in project, just change role privileges
                projectRole.setType(role);
                em.persist(projectRole);
                em.flush();
                if (user.getId() == curUser.getId()) {
                    curRole = projectRole;
                }
            }
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    // -- ADMIN METHOD --
    @WebMethod
    public synchronized boolean adminRemoveUserFromProject(
            @WebParam(name = "userID") long userID)
            throws HIPrivilegeException, HIServiceException, HIMaintenanceModeException {

        
        // retrieve user record
        HIUser user = adminGetUserByID(userID);
        if (user.getId() == curUser.getId() && !isSysop()) {
            throw new HIPrivilegeException("You can�t modify the current user!");
        }
        if (getState() != WSstates.PROJECT_SELECTED) {
            throw new HIServiceException("You need to select a project first!");
        }

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check if user is in project
        HIRole projectRole = null;
        try {
            projectRole = (HIRole) em.createQuery("SELECT r from HIRole r WHERE r.user=:user AND r.project=:project")
                    .setParameter("user", user)
                    .setParameter("project", curProject)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new HIPrivilegeException("User does not belong to current project!");
        }
        try {
            utx.begin();
            em.joinTransaction();
            // remove user from project
            em.remove(projectRole);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    // -- SYSOP METHOD, sysops can add/remove users to/from any project --
    @WebMethod
    public synchronized boolean sysopAddUserToProject(
            @WebParam(name = "userID") long userID,
            @WebParam(name = "projectID") long projectID,
            @WebParam(name = "role") HIRole.HI_Roles role)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        // retrieve user record
        HIUser user = adminGetUserByID(userID);
        if (user.getId() == curUser.getId() && !isSysop()) {
            throw new HIPrivilegeException("You can�t modify the current user!");
        }
        // retrieve project		
        EntityManager em = emf.createEntityManager();
        HIProject project = em.find(HIProject.class, projectID);
        if (project == null) {
            throw new HIEntityNotFoundException("Project not found!");
        }

        // check if user is already in project
        HIRole projectRole = null;
        try {
            projectRole = (HIRole) em.createQuery("SELECT r from HIRole r WHERE r.user=:user AND r.project=:project")
                    .setParameter("user", user)
                    .setParameter("project", project)
                    .getSingleResult();
        } catch (NoResultException e) {
        }
        try {
            utx.begin();
            em.joinTransaction();
            if (projectRole == null) {
                // add user to project
                projectRole = new HIRole(user, project, role);
                em.persist(projectRole);
                em.flush();
            } else {
                // user already in project, just change role privileges
                projectRole.setType(role);
                em.persist(projectRole);
                em.flush();
            }
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    // -- SYSOP METHOD --
    @WebMethod
    public synchronized boolean sysopRemoveUserFromProject(
            @WebParam(name = "userID") long userID,
            @WebParam(name = "projectID") long projectID)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        // retrieve user record
        HIUser user = adminGetUserByID(userID);
        if (user.getId() == curUser.getId() && !isSysop()) {
            throw new HIPrivilegeException("You can�t modify the current user!");
        }
        // retrieve project
        EntityManager em = emf.createEntityManager();
        HIProject project = em.find(HIProject.class, projectID);
        if (project == null) {
            throw new HIEntityNotFoundException("Project not found!");
        }

        // check if user is in project
        HIRole projectRole = null;
        try {
            projectRole = (HIRole) em.createQuery("SELECT r from HIRole r WHERE r.user=:user AND r.project=:project")
                    .setParameter("user", user)
                    .setParameter("project", project)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new HIPrivilegeException("User does not belong to project!");
        }
        try {
            utx.begin();
            em.joinTransaction();
            // remove user from project
            em.remove(projectRole);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;

    }

    // -- SYSOP METHOD --
    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized List<HIUser> sysopGetUsers() throws HIMaintenanceModeException {

        
        List<HIUser> users = null;
        EntityManager em = emf.createEntityManager();
        users = em.createQuery("SELECT u FROM HIUser u ORDER BY u.lastName").getResultList();
        if (users == null) {
            users = new ArrayList<HIUser>();
        }

        return users;
    }

    // -- ADMIN METHOD --
    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized List<HIUser> adminGetUsers() throws HIServiceException, HIMaintenanceModeException {

        
        if (getState() != WSstates.PROJECT_SELECTED) {
            throw new HIServiceException("You need to select a project first!");
        }

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        List<HIUser> users = null;
        users = em.createQuery("SELECT u FROM HIUser u ORDER BY u.lastName").getResultList();
        if (users == null) {
            users = new ArrayList<HIUser>();
        }

        return users;
    }

    // -- SYSOP METHOD --
    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized List<HIUser> sysopGetUsersForProject(
            @WebParam(name = "projectID") long projectID)
            throws HIEntityNotFoundException, HIMaintenanceModeException {

        
        List<HIUser> users;

        // find project
        EntityManager em = emf.createEntityManager();
        HIProject project = em.find(HIProject.class, projectID);
        if (project == null) {
            throw new HIEntityNotFoundException("Project not found!");
        }

        String query = "SELECT r.user FROM HIRole r WHERE r.project=:project";
        users = (List<HIUser>) em.createQuery(query)
                .setParameter("project", project)
                .getResultList();

        return users;
    }

    // -- SYSOP METHOD --
    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized List<HIUser> adminGetProjectUsers()
            throws HIServiceException, HIPrivilegeException, HIMaintenanceModeException {

        
        if (getState() != WSstates.PROJECT_SELECTED) {
            throw new HIServiceException("You need to select a project first!");
        }

        List<HIUser> users;

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        String query = "SELECT r.user FROM HIRole r WHERE r.project=:project";
        users = (List<HIUser>) em.createQuery(query)
                .setParameter("project", curProject)
                .getResultList();

        return users;
    }

    //-------------------------
    // Project Methods
    //-------------------------
    // -- SYSOP METHOD --
    @WebMethod
    public synchronized HIProject sysopCreateProject(
            @WebParam(name = "adminUsername") String adminUsername,
            @WebParam(name = "defaultLanguage") String defaultLanguage)
            throws HIParameterException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        HIProject project = null;

        checkParam(adminUsername, false);
        checkParam(defaultLanguage, false);

        HIUser projectAdmin = null;
        EntityManager em = emf.createEntityManager();
        try {
            projectAdmin = (HIUser) em.createQuery("SELECT u from HIUser u WHERE u.userName=:username")
                    .setParameter("username", adminUsername)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new HIEntityNotFoundException("User not found!");
        }

        // create import and trash groups
        HIGroup importGroup = new HIGroup(HIGroup.GroupTypes.HIGROUP_IMPORT, false);
        HIGroup trashGroup = new HIGroup(HIGroup.GroupTypes.HIGROUP_TRASH, false);

        project = new HIProject();
        HILanguage defLang = new HILanguage(defaultLanguage);
        project.setDefaultLanguage(defLang);
        project.getLanguages().add(defLang);

        importGroup.setProject(project);
        trashGroup.setProject(project);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(importGroup);
            em.flush();
            em.persist(trashGroup);
            em.flush();

            em.persist(project);
            em.flush();

            // add empty metadata in new language to project metadata
            HIProjectMetadata projMetadata = new HIProjectMetadata(defLang.getLanguageId(), "", project);
            project.getMetadata().add(projMetadata);
            em.persist(projMetadata);
            em.flush();
            em.persist(project);
            em.flush();

            // add groupSortOrder Preference
            HIPreference newPref = new HIPreference("groupSortOrder", "", project);
            em.persist(newPref);
            em.persist(project);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        // attach base internal templates
        addSystemTemplates(project);

        setRole(projectAdmin, project, HIRole.HI_Roles.ADMIN);

        if (project.getStartObject() != null) {
            project.setStartObjectInfo(createContentQuickInfo(project.getStartObject()));
        }

        // update lucene index
        indexer.initIndex(project);

        return project;
    }

    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized boolean adminAddLanguageToProject(
            @WebParam(name = "language") String language)
            throws HIServiceException, HIMaintenanceModeException {

        
        if (getState() != WSstates.PROJECT_SELECTED) {
            throw new HIServiceException("You need to select a project first!");
        }

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check if language is already in project
        for (HILanguage projLang : curProject.getLanguages()) {
            if (projLang.getLanguageId().equalsIgnoreCase(language)) {
                return false;
            }
        }

        // add language to project languages
        HILanguage newLang = new HILanguage(language);
        newLang.setProject(curProject);
        curProject.getLanguages().add(newLang);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(newLang);
            em.persist(curProject);
            em.flush();

            // add empty metadata in new language to project metadata
            HIProjectMetadata projMetadata = new HIProjectMetadata(newLang.getLanguageId(), "", curProject);
            curProject.getMetadata().add(projMetadata);
            em.persist(projMetadata);
            em.flush();
            em.persist(curProject);
            em.flush();

            // add empty metadata in new language to all project entities
            try {
                List<HIBase> projectEntities = em.createQuery("SELECT b FROM HIBase b WHERE b.project=:project")
                        .setParameter("project", curProject)
                        .getResultList();

                for (HIBase base : projectEntities) {
                    if (!(base instanceof HIURL) && !(base instanceof HILightTable)) {
                        createFlexMetadataRecord(em, base, language);
                        em.persist(base);
                    }
                }
                em.flush();
            } catch (NoResultException e) {
                // no entities in project
            }
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized boolean adminRemoveLanguageFromProject(
            @WebParam(name = "language") String language)
            throws HIServiceException, HIMaintenanceModeException {

        
        if (getState() != WSstates.PROJECT_SELECTED) {
            throw new HIServiceException("You need to select a project first!");
        }

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check if language is in project
        HILanguage projLang = null;
        int projLangIndex = -1;
        for (int i = 0; i < curProject.getLanguages().size(); i++) {
            HILanguage lang = curProject.getLanguages().get(i);
            if (lang.getLanguageId().equalsIgnoreCase(language)) {
                projLang = lang;
                projLangIndex = i;
            }
        }
        if (projLang == null) {
            return false;
        }

        // check if language is default language
        if (curProject.getDefaultLanguage().getLanguageId().equalsIgnoreCase(language)) {
            return false;
        }

        // a project needs at least one language (sanity check)
        if (curProject.getLanguages().size() <= 1) {
            return false;
        }

        // remove project metadata in this language
        int metadataIndex = -1;
        for (int i = 0; i < curProject.getMetadata().size(); i++) {
            if (curProject.getMetadata().get(i).getLanguageID().equalsIgnoreCase(projLang.getLanguageId())) {
                metadataIndex = i;
            }
        }
        try {
            utx.begin();
            em.joinTransaction();

            if (metadataIndex >= 0) {
                HIProjectMetadata metadata = curProject.getMetadata().get(metadataIndex);
                curProject.getMetadata().remove(metadataIndex);
                em.persist(curProject);
                em.flush();
                em.remove(metadata);
                em.flush();
            }

            // remove metadata in language from all project entities
            try {
                List<HIBase> projectEntities = em.createQuery("SELECT b FROM HIBase b WHERE b.project=:project")
                        .setParameter("project", curProject)
                        .getResultList();

                for (HIBase base : projectEntities) {
                    if (!(base instanceof HIURL) && !(base instanceof HILightTable)) {
                        int langIndex = -1;
                        for (int i = 0; i < base.getMetadata().size(); i++) {
                            if (base.getMetadata().get(i).getLanguage().equalsIgnoreCase(projLang.getLanguageId())) {
                                langIndex = i;
                            }
                        }
                        if (langIndex >= 0) {
                            HIFlexMetadataRecord record = base.getMetadata().get(langIndex);
                            base.getMetadata().remove(langIndex);
                            em.persist(base);
                            em.remove(record);
                            // update lucene index
                            indexer.storeElement(base, curProject); // update lucene index
                        }
                    }
                }
                em.flush();
            } catch (NoResultException e) {
                // no entities in project
            }

            // remove language from project languages
            curProject.getLanguages().remove(projLangIndex);
            em.persist(curProject);
            em.flush();
            em.remove(projLang);
            em.flush();

            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized boolean adminAddTemplateToProject(
            @WebParam(name = "template") HIFlexMetadataTemplate template)
            throws HIServiceException, HIMaintenanceModeException {

        
        if (getState() != WSstates.PROJECT_SELECTED) {
            throw new HIServiceException("You need to select a project first!");
        }

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check input
        if (template == null) {
            throw new HIParameterException("Required parameter template missing!");
        }
        if (template.getEntries() == null) {
            throw new HIParameterException("Required parameter template.entries missing!");
        }

        // check is template already belongs to project
        for (HIFlexMetadataTemplate projTemplate : curProject.getTemplates()) {
            if (projTemplate.getNamespacePrefix().equalsIgnoreCase(template.getNamespacePrefix())) {
                return false;
            }
        }

        // sanitize user input and add it to current project
        template.setId(0);
        template.setProject(curProject);

        List<HIFlexMetadataSet> entries = template.getEntries();
        template.setEntries(new ArrayList<HIFlexMetadataSet>());

        ArrayList<String> tags = new ArrayList<String>();
        // imprt entries, check for duplicate tag names
        for (HIFlexMetadataSet set : entries) {
            set.setId(0);
            set.setTemplate(template);
            if (set.getTagname() == null) {
                throw new HIParameterException("Required parameter tag name missing!");
            }

            // check for duplicates
            for (String tag : tags) {
                if (tag.compareTo(set.getTagname()) == 0) {
                    throw new HIEntityException("Duplicate tag name not allowed!");
                }
            }
            tags.add(set.getTagname());

            if (set.getDisplayNames() == null) {
                set.setDisplayNames(new ArrayList<HIFlexMetadataName>());
            } else {
                for (HIFlexMetadataName name : set.getDisplayNames()) {
                    name.setId(0);
                    name.setSet(set);
                }
            }
        }

        curProject.getTemplates().add(template);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(template);
            em.persist(curProject);
            em.flush();

            // add the sets back in
            for (HIFlexMetadataSet set : entries) {
                em.persist(set);
                em.flush();
                template.addEntry(set);
                em.persist(template);
                em.flush();
            }
            // update sort order property
            template.getSortOrder();
            em.persist(template);
            em.persist(curProject);
            em.flush();

            // add template to all objects in project
            try {
                List<HIObject> objects = em.createQuery("SELECT o FROM HIObject o WHERE o.project=:project")
                        .setParameter("project", curProject)
                        .getResultList();

                HIFlexMetadataRecord record = null;
                for (HIObject object : objects) {
                    for (HILanguage lang : curProject.getLanguages()) {
                        record = MetadataHelper.getDefaultMetadataRecord(object, lang);
                        boolean recordFound = true;
                        if (record == null) {
                            m_logger.info("Object MD record not found while adding template!");
                            recordFound = false;
                            record = new HIFlexMetadataRecord(lang.getLanguageId(), object);
                            record.setOwner(object);
                        }
                        for (HIFlexMetadataSet set : template.getEntries()) {
                            record.addEntry(new HIKeyValue(template.getNamespacePrefix() + "." + set.getTagname(), ""));
                        }
                        if (!recordFound) {
                            object.getMetadata().add(record);
                        }
                    }
                    em.persist(record);
                    em.persist(object);
                    em.flush();
                }

            } catch (NoResultException e) {
                // no objects in project
            }
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @WebMethod
    public synchronized boolean adminUpdateTemplate(
            @WebParam(name = "templateID") long templateID,
            @WebParam(name = "templateURI") String templateURI,
            @WebParam(name = "templateURL") String templateURL)
            throws HIMaintenanceModeException, HIEntityNotFoundException, HIPrivilegeException, HIParameterException {

        
        checkParam(templateURI, true);
        checkParam(templateURL, true);

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // find template
        HIFlexMetadataTemplate template = em.find(HIFlexMetadataTemplate.class, templateID);
        if (template == null) {
            throw new HIEntityNotFoundException("Template not found!");
        }
        if (!template.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Template does not belong to project!");
        }

        template.setNamespaceURI(templateURI);
        template.setNamespaceURL(templateURL);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(template);
            em.persist(curProject);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @WebMethod
    public synchronized boolean adminUpdateTemplateSortOrder(
            @WebParam(name = "templateID") long templateID,
            @WebParam(name = "sortOrder") String sortOrder)
            throws HIMaintenanceModeException, HIEntityNotFoundException, HIPrivilegeException, HIParameterException {

        
        checkParam(sortOrder, true);

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // find template
        HIFlexMetadataTemplate template = em.find(HIFlexMetadataTemplate.class, templateID);
        if (template == null) {
            throw new HIEntityNotFoundException("Template not found!");
        }
        if (!template.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Template does not belong to project!");
        }

        template.setSortOrder(sortOrder);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(template);
            em.persist(curProject);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @WebMethod
    public synchronized boolean adminRemoveTemplateFromProject(
            @WebParam(name = "templateID") long templateID)
            throws HIMaintenanceModeException, HIEntityNotFoundException, HIPrivilegeException, HIParameterException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // find template
        HIFlexMetadataTemplate template = em.find(HIFlexMetadataTemplate.class, templateID);
        if (template == null) {
            throw new HIEntityNotFoundException("Template not found!");
        }
        if (!template.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Template does not belong to project!");
        }

        // remove template from all objects in this project
        try {
            List<HIObject> objects = em.createQuery("SELECT o FROM HIObject o WHERE o.project=:project").setParameter("project", curProject).getResultList();

            utx.begin();
            em.joinTransaction();
            Vector<HIKeyValue> kvsToDelete = new Vector<HIKeyValue>();
            for (HIObject object : objects) {
                for (HIFlexMetadataRecord rec : object.getMetadata()) {
                    kvsToDelete.clear();
                    for (HIKeyValue kv : rec.getContents()) {
                        if (kv.getKey().startsWith(template.getNamespacePrefix() + ".")) {
                            kvsToDelete.addElement(kv);
                        }
                    }
                    for (HIKeyValue kvToDelete : kvsToDelete) {
                        rec.getContents().remove(kvToDelete);
                        em.remove(kvToDelete);
                        em.persist(rec);
                        em.persist(object);
                        em.flush();
                    }
                }

                em.persist(object);
                em.flush();

                // update lucene index
                indexer.storeElement(object, curProject);
            }

            // remove template from project
            if (!curProject.getTemplates().remove(template)) {
                m_logger.info("Warning: Could not remove template from project!");
            }
            em.remove(template);
            em.persist(curProject);
            em.flush();
            utx.commit();

        } catch (NoResultException e) {
            // no objects in project
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException | NotSupportedException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @WebMethod
    public synchronized HIFlexMetadataSet adminAddSetToTemplate(
            @WebParam(name = "templateID") long templateID,
            @WebParam(name = "tagName") String tagName,
            @WebParam(name = "isRichText") boolean isRichText)
            throws HIMaintenanceModeException, HIEntityNotFoundException, HIEntityException, HIPrivilegeException, HIParameterException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check tag name
        checkParam(tagName, false);
        if (!tagName.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
            throw new HIParameterException("Tag name must conform to regular expression: \"[a-zA-Z][a-zA-Z0-9_]*\"!");
        }

        // find template
        HIFlexMetadataTemplate template = em.find(HIFlexMetadataTemplate.class, templateID);
        if (template == null) {
            throw new HIEntityNotFoundException("Template not found!");
        }
        if (!template.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Template does not belong to project!");
        }

        // check for duplicate keys
        for (HIFlexMetadataSet set : template.getEntries()) {
            if (set.getTagname().compareTo(tagName) == 0) {
                throw new HIEntityException("Key tag name already exists in template!");
            }
        }

        HIFlexMetadataSet newSet = new HIFlexMetadataSet(tagName, template, isRichText);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(newSet);
            em.flush();
            template.addEntry(newSet);
            // update sort order property
            template.getSortOrder();
            em.persist(template);
            em.persist(curProject);
            em.flush();

            // add new key-value-set to all objects in current project
            try {
                List<HIObject> objects = em.createQuery("SELECT o FROM HIObject o WHERE o.project=:project").setParameter("project", curProject).getResultList();

                for (HIObject object : objects) {
                    HIFlexMetadataRecord record = null;
                    for (HILanguage lang : curProject.getLanguages()) {
                        record = MetadataHelper.getDefaultMetadataRecord(object, lang);
                        if (record != null) {
                            record.addEntry(new HIKeyValue(template.getNamespacePrefix() + "." + newSet.getTagname(), ""));
                            em.persist(record);
                            em.flush();
                        }
                    }
                    em.persist(object);
                    em.flush();
                }

            } catch (NoResultException e) {
                // no objects in project
            }
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return newSet;
    }

    @WebMethod
    public synchronized HIFlexMetadataName adminCreateSetDisplayName(
            @WebParam(name = "setID") long setID,
            @WebParam(name = "languageID") String languageID,
            @WebParam(name = "displayName") String displayName)
            throws HIMaintenanceModeException, HIParameterException, HIEntityNotFoundException, HIPrivilegeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check parameters
        checkParam(languageID, false);
        checkParam(displayName, true);

        // find set
        HIFlexMetadataSet set = null;
        for (HIFlexMetadataTemplate template : curProject.getTemplates()) {
            for (HIFlexMetadataSet projSet : template.getEntries()) {
                if (projSet.getId() == setID) {
                    set = projSet;
                }
            }
        }
        if (set == null) {
            throw new HIEntityNotFoundException("Set does not exist in current project!");
        }

        // check if name already exists
        HIFlexMetadataName newName = null;
        for (HIFlexMetadataName setName : set.getDisplayNames()) {
            if (setName.getLanguage().compareTo(languageID) == 0) {
                newName = setName;
            }
        }
        try {
            utx.begin();
            em.joinTransaction();
            if (newName == null) {
                // create new display name
                newName = new HIFlexMetadataName(languageID, displayName, set);
                em.persist(newName);
                em.flush();
                set.getDisplayNames().add(newName);
                em.persist(set);
                em.persist(set.getTemplate());
                em.persist(curProject);
                em.flush();
            } else {
                // update set display name for given language
                newName.setDisplayName(displayName);
                em.persist(newName);
                em.persist(set);
                em.persist(set.getTemplate());
                em.persist(curProject);
                em.flush();
            }
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return newName;
    }

    @WebMethod
    public synchronized void adminUpdateSetDisplayName(
            @WebParam(name = "setID") long setID,
            @WebParam(name = "languageID") String languageID,
            @WebParam(name = "displayName") String displayName)
            throws HIMaintenanceModeException, HIParameterException, HIEntityNotFoundException, HIPrivilegeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check parameters
        checkParam(languageID, false);
        checkParam(displayName, true);

        // find set
        HIFlexMetadataSet set = null;
        for (HIFlexMetadataTemplate template : curProject.getTemplates()) {
            for (HIFlexMetadataSet projSet : template.getEntries()) {
                if (projSet.getId() == setID) {
                    set = projSet;
                }
            }
        }
        if (set == null) {
            throw new HIEntityNotFoundException("Set does not exist in current project!");
        }

        // check if name exists
        HIFlexMetadataName newName = null;
        for (HIFlexMetadataName setName : set.getDisplayNames()) {
            if (setName.getLanguage().compareTo(languageID) == 0) {
                newName = setName;
            }
        }
        if (newName == null) {
            throw new HIEntityNotFoundException("Display name not found in specified set!");
        }

        // update set display name for given language
        newName.setDisplayName(displayName);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(newName);
            em.persist(set);
            em.persist(set.getTemplate());
            em.persist(curProject);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @WebMethod
    public synchronized boolean adminRemoveSetFromTemplate(
            @WebParam(name = "templateID") long templateID,
            @WebParam(name = "setID") long setID)
            throws HIMaintenanceModeException, HIParameterException, HIPrivilegeException, HIEntityNotFoundException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // find template
        HIFlexMetadataTemplate template = em.find(HIFlexMetadataTemplate.class, templateID);
        if (template == null) {
            throw new HIEntityNotFoundException("Template not found!");
        }
        if (!template.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Template does not belong to project!");
        }

        // find set
        HIFlexMetadataSet set = null;
        for (HIFlexMetadataSet tempSet : template.getEntries()) {
            if (tempSet.getId() == setID) {
                set = tempSet;
            }
        }
        if (set == null) {
            throw new HIEntityNotFoundException("Set not found in specified template!");
        }

        // remove key-value from all objects in current project
        try {
            List<HIObject> objects = em.createQuery("SELECT o FROM HIObject o WHERE o.project=:project").setParameter("project", curProject).getResultList();

            utx.begin();
            em.joinTransaction();
            Vector<HIKeyValue> kvsToDelete = new Vector<HIKeyValue>();
            for (HIObject object : objects) {
                for (HIFlexMetadataRecord rec : object.getMetadata()) {
                    kvsToDelete.clear();
                    for (HIKeyValue kv : rec.getContents()) {
                        if (kv.getKey().compareTo(template.getNamespacePrefix() + "." + set.getTagname()) == 0) {
                            kvsToDelete.addElement(kv);
                        }
                    }
                    for (HIKeyValue kvToDelete : kvsToDelete) {
                        rec.getContents().remove(kvToDelete);
                        em.remove(kvToDelete);
                        em.persist(rec);
                        em.persist(object);
                        em.flush();
                    }
                }

                em.persist(object);
                em.flush();

                // update lucene index
                indexer.storeElement(object, curProject);
            }
            // remove set from template and project
            template.removeEntry(set);
            template.getSortOrder();
            em.remove(set);
            em.flush();
            em.persist(template);
            em.persist(curProject);
            em.flush();
            utx.commit();

        } catch (NoResultException e) {
            // no objects in project
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @WebMethod
    public synchronized HIProject getProject() throws HIMaintenanceModeException {

        
       EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        if (curProject.getStartObject() != null) {
            curProject.setStartObjectInfo(createContentQuickInfo(curProject.getStartObject()));
        }
        curProject.setUsed(storageManager.getUsedSpaceProject(curProject));

        return curProject;
    }

    @WebMethod
    public synchronized boolean updateProjectMetadata(
            @WebParam(name = "languageID") String languageID,
            @WebParam(name = "title") String title)
            throws HIPrivilegeException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        for (HIProjectMetadata metadata : curProject.getMetadata()) {
            if (metadata.getLanguageID().compareTo(languageID) == 0) {
                metadata.setTitle(title);
                try {
                    utx.begin();
                    em.joinTransaction();
                    em.persist(metadata);
                    em.flush();
                    em.persist(curProject);
                    em.flush();
                    utx.commit();
                } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                    Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
                return true;
            }
        }

        return false;
    }

    @WebMethod
    public synchronized boolean updateProjectStartElement(
            @WebParam(name = "baseID") long baseID)
            throws HIPrivilegeException, HIMaintenanceModeException, HIEntityNotFoundException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIBase base = em.find(HIBase.class, baseID);
        if (base == null) {
            throw new HIEntityNotFoundException("Element not found!");
        }
        if (!base.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Element does not belong to project!");
        }

        curProject.setStartObject(base);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(curProject);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @WebMethod
    public synchronized boolean updateProjectDefaultLanguage(
            @WebParam(name = "language") String language)
            throws HIPrivilegeException, HIMaintenanceModeException, HIEntityNotFoundException, HIParameterException {

        
        checkParam(language, false);

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        // check if language is already the project�s default language
        if (language.equalsIgnoreCase(curProject.getDefaultLanguage().getLanguageId())) {
            return false;
        }

        // check if new language is a project language
        HILanguage newLang = null;
        for (HILanguage lang : curProject.getLanguages()) {
            if (lang.getLanguageId().equalsIgnoreCase(language)) {
                newLang = lang;
            }
        }
        if (newLang == null) {
            return false;
        }

        curProject.setDefaultLanguage(newLang);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(curProject);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @WebMethod
    public synchronized HIPreference createPreference(
            @WebParam(name = "key") String key,
            @WebParam(name = "value") String value)
            throws HIParameterException, HIMaintenanceModeException {

        
        checkParam(key, false);
        checkParam(value, true);
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check if pref key already exists
        HIPreference newPref = null;
        for (HIPreference pref : curProject.getPreferences()) {
            if (pref.getKey().compareTo(key) == 0) {
                newPref = pref;
            }
        }

        if (newPref == null) {
            newPref = new HIPreference(key, value, curProject);
            try {
                utx.begin();
                em.joinTransaction();
                em.persist(newPref);
                em.persist(curProject);
                em.flush();
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return newPref;
    }

    @WebMethod
    public synchronized boolean updatePreference(
            @WebParam(name = "prefID") long prefID,
            @WebParam(name = "value") String value)
            throws HIParameterException, HIEntityNotFoundException, HIPrivilegeException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        HIPreference pref = em.find(HIPreference.class, prefID);

        if (pref == null) {
            throw new HIEntityNotFoundException("Preference not found!");
        }
        if (!pref.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Preference does not belong to project!");
        }

        pref.setValue(value);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(pref);
            em.persist(curProject);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @WebMethod
    public synchronized boolean deletePreference(
            @WebParam(name = "prefID") long prefID)
            throws HIPrivilegeException, HIEntityException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        HIPreference pref = em.find(HIPreference.class, prefID);

        if (pref == null) {
            throw new HIEntityNotFoundException("Preference not found!");
        }
        if (!pref.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Preference does not belong to project!");
        }
        // can�t delete group sort order storage
        if (pref.getKey().compareTo("groupSortOrder") == 0) {
            throw new HIEntityException("Cannot delete group sort order property!");
        }

        curProject.getPreferences().remove(pref);
        try {
            utx.begin();
            em.joinTransaction();
            em.remove(pref);
            em.persist(curProject);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @WebMethod
    public synchronized HIGroup getImportGroup() throws HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        importGroup = em.find(HIGroup.class, importGroup.getId());
                
        // legacy update: generate UUID for element if missing
        if (importGroup.getUUID() == null) {
            try {
                utx.begin();
                em.joinTransaction();
                importGroup.setUUID(UUID.randomUUID().toString());
                em.persist(importGroup);
                em.flush();
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return importGroup;
    }

    @WebMethod
    public synchronized HIGroup getTrashGroup() throws HIMaintenanceModeException {
        
        EntityManager em = emf.createEntityManager();
        trashGroup = em.find(HIGroup.class, trashGroup.getId());

        // legacy update: generate UUID for element if missing
        if (trashGroup.getUUID() == null) {
            try {
                utx.begin();
                em.joinTransaction();
                trashGroup.setUUID(UUID.randomUUID().toString());
                em.persist(trashGroup);
                em.flush();
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return trashGroup;
    }

    @WebMethod
    public synchronized boolean deleteFromProject(
            @WebParam(name = "baseID") long baseID) throws HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        trashGroup = em.find(HIGroup.class, trashGroup.getId());

        HIBase base = em.find(HIBase.class, baseID);

        if (base == null) {
            return false;
        }
        if (!base.getProject().equals(curProject)) {
            return false;
        }

        // only delete elements in the trash
        if (!trashGroup.contains(base)) {
            return false;
        }

        removeFromGroup(base, trashGroup);
        removeLinks(base);

        // delete bitstream(s)
        if (base instanceof HIObject) {
            HIObject object = (HIObject) base;
            for (HIObjectContent content : object.getViews()) {
                if (content instanceof HIView) {
                    HIView view = (HIView) content;
                    storageManager.removeView(view);
                }
            }
        }

        indexer.removeElement(base, curProject); // update lucene index
        try {
            utx.begin();
            em.joinTransaction();
            // update project start element if necessary
            if (curProject.getStartObject() != null && curProject.getStartObject().getId() == base.getId()) {
                curProject.setStartObject(null);
                em.persist(curProject);
                em.flush();
            }

            // delete default view
            if (base instanceof HIObject) {
                HIObject object = (HIObject) base;
                object.setDefaultView(null);
                em.persist(object);
                em.flush();
            }
            
            // update timestamps
            if ( base instanceof HIView ) {
                HIObject object = (HIObject) ((HIView)base).getObject();
                object.touchTimestamp();
                em.persist(object);
                em.flush();
            }
            if ( base instanceof HIInscription ) {
                HIObject object = (HIObject) ((HIInscription)base).getObject();
                object.touchTimestamp();
                em.persist(object);
                em.flush();
            }
            
            em.remove(base);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    // -- SYSOP METHOD --
        @WebMethod
    public synchronized void sysopUpdateProjectQuota(
            @WebParam(name = "projectID") long projectID,
            @WebParam(name = "quota") long quota)
            throws HIPrivilegeException, HIEntityNotFoundException, HIParameterException, HIMaintenanceModeException {
        
        EntityManager em = emf.createEntityManager();
        quota = Math.abs(quota); // don't allow negative quota
        
        HIProject project = em.find(HIProject.class, projectID);
        if (project == null)
            throw new HIEntityNotFoundException("Project not found!");
        
         try {
            utx.begin();
            em.joinTransaction();
            project.setQuota(quota);
            em.persist(project);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    
    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized boolean sysopDeleteProject(
            @WebParam(name = "projectID") long projectID)
            throws HIPrivilegeException, HIEntityNotFoundException, HIParameterException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        if ( curProject != null ) curProject = em.find(HIProject.class, curProject.getId());

        HIProject project = em.find(HIProject.class, projectID);
        if (project == null)
            throw new HIEntityNotFoundException("Project not found!");

        try {
            utx.begin();
            em.joinTransaction();
            // remove start element
            project.setStartObject(null);
            em.persist(project);
            em.flush();

            // remove all repsitories
            List<HIRepository> repositories = em.createQuery("SELECT rep FROM HIRepository rep WHERE rep.project=:project")
                    .setParameter("project", project)
                    .getResultList();

            for (HIRepository repos : repositories) {
                em.remove(repos);
                em.flush();
            }

            // remove all layer links
            List<HILayer> layers = em.createQuery("SELECT l FROM HILayer l WHERE l.view.project=:project AND l.link is NOT NULL")
                    .setParameter("project", project)
                    .getResultList();
            for (HILayer layer : layers) {
                layer.setLink(null);
                em.persist(layer);
                em.flush();
            }

            // find project groups
            List<HIGroup> groups = em.createQuery("SELECT g FROM HIGroup g WHERE g.project=:project")
                    .setParameter("project", project)
                    .getResultList();

            // remove all project groups
            for (HIGroup group : groups) {
                indexer.removeElement(group, project); // update lucene index

                em.remove(group);
                em.flush();
            }

            // find all project elements
            List<HIBase> elements = em.createQuery("SELECT b FROM HIBase b WHERE b.project=:project")
                    .setParameter("project", project)
                    .getResultList();

            // delete all project elements
            for (HIBase base : elements) {
                // delete bitstream(s)
                if (base instanceof HIObject) {
                    HIObject object = (HIObject) base;
                    object.setDefaultView(null);
                    em.persist(object);
                    em.flush();
                    for (HIObjectContent content : object.getViews()) {
                        if (content instanceof HIView) {
                            HIView view = (HIView) content;
                            storageManager.removeView(view);
                        }
                    }

                }

                indexer.removeElement(base, project); // update lucene index

                em.remove(base);
                em.flush();
            }

            // find all project users
            String query = "SELECT r FROM HIRole r WHERE r.project=:project";
            List<HIRole> roles = (List<HIRole>) em.createQuery(query)
                    .setParameter("project", project)
                    .getResultList();
            // delete all project users
            for (HIRole role : roles) {
                em.remove(role);
                em.flush();
            }

            // check if deleted project was the current project
            if (curProject != null && project.getId() == curProject.getId()) {
                curProject = null;
                state = WSstates.AUTHENTICATED;
            }

            // remove the project
            em.remove(project);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    //-------------------------
    // Element Retrieval
    //-------------------------
    
    
    @WebMethod
    public synchronized List<HIText> getProjectTextElements()
            throws HIMaintenanceModeException, HIPrivilegeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        List<HIText> texts = em.createQuery("SELECT t FROM HIText t WHERE t.project=:project")
                .setParameter("project", curProject)
                .getResultList();        

        return texts;
    }


    @WebMethod
    public synchronized List<HILightTable> getProjectLightTableElements()
            throws HIMaintenanceModeException, HIPrivilegeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        List<HILightTable> lighttables = em.createQuery("SELECT l FROM HILightTable l WHERE l.project=:project")
                .setParameter("project", curProject)
                .getResultList();        

        return lighttables;
    }
    
    @WebMethod
    public synchronized HIBase getBaseElementByUUID(@WebParam(name = "uuid") String uuid)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException, HIParameterException {

        checkParam(uuid, false);
        if ( !isValidUUID(uuid) ) throw new HIParameterException("Invalid UUID format!");
        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIBase base = null;
        try {
            base = (HIBase) em.createQuery("SELECT base FROM HIBase base WHERE base.project=:project AND base.uuid=:uuid")
                .setParameter("project", curProject)
                .setParameter("uuid", uuid)
                .getSingleResult();
        } catch (NoResultException e) { base = null; }

        if (base == null) throw new HIEntityNotFoundException("Element not found!");

        em.refresh(base);
        if (!base.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Element does not belong to project!");
        }

        // attach layer quick info
        if (base instanceof HILayer) {
            if (((HILayer) base).getLink() != null) {
                ((HILayer) base).setLinkInfo(createContentQuickInfo(((HILayer) base).getLink()));
            }
        } else if (base instanceof HIView) {
            for (HILayer layer : ((HIView) base).getLayers()) {
                if (layer.getLink() != null) {
                    layer.setLinkInfo(createContentQuickInfo(layer.getLink()));
                }
            }
        } else if (base instanceof HIObject) {
            for (HIObjectContent content : ((HIObject) base).getViews()) {
                if (content instanceof HIView) {
                    for (HILayer layer : ((HIView) content).getLayers()) {
                        if (layer.getLink() != null) {
                            layer.setLinkInfo(createContentQuickInfo(layer.getLink()));
                        }
                    }
                }
            }
        }

        // bugfix for multiple metadata records per language
        if (base instanceof HIObject && base.getMetadata().size() > curProject.getLanguages().size()) {
            m_logger.info("Fixing erroneously created additional metadata records for Object ID" + base.getId());
            HIObject object = (HIObject) base;
            Vector<HIFlexMetadataRecord> extraRecords = new Vector<HIFlexMetadataRecord>();
            try {
                utx.begin();
                em.joinTransaction();
                // remove erroneously created additional metadata records
                for (HILanguage lang : curProject.getLanguages()) {
                    HIFlexMetadataRecord rec = MetadataHelper.getDefaultMetadataRecord(base, lang);
                    for (HIFlexMetadataRecord extraRecord : object.getMetadata()) {
                        if (extraRecord.getLanguage().compareTo(lang.getLanguageId()) == 0 && extraRecord.getId() != rec.getId()) {
                            extraRecords.addElement(extraRecord); // add to vector for later deletion

                            // merge metadata with default record
                            for (HIKeyValue extraKV : extraRecord.getContents()) {
                                HIKeyValue foundKV = null;
                                for (HIKeyValue kv : rec.getContents()) {
                                    if (kv.getKey().compareTo(extraKV.getKey()) == 0) {
                                        foundKV = kv;
                                    }
                                }

                                if (foundKV != null) {
                                    // merge info
                                    if (extraKV.getValue().length() > 0) {
                                        foundKV.setValue(foundKV.getValue() + " " + extraKV.getValue());
                                        em.persist(foundKV);
                                        em.persist(rec);
                                        em.persist(object);
                                        em.flush();
                                    }
                                } else {
                                    // add info
                                    rec.addEntry(new HIKeyValue(extraKV.getKey(), extraKV.getValue()));
                                    em.persist(rec);
                                    em.persist(object);
                                    em.flush();
                                }
                            }
                        }
                    }
                }
                // remove extra metadata records if any
                for (HIFlexMetadataRecord extraRecord : extraRecords) {
                    object.getMetadata().remove(extraRecord);
                    em.remove(extraRecord);
                    em.persist(object);
                    em.flush();
                }
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }

            // update lucene index
            indexer.storeElement(base, curProject);
        }

        return base;
    }
    
    
    @WebMethod
    public synchronized HIBase getBaseElement(
            @WebParam(name = "baseID") long baseID)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIBase base = null;

        base = em.find(HIBase.class, baseID);
        if (base == null) {
            throw new HIEntityNotFoundException("Element not found!");
        }
        em.refresh(base);
        if (!base.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Element does not belong to project!");
        }
        
        // legacy update: generate UUID for element if missing
        boolean needsUUIDUpgrade = false;
        if ( base.getUUID() == null ) needsUUIDUpgrade = true;
        if ( base instanceof HIObject ) {
            for (HIObjectContent content : ((HIObject)base).getViews() ) {
                if ( content.getUUID() == null ) needsUUIDUpgrade = true;
                if ( content instanceof HIView) {
                    for (HILayer layer : ((HIView)content).getLayers())
                        if ( layer.getUUID() == null ) needsUUIDUpgrade = true;
                }
            }
        }
        if ( base instanceof HIView ) {
            for (HILayer layer : ((HIView)base).getLayers())
                if ( layer.getUUID() == null ) needsUUIDUpgrade = true;
        }
        if ( needsUUIDUpgrade ) {
            try {
                utx.begin();
                em.joinTransaction();
                if ( base instanceof HIView) {
                    if ( base.getUUID() == null ) base.setUUID(UUID.nameUUIDFromBytes(storageManager.getOriginal((HIView)base)).toString());
                    for ( HILayer layer: ((HIView)base).getLayers() ) {
                        if ( layer.getUUID() == null ) layer.setUUID(UUID.randomUUID().toString());
                    }
                } else if ( base instanceof HIObject ) {
                    if ( base.getUUID() == null ) base.setUUID(UUID.randomUUID().toString());
                    for (HIObjectContent content : ((HIObject)base).getViews()) {
                        if ( content instanceof HIView) {
                        if ( content.getUUID() == null ) content.setUUID(UUID.nameUUIDFromBytes(storageManager.getOriginal((HIView)content)).toString());
                            for ( HILayer layer: ((HIView)content).getLayers() ) {
                            if ( layer.getUUID() == null ) layer.setUUID(UUID.randomUUID().toString());
                            }
                        } else if ( content.getUUID() == null ) content.setUUID(UUID.randomUUID().toString());
                    }
                } else if ( base.getUUID() == null ) base.setUUID(UUID.randomUUID().toString());

                em.persist(base);
                em.flush();
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // attach layer quick info
        if (base instanceof HILayer) {
            if (((HILayer) base).getLink() != null) {
                ((HILayer) base).setLinkInfo(createContentQuickInfo(((HILayer) base).getLink()));
            }
        } else if (base instanceof HIView) {
            for (HILayer layer : ((HIView) base).getLayers()) {
                if (layer.getLink() != null) {
                    layer.setLinkInfo(createContentQuickInfo(layer.getLink()));
                }
            }
        } else if (base instanceof HIObject) {
            for (HIObjectContent content : ((HIObject) base).getViews()) {
                if (content instanceof HIView) {
                    for (HILayer layer : ((HIView) content).getLayers()) {
                        if (layer.getLink() != null) {
                            layer.setLinkInfo(createContentQuickInfo(layer.getLink()));
                        }
                    }
                }
            }
        }

        // bugfix for multiple metadata records per language
        if (base instanceof HIObject && base.getMetadata().size() > curProject.getLanguages().size()) {
            m_logger.info("Fixing erroneously created additional metadata records for Object ID" + base.getId());
            HIObject object = (HIObject) base;
            Vector<HIFlexMetadataRecord> extraRecords = new Vector<HIFlexMetadataRecord>();
            try {
                utx.begin();
                em.joinTransaction();
                // remove erroneously created additional metadata records
                for (HILanguage lang : curProject.getLanguages()) {
                    HIFlexMetadataRecord rec = MetadataHelper.getDefaultMetadataRecord(base, lang);
                    for (HIFlexMetadataRecord extraRecord : object.getMetadata()) {
                        if (extraRecord.getLanguage().compareTo(lang.getLanguageId()) == 0 && extraRecord.getId() != rec.getId()) {
                            extraRecords.addElement(extraRecord); // add to vector for later deletion

                            // merge metadata with default record
                            for (HIKeyValue extraKV : extraRecord.getContents()) {
                                HIKeyValue foundKV = null;
                                for (HIKeyValue kv : rec.getContents()) {
                                    if (kv.getKey().compareTo(extraKV.getKey()) == 0) {
                                        foundKV = kv;
                                    }
                                }

                                if (foundKV != null) {
                                    // merge info
                                    if (extraKV.getValue().length() > 0) {
                                        foundKV.setValue(foundKV.getValue() + " " + extraKV.getValue());
                                        em.persist(foundKV);
                                        em.persist(rec);
                                        em.persist(object);
                                        em.flush();
                                    }
                                } else {
                                    // add info
                                    rec.addEntry(new HIKeyValue(extraKV.getKey(), extraKV.getValue()));
                                    em.persist(rec);
                                    em.persist(object);
                                    em.flush();
                                }
                            }
                        }
                    }
                }
                // remove extra metadata records if any
                for (HIFlexMetadataRecord extraRecord : extraRecords) {
                    object.getMetadata().remove(extraRecord);
                    em.remove(extraRecord);
                    em.persist(object);
                    em.flush();
                }
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }

            // update lucene index
            indexer.storeElement(base, curProject);
        }

        return base;
    }

    //-------------------------
    // Object Methods
    //-------------------------
    @WebMethod
    public synchronized HIObject createObject(@WebParam(name = "uuid") String uuid) throws HIMaintenanceModeException, HIParameterException, HIEntityException {

        if ( uuid != null && !isValidUUID(uuid) ) throw new HIParameterException("Invalid UUID format!");        

        HIObject object = new HIObject(uuid);
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check if UUID already exists in project
        if ( uuid != null ) {
            if ( (Long)em.createQuery("SELECT count(b) FROM HIBase b WHERE b.project=:project AND b.uuid=:uuid")
                .setParameter("project", curProject)
                .setParameter("uuid", uuid)
                .getSingleResult() != 0 ) throw new HIEntityException("Element with UUID already exists in project!");
        }
                
        object.setProject(curProject);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(object);
            em.flush();
            attachMetadataRecords(em, object);
            em.persist(object);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        addToImportGroup(object);

        indexer.storeElement(object, curProject); // update lucene index

        return object;
    }

    @WebMethod
    public synchronized void updateObjectSortOrder(
            @WebParam(name = "objectID") long objectID,
            @WebParam(name = "sortOrder") String sortOrder)
            throws HIEntityNotFoundException, HIPrivilegeException, HIParameterException, HIMaintenanceModeException {

        
        HIObject object = null;
        checkParam(sortOrder, true);

        EntityManager em = emf.createEntityManager();
        object = em.find(HIObject.class, objectID);
        if (object == null) {
            throw new HIEntityNotFoundException("Object not found!");
        }
        if (!object.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Object does not belong to project!");
        }

        // update sort order
        object.setSortOrder(sortOrder);
        object.touchTimestamp();
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(object);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //-------------------------
    // View Methods
    //-------------------------
    @WebMethod
    public synchronized HIView createView(
            @WebParam(name = "objectID") long objectID,
            @WebParam(name = "filename") String filename,
            @WebParam(name = "repositoryIDString") String repositoryIDString,
            @WebParam(name = "data") byte[] data,
            @WebParam(name = "uuid") String uuid)
            throws HIParameterException, HIEntityNotFoundException, HIPrivilegeException, HIEntityException, HIMaintenanceModeException {

        if ( uuid != null && !isValidUUID(uuid) ) throw new HIParameterException("Invalid UUID format!");

        HIView view = null;
        if (wsContext != null)
            wsContext.getMessageContext().put(com.sun.xml.ws.developer.JAXWSProperties.MTOM_THRESHOLOD_VALUE, 0);

        checkParam(filename, false);
        checkParam(repositoryIDString, true);
        if (data == null) {
            return null;
        }

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        curProject.setUsed(storageManager.getUsedSpaceProject(curProject));
        
        // check if project has quota and new image exceeds quota
        if ( curProject.getQuota() > 0 && (curProject.getUsed()+data.length) > curProject.getQuota() )
            throw new HIPrivilegeException(("Quota exceeded for project! Max storage: "+curProject.getQuota()+" - Used: "+curProject.getUsed()));

        HIObject object = em.find(HIObject.class, objectID);
        if (object == null) {
            throw new HIEntityNotFoundException("Object not found!");
        }
        if (!object.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Object does not belong to project!");
        }

        String hash = getMD5HashString(data);
        long count = (Long) em.createQuery("SELECT COUNT(v) FROM HIView v WHERE v.hash=:hash and v.project=:project")
                .setParameter("hash", hash)
                .setParameter("project", curProject)
                .getSingleResult();

        if (count > 0) {
            throw new HIEntityException("View already in project!");
        }

        // check if UUID already exists in project
        if ( uuid != null ) {
            if ( (Long)em.createQuery("SELECT count(b) FROM HIBase b WHERE b.project=:project AND b.uuid=:uuid")
                .setParameter("project", curProject)
                .setParameter("uuid", uuid)
                .getSingleResult() != 0 ) throw new HIEntityException("Element with UUID already exists in project!");
        }
        
        // create new view
        // generate type 3 UUID from file data if none was supplied
        if ( uuid == null ) uuid = UUID.nameUUIDFromBytes(data).toString(); 
        view = new HIView(filename, hash, repositoryIDString, object, uuid);
        view.setProject(curProject);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(view);
            em.flush();
            object.addView(view);
            object.touchTimestamp();
            em.persist(object);
            em.flush();
            attachMetadataRecords(em, view);

            // store bitstream
            storageManager.storeView(view, data);
            data = null;
            em.persist(view);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        indexer.storeElement(view, curProject); // update lucene index

        return view;
    }

    @WebMethod
    public synchronized boolean setDefaultView(
            @WebParam(name = "objectID") long objectID,
            @WebParam(name = "contentID") long contentID)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIObject object = em.find(HIObject.class, objectID);
        if (object == null) {
            throw new HIEntityNotFoundException("Object not found!");
        }
        if (!object.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Object does not belong to project!");
        }

        // make sure content is valid
        HIObjectContent content = em.find(HIObjectContent.class, contentID);
        if (content == null) {
            throw new HIEntityNotFoundException("Content not found!");
        }
        if (!content.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Content does not belong to project!");
        }
        // check if content belongs to current object
        boolean found = false;
        for (HIObjectContent objContent : object.getViews()) {
            if (objContent.getId() == content.getId()) {
                content = objContent;
                found = true;
            }
        }

        if (found) {
            object.setDefaultView(content);
            try {
                utx.begin();
                em.joinTransaction();
                object.touchTimestamp();
                em.persist(object);
                em.flush();
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            return true;
        }

        return false;
    }

    @WebMethod
    public synchronized boolean updateContentOwner(
            @WebParam(name = "objectID") long objectID,
            @WebParam(name = "contentID") long contentID)
            throws HIEntityNotFoundException, HIPrivilegeException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIObject object = em.find(HIObject.class, objectID);
        if (object == null) {
            throw new HIEntityNotFoundException("Object not found!");
        }
        if (!object.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Object does not belong to project!");
        }

        // make sure content is valid
        HIObjectContent content = em.find(HIObjectContent.class, contentID);
        if (content == null) {
            throw new HIEntityNotFoundException("Content not found!");
        }
        if (!content.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Content does not belong to project!");
        }

        // check if content already belongs to current object
        boolean found = false;
        for (HIObjectContent objContent : object.getViews()) {
            if (objContent.getId() == content.getId()) {
                content = objContent;
                found = true;
            }
        }
        if (found) {
            return false;
        }

        // add content to this object, removing it from the previous object
        HIObject prevObject = content.getObject();
        prevObject = em.find(HIObject.class, prevObject.getId());
        try {
            utx.begin();
            em.joinTransaction();
            prevObject.removeView(content);
            em.persist(prevObject);
            em.flush();
            object.addView(content);
            em.persist(content);
            em.flush();
            content.setObject(object);
            em.persist(object);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @WebMethod
    public synchronized void removeDefaultView(@WebParam(name = "objectID") long objectID)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        HIObject object = em.find(HIObject.class, objectID);
        if (object == null) {
            throw new HIEntityNotFoundException("Object not found!");
        }
        if (!object.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Object does not belong to project!");
        }
        try {
            // remove default view
            utx.begin();
            em.joinTransaction();
            object.setDefaultView(null);
            em.persist(object);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @WebMethod
    public synchronized void updateViewSortOrder(
            @WebParam(name = "viewID") long viewID,
            @WebParam(name = "sortOrder") String sortOrder)
            throws HIEntityNotFoundException, HIPrivilegeException, HIParameterException, HIMaintenanceModeException {

        
        HIView view = null;
        checkParam(sortOrder, true);

        EntityManager em = emf.createEntityManager();
        view = em.find(HIView.class, viewID);
        if (view == null) {
            throw new HIEntityNotFoundException("View not found!");
        }
        if (!view.getProject().equals(curProject)) {
            throw new HIPrivilegeException("View does not belong to project!");
        }
        try {
            // update sort order
            utx.begin();
            em.joinTransaction();
            view.setSortOrder(sortOrder);
            view.touchTimestamp();
            em.persist(view);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //-------------------------
    // Bitstream Methods
    //-------------------------
    @WebMethod
    public synchronized byte[] getImage(
            @WebParam(name = "baseID") long baseID, HI_ImageSizes size)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        byte[] bitstream = null;

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIBase base = em.find(HIBase.class, baseID);
        if (base == null) {
            throw new HIEntityNotFoundException("Element not found!");
        }
        if (!base.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Element does not belong to project!");
        }

        HIView view = null;

        // if base id belonged to a view, use directly
        if (base instanceof HIView) {
            view = (HIView) base;
        } // if base was an object try to find the default view, else take the first view
        else if (base instanceof HIObject) {
            HIObject object = (HIObject) base;
            if (object.getDefaultView() != null) {
                if (object.getDefaultView() instanceof HIView) {
                    view = (HIView) object.getDefaultView();
                }
            }

            if (view == null) {
                if (object.getViews().size() > 0) {
                    if (object.getViews().get(0) instanceof HIView) {
                        view = (HIView) object.getViews().get(0);
                    }
                }
            }
        } else if (base instanceof HILayer) {
            // if base was layer find parent view and manually render layer on top of it
            view = ((HILayer) base).getView();
        }

        if (view == null) {
            return null;
        }

        if (wsContext != null) {
            wsContext.getMessageContext().put(com.sun.xml.ws.developer.JAXWSProperties.MTOM_THRESHOLOD_VALUE, 0);
        }

        if (size == HI_ImageSizes.HI_THUMBNAIL) {
            String mimetype = URLConnection.guessContentTypeFromName(view.getFilename());
            if (mimetype == null || !mimetype.startsWith("image/")) {
                // no image, nothing to retrieve --> use default icon for now
                // TODO: choose icon depending on mime type, add more icons
//				try {
//					bitstream = FileStorageManager.getBytesFromFile(getClass().getResource("/resources/no-thumbnail.png").getFile());
                return bitstream;
//				} catch (IOException e) {
                // TODO handle and replace with stable icon system
//					return bitstream;
//				}
            } else {
                if (base instanceof HILayer) {
                    // if base element was layer, render layer on top of image
                    BufferedImage previewImage = ImageHelper.convertByteArrayToBufferedImage(storageManager.getPreview(view));
                    HILayerRenderer renderLayer = new HILayerRenderer((HILayer) base, previewImage.getWidth(), previewImage.getHeight());
                    Graphics2D g2d = previewImage.createGraphics();

                    // return full view if layer has no polygons
                    if (renderLayer.getRelativePolygons().size() == 0) {
                        bitstream = storageManager.getThumbnail(((HILayer) base).getView());
                        return bitstream;
                    }

                    // render layer
                    GeneralPath combinedPath = new GeneralPath();
                    for (RelativePolygon polygon : renderLayer.getRelativePolygons()) {
                        if (polygon.isClosed()) // only add closed polygon paths
                        {
                            combinedPath.append(polygon.getPolygonPath(), false);
                        }
                    }
                    // draw inside of all closed polygons on this layer					
                    Composite orgComposite = g2d.getComposite();
                    // set default opacity as suggested by Christian Terstegge
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                    g2d.setColor(renderLayer.getSolidColour());
                    g2d.fill(combinedPath);
                    g2d.setComposite(orgComposite);
                    g2d.setStroke(HILayerRenderer.selectStrokeWhite);
                    g2d.setColor(Color.WHITE);
                    g2d.draw(combinedPath);
                    g2d.setStroke(HILayerRenderer.selectStrokeBlack);
                    g2d.setColor(Color.black);
                    g2d.draw(combinedPath);

                    // clip image to selected layer
                    int x = combinedPath.getBounds().x - 5;
                    x = Math.max(0, x);
                    int y = combinedPath.getBounds().y - 5;
                    y = Math.max(0, y);
                    int width = combinedPath.getBounds().width + 10;
                    width = Math.min(previewImage.getWidth() - x, width);
                    int height = combinedPath.getBounds().height + 10;
                    height = Math.min(previewImage.getHeight() - y, height);

                    // clip image to layer bounds
                    previewImage = previewImage.getSubimage(x, y, width, height);
                    // scale image back to thumbnail size
                    previewImage = ImageHelper.scaleImageTo(previewImage, new Dimension(128, 128)).getAsBufferedImage();

                    // convert back to bytes
                    ByteArrayOutputStream outThumbnail = new ByteArrayOutputStream();
                    JPEGEncodeParam jpegParam = new JPEGEncodeParam();
                    jpegParam.setQuality(0.8f); // set encoding quality
                    JAI.create("encode", previewImage, outThumbnail, "JPEG", jpegParam);
                    bitstream = outThumbnail.toByteArray();

                } else {
                    bitstream = storageManager.getThumbnail(view);
                }

            }
        } else if (size == HI_ImageSizes.HI_PREVIEW) {
            bitstream = storageManager.getPreview(view);
        } else if (size == HI_ImageSizes.HI_FULL) {
            bitstream = storageManager.getHiRes(view);
        } else if (size == HI_ImageSizes.HI_ORIGINAL) {
            bitstream = storageManager.getOriginal(view);
        }

        return bitstream;
    }

    
    /*
     *   Returns image scaled to custom resolution. 
     *   This image is generated on-the-fly and not stored in HIStore.
     */
    @WebMethod
    public synchronized byte[] getScaledImage(
            @WebParam(name = "baseID") long baseID, double scale)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        byte[] bitstream = null;

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIBase base = em.find(HIBase.class, baseID);
        if (base == null) {
            throw new HIEntityNotFoundException("Element not found!");
        }
        if (!base.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Element does not belong to project!");
        }

        HIView view = null;

        // if base id belonged to a view, use directly
        if (base instanceof HIView) {
            view = (HIView) base;
        } // if base was an object try to find the default view, else take the first view
        else if (base instanceof HIObject) {
            HIObject object = (HIObject) base;
            if (object.getDefaultView() != null) {
                if (object.getDefaultView() instanceof HIView) {
                    view = (HIView) object.getDefaultView();
                }
            }

            if (view == null) {
                if (object.getViews().size() > 0) {
                    if (object.getViews().get(0) instanceof HIView) {
                        view = (HIView) object.getViews().get(0);
                    }
                }
            }
        } else if (base instanceof HILayer) {
            // if base was layer find parent view and manually render layer on top of it
            view = ((HILayer) base).getView();
        }

        if (view == null) {
            return null;
        }

        if (wsContext != null) {
            wsContext.getMessageContext().put(com.sun.xml.ws.developer.JAXWSProperties.MTOM_THRESHOLOD_VALUE, 0);
        }
        
        // set scale bounds
        scale = Math.min(1.0d, scale);
        double minScale = Math.max((128d/view.getWidth()),(128d/view.getHeight()));
        scale = Math.max(scale, minScale);
        
        BufferedImage previewImage = ImageHelper.convertByteArrayToBufferedImage(storageManager.getHiRes(view));
        // scale image back to thumbnail size
        previewImage = ImageHelper.scaleImageTo(previewImage, new Dimension((int)Math.round(view.getWidth()*scale), (int)Math.round(view.getHeight()*scale))).getAsBufferedImage();

        // convert back to bytes
        ByteArrayOutputStream outImage = new ByteArrayOutputStream();
        JPEGEncodeParam jpegParam = new JPEGEncodeParam();
        jpegParam.setQuality(0.8f); // set encoding quality
        JAI.create("encode", previewImage, outImage, "JPEG", jpegParam);
        bitstream = outImage.toByteArray();


        return bitstream;
    }

    
    
    //-------------------------
    // Inscription Methods
    //-------------------------
    @WebMethod
    public synchronized HIInscription createInscription(
            @WebParam(name = "objectID") long objectID,
            @WebParam(name = "uuid") String uuid)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException, HIParameterException, HIEntityException {

        if ( uuid != null && !isValidUUID(uuid) ) throw new HIParameterException("Invalid UUID format!");

        HIInscription inscription = null;

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIObject object = em.find(HIObject.class, objectID);
        if (object == null) throw new HIEntityNotFoundException("Object not found!");
        if (!object.getProject().equals(curProject)) throw new HIPrivilegeException("Object does not belong to project!");

        // check if UUID already exists in project
        if ( uuid != null ) {
            if ( (Long)em.createQuery("SELECT count(b) FROM HIBase b WHERE b.project=:project AND b.uuid=:uuid")
                .setParameter("project", curProject)
                .setParameter("uuid", uuid)
                .getSingleResult() != 0 ) throw new HIEntityException("Element with UUID already exists in project!");
        }

        // create new inscription
        inscription = new HIInscription(object, uuid);
        inscription.setProject(curProject);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(inscription);
            em.flush();
            object.addView(inscription);
            object.touchTimestamp();
            em.persist(object);
            em.flush();
            attachMetadataRecords(em, inscription);
            em.persist(inscription);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }


        indexer.storeElement(inscription, curProject); // update lucene index

        return inscription;

    }

    //-------------------------
    // Layer Methods
    //-------------------------
    @WebMethod
    public synchronized HILayer createLayer(
            @WebParam(name = "viewID") long viewID,
            @WebParam(name = "red") int red,
            @WebParam(name = "green") int green,
            @WebParam(name = "blue") int blue,
            @WebParam(name = "opacity") float opacity,
            @WebParam(name = "uuid") String uuid
    ) throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException, HIParameterException, HIEntityException {

        if ( uuid != null && !isValidUUID(uuid) ) throw new HIParameterException("Invalid UUID format!");

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIView view = em.find(HIView.class, viewID);
        if (view == null) {
            throw new HIEntityNotFoundException("View not found!");
        }
        if (!view.getProject().equals(curProject)) {
            throw new HIPrivilegeException("View does not belong to project!");
        }

        // check if UUID already exists in project
        if ( uuid != null ) {
            if ( (Long)em.createQuery("SELECT count(b) FROM HIBase b WHERE b.project=:project AND b.uuid=:uuid")
                .setParameter("project", curProject)
                .setParameter("uuid", uuid)
                .getSingleResult() != 0 ) throw new HIEntityException("Element with UUID already exists in project!");
        }
        
        HILayer layer = new HILayer(view, red, green, blue, opacity, uuid);
        layer.setProject(curProject);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(layer);
            em.flush();
            attachMetadataRecords(em, layer);
            em.persist(layer);
            view.touchTimestamp();
            em.persist(view);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        indexer.storeElement(layer, curProject); // update lucene index

        return layer;
    }

    @WebMethod
    public synchronized boolean updateLayerProperties(
            @WebParam(name = "layerID") long layerID,
            @WebParam(name = "red") int red,
            @WebParam(name = "green") int green,
            @WebParam(name = "blue") int blue,
            @WebParam(name = "opacity") float opacity,
            @WebParam(name = "polygons") String polygons)
            throws HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HILayer layer = em.find(HILayer.class, layerID);
        if (!layer.getProject().equals(curProject)) {
            return false;
        }

        layer.setRed(red);
        layer.setGreen(green);
        layer.setBlue(blue);
        layer.setOpacity(opacity);
        layer.setPolygons(polygons);
        layer.touchTimestamp();
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(layer);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @WebMethod
    public synchronized boolean removeLayer(
            @WebParam(name = "layerID") long layerID)
            throws HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HILayer layer = em.find(HILayer.class, layerID);
        if (!layer.getProject().equals(curProject)) {
            return false;
        }

        // remove layer from groups
        for (HIGroup group : getGroups()) {
            removeFromGroup(layer, group);
        }
        removeLinks(layer);

        HIView view = layer.getView();
        layer.getView().getLayers().remove(layer);
        layer.setView(null);

        indexer.removeElement(layer, curProject); // update lucene index
        try {
            utx.begin();
            em.joinTransaction();
            view.touchTimestamp();
            em.persist(view);
            em.remove(layer);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @WebMethod
    public synchronized boolean setLayerLink(
            @WebParam(name = "layerID") long layerID,
            @WebParam(name = "linkID") long linkID)
            throws HIParameterException, HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HILayer layer = em.find(HILayer.class, layerID);
        if (layer == null) {
            throw new HIEntityNotFoundException("Layer not found!");
        }
        if (!layer.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Layer does not belong to project!");
        }

        HIBase base = em.find(HIBase.class, linkID);
        if (base == null) {
            throw new HIEntityNotFoundException("Link element not found!");
        }
        if (!base.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Link element does not belong to project!");
        }

        // check if link element is in trash --> can�t link to things in the trash
        trashGroup = em.find(HIGroup.class, trashGroup.getId());
        if (trashGroup == null) {
            throw new HIEntityNotFoundException("Mandatory project groups not found!");
        }

        if (trashGroup.contains(base)) {
            return false; // can�t link to items in trash
        }
        // can�t link to import or trash group
        importGroup = em.find(HIGroup.class, importGroup.getId());
        if (importGroup == null) {
            throw new HIEntityNotFoundException("Mandatory project groups not found!");
        }
        if (base.getId() == importGroup.getId() || base.getId() == trashGroup.getId()) {
            return false;
        }

        // set link
        layer.setLink(base);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(layer);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @WebMethod
    public synchronized boolean removeLayerLink(
            @WebParam(name = "layerID") long layerID)
            throws HIParameterException, HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HILayer layer = em.find(HILayer.class, layerID);
        if (layer == null) {
            throw new HIEntityNotFoundException("Layer not found!");
        }
        if (!layer.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Layer does not belong to project!");
        }

        // remove link
        layer.setLink(null);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(layer);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    //-------------------------
    // URL Methods
    //-------------------------
    @WebMethod
    public synchronized HIURL createURL(
            @WebParam(name = "url") String url,
            @WebParam(name = "title") String title,
            @WebParam(name = "lastAccess") String lastAccess,
            @WebParam(name = "uuid") String uuid)
            throws HIParameterException, HIMaintenanceModeException, HIEntityException {

        
        checkParam(url, true);
        checkParam(title, true);
        checkParam(lastAccess, true);

        if ( uuid != null && !isValidUUID(uuid) ) throw new HIParameterException("Invalid UUID format!");

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check if UUID already exists in project
        if ( uuid != null ) {
            if ( (Long)em.createQuery("SELECT count(b) FROM HIBase b WHERE b.project=:project AND b.uuid=:uuid")
                .setParameter("project", curProject)
                .setParameter("uuid", uuid)
                .getSingleResult() != 0 ) throw new HIEntityException("Element with UUID already exists in project!");
        }
        
        HIURL hiUrl = new HIURL(url, title, lastAccess, uuid);
        try {
            utx.begin();
            em.joinTransaction();
            hiUrl.setProject(curProject);
            em.persist(hiUrl);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        addToImportGroup(hiUrl);

        indexer.storeElement(hiUrl, curProject); // update lucene index

        return hiUrl;
    }

    @WebMethod
    public synchronized boolean updateURL(
            @WebParam(name = "hiURL") HIURL hiURL)
            throws HIParameterException, HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        checkParam(hiURL);
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIURL systemURL = em.find(HIURL.class, hiURL.getId());
        if (systemURL == null) {
            throw new HIEntityNotFoundException("External URL not found!");
        }

        if (systemURL.getProject().getId() != curProject.getId()) {
            throw new HIPrivilegeException("External URL does not belong to project!");
        }

        // update URL
        try {
            systemURL.setLastAccess(hiURL.getLastAccess());
            systemURL.setUrl(hiURL.getUrl());
            systemURL.setTitle(hiURL.getTitle());
            utx.begin();
            em.joinTransaction();
            systemURL.touchTimestamp();
            em.persist(systemURL);
            em.flush();
            utx.commit();

            indexer.storeElement(systemURL, curProject); // update lucene index

        } catch (Exception e) {
            return false;
        }
        return true;

    }

    //-------------------------
    // Text Methods
    //-------------------------
    @WebMethod
    public synchronized HIText createText(@WebParam(name = "uuid") String uuid) 
            throws HIMaintenanceModeException, HIParameterException, HIEntityException {

        if ( uuid != null && !isValidUUID(uuid) ) throw new HIParameterException("Invalid UUID format!");
        
        HIText text = new HIText(uuid);
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check if UUID already exists in project
        if ( uuid != null ) {
            if ( (Long)em.createQuery("SELECT count(b) FROM HIBase b WHERE b.project=:project AND b.uuid=:uuid")
                .setParameter("project", curProject)
                .setParameter("uuid", uuid)
                .getSingleResult() != 0 ) throw new HIEntityException("Element with UUID already exists in project!");
        }
        
        text.setProject(curProject);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(text);
            em.flush();
            utx.commit();
            utx.begin();
            em.joinTransaction();
            attachMetadataRecords(em, text);
            em.persist(text);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        addToImportGroup(text);
        
        indexer.storeElement(text, curProject); // update lucene index

        return text;
    }

    //-------------------------
    // Light Table Methods
    //-------------------------
    @WebMethod
    public synchronized HILightTable createLightTable(
            @WebParam(name = "title") String title,
            @WebParam(name = "xml") String xml,
            @WebParam(name = "uuid") String uuid)
            throws HIParameterException, HIMaintenanceModeException, HIEntityException {

        if ( uuid != null && !isValidUUID(uuid) ) throw new HIParameterException("Invalid UUID format!");
        
        checkParam(title, true);
        checkParam(xml, false);

        HILightTable lightTable = new HILightTable(title, xml, uuid);
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // check if UUID already exists in project
        if ( uuid != null ) {
            if ( (Long)em.createQuery("SELECT count(b) FROM HIBase b WHERE b.project=:project AND b.uuid=:uuid")
                .setParameter("project", curProject)
                .setParameter("uuid", uuid)
                .getSingleResult() != 0 ) throw new HIEntityException("Element with UUID already exists in project!");
        }
        
        lightTable.setProject(curProject);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(lightTable);
            em.flush();
            attachMetadataRecords(em, lightTable);
            em.persist(lightTable);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        addToImportGroup(lightTable);

        indexer.storeElement(lightTable, curProject); // update lucene index

        return lightTable;
    }

    @WebMethod
    public synchronized boolean updateLightTable(
            @WebParam(name = "lightTable") HILightTable lightTable)
            throws HIParameterException, HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        checkParam(lightTable);
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HILightTable systemLT = em.find(HILightTable.class, lightTable.getId());
        if (systemLT == null) {
            throw new HIEntityNotFoundException("Light Table not found!");
        }

        if (systemLT.getProject().getId() != curProject.getId()) {
            throw new HIPrivilegeException("Light Table does not belong to project!");
        }

        // update Light Table
        try {
            systemLT.setTitle(lightTable.getTitle());
            systemLT.setXml(lightTable.getXml());
            utx.begin();
            em.joinTransaction();
            systemLT.touchTimestamp();
            em.persist(systemLT);
            em.flush();
            utx.commit();
            indexer.storeElement(systemLT, curProject); // update lucene index
        } catch (Exception e) {
            return false;
        }
        return true;

    }

    //-------------------------
    // Repository Methods
    //-------------------------
    // -- ADMIN METHOD --
    @WebMethod
    public synchronized HIRepository adminCreateRepository(@WebParam(name = "newRepository") HIRepository newRepository)
            throws HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        curUser = em.find(HIUser.class, curUser.getId());

        HIRepository userRepos = new HIRepository();
        userRepos.setCheckoutPermission(newRepository.getCheckoutPermission());
        userRepos.setCreator(curUser);
        userRepos.setProject(curProject);
        userRepos.setUserName(newRepository.getUserName());
        userRepos.setPassword(newRepository.getPassword());
        userRepos.setUrl(newRepository.getUrl());
        userRepos.setDisplayTitle(newRepository.getDisplayTitle());
        userRepos.setRepoType(newRepository.getRepoType());
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(userRepos);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return userRepos;
    }

    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized List<HIRepository> getRepositories() throws HIMaintenanceModeException {
        List<HIRepository> repositories;

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        repositories = em.createQuery("SELECT rep FROM HIRepository rep WHERE rep.project=:project")
                .setParameter("project", curProject)
                .getResultList();

        // TODO filter list by checkout permissions
        return repositories;
    }

    @WebMethod
    public synchronized boolean adminDeleteRepository(@WebParam(name = "repository") HIRepository repository)
            throws HIEntityNotFoundException, HIPrivilegeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIRepository repos = em.find(HIRepository.class, repository.getId());

        if (repos == null) {
            throw new HIEntityNotFoundException("Repository not found!");
        }
        if (repos.getProject().getId() != curProject.getId()) {
            throw new HIPrivilegeException("Repository does not belong to project!");
        }
        try {
            utx.begin();
            em.joinTransaction();
            em.remove(repos);
            em.flush();
            utx.commit();
        } catch ( NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    //-------------------------
    // Group Methods
    //-------------------------
    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized List<HIGroup> getGroups() throws HIMaintenanceModeException {
        
        List<HIGroup> groups;

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        groups = em.createQuery("SELECT g FROM HIGroup g WHERE g.project=:project AND g.type=:type")
                .setParameter("project", curProject)
                .setParameter("type", HIGroup.GroupTypes.HIGROUP_REGULAR)
                .getResultList();
        
        // legacy update: generate UUID for element if missing
        for (HIGroup group : groups) {
            if (group.getUUID() == null) {
                try {
                    utx.begin();
                    em.joinTransaction();
                    group.setUUID(UUID.randomUUID().toString());
                    em.persist(group);
                    em.flush();
                    utx.commit();
                } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                    Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return groups;

    }

    @WebMethod
    public synchronized HIGroup createGroup(@WebParam(name = "uuid") String uuid) throws HIMaintenanceModeException, HIEntityException, HIParameterException {

        if ( uuid != null && !isValidUUID(uuid) ) throw new HIParameterException("Invalid UUID format!");
        
        HIGroup group = new HIGroup(HIGroup.GroupTypes.HIGROUP_REGULAR, true, uuid);

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        
        // check if UUID already exists in project
        if ( uuid != null ) {
            if ( (Long)em.createQuery("SELECT count(b) FROM HIBase b WHERE b.project=:project AND b.uuid=:uuid")
                .setParameter("project", curProject)
                .setParameter("uuid", uuid)
                .getSingleResult() != 0 ) throw new HIEntityException("Element with UUID already exists in project!");
        }
        
        try {
            utx.begin();
            em.joinTransaction();
            group.setProject(curProject);
            em.persist(group);
            em.flush();
            attachMetadataRecords(em, group);
            em.persist(group);
            em.flush();
            utx.commit();

            indexer.storeElement(group, curProject); // update lucene index

            // set initial sort order
            String groupSortOrder = MetadataHelper.findPreferenceValue(curProject, "groupSortOrder");
            if (groupSortOrder == null) {
                groupSortOrder = "";
            }
            groupSortOrder = groupSortOrder + "," + group.getId();
            if (groupSortOrder.startsWith(",")) {
                groupSortOrder = groupSortOrder.substring(1);
            }

            MetadataHelper.setPreferenceValue(curProject, "groupSortOrder", groupSortOrder);
            utx.begin();
            em.joinTransaction();
            if (MetadataHelper.findPreference(curProject, "groupSortOrder") != null)
                em.persist(MetadataHelper.findPreference(curProject, "groupSortOrder"));
            em.persist(curProject);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        group = em.find(HIGroup.class, group.getId());
        return group;
    }

    @WebMethod
    @Deprecated
    public synchronized boolean updateGroupProperties(
            @WebParam(name = "groupID") long groupID,
            @WebParam(name = "visible") boolean visible)
            throws HIEntityNotFoundException, HIPrivilegeException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIGroup group = em.find(HIGroup.class, groupID);

        if (group == null) {
            throw new HIEntityNotFoundException("Group not found!");
        }
        if (group.getProject().getId() != curProject.getId()) {
            throw new HIPrivilegeException("Group does not belong to project!");
        }

        group.setVisible(visible);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(group);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @WebMethod
    public synchronized void updateGroupSortOrder(
            @WebParam(name = "groupID") long groupID,
            @WebParam(name = "sortOrder") String sortOrder)
            throws HIEntityException, HIEntityNotFoundException, HIPrivilegeException, HIParameterException, HIMaintenanceModeException {

        
        HIGroup group = null;
        checkParam(sortOrder, true);

        EntityManager em = emf.createEntityManager();
        group = em.find(HIGroup.class, groupID);
        if (group == null) {
            throw new HIEntityNotFoundException("Group not found!");
        }
        if (!group.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Group does not belong to project!");
        }
        if (group.getType() != HIGroup.GroupTypes.HIGROUP_REGULAR) {
            throw new HIEntityException("You may only specify the sort order for regular groups!");
        }

        // update sort order
        group.setSortOrder(sortOrder);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(group);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @WebMethod
    public synchronized boolean deleteGroup(
            @WebParam(name = "groupID") long groupID)
            throws HIParameterException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        importGroup = em.find(HIGroup.class, importGroup.getId());
        trashGroup = em.find(HIGroup.class, trashGroup.getId());

        HIGroup group = em.find(HIGroup.class, groupID);
        if (!group.getProject().equals(curProject)) {
            return false;
        }

        // can't delete import or trash group
        if (group.equals(importGroup) || group.equals(trashGroup)) {
            return false;
        }

        // find orphaned content
        ArrayList<HIBase> orphaned = new ArrayList<HIBase>();
        for (HIBase content : group.getContents()) {
            if (!(content instanceof HIView)
                    && !(content instanceof HIInscription)
                    && !(content instanceof HILayer)) {
                if (!(content instanceof HIGroup) && countGroups(content) <= 1) {
                    orphaned.add(content);
                }
            }
        }

        // move orphaned content back to the import group
        importGroup = em.find(HIGroup.class, importGroup.getId());
        importGroup.getContents().size();
        try {
            utx.begin();
            em.joinTransaction();
            em.refresh(importGroup);
            for (HIBase content : orphaned) {
                group.removeContent(content);
                importGroup.addContent(content);
                em.persist(content);
                em.flush();
            }
            em.persist(group);
            em.flush();
            em.persist(importGroup);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        // remove group from all other groups and delete group
        for (HIGroup foundGroup : findGroups(em, group)) {
            if (removeFromGroup(group, foundGroup) == false) {
                return false;
            }
        }

        indexer.removeElement(group, curProject); // update lucene index

        removeLinks(group);

        String groupSortOrder = MetadataHelper.findPreferenceValue(curProject, "groupSortOrder");
        if (groupSortOrder == null) {
            groupSortOrder = "";
        }
        // remove content from sort order index if possible
        String newOrder = "";
        for (String sortPos : groupSortOrder.split(",")) {
            sortPos = sortPos.replaceAll(",", "");
            if ( sortPos.length() > 0 ) {
                long sortId = Long.parseLong(sortPos);
                if (sortId != group.getId()) {
                    newOrder = newOrder + "," + sortPos;
                }
            }
        }
        if (newOrder.startsWith(",")) {
            newOrder = newOrder.substring(1);
        }
        try {
            utx.begin();
            em.joinTransaction();
            em.remove(group);
            em.flush();

            // update group sort order
            curProject = em.find(HIProject.class, curProject.getId());
            MetadataHelper.setPreferenceValue(curProject, "groupSortOrder", newOrder);
            if (MetadataHelper.findPreference(curProject, "groupSortOrder") != null)
                em.persist(MetadataHelper.findPreference(curProject, "groupSortOrder"));
            em.persist(curProject);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @WebMethod
    public synchronized List<HIQuickInfo> getGroupContentQuickInfo(
            @WebParam(name = "groupID") long groupID)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        List<HIQuickInfo> previews = new ArrayList<HIQuickInfo>();
        HIQuickInfo info;

        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        HIGroup group = null;
        group = em.find(HIGroup.class, groupID);

        if (group == null) {
            throw new HIEntityNotFoundException("Group not found!");
        }
        if (!group.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Group does not belong to project!");
        }

        group.getContents().size();
        em.refresh(group);

        for (HIBase content : group.getContents()) {
            // legacy update: generate UUID for element if missing
            if (content.getUUID() == null) {
                try {
                    utx.begin();
                    em.joinTransaction();
                    if (!(content instanceof HIView)) content.setUUID(UUID.randomUUID().toString());
                    else content.setUUID(UUID.nameUUIDFromBytes(storageManager.getOriginal((HIView) content)).toString());
                    em.persist(content);
                    em.flush();
                    utx.commit();
                } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                    Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            info = createContentQuickInfo(content);
            previews.add(info);
        }

        return previews;
    }

    @WebMethod
    public synchronized HIQuickInfo getBaseQuickInfo(
            @WebParam(name = "baseID") long baseID)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIBase base = em.find(HIBase.class, baseID);
        if (base == null) {
            throw new HIEntityNotFoundException("Base element not found!");
        }
        if (!base.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Base element does not belong to project!");
        }
        // legacy update: generate UUID for element if missing
        if (base.getUUID() == null) {
            try {
                utx.begin();
                em.joinTransaction();
                if ( !(base instanceof HIView) ) base.setUUID(UUID.randomUUID().toString());
                else base.setUUID(UUID.nameUUIDFromBytes(storageManager.getOriginal((HIView)base)).toString());
                em.persist(base);
                em.flush();
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return createContentQuickInfo(base);
    }

    @WebMethod
    public synchronized HIQuickInfo getBaseQuickInfoByUUID(
            @WebParam(name = "uuid") String uuid)
            throws HIPrivilegeException, HIEntityNotFoundException, HIMaintenanceModeException, HIParameterException {

        checkParam(uuid, false);
        if ( !isValidUUID(uuid) ) throw new HIParameterException("Invalid UUID format!");
        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIBase base = null;
        try {
            base = (HIBase) em.createQuery("SELECT base FROM HIBase base WHERE base.project=:project AND base.uuid=:uuid")
                .setParameter("project", curProject)
                .setParameter("uuid", uuid)
                .getSingleResult();
        } catch (NoResultException e) { base = null; }

        if (base == null) throw new HIEntityNotFoundException("Base Element not found!");

        if (!base.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Base element does not belong to project!");
        }
        
        return createContentQuickInfo(base);
    }
    
    public synchronized HIQuickInfo createContentQuickInfo(HIBase content) {
        HIQuickInfo info = new HIQuickInfo(content);

        if (!(content instanceof HIURL) && !(content instanceof HILightTable)) {
            // extract title
            HIFlexMetadataRecord record;
            if (curProject != null) {
                record = MetadataHelper.getDefaultMetadataRecord(content, curProject.getDefaultLanguage());
            } else {
                record = MetadataHelper.getDefaultMetadataRecord(content, content.getProject().getDefaultLanguage());
            }

            // add title to quick info
            if (record != null) {
                String title = record.findValue("HIBase", "title");
                // for objects respect user wishes --> fall back to HIClassic title if no pref exists
                EntityManager em = emf.createEntityManager();
                if (content instanceof HIObject) {
                    HIPreference fieldPref;
                    if (curProject != null) {
                        fieldPref = MetadataHelper.findPreference(curProject, "admin.preview.objectTitleField");
                    } else {
                        fieldPref = MetadataHelper.findPreference(content.getProject(), "admin.preview.objectTitleField");
                    }

                    if (curProject != null) {
                        if (fieldPref == null) {
                            curProject = em.find(HIProject.class, curProject.getId());
                            fieldPref = new HIPreference("admin.preview.objectTitleField", "dc.title", curProject);
                            try {
                                utx.begin();
                                em.joinTransaction();
                                em.persist(fieldPref);
                                em.persist(curProject);
                                em.flush();
                                utx.commit();
                            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else if (fieldPref.getValue().indexOf(".") < 0) {
                            fieldPref.setValue("dc.title");
                            try {
                                utx.begin();
                                em.joinTransaction();
                                em.persist(fieldPref);
                                em.persist(curProject);
                                em.flush();
                                utx.commit();
                            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    if ( fieldPref != null ) {
                        title = record.findValue(fieldPref.getValue().split("\\.")[0], fieldPref.getValue().split("\\.")[1]);
                    }
                    // fallback to dc.title
                    if ( title == null ) title = record.findValue("dc", "title");
                    // fallback to view title of default view (if instanceof HIView and available)
                    if ( title == null ) {
                       HIFlexMetadataRecord titleRecord = null;
                       HIObject object = (HIObject) content;
                       if ( object.getViews().size() > 0 ) {
                           HIView defView = null;
                           if ( object.getDefaultView() != null && object.getDefaultView() instanceof HIView ) defView = (HIView)object.getDefaultView();
                           if ( defView == null && object.getViews().get(0) instanceof HIView ) defView = (HIView)object.getViews().get(0);
                           if ( defView != null && curProject != null ) 
                               titleRecord = MetadataHelper.getDefaultMetadataRecord(defView, curProject.getDefaultLanguage());
                       }
                       
                       if ( titleRecord != null ) title =  titleRecord.findValue("HIBase", "title");
                    }
                }

                if (title != null) {
                    info.setTitle(title);
                }

                boolean needsPreviewText = false;
                if ((content instanceof HIInscription) || (content instanceof HIText)) {
                    needsPreviewText = true;
                }

                if (content instanceof HIObject) {
                    HIObject object = (HIObject) content;

                    if (object.getDefaultView() != null) {
                        if (object.getDefaultView() instanceof HIInscription) {
                            if (curProject != null) {
                                record = MetadataHelper.getDefaultMetadataRecord(object.getDefaultView(), curProject.getDefaultLanguage());
                            } else {
                                record = MetadataHelper.getDefaultMetadataRecord(object.getDefaultView(), object.getProject().getDefaultLanguage());
                            }
                            needsPreviewText = true;
                        }
                    } else {
                        if (object.getViews().size() > 0) {
                            if (object.getSortedViews().get(0) instanceof HIInscription) {
                                if (curProject != null) {
                                    record = MetadataHelper.getDefaultMetadataRecord(object.getViews().get(0), curProject.getDefaultLanguage());
                                } else {
                                    record = MetadataHelper.getDefaultMetadataRecord(object.getViews().get(0), object.getProject().getDefaultLanguage());
                                }
                                needsPreviewText = true;
                            }
                        }
                    }
                }

                if (needsPreviewText) {
                    String preview = record.findValue("HIBase", "content");
                    if ( preview == null ) preview = "";
                    if (preview.length() > 160) {
                        preview = preview.substring(0, 160) + "...";
                    }
                    info.setPreview(preview);
                }
            }
        } else if (content instanceof HIURL) {
            // process external URLs directly, since they don�t have metadata
            if (((HIURL) content).getTitle() != null && ((HIURL) content).getTitle().length() > 0) {
                info.setTitle(((HIURL) content).getTitle());
            } else {
                info.setTitle(((HIURL) content).getUrl());
            }
            info.setPreview(((HIURL)content).getUrl());
        } else {
            // process light tables
            info.setTitle(((HILightTable) content).getTitle());
        }

        // get additional size/count info for base object if applicable
        // set parent/child ID if applicable
        if (info.getContentType() == HIQuickInfo.HI_BaseTypes.HIGroup) {
            info.setCount(((HIGroup) content).getContents().size());
        }

        if (info.getContentType() == HIQuickInfo.HI_BaseTypes.HIObject) {
            HIObject object = ((HIObject) content);
            info.setCount(object.getViews().size());

            if (object.getViews().size() > 0) {
                if (object.getDefaultView() != null) {
                    info.setRelatedID(object.getDefaultView().getId());
                } else {
                    info.setRelatedID(object.getSortedViews().get(0).getId());
                }
            }
        }

        if (info.getContentType() == HIQuickInfo.HI_BaseTypes.HIView) {
            info.setCount(((HIView) content).getLayers().size());
            info.setRelatedID(((HIView) content).getObject().getId());
        }

        if (info.getContentType() == HIQuickInfo.HI_BaseTypes.HIInscription) {
            info.setRelatedID(((HIInscription) content).getObject().getId());
        }

        if (info.getContentType() == HIQuickInfo.HI_BaseTypes.HILayer) {
            info.setRelatedID(((HILayer) content).getView().getId());
        }

        return info;
    }

    @WebMethod
    public synchronized boolean moveToGroup(
            @WebParam(name = "baseID") long baseID,
            @WebParam(name = "fromGroupID") long fromGroupID,
            @WebParam(name = "toGroupID") long toGroupID)
            throws HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        importGroup = em.find(HIGroup.class, importGroup.getId());
        trashGroup = em.find(HIGroup.class, trashGroup.getId());

        HIGroup fromGroup = null;
        HIGroup toGroup = null;
        HIBase base = null;

        // can�t move to import or trash group
        if (toGroupID == importGroup.getId() || toGroupID == trashGroup.getId()) {
            return false;
        }

        // check if groups and elements exist and belong to current project
        try {
            fromGroup = em.find(HIGroup.class, fromGroupID);
            if (!fromGroup.getProject().equals(curProject)) {
                return false;
            }

            toGroup = em.find(HIGroup.class, toGroupID);
            if (!toGroup.getProject().equals(curProject)) {
                return false;
            }

            base = em.find(HIBase.class, baseID);
            if (!base.getProject().equals(curProject)) {
                return false;
            }
        } catch (NoResultException e) {
            return false;
        }

        // refresh contents
        fromGroup.getContents().size();
        em.refresh(fromGroup);
        toGroup.getContents().size();
        em.refresh(toGroup);

        if (!fromGroup.contains(base)) {
            return false;
        }

        fromGroup.removeContent(base);
        if (!toGroup.contains(base)) {
            toGroup.addContent(base);
        }

        if (!fromGroup.equals(importGroup)) {
            removeFromGroup(base, fromGroup);
        }
        try {
            utx.begin();
            em.joinTransaction();
            fromGroup.touchTimestamp();
            em.persist(fromGroup);
            em.flush();
            toGroup.touchTimestamp();
            em.persist(toGroup);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @WebMethod
    public synchronized boolean copyToGroup(
            @WebParam(name = "baseID") long baseID,
            @WebParam(name = "groupID") long groupID)
            throws HIParameterException, HIMaintenanceModeException {

        
        // check parameters
        EntityManager em = emf.createEntityManager();
        HIGroup group = em.find(HIGroup.class, groupID);
        checkParam(group);
        HIBase base = em.find(HIBase.class, baseID);
        checkParam(base);

        // sync state with entity manager
        importGroup = em.find(HIGroup.class, importGroup.getId());
        trashGroup = em.find(HIGroup.class, trashGroup.getId());
        curProject = em.find(HIProject.class, curProject.getId());

        // check permissions (does group belong to current project?)
        if (!group.getProject().equals(curProject)) {
            return false;
        }
        if (!base.getProject().equals(curProject)) {
            return false;
        }

        // user can't copy objects to the import or trash group
        if (group.equals(importGroup) || group.equals(trashGroup)) {
            return false;
        }

        // add content to target group
        return addToGroup(base, group);
    }

    @WebMethod
    public synchronized boolean moveToTrash(
            @WebParam(name = "baseID") long baseID)
            throws HIParameterException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        importGroup = em.find(HIGroup.class, importGroup.getId());
        trashGroup = em.find(HIGroup.class, trashGroup.getId());

        // find element
        HIBase base = em.find(HIBase.class, baseID);
        checkParam(base);
        if (!base.getProject().equals(curProject)) {
            return false;
        }

        // don't put groups or layers into the trash
        if (base instanceof HIGroup) {
            return false;
        }
        if (base instanceof HILayer) {
            return false;
        }

        // only move if element isn't already in trash
        if (trashGroup.contains(base)) {
            return false;
        }

        // delete element from all other groups, including the import group
        removeFromGroup(base, importGroup);

        for (HIGroup group : findGroups(em, base)) {
            if (removeFromGroup(base, group) == false) {
                return false;
            }
        }

        // if element was an object, remove all views, inscriptions and layers from groups, and their links
        if (base instanceof HIObject) {

            for (HIBase objBase : ((HIObject) base).getViews()) {
                if (objBase instanceof HIView) {
                    // remove layers
                    for (HILayer layer : ((HIView) objBase).getLayers()) {
                        for (HIGroup group : findGroups(em, layer)) {
                            if (removeFromGroup(layer, group) == false) {
                                return false;
                            }
                        }
                        removeLinks(layer);
                    }
                }
                // remove view/inscription
                for (HIGroup group : findGroups(em, objBase)) {
                    if (removeFromGroup(objBase, group) == false) {
                        return false;
                    }
                }
                removeLinks(objBase);
            }
        } else if (base instanceof HIObjectContent) {
            // if element was a view, remove all layers from groups, and their links			
            if (base instanceof HIView) {
                for (HILayer layer : ((HIView) base).getLayers()) {
                    for (HIGroup group : findGroups(em, layer)) {
                        if (removeFromGroup(layer, group) == false) {
                            return false;
                        }
                    }
                    removeLinks(layer);
                }
            }
            // remove view from object
            HIObjectContent content = (HIObjectContent) base;
            HIObject object = content.getObject();
            object.removeView(content);
            HIObject trashObject = null;
            try {
                utx.begin();
                em.joinTransaction();
                object.touchTimestamp();
                em.persist(object);
                em.flush();
                // create empty object for view
                trashObject = new HIObject();
                curProject = em.find(HIProject.class, curProject.getId());
                trashObject.setProject(curProject);
                em.persist(trashObject);
                em.flush();
                attachMetadataRecords(em, trashObject);
                // add view to empty object wrapper
                content.setObject(trashObject);
                trashObject.addView(content);
                em.persist(content);
                em.flush();
                em.persist(object);
                em.flush();
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }

            // add element to trash group
            if (addToGroup(trashObject, trashGroup) == false) {
                return false;
            }

        }

        // remove all links from layers to this element
        removeLinks(base);

        // remove element as project start element if necessary
        if (curProject.getStartObject() != null && curProject.getStartObject().getId() == base.getId()) {
            curProject.setStartObject(null);
            try {
                utx.begin();
                em.joinTransaction();
                em.persist(curProject);
                em.flush();
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // add element to trash group
        if (!(base instanceof HIObjectContent)) {
            if (addToGroup(base, trashGroup) == false) {
                return false;
            }
        }

        return true;
    }

    @WebMethod
    public synchronized boolean removeFromGroup(
            @WebParam(name = "baseID") long baseID,
            @WebParam(name = "groupID") long groupID)
            throws HIEntityNotFoundException, HIPrivilegeException, HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());
        HIGroup group = em.find(HIGroup.class, groupID);
        HIBase base = em.find(HIBase.class, baseID);

        if (group == null) {
            throw new HIEntityNotFoundException("Object not found!");
        }
        if (!group.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Group does not belong to project!");
        }
        if (base == null) {
            throw new HIEntityNotFoundException("Object not found!");
        }
        if (!base.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Element does not belong to project!");
        }

        // cannot remove elements from import or trash group
        if (group.getType() != HIGroup.GroupTypes.HIGROUP_REGULAR) {
            return false;
        }
        // try to remove element
        if (removeFromGroup(base, group) == false) {
            return false;
        }

        // check if element is in another group, if not add to import group
        if (!(base instanceof HIGroup) && countGroups(base) < 1) {
            addToImportGroup(base);
        }
        return true;
    }

    private synchronized long countGroups(HIBase base) {
        EntityManager em = emf.createEntityManager();
        long size = (Long) em.createQuery("SELECT count(g) FROM HIGroup g WHERE g.contents=:base AND g.type=:type")
                .setParameter("base", base)
                .setParameter("type", HIGroup.GroupTypes.HIGROUP_REGULAR)
                .getSingleResult();

        return size;
    }

    private synchronized List<HIGroup> findGroups(EntityManager em, HIBase base) throws HIMaintenanceModeException {
        List<HIGroup> groups = new ArrayList<HIGroup>();

        curProject = em.find(HIProject.class, curProject.getId());
        base = em.find(HIBase.class, base.getId());

        List<HIGroup> projectGroups = getGroups();

        for (HIGroup group : projectGroups) {
            group = em.find(HIGroup.class, group.getId());
            if (group.contains(base)) {
                groups.add(group);
            }
        }

        return groups;
    }

    private synchronized boolean addToImportGroup(HIBase base) {
        if ((base instanceof HILayer)
                || (base instanceof HIView)
                || (base instanceof HIInscription)) {
            return false;
        }

        return addToGroup(base, importGroup);
    }

    private synchronized boolean addToGroup(HIBase base, HIGroup group) {

        // sync objects with entity manager
        EntityManager em = emf.createEntityManager();
        group = em.find(HIGroup.class, group.getId());
        importGroup = em.find(HIGroup.class, importGroup.getId());
        trashGroup = em.find(HIGroup.class, trashGroup.getId());
        base = em.find(HIBase.class, base.getId());

        // can't add a group to itself
        if (base instanceof HIGroup)
            if (base.getId() == group.getId())
                return false;

        // refresh group contents
        group.getContents().size();
        em.refresh(group);

        // don't add an object that's already in the group
        if (group.contains(base)) return true;

        try {
            // add content to target group
            group.addContent(base);
            utx.begin();
            em.joinTransaction();
            group.touchTimestamp();
            em.persist(group);
            em.flush();
            utx.commit();

            // remove content from import group
            if (!group.equals(importGroup)) {
                /*if (base instanceof HIObjectContent) {
                    removeFromGroup(((HIObjectContent) base).getObject(), importGroup);
                } else if (base instanceof HILayer) {
                    removeFromGroup(((HILayer) base).getView().getObject(), importGroup);
                } else */if (!(base instanceof HIGroup)) {
                    removeFromGroup(base, importGroup);
                }
            }
            // remove content from trash
            if (!group.equals(trashGroup)) {
                removeFromGroup(base, trashGroup);
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private synchronized boolean removeFromGroup(HIBase base, HIGroup group) {
        EntityManager em = emf.createEntityManager();
        base = em.find(HIBase.class, base.getId());
        group = em.find(HIGroup.class, group.getId());

        // refresh group contents
        group.getContents().size();
        em.refresh(group);

        if (group.contains(base)) {
            group.removeContent(base);
            try {
                utx.begin();
                em.joinTransaction();
                group.touchTimestamp();
                em.persist(group);
                em.flush();
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private synchronized void removeLinks(HIBase base) {
        EntityManager em = emf.createEntityManager();
        base = em.find(HIBase.class, base.getId());
        curProject = em.find(HIProject.class, curProject.getId());
        
        // check if element is start element of project
        if ( curProject.getStartObject() != null && curProject.getStartObject().getId() == base.getId() ) {
            try {
                utx.begin();
                em.joinTransaction();
                curProject.setStartObject(null);
                em.persist(curProject);
                em.flush();
                utx.commit();
                base = em.find(HIBase.class, base.getId());
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        List<HILayer> layers = em.createQuery("SELECT l FROM HILayer l WHERE l.link=:base")
                .setParameter("base", base)
                .getResultList();

        for (HILayer layer : layers) {
            layer.setLink(null);
            try {
                utx.begin();
                em.joinTransaction();
                layer.touchTimestamp();
                em.persist(layer);
                em.flush();
                utx.commit();
            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //-------------------------
    // Search Methods
    //-------------------------
    // DEBUG
    @WebMethod
    public synchronized List<HIQuickInfo> simpleSearch(
            @WebParam(name = "text") String text,
            @WebParam(name = "language") String language)
            throws HIMaintenanceModeException {
        ArrayList<HIQuickInfo> results;

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        Vector<Long> baseIDs = indexer.simpleSearch(text, curProject, language);
        results = new ArrayList<HIQuickInfo>(baseIDs.size());

        for (long baseID : baseIDs) {
            HIBase base = em.find(HIBase.class, baseID);
            if (base != null && base.getProject().getId() == curProject.getId()) {
                results.add(createContentQuickInfo(base));
            }
        }

        return results;
    }

    @WebMethod
    public synchronized List<HIQuickInfo> fieldSearch(
            @WebParam(name = "fields") List<String> fields,
            @WebParam(name = "contents") List<String> contents,
            @WebParam(name = "language") String language)
            throws HIMaintenanceModeException, HIParameterException {
        ArrayList<HIQuickInfo> results;

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        if (fields == null || contents == null || fields.size() != contents.size()) {
            throw new HIParameterException("Invalid or missing parameters for fields and/or contents!");
        }

        Vector<Long> baseIDs = indexer.fieldSearch(fields, contents, curProject, language);
        results = new ArrayList<HIQuickInfo>(baseIDs.size());

        for (long baseID : baseIDs) {
            HIBase base = em.find(HIBase.class, baseID);
            if (base != null && base.getProject().getId() == curProject.getId()) {
                results.add(createContentQuickInfo(base));
            }
        }

        return results;
    }

    // DEBUG remove
    @SuppressWarnings("unchecked")
    @WebMethod
    public synchronized void rebuildSearchIndex() throws HIMaintenanceModeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        // init empty search index
        indexer.initIndex(curProject);

        // rebuild index
        List<HIBase> elements = em.createQuery("SELECT b FROM HIBase b WHERE b.project=:project")
                .setParameter("project", curProject).getResultList();

        for (HIBase element : elements) {
            indexer.storeElement(element, curProject);
        }

    }

    //-------------------------
    // Metadata Methods
    //-------------------------
    @WebMethod
    public HIFlexMetadataRecord createFlexMetadataRecord(
            @WebParam(name = "baseID") long baseID,
            @WebParam(name = "language") String language)
            throws HIParameterException, HIEntityNotFoundException, HIMaintenanceModeException {

        
        checkParam(language, false);

        // TODO: refactor entity search
        HIBase base;
        EntityManager em = emf.createEntityManager();
        try {
            base = (HIBase) em.createQuery("SELECT b FROM HIBase b WHERE b.id=:id AND b.project=:project")
                    .setParameter("id", baseID)
                    .setParameter("project", curProject)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new HIEntityNotFoundException("Base entity not found or does not belong to current project!");
        }

        // TODO: refactor language search
        boolean langFound = false;
        for (HILanguage lang : curProject.getLanguages()) {
            if (lang.getLanguageId().compareTo(language) == 0) {
                langFound = true;
            }
        }

        if (!langFound) {
            throw new HIEntityNotFoundException("Desired language not in project languages!");
        }
        
        HIFlexMetadataRecord rec = null;
        try {
            utx.begin();
            em.joinTransaction();
            rec = createFlexMetadataRecord(em, base, language);
            utx.commit();
        } catch ( NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rec;
    }

    // internal method
    private HIFlexMetadataRecord createFlexMetadataRecord(
            EntityManager em, HIBase base, String language) {

        HIFlexMetadataRecord record = null;
//        EntityManager em = emf.createEntityManager();

        // URLs and Light Tables don�t have multilingual-metadata
        if (base instanceof HIURL || base instanceof HILightTable) {
            return null;
        }

        List<HIFlexMetadataRecord> records = MetadataHelper.resolveMetadataRecords(base);

        // check if record already exists, if so --> return it		
        for (HIFlexMetadataRecord rec : records) {
            if (rec.getLanguage().compareTo(language) == 0) {
                return rec;
            }
        }

        // create new record
        record = new HIFlexMetadataRecord(language, base);

        if (base instanceof HIGroup || base instanceof HILayer) {
            record.addEntry(new HIKeyValue("HIBase.title", ""));
            record.addEntry(new HIKeyValue("HIBase.comment", ""));

        } else if (base instanceof HIInscription
                || base instanceof HILightTable
                || base instanceof HIText) {
            record.addEntry(new HIKeyValue("HIBase.title", ""));
            record.addEntry(new HIKeyValue("HIBase.content", ""));

        } else if (base instanceof HIView) {
            record.addEntry(new HIKeyValue("HIBase.title", ""));
            record.addEntry(new HIKeyValue("HIBase.source", ""));
            record.addEntry(new HIKeyValue("HIBase.comment", ""));

        } else if (base instanceof HIObject) {
            for (HIFlexMetadataTemplate template : curProject.getTemplates()) {
                if (template.getNamespacePrefix().compareTo("HIBase") != 0) {
                    for (HIFlexMetadataSet set : template.getEntries()) {
                        record.addEntry(new HIKeyValue(template.getNamespacePrefix() + "." + set.getTagname(), ""));
                    }
                }
            }
        }

        addFlexMetadataRecord(record, base);
/*        try {
            utx.begin();
*/
//            em.joinTransaction();
            em.persist(base);
            em.flush();
/*            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
*/
        indexer.storeElement(base, curProject); // update lucene index

        return record;

    }

    @WebMethod
    public List<HIFlexMetadataRecord> getFlexMetadataRecords(
            @WebParam(name = "baseID") long baseID)
            throws HIMaintenanceModeException, HIParameterException, HIEntityNotFoundException, HIPrivilegeException {

        
        EntityManager em = emf.createEntityManager();
        curProject = em.find(HIProject.class, curProject.getId());

        HIBase base = em.find(HIBase.class, baseID);
        if (base == null) {
            throw new HIEntityNotFoundException("Element not found!");
        }
        if (!base.getProject().equals(curProject)) {
            throw new HIPrivilegeException("Element does not belong to project!");
        }

        return base.getMetadata();
    }

    @WebMethod
    public boolean updateFlexMetadataRecords(
            @WebParam(name = "records") List<HIFlexMetadataRecord> records)
            throws HIParameterException, HIMaintenanceModeException {

        
        checkParam(records);

        // TODO: optimize function for multiple records
        for (HIFlexMetadataRecord record : records) {
            if (updateFlexMetadataRecord(record) == false) {
                return false;
            }
        }

        return true;
    }

    // DEBUG: optimize this
    @WebMethod
    public boolean updateFlexMetadataRecord(
            @WebParam(name = "record") HIFlexMetadataRecord record)
            throws HIParameterException, HIMaintenanceModeException {

        
        checkParam(record);

        EntityManager em = emf.createEntityManager();
        HIFlexMetadataRecord dbRecord = em.find(HIFlexMetadataRecord.class, record.getId());
        if (dbRecord == null) {
            return false;
        }
        HIBase owner = dbRecord.getOwner();
        em.refresh(owner);

        for (HIFlexMetadataRecord rec : owner.getMetadata()) {
            if (rec.getId() == dbRecord.getId()) {
                dbRecord = rec;
            }
        }

        String value;
        try {
            utx.begin();
            em.joinTransaction();
            for (HIKeyValue kv : dbRecord.getContents()) {
                value = null;
                for (HIKeyValue userKV : record.getContents()) {
                    if (userKV.getKey().compareTo(kv.getKey()) == 0) {
                        value = userKV.getValue();
                    }
                }
                if (value != null) {
                    kv.setValue(value);
                    em.persist(kv);
                    em.flush();
                }
            }
            owner.touchTimestamp(); // update timestamp
            em.persist(owner);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        indexer.storeElement(owner, curProject); // update lucene index

        return true;
    }

    private void attachMetadataRecords(EntityManager em, HIBase base) {
        if (base == null) return;

        curProject = em.find(HIProject.class, curProject.getId());
        base = em.find(HIBase.class, base.getId());
        if (base == null) return;

        for (HILanguage lang : curProject.getLanguages())
            createFlexMetadataRecord(em, base, lang.getLanguageId());
    }

    // -----------------------------------------------------------------------
    public WSstates getState() {
        return state;

    }

    public HIRole.HI_Roles getRole() {
        if (curRole == null) {
            return null;
        }
        return curRole.getType();
    }

    public boolean isSysop() {
        boolean isSysop = false;

        if (curUser.getUserName().equalsIgnoreCase("sysop")) {
            isSysop = true;
        }

        return isSysop;
    }

    public String getCurrentUser() {
        return curUser.getUserName();
    }

    // -------------------------------------------------------------------
    private HIRole getRole(HIUser user, HIProject project) {
        HIRole role = null;

        EntityManager em = emf.createEntityManager();
        try {
            role = (HIRole) em.createQuery("SELECT r FROM HIRole r WHERE r.user=:user AND r.project=:project")
                    .setParameter("user", user)
                    .setParameter("project", project)
                    .getSingleResult();
        } catch (NoResultException e) {
        };

        return role;
    }

    private void setRole(HIUser user, HIProject project, HIRole.HI_Roles type) {
        HIRole role = null;

        role = getRole(user, project);
        EntityManager em = emf.createEntityManager();
        try {
            utx.begin();
            em.joinTransaction();
            if (role != null) {
                // update user´s role in this project
                role.setType(type);
                em.persist(role);
                em.flush();
            } else {
                // create role for user, effectively adding the user to the project
                role = new HIRole(user, project, type);
                em.persist(role);
                em.flush();
            }
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //-------------------------
    // Metadata Methods
    //-------------------------
    
    private void addSystemTemplates(HIProject project) {
        // create the base template
        EntityManager em = emf.createEntityManager();
        project = em.find(HIProject.class, project.getId());
        HIFlexMetadataTemplate baseTemplate = new HIFlexMetadataTemplate();
        baseTemplate.setNamespacePrefix("HIInternal");
        baseTemplate.setNamespaceURI("http://hyperimage.ws/PeTAL/HIInternal/2.0");
        baseTemplate.setNamespaceURL("http://hyperimage.ws/dev/schema/hiinternal_schema_v2.xsd");
        baseTemplate.setProject(project);

        project.getTemplates().add(baseTemplate);
        try {
            utx.begin();
            em.joinTransaction();
            em.persist(baseTemplate);
            em.flush();

            baseTemplate.getEntries().add(new HIFlexMetadataSet("note", baseTemplate, true));
            em.persist(baseTemplate);
            em.flush();
            baseTemplate.getEntries().add(new HIFlexMetadataSet("catchall", baseTemplate, true));
            em.persist(baseTemplate);
            em.flush();
            // update sort order
            baseTemplate.getSortOrder();
            em.persist(baseTemplate);
            em.flush();

            // create the view template
            HIFlexMetadataTemplate viewTemplate = new HIFlexMetadataTemplate();
            viewTemplate.setNamespacePrefix("HIBase");
            viewTemplate.setNamespaceURI("http://hyperimage.ws/PeTAL/HIBase/2.0");
            viewTemplate.setNamespaceURL("http://hyperimage.ws/dev/schema/hibase_schema_v2.xsd");
            viewTemplate.setProject(project);

            project.getTemplates().add(viewTemplate);
            em.persist(viewTemplate);
            em.flush();

            viewTemplate.getEntries().add(new HIFlexMetadataSet("title", viewTemplate, false));
            em.persist(viewTemplate);
            em.flush();
            viewTemplate.getEntries().add(new HIFlexMetadataSet("source", viewTemplate, false));
            em.persist(viewTemplate);
            em.flush();
            viewTemplate.getEntries().add(new HIFlexMetadataSet("comment", viewTemplate, true));
            em.persist(viewTemplate);
            em.flush();
            viewTemplate.getEntries().add(new HIFlexMetadataSet("content", viewTemplate, true));
            em.persist(viewTemplate);
            em.flush();
            // update sort order
            viewTemplate.getSortOrder();
            em.persist(viewTemplate);
            em.persist(project);
            em.flush();
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            Logger.getLogger(HIEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void addFlexMetadataRecord(HIFlexMetadataRecord record, HIBase base) {
        record.setOwner(base);

        if (!(base instanceof HIURL) && !(base instanceof HILightTable)) {
            base.getMetadata().add(record);
        }
    }

    public static String getMD5HashString(byte[] inputData) {
        String hashString = "";
        MessageDigest md5;
        byte[] digest;
        int curNum;

        final char[] hexTable = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.reset();

            md5.update(inputData);
            digest = md5.digest();

            for (int i = 0; i < digest.length; i++) {
                curNum = digest[i]; // make sure current byte of digest hex is unsigned
                if (curNum < 0) {
                    curNum = curNum + 256;
                }
                hashString = hashString + hexTable[ curNum / 16] + hexTable[ curNum % 16];
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("MD5 not supported by current Java VM!");
            System.exit(1);
        }
        return hashString;
    }

    public static boolean isValidUUID(String uuid) {
        return uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }
    
    public static void checkParam(String input, boolean mayBeEmpty) throws HIParameterException {

        if (input == null) {
            throw new HIParameterException("A required parameter was missing!");
        }
        if (!mayBeEmpty && input.length() == 0) {
            throw new HIParameterException("A required parameter was empty!");
        }
    }

    private void checkParam(HIProject project) throws HIParameterException {
        if (project == null) {
            throw new HIParameterException("A required parameter (HIProject) was missing!");
        }
    }

    private void checkParam(HIBase base) throws HIParameterException {
        if (base == null) {
            throw new HIParameterException("A required parameter (HIBase) was missing!");
        }
    }

    private void checkParam(HIFlexMetadataRecord record) throws HIParameterException {
        if (record == null) {
            throw new HIParameterException("A required parameter (HIFlexMetadataRecord) was missing!");
        }
    }

    private void checkParam(Object object) throws HIParameterException {
        if (object == null) {
            throw new HIParameterException("A required parameter was missing!");
        }
    }

}
