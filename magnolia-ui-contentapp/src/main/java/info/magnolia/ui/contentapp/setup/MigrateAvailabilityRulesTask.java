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
package info.magnolia.ui.contentapp.setup;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.QueryTask;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Substitutes availability rule class property with multiple availability rule definitions.
 */
public class MigrateAvailabilityRulesTask extends QueryTask {

    protected static final String RULE_CLASS = "ruleClass";
    protected static final String RULES = "rules";
    private static final String QUERY = " select * from [mgnl:contentNode] as t where name(t) = 'availability' ";

    protected MigrateAvailabilityRulesTask() {
        super("Migrate availability rules", "Substitute availability rule class property with multiple availability rule definitions", RepositoryConstants.CONFIG, QUERY);
    }

    @Override
    protected void operateOnNode(InstallContext installContext, Node node) {
        try {
            if (node.hasProperty(RULE_CLASS)) {
                Property ruleClassProperty = node.getProperty(RULE_CLASS);
                String ruleClass = ruleClassProperty.getString();

                Node ruleNode = NodeUtil.createPath(node, RULES + "/" + resolveRuleName(ruleClass), NodeTypes.ContentNode.NAME);
                ruleNode.setProperty("implementationClass", ruleClass);

                ruleClassProperty.remove();
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException("Failed to migrate availability-rules.",e);
        }
    }

    private String resolveRuleName(String ruleClass) {
        return StringUtils.substringAfterLast(ruleClass, ".");
    }
}
