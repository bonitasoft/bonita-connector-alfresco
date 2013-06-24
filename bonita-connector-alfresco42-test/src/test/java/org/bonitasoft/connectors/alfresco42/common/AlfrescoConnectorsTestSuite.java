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
package org.bonitasoft.connectors.alfresco42.common;

import org.bonitasoft.connectors.alfresco42.AlfrescoCreateFolderByPathTest;
import org.bonitasoft.connectors.alfresco42.AlfrescoDeleteFolderByPathTest;
import org.bonitasoft.connectors.alfresco42.AlfrescoDeleteItemByIdTest;
import org.bonitasoft.connectors.alfresco42.AlfrescoUploadFileTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    AlfrescoCreateFolderByPathTest.class,
    AlfrescoDeleteFolderByPathTest.class,
    AlfrescoDeleteItemByIdTest.class,
    AlfrescoUploadFileTest.class })

public class AlfrescoConnectorsTestSuite {

}
