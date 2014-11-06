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
package info.magnolia.ui.workbench.autosuggest;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

import java.util.Arrays;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link AutoSuggesterUtil}.
 */
public class AutoSuggesterUtilTest extends RepositoryTestCase {

    private Session session;
    private Node rootNode;
    private AutoSuggesterForConfigurationApp autoSuggesterForConfigurationApp;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        rootNode = session.getRootNode();
        ComponentsTestUtil.setImplementation(EventBus.class, SimpleEventBus.class);
        autoSuggesterForConfigurationApp = new AutoSuggesterForConfigurationApp();
    }

    @Test
    public void testGetSuggestedSubPropertyNames() throws RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "testnode", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());

        // WHEN
        Collection<String> suggestedSubPropertyNames = AutoSuggesterUtil.getSuggestedSubPropertyNames(autoSuggesterForConfigurationApp, testNode);

        // THEN
        assertEquals(suggestedSubPropertyNames.size(), 3);
        assertTrue(suggestedSubPropertyNames.containsAll(Arrays.asList("booleanProperty", "stringProperty", "extends")));
    }

    @Test
    public void testGetSuggestedSubContentNodeNames() throws RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "testnode", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());

        // WHEN
        Collection<String> suggestedSubNodeNames = AutoSuggesterUtil.getSuggestedSubContentNodeNames(autoSuggesterForConfigurationApp, testNode);

        // THEN
        assertEquals(suggestedSubNodeNames.size(), 4);
        assertTrue(suggestedSubNodeNames.containsAll(Arrays.asList("objectProperty", "mapProperty", "testBean", "mockBean")));
    }

    @Test
    public void testGetSuggestedSubContentNodeNamesWhenParentIsModulesFolderAndBean() throws RepositoryException {
        // GIVEN
        Node testModule = NodeUtil.createPath(rootNode, "modules/testmodule", NodeTypes.Content.NAME, true);
        testModule.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());

        // WHEN
        Collection<String> suggestedSubNodeNames = AutoSuggesterUtil.getSuggestedSubContentNodeNames(autoSuggesterForConfigurationApp, testModule);

        // THEN
        assertEquals(suggestedSubNodeNames.size(), 4);
        assertTrue(suggestedSubNodeNames.containsAll(Arrays.asList("objectProperty", "mapProperty", "testBean", "mockBean")));
    }

    @Test
    public void testGetSuggestedSubFolderNodeNames() throws RepositoryException {
        // GIVEN
        Node testModule = NodeUtil.createPath(rootNode, "modules/testmodule", NodeTypes.Content.NAME, true);

        // WHEN
        Collection<String> suggestedSubFolderNames = AutoSuggesterUtil.getSuggestedSubContentNames(autoSuggesterForConfigurationApp, testModule);

        // THEN
        assertEquals(suggestedSubFolderNames.size(), 8);
        assertTrue(suggestedSubFolderNames.containsAll(Arrays.asList(
                "apps", "templates", "dialogs", "commands", "fieldTypes", "virtualURIMapping", "renderers", "config")));
    }

    @Test
    public void testGetSuggestedSubFolderNodeNamesWhenParentIsModulesFolderAndBean() throws RepositoryException {
        // GIVEN
        Node testModule = NodeUtil.createPath(rootNode, "modules/testmodule", NodeTypes.Content.NAME, true);
        testModule.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());

        // WHEN
        Collection<String> suggestedSubFolderNames = AutoSuggesterUtil.getSuggestedSubContentNames(autoSuggesterForConfigurationApp, testModule);

        // THEN
        assertEquals(suggestedSubFolderNames.size(), 8);
        assertTrue(suggestedSubFolderNames.containsAll(Arrays.asList(
                "apps", "templates", "dialogs", "commands", "fieldTypes", "virtualURIMapping", "renderers", "config")));
    }

    @Test
    public void testGetSuggestedSubFolderNodeNamesWhenParentIsBean() throws RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "testnode", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());

        // WHEN
        Collection<String> suggestedSubFolderNames = AutoSuggesterUtil.getSuggestedSubContentNames(autoSuggesterForConfigurationApp, testNode);

        // THEN
        assertTrue(suggestedSubFolderNames.isEmpty());
    }

}
