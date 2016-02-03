/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.workbench.column;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.NodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.workbench.column.definition.PropertyTypeColumnDefinition;
import info.magnolia.ui.workbench.column.definition.StatusColumnDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Table;

/**
 * Test class.
 */
public class StatusColumnFormatterTest extends RepositoryTestCase {
    private Table table;
    private Node node;
    private JcrItemId itemId;
    private Session session;
    private final StatusColumnDefinition statusColumnDefinition = new StatusColumnDefinition();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init parent Node
        String nodeProperties =
                "/parent.@type=mgnl:page\n" +
                        "/parent.propertyString=hello\n" +
                        "/parent/child.@type=mgnl:content\n" +
                        "/parent/child.propertyString=chield1\n";

        session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        new PropertiesImportExport().createNodes(session.getRootNode(), IOUtils.toInputStream(nodeProperties));
        session.save();

        node = session.getRootNode().getNode("parent");
        node.addMixin(NodeTypes.LastModified.NAME);
        node.addMixin(NodeTypes.Activatable.NAME);
        itemId = JcrItemUtil.getItemId(node);

        ConfiguredWorkbenchDefinition configuredWorkbench = new ConfiguredWorkbenchDefinition();

        // Add view
        ConfiguredContentPresenterDefinition contentView = new TreePresenterDefinition();
        configuredWorkbench.addContentView(contentView);

        PropertyTypeColumnDefinition colDef1 = new PropertyTypeColumnDefinition();
        colDef1.setSortable(true);
        colDef1.setName("propertyString");
        colDef1.setLabel("Label_" + "propertyString");

        contentView.addColumn(colDef1);

        NodeTypeDefinition nodeTypeDefinition = new ConfiguredNodeTypeDefinition();
        ((ConfiguredNodeTypeDefinition) nodeTypeDefinition).setName(NodeTypes.Content.NAME);

        ConfiguredJcrContentConnectorDefinition connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        connectorDefinition.setRootPath("/parent");
        connectorDefinition.setWorkspace(RepositoryConstants.WEBSITE);
        connectorDefinition.addNodeType(nodeTypeDefinition);

        ConfiguredWorkbenchDefinition workbenchDefinition = configuredWorkbench;

        HierarchicalJcrContainer hierarchicalJcrContainer = new HierarchicalJcrContainer(connectorDefinition);

        table = new Table();
        table.setContainerDataSource(hierarchicalJcrContainer);

        statusColumnDefinition.setPermissions(false);
    }

    @Test
    public void testActivationStatusNotActivated() throws Exception {
        // GIVEN
        SimpleTranslator i18n = mock(SimpleTranslator.class);
        when(i18n.translate("activation-status.columns.not-activated")).thenReturn("not-activated");
        StatusColumnFormatter statusColumnFormatter = new StatusColumnFormatter(statusColumnDefinition, i18n);

        // WHEN
        Object res = statusColumnFormatter.generateCell(table, itemId, null);

        // THEN
        assertNotNull(res);
        // RED, not activated
        assertEquals("<span class=\"icon-shape-circle activation-status color-red\" title=\"not-activated\"></span>" + "<span class=\"hidden-for-aria\">not-activated</span>", res.toString());
    }

    @Test
    public void testActivationStatusActivated() throws Exception {
        // GIVEN
        SimpleTranslator i18n = mock(SimpleTranslator.class);
        when(i18n.translate("activation-status.columns.activated")).thenReturn("activated");
        NodeTypes.Activatable.update(node, "superuser", true);
        StatusColumnFormatter statusColumnFormatter = new StatusColumnFormatter(statusColumnDefinition, i18n);

        // WHEN
        Object res = statusColumnFormatter.generateCell(table, itemId, null);

        // THEN
        assertNotNull(res);
        // GREEN, was activated
        assertEquals("<span class=\"icon-shape-circle activation-status color-green\" title=\"activated\"></span>" + "<span class=\"hidden-for-aria\">activated</span>", res.toString());
    }

    @Test
    public void testActivationStatusModified() throws Exception {
        // GIVEN
        SimpleTranslator i18n = mock(SimpleTranslator.class);
        when(i18n.translate("activation-status.columns.modified")).thenReturn("modified");
        NodeTypes.Activatable.update(node, "superuser", true);
        Thread.sleep(5); // make sure lastActivated > lastModified, otherwise test fails if both happen to be set to the same millisecond (activation status is incorrect)
        node.setProperty("blabla", "He - I just modified the node. LUD wrapper should trigger updated of lastModified property...");
        node.getSession().save();
        StatusColumnFormatter statusColumnFormatter = new StatusColumnFormatter(statusColumnDefinition, i18n);

        // WHEN
        Object res = statusColumnFormatter.generateCell(table, itemId, null);

        // THEN
        assertNotNull(res);
        // YELLOW, was activated and then modified
        assertEquals("<span class=\"icon-shape-circle activation-status color-yellow\" title=\"modified\"></span>" + "<span class=\"hidden-for-aria\">modified</span>", res.toString());
    }

    @Test
    public void testReadPermissionsAreNotShown() throws Exception {
        // GIVEN
        SimpleTranslator i18n = mock(SimpleTranslator.class);
        when(i18n.translate("activation-status.columns.not-activated")).thenReturn("not-activated");
        statusColumnDefinition.setActivation(false);
        StatusColumnFormatter statusColumnFormatter = new StatusColumnFormatter(statusColumnDefinition, i18n);

        // WHEN
        Object res = statusColumnFormatter.generateCell(table, itemId, null);

        // THEN
        assertNotNull(res);
        assertEquals("", res.toString());
    }
}
