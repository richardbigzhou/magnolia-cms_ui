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

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.api.autosuggest.AutoSuggester.AutoSuggesterResult;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import java.util.Arrays;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test for AutoSuggesterForConfigurationApp.
 */
public class AutoSuggesterForConfigurationAppTest extends RepositoryTestCase {

    private Session session;
    private Node rootNode;
    private AutoSuggesterForConfigurationApp autoSuggesterForConfigurationApp;
    private DialogDefinitionRegistry dialogRegistry;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        rootNode = session.getRootNode();

        dialogRegistry = Mockito.mock(DialogDefinitionRegistry.class);
        Collection<String> dialogs = Arrays.asList(
                "standard-templating-kit:components/teasers/stkLinkListArea",
                "standard-templating-kit:components/links/stkDownloadLink",
                "standard-templating-kit:components/stages/stkStagePaging"
                );
        Mockito.when(dialogRegistry.getRegisteredDialogNames()).thenReturn(dialogs);
        ComponentProvider provider = Mockito.spy(Components.getComponentProvider());
        Mockito.when(provider.getComponent(DialogDefinitionRegistry.class)).thenReturn(dialogRegistry);
        Components.setComponentProvider(provider);

        autoSuggesterForConfigurationApp = new AutoSuggesterForConfigurationApp();
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanWhenBeanPropertyIsBooleanTypeAndPropertyIsBooleanTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", TestBean.class.getName());
        workbench.setProperty("booleanProperty", false);
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("booleanProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 2);
        assertTrue(autoSuggesterResult.getSuggestions().contains("true"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("false"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(AutoSuggester.AutoSuggesterResult.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanWhenBeanPropertyIsBooleanTypeAndPropertyIsStringTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", TestBean.class.getName());
        workbench.setProperty("booleanProperty", "false");
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("booleanProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 2);
        assertTrue(autoSuggesterResult.getSuggestions().contains("true"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("false"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(AutoSuggester.AutoSuggesterResult.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanWhenBeanPropertyIsBooleanTypeAndPropertyIsLongTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", TestBean.class.getName());
        workbench.setProperty("booleanProperty", 123);
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("booleanProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanWhenBeanPropertyIsNotBooleanTypeAndPropertyIsBooleanTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", TestBean.class.getName());
        workbench.setProperty("stringProperty", false);
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("stringProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanWhenParentBeanTypeIsNullAndPropertyIsBooleanTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("booleanProperty", false);
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("booleanProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 2);
        assertTrue(autoSuggesterResult.getSuggestions().contains("true"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("false"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(AutoSuggester.AutoSuggesterResult.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanWhenParentBeanTypeIsNullAndPropertyIsNotBooleanTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("booleanProperty", "false");
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("booleanProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsEnumWhenParentBeanTypeIsNull() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("enumProperty", TestEnum.enum2.toString());
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("enumProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsEnumWhenBeanPropertyIsEnumTypeAndPropertyIsStringTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", TestBean.class.getName());
        workbench.setProperty("enumProperty", TestEnum.enum2.toString());
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("enumProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains(TestEnum.enum1.toString()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(TestEnum.enum2.toString()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(TestEnum.enum3.toString()));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertFalse(autoSuggesterResult.showErrorHighlighting());
        assertTrue(AutoSuggester.AutoSuggesterResult.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsEnumWhenBeanPropertyIsEnumTypeAndPropertyIsNotStringTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", TestBean.class.getName());
        workbench.setProperty("enumProperty", true);
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("enumProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsEnumWhenBeanPropertyIsNotEnumType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", TestBean.class.getName());
        workbench.setProperty("stringProperty", "hi");
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("stringProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsDialogReferenceWhenParentBeanTypeIsConfiguredTemplateDefinitionAndPropertyIsStringTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node template = NodeUtil.createPath(rootNode, "template", NodeTypes.Content.NAME, true);
        template.setProperty("class", ConfiguredTemplateDefinition.class.getName());
        template.setProperty("dialog", "");
        Object jcrItemId = JcrItemUtil.getItemId(template.getProperty("dialog"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertEquals(dialogRegistry.getRegisteredDialogNames(), autoSuggesterResult.getSuggestions());
        assertTrue(autoSuggesterResult.getMatchMethod() == AutoSuggesterResult.CONTAINS);
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsDialogReferenceWhenParentBeanTypeIsConfiguredTemplateDefinitionAndPropertyIsNotStringTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node template = NodeUtil.createPath(rootNode, "template", NodeTypes.Content.NAME, true);
        template.setProperty("class", ConfiguredTemplateDefinition.class.getName());
        template.setProperty("dialog", true);
        Object jcrItemId = JcrItemUtil.getItemId(template.getProperty("dialog"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsDialogReferenceWhenParentBeanTypeIsConfiguredActionDefinitionAndPropertyIsStringTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node template = NodeUtil.createPath(rootNode, "template", NodeTypes.Content.NAME, true);
        template.setProperty("class", ConfiguredActionDefinition.class.getName());
        template.setProperty("dialogName", "");
        Object jcrItemId = JcrItemUtil.getItemId(template.getProperty("dialogName"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertEquals(dialogRegistry.getRegisteredDialogNames(), autoSuggesterResult.getSuggestions());
        assertTrue(autoSuggesterResult.getMatchMethod() == AutoSuggesterResult.CONTAINS);
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsDialogReferenceWhenParentBeanTypeIsConfiguredActionDefinitionAndPropertyIsNotStringTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node template = NodeUtil.createPath(rootNode, "template", NodeTypes.Content.NAME, true);
        template.setProperty("class", ConfiguredActionDefinition.class.getName());
        template.setProperty("dialogName", 123);
        Object jcrItemId = JcrItemUtil.getItemId(template.getProperty("dialogName"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsDialogReferenceWhenParentBeanTypeIsOtherType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node template = NodeUtil.createPath(rootNode, "template", NodeTypes.Content.NAME, true);
        template.setProperty("class", TestBean.class.getName());
        template.setProperty("dialogName", "");
        Object jcrItemId = JcrItemUtil.getItemId(template.getProperty("dialogName"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsDialogReferenceWhenParentBeanTypeIsNull() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node template = NodeUtil.createPath(rootNode, "template", NodeTypes.Content.NAME, true);
        template.setProperty("dialogName", "");
        Object jcrItemId = JcrItemUtil.getItemId(template.getProperty("dialogName"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    private class TestBean {
        private String stringProperty;
        private boolean booleanProperty;
        private TestEnum enumProperty;

        public String getStringProperty() {
            return stringProperty;
        }

        public void setStringProperty(String stringProperty) {
            this.stringProperty = stringProperty;
        }

        public boolean isBooleanProperty() {
            return booleanProperty;
        }

        public void setBooleanProperty(boolean booleanProperty) {
            this.booleanProperty = booleanProperty;
        }

        public TestEnum getEnumProperty() {
            return enumProperty;
        }

        public void setEnumProperty(TestEnum enumProperty) {
            this.enumProperty = enumProperty;
        }
    }

    /**
     * TestEnum.
     */
    enum TestEnum {
        enum1,
        enum2,
        enum3;
    }
}