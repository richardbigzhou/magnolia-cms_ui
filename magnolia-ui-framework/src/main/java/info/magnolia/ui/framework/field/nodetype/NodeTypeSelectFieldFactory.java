/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.framework.field.nodetype;

import info.magnolia.context.Context;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * {@linkplain NodeTypeSelectFieldFactory} produces a drop-down select field with all the available primary node types available
 * via {@link NodeTypeManager}.
 *
 * @see NodeTypeSelectFieldDefinition
 */
public class NodeTypeSelectFieldFactory extends SelectFieldFactory<NodeTypeSelectFieldDefinition> {

    private static final Logger log = LoggerFactory.getLogger(NodeTypeSelectFieldFactory.class);

    private final Context context;

    @Inject
    public NodeTypeSelectFieldFactory(NodeTypeSelectFieldDefinition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport, Context context) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
        this.context = context;
    }

    /**
     * @deprecated since 5.4.7 - use {@link #NodeTypeSelectFieldFactory(NodeTypeSelectFieldDefinition, Item, UiContext, I18NAuthoringSupport, Context)} instead.
     */
    @Deprecated
    public NodeTypeSelectFieldFactory(NodeTypeSelectFieldDefinition definition, Item relatedFieldItem, Context context) {
        this(definition, relatedFieldItem, null, Components.getComponent(I18NAuthoringSupport.class), context);
    }

    @Override
    public List<SelectFieldOptionDefinition> getSelectFieldOptionDefinition() {
        try {
            final NodeTypeManager nodeTypeManager = context.getJCRSession(RepositoryConstants.CONFIG).getWorkspace().getNodeTypeManager();
            final NodeTypeIterator nodeTypes = nodeTypeManager.getPrimaryNodeTypes();
            final List<SelectFieldOptionDefinition> options = Lists.newArrayListWithCapacity(Long.valueOf(nodeTypes.getSize()).intValue());

            while (nodeTypes.hasNext()) {
                final NodeType nodeType = nodeTypes.nextNodeType();
                SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();

                final String nodeTypeName = nodeType.getName();

                option.setName(nodeTypeName);
                option.setLabel(nodeTypeName);
                option.setValue(nodeTypeName);
                option.setSelected(nodeTypeName.equals(definition.getDefaultValue()));

                options.add(option);
            }

            return options;
        } catch (RepositoryException e) {
            log.warn(String.format("Failed to populate a list of available node types due to: [%s], returning empty collection.", e.getMessage()));
            return Collections.emptyList();
        }
    }

    @Override
    protected String createDefaultValue(Property<?> dataSource) {
        return definition.getDefaultValue();
    }
}
