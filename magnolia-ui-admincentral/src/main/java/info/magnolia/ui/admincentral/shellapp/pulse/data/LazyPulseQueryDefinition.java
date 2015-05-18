/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.data;

import java.util.Collections;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

/**
 * {@link LazyQueryDefinition} extension specific for Pulse shell-app. Allows for specifying:
 * <ul>
 * <li>user name</li>
 * <li>types of entries to display</li>
 * <li>grouping flag</li>
 * </ul>
 *
 * Pre-sets the batch size to 75 entries. Pre-sets the item identification property to <code>id</code>.
 *
 * @param <ET> entry type
 * @see info.magnolia.ui.api.message.MessageType
 * @see info.magnolia.task.Task.Status
 * @see info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageQueryDefinition
 * @see info.magnolia.ui.admincentral.shellapp.pulse.task.data.TaskQueryDefinition
 */
public class LazyPulseQueryDefinition<ET> extends LazyQueryDefinition implements PulseQueryDefinition<ET> {

    public static final String ID = "id";

    public static final int DEFAULT_BATCH_SIZE = 150;

    private boolean isGroupingByType = false;

    private List<ET> types = Collections.emptyList();

    private String userName = null;

    public LazyPulseQueryDefinition() {
        super(false, DEFAULT_BATCH_SIZE, ID);
    }

    @Override
    public List<ET> types() {
        return types;
    }

    @Override
    public void setTypes(List<ET> types) {
        this.types = types;
    }

    @Override
    public boolean isGroupingByType() {
        return isGroupingByType;
    }

    @Override
    public void setGroupingByType(boolean isGroupingByCategory) {
        this.isGroupingByType = isGroupingByCategory;
    }

    @Override
    public String userName() {
        return userName;
    }

    @Override
    public void setUserName(String userId) {
        this.userName = userId;
    }
}
