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

import org.bonitasoft.connectors.alfresco34.AlfrescoConnector;
import org.bonitasoft.connectors.alfresco34.CreateFolderByPathConnector;
import org.bonitasoft.connectors.alfresco34.DeleteFolderByPathConnector;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class AlfrescoDeleteFolderByPathTest extends AlfrescoConnectorTest {

    @Override
    protected Class<? extends AlfrescoConnectorTest> getConnectorTestClass() {
        return this.getClass();
    }

    @Cover(classes = { DeleteFolderByPathConnector.class }, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-580", exceptions = ConnectorException.class,
            story = "Tests the deletion of a non-existing folder with its path with Alfresco", keywords = { "Alfresco", "Connector", "Deletion", "Folder" })
    @Test(expected = ConnectorException.class)
    public void deleteNonExistingFolder() throws Exception {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        final String nonExistingFolder = "folder" + System.currentTimeMillis();
        inputs.put(DeleteFolderByPathConnector.FOLDER_PATH, nonExistingFolder);
        final Connector connector = this.getAlfrescoConnector(inputs);
        connector.validateInputParameters();
        connector.execute();
    }

    @Cover(classes = { DeleteFolderByPathConnector.class }, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-580",
            story = "Tests the deletion of a folder with its path with Alfresco", keywords = { "Alfresco", "Connector", "Deletion", "Folder" })
    @Test
    public void deleteFolderByPath() throws Exception {
        // prepare inputs
        final Map<String, Object> createInputs = new HashMap<String, Object>();
        final String parentPath = "/User%20Homes/dev/";
        final String newFolderName = "folder" + System.currentTimeMillis();
        final String fodlerDescription = "create folder by path test";
        createInputs.put(CreateFolderByPathConnector.PARENT_PATH, parentPath);
        createInputs.put(CreateFolderByPathConnector.NEW_FOLDERS_NAME, newFolderName);
        createInputs.put(CreateFolderByPathConnector.NEW_FOLDERS_DESCRIPTION, fodlerDescription);
        // create folder and check
        final Connector createFolderConnector = new CreateFolderByPathConnector();
        final Map<String, Object> globalParameters = this.prepareGlobalAlfrescoConnectorInputs();
        createInputs.putAll(globalParameters);
        createFolderConnector.setInputParameters(createInputs);
        createFolderConnector.validateInputParameters();
        Map<String, Object> result = createFolderConnector.execute();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("201", result.get(STATUS_CODE));

        // delete folder and check
        final Map<String, Object> deleteInputs = new HashMap<String, Object>();
        deleteInputs.put(DeleteFolderByPathConnector.FOLDER_PATH, parentPath + newFolderName);
        final Connector deleteFolderConnector = this.getAlfrescoConnector(deleteInputs);
        deleteFolderConnector.validateInputParameters();
        result = deleteFolderConnector.execute();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
    }

    @Override
    protected AlfrescoConnector getAlfrescoConnector(final Map<String, Object> specificInputs) {
        final DeleteFolderByPathConnector connectorWithParams = new DeleteFolderByPathConnector();
        final Map<String, Object> globalInputs = this.prepareGlobalAlfrescoConnectorInputs();
        globalInputs.putAll(specificInputs);
        connectorWithParams.setInputParameters(globalInputs);
        return connectorWithParams;
    }
}
