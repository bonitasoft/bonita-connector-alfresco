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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.Response.ResponseType;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;

/**
 * @author Jordi Anguela, Yanyan Liu
 */
public class AlfrescoRestClient {

    private static Logger LOGGER = Logger.getLogger(AlfrescoRestClient.class.getName());

    public static String NS_CMIS_RESTATOM = "http://docs.oasis-open.org/ns/cmis/restatom/200908/";

    public static String NS_CMIS_CORE = "http://docs.oasis-open.org/ns/cmis/core/200908/";

    public static String CMISRA = "cmisra";

    public static String CMIS = "cmis";

    private final String server; // host + port

    private final String username;

    private final String password;

    public AlfrescoRestClient(final String host, final String port, final String user, final String passw) {
        this.username = user;
        this.password = passw;
        this.server = "http://" + host + ":" + port;
    }

    /**
     * STATUS 201 - Folder Created STATUS 500 - Server error (may be caused by: Folder already exists)
     * 
     * @param parentPath
     * @param newFoldersName
     * @param newFoldersDescription
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse createFolderByPath(final String parentPath, final String newFoldersName, final String newFoldersDescription) throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("createFolderByPath parentPath=" + parentPath + " newFoldersName=" + newFoldersName + " newFoldersDescription=" + newFoldersDescription);
        }

        // Build the input Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();
        entry.setTitle(newFoldersName);
        entry.setSummary(newFoldersDescription);

        final ExtensibleElement objElement = (ExtensibleElement) entry.addExtension(NS_CMIS_RESTATOM, "object", CMISRA);
        final ExtensibleElement propsElement = objElement.addExtension(NS_CMIS_CORE, "properties", CMIS);
        final ExtensibleElement stringElement = propsElement.addExtension(NS_CMIS_CORE, "propertyId", CMIS);
        stringElement.setAttributeValue("propertyDefinitionId", "cmis:objectTypeId");
        final Element valueElement = stringElement.addExtension(NS_CMIS_CORE, "value", CMIS);
        valueElement.setText("cmis:folder");

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/p" + parentPath + "/children";
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("POST " + uri);
        }

        final ClientResponse clientResponse = client.post(uri, entry, options);
        final AlfrescoResponse alfResponse = this.parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 200: SUCCESS
     * 
     * @param folderPath
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse listFolderByPath(final String folderPath) throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("listFolderByPath folderPath=" + folderPath);
        }

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/p" + folderPath + "/children";
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("GET " + uri);
        }

        final ClientResponse clientResponse = client.get(uri, options);
        final AlfrescoResponse alfResponse = this.parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 204 : SUCCESS STATUS 404 : CLIENT_ERROR - Not found
     * 
     * @param folderPath
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse deleteFolderByPath(final String folderPath) throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("deleteFolderByPath folderPath=" + folderPath);
        }

        // use recursive algorithm to delete subFolder
        final AlfrescoResponse response = this.listFolderByPath(folderPath);
        if (ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
            final Document<Element> doc = response.getDocument();
            final Feed feed = (Feed) doc.getRoot();
            for (final Entry entry : feed.getEntries()) {
                final String title = entry.getTitle();
                final String subFolderPath = folderPath + "/" + title;
                this.deleteFolderByPath(subFolderPath);
            }
        } else {
            throw new IOException();
        }

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/p" + folderPath;
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("DELETE " + uri);
        }

        final ClientResponse clientResponse = client.delete(uri, options);
        final AlfrescoResponse alfResponse = this.parseResponse(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 200 : SUCCESS STATUS 404 : CLIENT_ERROR - Not found
     * 
     * @param fileId
     * @param outputFileFolder
     * @param outputFileName
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse downloadFileById(final String fileId, final String outputFileFolder, final String outputFileName) throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("downloadFileById fileId=" + fileId + " outputFileFolder=" + outputFileFolder + " outputFileName=" + outputFileName);
        }

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/service/cmis/i/" + fileId + "/content";
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("GET " + uri);
        }

        final ClientResponse clientResponse = client.get(uri, options);
        final AlfrescoResponse alfResponse = this.parseResponseAsOutputFile(clientResponse, outputFileFolder, outputFileName);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 200: SUCCESS STATUS 404 : CLIENT_ERROR - Not found
     * 
     * @param fileId
     * @param outputFileFolder
     * @param outputFileName
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse downloadFileByStoreAndId(final String store, final String fileId, final String outputFileFolder, final String outputFileName)
            throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("downloadFileByStoreAndId store=" + store + " fileId=" + fileId + " outputFileFolder=" + outputFileFolder + " outputFileName="
                    + outputFileName);
        }

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/service/cmis/s/" + store + "/i/" + fileId + "/content";
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("GET " + uri);
        }

        final ClientResponse clientResponse = client.get(uri, options);
        final AlfrescoResponse alfResponse = this.parseResponseAsOutputFile(clientResponse, outputFileFolder, outputFileName);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * Uploads the file at the given path in Alfresco STATUS 201 : Created STATUS 404 : Not found - May be caused by:
     * Destination directory not found STATUS 500 : Internal server error - May be caused by: File already exist
     * 
     * @param fileAbsolutePath
     * @param description
     * @param mimeType
     * @param destinationFolder
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse uploadFileByPath(final String fileAbsolutePath, final String fileName, final String description, final String mimeType,
            final String destinationFolder) throws IOException {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("uploadFileByPath fileAbsolutePath=" + fileAbsolutePath + " fileName=" + fileName + " description=" + description + " mimeType="
                    + mimeType + " destinationFolder=" + destinationFolder);
        }
        if ("text/plain".equalsIgnoreCase(mimeType)) {
            // Get file content
            final File fileToUpload = new File(fileAbsolutePath);
            final byte[] fileBytes = getBytesFromFile(fileToUpload);
            // Upload file
            return this.uploadFile(fileBytes, fileName, description, mimeType, destinationFolder);
        } else {
            // Upload stream
            final FileInputStream inputStream = new FileInputStream(fileAbsolutePath);
            return this.uploadFile(inputStream, fileName, description, mimeType, destinationFolder);
        }

    }

    /**
     * Uploads the given Document in Alfresco STATUS 201 : Created STATUS 404 : Not found - May be caused by: Destination
     * directory not found STATUS 500 : Internal server error - May be caused by: File already exist
     * 
     * @param document
     * @param fileBytes
     * @param description
     * @param mimeType
     * @param destinationFolder
     * @return AlfrescoResponse
     * @throws IOException
     * @throws Exception
     */
    public AlfrescoResponse uploadFileFromDocument(final org.bonitasoft.engine.bpm.document.Document document, final byte[] fileBytes,
            final String fileName, final String description, final String mimeType, final String destinationFolder) throws IOException, Exception {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("uploadFileFromDocument documentName=" + document.getName() + " fileName=" + fileName + " description=" + description + " mimeType="
                    + mimeType + " destinationFolder=" + destinationFolder);
        }

