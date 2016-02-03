/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.pages.app.dnd;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Main test class for {@link TemplateTypeRestrictionDropConstraint}.
 */
public class TemplateTypeRestrictionDropConstraintTest {

    private Node source;
    private Node target;
    private JcrNodeAdapter sourceItem;
    private JcrNodeAdapter targetItem;

    private TemplateDefinitionAssignment templateAssignment;
    private TemplateDefinition sourceTemplateDefinition;
    private Collection<TemplateDefinition> availableTemplates;
    private MockSession session;

    @Before
    public void setUp() throws Exception {
        MockUtil.initMockContext();
        session = new MockSession(RepositoryConstants.WEBSITE);
        Node root = session.getRootNode();
        source = root.addNode("source", NodeTypes.Page.NAME);
        sourceItem = new JcrNodeAdapter(source);

        target = root.addNode("target", NodeTypes.Page.NAME);
        targetItem = new JcrNodeAdapter(target);

        ((MockContext) MgnlContext.getInstance()).addSession(RepositoryConstants.WEBSITE, session);

        sourceTemplateDefinition = new ConfiguredTemplateDefinition(null);
        templateAssignment = mock(TemplateDefinitionAssignment.class);
        availableTemplates = new ArrayList<TemplateDefinition>();
        when(templateAssignment.getAssignedTemplateDefinition(source)).thenReturn(sourceTemplateDefinition);
        when(templateAssignment.getAvailableTemplates(any(Node.class))).thenReturn(availableTemplates);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void allowedAsChild() {
        // GIVEN
        TemplateTypeRestrictionDropConstraint constraint = new TemplateTypeRestrictionDropConstraint(templateAssignment);
        availableTemplates.add(sourceTemplateDefinition);

        // WHEN
        boolean res = constraint.allowedAsChild(sourceItem, targetItem);

        // THEN
        assertTrue(res);
    }

    @Test
    public void notAllowedAsChild() {
        // GIVEN
        TemplateTypeRestrictionDropConstraint constraint = new TemplateTypeRestrictionDropConstraint(templateAssignment);

        // WHEN
        boolean res = constraint.allowedAsChild(sourceItem, targetItem);

        // THEN
        assertFalse(res);
    }

    @Test
    public void testAllowedBeforeAndAfter() throws Exception {
        // GIVEN
        TemplateTypeRestrictionDropConstraint constraint = new TemplateTypeRestrictionDropConstraint(templateAssignment);
        targetItem.addChild(sourceItem);

        final TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        final JcrNodeAdapter newChildNodeAdapter = createMockNodeAndBindToTemplate(templateDefinition);

        availableTemplates.add(sourceTemplateDefinition);

        // WHEN
        boolean allowedAfter = constraint.allowedAfter(newChildNodeAdapter, sourceItem);
        boolean allowedBefore = constraint.allowedBefore(newChildNodeAdapter, sourceItem);

        // THEN
        assertFalse(allowedAfter);
        assertFalse(allowedBefore);

    }

    @Test
    public void testNotAllowedBeforeAndAfter() throws Exception {
        // GIVEN
        TemplateTypeRestrictionDropConstraint constraint = new TemplateTypeRestrictionDropConstraint(templateAssignment);
        targetItem.addChild(sourceItem);

        final TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        final JcrNodeAdapter newChildNodeAdapter = createMockNodeAndBindToTemplate(templateDefinition);

        availableTemplates.add(sourceTemplateDefinition);
        availableTemplates.add(templateDefinition);

        // WHEN
        boolean allowedAfter = constraint.allowedAfter(newChildNodeAdapter, sourceItem);
        boolean allowedBefore = constraint.allowedBefore(newChildNodeAdapter, sourceItem);

        // THEN
        assertTrue(allowedAfter);
        assertTrue(allowedBefore);
    }

    private JcrNodeAdapter createMockNodeAndBindToTemplate(TemplateDefinition templateDefinition) throws RepositoryException, RegistrationException {
        final Node newChild = session.getRootNode().addNode("newChild");
        final JcrNodeAdapter newChildNodeAdapter = new JcrNodeAdapter(newChild);
        when(templateAssignment.getAssignedTemplateDefinition(newChild)).thenReturn(templateDefinition);
        return newChildNodeAdapter;
    }
}
