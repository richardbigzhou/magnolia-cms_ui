/**
 * This file Copyright (c) 2015 Magnolia International
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

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.context.Context;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.form.field.factory.AbstractFieldFactoryTestCase;

import java.util.List;

import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;

import org.apache.jackrabbit.commons.iterator.NodeTypeIteratorAdapter;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.ui.ComboBox;

public class NodeTypeSelectFieldFactoryTest extends AbstractFieldFactoryTestCase<NodeTypeSelectFieldDefinition> {

    private NodeTypeSelectFieldFactory nodeTypeSelectFieldFactory;

    private NodeTypeSelectFieldDefinition definition;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        final Context context = mock(Context.class);
        final Session configSession = mock(Session.class);

        doReturn(configSession).when(context).getJCRSession(RepositoryConstants.CONFIG);

        final Workspace workspace = mock(Workspace.class);
        doReturn(workspace).when(configSession).getWorkspace();

        final NodeTypeManager nodeTypeManager = mock(NodeTypeManager.class);
        doReturn(nodeTypeManager).when(workspace).getNodeTypeManager();

        final List<NodeType> primaryNodeTypes = Lists.newArrayList(
                createMockNodeType("foo"),
                createMockNodeType("bar"));

        final List<NodeType> mixinNodeTypes = Lists.newArrayList(
                createMockNodeType("baz"),
                createMockNodeType("qux"));

        doReturn(new NodeTypeIteratorAdapter(primaryNodeTypes)).
                when(nodeTypeManager).
                    getPrimaryNodeTypes();

        doReturn(new NodeTypeIteratorAdapter(mixinNodeTypes)).
                when(nodeTypeManager).
                    getMixinNodeTypes();

        doReturn(new NodeTypeIteratorAdapter(
                ImmutableList.<NodeType>builder().
                        addAll(primaryNodeTypes).
                        addAll(mixinNodeTypes).build())).
                when(nodeTypeManager).
                    getMixinNodeTypes();

        this.definition = new NodeTypeSelectFieldDefinition();

        this.nodeTypeSelectFieldFactory = new NodeTypeSelectFieldFactory(definition, mock(Item.class), context);
        this.nodeTypeSelectFieldFactory.setComponentProvider(componentProvider);
    }

    @Test
    public void createsNodeTypeSelectFieldWithAllAvailablePrimaryNodeTypes() throws Exception {
        // WHEN
        final ComboBox field = (ComboBox) nodeTypeSelectFieldFactory.createField();

        // THEN
        assertThat(field.getContainerDataSource().getItemIds(), Matchers.<Object>contains("bar", "foo"));
    }

    @Test
    public void defaultNodeTypeIsPreselected() throws Exception {
        // GIVEN
        this.definition.setDefaultValue("foo");

        // WHEN
        final ComboBox field = (ComboBox) nodeTypeSelectFieldFactory.createField();

        // THEN
        assertThat(field.getValue(), Matchers.<Object>equalTo("foo"));

    }

    @Override
    protected void createConfiguredFieldDefinition() {
        final NodeTypeSelectFieldDefinition nodeTypeSelectFieldDefinition = new NodeTypeSelectFieldDefinition();
        this.definition = nodeTypeSelectFieldDefinition;
    }

    private NodeType createMockNodeType(String name) {
        final NodeType fooType = mock(NodeType.class);
        doReturn(name).when(fooType).getName();
        return fooType;
    }
}