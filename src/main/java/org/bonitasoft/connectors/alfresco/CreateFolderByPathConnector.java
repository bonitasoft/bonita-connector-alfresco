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
package org.bonitasoft.connectors.alfresco;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Jordi Anguela
 */
public class CreateFolderByPathConnector extends AlfrescoConnector {

    // input parameters
    public static final String PARENT_PATH = "parentPath";

    public static final String NEW_FOLDERS_NAME = "newFoldersName";

    public static final String NEW_FOLDERS_DESCRIPTION = "newFoldersDescription";

    private String parentPath;

    private String newFoldersName;

    private String newFoldersDescription;

    private Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @Override
    protected List<String> validateFunctionParameters() {
        this.fillInputParameters();

        final List<String> errors = new ArrayList<String>();
        if (this.parentPath == null || this.parentPath.trim().length() == 0) {
            errors.add("parentPath can not be null");
        }
        if (this.newFoldersName == null || this.newFoldersName.trim().length() == 0) {
            errors.add("newFoldersName can not be null");
        }
        return errors;
    }

    private void fillInputParameters() {
        this.parentPath = (String) this.getInputParameter(PARENT_PATH);
        LOGGER.info(PARENT_PATH + " " + parentPath);
        this.newFoldersName = (String) this.getInputParameter(NEW_FOLDERS_NAME);
        LOGGER.info(NEW_FOLDERS_NAME + " " + newFoldersName);
        this.newFoldersDescription = (String) this.getInputParameter(NEW_FOLDERS_DESCRIPTION);
        LOGGER.info(NEW_FOLDERS_DESCRIPTION + " " + newFoldersDescription);
    }

    @Override
    protected AlfrescoResponse executeFunction(final AlfrescoRestClient alfrescoClient) throws Exception {
        return alfrescoClient.createFolderByPath(this.parentPath, this.newFoldersName, this.newFoldersDescription);
    }

}
