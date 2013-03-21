/**
 * This file Copyright (c) 2013 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.admincentral.tree.action.export;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;

import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;

/**
 * Utility class that create a {@link StreamResource.StreamSource} based on an
 * Byte[], initialize a {@link StreamResource}, and based on the current window,
 * send the response to the endUser in order to download a File.
 */
public class ExportStreamer {
    /**
     * Opens a new download window with provided content.
     *
     * @param app
     *            The application
     * @param fileName
     *            The filename linked with the content
     * @param fileContent
     *            The content
     * @param mimeType
     *            The exact mime type of the content
     */
    public static void openFileInNewWindow(String fileName, final byte[] fileContent, String mimeType) {
        openFile(fileName, fileContent, mimeType, "_blank");
    }
    
    /**
     * Opens a download window (only the select target) with provided content.
     * 
     * @param app
     *            The application
     * @param fileName
     *            The filename linked with the content
     * @param fileContent
     *            The content
     * @param mimeType
     *            The exact mime type of the content
     */
    public static void openFileInBlankWindow(String fileName, final byte[] fileContent, String mimeType) {
        openFile(fileName, fileContent, mimeType, "");
    }

    /**
     * Opens a new window with provided content.
     * 
     * @param app
     *            The application
     * @param reportName
     *            The name of the report
     * @param report
     *            The report content
     * @param format
     *            The format of the report (i.e. PDF)
     */
    public static void showFile(String reportName, final byte[] report, String format) {
        try {
            String fileName = reportName + "." + StringUtils.lowerCase(format);
            openFileInNewWindow(fileName, report, "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts download in current window with provided content.
     *
     * @param app
     *            The application
     * @param reportName
     *            The name of the report
     * @param report
     *            The report content
     * @param format
     *            The format of the report (i.e. PDF)
     */
    public static void openFileInCurrentWindow(String filename, byte[] reportContent, String mimeType) {
        openFile(filename, reportContent, mimeType, "_self");
    }

    private static void openFile(String fileName, final byte[] fileContent, String mimeType, String windowName) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(fileContent);
            }
        };

        StreamResource resource = new StreamResource(source, fileName);
        resource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName + "\"");
        resource.setMIMEType(mimeType);
        resource.setCacheTime(0);
        Page.getCurrent().open(resource, windowName, true);
    }

}
