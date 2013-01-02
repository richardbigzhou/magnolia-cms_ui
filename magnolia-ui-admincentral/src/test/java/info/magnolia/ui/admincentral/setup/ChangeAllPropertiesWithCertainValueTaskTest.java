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
package info.magnolia.ui.admincentral.setup;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;

/**
 * Test case for {@link ChangeAllPropertiesWithCertainValueTask}.
 */
public class ChangeAllPropertiesWithCertainValueTaskTest extends RepositoryTestCase {

    @Test
    public void testChangeAllPropertiesWithCertainValueTask() throws RepositoryException, TaskExecutionException {

        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);

        Node parent = session.getRootNode().addNode("parent");
        parent.setProperty("propertyWithOldValue", "oldValue");
        parent.setProperty("propertyWithOtherValue", "otherValue");

        Node child = parent.addNode("child");
        child.setProperty("propertyWithOldValue", "oldValue");
        child.setProperty("propertyWithOtherValue", "otherValue");

        session.save();

        ChangeAllPropertiesWithCertainValueTask task = new ChangeAllPropertiesWithCertainValueTask("", "", RepositoryConstants.CONFIG, "oldValue", "newValue");

        task.execute(mock(InstallContextImpl.class));

        assertEquals("newValue", parent.getProperty("propertyWithOldValue").getString());
        assertEquals("otherValue", parent.getProperty("propertyWithOtherValue").getString());

        assertEquals("newValue", parent.getProperty("child/propertyWithOldValue").getString());
        assertEquals("otherValue", parent.getProperty("child/propertyWithOtherValue").getString());
    }

    @Test
    public void testWorksWithClassName() throws RepositoryException, TaskExecutionException {

        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);

        Node parent = session.getRootNode().addNode("parent");
        parent.setProperty("propertyWithOldValue", "info.magnolia.package.Class");
        parent.setProperty("propertyWithOtherValue", "otherValue");

        Node child = parent.addNode("child");
        child.setProperty("propertyWithOldValue", "info.magnolia.package.Class");
        child.setProperty("propertyWithOtherValue", "otherValue");

        session.save();

        ChangeAllPropertiesWithCertainValueTask task = new ChangeAllPropertiesWithCertainValueTask("", "", RepositoryConstants.CONFIG, "info.magnolia.package.Class", "info.magnolia.package.NewClass");

        task.execute(mock(InstallContextImpl.class));

        assertEquals("info.magnolia.package.NewClass", parent.getProperty("propertyWithOldValue").getString());
        assertEquals("otherValue", parent.getProperty("propertyWithOtherValue").getString());

        assertEquals("info.magnolia.package.NewClass", parent.getProperty("child/propertyWithOldValue").getString());
        assertEquals("otherValue", parent.getProperty("child/propertyWithOtherValue").getString());
    }
}
