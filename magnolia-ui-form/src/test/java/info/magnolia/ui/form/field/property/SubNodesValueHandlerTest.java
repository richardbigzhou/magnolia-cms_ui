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
package info.magnolia.ui.form.field.property;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * .
 */
public class SubNodesValueHandlerTest extends RepositoryTestCase {
    private Node rootNode;
    private final String subNodeName = "subNodeName";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init parent Node
        String nodeProperties =
                "/parent.@type=mgnl:content\n" +
                        "/parent.propertyString=hello\n" +
                        "/parent/subNodeName.@type=mgnl:content\n" +
                        "/parent/subNodeName/aaa.@type=mgnl:content\n" +
                        "/parent/subNodeName/aaa.subNodeName=value1\n" +
                        "/parent/subNodeName/bbb.@type=mgnl:content\n" +
                        "/parent/subNodeName/bbb.subNodeName=value2\n";

        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        new PropertiesImportExport().createNodes(session.getRootNode(), IOUtils.toInputStream(nodeProperties));
        session.save();

        rootNode = session.getRootNode().getNode("parent");
    }

    @Test
    public void testCreateMultiProperty() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        SubNodesValueHandler delegate = new SubNodesValueHandler(parent, subNodeName);

        // WHEN
        delegate.setValue(new ArrayList<String>());

        // THEN
        assertTrue(parent.getItemProperty(subNodeName) == null);
        assertTrue(!parent.applyChanges().hasNode(subNodeName));
    }

    @Test
    public void testReadMultiProperty() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        SubNodesValueHandler delegate = new SubNodesValueHandler(parent, subNodeName);

        // WHEN
        List<String> res = delegate.getValue();

        // THEN
        assertEquals(2, res.size());
        assertTrue(res.contains("value1"));
        assertTrue(res.contains("value2"));
    }

    @Test
    public void testUpdateMultiProperty() throws RepositoryException {
        // GIVEN
        String[] newValues = { "Pig", "Ph" };
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        SubNodesValueHandler delegate = new SubNodesValueHandler(parent, subNodeName);

        // WHEN
        delegate.setValue(Arrays.asList(newValues));

        // THEN
        assertTrue(parent.getItemProperty(subNodeName) == null);
        assertTrue(parent.getChild(subNodeName) != null);
        assertTrue(parent.getChild(subNodeName).getChildren() != null);
        assertEquals(2, parent.getChild(subNodeName).getChildren().size());
        assertTrue(parent.applyChanges().hasNode(subNodeName));
        Node child = parent.getJcrItem().getNode(subNodeName);
        assertTrue(child.hasNodes());
        assertEquals(2, child.getNodes().getSize());
        NodeIterator iterator = child.getNodes();
        assertEquals("Pig", iterator.nextNode().getProperty(subNodeName).getString());
        assertEquals("Ph", iterator.nextNode().getProperty(subNodeName).getString());
    }

    @Test
    public void testUpdateMultiPropertyCheckName() throws RepositoryException {
        // GIVEN
        String[] newValues = { "a", "1234567890123456789012" };
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        SubNodesValueHandler delegate = new SubNodesValueHandler(parent, subNodeName);

        // WHEN
        delegate.setValue(Arrays.asList(newValues));

        // THEN
        Node child = parent.applyChanges().getNode(subNodeName);
        assertTrue(child.hasNodes());
        assertEquals(2, child.getNodes().getSize());
        NodeIterator iterator = child.getNodes();
        Node child_1 = iterator.nextNode();
        assertEquals("a", child_1.getName());
        Node child_2 = iterator.nextNode();
        assertEquals("12345678901234567890", child_2.getName());
    }
}
