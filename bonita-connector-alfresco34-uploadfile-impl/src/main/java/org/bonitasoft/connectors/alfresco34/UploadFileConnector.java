/**
 * Copyright (C) 2009-2012 BonitaSoft S.A.
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;

/**
 * @author Jordi Anguela, Yanyan Liu
 */
public class UploadFileConnector extends AlfrescoConnector {

    // input parameters
    public static final String FILE_OBJECT = "fileObject";

    public static final String FILE_NAME = "fileName";

    public static final String DESCRIPTION = "description";

    public static final String MIME_TYPE = "mimeType";

    public static final String DESTINATION_FOLDER = "destinationFolder";

    private Object fileObject;

    private String fileName;

    private String description;

    private String mimeType;

    private String destinationFolder;

    private Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @Override
    protected List<String> validateFunctionParameters() {
        this.fillInputParameters();
        final List<String> errors = new ArrayList<String>();
        if (this.fileObject == null) {
            errors.add("fileObject can not be null");
        }
        if (this.mimeType == null || this.mimeType.trim().length() == 0) {
            errors.add("mimeType can not be null");
        }
        if (this.destinationFolder == null || this.destinationFolder.trim().length() == 0) {
            errors.add("destinationFolder can not be null");
        }
        return errors;
    }

    private void fillInputParameters() {
        this.fileObject = this.getInputParameter(FILE_OBJECT);
        LOGGER.info(FILE_OBJECT + " " + fileObject);
        this.fileName = (String) this.getInputParameter(FILE_NAME);
        LOGGER.info(FILE_NAME + " " + fileName);
        this.description = (String) this.getInputParameter(DESCRIPTION);
        LOGGER.info(DESCRIPTION + " " + description);
        this.mimeType = (String) this.getInputParameter(MIME_TYPE);
        LOGGER.info(DESTINATION_FOLDER + " " + destinationFolder);
        this.destinationFolder = (String) this.getInputParameter(DESTINATION_FOLDER);
    }

    @Override
    protected AlfrescoResponse executeFunction(final AlfrescoRestClient alfrescoClient) throws Exception {
        if (this.fileObject instanceof String) {
            return alfrescoClient.uploadFileByPath((String) this.fileObject, this.fileName, this.description, this.mimeType, this.destinationFolder);
        } else if (this.fileObject instanceof Document) {
            final Document document = (Document) this.fileObject;
            final ProcessAPI processAPI = this.getAPIAccessor().getProcessAPI();
            final byte[] content = processAPI.getDocumentContent(document.getContentStorageId());
            return alfrescoClient.uploadFileFromDocument((Document) this.fileObject, content, this.fileName, this.description, this.mimeType,
                    this.destinationFolder);
        } else {
            throw new Exception("Unsupported class for file upload: " + this.fileObject.getClass().getName()
                    + ". Supported classes: String, AttachmentInstance, Document");
        }
    }

}
