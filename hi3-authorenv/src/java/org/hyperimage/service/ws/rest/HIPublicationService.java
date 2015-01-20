/*
 * Copyright 2014 Leuphana Universität Lüneburg. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hyperimage.service.ws.rest;

import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * This ReST service serves the publication view of a HyperImage project element 
 * if the project admin has chosen to make the project available to the public.
 * 
 * @author Jens-Martin Loebel
 * 
 */
@Stateless
@Path("/")
@ApplicationPath("/pub")
public class HIPublicationService extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        resources.add(org.hyperimage.service.ws.rest.HIPublicationService.class);
        return resources;
    }
    
    // add security context for user authentication agains HI database
    @Context
    SecurityContext securityContext;
    
    // add HI persistence context
    @PersistenceUnit(unitName = "HIEditor_ORM")
    @PersistenceContext(unitName = "HIEditor_ORM")
    EntityManagerFactory emf;

    
    /**
     * show list of published projects to user
     * 
     * @return html page of published projects
     */
    @GET
    @Produces("text/html")
    public Response getProjectList() {
        return Response.status(Response.Status.OK).entity("<html><body><h1>HI Publication Service</h1><p><pre>not yet implemented</pre></p></body></html>").build();
    }
}
