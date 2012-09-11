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
package info.magnolia.ui.admincentral.field.upload;

import java.text.NumberFormat;

import org.apache.commons.io.FileUtils;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

/**
 * Custom Component used to create a custom display for {@link ProgressIndicator}.
 * <p>
 * This view normally contains a progress bar and label indicating the uploaded percentage, filename....
 * Refresh of the view is done by calling refreshOnProgressUploadLayout(..)
 *<p>
 * Layout composition:
 * <ul>
 *  <li>Label.FileName
 *  <li>ProgressBar
 *  <li>Label.percentage
 *  <li>Label.UploadexOfy
 *  </ul>
 *
 */
public class ProgressIndicatorComponentDefaultImpl extends CustomComponent implements ProgressIndicatorComponent {

    private static final String DEFAULT_FILE_LOCATION_CAPTION = "Uploading file ";
    private static final String DEFAULT_FILE_RATIO_CAPTION = " uploaded";
    private static final String DEFAULT_FILE_RATIO_OF_CAPTION = " of ";

    private ProgressIndicator progressIndicator;
    private Label uploadFileLocation;
    private Label uploadFileRatio;
    private Label uploadFileProgress;

    private VerticalLayout mainLayout;

    public ProgressIndicatorComponentDefaultImpl () {
        //Init Label
        uploadFileLocation = new Label(DEFAULT_FILE_LOCATION_CAPTION);
        uploadFileLocation.setSizeUndefined();
        uploadFileLocation.addStyleName("uploading-file");

        uploadFileRatio = new Label(DEFAULT_FILE_RATIO_CAPTION);
        uploadFileRatio.setSizeUndefined();
        uploadFileRatio.addStyleName("uploaded-file");

        uploadFileProgress = new Label("");
        uploadFileProgress.setSizeUndefined();
        uploadFileProgress.addStyleName("uploading-file-progress");
        //Init base progress Indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPollingInterval(500);
        progressIndicator.setWidth("100%");
        //Init Layout
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        //Set Layout
        mainLayout.addComponent(uploadFileLocation);
        mainLayout.addComponent(progressIndicator);
        mainLayout.addComponent(uploadFileProgress);
        mainLayout.addComponent(uploadFileRatio);

        setCompositionRoot(mainLayout);
        addStyleName("uploading-progress-indicator");
    }

    @Override
    public void setProgressIndicatorValue(Object newValue) {
        progressIndicator.setValue(newValue);
    }

    @Override
    public void setVisible(boolean visible) {
        mainLayout.setVisible(visible);
        progressIndicator.setVisible(visible);
    }

    @Override
    public void refreshOnProgressUploadLayout(long readBytes, long contentLength, String fileName) {
        progressIndicator.setValue(new Float(readBytes / (float) contentLength));
        uploadFileLocation.setValue(DEFAULT_FILE_LOCATION_CAPTION + fileName + "...");
        uploadFileProgress.setValue(createPercentage(readBytes, contentLength));
        uploadFileRatio.setValue(FileUtils.byteCountToDisplaySize(readBytes) + DEFAULT_FILE_RATIO_OF_CAPTION + FileUtils.byteCountToDisplaySize(contentLength) + DEFAULT_FILE_RATIO_CAPTION);
    }

    @Override
    public ProgressIndicator getProgressIndicator() {
        return this.progressIndicator;
    }

    /**
     * Creates a percentage representation of the upload currently in progress.
     */
    private String createPercentage(long readBytes, long contentLength) {
        double read = Double.valueOf(readBytes);
        double from = Double.valueOf(contentLength);

        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        defaultFormat.setMaximumFractionDigits(0);

        return defaultFormat.format((read/from));
    }
}
