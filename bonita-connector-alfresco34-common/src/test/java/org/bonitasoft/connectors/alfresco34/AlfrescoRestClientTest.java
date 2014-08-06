/**
 * Copyright (C) 2014 Bonitasoft S.A.
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AlfrescoRestClientTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void initialize_server_url_with_valid_protocol() throws Exception {
        AlfrescoRestClient alfrescoRestClient = new AlfrescoRestClient("localhost", "8080", "", "");
        assertThat(alfrescoRestClient.getServerURL()).isEqualTo("http://localhost:8080");

        alfrescoRestClient = new AlfrescoRestClient("https://localhost", "8080", "", "");
        assertThat(alfrescoRestClient.getServerURL()).isEqualTo("https://localhost:8080");

        alfrescoRestClient = new AlfrescoRestClient("unknown://localhost", "8080", "", "");
        assertThat(alfrescoRestClient.getServerURL()).isEqualTo("http://localhost:8080");

        alfrescoRestClient = new AlfrescoRestClient("file://localhost", "8080", "", "");
        assertThat(alfrescoRestClient.getServerURL()).isEqualTo("file://localhost:8080");
    }

}
