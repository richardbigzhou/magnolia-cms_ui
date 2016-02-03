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
package info.magnolia.security.app.dialog.field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.Permission;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.io.IOException;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Test case for {@link WorkspaceAccessFieldFactory}.
 */
public class WorkspaceAccessFieldFactoryTest extends MgnlTestCase {

    private SimpleTranslator i18n;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        i18n = mock(SimpleTranslator.class);
        when(i18n.translate("security.workspace.field.choose")).thenReturn("Choose...");
        when(i18n.translate("security.workspace.field.delete")).thenReturn("Delete");
        when(i18n.translate("security.workspace.field.noAccess")).thenReturn("No access.");
        when(i18n.translate("security.workspace.field.addButton")).thenReturn("Add new");
    }

    @Test
    public void testShowsAddButtonAndEmptyLabelWhenEmpty() {

        // GIVEN
        MockSession session = new MockSession(RepositoryConstants.CONFIG);
        MockUtil.setSessionAndHierarchyManager(session);
        JcrNewNodeAdapter item = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Content.NAME);

        WorkspaceAccessFieldDefinition definition = new WorkspaceAccessFieldDefinition();
        WorkspaceAccessFieldFactory builder = new WorkspaceAccessFieldFactory<WorkspaceAccessFieldDefinition>(definition, item, null, null, i18n);

        // WHEN
        Field<Object> field = builder.createFieldComponent();

        // THEN
        VerticalLayout layout = (VerticalLayout) ((HasComponents) field).iterator().next();
        assertEquals(1, layout.getComponentCount());

        VerticalLayout aclLayout = (VerticalLayout) layout.iterator().next();
        assertEquals(2, aclLayout.getComponentCount());

        Iterator<Component> aclLayoutIterator = aclLayout.iterator();
        Label emptyLabel = (Label) aclLayoutIterator.next();
        assertEquals("No access.", emptyLabel.getValue());

        HorizontalLayout buttonLayout = (HorizontalLayout) aclLayoutIterator.next();
        assertEquals(1, buttonLayout.getComponentCount());

        Button addButton = (Button) buttonLayout.iterator().next();
        assertEquals("Add new", addButton.getCaption());
    }

    @Test
    public void testShowsAclRuleFromRepository() throws IOException, RepositoryException {

        // GIVEN
        Session session = SessionTestUtil.createSession(RepositoryConstants.CONFIG,
                "/role/acl_config/0.permissions=long:63",
                "/role/acl_config/0.path=/*"
        );
        MockUtil.setSessionAndHierarchyManager(session);
        JcrNodeAdapter item = new JcrNodeAdapter(session.getNode("/role"));

        WorkspaceAccessFieldDefinition definition = new WorkspaceAccessFieldDefinition();
        definition.setWorkspace(RepositoryConstants.CONFIG);
        WorkspaceAccessFieldFactory builder = new WorkspaceAccessFieldFactory<WorkspaceAccessFieldDefinition>(definition, item, null, null, i18n);

        // WHEN
        Field<Object> field = builder.createFieldComponent();

        // THEN
        VerticalLayout layout = (VerticalLayout) ((HasComponents) field).iterator().next();
        assertEquals(1, layout.getComponentCount());

        VerticalLayout aclLayout = (VerticalLayout) layout.iterator().next();
        assertEquals(2, aclLayout.getComponentCount());

        Iterator<Component> aclLayoutIterator = aclLayout.iterator();

        assertEntryLayout((HorizontalLayout) aclLayoutIterator.next(), Permission.ALL, AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "/");

        HorizontalLayout buttonLayout = (HorizontalLayout) aclLayoutIterator.next();
        assertEquals(1, buttonLayout.getComponentCount());

        Button addButton = (Button) buttonLayout.iterator().next();
        assertEquals("Add new", addButton.getCaption());
    }

    @Test
    public void testShowsCombinedEntryAsOneRow() throws IOException, RepositoryException {

        // GIVEN
        Session session = SessionTestUtil.createSession(RepositoryConstants.CONFIG,
                "/role/acl_config/0.permissions=long:63",
                "/role/acl_config/0.path=/foobar",
                "/role/acl_config/1.permissions=long:63",
                "/role/acl_config/1.path=/foobar/*"
        );
        MockUtil.setSessionAndHierarchyManager(session);
        JcrNodeAdapter item = new JcrNodeAdapter(session.getNode("/role"));

        WorkspaceAccessFieldDefinition definition = new WorkspaceAccessFieldDefinition();
        definition.setWorkspace(RepositoryConstants.CONFIG);
        WorkspaceAccessFieldFactory builder = new WorkspaceAccessFieldFactory<WorkspaceAccessFieldDefinition>(definition, item, null, null, i18n);

        // WHEN
        Field<Object> field = builder.createFieldComponent();

        // THEN
        VerticalLayout layout = (VerticalLayout) ((HasComponents) field).iterator().next();
        assertEquals(1, layout.getComponentCount());

        VerticalLayout aclLayout = (VerticalLayout) layout.iterator().next();
        assertEquals(2, aclLayout.getComponentCount());

        Iterator<Component> aclLayoutIterator = aclLayout.iterator();

        assertEntryLayout((HorizontalLayout) aclLayoutIterator.next(), Permission.ALL, AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "/foobar");

        HorizontalLayout buttonLayout = (HorizontalLayout) aclLayoutIterator.next();
        assertEquals(1, buttonLayout.getComponentCount());

        Button addButton = (Button) buttonLayout.iterator().next();
        assertEquals("Add new", addButton.getCaption());
    }

    private void assertEntryLayout(HorizontalLayout entryLayout, long expectedPermissions, long expectedAccessType, String expectedPath) {
        Iterator<Component> entryLayoutIterator = entryLayout.iterator();

        NativeSelect permissions = (NativeSelect) entryLayoutIterator.next();
        assertEquals(expectedPermissions, permissions.getValue());

        NativeSelect accessType = (NativeSelect) entryLayoutIterator.next();
        assertEquals(expectedAccessType, accessType.getValue());

        TextField path = (TextField) entryLayoutIterator.next();
        assertEquals(expectedPath, path.getValue());

        Button chooseButton = (Button) entryLayoutIterator.next();
        assertEquals("Choose...", chooseButton.getCaption());

        Button deleteButton = (Button) entryLayoutIterator.next();
        assertEquals("<span class=\"" + "icon-trash" + "\"></span>", deleteButton.getCaption());
        assertEquals("Delete", deleteButton.getDescription());
    }
}
