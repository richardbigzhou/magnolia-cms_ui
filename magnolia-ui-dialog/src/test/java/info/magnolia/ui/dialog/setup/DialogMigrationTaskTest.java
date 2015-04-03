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
package info.magnolia.ui.dialog.setup;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.dialog.setup.migration.ActionCreator;
import info.magnolia.ui.dialog.setup.migration.BaseActionCreator;
import info.magnolia.ui.dialog.setup.migration.CheckBoxRadioControlMigrator;
import info.magnolia.ui.dialog.setup.migration.CheckBoxSwitchControlMigrator;
import info.magnolia.ui.dialog.setup.migration.ControlMigratorsRegistry;
import info.magnolia.ui.dialog.setup.migration.DateControlMigrator;
import info.magnolia.ui.dialog.setup.migration.EditCodeControlMigrator;
import info.magnolia.ui.dialog.setup.migration.EditControlMigrator;
import info.magnolia.ui.dialog.setup.migration.FckEditControlMigrator;
import info.magnolia.ui.dialog.setup.migration.FileControlMigrator;
import info.magnolia.ui.dialog.setup.migration.HiddenControlMigrator;
import info.magnolia.ui.dialog.setup.migration.LinkControlMigrator;
import info.magnolia.ui.dialog.setup.migration.MultiSelectControlMigrator;
import info.magnolia.ui.dialog.setup.migration.SelectControlMigrator;
import info.magnolia.ui.dialog.setup.migration.StaticControlMigrator;
import info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter;
import info.magnolia.ui.form.field.definition.CheckboxFieldDefinition;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Basic Dialog migration test class.
 */
public class DialogMigrationTaskTest extends RepositoryTestCase {

    private Session session;
    private Node dialogNode;
    private InstallContextImpl installContext;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        installContext = new InstallContextImpl(moduleRegistry);

        InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream("config.modules.standard-templating-kit.dialogs.generic.xml");
        DataTransporter.importXmlStream(
                xmlStream,
                RepositoryConstants.CONFIG,
                "/modules/testModule/dialogs",
                "",
                false,
                ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW,
                true,
                true);

        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        dialogNode = session.getNode("/modules/testModule/dialogs");

