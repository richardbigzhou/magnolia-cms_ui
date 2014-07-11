/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.pages.app.availability;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.configured.ConfiguredAreaDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateAvailability;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link CanAddComponentRule}.
 */
public class CanAddComponentRuleTest {

    private CanAddComponentRule rule;
    private Object itemId;
    private ConfiguredAreaDefinition areaDefinition;
    private Node area;

    @Before
    public void setUp() throws Exception {
        MockSession session = new MockSession(RepositoryConstants.WEBSITE);
        Node page = session.getRootNode().addNode("page", NodeTypes.Page.NAME);
        NodeTypes.Renderable.set(page, "template");
        area = page.addNode("area", NodeTypes.Area.NAME);

        ConfiguredTemplateDefinition templateDefinition = new ConfiguredTemplateDefinition(new ConfiguredTemplateAvailability());
        areaDefinition = new ConfiguredAreaDefinition(new ConfiguredTemplateAvailability());
        templateDefinition.addArea("area", areaDefinition);
        TemplateDefinitionRegistry templateRegistry = mock(TemplateDefinitionRegistry.class);
        when(templateRegistry.getTemplateDefinition("template")).thenReturn(templateDefinition);
        rule = new CanAddComponentRule(templateRegistry);
        itemId = JcrItemUtil.getItemId(area);

        MockContext ctx = new MockContext();
        ctx.addSession(RepositoryConstants.WEBSITE, session);
        MgnlContext.setInstance(ctx);
    }

    @Test
    public void testNoComponentArea() throws Exception {
        // GIVEN
        areaDefinition.setType(AreaDefinition.TYPE_NO_COMPONENT);

        // WHEN
        boolean available = rule.isAvailableForItem(itemId);

        // THEN
        assertThat(available, is(false));
    }

    @Test
    public void testEmptySingleArea() throws Exception {
        // GIVEN
        areaDefinition.setType(AreaDefinition.TYPE_SINGLE);

        // WHEN
        boolean available = rule.isAvailableForItem(itemId);

        // THEN
        assertThat(available, is(true));
    }

    @Test
    public void testSingleAreaWithSingletonOfTypeContentNode() throws Exception {
        // GIVEN
        areaDefinition.setType(AreaDefinition.TYPE_SINGLE);
        area.addNode("singleton", NodeTypes.ContentNode.NAME);

        // WHEN
        boolean available = rule.isAvailableForItem(itemId);

        // THEN
        assertThat(available, is(false));
    }

    @Test
    public void testListArea() throws Exception {
        // GIVEN
        areaDefinition.setType(AreaDefinition.TYPE_LIST);

        // WHEN
        boolean available = rule.isAvailableForItem(itemId);

        // THEN
        assertThat(available, is(true));
    }

    @Test
    public void testMaxComponentsDefinedAndNotReached() throws Exception {
        // GIVEN
        areaDefinition.setMaxComponents(2);
        area.addNode("component", NodeTypes.Component.NAME);
        area.addNode("noComponentChildNode", NodeTypes.Content.NAME);

        // WHEN
        boolean available = rule.isAvailableForItem(itemId);

        // THEN
        assertThat(available, is(true));
    }

    @Test
    public void testMaxComponentsDefinedAndReached() throws Exception {
        // GIVEN
        areaDefinition.setMaxComponents(1);
        area.addNode("component", NodeTypes.Component.NAME);

        // WHEN
        boolean available = rule.isAvailableForItem(itemId);

        // THEN
        assertThat(available, is(false));
    }

}
