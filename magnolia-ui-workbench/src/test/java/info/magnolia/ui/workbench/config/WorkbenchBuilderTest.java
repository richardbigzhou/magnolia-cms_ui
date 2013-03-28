/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.workbench.config;

import static org.junit.Assert.assertEquals;

import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.tree.drop.AlwaysTrueDropConstraint;

import org.junit.Test;

/**
 * Main test class for {@link WorkbenchBuilder.
 */
public class WorkbenchBuilderTest {
    
    private WorkbenchBuilder workbenchBuilder;
    
    @Test
    public void testConfigByCode() {
        // GIVEN
        workbenchBuilder = new WorkbenchBuilder();
        workbenchBuilder.name("workbenchBuilder").workspace(RepositoryConstants.WEBSITE).path("/path").defaultOrder(ModelConstants.JCR_NAME).nodeTypes(new NodeTypeBuilder("mgnl:folder"))
                .includeProperties(false).editable(true).dialogWorkbench(true).dropConstraintClass(AlwaysTrueDropConstraint.class);
        
        // WHEN
        ConfiguredWorkbenchDefinition definition = workbenchBuilder.exec();
        
        // THEN
        assertEquals("workbenchBuilder", definition.getName());
        assertEquals(RepositoryConstants.WEBSITE, definition.getWorkspace());
        assertEquals("/path", definition.getPath());
        assertEquals(ModelConstants.JCR_NAME, definition.getDefaultOrder());
        assertEquals("mgnl:folder", definition.getNodeTypes().get(0).getName());
        assertEquals(false, definition.includeProperties());
        assertEquals(true, definition.isEditable());
        assertEquals(true, definition.isDialogWorkbench());
        assertEquals(AlwaysTrueDropConstraint.class.getName(), definition.getDropConstraintClass().getName());
        
    }
}
