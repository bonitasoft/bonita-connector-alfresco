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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.bonitasoft.engine.connector.Connector;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Yanyan Liu
 */
public class AlfrescoListCheckedOutFilesTest extends org.bonitasoft.connectors.alfresco34.AlfrescoConnectorTest {

    @Override
    protected Class<? extends org.bonitasoft.connectors.alfresco34.AlfrescoConnectorTest> getConnectorTestClass() {
        return this.getClass();
    }

    @SuppressWarnings("unchecked")
    @Ignore
    @Test
    public void listCheckedOutFiles() throws Exception {
        // create file to upload first
        final String fileName = "file" + System.currentTimeMillis() + ".txt";
        final String parentPath = System.getProperty("user.home") + System.getProperty("file.separator");
        final String filePath = parentPath + fileName;
        final File createdFile = this.createFile(filePath);

        System.out.println(filePath);

        // upload file by path
        final Map<String, Object> uploadFileInputs = new HashMap<String, Object>();
        final String description = "test upload file by path";
        final String destinationFolder = "/User%20Homes/dev/";
        final String mimeType = "text/plain";
        Map<String, Object> result = this.uploadFile(fileName, filePath, uploadFileInputs, description, destinationFolder, mimeType);
        Document<Element> responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        assertNotNull(responseDocument);
        final Entry entry = (Entry) responseDocument.getRoot();
        String id = entry.getId().toString(); // id format: urn:uuid:0cb4d018-2ca8-4bab-8eff-8c77e4361985
        final String itemId = id.substring(id.lastIndexOf(":") + 1);
        // check out
        result = this.checkOutById(itemId);
        responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        final Entry responseEntry = (Entry) responseDocument.getRoot();
        id = responseEntry.getId().toString();
        final String workingCopyId = id.substring(id.lastIndexOf(":") + 1);
        // test list checked out files
        final Map<String, Object> parameters = Collections.emptyMap();
        final Connector connector = this.getAlfrescoConnector(parameters);
        connector.validateInputParameters();
        result = connector.execute();
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
        responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        final Feed feed = (Feed) responseDocument.getRoot();
        LOG.info("FeedTitle: " + feed.getTitle());
        assertTrue(feed.getEntries().size() >= 1);
        boolean isExist = false;
        System.out.println("Looking for id : " + id);
        for (final Entry entry1 : feed.getEntries()) {
            LOG.info("-------------- ENTRY ---------------");
            LOG.info("          Title : " + entry1.getTitle());
            LOG.info("             ID : " + entry1.getId());
            LOG.info("    ContentType : " + entry1.getContentType());
            LOG.info("ContentMimeType : " + entry1.getContentMimeType());
            if (entry1.getId().toString().equals(id)) {
                isExist = true;
            }
        }

        assertTrue(isExist);

        // cancel check out before delete to avoid server error
        this.cancelCheckOutById(workingCopyId);
        // delete files in alfresco server and local file system.
        this.deleteItemById(itemId);
        createdFile.delete();
    }

    @Override
    protected AlfrescoConnector getAlfrescoConnector(final Map<String, Object> specificInputs) {
        final AlfrescoConnector connectorWithParams = new ListCheckedOutFilesConnector();
        final Map<String, Object> defaultParameters = this.prepareGlobalAlfrescoConnectorInputs();
        defaultParameters.putAll(specificInputs);
        connectorWithParams.setInputParameters(defaultParameters);
        return connectorWithParams;
    }

}