        if ("text/plain".equalsIgnoreCase(mimeType)) {
            // Upload file
            return this.uploadFile(fileBytes, fileName, description, mimeType, destinationFolder);
        } else {
            // Upload stream
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
            return this.uploadFile(inputStream, fileName, description, mimeType, destinationFolder);
        }

    }

    /**
     * upload non test/plain MimeType file
     * 
     * @param inputStream
     * @param fileName
     * @param description
     * @param mimeType
     * @param destinationFolder
     * @return
     */
    private AlfrescoResponse uploadFile(final InputStream inputStream, final String fileName, final String description, final String mimeType,
            final String destinationFolder) {
        // Build the input Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();
        entry.setTitle(fileName);
        entry.setSummary(description);
        entry.setContent(inputStream, mimeType);

        final ExtensibleElement objElement = (ExtensibleElement) entry.addExtension(NS_CMIS_CORE, "object", CMIS);
        final ExtensibleElement propsElement = objElement.addExtension(NS_CMIS_CORE, "properties", CMIS);
        final ExtensibleElement stringElement = propsElement.addExtension(NS_CMIS_CORE, "propertyId", CMIS);
        stringElement.setAttributeValue("cmis:name", "ObjectTypeId");
        final Element valueElement = stringElement.addExtension(NS_CMIS_CORE, "value", CMIS);
        valueElement.setText("document"); // This could be changed as an input parameter

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/p" + destinationFolder + "/children";
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("POST " + uri);
        }

        final ClientResponse clientResponse = client.post(uri, entry, options);
        final AlfrescoResponse alfResponse = this.parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * Uploads the given file bytes to an Alfresco file<br/>
     * STATUS 201 : Created STATUS 404 : Not found - May be caused by: Destination directory not found STATUS 500 :
     * Internal server error - May be caused by: File already exist
     * 
     * @param fileBytes
     * @param fileName
     * @param description
     * @param mimeType
     * @param destinationFolder
     * @return
     * @throws IOException
     */
    private AlfrescoResponse uploadFile(final byte[] fileBytes, final String fileName, final String description, final String mimeType,
            final String destinationFolder) throws IOException {
        // String encodedFileString = new String(fileBytes);
        String encodedFileString = null;
        if (fileBytes != null) {
            encodedFileString = new String(fileBytes);
        }
        // Build the input Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();
        entry.setTitle(fileName);
        entry.setSummary(description);
        entry.setContent(encodedFileString, mimeType);

        final ExtensibleElement objElement = (ExtensibleElement) entry.addExtension(NS_CMIS_CORE, "object", CMIS);
        final ExtensibleElement propsElement = objElement.addExtension(NS_CMIS_CORE, "properties", CMIS);
        final ExtensibleElement stringElement = propsElement.addExtension(NS_CMIS_CORE, "propertyId", CMIS);
        stringElement.setAttributeValue("cmis:name", "ObjectTypeId");
        final Element valueElement = stringElement.addExtension(NS_CMIS_CORE, "value", CMIS);
        valueElement.setText("document"); // This could be changed as an input parameter

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/p" + destinationFolder + "/children";
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("POST " + uri);
        }

        final ClientResponse clientResponse = client.post(uri, entry, options);
        final AlfrescoResponse alfResponse = this.parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 204 : SUCCESS STATUS 404 : CLIENT_ERROR - Not found
     * 
     * @param itemId
     *            if a folderId is specified it also deletes its contents
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse deleteItemById(final String itemId) throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("deleteItemById itemId=" + itemId);
        }

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/i/" + itemId;
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("DELETE " + uri);
        }

        final ClientResponse clientResponse = client.delete(uri, options);
        final AlfrescoResponse alfResponse = this.parseResponse(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 201 : Created - (working copy created) STATUS 404 : CLIENT_ERROR - File not found STATUS 400 : CLIENT_ERROR
     * - Bad request, file already checked-out
     * 
     * @param fileId
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse checkout(final String fileId) throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("checkout fileId=" + fileId);
        }

        // Build the input Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();

        final ExtensibleElement objElement = (ExtensibleElement) entry.addExtension(NS_CMIS_RESTATOM, "object", CMISRA);
        final ExtensibleElement propsElement = objElement.addExtension(NS_CMIS_CORE, "properties", CMIS);
        final ExtensibleElement stringElement = propsElement.addExtension(NS_CMIS_CORE, "propertyId", CMIS);
        stringElement.setAttributeValue("propertyDefinitionId", "cmis:objectId");
        final Element valueElement = stringElement.addExtension(NS_CMIS_CORE, "value", CMIS);
        valueElement.setText("workspace://SpacesStore/" + fileId);

        // Post it
        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/checkedout";
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("POST " + uri);
        }

        final ClientResponse clientResponse = client.post(uri, entry, options);
        final AlfrescoResponse alfResponse = this.parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 200: SUCCESS
     * 
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse listCheckedOutFiles() throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("listCheckedOutFiles");
        }

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/checkedout";
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("GET " + uri);
        }

        final ClientResponse clientResponse = client.get(uri, options);
        final AlfrescoResponse alfResponse = this.parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 204 : SUCCESS STATUS 404 : CLIENT_ERROR - File not found STATUS 500 : SERVER_ERROR - (may be caused because
     * the file is not checked-out)
     * 
     * @param fileId
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse cancelCheckout(final String fileId) throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("cancelCheckout fileId=" + fileId);
        }

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/pwc/i/" + fileId;
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("DELETE " + uri);
        }

        final ClientResponse clientResponse = client.delete(uri, options);
        final AlfrescoResponse alfResponse = this.parseResponse(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 200 : SUCCESS STATUS 500 : Internal server error - May be caused by: Duplicate child name not allowed (you
     * are trying to use the original file name instead of the checked-out file name) Example: original file name:
     * demo.txt checked-out file name: demo (Working copy).txt
     * 
     * @param fileAbsolutePath
     * @param description
     * @param mimeType
     * @param checkedOutFileId
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse updateCheckedOutFile(final String fileAbsolutePath, final String description, final String mimeType, final String checkedOutFileId)
            throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("updateCheckedOutFile fileAbsolutePath=" + fileAbsolutePath + " description=" + description + " mimeType=" + mimeType
                    + " checkedOutFileId=" + checkedOutFileId);
        }

        final File fileToUpload = new File(fileAbsolutePath);

        final byte[] fileBytes = getBytesFromFile(fileToUpload);
        // char[] encodedFile = Base64Coder.encode(fileBytes); This will cause messy code issue.
        final String encodedFileString = new String(fileBytes);

        final String fileName = fileToUpload.getName();

        // Build the input Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();
        entry.setTitle(fileName);
        entry.setSummary(description);
        entry.setContent(encodedFileString, mimeType);
        // entry.setContent(new FileInputStream(fileToUpload), mimeType);

        final ExtensibleElement objElement = (ExtensibleElement) entry.addExtension(NS_CMIS_CORE, "object", CMIS);
        final ExtensibleElement propsElement = objElement.addExtension(NS_CMIS_CORE, "properties", CMIS);
        final ExtensibleElement stringElement = propsElement.addExtension(NS_CMIS_CORE, "propertyId", CMIS);
        stringElement.setAttributeValue("cmis:name", "ObjectTypeId");
        final Element valueElement = stringElement.addExtension(NS_CMIS_CORE, "value", CMIS);
        valueElement.setText("document"); // This could be changed as an input parameter

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/i/" + checkedOutFileId;
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("PUT " + uri);
        }

        final ClientResponse clientResponse = client.put(uri, entry, options);
        final AlfrescoResponse alfResponse = this.parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 200 : SUCCESS STATUS 404 : CLIENT_ERROR - File not found
     * 
     * @param checkedOutFileId
     * @param isMajorVersion
     * @param checkinComments
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse checkin(final String checkedOutFileId, final boolean isMajorVersion, String checkinComments) throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("checkin checkedOutFileId=" + checkedOutFileId + " isMajorVersion=" + isMajorVersion + " checkinComments=" + checkinComments);
        }

        // Replace white spaces from the URI
        checkinComments = checkinComments.replace(" ", "%20");

        // Create an empty Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/pwc/i/" + checkedOutFileId + "?checkin=true&major=" + isMajorVersion + "&checkinComment="
                + checkinComments;
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("PUT " + uri);
        }

        final ClientResponse clientResponse = client.put(uri, entry, options);
        final AlfrescoResponse alfResponse = this.parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 200: SUCCESS STATUS 404 : CLIENT_ERROR - File not found
     * 
     * @param fileId
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse fileVersions(final String fileId) throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("fileVersions fileId=" + fileId);
        }

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = this.server + "/alfresco/s/cmis/i/" + fileId + "/versions";
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("GET " + uri);
        }

        final ClientResponse clientResponse = client.get(uri, options);
        final AlfrescoResponse alfResponse = this.parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * Parse ClientResponse
     * 
     * @param response
     * @return AlfrescoResponse
     */
    private AlfrescoResponse parseResponse(final ClientResponse response) {

        AlfrescoResponse alfResponse;

        String responseType = "";
        if (response.getType() != null) {
            responseType = response.getType().toString();
        }
        final String statusCode = String.valueOf(response.getStatus());
        final String statusText = response.getStatusText();

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Response type : " + responseType);
            LOGGER.info("Status code is: " + statusCode);
            LOGGER.info("Status text is: " + statusText);
        }

        alfResponse = new AlfrescoResponse(responseType, statusCode, statusText);

        if (ResponseType.SUCCESS != response.getType()) {
            // printStackTrace
            InputStream inputStream;
            try {
                inputStream = response.getInputStream();

                final char[] buffer = new char[0x10000];
                final StringBuilder stackTrace = new StringBuilder();
                final Reader in = new InputStreamReader(inputStream, "UTF-8");
                int read;
                do {
                    read = in.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        stackTrace.append(buffer, 0, read);
                    }
                } while (read >= 0);
                in.close();

                alfResponse.setStackTrace(stackTrace.toString());
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return alfResponse;
    }

    /**
     * Parse ClientResponse
     * 
     * @param response
     * @return AlfrescoResponse
     */
    @SuppressWarnings("unchecked")
    private AlfrescoResponse parseResponseWithDocument(final ClientResponse response) {

        final AlfrescoResponse alfResponse = this.parseResponse(response);

        if (ResponseType.SUCCESS == response.getType()) {
            final Document<Element> document = response.getDocument();
            if (document != null) {
                alfResponse.setDocument((Document<Element>) document.clone());
            }
        }
        return alfResponse;
    }

    /**
     * Parse ClientResponse
     * 
     * @param response
     * @return
     * @throws IOException
     */
    private AlfrescoResponse parseResponseAsOutputFile(final ClientResponse response, final String outputFileFolder, final String outputFileName)
            throws IOException {

        final AlfrescoResponse alfResponse = this.parseResponse(response);

        if (ResponseType.SUCCESS == response.getType()) {
            if (response.getContentLength() > 0) {
                final InputStream inputStream = response.getInputStream();
                final File responseFile = new File(outputFileFolder + outputFileName);
                final OutputStream outputStream = new FileOutputStream(responseFile);
                final byte buf[] = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();
            }
        }
        return alfResponse;
    }

    private static byte[] getBytesFromFile(final File file) throws IOException {
        final InputStream is = new FileInputStream(file);

        // Get the size of the file
        final long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
            is.close();
            throw new IOException("File too long");
        }

        // Create the byte array to hold the data
        final byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            is.close();
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

}
