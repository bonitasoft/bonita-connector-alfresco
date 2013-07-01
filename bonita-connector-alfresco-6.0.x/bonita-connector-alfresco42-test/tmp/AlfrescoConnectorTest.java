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
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.ConnectorException;
import org.bonitasoft.engine.exception.ConnectorValidationException;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * @author Yanyan Liu
 */
public abstract class AlfrescoConnectorTest {

    protected static final Logger LOG = Logger.getLogger(AlfrescoConnectorTest.class.getName());

    // global inputs
    protected static final String HOST = "host";

    protected static final String PORT = "port";

    protected static final String USERNAME = "username";

    protected static final String PASSWORD = "password";

    public static final String PASSWORD_VALUE = "bonita";

    public static final String USERNAME_VALUE = "dev";

    public static final long PORT_VALUE = 28080;

    public static final String HOST_VALUE = "192.168.1.132";

    protected static final String RESPONSE_DOCUMENT = "responseDocument";

    protected static final String RESPONSE_TYPE = "responseType";

    protected static final String STATUS_CODE = "statusCode";

    protected static final String STATUS_TEXT = "statusText";

    protected static final String STACK_TRACE = "stackTrace";

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOG.warning("==== Starting test: " + this.getClass().getName() + "" + d.getMethodName() + "() ====");
        }

        @Override
        public void failed(final Throwable e, final Description d) {
            LOG.warning("==== Failed test: " + this.getClass().getName() + "" + d.getMethodName() + "() ====");
        };

        @Override
        public void succeeded(final Description d) {
            LOG.warning("==== Succeeded test: " + this.getClass().getName() + "" + d.getMethodName() + "() ====");
        }

    };

    public Map<String, Object> prepareGlobalAlfrescoConnectorInputs() {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(HOST, HOST_VALUE);
        parameters.put(PORT, PORT_VALUE);
        parameters.put(USERNAME, USERNAME_VALUE);
        parameters.put(PASSWORD, PASSWORD_VALUE);
        return parameters;
    }

    protected abstract Class<? extends AlfrescoConnectorTest> getConnectorTestClass();

    protected abstract AlfrescoConnector getAlfrescoConnector(Map<String, Object> specificInputs);

    public File createFile(final String filePath) throws IOException {
        final File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(filePath, true);
            writer.write("Hello Bonita!");
            writer.close();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return file;
    }

    public void checkStatus(final String expected, final Object objStatus) {
        boolean statusInFrench = true;
        if (statusInFrench) {
            String strStatus = (String) objStatus;
            if (expected.startsWith("Cr")) {
                assertEquals("Cr", strStatus.substring(0, 2));
            }
            else if (expected.equals("Not Found")) {
                assertEquals("Introuvable", strStatus);
            }
            else {
                assertEquals(true, false);
            }
        }
        else {
            assertEquals(expected, objStatus);
        }
    }

    public Map<String, Object> createFolder(final String parentPath, final String newFolderName, final String fodlerDescription)
            throws ConnectorValidationException, ConnectorException {
        final Map<String, Object> createInputs = new HashMap<String, Object>();
        createInputs.put(CreateFolderByPathConnector.PARENT_PATH, parentPath);
        createInputs.put(CreateFolderByPathConnector.NEW_FOLDERS_NAME, newFolderName);
        createInputs.put(CreateFolderByPathConnector.NEW_FOLDERS_DESCRIPTION, fodlerDescription);
        // create folder
        final Connector createFolderConnector = new CreateFolderByPathConnector();
        final Map<String, Object> globalParameters = this.prepareGlobalAlfrescoConnectorInputs();
        createInputs.putAll(globalParameters);
        createFolderConnector.setInputParameters(createInputs);
        createFolderConnector.validateInputParameters();
        final Map<String, Object> result = createFolderConnector.execute();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("201", result.get(STATUS_CODE));
        return result;
    }

    /**
     * @param folderPath
     * @throws org.bonitasoft.engine.exception.ConnectorValidationException
     * @throws org.bonitasoft.engine.exception.ConnectorException
     */
    public void deleteFolderByPath(final String folderPath) throws ConnectorValidationException, ConnectorException {
        Map<String, Object> result;
        final Map<String, Object> deleteInputs = new HashMap<String, Object>();
        deleteInputs.put(DeleteFolderByPathConnector.FOLDER_PATH, folderPath);
        final DeleteFolderByPathConnector deleteFolderConnector = new DeleteFolderByPathConnector();
        final Map<String, Object> defaultParameters = this.prepareGlobalAlfrescoConnectorInputs();
        defaultParameters.putAll(deleteInputs);
        deleteFolderConnector.setInputParameters(defaultParameters);
        deleteFolderConnector.validateInputParameters();
        result = deleteFolderConnector.execute();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
    }

    /**
     * @param fileName
     * @param filePath
     * @param uploadFileInputs
     * @param description
     * @param destinationFolder
     * @param mimeType
     * @return
     * @throws org.bonitasoft.engine.exception.ConnectorValidationException
     * @throws org.bonitasoft.engine.exception.ConnectorException
     */
    public Map<String, Object> uploadFile(final String fileName, final String filePath, final Map<String, Object> uploadFileInputs, final String description,
            final String destinationFolder, final String mimeType) throws ConnectorValidationException, ConnectorException {
        uploadFileInputs.put(UploadFileConnector.FILE_OBJECT, filePath);
        uploadFileInputs.put(UploadFileConnector.FILE_NAME, fileName);
        uploadFileInputs.put(UploadFileConnector.DESCRIPTION, description);
        uploadFileInputs.put(UploadFileConnector.DESTINATION_FOLDER, destinationFolder);
        uploadFileInputs.put(UploadFileConnector.MIME_TYPE, mimeType);
        final AlfrescoConnector uploadFileConnector = new UploadFileConnector();
        final Map<String, Object> globalParameters = this.prepareGlobalAlfrescoConnectorInputs();
        uploadFileInputs.putAll(globalParameters);
        uploadFileConnector.setInputParameters(uploadFileInputs);
        uploadFileConnector.validateInputParameters();
        final Map<String, Object> result = uploadFileConnector.execute();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("201", result.get(STATUS_CODE));
        return result;
    }

    /**
     * @param itemId
     * @throws org.bonitasoft.engine.exception.ConnectorValidationException
     * @throws org.bonitasoft.engine.exception.ConnectorException
     */
    public void deleteItemById(final String itemId) throws ConnectorValidationException, ConnectorException {
        Map<String, Object> result;
        final Map<String, Object> deleteItemInputs = new HashMap<String, Object>();
        deleteItemInputs.put(DeleteItemByIdConnector.ITEM_ID, itemId);
        final AlfrescoConnector deleteItemConnector = new DeleteItemByIdConnector();
        final Map<String, Object> globalInputs = this.prepareGlobalAlfrescoConnectorInputs();
        globalInputs.putAll(deleteItemInputs);
        deleteItemConnector.setInputParameters(globalInputs);
        deleteItemConnector.validateInputParameters();
        result = deleteItemConnector.execute();
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
    }

    /**
     * @param itemId
     * @return
     * @throws org.bonitasoft.engine.exception.ConnectorValidationException
     * @throws org.bonitasoft.engine.exception.ConnectorException
     */
    public Map<String, Object> checkOutById(final String itemId) throws ConnectorValidationException, ConnectorException {
        Map<String, Object> result;
        final Map<String, Object> checkOutInputs = new HashMap<String, Object>();
        checkOutInputs.put(CheckoutConnector.FILE_ID, itemId);
        final Connector checkOutConnector = new CheckoutConnector();
        final Map<String, Object> defaultParameters = this.prepareGlobalAlfrescoConnectorInputs();
        checkOutInputs.putAll(defaultParameters);
        checkOutConnector.setInputParameters(checkOutInputs);
        checkOutConnector.validateInputParameters();
        result = checkOutConnector.execute();
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
        assertEquals("201", result.get(STATUS_CODE));
        return result;
    }

    /**
     * @param workingCopyId
     * @return
     * @throws org.bonitasoft.engine.exception.ConnectorValidationException
     * @throws org.bonitasoft.engine.exception.ConnectorException
     */
    public Map<String, Object> cancelCheckOutById(final String workingCopyId) throws ConnectorValidationException, ConnectorException {
        Map<String, Object> result;
        final Map<String, Object> checkOutInputs = new HashMap<String, Object>();
        checkOutInputs.put(CancelCheckoutConnector.FILE_ID, workingCopyId);
        final Connector cancelCheckOutConnector = new CancelCheckoutConnector();
        final Map<String, Object> defaultParameters = this.prepareGlobalAlfrescoConnectorInputs();
        checkOutInputs.putAll(defaultParameters);
        cancelCheckOutConnector.setInputParameters(checkOutInputs);
        cancelCheckOutConnector.validateInputParameters();
        result = cancelCheckOutConnector.execute();
        assertEquals("SUCCESS", result.get(AlfrescoConnector.RESPONSE_TYPE));
        return result;
    }
}
