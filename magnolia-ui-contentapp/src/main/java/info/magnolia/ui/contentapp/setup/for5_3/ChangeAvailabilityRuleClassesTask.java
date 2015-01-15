/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.contentapp.setup.for5_3;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.NodeVisitorTask;
import info.magnolia.repository.RepositoryConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task which is changing values for availability@ruleClass-properties in the config app- for a few classes which have been moved from
 * package info.magnolia.ui.api.availability to package info.magnolia.ui.framework.availability.
 * This task normally is not meant to be used standalone.
 *
 * @see {@link ContentAppMigrationTask}
 */
public class ChangeAvailabilityRuleClassesTask extends NodeVisitorTask {

    private static final Logger log = LoggerFactory.getLogger(ChangeAvailabilityRuleClassesTask.class);

    private static final String RULE_CLASS = "ruleClass";

    private Map<String, String> classMappings;

    public ChangeAvailabilityRuleClassesTask(String path) {
        super("Update rule classes to ui-framework", "This task changes availability@ruleClass properties for classes which have been moved from package info.magnolia.ui.api.availability to package info.magnolia.ui.framework.availability.", RepositoryConstants.CONFIG, path);
    }

    public ChangeAvailabilityRuleClassesTask() {
        this("/");
    }

    @Override
    protected boolean nodeMatches(Node node) {
        try {
            return node.getPrimaryNodeType().getName().equals(NodeTypes.ContentNode.NAME) && node.getName().equals("availability");
        } catch (RepositoryException e) {
            log.error("Couldn't evaluate visited node's name or node-type", e);
        }
        return false;
    }

    @Override
    protected void operateOnNode(InstallContext installContext, Node node) {
        try {
            if (node.hasProperty(RULE_CLASS)) {
                String classRulePropertyValue = node.getProperty(RULE_CLASS).getString();
                if (getClassMapping().containsKey(classRulePropertyValue)) {
                    node.setProperty(RULE_CLASS, getClassMapping().get(classRulePropertyValue));
                }
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException("Failed to change availability-rule-classes.", e);
        }
    }

    private Map<String, String> getClassMapping() {
        if (classMappings == null) {
            classMappings = new HashMap<String, String>();
            classMappings.put("info.magnolia.ui.api.availability.HasVersionsRule", "info.magnolia.ui.framework.availability.HasVersionsRule");
            classMappings.put("info.magnolia.ui.api.availability.IsDeletedRule", "info.magnolia.ui.framework.availability.IsDeletedRule");
            classMappings.put("info.magnolia.ui.api.availability.IsNotDeletedRule", "info.magnolia.ui.framework.availability.IsNotDeletedRule");
            classMappings.put("info.magnolia.ui.api.availability.IsNotVersionedRule", "info.magnolia.ui.framework.availability.IsNotVersionedRule");
            classMappings = Collections.unmodifiableMap(classMappings);
        }
        return classMappings;
    }


}
