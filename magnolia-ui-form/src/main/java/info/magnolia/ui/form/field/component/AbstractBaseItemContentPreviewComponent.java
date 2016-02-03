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
package info.magnolia.ui.form.field.component;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;

/**
 * {@link Item} implementation of {@link AbstractContentPreviewComponent}.
 */
public abstract class AbstractBaseItemContentPreviewComponent extends AbstractContentPreviewComponent<Item> {

    private static final Logger log = LoggerFactory.getLogger(AbstractBaseItemContentPreviewComponent.class);
    protected ImageProvider imageProvider;

    public AbstractBaseItemContentPreviewComponent(String workspace) {
        super(workspace);
        // Always set the root layout as CompositionRoot.
        this.rootLayout = new HorizontalLayout();
        setCompositionRoot(rootLayout);
    }

    public abstract void setImageProvider();

    /**
     * Returned components (Label) will be displayed in the order of creation (first display is the first put into the list).
     *
     * @param fileItem
     * @return list of detail components (Generally a Label)
     * @throws RepositoryException
     */
    public abstract List<Component> createFieldDetail(Item fileItem) throws RepositoryException;

    @Override
    public Component refreshContentDetail(Item item) {
        FormLayout fileInfo = new FormLayout();
        fileInfo.setSizeUndefined();
        fileInfo.addStyleName("file-details");
        try {
            List<Component> res = createFieldDetail(item);
            fileInfo.addComponents(res.toArray(new Component[res.size()]));
        } catch (RepositoryException e) {
            log.warn("Could not get the related File node", e);
        }
        return fileInfo;
    }

    @Override
    public Component refreshContentPreview(Item item) {
        Image thumbnail = new Image();
        String path = imageProvider.getPortraitPath(((JcrItemAdapter)item).getItemId());
        if (StringUtils.isNotBlank(path)) {
            thumbnail = new Image("", new ExternalResource(path));
            thumbnail.addStyleName("file-preview-area");
        }
        return thumbnail;
    }

    /**
     * Retrieve an {@link Item} based on the itemPath.<br>
     * This {@link Item} is then used by sub classes to display Item detail and preview.
     */
    @Override
    protected Item refreshItem(String itemPath) {
        Item currentItem = null;
        try {
            Node fileNode = MgnlContext.getJCRSession(workspace).getNode(itemPath);
            if (fileNode != null) {
                currentItem = new JcrNodeAdapter(fileNode);
            }
        } catch (RepositoryException e) {
            log.warn("Not able to refresh the Preview Component view for the following path: {}", itemPath, e);
        }
        return currentItem;
    }

}
