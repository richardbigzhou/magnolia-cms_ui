/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.form.field.upload.UploadProgressIndicator;

import java.text.NumberFormat;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;

/**
 * Custom Component used to create a custom display for the progress indicator.
 * <p>
 * This view normally contains a progress bar and a label indicating the uploaded percentage, filename...<br>
 * Refreshing the view is done by calling {@link #refreshLayout(long, long, String)}
 * <p>
 * Layout composition:
 * <ul>
 * <li>Label.FileName
 * <li>ProgressBar
 * <li>Label.percentage
 * <li>Label.UploadexOfy
 * </ul>
 */
public class BasicUploadProgressIndicator extends CustomComponent implements UploadProgressIndicator {

    private static final long serialVersionUID = 1L;

    private ProgressBar progressIndicator;
    private Label uploadFileLocation;
    private Label uploadFileRatio;
    private Label uploadFileProgress;
    private String inProgressCaption;
    private String inProgressRatioCaption;
    private VerticalLayout mainLayout;
    private final SimpleTranslator i18n;

    public BasicUploadProgressIndicator(String inProgressCaption, String inProgressRatioCaption, SimpleTranslator i18n) {
        this.inProgressCaption = inProgressCaption;
        this.inProgressRatioCaption = inProgressRatioCaption;
        this.i18n = i18n;

        uploadFileLocation = new Label("");
        uploadFileLocation.setSizeUndefined();
        uploadFileLocation.addStyleName("uploading-file");

        uploadFileRatio = new Label("");
        uploadFileRatio.setSizeUndefined();
        uploadFileRatio.addStyleName("uploaded-file");

        uploadFileProgress = new Label("");
        uploadFileProgress.setSizeUndefined();
        uploadFileProgress.addStyleName("uploading-file-progress");

        progressIndicator = new ProgressBar();
        progressIndicator.setVisible(false);
        progressIndicator.setWidth("100%");

        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();

        mainLayout.addComponent(uploadFileLocation);

        CssLayout progressLayout = new CssLayout();
        progressLayout.addStyleName("progress-layout");
        progressLayout.addComponent(progressIndicator);
        progressLayout.addComponent(uploadFileProgress);
        progressLayout.setWidth("100%");
        mainLayout.addComponent(progressLayout);
        mainLayout.addComponent(uploadFileRatio);

        Iterator<Component> it = mainLayout.iterator();
        while (it.hasNext()) {
            Component c = it.next();
            mainLayout.setComponentAlignment(c, Alignment.MIDDLE_CENTER);
        }
        mainLayout.setMargin(new MarginInfo(false, true, false, true));

        setCompositionRoot(mainLayout);
        addStyleName("uploading-progress-indicator");

    }

    @Override
    public void refreshLayout(long readBytes, long contentLength, String fileName) {
        progressIndicator.setValue(Float.valueOf(readBytes / (float) contentLength));

        uploadFileLocation.setValue(i18n.translate(this.inProgressCaption,fileName ));

        uploadFileProgress.setValue(createPercentage(readBytes, contentLength));

        String bytesRead = FileUtils.byteCountToDisplaySize(readBytes);
        String totalBytes = FileUtils.byteCountToDisplaySize(contentLength);
        uploadFileRatio.setValue(i18n.translate(this.inProgressRatioCaption,bytesRead, totalBytes));
    }

    @Override
    public void setProgress(float progress) {
        progressIndicator.setValue(progress);
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

    @Override
    public Component asVaadinComponent() {
        return this;
    }
}
