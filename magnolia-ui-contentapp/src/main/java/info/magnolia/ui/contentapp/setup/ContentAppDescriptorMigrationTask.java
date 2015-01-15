/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.QueryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes type of content app descriptors to {@link info.magnolia.ui.contentapp.ConfiguredContentAppDescriptor}.
 */
public class ContentAppDescriptorMigrationTask extends QueryTask {

    private static final Logger log = LoggerFactory.getLogger(ContentAppDescriptorMigrationTask.class);
    private NodeVisitor nodeVisitor = new AppNodeVisitor();

    public ContentAppDescriptorMigrationTask(String name, String description, String configRepository, String query) {
        super(name, description, configRepository, query);
    }

    public ContentAppDescriptorMigrationTask(String name, String description, String configRepository, String query, NodeVisitor nodeVisitor) {
        super(name, description, configRepository, query);
        this.nodeVisitor = nodeVisitor;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        log.info("Start content app descriptor migration ");
        try {
            super.doExecute(ctx);
            log.info("Successfully execute cleanup of the content repository ");
        } catch (Exception e) {
            log.error("Unable to clean content repository", e);
            ctx.error("Unable to perform Migration task " + getName(), e);
            throw new TaskExecutionException(e.getMessage());
        } finally {
            log.info("Finished content app descriptor migration ");
        }
    }

    @Override
    protected void operateOnNode(InstallContext ctx, Node node) {
        try {
            NodeUtil.visit(node.getParent(), nodeVisitor);
        } catch (RepositoryException e) {
            log.error("Unable to process app node ", e);
        }

    }
}
