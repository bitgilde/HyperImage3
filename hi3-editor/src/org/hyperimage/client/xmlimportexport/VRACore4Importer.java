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

package org.hyperimage.client.xmlimportexport;

import java.io.File;
import org.w3c.dom.Document;

/**
 *
 * @author Heinz-Günter Kuper
 */
public class VRACore4Importer extends XMLImporter {

    public VRACore4Importer(File inputFile, Document xmlDocument) {
        this.xmlDocument = xmlDocument; // inherited from superclass
        this.inputFile = inputFile;
    }

    public void importXMLToProject() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void importLanguages() {
        // find all attributes with xml:lang and add as appropriate.
        
    }
    
}
