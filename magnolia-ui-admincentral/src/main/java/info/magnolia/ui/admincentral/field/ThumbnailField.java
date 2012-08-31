/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.image.ImageThumbnailProvider;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.CustomField;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * A base custom field comprising of a Thumbnail and related image information.
 * This Field is waiting for path Node value.
 */
public class ThumbnailField extends CustomField{
    private static final Logger log = LoggerFactory.getLogger(ThumbnailField.class);
    private HorizontalLayout layout;
    private Label label;
    private Embedded embedded;

    private ImageThumbnailProvider imageThumbnailProvider;
    private String workspace;
    private int width;
    private int height;
    private String currentIdentifier = "";

    public ThumbnailField(ImageThumbnailProvider imageThumbnailProvider, String workspace, int width, int height) {
        this.imageThumbnailProvider = imageThumbnailProvider;
        this.workspace = workspace;
        this.height = height;
        this.width = width;
        // Init layout
        label = new Label("", Label.CONTENT_XHTML);
        label.addStyleName("thumbnail-info");
        embedded = new Embedded(null);
        embedded.setType(Embedded.TYPE_IMAGE);
        layout = new HorizontalLayout();

        layout.addComponent(embedded);
        layout.addComponent(label);
        setCompositionRoot(layout);

        addStyleName("thumbnail-field");
        setSizeUndefined();
    }

    @Override
    public Class< ? > getType() {
        return String.class;
    }

    /**
     * Create a value change listener in order to refresh the View.
     */
    public void ValueChangeListener(Field field) {

        field.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                try {
                    Node parentNode = MgnlContext.getJCRSession(workspace).getNode(event.getProperty().getValue().toString());
                    String identifier = parentNode.getIdentifier();
                    if(!currentIdentifier.equals(identifier)) {
                        String path = imageThumbnailProvider.getPath(identifier, workspace, width, height);
                        layout.removeComponent(embedded);
                        embedded = new Embedded("", new ExternalResource(path));
                        label.setValue(createFieldDetail(parentNode));
                        layout.addComponent(embedded);
                    }
                } catch (RepositoryException e) {
                    log.warn("Not able to refresh the Thumbnail Field view for the following Node path: {}", event.getProperty().getValue(), e);
                }
            }
        });
    }

    /**
     * Create a field detail displayed after the thumbnail.
     */
    public String createFieldDetail(Node parentNode) throws RepositoryException{
        return "";
    }
}
