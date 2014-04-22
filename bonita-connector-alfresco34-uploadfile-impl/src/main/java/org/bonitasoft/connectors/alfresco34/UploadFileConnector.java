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

import java.io.File;
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

    private String fileObject;

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
        this.fileObject = (String) this.getInputParameter(FILE_OBJECT);
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
    	ProcessAPI processAPI = getAPIAccessor().getProcessAPI();
    	long processInstanceId = getExecutionContext().getProcessInstanceId();
		Document document = processAPI.getLastDocument(processInstanceId, this.fileObject);
		if (document != null) {
	            final byte[] content = processAPI.getDocumentContent(document.getContentStorageId());
	            return alfrescoClient.uploadFileFromDocument(document, content, this.fileName, this.description, this.mimeType,
	                    this.destinationFolder);
		}else if(new File(fileObject).exists()){          
			return alfrescoClient.uploadFileByPath((String) this.fileObject, this.fileName, this.description, this.mimeType, this.destinationFolder);
        } else {
            throw new Exception("File to upload input parameter ("+fileObject+") is neither a document reference nor a a valid file path");
        }
    }

}
