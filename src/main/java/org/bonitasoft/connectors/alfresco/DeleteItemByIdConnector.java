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
public class DeleteItemByIdConnector extends AlfrescoConnector {

    // input parameters
    public static final String ITEM_ID = "itemId";

    private Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @Override
    protected AlfrescoResponse executeFunction(final AlfrescoRestClient alfrescoClient) throws Exception {
        final String itemId = (String) this.getInputParameter(ITEM_ID);
        return alfrescoClient.deleteItemById(itemId);
    }

    @Override
    protected List<String> validateFunctionParameters() {
        final String itemId = (String) this.getInputParameter(ITEM_ID);

        LOGGER.info(ITEM_ID + " " + itemId);

        if (itemId == null || itemId.trim().length() == 0) {
            final List<String> errors = new ArrayList<String>();
            errors.add("itemId can not be null");
            return errors;
        }
        return null;
    }
}
