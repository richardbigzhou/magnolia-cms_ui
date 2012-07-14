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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.admincentral.field.DialogEditField;
import info.magnolia.ui.admincentral.field.builder.FieldTypeProvider;
import info.magnolia.ui.model.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.model.dialog.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.model.dialog.definition.ConfiguredTabDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.EditFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.widget.dialog.Dialog;
import info.magnolia.ui.widget.dialog.DialogView;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class DialogBuilderTest {

    private final String worksapceName = "workspace";

    private MockSession session;

    @Before
    public void setUp() {
        session = new MockSession(worksapceName);
        MockContext ctx = new MockContext();
        ctx.addSession(worksapceName, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testBuildingWithoutTabsAndActions() {
        // GIVEN
        final DialogBuilder builder = new DialogBuilder();
        final DialogDefinition def = new ConfiguredDialogDefinition();
        final Dialog dialog = new Dialog();

        // WHEN
        final DialogView result = builder.build(null, def, null, dialog);

        // THEN
        assertEquals(result, dialog);
    }

    @Test
    public void testBuildingWithTabsAndActions() throws Exception {
        // GIVEN
        final String propertyName = "test";
        final DialogBuilder builder = new DialogBuilder();
        final ConfiguredDialogDefinition dialogDef = new ConfiguredDialogDefinition();
        final EditFieldDefinition fieldTypeDef = new EditFieldDefinition();
        fieldTypeDef.setName(propertyName);

        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);

        final Dialog dialog = new Dialog();
        final ConfiguredTabDefinition tabDef = new ConfiguredTabDefinition();
        final ConfiguredFieldDefinition fieldDef = new ConfiguredFieldDefinition();
        fieldDef.setName(propertyName);
        tabDef.addField(fieldDef);
        dialogDef.addTab(tabDef);

        final FieldTypeProvider fieldTypeProvider = mock(FieldTypeProvider.class);
        when(fieldTypeProvider.create(fieldDef, fieldDef, item)).thenReturn(new DialogEditField(fieldTypeDef, item));

        // WHEN
        final DialogView result = builder.build(fieldTypeProvider, dialogDef, item, dialog);

        // THEN
        assertEquals(result, dialog);
    }

}

