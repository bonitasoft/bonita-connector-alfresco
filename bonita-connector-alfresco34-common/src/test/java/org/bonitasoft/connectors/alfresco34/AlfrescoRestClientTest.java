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
