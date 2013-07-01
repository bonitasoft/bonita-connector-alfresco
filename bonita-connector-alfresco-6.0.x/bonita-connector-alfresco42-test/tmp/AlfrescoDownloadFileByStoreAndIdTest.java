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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Feed;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.ConnectorException;
import org.bonitasoft.engine.exception.ConnectorValidationException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Yanyan Liu
 */
public class AlfrescoDownloadFileByStoreAndIdTest extends org.bonitasoft.connectors.alfresco34.AlfrescoConnectorTest {

    @Override
    protected Class<? extends org.bonitasoft.connectors.alfresco34.AlfrescoConnectorTest> getConnectorTestClass() {
        return this.getClass();
    }

    @SuppressWarnings("unchecked")
    @Cover(classes = { DownloadFileByStoreAndIdConnector.class }, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-580",
            story = "Tests the download of a  file with its store and id with Alfresco", keywords = { "Alfresco", "Connector", "Download", "File" })
    @Test
    public void downloadFileByStoreAndId() throws Exception {
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
        Document<Element> responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        assertNotNull(responseDocument);
        final Entry entry = (Entry) responseDocument.getRoot();
        final String id = entry.getId().toString(); // id format: urn:uuid:0cb4d018-2ca8-4bab-8eff-8c77e4361985
        final String itemId = id.substring(id.lastIndexOf(":") + 1);

        // make file in version
        result = this.fileVersion(itemId);
        responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        final Feed feed = (Feed) responseDocument.getRoot();
        final String versionedId = this.getVersionedId(feed);

        // down load file by version store and id test and check
        final Map<String, Object> downloadFileInputs = new HashMap<String, Object>();
        downloadFileInputs.put(DownloadFileByStoreAndIdConnector.FILE_ID, versionedId);
        final String versionStore = "versionStore://version2Store";
        downloadFileInputs.put(DownloadFileByStoreAndIdConnector.STORE, versionStore);
        downloadFileInputs.put(DownloadFileByStoreAndIdConnector.OUTPUT_FILE_NAME, fileName + "_download");
        downloadFileInputs.put(DownloadFileByStoreAndIdConnector.OUTPUT_FILE_FOLDER, parentPath);
        final Connector connector = this.getAlfrescoConnector(downloadFileInputs);
        connector.validateInputParameters();
        result = connector.execute();
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
        final File downloadedFile = new File(filePath + "_download");
        assertTrue(downloadedFile.exists());

        // delete files in alfresco server and local file system
        this.deleteItemById(itemId);
        createdFile.delete();
        downloadedFile.delete();
    }

    /**
     * @param itemId
     * @return
     * @throws ConnectorValidationException
     * @throws ConnectorException
     */
    public Map<String, Object> fileVersion(final String itemId) throws ConnectorValidationException, ConnectorException {
        Map<String, Object> result;
        final Map<String, Object> fileVersionInputs = new HashMap<String, Object>();
        fileVersionInputs.put(FileVersionsConnector.FILE_ID, itemId);
        final AlfrescoConnector fileVersionConnector = new FileVersionsConnector();
        final Map<String, Object> defaultParameters = this.prepareGlobalAlfrescoConnectorInputs();
        fileVersionInputs.putAll(defaultParameters);
        fileVersionConnector.setInputParameters(fileVersionInputs);
        fileVersionConnector.validateInputParameters();
        result = fileVersionConnector.execute();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
        return result;
    }

    /**
     * @param feed
     * @return
     */
    public String getVersionedId(final Feed feed) {
        ExtensibleElement objElement;
        ExtensibleElement propsElement;
        List<ExtensibleElement> listExtensions;
        String id;
        String objectId;
        String versionedId = null;
        for (final Entry entry1 : feed.getEntries()) {
            LOG.info("-------------- ENTRY ---------------");
            LOG.info("          Title : " + entry1.getTitle());
            LOG.info("             ID : " + entry1.getId());
            id = entry1.getId().toString();
            versionedId = id.substring(id.lastIndexOf(":") + 1);
            LOG.info("    ContentType : " + entry1.getContentType());
            LOG.info("ContentMimeType : " + entry1.getContentMimeType());

            objElement = entry1.getExtension(new QName(AlfrescoRestClient.NS_CMIS_RESTATOM, "object", AlfrescoRestClient.CMISRA));
            propsElement = objElement.getExtension(new QName(AlfrescoRestClient.NS_CMIS_CORE, "properties", AlfrescoRestClient.CMIS));

            listExtensions = propsElement.getExtensions(new QName(AlfrescoRestClient.NS_CMIS_CORE, "propertyId", AlfrescoRestClient.CMIS));
            for (final ExtensibleElement tmpExtension : listExtensions) {
                final String attValue = tmpExtension.getAttributeValue("propertyDefinitionId");
                if ("cmis:objectId".equals(attValue)) {
                    final Element valueElement = tmpExtension.getExtension(new QName(AlfrescoRestClient.NS_CMIS_CORE, "value", AlfrescoRestClient.CMIS));
                    objectId = valueElement.getText();
                    LOG.info("       objectId : " + objectId);
                }
            }
        }
        return versionedId;
    }

    @Override
    protected AlfrescoConnector getAlfrescoConnector(final Map<String, Object> specificInputs) {
        final AlfrescoConnector connectorWithInputs = new DownloadFileByStoreAndIdConnector();
        final Map<String, Object> globalInputs = this.prepareGlobalAlfrescoConnectorInputs();
        globalInputs.putAll(specificInputs);
        connectorWithInputs.setInputParameters(globalInputs);
        return connectorWithInputs;
    }

}
