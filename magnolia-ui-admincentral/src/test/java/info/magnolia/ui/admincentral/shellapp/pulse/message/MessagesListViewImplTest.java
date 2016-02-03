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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.admincentral.shellapp.pulse.message.MessagesListViewImpl.MessageSubjectColumnGenerator;
import info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageConstants;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Table;

/**
 * Tests for the Pulse {@link MessagesListViewImpl}.
 */
public class MessagesListViewImplTest {

    @Before
    public void setUp() {
        WebContext ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);
        ComponentsTestUtil.setInstance(WebContext.class, ctx);
        ComponentsTestUtil.setImplementation(SystemContext.class, MockContext.class);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testEnsureMessageIsEscaped() throws Exception {
        // GIVEN
        MessagesListViewImpl view = new MessagesListViewImpl(mock(SimpleTranslator.class));
        HierarchicalContainer container = mock(HierarchicalContainer.class);
        String itemId = "1234";
        when(container.getContainerProperty(itemId, "message")).thenReturn(new DefaultProperty(String.class, "<span onmouseover=\"alert('xss')\">bug</span>"));
        when(container.getContainerProperty(itemId, MessageConstants.SUBJECT_PROPERTY_ID)).thenReturn(new DefaultProperty(String.class, "subject"));
        Table source = new Table();
        source.setContainerDataSource(container);
        MessageSubjectColumnGenerator messageColumnGenerator = view.new MessageSubjectColumnGenerator();

        // WHEN
        String cell = (String) messageColumnGenerator.generateCell(source, itemId, "message");

        // THEN
        assertThat(cell, containsString("<strong>subject</strong>"));
        assertThat(cell, containsString("&lt;span onmouseover=&quot;alert(&apos;xss&apos;)&quot;&gt;bug&lt;/..."));
    }
}
