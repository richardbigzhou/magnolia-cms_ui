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
package info.magnolia.ui.contentapp.movedialog.predicate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link MovePossibilityPredicate} and its children.
 */
public class MovePossibilityPredicateTest {

    private final String workspaceName = "workspace";

    private MockSession session;

    private MockContext ctx;

    private DropConstraint dummyConstraint = mock(DropConstraint.class);

    private final String nodeName1 = "node1";

    private final String nodeName2 = "node2";

    private final String nodeName3 = "node3";

    private Node node3;

    private Node node1;

    private Node node2;

    private JcrPropertyAdapter propertyAdapter;

    @Before
    public void setUp() {
        this.session = new MockSession(workspaceName);
        this.ctx = new MockContext();
        this.ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);

        try {
            node3 = session.getRootNode().addNode(nodeName3);
            node1 = node3.addNode(nodeName1);
            node2 = session.getRootNode().addNode(nodeName2);
            node2.setProperty("testProperty", "");
            propertyAdapter = new JcrPropertyAdapter(node2.getProperty("testProperty"));
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testIsMovePossible() throws Exception {
        //GIVEN
        DropConstraint constraint = mock(DropConstraint.class);
        JcrNodeAdapter a1 = new JcrNodeAdapter(node1);
        JcrNodeAdapter a2 = new JcrNodeAdapter(node2);
        JcrNodeAdapter a3 = new JcrNodeAdapter(node3);

        MoveInsidePossibilityPredicate insidePredicate = new MoveInsidePossibilityPredicate(constraint, Arrays.asList(a2, a3));
        MoveAfterPossibilityPredicate afterPredicate = new MoveAfterPossibilityPredicate(constraint, Arrays.asList(a2, a3));
        MoveAfterPossibilityPredicate beforePredicate = new MoveAfterPossibilityPredicate(constraint, Arrays.asList(a2, a3));

        //WHEN
        when(constraint.allowedAfter(a2, a1)).thenReturn(false);
        when(constraint.allowedBefore(a2, a1)).thenReturn(false);
        when(constraint.allowedAsChild(a2, a1)).thenReturn(true);
        when(constraint.allowedAsChild(a3, a1)).thenReturn(true);

        //THEN
        assert(insidePredicate.isMovePossible(a1));
        assert(!afterPredicate.isMovePossible(a1));
        assert(!beforePredicate.isMovePossible(a1));
    }

    @Test
    public void testHostIsRoot() throws Exception {
        //GIVEN
        MovePossibilityPredicate predicate = new MovePossibilityPredicate(dummyConstraint, Arrays.asList(new JcrNodeAdapter(node1)));

        //THEN
        assert(predicate.hostIsRoot(new JcrNodeAdapter(session.getRootNode())));
        assert(!predicate.hostIsRoot(new JcrNodeAdapter(node2)));
    }

    @Test
    public void testBasicMoveCheck() throws Exception {
        //GIVEN
        JcrNodeAdapter adapter1 = new JcrNodeAdapter(node1);
        MovePossibilityPredicate predicate = new MovePossibilityPredicate(dummyConstraint, Arrays.asList(adapter1));

        //THEN
        assert(!predicate.basicMoveCheck(node1, node1));
        assert(predicate.basicMoveCheck(node1, node2));
        assert(!predicate.basicMoveCheck(node1, propertyAdapter.getJcrItem()));
        assert(!predicate.basicMoveCheck(node1, node3));
    }
}
