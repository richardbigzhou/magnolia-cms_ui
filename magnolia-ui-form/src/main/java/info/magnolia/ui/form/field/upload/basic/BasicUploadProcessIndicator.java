/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.form.field.upload.basic;

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.form.field.upload.UploadProgressIndicator;

import java.text.NumberFormat;

import org.apache.commons.io.FileUtils;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

/**
 * Custom Component used to create a custom display for {@link ProgressIndicator}.
 * <p>
 * This view normally contains a progress bar and label indicating the uploaded percentage, filename.... Refresh of the view is done by calling refreshOnProgressUploadLayout(..)
 * <p>
 * Layout composition:
 * <ul>
 * <li>Label.FileName
 * <li>ProgressBar
 * <li>Label.percentage
 * <li>Label.UploadexOfy
 * </ul>
 */
public class BasicUploadProcessIndicator extends CustomComponent implements UploadProgressIndicator {

    private static final long serialVersionUID = 1L;

    private ProgressIndicator progressIndicator;
    private Label uploadFileLocation;
    private Label uploadFileRatio;
    private Label uploadFileProgress;
    private String inProgressCaption;
    private String inProgressRatioCaption;
    private VerticalLayout mainLayout;

    public BasicUploadProcessIndicator(String inProgressCaption, String inProgressRatioCaption) {
        this.inProgressCaption = inProgressCaption;
        this.inProgressRatioCaption = inProgressRatioCaption;

        uploadFileLocation = new Label("");
        uploadFileLocation.setSizeUndefined();
        uploadFileLocation.addStyleName("uploading-file");

        uploadFileRatio = new Label("");
        uploadFileRatio.setSizeUndefined();
        uploadFileRatio.addStyleName("uploaded-file");

        uploadFileProgress = new Label("");
        uploadFileProgress.setSizeUndefined();
        uploadFileProgress.addStyleName("uploading-file-progress");

        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPollingInterval(50);
        progressIndicator.setWidth("100%");

        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        mainLayout.addComponent(uploadFileLocation);
        mainLayout.addComponent(progressIndicator);
        mainLayout.addComponent(uploadFileProgress);
        mainLayout.addComponent(uploadFileRatio);

        setCompositionRoot(mainLayout);
        addStyleName("uploading-progress-indicator");

    }

    @Override
    public void refreshOnProgressUploadLayout(long readBytes, long contentLength, String fileName) {
        progressIndicator.setValue(Float.valueOf(readBytes / (float) contentLength));

        uploadFileLocation.setValue(MessagesUtil.get(this.inProgressCaption, new String[] { fileName }));

        uploadFileProgress.setValue(createPercentage(readBytes, contentLength));

        String bytesRead = FileUtils.byteCountToDisplaySize(readBytes);
        String totalBytes = FileUtils.byteCountToDisplaySize(contentLength);
        uploadFileRatio.setValue(MessagesUtil.get(this.inProgressRatioCaption, new String[] { bytesRead, totalBytes }));
    }

    @Override
    public ProgressIndicator getProgressIndicator() {
        return this.progressIndicator;
    }

    @Override
    public void setProgressIndicatorValue(float newValue) {
        progressIndicator.setValue(newValue);
    }

    /**
     * Creates a percentage representation of the upload currently in progress.
     */
    private String createPercentage(long readBytes, long contentLength) {
        double read = Double.valueOf(readBytes);
        double from = Double.valueOf(contentLength);

        NumberFormat defaultFormat = NumberFormat.getPercentInstance();

        return defaultFormat.format((read / from));
    }

    @Override
    public void setVisible(boolean visible) {
        mainLayout.setVisible(visible);
        progressIndicator.setVisible(visible);
    }

}
