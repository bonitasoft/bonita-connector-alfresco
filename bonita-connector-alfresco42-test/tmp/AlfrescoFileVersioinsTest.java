/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.connectors.alfresco34;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Yanyan Liu
 */
public class AlfrescoFileVersioinsTest extends org.bonitasoft.connectors.alfresco34.AlfrescoConnectorTest {

    @Override
    protected Class<? extends org.bonitasoft.connectors.alfresco34.AlfrescoConnectorTest> getConnectorTestClass() {
        return this.getClass();
    }

    @SuppressWarnings("unchecked")
    @Cover(classes = { DeleteItemByIdConnector.class }, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-580",
            story = "Tests the version of a file with Alfresco", keywords = { "Alfresco", "Connector", "Version", "File" })
    @Test
    public void fileVersion() throws Exception {
        // upload file by path in order to do preparation for file version action
        final String fileName = "file" + System.currentTimeMillis() + ".txt";
        final String parentPath = System.getProperty("user.home") + System.getProperty("file.separator");
        final String filePath = parentPath + fileName;
        final File createdFile = this.createFile(filePath);

        final Map<String, Object> uploadFileInputs = new HashMap<String, Object>();
        final String description = "upload file by path in file version test";
        final String destinationFolder = "/User%20Homes/dev/";
        final String mimeType = "text/plain";
        Map<String, Object> result = this.uploadFile(fileName, filePath, uploadFileInputs, description, destinationFolder, mimeType);

        Document<Element> responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        assertNotNull(responseDocument);
        final Entry entry = (Entry) responseDocument.getRoot();
        final String id = entry.getId().toString(); // id format: urn:uuid:0cb4d018-2ca8-4bab-8eff-8c77e4361985
        final String itemId = id.substring(id.lastIndexOf(":") + 1);

        // file version test
        final Map<String, Object> fileVersionInputs = new HashMap<String, Object>();
        fileVersionInputs.put(FileVersionsConnector.FILE_ID, itemId);
        final Connector fileVersionConnector = this.getAlfrescoConnector(fileVersionInputs);
        fileVersionConnector.validateInputParameters();
        result = fileVersionConnector.execute();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
        responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        assertNotNull(responseDocument);
        // entry = (Entry) responseDocument.getRoot();//TODO improvement

        // delete file in alfresco server and local file system.
        this.deleteItemById(itemId);
        createdFile.delete();
    }

    @Override
    protected AlfrescoConnector getAlfrescoConnector(final Map<String, Object> specificInputs) {
        final AlfrescoConnector connectorWithParams = new FileVersionsConnector();
        final Map<String, Object> defaultParameters = this.prepareGlobalAlfrescoConnectorInputs();
        defaultParameters.putAll(specificInputs);
        connectorWithParams.setInputParameters(defaultParameters);
        return connectorWithParams;
    }

}
