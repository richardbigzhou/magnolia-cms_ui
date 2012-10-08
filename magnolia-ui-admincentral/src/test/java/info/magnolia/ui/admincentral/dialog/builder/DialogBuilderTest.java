/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.dialog.builder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.admincentral.field.builder.DialogFieldFactory;
import info.magnolia.ui.admincentral.field.builder.TextFieldBuilder;
import info.magnolia.ui.model.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.model.field.definition.TextFieldDefinition;
import info.magnolia.ui.model.tab.definition.ConfiguredTabDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.widget.dialog.DialogView;
import info.magnolia.ui.widget.dialog.FormDialog;

import java.util.Locale;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Main test for {@link DialogBuilder}.
 */
public class DialogBuilderTest {

    private final String workspaceName = "workspace";

    private MockSession session;

    @Before
    public void setUp() {
        DefaultMessagesManager manager = new DefaultMessagesManager();
        ComponentsTestUtil.setInstance(MessagesManager.class, manager);
        SystemContext systemContext = mock(SystemContext.class);
        when(systemContext.getLocale()).thenReturn(new Locale("en"));
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);
        session = new MockSession(workspaceName);
        MockContext ctx = new MockContext();
        ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testBuildingWithoutTabsAndActions() {
        // GIVEN
        final DialogBuilder builder = new DialogBuilder();
        final DialogDefinition def = new ConfiguredDialogDefinition();
        final FormDialog dialog = new FormDialog();

        // WHEN
        final DialogView result = builder.buildFormDialog(null, def, null, dialog);

        // THEN
        assertEquals(result, dialog);
    }

    @Test
    public void testBuildingWithTabsAndActions() throws Exception {
        // GIVEN
        final String propertyName = "test";
        final DialogBuilder builder = new DialogBuilder();
        final ConfiguredDialogDefinition dialogDef = new ConfiguredDialogDefinition();
        final TextFieldDefinition fieldTypeDef = new TextFieldDefinition();
        fieldTypeDef.setName(propertyName);

        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);

        final FormDialog dialog = new FormDialog();
        final ConfiguredTabDefinition tabDef = new ConfiguredTabDefinition();
        final ConfiguredFieldDefinition fieldDef = new ConfiguredFieldDefinition();
        fieldDef.setName(propertyName);
        tabDef.addField(fieldDef);
        dialogDef.addTab(tabDef);

        final DialogFieldFactory dialogFieldFactory = mock(DialogFieldFactory.class);
        TextFieldBuilder editField = new TextFieldBuilder(fieldTypeDef, item);
        DefaultI18nContentSupport i18nContentSupport = new DefaultI18nContentSupport();
        i18nContentSupport.setFallbackLocale(new Locale("en"));
        editField.setI18nContentSupport(i18nContentSupport);
        when(dialogFieldFactory.create(same(fieldDef), same(item))).thenReturn(editField);

        // WHEN
        final DialogView result = builder.buildFormDialog(dialogFieldFactory, dialogDef, item, dialog);

        // THEN
        assertEquals(result, dialog);
    }

}