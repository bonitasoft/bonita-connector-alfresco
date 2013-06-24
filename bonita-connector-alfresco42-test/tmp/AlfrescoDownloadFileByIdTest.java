/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
import static org.junit.Assert.assertTrue;

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
public class AlfrescoDownloadFileByIdTest extends org.bonitasoft.connectors.alfresco34.AlfrescoConnectorTest {

    @Override
    protected Class<? extends org.bonitasoft.connectors.alfresco34.AlfrescoConnectorTest> getConnectorTestClass() {
        return this.getClass();
    }

    @Cover(classes = { DownloadFileByIdConnector.class }, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-580",
            story = "Tests the download of a non-existing file with its id with Alfresco", keywords = { "Alfresco", "Connector", "Download", "File" })
    @Test
    public void downloadNonExistingFile() throws Exception {
        // download file by id test and check
        final String parentPath = System.getProperty("user.home") + System.getProperty("file.separator");
        final Map<String, Object> downloadFileInputs = new HashMap<String, Object>();
        downloadFileInputs.put(DownloadFileByIdConnector.FILE_ID, String.valueOf(System.currentTimeMillis()));
        downloadFileInputs.put(DownloadFileByIdConnector.OUTPUT_FILE_NAME, "file_download");
        downloadFileInputs.put(DownloadFileByIdConnector.OUTPUT_FILE_FOLDER, parentPath);
        final Connector connector = this.getAlfrescoConnector(downloadFileInputs);
        connector.validateInputParameters();
        final Map<String, Object> result = connector.execute();
        assertEquals("CLIENT_ERROR", result.get(AlfrescoConnector.RESPONSE_TYPE));
        assertEquals("404", result.get(AlfrescoConnector.STATUS_CODE));
        assertNotNull(result.get(AlfrescoConnector.STACK_TRACE));
    }

    @Cover(classes = { DownloadFileByIdConnector.class }, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-580",
            story = "Tests the download of a file with its id with Alfresco", keywords = { "Alfresco", "Connector", "Download", "File" })
    @Test
    public void downloadFileById() throws Exception {
        // create file to upload first
        final String fileName = "file" + System.currentTimeMillis() + ".txt";
        final String parentPath = System.getProperty("user.home") + System.getProperty("file.separator");
        final String filePath = parentPath + fileName;
        final File createdFile = this.createFile(filePath);

        // upload file by path
        final Map<String, Object> uploadFileInputs = new HashMap<String, Object>();
        final String description = "test upload file by path";
        final String destinationFolder = "/User%20Homes/dev/";
        final String mimeType = "text/plain";
        Map<String, Object> result = this.uploadFile(fileName, filePath, uploadFileInputs, description, destinationFolder, mimeType);
        @SuppressWarnings("unchecked")
        final Document<Element> responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        assertNotNull(responseDocument);
        final Entry entry = (Entry) responseDocument.getRoot();
        final String id = entry.getId().toString(); // id format: urn:uuid:0cb4d018-2ca8-4bab-8eff-8c77e4361985
        final String itemId = id.substring(id.lastIndexOf(":") + 1);

        // down load file by id test and check
        final Map<String, Object> downloadFileInputs = new HashMap<String, Object>();
        downloadFileInputs.put(DownloadFileByIdConnector.FILE_ID, itemId);
        downloadFileInputs.put(DownloadFileByIdConnector.OUTPUT_FILE_NAME, fileName + "_download");
        downloadFileInputs.put(DownloadFileByIdConnector.OUTPUT_FILE_FOLDER, parentPath);
        final Connector connector = this.getAlfrescoConnector(downloadFileInputs);
        connector.validateInputParameters();
        result = connector.execute();
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
        final File downloadedFile = new File(filePath + "_download");
        assertTrue(downloadedFile.exists());

        // delete files both in afresco server and local file system.
        this.deleteItemById(itemId);
        createdFile.delete();
        downloadedFile.delete();
    }

    @Override
    protected AlfrescoConnector getAlfrescoConnector(final Map<String, Object> specificInputs) {
        final AlfrescoConnector connectorWithParams = new DownloadFileByIdConnector();
        final Map<String, Object> defaultParameters = this.prepareGlobalAlfrescoConnectorInputs();
        defaultParameters.putAll(specificInputs);
        connectorWithParams.setInputParameters(defaultParameters);
        return connectorWithParams;
    }

}
