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
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.api.autosuggest.AutoSuggester.AutoSuggesterResult;
import info.magnolia.ui.api.autosuggest.AutoSuggester.AutoSuggesterResult.MatchMethod;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.fieldtype.definition.FieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionRegistry;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;
import info.magnolia.ui.workbench.autosuggest.MockSubClass.MockInnerSubClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
    private AppDescriptorRegistry appDescriptorRegistry;
    private TemplateDefinitionRegistry templateDefinitionRegistry;
    private FieldTypeDefinitionRegistry fieldTypeDefinitionRegistry;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        rootNode = session.getRootNode();

        ComponentsTestUtil.setImplementation(EventBus.class, SimpleEventBus.class);

        dialogRegistry = Mockito.mock(DialogDefinitionRegistry.class);
        Collection<String> dialogs = Arrays.asList(
                "standard-templating-kit:components/teasers/stkLinkListArea",
                "standard-templating-kit:components/links/stkDownloadLink",
                "standard-templating-kit:components/stages/stkStagePaging"
                );
        Collection<String> dialogPaths = Arrays.asList(
                "/modules/standard-templating-kit/dialogs/components/teasers/stkLinkListArea",
                "/modules/standard-templating-kit/dialogs/components/links/stkDownloadLink",
                "/modules/standard-templating-kit/dialogs/components/stages/stkStagePaging"
                );
        Mockito.when(dialogRegistry.getRegisteredDialogNames()).thenReturn(dialogs);
        Mockito.when(dialogRegistry.getRegisteredDialogPaths()).thenReturn(dialogPaths);
        ComponentProvider provider = Mockito.spy(Components.getComponentProvider());
        Mockito.when(provider.getComponent(DialogDefinitionRegistry.class)).thenReturn(dialogRegistry);

        appDescriptorRegistry = Mockito.mock(AppDescriptorRegistry.class);
        Set<String> apps = new HashSet<String>();
        apps.add("/modules/pages/apps/pages");
        apps.add("/modules/security-app/apps/security");
        apps.add("/modules/contacts/apps/contacts");
        Mockito.when(appDescriptorRegistry.getRegisteredAppPaths()).thenReturn(apps);
        Mockito.when(provider.getComponent(AppDescriptorRegistry.class)).thenReturn(appDescriptorRegistry);

        templateDefinitionRegistry = Mockito.mock(TemplateDefinitionRegistry.class);
        Collection<String> templates = Arrays.asList(
                "/modules/standard-templating-kit/templates/components/content/stkTextImage",
                "/modules/forum/templates/components/threadNew",
                "/modules/form/templates/components/form"
                );
        Mockito.when(templateDefinitionRegistry.getRegisteredTemplatePaths()).thenReturn(templates);
        Mockito.when(provider.getComponent(TemplateDefinitionRegistry.class)).thenReturn(templateDefinitionRegistry);

        fieldTypeDefinitionRegistry = Mockito.mock(FieldTypeDefinitionRegistry.class);
        Set<String> fieldTypes = new HashSet<String>();
        fieldTypes.add("/modules/ui-framework/fieldTypes/textField");
        fieldTypes.add("/modules/pages/fieldTypes/templateSelect");
        fieldTypes.add("/modules/security-app/fieldTypes/roleManagementField");
        Mockito.when(fieldTypeDefinitionRegistry.getRegisteredFieldTypePaths()).thenReturn(fieldTypes);
        Mockito.when(provider.getComponent(FieldTypeDefinitionRegistry.class)).thenReturn(fieldTypeDefinitionRegistry);

        Components.setComponentProvider(provider);

        autoSuggesterForConfigurationApp = new AutoSuggesterForConfigurationApp();
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanWhenBeanPropertyIsBooleanTypeAndPropertyIsBooleanTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", MockBean.class.getName());
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
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanWhenBeanPropertyIsBooleanTypeAndPropertyIsStringTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", MockBean.class.getName());
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
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanWhenBeanPropertyIsBooleanTypeAndPropertyIsLongTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", MockBean.class.getName());
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
        workbench.setProperty("class", MockBean.class.getName());
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
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
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
        workbench.setProperty("class", MockBean.class.getName());
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
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsEnumWhenBeanPropertyIsEnumTypeAndPropertyIsNotStringTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", MockBean.class.getName());
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
        workbench.setProperty("class", MockBean.class.getName());
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
        assertTrue(autoSuggesterResult.getMatchMethod() == MatchMethod.CONTAINS);
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
        assertTrue(autoSuggesterResult.getMatchMethod() == MatchMethod.CONTAINS);
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
        template.setProperty("class", MockBean.class.getName());
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

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenParentBeanTypeIsNullAndPropertyNameIsClass() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", "NoSuchClass");
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("String"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenParentBeanTypeIsNullAndPropertyNameIsExtends() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("extends", "../");
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("extends"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("String"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenParentBeanTypeIsNullAndPropertyNameIsNotClassOrExtends() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("stringProperty", "workbench");
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("stringProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 4);
        assertTrue(autoSuggesterResult.getSuggestions().contains("String"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("Boolean"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("Long"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("Double"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsStringType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("stringProperty", "workbench");
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("stringProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("String"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsIsCharacterType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("characterProperty", "workbench");
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("characterProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("String"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsClassType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("classProperty", MockBean.class.getName());
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("classProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("String"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsEnumType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("enumProperty", TestEnum.enum1.toString());
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("enumProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("String"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsPrimitiveBooleanType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("booleanProperty", true);
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("booleanProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("Boolean"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsWrappedBooleanType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("booleanWrappedProperty", true);
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("booleanWrappedProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("Boolean"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsLongType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("longProperty", 123);
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("longProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("Long"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsIntegerType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("integerProperty", 123);
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("integerProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("Long"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsByteType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("byteProperty", 1);
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("byteProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("Long"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsShortType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("shortProperty", 1);
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("shortProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("Long"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsDoubleType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("doubleProperty", 1.0);
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("doubleProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("Double"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsFloatType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("floatProperty", 1.0);
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("floatProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("Double"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsStringBufferTypeAndPropertyNameIsNotClassOrExtends() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("stringBufferProperty", "buffer");
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("stringBufferProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 4);
        assertTrue(autoSuggesterResult.getSuggestions().contains("String"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("Boolean"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("Long"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("Double"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForTypeOfPropertyWhenBeanPropertyIsOtherTypeAndPropertyNameIsExtends() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node testNode = NodeUtil.createPath(rootNode, "test", NodeTypes.ContentNode.NAME, true);
        testNode.setProperty("class", MockBean.class.getName());
        testNode.setProperty("extends", 123);
        Object jcrItemId = JcrItemUtil.getItemId(testNode.getProperty("extends"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "type");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("Long"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfPropertyWhenParentBeanTypeIsNullAndNodeIsModuleFolderAndNodeHasNoProperties() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "/modules/core", NodeTypes.Content.NAME, true);
        core.setProperty("untitled", "");
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("untitled"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains("class"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("extends"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("version"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfPropertyWhenParentBeanTypeIsNullAndNodeIsNotModuleFolderAndNodeHasNoProperties() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "/modules", NodeTypes.Content.NAME, true);
        core.setProperty("untitled", "");
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("untitled"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 2);
        assertTrue(autoSuggesterResult.getSuggestions().contains("class"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("extends"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfPropertyWhenParentBeanTypeIsNullAndNodeIsContentNodeAndNodeHasNoPropertiess() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        core.setProperty("untitled", "");
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("untitled"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 2);
        assertTrue(autoSuggesterResult.getSuggestions().contains("class"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("extends"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfPropertyWhenNodeIsBean() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 4);
        assertTrue(autoSuggesterResult.getSuggestions().contains("class"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("booleanProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("stringProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("extends"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfPropertyWhenNodeIsBeanAndNodeHasSomePropertiesAndPropertyIsInBean() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        workbench.setProperty("booleanProperty", true);
        workbench.setProperty("stringProperty", "");
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("booleanProperty"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 2);
        assertTrue(autoSuggesterResult.getSuggestions().contains("booleanProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("extends"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfPropertyWhenNodeIsBeanAndNodeHasSomePropertiesAndPropertyIsNotInBean() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node workbench = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        workbench.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        workbench.setProperty("booleanProperty", true);
        workbench.setProperty("stringProperty", "");
        workbench.setProperty("untitled", "");
        Object jcrItemId = JcrItemUtil.getItemId(workbench.getProperty("untitled"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("extends"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfPropertyWhenNodeIsNotBeanAndNodeIsModuleFolder() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "/modules/core", NodeTypes.Content.NAME, true);
        core.setProperty("class", HashMap.class.getName());
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains("class"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("extends"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("version"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfPropertyWhenNodeIsNotBeanAndNodeIsNotModuleFolder() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "/modules", NodeTypes.ContentNode.NAME, true);
        core.setProperty("class", HashMap.class.getName());
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 2);
        assertTrue(autoSuggesterResult.getSuggestions().contains("class"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("extends"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfPropertyWhenNodeIsNotBeanAndNodeIsContentNode() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "workbench", NodeTypes.ContentNode.NAME, true);
        core.setProperty("class", HashMap.class.getName());
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 2);
        assertTrue(autoSuggesterResult.getSuggestions().contains("class"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("extends"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenParentBeanTypeIsNullAndPropertyNameIsClass() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "core", NodeTypes.ContentNode.NAME, true);
        core.setProperty("class", "NoSuchClass");
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenNoParentAndNoSubClassesAndPropertyIsStringTypeInJCRAndPropertyNameIsClass() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        rootNode.setProperty("class", MockClassWithoutSubClasses.class.getName());
        Object jcrItemId = JcrItemUtil.getItemId(rootNode.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockClassWithoutSubClasses.class.getName()));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.CONTAINS == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenNoParentAndHasSubClassesAndPropertyIsStringTypeInJCRAndPropertyNameIsClass() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        rootNode.setProperty("class", MockClassWithSubClasses.class.getName());
        Object jcrItemId = JcrItemUtil.getItemId(rootNode.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockClassWithSubClasses.class.getName()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockSubClass.class.getName()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockInnerSubClass.class.getName()));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.CONTAINS == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenHasParentAndNotInParentAndPropertyNameIsClass() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "core", NodeTypes.ContentNode.NAME, true);
        core.setProperty("class", MockClassWithoutSubClasses.class.getName());
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockClassWithoutSubClasses.class.getName()));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.CONTAINS == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenHasParentAndInParentAndPropertyNameIsClass() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockParentBean.class.getName());
        Node testBean = NodeUtil.createPath(rootNode, "parent/child", NodeTypes.ContentNode.NAME, true);
        testBean.setProperty("class", "");
        Object jcrItemId = JcrItemUtil.getItemId(testBean.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockClassWithSubClasses.class.getName()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockSubClass.class.getName()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockInnerSubClass.class.getName()));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.CONTAINS == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenPropertyNameIsClassAndPropertyIsNotStringTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockParentBean.class.getName());
        Node testBean = NodeUtil.createPath(rootNode, "parent/child", NodeTypes.ContentNode.NAME, true);
        testBean.setProperty("class", 123);
        Object jcrItemId = JcrItemUtil.getItemId(testBean.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenPropertyNameIsNotClassAndBeanPropertyIsClassTypeWithNoTypeParameter() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "core", NodeTypes.ContentNode.NAME, true);
        core.setProperty("class", MockBeanWithClassProperties.class.getName());
        core.setProperty("classPropertyWithoutTypeParameter", "");
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("classPropertyWithoutTypeParameter"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenPropertyNameIsNotClassAndBeanPropertyIsClassTypeWithUnboundedWildcardTypeParameter() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "core", NodeTypes.ContentNode.NAME, true);
        core.setProperty("class", MockBeanWithClassProperties.class.getName());
        core.setProperty("classPropertyWithUnboundedWildcardTypeParameter", "");
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("classPropertyWithUnboundedWildcardTypeParameter"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenPropertyNameIsNotClassAndBeanPropertyIsClassTypeWithConcreteTypeParameter() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "core", NodeTypes.ContentNode.NAME, true);
        core.setProperty("class", MockBeanWithClassProperties.class.getName());
        core.setProperty("classPropertyWithConcreteTypeParameter", "");
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("classPropertyWithConcreteTypeParameter"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockClassWithSubClasses.class.getName()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockSubClass.class.getName()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockInnerSubClass.class.getName()));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.CONTAINS == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenPropertyNameIsNotClassAndBeanPropertyIsClassTypeWithLowerBoundedWildcardTypeParameter() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "core", NodeTypes.ContentNode.NAME, true);
        core.setProperty("class", MockBeanWithClassProperties.class.getName());
        core.setProperty("classPropertyWithLowerBoundedWildcardTypeParameter", "");
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("classPropertyWithLowerBoundedWildcardTypeParameter"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenPropertyNameIsNotClassAndBeanPropertyIsClassTypeWithUpperBoundedWildcardTypeParameter() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "core", NodeTypes.ContentNode.NAME, true);
        core.setProperty("class", MockBeanWithClassProperties.class.getName());
        core.setProperty("classPropertyWithUpperBoundedWildcardTypeParameter", "");
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("classPropertyWithUpperBoundedWildcardTypeParameter"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockClassWithSubClasses.class.getName()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockSubClass.class.getName()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockInnerSubClass.class.getName()));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.CONTAINS == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenPropertyNameIsNotClassAndBeanPropertyIsClassTypeAndPropertyIsNotStringInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "core", NodeTypes.ContentNode.NAME, true);
        core.setProperty("class", MockBeanWithClassProperties.class.getName());
        core.setProperty("classPropertyWithUpperBoundedWildcardTypeParameter", 123);
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("classPropertyWithUpperBoundedWildcardTypeParameter"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsClassReferenceWhenPropertyNameIsNotClassAndBeanPropertyIsNotClassType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node core = NodeUtil.createPath(rootNode, "core", NodeTypes.ContentNode.NAME, true);
        core.setProperty("class", MockBeanWithClassProperties.class.getName());
        core.setProperty("modelClass", "");
        Object jcrItemId = JcrItemUtil.getItemId(core.getProperty("modelClass"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForNameOfNodeWhenParentNodeIsNull() throws RepositoryException {
        // GIVEN
        Object jcrItemId = JcrItemUtil.getItemId(rootNode);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForNameOfFolderNodeParentNodeIsModulesFolder() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        NodeUtil.createPath(rootNode, "modules/pages", NodeTypes.Content.NAME, true);
        Node untitled = NodeUtil.createPath(rootNode, "modules/pages/untitled", NodeTypes.Content.NAME, true);
        Object jcrItemId = JcrItemUtil.getItemId(untitled);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 8);
        assertTrue(autoSuggesterResult.getSuggestions().contains("apps"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("templates"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("dialogs"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("commands"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("fieldTypes"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("virtualURIMapping"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("renderers"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("config"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertFalse(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfContentNodeParentNodeIsModulesFolder() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        NodeUtil.createPath(rootNode, "modules/pages", NodeTypes.Content.NAME, true);
        Node core = NodeUtil.createPath(rootNode, "modules/pages/core", NodeTypes.ContentNode.NAME, true);
        Object jcrItemId = JcrItemUtil.getItemId(core);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForNameOfFolderNodeParentNodeIsModulesFolderAndAlsoBean() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node pages = NodeUtil.createPath(rootNode, "modules/pages", NodeTypes.Content.NAME, true);
        pages.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        Node untitled = NodeUtil.createPath(rootNode, "modules/pages/untitled", NodeTypes.Content.NAME, true);
        Object jcrItemId = JcrItemUtil.getItemId(untitled);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 8);
        assertTrue(autoSuggesterResult.getSuggestions().contains("apps"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("templates"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("dialogs"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("commands"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("fieldTypes"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("virtualURIMapping"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("renderers"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("config"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertFalse(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfContentNodeParentNodeIsModulesFolderAndAlsoBean() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node pages = NodeUtil.createPath(rootNode, "modules/pages", NodeTypes.Content.NAME, true);
        pages.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        Node untitled = NodeUtil.createPath(rootNode, "modules/pages/untitled", NodeTypes.ContentNode.NAME, true);
        Object jcrItemId = JcrItemUtil.getItemId(untitled);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains("objectProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("mapProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("testBean"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfContentNodeWhenNodeParentTypeIsArray() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockBean.class.getName());
        NodeUtil.createPath(rootNode, "parent/arrayProperty", NodeTypes.ContentNode.NAME, true);
        Node arrayMember = NodeUtil.createPath(rootNode, "parent/arrayProperty/arrayMember", NodeTypes.ContentNode.NAME, true);
        Object jcrItemId = JcrItemUtil.getItemId(arrayMember);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForNameOfContentNodeWhenNodeParentTypeIsCollection() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockBean.class.getName());
        NodeUtil.createPath(rootNode, "parent/collectionProperty", NodeTypes.ContentNode.NAME, true);
        Node collectionMember = NodeUtil.createPath(rootNode, "parent/collectionProperty/collectionMember", NodeTypes.ContentNode.NAME, true);
        Object jcrItemId = JcrItemUtil.getItemId(collectionMember);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForNameOfContentNodeWhenNodeParentTypeIsMap() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockBean.class.getName());
        NodeUtil.createPath(rootNode, "parent/mapProperty", NodeTypes.ContentNode.NAME, true);
        Node mapMember = NodeUtil.createPath(rootNode, "parent/mapProperty/mapMember", NodeTypes.ContentNode.NAME, true);
        Object jcrItemId = JcrItemUtil.getItemId(mapMember);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForNameOfContentNodeWhenNodeParentTypeIsBean() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        Node untitled = NodeUtil.createPath(rootNode, "parent/untitled", NodeTypes.ContentNode.NAME, true);
        Object jcrItemId = JcrItemUtil.getItemId(untitled);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains("objectProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("mapProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("testBean"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfContentNodeWhenNodeParentTypeIsBeanAndParentNodeHasSomeNodesAndNodeIsInParent() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        NodeUtil.createPath(rootNode, "parent/mapProperty", NodeTypes.ContentNode.NAME, true);
        Node testBean = NodeUtil.createPath(rootNode, "parent/testBean", NodeTypes.ContentNode.NAME, true);
        Object jcrItemId = JcrItemUtil.getItemId(testBean);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 2);
        assertTrue(autoSuggesterResult.getSuggestions().contains("objectProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("testBean"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testGetSuggestionsForNameOfContentNodeWhenNodeParentTypeIsBeanAndParentNodeHasSomeNodesAndNodeIsNotInParent() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        NodeUtil.createPath(rootNode, "parent/mapProperty", NodeTypes.ContentNode.NAME, true);
        NodeUtil.createPath(rootNode, "parent/testBean", NodeTypes.ContentNode.NAME, true);
        Node untitled = NodeUtil.createPath(rootNode, "parent/untitled", NodeTypes.ContentNode.NAME, true);
        Object jcrItemId = JcrItemUtil.getItemId(untitled);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 1);
        assertTrue(autoSuggesterResult.getSuggestions().contains("objectProperty"));
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
        assertTrue(MatchMethod.STARTS_WITH == autoSuggesterResult.getMatchMethod());
    }

    @Test
    public void testTypeInferenceWhenClassPropertyIsInAncestor() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node node = NodeUtil.createPath(rootNode, "node", NodeTypes.ContentNode.NAME, true);
        node.setProperty("class", MockGrandParentBean.class.getName());
        Node child = NodeUtil.createPath(rootNode, "node/child/child", NodeTypes.ContentNode.NAME, true);
        child.setProperty("class", "");
        Object jcrItemId = JcrItemUtil.getItemId(child.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockClassWithSubClasses.class.getName()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockSubClass.class.getName()));
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockInnerSubClass.class.getName()));
    }

    @Test
    public void testTypeInferenceWhenNodeExtends() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node node1 = NodeUtil.createPath(rootNode, "node1", NodeTypes.ContentNode.NAME, true);
        node1.setProperty("class", MockBeanForNameOfPropertyAndNameOfNode.class.getName());
        Node node2 = NodeUtil.createPath(rootNode, "node2", NodeTypes.ContentNode.NAME, true);
        node2.setProperty("extends", "/node1");
        Node node3 = NodeUtil.createPath(rootNode, "node3", NodeTypes.ContentNode.NAME, true);
        node3.setProperty("extends", "/node2");
        Node untitled = NodeUtil.createPath(rootNode, "node3/testBean/untitled", NodeTypes.ContentNode.NAME, true);
        Object jcrItemId = JcrItemUtil.getItemId(untitled);

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 4);
        assertTrue(autoSuggesterResult.getSuggestions().contains("arrayProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("collectionProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("mapProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("stringBufferProperty"));
    }

    @Test
    public void testTypeInferenceWhenInModuleFolder() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        NodeUtil.createPath(rootNode, "/modules/pages/apps/folder", NodeTypes.Content.NAME, true);
        Node node = NodeUtil.createPath(rootNode, "/modules/pages/apps/folder/pages", NodeTypes.ContentNode.NAME, true);
        node.setProperty("class", "");
        Object jcrItemId = JcrItemUtil.getItemId(node.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() > 0);
        assertTrue(autoSuggesterResult.getSuggestions().contains(AppDescriptor.class.getName()));
    }

    @Test
    public void testTypeInferenceWhenNotInFolder() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        NodeUtil.createPath(rootNode, "/modules/pages/apps/node", NodeTypes.ContentNode.NAME, true);
        Node node = NodeUtil.createPath(rootNode, "/modules/pages/apps/node/pages", NodeTypes.ContentNode.NAME, true);
        node.setProperty("class", "");
        Object jcrItemId = JcrItemUtil.getItemId(node.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor(jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testTypeInferenceWhenInCollection() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockBean.class.getName());
        Node node = NodeUtil.createPath(rootNode, "parent/collectionProperty/node", NodeTypes.ContentNode.NAME, true);
        node.setProperty("class", "");
        Object jcrItemId = JcrItemUtil.getItemId(node.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() > 0);
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockBean.class.getName()));
    }

    @Test
    public void testTypeInferenceWhenInMap() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockBean.class.getName());
        Node node = NodeUtil.createPath(rootNode, "parent/mapProperty/node", NodeTypes.ContentNode.NAME, true);
        node.setProperty("class", "");
        Object jcrItemId = JcrItemUtil.getItemId(node.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() > 0);
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockBean.class.getName()));
    }

    @Test
    public void testTypeInferenceWhenInArray() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockBean.class.getName());
        Node node = NodeUtil.createPath(rootNode, "parent/arrayProperty/node", NodeTypes.ContentNode.NAME, true);
        node.setProperty("class", "");
        Object jcrItemId = JcrItemUtil.getItemId(node.getProperty("class"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() > 0);
        assertTrue(autoSuggesterResult.getSuggestions().contains(MockBean.class.getName()));
    }

    @Test
    public void testTypeInferenceWhenImplementingClass() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        ComponentsTestUtil.setImplementation(MockSuperInterface.class, MockImplementingClass.class);
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockSuperInterface.class.getName());
        parent.setProperty("untitled", "");
        Object jcrItemId = JcrItemUtil.getItemId(parent.getProperty("untitled"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains("property1"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("booleanProperty"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("extends"));
    }

    @Test
    public void testTypeInferenceWhenSubInterface() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node parent = NodeUtil.createPath(rootNode, "parent", NodeTypes.ContentNode.NAME, true);
        parent.setProperty("class", MockSubInterface.class.getName());
        parent.setProperty("untitled", "");
        Object jcrItemId = JcrItemUtil.getItemId(parent.getProperty("untitled"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "jcrName");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertTrue(autoSuggesterResult.getSuggestions().size() == 3);
        assertTrue(autoSuggesterResult.getSuggestions().contains("property1"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("property2"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("extends"));
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsExtendsWhenParentBeanTypeIsAppDescriptor() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node securityApp = NodeUtil.createPath(rootNode, "modules/security-app/apps/security", NodeTypes.ContentNode.NAME, true);
        securityApp.setProperty("class", AppDescriptor.class.getName());
        securityApp.setProperty("extends", "");
        Object jcrItemId = JcrItemUtil.getItemId(securityApp.getProperty("extends"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertEquals(appDescriptorRegistry.getRegisteredAppPaths().size() - 1, autoSuggesterResult.getSuggestions().size());
        assertTrue(autoSuggesterResult.getSuggestions().contains("/modules/pages/apps/pages"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("/modules/contacts/apps/contacts"));
        assertTrue(autoSuggesterResult.getMatchMethod() == MatchMethod.CONTAINS);
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsExtendsWhenParentBeanTypeIsFormDialogDefinition() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node stkLinkListAreaDialog = NodeUtil.createPath(rootNode, "modules/standard-templating-kit/dialogs/components/teasers/stkLinkListArea", NodeTypes.ContentNode.NAME, true);
        stkLinkListAreaDialog.setProperty("class", FormDialogDefinition.class.getName());
        stkLinkListAreaDialog.setProperty("extends", "");
        Object jcrItemId = JcrItemUtil.getItemId(stkLinkListAreaDialog.getProperty("extends"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertEquals(dialogRegistry.getRegisteredDialogNames().size() - 1, autoSuggesterResult.getSuggestions().size());
        assertTrue(autoSuggesterResult.getSuggestions().contains("/modules/standard-templating-kit/dialogs/components/links/stkDownloadLink"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("/modules/standard-templating-kit/dialogs/components/stages/stkStagePaging"));
        assertTrue(autoSuggesterResult.getMatchMethod() == MatchMethod.CONTAINS);
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsExtendsWhenParentBeanTypeIsTemplateDefinition() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node threadNew = NodeUtil.createPath(rootNode, "modules/forum/templates/components/threadNew", NodeTypes.ContentNode.NAME, true);
        threadNew.setProperty("class", TemplateDefinition.class.getName());
        threadNew.setProperty("extends", "");
        Object jcrItemId = JcrItemUtil.getItemId(threadNew.getProperty("extends"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertEquals(templateDefinitionRegistry.getRegisteredTemplatePaths().size() - 1, autoSuggesterResult.getSuggestions().size());
        assertTrue(autoSuggesterResult.getSuggestions().contains("/modules/standard-templating-kit/templates/components/content/stkTextImage"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("/modules/form/templates/components/form"));
        assertTrue(autoSuggesterResult.getMatchMethod() == MatchMethod.CONTAINS);
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsExtendsWhenParentBeanTypeIsFieldTypeDefinition() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node templateSelect = NodeUtil.createPath(rootNode, "modules/pages/fieldTypes/templateSelect", NodeTypes.ContentNode.NAME, true);
        templateSelect.setProperty("class", FieldTypeDefinition.class.getName());
        templateSelect.setProperty("extends", "");
        Object jcrItemId = JcrItemUtil.getItemId(templateSelect.getProperty("extends"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertTrue(autoSuggesterResult.suggestionsAvailable());
        assertEquals(fieldTypeDefinitionRegistry.getRegisteredFieldTypePaths().size() - 1, autoSuggesterResult.getSuggestions().size());
        assertTrue(autoSuggesterResult.getSuggestions().contains("/modules/ui-framework/fieldTypes/textField"));
        assertTrue(autoSuggesterResult.getSuggestions().contains("/modules/security-app/fieldTypes/roleManagementField"));
        assertTrue(autoSuggesterResult.getMatchMethod() == MatchMethod.CONTAINS);
        assertTrue(autoSuggesterResult.showMismatchedSuggestions());
        assertTrue(autoSuggesterResult.showErrorHighlighting());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsExtendsWhenParentBeanTypeIsOtherType() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node editDialog = NodeUtil.createPath(rootNode, "editDialog", NodeTypes.ContentNode.NAME, true);
        editDialog.setProperty("class", MockBean.class.getName());
        editDialog.setProperty("extends", "");
        Object jcrItemId = JcrItemUtil.getItemId(editDialog.getProperty("extends"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsExtendsWhenParentBeanTypeIsNull() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node editDialog = NodeUtil.createPath(rootNode, "editDialog", NodeTypes.ContentNode.NAME, true);
        editDialog.setProperty("extends", "");
        Object jcrItemId = JcrItemUtil.getItemId(editDialog.getProperty("extends"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
    }

    @Test
    public void testGetSuggestionsForValueOfPropertyIfPropertyIsExtendsWhenPropertyIsNotStringTypeInJCR() throws AccessDeniedException, PathNotFoundException, RepositoryException {
        // GIVEN
        Node editDialog = NodeUtil.createPath(rootNode, "editDialog", NodeTypes.ContentNode.NAME, true);
        editDialog.setProperty("class", FormDialogDefinition.class.getName());
        editDialog.setProperty("extends", 123);
        Object jcrItemId = JcrItemUtil.getItemId(editDialog.getProperty("extends"));

        // WHEN
        AutoSuggesterResult autoSuggesterResult = autoSuggesterForConfigurationApp.getSuggestionsFor((JcrPropertyItemId) jcrItemId, "value");

        // THEN
        assertFalse(autoSuggesterResult.suggestionsAvailable());
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