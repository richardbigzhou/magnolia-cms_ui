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
package info.magnolia.security.app.dialog.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;

/**
 * Test case for {@link SaveGroupDialogAction}.
 */
public class SaveGroupDialogActionTest extends RepositoryTestCase {

    @Test
    public void testUpdatesByOverwriting() throws RepositoryException, ActionExecutionException {

        // WHEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node node = session.getRootNode().addNode("group", NodeTypes.Content.NAME);
        node.addNode("groups", NodeTypes.ContentNode.NAME);
        node.addNode("roles", NodeTypes.ContentNode.NAME);

        // this intermittent save ensures that we get mgnl:lastModified and mgnl:lastModifiedBy added below
        session.save();

        node = session.getRootNode().getNode("group");
        node.getNode("groups").setProperty("0", "8");
        node.getNode("roles").setProperty("0", "9");
        session.save();

        JcrNodeAdapter item = new JcrNodeAdapter(node);
        item.addItemProperty("groups", new DefaultProperty<String>("1,2,3"));
        item.addItemProperty("roles", new DefaultProperty<String>("4,5,6"));

        SaveDialogActionDefinition definition = mock(SaveDialogActionDefinition.class);
        when(definition.getName()).thenReturn("save");
        EditorValidator editorValidator = mock(EditorValidator.class);
        when(editorValidator.isValid()).thenReturn(true);
        EditorCallback editorCallback = mock(EditorCallback.class);

        SaveGroupDialogAction action = new SaveGroupDialogAction(definition, item, editorValidator, editorCallback);

        // WHEN
        action.execute();

        // THEN
        assertFalse(node.hasProperty("groups"));
        assertTrue(node.hasNode("groups"));
        Node groupsNode = node.getNode("groups");
        assertTrue(groupsNode.hasProperty("0"));
        assertEquals("1", groupsNode.getProperty("0").getString());
        assertTrue(groupsNode.hasProperty("1"));
        assertEquals("2", groupsNode.getProperty("1").getString());
        assertTrue(groupsNode.hasProperty("2"));
        assertEquals("3", groupsNode.getProperty("2").getString());

        assertFalse(node.hasProperty("roles"));
        assertTrue(node.hasNode("roles"));
        Node rolesNode = node.getNode("roles");
        assertTrue(rolesNode.hasProperty("0"));
        assertEquals("4", rolesNode.getProperty("0").getString());
        assertTrue(rolesNode.hasProperty("1"));
        assertEquals("5", rolesNode.getProperty("1").getString());
        assertTrue(rolesNode.hasProperty("2"));
        assertEquals("6", rolesNode.getProperty("2").getString());
    }
}
