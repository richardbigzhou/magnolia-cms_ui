/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.form.field.transformer.basic;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.security.SecurityUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Main test class for {@link BCryptTransformer}.
 */
public class BCryptTransformerTest {

    Node dialogNode = null;
    private ConfiguredFieldDefinition definition = new ConfiguredFieldDefinition();
    private String propertyName = "password";
    private BCryptTransformer transformer;

    @Before
    public void setUp() throws Exception {
        MockContext mockContext = MockUtil.initMockContext();
        Session session = SessionTestUtil.createSession("test",
                "/dialog.@type=" + NodeTypes.ContentNode.NAME + "\n" +
                        "/dialog.name=dialogName\n"
                );
        mockContext.addSession("test", session);
        dialogNode = session.getNode("/dialog");
        // init Definition
        definition.setTransformerClass(BCryptTransformer.class);
        definition.setName("password");
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testReadEncriptedData() throws RepositoryException {
        // GIVEN
        String encriptedPassword = SecurityUtil.getBCrypt("password");
        dialogNode.setProperty(propertyName, encriptedPassword);
        JcrNodeAdapter item = new JcrNodeAdapter(dialogNode);
        transformer = new BCryptTransformer(item, definition, String.class, mock(I18NAuthoringSupport.class));

        // WHEN
        String res = transformer.readFromItem();

        // THEN
        assertNotNull(res);
        assertEquals(res, encriptedPassword);
    }

    @Test
    public void testWriteData() {
        // GIVEN
        String newPassword = "New password";
        JcrNodeAdapter item = new JcrNodeAdapter(dialogNode);
        item.addItemProperty(propertyName, DefaultPropertyUtil.newDefaultProperty(String.class, newPassword));
        transformer = new BCryptTransformer(item, definition, String.class, mock(I18NAuthoringSupport.class));

        // WHEN
        transformer.writeToItem(newPassword);

        // THEN
        assertNotNull(item.getItemProperty(propertyName));
        assertTrue(SecurityUtil.matchBCrypted(newPassword, (String) item.getItemProperty(propertyName).getValue()));
    }

}
