/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.field;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.PathUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.jackrabbit.value.BinaryImpl;
import org.vaadin.easyuploads.UploadField;

import com.vaadin.data.Property;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Upload.FinishedEvent;

/**
 * File Upload Field.
 * Initialize File information if one file was already stored (Name/Thumbnail/...).
 * On success Set all information needed to store the image to the Item.
 *
 */
public class FileUpload extends UploadField {

    private JcrNodeAdapter item;

    public FileUpload ( JcrNodeAdapter item) {
        super();
        this.item = item;
        // Set Storage and FileType
        setStorageMode(UploadField.StorageMode.MEMORY);
        setFieldType(UploadField.FieldType.BYTE_ARRAY);

    }

    /**
     * Init Display with existing Data.
     */
    @Override
    public void attach() {
        super.attach();
        //TODO SCRUM-1401: Reactivate after resolution of this issue (Applicationis null)
        //Init values with existing data.
//        Property data =  item.getItemProperty(MgnlNodeType.JCR_DATA);
//        if(data !=null && data.getValue()!=null) {
//            setValue(data.getValue());
//            updateDisplay();
//        }
    }
    /**
     * Populate item Property.
     */
    @Override
    public void uploadFinished(FinishedEvent event) {
        super.uploadFinished(event);
        populateItemProperty();
    }


    @Override
    protected void updateDisplay() {
        final byte[] pngData = (byte[]) getValue();
        String filename = getLastFileName()!=null?getLastFileName():(String)item.getItemProperty(FileProperties.PROPERTY_FILENAME).getValue();
        String mimeType = getLastMimeType()!=null?getLastMimeType():(String)item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).getValue();
        long filesize = getLastFileSize();
        if (mimeType.equals("image/png")) {
            Resource resource = new StreamResource(
                    new StreamResource.StreamSource() {
                        @Override
                        public InputStream getStream() {
                            return new ByteArrayInputStream(pngData);
                        }
                    }, "", this.getApplication()) {
                @Override
                public String getMIMEType() {
                    return "image/png";
                }
            };
            Embedded embedded = new Embedded("Image:" + filename + "("
                    + filesize + " bytes)", resource);

            getRootLayout().addComponent(embedded);
        } else {
            super.updateDisplay();
        }
    }

    /**
     * Populate the Item property (data/image name/...)
     * Data is stored as a JCR Binary object.
     */
    private void populateItemProperty() {
        //Populate Data
        Property data =  item.getItemProperty(MgnlNodeType.JCR_DATA);

        if(data!=null) {
            BinaryImpl binaryImpl;
            try {
                binaryImpl = new BinaryImpl(getContentAsStream());
                data.setValue(binaryImpl);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        item.getItemProperty(FileProperties.PROPERTY_FILENAME).setValue(getLastFileName());
        item.getItemProperty(FileProperties.PROPERTY_CONTENTTYPE).setValue(getLastMimeType());
        item.getItemProperty(FileProperties.PROPERTY_LASTMODIFIED).setValue(new GregorianCalendar(TimeZone.getDefault()));
        item.getItemProperty(FileProperties.PROPERTY_SIZE).setValue(getLastFileSize());
        String extension = PathUtil.getExtension(getLastFileName());
        item.getItemProperty(FileProperties.PROPERTY_EXTENSION).setValue(extension);

//        ImageSize imageSize;
//        try {
//            imageSize = ImageSize.valueOf(file);
//            item.getItemProperty(FileProperties.PROPERTY_WIDTH).setValue(imageSize.getWidth());
//            item.getItemProperty(FileProperties.PROPERTY_HEIGHT).setValue(imageSize.getHeight());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

}
