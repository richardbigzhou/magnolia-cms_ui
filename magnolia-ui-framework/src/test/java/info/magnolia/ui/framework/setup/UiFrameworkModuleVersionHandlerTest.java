/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.framework.setup;

import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.*;

import info.magnolia.cms.util.UnicodeNormalizer;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.ui.dialog.action.CallbackDialogActionDefinition;
import info.magnolia.ui.dialog.setup.migration.ControlMigratorsRegistry;
import info.magnolia.ui.form.field.definition.BasicTextCodeFieldDefinition;
import info.magnolia.ui.form.field.definition.CodeFieldDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.factory.BasicTextCodeFieldFactory;
import info.magnolia.ui.form.field.factory.CodeFieldFactory;
import info.magnolia.ui.form.field.factory.SwitchableFieldFactory;
import info.magnolia.ui.form.field.transformer.multi.MultiValueJSONTransformer;
import info.magnolia.ui.form.field.transformer.multi.MultiValueSubChildrenNodeTransformer;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test class.
 */
public class UiFrameworkModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Node i18n;

    private Node framework;

    private Session session;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/ui-framework.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new UiFrameworkModuleVersionHandler(Components.getComponent(ControlMigratorsRegistry.class));
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        // Super implementation returns this module's descriptor.
        // We override this because version-handler logic doesn't depend on the whole module dependency cascade
        // (only needs to be a non-empty list of existing module descriptors, so we pick the most basic one).
        return Arrays.asList(
                "/META-INF/magnolia/core.xml"
        );
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        NodeUtil.createPath(session.getRootNode(), "/modules/adminInterface", NodeTypes.ContentNode.NAME);
        i18n = NodeUtil.createPath(session.getRootNode(), "/server/i18n", NodeTypes.ContentNode.NAME);
        i18n.addNode("authoring", NodeTypes.ContentNode.NAME);
        i18n.addNode("authoring50", NodeTypes.ContentNode.NAME);
        i18n.getSession().save();

        framework = NodeUtil.createPath(session.getRootNode(), "/modules/ui-framework", NodeTypes.ContentNode.NAME);
        framework.addNode("dialogs", NodeTypes.ContentNode.NAME);

        ComponentsTestUtil.setImplementation(UnicodeNormalizer.Normalizer.class, "info.magnolia.cms.util.UnicodeNormalizer$NonNormalizer");
    }

    @Test
    public void testUpdateTo5_0_1WithoutLegacyModule() throws ModuleManagementException, RepositoryException {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(i18n.hasNode("authoring50"));

    }

    @Test
    public void testUpdateTo5_0_1WithLegacyModule() throws ModuleManagementException, RepositoryException {
        // GIVEN
        NodeUtil.createPath(session.getRootNode(), "/modules/adminInterface", NodeTypes.ContentNode.NAME);
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(i18n.hasNode("authoringLegacy"));
        assertTrue(i18n.hasNode("authoring"));
        assertFalse(i18n.hasNode("authoring50"));
    }

    @Test
    public void testUpdateTo5_0_1ThatDialogsAreInstalled() throws ModuleManagementException, RepositoryException {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(framework.hasNode("dialogs"));
        // Probably un-necessary - but verify that the important subnodes are there as well.
        assertTrue(framework.hasNode("dialogs/folder"));
        assertTrue(framework.hasNode("dialogs/rename"));
        assertTrue(framework.hasNode("dialogs/generic/standardActions"));
    }

    @Test
    public void testUpdateTo5_1AddFieldType() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Node fieldTypes = framework.addNode("fieldTypes", NodeTypes.Content.NAME);
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        // Code field
        assertThat(fieldTypes, hasNode(allOf(
                nodeName("code"),
                hasProperty("definitionClass", CodeFieldDefinition.class.getName()),
                hasProperty("factoryClass", CodeFieldFactory.class.getName()))));
        // Switchable field
        assertThat(fieldTypes, hasNode(allOf(
                nodeName("switchableField"),
                hasProperty("definitionClass", SwitchableFieldDefinition.class.getName()),
                hasProperty("factoryClass", SwitchableFieldFactory.class.getName()))));
        // Workbench field
        assertThat(fieldTypes, hasNode(allOf(
                nodeName("workbenchField"),
                hasProperty("definitionClass", "info.magnolia.ui.contentapp.field.WorkbenchFieldDefinition"),
                hasProperty("factoryClass", "info.magnolia.ui.contentapp.field.WorkbenchFieldFactory"))));
    }

    @Test
    public void testUpdateTo5_1ChangeSaveModeTypeDefaultValue() throws ModuleManagementException, RepositoryException {
        // GIVEN
        // This one should not be handled as his path is not part of a fields definition
        Node noFields = framework.addNode("saveModeType", NodeTypes.ContentNode.NAME);
        noFields.setProperty("multiValueHandlerClass", "info.magnolia.ui.form.field.property.MultiValuesHandler");
        // This one should be handled
        Node fields = framework.addNode("fields", NodeTypes.ContentNode.NAME);
        Node saveModeType = fields.addNode("saveModeType", NodeTypes.ContentNode.NAME);
        saveModeType.setProperty("multiValueHandlerClass", "info.magnolia.ui.form.field.property.MultiValuesHandler");
        session.save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        // Basic Code field
        assertTrue(framework.hasNode("saveModeType"));
        assertTrue(framework.hasNode("fields"));
        assertFalse(framework.hasNode("fields/saveModeType"));
        assertFalse(framework.hasNode("fields/propertyBuilder"));
    }

    @Test
    public void testUpdateTo5_1ChangeSaveModeTypeSubNodesValueHandler() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Node fields = framework.addNode("fields", NodeTypes.ContentNode.NAME);
        Node saveModeType = fields.addNode("saveModeType", NodeTypes.ContentNode.NAME);
        saveModeType.setProperty("multiValueHandlerClass", "info.magnolia.ui.form.field.property.SubNodesValueHandler");
        session.save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(framework.hasNode("fields"));
        assertFalse(framework.hasNode("fields/saveModeType"));
        assertTrue(framework.hasProperty("fields/transformerClass"));
        assertEquals(MultiValueSubChildrenNodeTransformer.class.getName(), framework.getProperty("fields/transformerClass").getString());
    }

    @Test
    public void testUpdateTo5_1ChangeSaveModeTypeCommaSeparatedValueHandler() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Node fields = framework.addNode("fields", NodeTypes.ContentNode.NAME);
        Node saveModeType = fields.addNode("saveModeType", NodeTypes.ContentNode.NAME);
        saveModeType.setProperty("multiValueHandlerClass", "info.magnolia.ui.form.field.property.CommaSeparatedValueHandler");
        session.save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(framework.hasNode("fields"));
        assertFalse(framework.hasNode("fields/saveModeType"));
        assertTrue(framework.hasProperty("fields/transformerClass"));
        assertEquals(MultiValueJSONTransformer.class.getName(), framework.getProperty("fields/transformerClass").getString());
    }

    @Test
    @Ignore("Task has been commented out")
    public void testUpdateTo5_1ChangePackageName() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Node path = framework.addNode("path", NodeTypes.ContentNode.NAME);
        path.setProperty("callbackDialogActionDefinition", "info.magnolia.ui.admincentral.dialog.action.CallbackDialogActionDefinition");
        session.save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(path.hasProperty("callbackDialogActionDefinition"));
        assertEquals(CallbackDialogActionDefinition.class.getName(), path.getProperty("callbackDialogActionDefinition").getString());
    }

    @Test
    public void testUpdateTo5_2AddFieldTypeIfNotExisiting() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Node fieldTypes = framework.addNode("fieldTypes", NodeTypes.Content.NAME);
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertTrue(framework.hasNode("fieldTypes/workbenchField"));
        Node workbenchField = framework.getNode("fieldTypes/workbenchField");
        assertTrue(workbenchField.hasProperty("definitionClass"));
        assertEquals("info.magnolia.ui.contentapp.field.WorkbenchFieldDefinition", workbenchField.getProperty("definitionClass").getString());
        assertTrue(workbenchField.hasProperty("factoryClass"));
        assertEquals("info.magnolia.ui.contentapp.field.WorkbenchFieldFactory", workbenchField.getProperty("factoryClass").getString());
        assertThat(fieldTypes, hasNode(allOf(
                nodeName("workbenchField"),
                hasProperty("definitionClass", "info.magnolia.ui.contentapp.field.WorkbenchFieldDefinition"),
                hasProperty("factoryClass", "info.magnolia.ui.contentapp.field.WorkbenchFieldFactory"))));
    }

    @Test
    public void testUpdateFrom52() throws ModuleManagementException, RepositoryException {
        // GIVEN
        this.setupConfigNode("/modules/ui-framework/commands/deafult");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2"));

        // THEN
        assertTrue(session.nodeExists("/modules/ui-framework/commands/default"));
    }

    @Test
    public void testUpdateFrom50() throws ModuleManagementException, RepositoryException {
        // GIVEN
        this.setupConfigNode("/modules/ui-framework/dialogs/importZip/form/tabs/import/fields/encoding/options/utf-8/");
        this.setupConfigNode("/modules/ui-framework/dialogs/importZip/form/tabs/import/fields/encoding/options/windows/");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(session.nodeExists("/modules/ui-framework/commands/default/importZip"));
        assertTrue(session.propertyExists("/modules/ui-framework/dialogs/importZip/form/tabs/import/fields/encoding/options/utf-8/label"));
        assertTrue(session.propertyExists("/modules/ui-framework/dialogs/importZip/form/tabs/import/fields/encoding/options/windows/label"));
    }

    @Test
    public void testUpdateFrom504() throws ModuleManagementException, RepositoryException {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.4"));

        // THEN
        assertTrue(session.nodeExists("/modules/ui-framework/dialogs/importZip/"));
    }

    @Test
    public void testUpdateFrom521() throws ModuleManagementException, RepositoryException {
        // GIVEN
        this.setupConfigNode("/modules/ui-framework/fieldTypes/compositField");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.1"));

        // THEN
        assertFalse(session.nodeExists("/modules/ui-framework/fieldTypes/compositField"));
        assertTrue(session.nodeExists("/modules/ui-framework/fieldTypes/compositeField"));
    }

    @Test
    public void testDialogsAreAddedModalityLevelProperty() throws ModuleManagementException, RepositoryException {
        // GIVEN
        this.setupConfigNode("/modules/ui-framework/dialogs/rename");
        this.setupConfigNode("/modules/ui-framework/dialogs/folder");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.2"));

        // THEN
        assertEquals("light", session.getProperty("/modules/ui-framework/dialogs/rename/modalityLevel").getString());
        assertEquals("light", session.getProperty("/modules/ui-framework/dialogs/folder/modalityLevel").getString());
    }

    @Test
    public void updateFrom538AddsNewMappingForCodeFieldTypeDefinition() throws Exception {
        // GIVEN
        Node fieldTypes = framework.addNode("fieldTypes", NodeTypes.Content.NAME);
        Node codeField = fieldTypes.addNode("basicTextCodeField", NodeTypes.ContentNode.NAME);
        codeField.setProperty("definitionClass", "info.magnolia.ui.form.field.definition.BasicTextCodeFieldDefinition");
        codeField.setProperty("factoryClass", "info.magnolia.ui.form.field.factory.BasicTextCodeFieldFactory");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.8"));

        // THEN — old mapping has to stick around by the time we adjust affected modules, and remove deprecated classes
        assertThat(fieldTypes, hasNode(allOf(
                nodeName("basicTextCodeField"),
                hasProperty("definitionClass", BasicTextCodeFieldDefinition.class.getName()),
                hasProperty("factoryClass", BasicTextCodeFieldFactory.class.getName()))));
        assertThat(fieldTypes, hasNode(allOf(
                nodeName("code"),
                hasProperty("definitionClass", CodeFieldDefinition.class.getName()),
                hasProperty("factoryClass", CodeFieldFactory.class.getName()))));
    }

    @Test
    public void updateFrom541ChangeZipUploadActionProperties() throws Exception {
        //GIVEN
        this.setupConfigNode("/modules/ui-framework/dialogs/importZip/form/tabs/import/fields/name");
        this.setupConfigProperty("/modules/ui-framework/dialogs/importZip/form/tabs/import/fields/name", "allowedMimeTypePattern", "application/(zip|x-zip|x-zip-compressed)");

        //WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.4.1"));

        //THEN
        assertThat(session.getNode("/modules/ui-framework/dialogs/importZip/form/tabs/import/fields/name"), hasProperty("allowedMimeTypePattern", "application/(zip|x-zip|x-zip-compressed|octet-stream)"));
        assertThat(session.getNode("/modules/ui-framework/dialogs/importZip/form/tabs/import/fields/name"), hasProperty("allowedFileExtensionPattern", "*.(zip)$"));
        assertThat(session.getNode("/modules/ui-framework/dialogs/importZip/form/tabs/import/fields/name"), hasProperty("fallbackMimeType", "application/zip"));
    }
}
