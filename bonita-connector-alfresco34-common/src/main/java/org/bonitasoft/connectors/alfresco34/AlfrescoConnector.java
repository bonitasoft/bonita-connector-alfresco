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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * @author Jordi Anguela
 */
public abstract class AlfrescoConnector extends AbstractConnector {

    // global inputs
    protected static final String HOST = "host";

    protected static final String PORT = "port";

    protected static final String USERNAME = "username";

    protected static final String PASSWORD = "password";

    private String host;

    private Long port;

    private String username;

    private String password;

    // global outputs
    public static final String RESPONSE_DOCUMENT = "responseDocument";

    public static final String RESPONSE_TYPE = "responseType";

    public static final String STATUS_CODE = "statusCode";

    public static final String STATUS_TEXT = "statusText";

    public static final String STACK_TRACE = "stackTrace";

    // others
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("executing AlfrescoConnector with params: " + host + ":" + port.toString() + " " + username + ":" + password);
        }
        final AlfrescoRestClient alfrescoClient = new AlfrescoRestClient(host, port.toString(), username, password);
        try {
            final AlfrescoResponse response = executeFunction(alfrescoClient);
            getOutputParameters().put(RESPONSE_DOCUMENT, response.getDocument());
            getOutputParameters().put(RESPONSE_TYPE, response.getResponseType());
            getOutputParameters().put(STATUS_CODE, response.getStatusCode());
            getOutputParameters().put(STATUS_TEXT, response.getStatusText());
            getOutputParameters().put(STACK_TRACE, response.getStackTrace());
        } catch (final Exception e) {
            throw new ConnectorException(e);
        }
    }

    protected abstract AlfrescoResponse executeFunction(AlfrescoRestClient alfrescoClient) throws Exception;

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        fillGlobalInputParameters();
        final List<String> errors = new ArrayList<String>();
        if (port != null) {
            if (port < 0) {
                errors.add("proxyPort cannot be less than 0!");
            } else if (port > 65535) {
                errors.add("proxyPort cannot be greater than 65535!");
            }
        }
        final List<String> specificErrors = validateFunctionParameters();
        if (specificErrors != null) {
            errors.addAll(specificErrors);
        }
        if (!errors.isEmpty()) {
            throw new ConnectorValidationException(this, errors);
        }
    }

    private void fillGlobalInputParameters() {
        host = (String) this.getInputParameter(HOST);
        LOGGER.info(HOST + " " + host);
        port = (Long) this.getInputParameter(PORT);
        LOGGER.info(PORT + " " + port);
        username = (String) this.getInputParameter(USERNAME);
        LOGGER.info(USERNAME + " " + username);
        password = (String) this.getInputParameter(PASSWORD);
        LOGGER.info(PASSWORD + " " + password);
    }

    protected abstract List<String> validateFunctionParameters();

}
