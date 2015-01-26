/*
 * Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt). All rights reserved.
 * http://bitgilde.de/
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
 *
 * For further information on HyperImage visit http://hyperimage.ws/
 */

package org.hyperimage.service.model;

import java.util.HashMap;

/**
 *
 * @author Jens-Martin Loebel (loebel@bitgilde.de)
 */
public class PRImage {
    
    private String pid;
    private HashMap<String, String> metadata = new HashMap<String, String>();
    private byte[] data;


    public void setPID (String pid) {
        this.pid = pid;
    }


    public String getPID () {
        return pid;
    }
    
    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public HashMap<String, String> getMetadata() {
        return metadata;
    }


    public void setData (byte[] data) {
        this.data = data;
    }

    public byte[] getData () {
        return data;
    }

}
