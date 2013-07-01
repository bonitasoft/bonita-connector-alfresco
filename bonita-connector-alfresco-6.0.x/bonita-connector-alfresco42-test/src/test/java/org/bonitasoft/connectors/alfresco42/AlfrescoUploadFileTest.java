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
package org.bonitasoft.connectors.alfresco42;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.bonitasoft.connectors.alfresco34.AlfrescoConnector;
import org.bonitasoft.connectors.alfresco34.UploadFileConnector;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Yanyan Liu
 */
public class AlfrescoUploadFileTest extends AlfrescoConnectorTest {

    @Override
    protected Class<? extends AlfrescoConnectorTest> getConnectorTestClass() {
        return this.getClass();
    }

    @Cover(classes = { UploadFileConnector.class }, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-580",
            story = "Tests the upload of a file with Alfresco", keywords = { "Alfresco", "Connector", "Upload", "File" })
    @Test
    public void uploadFileByPath() throws Exception {
        // create file to upload first
        final String fileName = "file" + System.currentTimeMillis() + ".txt";
        final String parentPath = System.getProperty("user.home") + System.getProperty("file.separator");
        final String filePath = parentPath + fileName;
        final File createdFile = this.createFile(filePath);
        // upload file by path
        final Map<String, Object> inputs = new HashMap<String, Object>();
        final String description = "test upload file by path";
        final String destinationFolder = "/User%20Homes/dev/";
        final String mimeType = "text/plain";
        inputs.put(UploadFileConnector.FILE_OBJECT, filePath);
        inputs.put(UploadFileConnector.FILE_NAME, fileName);
        inputs.put(UploadFileConnector.DESCRIPTION, description);
        inputs.put(UploadFileConnector.DESTINATION_FOLDER, destinationFolder);
        inputs.put(UploadFileConnector.MIME_TYPE, mimeType);
        final AlfrescoConnector connector = this.getAlfrescoConnector(inputs);
        connector.validateInputParameters();
        final Map<String, Object> result = connector.execute();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("201", result.get(STATUS_CODE));
        @SuppressWarnings("unchecked")
        final Document<Element> responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        assertNotNull(responseDocument);
        final Entry entry = (Entry) responseDocument.getRoot();
        assertEquals(fileName, entry.getTitle());
        assertEquals(description, entry.getSummary());
        final String id = entry.getId().toString(); // id format: urn:uuid:0cb4d018-2ca8-4bab-8eff-8c77e4361985
        final String itemId = id.substring(id.lastIndexOf(":") + 1);
        // delete files in alfresco server and local file system.
        this.deleteItemById(itemId);
        createdFile.delete();
    }

    @Override
    protected AlfrescoConnector getAlfrescoConnector(final Map<String, Object> specificInputs) {
        final AlfrescoConnector connectorWithParams = new UploadFileConnector();
        final Map<String, Object> globalParameters = this.prepareGlobalAlfrescoConnectorInputs();
        globalParameters.putAll(specificInputs);
        connectorWithParams.setInputParameters(globalParameters);
        return connectorWithParams;
    }

}
