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

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 *
 * @author Jens-Martin Loebel
 */
public class HIServiceCORSFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        response.getHttpHeaders().putSingle("Access-Control-Allow-Origin", "*");
        response.getHttpHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        response.getHttpHeaders().putSingle("Access-Control-Allow-Headers", "content-type");
        return response;
    }
    
}
