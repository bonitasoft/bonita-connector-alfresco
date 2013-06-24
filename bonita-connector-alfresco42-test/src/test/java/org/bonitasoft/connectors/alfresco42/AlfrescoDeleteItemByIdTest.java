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

import java.util.HashMap;
import java.util.Map;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.bonitasoft.connectors.alfresco34.AlfrescoConnector;
import org.bonitasoft.connectors.alfresco34.DeleteItemByIdConnector;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Yanyan Liu
 */
public class AlfrescoDeleteItemByIdTest extends AlfrescoConnectorTest {

    @Override
    protected Class<? extends AlfrescoConnectorTest> getConnectorTestClass() {
        return this.getClass();
    }

    @Cover(classes = { DeleteItemByIdConnector.class }, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-580", exceptions = ConnectorException.class,
            story = "Tests the deletion of a non-existing folder with its id with Alfresco", keywords = { "Alfresco", "Connector", "Deletion", "Folder" })
    @Test
    public void deleteNonExistingItem() throws Exception {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        final String nonExistingItemId = String.valueOf(System.currentTimeMillis());
        inputs.put(DeleteItemByIdConnector.ITEM_ID, nonExistingItemId);
        final Connector connector = this.getAlfrescoConnector(inputs);
        connector.validateInputParameters();
        final Map<String, Object> result = connector.execute();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("CLIENT_ERROR", result.get(AlfrescoConnector.RESPONSE_TYPE));
        assertEquals("404", result.get(AlfrescoConnector.STATUS_CODE));
        System.out.println("Status code is : " + result.get(STATUS_CODE));
    }

    @Cover(classes = { DeleteItemByIdConnector.class }, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-580",
            story = "Tests the deletion of a folder with its id with Alfresco", keywords = { "Alfresco", "Connector", "Deletion", "Folder" })
    @Test
    public void deleteItemById() throws Exception {
        // create folder item
        final String parentPath = "/User%20Homes/dev/";
        final String newFolderName = "folder" + System.currentTimeMillis();
        final String fodlerDescription = "create folder by path test";
        Map<String, Object> result = this.createFolder(parentPath, newFolderName, fodlerDescription);
        @SuppressWarnings("unchecked")
        final Document<Element> responseDocument = (Document<Element>) result.get(AlfrescoConnector.RESPONSE_DOCUMENT);
        assertNotNull(responseDocument);
        final Entry entry = (Entry) responseDocument.getRoot();
        final String id = entry.getId().toString(); // urn:uuid:0cb4d018-2ca8-4bab-8eff-8c77e4361985
        final String itemId = id.substring(id.lastIndexOf(":") + 1);

        // delete folder item by id and check
        final Map<String, Object> deleteItemInputs = new HashMap<String, Object>();
        deleteItemInputs.put(DeleteItemByIdConnector.ITEM_ID, itemId);
        final Connector deleteItemConnector = this.getAlfrescoConnector(deleteItemInputs);
        deleteItemConnector.validateInputParameters();
        result = deleteItemConnector.execute();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
    }

    @Override
    protected AlfrescoConnector getAlfrescoConnector(final Map<String, Object> specificInputs) {
        final AlfrescoConnector connectorWithInputs = new DeleteItemByIdConnector();
        final Map<String, Object> globalInputs = this.prepareGlobalAlfrescoConnectorInputs();
        globalInputs.putAll(specificInputs);
        connectorWithInputs.setInputParameters(globalInputs);
        return connectorWithInputs;
    }

}