        ControlMigratorsRegistry registery = Components.getComponent(ControlMigratorsRegistry.class);
        registery.register("edit", new EditControlMigrator());
        registery.register("fckEdit", new FckEditControlMigrator());
        registery.register("date", new DateControlMigrator());
        registery.register("select", new SelectControlMigrator());
        registery.register("checkbox", new CheckBoxRadioControlMigrator(true));
        registery.register("checkboxSwitch", new CheckBoxSwitchControlMigrator());
        registery.register("radio", new CheckBoxRadioControlMigrator(false));
        registery.register("uuidLink", new LinkControlMigrator());
        registery.register("link", new LinkControlMigrator());
        registery.register("multiselect", new MultiSelectControlMigrator(false));
        registery.register("file", new FileControlMigrator());
        registery.register("static", new StaticControlMigrator());
        registery.register("hidden", new HiddenControlMigrator());
        registery.register("editCode", new EditCodeControlMigrator());
    }

    @Test
    public void testExecuteRemoveTmpPath() throws RepositoryException, TaskExecutionException {
        // GIVEN
        DialogMigrationTask task = new DialogMigrationTask("", "", "testModule", null, null);

        // WHEN
        task.execute(installContext);

        // THEN
        assertFalse(dialogNode.getParent().hasNode("dialogs50"));
    }

    @Test
    public void testExecuteControlsMigration() throws RepositoryException, TaskExecutionException {
        // GIVEN
        DialogMigrationTask task = new DialogMigrationTask("", "", "testModule", null, null);

        // WHEN
        task.execute(installContext);

        // THEN
        // Simple control check
        assertTrue(dialogNode.hasNode("generic/teasers/hideTeaserImage"));
        assertEquals(CheckboxFieldDefinition.class.getName(), dialogNode.getNode("generic/teasers/hideTeaserImage").getProperty("class").getString());
        // Link field control migration check
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserList/form/tabs/tabTeaser/fields/searchLink"));
        assertEquals(LinkFieldDefinition.class.getName(), dialogNode.getNode("generic/master/baseTeaserList/form/tabs/tabTeaser/fields/searchLink").getProperty("class").getString());
        assertEquals("pages", dialogNode.getNode("generic/master/baseTeaserList/form/tabs/tabTeaser/fields/searchLink").getProperty("appName").getString());
        assertEquals("website", dialogNode.getNode("generic/master/baseTeaserList/form/tabs/tabTeaser/fields/searchLink").getProperty("targetWorkspace").getString());
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserList/form/tabs/tabTeaser/fields/searchLink/identifierToPathConverter"));
        assertEquals(BaseIdentifierToPathConverter.class.getName(), dialogNode.getNode("generic/master/baseTeaserList/form/tabs/tabTeaser/fields/searchLink/identifierToPathConverter").getProperty("class").getString());

    }

    @Test
    public void testExecuteActionMigration() throws RepositoryException, TaskExecutionException {
        // GIVEN
        DialogMigrationTask task = new DialogMigrationTask("", "", "testModule", null, null);

        // WHEN
        task.execute(installContext);

        // THEN
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserList/actions"));
        // Has Commit
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserList/actions/commit"));
        assertEquals("info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition", dialogNode.getNode("generic/master/baseTeaserList/actions/commit").getProperty("class").getString());
        // Has Cancel
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserList/actions/cancel"));
        assertEquals("info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition", dialogNode.getNode("generic/master/baseTeaserList/actions/cancel").getProperty("class").getString());
    }

    @Test
    public void testExecuteCustomActionMigration() throws RepositoryException, TaskExecutionException {
        // GIVEN
        HashMap<String, List<ActionCreator>> customActions = new HashMap<String, List<ActionCreator>>();
        ActionCreator saveAction = new BaseActionCreator("commit", "saveTeaser", "TeaserSaveActionDefinition");
        ActionCreator cancelAction = new BaseActionCreator("cancel", "cancelTeaser", "TeaserCancelActionDefinition");
        customActions.put("baseTeaserDownload", Arrays.asList(saveAction, cancelAction));

        DialogMigrationTask task = new DialogMigrationTask("", "", "testModule", null, customActions);

        // WHEN
        task.execute(installContext);

        // THEN
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserDownload/actions"));
        // Has Commit
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserDownload/actions/commit"));
        assertEquals("TeaserSaveActionDefinition", dialogNode.getNode("generic/master/baseTeaserDownload/actions/commit").getProperty("class").getString());
        // Has Cancel
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserDownload/actions/cancel"));
        assertEquals("TeaserCancelActionDefinition", dialogNode.getNode("generic/master/baseTeaserDownload/actions/cancel").getProperty("class").getString());

    }

    @Test
    public void testExecuteTabsMigration() throws RepositoryException, TaskExecutionException {
        // GIVEN
        DialogMigrationTask task = new DialogMigrationTask("", "", "testModule", null, null);

        // WHEN
        task.execute(installContext);

        // THEN
        // Has main tab
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserDownload/form/tabs"));
        assertEquals("info.magnolia.module.templatingkit.messages", dialogNode.getNode("generic/master/baseTeaserDownload/form").getProperty("i18nBasename").getString());
        assertEquals("dialogs.paragraphs.teasers.horizontalTabbed.stkTeaserHorizontalTabitemDownloadFile.label", dialogNode.getNode("generic/master/baseTeaserDownload/form").getProperty("label").getString());
        // Has sub Tabs
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserDownload/form/tabs/tabTeaser"));
        assertEquals("dialogs.paragraphs.teasers.horizontalTabbed.stkTeaserHorizontalTabitemDownloadFile.tabTeaser.label", dialogNode.getNode("generic/master/baseTeaserDownload/form/tabs/tabTeaser").getProperty("label").getString());
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserDownload/form/tabs/tabTeaserOverwrite"));
        assertEquals("dialogs.paragraphs.teasers.horizontalTabbed.stkTeaserHorizontalTabitemDownloadFile.tabTeaserOverwrite.label", dialogNode.getNode("generic/master/baseTeaserDownload/form/tabs/tabTeaserOverwrite").getProperty("label").getString());

    }

    @Test
    public void testExecuteExtendsMigration() throws RepositoryException, TaskExecutionException {
        // GIVEN
        DialogMigrationTask task = new DialogMigrationTask("", "", "testModule", null, null);

        // WHEN
        task.execute(installContext);

        // THEN
        assertTrue(dialogNode.hasNode("generic/master/baseTeaserList/form/tabs/tabTeaser/fields/highlighted"));
        assertEquals("Check relative path reference", "/modules/testModule/dialogs/generic/teasers/highlighted", dialogNode.getNode("generic/master/baseTeaserList/form/tabs/tabTeaser/fields/highlighted").getProperty("extends").getString());
        assertTrue(dialogNode.hasNode("generic/master/basePageProperties/form/tabs/tabMetaData"));
        assertEquals("/modules/testModule/dialogs/generic/pages/tabMetaData", dialogNode.getNode("generic/master/basePageProperties/form/tabs/tabMetaData").getProperty("extends").getString());
        assertEquals("Do not change invalid path", "../../../teasers/highlighted", dialogNode.getNode("generic/master/baseTeaserGroup/form/tabs/tabTeaser/fields/highlighted").getProperty("extends").getString());
    }
}
