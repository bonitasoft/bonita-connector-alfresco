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
public class AlfrescoUpdateCheckedOutFileTest extends org.bonitasoft.connectors.alfresco34.AlfrescoConnectorTest {

    @Override
    protected Class<? extends org.bonitasoft.connectors.alfresco34.AlfrescoConnectorTest> getConnectorTestClass() {
        return this.getClass();
    }

    @SuppressWarnings("unchecked")
    @Cover(classes = { UpdateCheckedOutFileConnector.class }, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-580",
            story = "Tests the update of the checked out files with Alfresco", keywords = { "Alfresco", "Connector", "Update", "Checkout", "File" })
    @Test
    public void updateCheckedOutFile() throws Exception {
        // create file to upload first
        final String fileName1 = "file" + System.currentTimeMillis() + ".txt";
        final String parentPath = System.getProperty("user.home") + System.getProperty("file.separator");
        final String filePath1 = parentPath + fileName1;
        final File createdFile1 = this.createFile(filePath1);

        // upload file by path
        final Map<String, Object> uploadFileInputs = new HashMap<String, Object>();
        final String description = "test upload file by path";
        final String destinationFolder = "/User%20Homes/dev/";
        final String mimeType = "text/plain";
        Map<String, Object> result = this.uploadFile(fileName1, filePath1, uploadFileInputs, description, destinationFolder, mimeType);
        Document<Element> responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        assertNotNull(responseDocument);
        final Entry entry = (Entry) responseDocument.getRoot();
        final String id = entry.getId().toString(); // id format: urn:uuid:0cb4d018-2ca8-4bab-8eff-8c77e4361985
        final String itemId = id.substring(id.lastIndexOf(":") + 1);
        // check out
        result = this.checkOutById(itemId);
        responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        Entry responseEntry = (Entry) responseDocument.getRoot();
        final String checkedOutid = responseEntry.getId().toString();
        final String workingCopyId = checkedOutid.substring(checkedOutid.lastIndexOf(":") + 1);
        // test list checked out files
        final String fileName2 = "file" + System.currentTimeMillis() + ".txt";
        final String filePath2 = parentPath + fileName2;
        final File createdFile2 = this.createFile(filePath2);
        final Map<String, Object> updateCheckedOutFileConnectorInputs = new HashMap<String, Object>();
        updateCheckedOutFileConnectorInputs.put(UpdateCheckedOutFileConnector.CHECKED_OUT_FILE_ID, workingCopyId);
        updateCheckedOutFileConnectorInputs.put(UpdateCheckedOutFileConnector.FILE_ABSOLUTE_PATH, filePath2);
        updateCheckedOutFileConnectorInputs.put(UpdateCheckedOutFileConnector.DESCRIPTION, "updateCheckedOutFile");
        updateCheckedOutFileConnectorInputs.put(UpdateCheckedOutFileConnector.MIME_TYPE, "text/plain");
        final Connector connector = this.getAlfrescoConnector(updateCheckedOutFileConnectorInputs);
        connector.validateInputParameters();
        result = connector.execute();
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
        responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        responseEntry = (Entry) responseDocument.getRoot();
        assertEquals(checkedOutid, responseEntry.getId().toString());
        // check in to make update come into operation
        final Map<String, Object> checkInInputs = new HashMap<String, Object>();
        checkInInputs.put(CheckinConnector.CHECKED_OUT_FILE_ID, workingCopyId);
        checkInInputs.put(CheckinConnector.IS_MAJOR_VERSION, Boolean.FALSE);
        checkInInputs.put(CheckinConnector.CHECK_IN_COMMENTS, "test check in action");
        final AlfrescoConnector checkInConnector = new CheckinConnector();
        final Map<String, Object> defaultParameters = this.prepareGlobalAlfrescoConnectorInputs();
        checkInInputs.putAll(defaultParameters);
        checkInConnector.setInputParameters(checkInInputs);
        checkInConnector.validateInputParameters();
        result = checkInConnector.execute();
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
        // delete files in alfresco server and local file system.
        this.deleteItemById(itemId);
        createdFile1.delete();
        createdFile2.delete();
    }

    @Override
    protected AlfrescoConnector getAlfrescoConnector(final Map<String, Object> specificInputs) {
        final AlfrescoConnector connectorWithParams = new UpdateCheckedOutFileConnector();
        final Map<String, Object> globalParameters = this.prepareGlobalAlfrescoConnectorInputs();
        globalParameters.putAll(specificInputs);
        connectorWithParams.setInputParameters(globalParameters);
        return connectorWithParams;
    }

}
