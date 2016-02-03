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
package info.magnolia.ui.framework.availability;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import info.magnolia.config.registry.DefinitionMetadata;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.Registry;
import info.magnolia.config.registry.RegistryFacade;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import java.util.Arrays;
import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

public class IsDefinitionRuleTest extends RepositoryTestCase {

    private IsDefinitionRule rule;
    private RegistryFacade registryFacade;
    private Session session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        Node node = session.getRootNode().addNode("testNode", NodeTypes.ContentNode.NAME);

        registryFacade = mock(RegistryFacade.class);

        Registry registry = mock(Registry.class);
        DefinitionProvider definitionProvider = mock(DefinitionProvider.class);
        DefinitionMetadata definitionMetadata = mock(DefinitionMetadata.class);

        given(definitionProvider.getMetadata()).willReturn(definitionMetadata);
        given(registry.getAllProviders()).willReturn(newArrayList(definitionProvider));
        given(registryFacade.all()).willReturn(Arrays.asList(registry));
        given(definitionProvider.getMetadata().getLocation()).willReturn(node.getPath());

        rule = new IsDefinitionRule(registryFacade);
    }

    @Test
    public void availableForIfDefinitionPresent() throws Exception {
        // GIVEN
        Node node = session.getNode("/testNode");
        Object itemId = JcrItemUtil.getItemId(node);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertThat(isAvailable, is(true));
    }

    @Test
    public void notAvailableForJcrPropertyId() {
        // GIVEN
        JcrPropertyItemId itemId = mock(JcrPropertyItemId.class);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertThat(isAvailable, is(false));
    }

    @Test
    public void notAvailableForNoDefinitionItem() throws Exception {
        // GIVEN
        given(registryFacade.all()).willReturn(Collections.EMPTY_LIST);
        IsDefinitionRule rule = new IsDefinitionRule(registryFacade);
        Node node = session.getNode("/testNode");
        Object itemId = JcrItemUtil.getItemId(node);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertThat(isAvailable, is(false));
    }
}