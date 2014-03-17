/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.framework.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.QueryTask;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task which is changing values for availability@ruleClass-properties in the config app- for a few classes which have been moved from
 * package info.magnolia.ui.api.availability to package info.magnolia.ui.framework.availability.
 */
public class ChangeJcrDependentAvailabilityRuleClassesFqcnTask extends QueryTask {

    private static final String QUERY = " select * from [mgnl:contentNode] as t where name(t) = 'availability' ";
    public static final String RULE_CLASS = "ruleClass";

    private static String hasVersionsRuleOldFqcn = "info.magnolia.ui.api.availability.HasVersionsRule";
    private static String hasVersionsRuleNewFqcn = "info.magnolia.ui.framework.availability.HasVersionsRule";
    private static String iIsDeletedRuleOldFqcn = "info.magnolia.ui.api.availability.IsDeletedRule";
    private static String iIsDeletedRuleNewFqcn = "info.magnolia.ui.framework.availability.IsDeletedRule";
    private static String iIsNotDeletedRuleOldFqcn = "info.magnolia.ui.api.availability.IsNotDeletedRule";
    private static String iIsNotDeletedRuleNewFqcn = "info.magnolia.ui.framework.availability.IsNotDeletedRule";
    private static String iIsNotVersionedRuleOldFqcn = "info.magnolia.ui.api.availability.IsNotVersionedRule";
    private static String iIsNotVersionedRuleNewFqcn = "info.magnolia.ui.framework.availability.IsNotVersionedRule";



    private Logger log = LoggerFactory.getLogger(getClass());


    public ChangeJcrDependentAvailabilityRuleClassesFqcnTask() {
        super("Change availability@ruleClass-properties for classes which have been moved from package info.magnolia.ui.api.availability to package info.magnolia.ui.framework.availability.",
                "Changing availability@ruleClass-properties for classes which have been moved from package info.magnolia.ui.api.availability to package info.magnolia.ui.framework.availability.", RepositoryConstants.CONFIG, QUERY);
    }

    @Override
    protected void operateOnNode(InstallContext installContext, Node node) {
        try {
            if (node.hasProperty(RULE_CLASS)) {
                String classRulePropertyValue = node.getProperty(RULE_CLASS).getString();

                if(hasVersionsRuleOldFqcn.equals(classRulePropertyValue)){
                    node.setProperty(RULE_CLASS,hasVersionsRuleNewFqcn);
                }else if(iIsDeletedRuleOldFqcn.equals(classRulePropertyValue)){
                    node.setProperty(RULE_CLASS, iIsDeletedRuleNewFqcn);
                }else if(iIsNotDeletedRuleOldFqcn.equals(classRulePropertyValue)){
                    node.setProperty(RULE_CLASS, iIsNotDeletedRuleNewFqcn);
                }else if(iIsNotVersionedRuleOldFqcn.equals(classRulePropertyValue)){
                    node.setProperty(RULE_CLASS, iIsNotVersionedRuleNewFqcn);
                }
            }
        } catch (RepositoryException e) {
            log.error("Unable to process app node ", e);
        }
    }
}
