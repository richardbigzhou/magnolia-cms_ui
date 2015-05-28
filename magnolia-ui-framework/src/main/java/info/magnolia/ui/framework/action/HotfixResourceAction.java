/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.framework.action;


import static info.magnolia.jcr.util.NodeUtil.createPath;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import info.magnolia.context.Context;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes.Content;
import info.magnolia.jcr.util.NodeTypes.Folder;
import info.magnolia.resourceloader.ResourcePath;
import info.magnolia.resourceloader.jcr.JcrOrigin;
import info.magnolia.resourceloader.layered.LayeredResourcePath;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

/**
 * Action for creating hotfix in resource detailed sub-app.
 */
public class HotfixResourceAction extends AbstractAction<HotfixResourceActionDefinition> {

    private final Item resourceItem;
    private final Context context;
    private final SimpleTranslator i18n;
    private final UiContext uiContext;

    public HotfixResourceAction(HotfixResourceActionDefinition definition,
                                Item resourceItem,
                                Context context,
                                UiContext uiContext,
                                SimpleTranslator i18n) {
        super(definition);
        this.resourceItem = resourceItem;
        this.context = context;
        this.i18n = i18n;
        this.uiContext = uiContext;
    }

    @Override
    public void execute() throws ActionExecutionException {
        ResourcePath resourceToHotfix = getResourcePath(resourceItem).getFirst();
        String resourcePath = resourceToHotfix.getResourceHandle();
        String folderPath;
        String fileName;

        if (isContainingFolder(resourcePath)) {
            folderPath = resourcePath.substring(1, resourcePath.lastIndexOf("/"));
            fileName = resourcePath.substring(resourcePath.lastIndexOf("/")).replaceAll("/", "");
        } else {
            folderPath = null;
            fileName = resourcePath.replaceAll("/", "");
        }

        try {
            Session jcrSession = context.getJCRSession("resources");
            Node createdNode = getOrCreateNode(jcrSession, folderPath);
            addFile(resourceToHotfix, createdNode, fileName);

            uiContext.openNotification(
                    MessageStyleTypeEnum.INFO,
                    true,
                    i18n.translate("resources24.actions.hotfixResource.notification.success"));

            jcrSession.save();
        } catch (Exception e) {
            uiContext.openNotification(
                    MessageStyleTypeEnum.ERROR,
                    true,
                    i18n.translate("resources24.actions.hotfixResource.notification.error"));

            throw new ActionExecutionException(e);
        }
    }

    /**
     * Return the existing rootNode if the path only contains fileName.
     * If path contains more than fileName {e.g. x/y//abc.xml} then it will create the node(folders) and return created node.
     */
    private Node getOrCreateNode(Session jcrSession, String path) throws RepositoryException {
        if (isNotBlank(path)) {
            return createPath(jcrSession.getRootNode(), path, Folder.NAME);
        } else {
            return jcrSession.getRootNode();
        }
    }

    private void addFile(ResourcePath resourceToHotfix, Node node, String fileName) throws RepositoryException, IOException {
        String content = IOUtils.toString(resourceToHotfix.openStream(), "UTF-8");
        Node createdFileNode = node.addNode(fileName, Content.NAME);
        createdFileNode.setProperty(JcrOrigin.TEXT_PROPERTY, content);
    }

    private LayeredResourcePath getResourcePath(Item resourceItem) throws IllegalStateException {
        // We expect resourceItem to be a BeanItem<ResourcePath>, as provided by ResourcesContainer
        if (resourceItem instanceof BeanItem) {
            BeanItem<?> beanItem = ((BeanItem<?>) resourceItem);
            if (beanItem.getBean() instanceof ResourcePath) {
                return (LayeredResourcePath) beanItem.getBean();
            }
        }
        throw new IllegalStateException("Expected a BeanItem<ResourcePath> but got " + resourceItem.getClass());
    }

    private boolean isContainingFolder(String resourcePath) {
        return isNotBlank(resourcePath.substring(0, resourcePath.lastIndexOf("/")));
    }
}
