/**
 * Created by Jordi Anguela
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

package org.bonitasoft.connectors.alfresco34.common;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.Response.ResponseType;
import org.bonitasoft.connectors.alfresco34.AlfrescoResponse;
import org.bonitasoft.connectors.alfresco34.AlfrescoRestClient;

public class TestAlfrescoConnector {

    private static final String HOST = "192.168.1.132";

    private static final String PORT = "28080";

    private static final String USER = "dev";

    private static final String PASSWORD = "bonita";

    AlfrescoRestClient alfCon = new AlfrescoRestClient(HOST, PORT, USER, PASSWORD);

    AlfrescoResponse response = null;

    public void createFolderByPath(final String parentFolder, final String folderName, final String folderDescription) {
        try {
            this.response = this.alfCon.createFolderByPath(parentFolder, folderName, folderDescription);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                final Document<Element> doc = this.response.getDocument();
                final Entry responseEntry = (Entry) doc.getRoot();
                System.out.println("Title  : " + responseEntry.getTitle()); // prints newFoldersName
                System.out.println("Content: " + responseEntry.getContent()); // prints newFoldersID
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void listFolderByPath(final String folderPath) {
        try {
            this.response = this.alfCon.listFolderByPath(folderPath);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                final Document<Element> doc = this.response.getDocument();
                final Feed feed = (Feed) doc.getRoot();
                System.out.println("FeedTitle: " + feed.getTitle());

                for (final Entry entry : feed.getEntries()) {
                    System.out.println("-------------- ENTRY ---------------");
                    System.out.println("          Title : " + entry.getTitle());
                    System.out.println("             ID : " + entry.getId());
                    System.out.println("    ContentType : " + entry.getContentType());
                    System.out.println("ContentMimeType : " + entry.getContentMimeType());
                }
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFolderByPath(final String folderPath) {
        try {
            this.response = this.alfCon.deleteFolderByPath(folderPath);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                // no content returned
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadFileById(final String fileId, final String outputFileFolder, final String outputFileName) {
        try {
            this.response = this.alfCon.downloadFileById(fileId, outputFileFolder, outputFileName);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                // check that files was correctly download
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(final Object fileObject, final String fileName, final String description, final String mimeType, final String destinationFolder) {
        try {

            this.response = this.alfCon.uploadFileByPath((String) fileObject, fileName, description, mimeType, destinationFolder);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                final Document<Element> doc = this.response.getDocument();
                final Entry responseEntry = (Entry) doc.getRoot();
                System.out.println("Title: " + responseEntry.getTitle());
                System.out.println("ID   : " + responseEntry.getId());
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteItemById(final String itemId) {
        try {
            this.response = this.alfCon.deleteItemById(itemId);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                // no content returned
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void checkout(final String fileId) {
        try {
            this.response = this.alfCon.checkout(fileId);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                final Document<Element> doc = this.response.getDocument();
                final Entry responseEntry = (Entry) doc.getRoot();
                System.out.println("Title: " + responseEntry.getTitle());
                System.out.println("ID   : " + responseEntry.getId());
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void listCheckedOutFiles() {
        try {
            this.response = this.alfCon.listCheckedOutFiles();

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                final Document<Element> doc = this.response.getDocument();
                final Feed feed = (Feed) doc.getRoot();
                System.out.println("FeedTitle: " + feed.getTitle());

                for (final Entry entry : feed.getEntries()) {
                    System.out.println("-------------- ENTRY ---------------");
                    System.out.println("          Title : " + entry.getTitle());
                    System.out.println("             ID : " + entry.getId());
                    System.out.println("    ContentType : " + entry.getContentType());
                    System.out.println("ContentMimeType : " + entry.getContentMimeType());
                }
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelCheckout(final String fileId) {
        try {
            this.response = this.alfCon.cancelCheckout(fileId);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                // no content
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void updateCheckedOutFile(final String fileAbsolutePath, final String description, final String mimeType, final String checkedOutFileId) {
        try {
            this.response = this.alfCon.updateCheckedOutFile(fileAbsolutePath, description, mimeType, checkedOutFileId);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                final Document<Element> doc = this.response.getDocument();
                final Entry responseEntry = (Entry) doc.getRoot();
                System.out.println("Title: " + responseEntry.getTitle());
                System.out.println("ID   : " + responseEntry.getId());
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void checkin(final String checkedOutFileId, final boolean isMajorVersion, final String checkinComments) {
        try {
            this.response = this.alfCon.checkin(checkedOutFileId, isMajorVersion, checkinComments);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                final Document<Element> doc = this.response.getDocument();
                final Entry responseEntry = (Entry) doc.getRoot();
                System.out.println("ID: " + responseEntry.getId());
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void fileVersions(final String fileId) {
        try {
            this.response = this.alfCon.fileVersions(fileId);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                final Document<Element> doc = this.response.getDocument();
                final Feed feed = (Feed) doc.getRoot();
                System.out.println("FeedTitle: " + feed.getTitle());

                for (final Entry entry : feed.getEntries()) {
                    System.out.println("-------------- ENTRY ---------------");
                    System.out.println("          Title : " + entry.getTitle());
                    System.out.println("             ID : " + entry.getId());
                    System.out.println("    ContentType : " + entry.getContentType());
                    System.out.println("ContentMimeType : " + entry.getContentMimeType());

                    final ExtensibleElement objElement = entry
                            .getExtension(new QName(AlfrescoRestClient.NS_CMIS_RESTATOM, "object", AlfrescoRestClient.CMISRA));
                    final ExtensibleElement propsElement = objElement.getExtension(new QName(AlfrescoRestClient.NS_CMIS_CORE, "properties",
                            AlfrescoRestClient.CMIS));

                    final List<ExtensibleElement> listExtensions = propsElement.getExtensions(new QName(AlfrescoRestClient.NS_CMIS_CORE, "propertyId",
                            AlfrescoRestClient.CMIS));
                    for (final ExtensibleElement tmpExtension : listExtensions) {
                        final String attValue = tmpExtension.getAttributeValue("propertyDefinitionId");
                        if ("cmis:objectId".equals(attValue)) {
                            final Element valueElement = tmpExtension
                                    .getExtension(new QName(AlfrescoRestClient.NS_CMIS_CORE, "value", AlfrescoRestClient.CMIS));
                            final String objectId = valueElement.getText();
                            System.out.println("       objectId : " + objectId);
                        }
                    }
                }
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadFileByStoreAndId(final String store, final String fileId, final String outputFileFolder, final String outputFileName) {
        try {
            this.response = this.alfCon.downloadFileByStoreAndId(store, fileId, outputFileFolder, outputFileName);

            System.out.println("Response type : " + this.response.getResponseType());
            System.out.println("Status code is: " + this.response.getStatusCode());
            System.out.println("Status text is: " + this.response.getStatusText());

            if (ResponseType.SUCCESS.toString().equals(this.response.getResponseType())) {
                // check that files was correctly download
            } else {
                // printStackTrace
                System.out.println("StackTrace: " + this.response.getStackTrace());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
