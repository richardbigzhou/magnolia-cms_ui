/**
 * This file Copyright (c) 2003-2015 Magnolia International
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
package info.magnolia.ui.form.field;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;

/**
 * A base custom field comprising of a Thumbnail and related image information.
 * This Field is waiting for path Node value.
 */
public class ThumbnailField extends CustomField<String> {
    private static final Logger log = LoggerFactory.getLogger(ThumbnailField.class);
    private HorizontalLayout layout;
    private Label label;
    protected Image embedded = new Image();

    private ImageProvider imageThumbnailProvider;
    private String workspace;

    private String currentIdentifier = "";

    public ThumbnailField(ImageProvider imageThumbnailProvider, String workspace) {
        this.imageThumbnailProvider = imageThumbnailProvider;
        this.workspace = workspace;
        this.layout = new HorizontalLayout();

        label = new Label("", ContentMode.HTML);
        label.addStyleName("thumbnail-info");

        addStyleName("thumbnail-field");
        setSizeUndefined();

    }

    @Override
    protected Component initContent() {
        layout.addComponent(label);
        layout.addComponent(embedded);
        return layout;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    /**
     * Create a value change listener in order to refresh the View.
     */
    public void ValueChangeListener(Field<?> field) {
        field.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                setLabelAndImage(event.getProperty().getValue().toString());
            }
        });
    }

    /**
     * Set the Label And Image.
     */
    public void setLabelAndImage(String nodePath) {
        try {
            if (StringUtils.isEmpty(nodePath)) {
                return;
            }
            Node parentNode = MgnlContext.getJCRSession(workspace).getNode(nodePath);
            String uuid = parentNode.getIdentifier();

            if (!currentIdentifier.equals(uuid)) {
                // Set Text info
                label.setValue(createFieldDetail(parentNode));
                // Set Thumbnail
                String path = imageThumbnailProvider.getPortraitPath(new JcrNodeAdapter(parentNode));
                if (layout.getComponentIndex(embedded) != -1) {
                    layout.removeComponent(embedded);
                }
                embedded = path != null ? new Image("", new ExternalResource(path)) : new Image(null);
                layout.addComponent(embedded);
            }
        } catch (RepositoryException e) {
            log.warn("Not able to refresh the Thumbnail Field view for the following Node path: {}", nodePath, e);
        }
    }

    /**
     * Create a field detail displayed after the thumbnail.
     */
    public String createFieldDetail(Node parentNode) throws RepositoryException {
        return "";
    }
}
