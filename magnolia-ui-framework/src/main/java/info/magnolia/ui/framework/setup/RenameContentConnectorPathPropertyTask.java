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

import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.QueryTask;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task which is renaming the peroperty "path" to "rootPath" on  contentConnector-nodes.
 */
public class RenameContentConnectorPathPropertyTask extends QueryTask {

    private Logger log = LoggerFactory.getLogger(getClass());


    private static final String QUERY = " select * from [mgnl:contentNode] as t where name(t) = 'contentConnector' ";
    public static final String PATH_PROPERTY = "path";
    public static final String ROOTPATH_PROPERTY = "rootPath";


    public RenameContentConnectorPathPropertyTask() {
        super("Rename property 'path' to 'rootPath' on contentConnector-nodes.", "Renaming property 'path' to 'rootPath' on contentConnector-nodes.", RepositoryConstants.CONFIG, QUERY);
    }

    @Override
    protected void operateOnNode(InstallContext installContext, Node node) {
        try {
            if (node.hasProperty(PATH_PROPERTY)) {
                String value = PropertyUtil.getString(node, PATH_PROPERTY);
                node.setProperty(PATH_PROPERTY,(String)null);
                node.setProperty(ROOTPATH_PROPERTY, value);
            }
        } catch (RepositoryException e) {
            log.error("Unable to process app node ", e);
        }
    }
}
