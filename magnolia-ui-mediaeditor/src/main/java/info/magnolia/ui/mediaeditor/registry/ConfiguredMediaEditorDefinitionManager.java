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
package info.magnolia.ui.mediaeditor.registry;

import info.magnolia.cms.util.ModuleConfigurationObservingManager;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.mediaeditor.definition.ConfiguredMediaEditorDefinition;
import info.magnolia.ui.mediaeditor.definition.MediaEditorDefinition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Populates {@link MediaEditorRegistry} by scanning the nodes in the config workspace.
 */
@Singleton
public class ConfiguredMediaEditorDefinitionManager  extends ModuleConfigurationObservingManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String MEDIA_EDITORS_CONFIG_NODE_NAME = "mediaEditors";

    private Set<String> registeredIds = new HashSet<String>();

    private final MediaEditorRegistry registry;

    @Inject
    protected ConfiguredMediaEditorDefinitionManager(ModuleRegistry moduleRegistry, MediaEditorRegistry registry) {
        super(MEDIA_EDITORS_CONFIG_NODE_NAME, moduleRegistry);
        this.registry = registry;
    }

    @Override
    protected void reload(List<Node> nodes) throws RepositoryException {
        final List<MediaEditorDefinition> definitions = new ArrayList<MediaEditorDefinition>();
        for (Node node : nodes) {
            NodeUtil.visit(node, new NodeVisitor() {
                @Override
                public void visit(Node current) throws RepositoryException {
                    for (Node editorNode : NodeUtil.getNodes(current, NodeTypes.ContentNode.NAME)) {
                        if (isMediaEditor(editorNode)) {
                            // Handle as dialog only if it has sub nodes indicating that it is actually representing a dialog.
                            // This will filter the fields in dialogs used by the extends mechanism.
                            MediaEditorDefinition definition = createMediaEditorDefinition(editorNode);
                            if (definition != null) {
                                definitions.add(definition);
                            }
                        } else {
                            log.warn("node " + editorNode.getName() + " will not be handled as Dialog.");
                        }
                    }
                }
            }, new NodeTypePredicate(NodeTypes.Content.NAME));
        }
        this.registeredIds = registry.unregisterAndRegister(registeredIds, definitions);
    }

    protected MediaEditorDefinition createMediaEditorDefinition(Node editorNode) throws RepositoryException {
        String id = createId(editorNode);
        try {
            ConfiguredMediaEditorDefinition def =  (ConfiguredMediaEditorDefinition) Components.getComponent(Node2BeanProcessor.class).toBean(editorNode, MediaEditorDefinition.class);
            if (def != null) {
                def.setId(id);
            }
            return def;
        } catch (Node2BeanException e) {
            log.error("Unable to create a definition for editor [" + id + "]: " + e);
        } catch (RepositoryException e) {
            log.error("Unable to create a definition for editor [" + id + "]: " + e);
        }
        return null;
    }

    protected String createId(Node configNode) throws RepositoryException {
        final String path = configNode.getPath();
        final String[] pathElements = path.split("/");
        final String moduleName = pathElements[2];
        return moduleName + ":" + StringUtils.removeStart(path, "/modules/" + moduleName + "/" + MEDIA_EDITORS_CONFIG_NODE_NAME + "/");
    }

    private boolean isMediaEditor(Node mediaEditorNode) {
        return true;
    }
}
