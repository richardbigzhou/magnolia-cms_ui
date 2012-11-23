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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import info.magnolia.cms.util.QueryUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

/**
 * Changes all properties in a workspace having a certain value set.
 */
public class ChangeAllPropertiesWithCertainValueTask extends AbstractRepositoryTask {

    private String currentValue;
    private String newValue;
    private String workspaceName;

    public ChangeAllPropertiesWithCertainValueTask(String name, String description, String workspaceName, String currentValue, String newValue) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.currentValue = currentValue;
        this.newValue = newValue;
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {

        String query = "select * from [nt:base] as t where contains(t.*,'" + currentValue + "')";
        NodeIterator nodeIterator = QueryUtil.search(workspaceName, query, Query.JCR_SQL2);
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.nextNode();
            PropertyIterator iterator = node.getProperties();
            while (iterator.hasNext()) {
                Property property = iterator.nextProperty();
                if (property.getType() == PropertyType.STRING) {
                    String string = property.getString();
                    if (string.equals(currentValue)) {
                        property.setValue(newValue);
                    }
                }
            }
        }
    }
}
