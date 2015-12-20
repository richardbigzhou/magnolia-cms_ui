/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.workbench.column.definition;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.column.definition.NodeTypeColumnDefinition.NodeTypeColumnFormatter;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Table;

public class NodeTypeColumnFormatterTest {

    private NodeTypeColumnFormatter columnFormatter;

    private Table table;

    private Session session;

    private Node fooNode;

    private Node barNode;

    @Before
    public void setUp() throws Exception {
        this.columnFormatter = new NodeTypeColumnFormatter(new NodeTypeColumnDefinition());
        this.table = mock(Table.class);
        this.session = new MockSession("foo");

        fooNode = session.getRootNode().addNode("foo", "fooType");
        barNode = session.getRootNode().addNode("bar", "barType");

        doReturn(new JcrNodeAdapter(fooNode)).when(table).getItem(JcrItemUtil.getItemId(fooNode));
        doReturn(new JcrNodeAdapter(barNode)).when(table).getItem(JcrItemUtil.getItemId(barNode));

        final MockContext context = new MockContext();
        context.addSession("foo", session);
        MgnlContext.setInstance(context);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void renderNodeType() throws Exception {
        // WHEN
        final String fooNodeTypeColumnContent = (String) columnFormatter.generateCell(table, JcrItemUtil.getItemId(fooNode), null);
        final String barNodeTypeColumnContent = (String) columnFormatter.generateCell(table, JcrItemUtil.getItemId(barNode), null);

        // THEN
        assertThat(fooNodeTypeColumnContent, equalTo("fooType"));
        assertThat(barNodeTypeColumnContent, equalTo("barType"));
    }

    @Test
    public void renderPropertyType() throws Exception {
        // GIVEN
        fooNode.setProperty("doubleBaz", 0d);
        fooNode.setProperty("longBaz", 0l);
        fooNode.setProperty("dateBaz", Calendar.getInstance());

        final Property doubleBaz = fooNode.getProperty("doubleBaz");
        final Property longBaz = fooNode.getProperty("longBaz");
        final Property dateBaz = fooNode.getProperty("dateBaz");

        final JcrItemId doubleBazId = JcrItemUtil.getItemId(doubleBaz);
        final JcrItemId longBazId = JcrItemUtil.getItemId(longBaz);
        final JcrItemId dateBazId = JcrItemUtil.getItemId(dateBaz);

        doReturn(new JcrPropertyAdapter(doubleBaz)).when(table).getItem(doubleBazId);
        doReturn(new JcrPropertyAdapter(longBaz)).when(table).getItem(longBazId);
        doReturn(new JcrPropertyAdapter(dateBaz)).when(table).getItem(dateBazId);

        // WHEN
        final String doubleBazColumnContent = (String) columnFormatter.generateCell(table, doubleBazId, null);
        final String longBazColumnContent = (String) columnFormatter.generateCell(table, longBazId, null);
        final String dateBazColumnContent = (String) columnFormatter.generateCell(table, dateBazId, null);

        // THEN
        assertThat(doubleBazColumnContent, equalTo("Double"));
        assertThat(longBazColumnContent, equalTo("Long"));
        assertThat(dateBazColumnContent, equalTo("Date"));
    }

    @Test
    public void isNullSafe() throws Exception {
        // WHEN
        final String nullItemIdColumnContent = (String) columnFormatter.generateCell(table, null, null);

        // THEN
        assertThat(nullItemIdColumnContent, isEmptyString());
    }

}