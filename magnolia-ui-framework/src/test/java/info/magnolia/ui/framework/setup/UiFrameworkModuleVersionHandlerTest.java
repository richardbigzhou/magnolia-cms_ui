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
package info.magnolia.ui.framework.setup;

import static org.junit.Assert.*;

import info.magnolia.cms.util.UnicodeNormalizer;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.ui.dialog.action.CallbackDialogActionDefinition;
import info.magnolia.ui.form.field.definition.BasicTextCodeFieldDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.factory.BasicTextCodeFieldFactory;
import info.magnolia.ui.form.field.factory.SwitchableFieldFactory;
import info.magnolia.ui.form.field.transformer.multi.MultiValueJSONTransformer;
import info.magnolia.ui.form.field.transformer.multi.MultiValueSubChildrenNodeTransformer;

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

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/ui-framework.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new UiFrameworkModuleVersionHandler();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
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
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
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
        framework.addNode("fieldTypes", NodeTypes.ContentNode.NAME);
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        // Basic Code field
        assertTrue(framework.hasNode("fieldTypes/basicTextCodeField"));
        Node basicTextCodeField = framework.getNode("fieldTypes/basicTextCodeField");
        assertTrue(basicTextCodeField.hasProperty("definitionClass"));
        assertEquals(BasicTextCodeFieldDefinition.class.getName(), basicTextCodeField.getProperty("definitionClass").getString());
        assertTrue(basicTextCodeField.hasProperty("factoryClass"));
        assertEquals(BasicTextCodeFieldFactory.class.getName(), basicTextCodeField.getProperty("factoryClass").getString());
        // Switchable Field
        assertTrue(framework.hasNode("fieldTypes/switchableField"));
        Node switchableField = framework.getNode("fieldTypes/switchableField");
        assertTrue(switchableField.hasProperty("definitionClass"));
        assertEquals(SwitchableFieldDefinition.class.getName(), switchableField.getProperty("definitionClass").getString());
        assertTrue(switchableField.hasProperty("factoryClass"));
        assertEquals(SwitchableFieldFactory.class.getName(), switchableField.getProperty("factoryClass").getString());
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
        framework.getSession().save();

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
        framework.getSession().save();

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
        framework.getSession().save();

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
        framework.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(path.hasProperty("callbackDialogActionDefinition"));
        assertEquals(CallbackDialogActionDefinition.class.getName(), path.getProperty("callbackDialogActionDefinition").getString());
    }

}
