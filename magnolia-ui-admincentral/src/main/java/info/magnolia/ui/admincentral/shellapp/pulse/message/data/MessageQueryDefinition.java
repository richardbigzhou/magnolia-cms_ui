/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.message.data;

import info.magnolia.ui.admincentral.shellapp.pulse.data.LazyPulseQueryDefinition;
import info.magnolia.ui.api.message.MessageType;

import java.util.Arrays;
import java.util.Date;

/**
 * Extends {@link info.magnolia.ui.admincentral.shellapp.pulse.data.LazyPulseQueryDefinition} for {@link info.magnolia.ui.api.message.Message}
 * object queries. Pre-defines such container properties as:
 * <ul>
 * <li>isNew</li>
 * <li>subject</li>
 * <li>type</li>
 * <li>message</li>
 * <li>sender</li>
 * <li>timestamp</li>
 * </ul>
 *
 * Sets <code>timestamp</code> property as a sorting criteria. By default allows all {@link MessageType}'s in the query.
 *
 * @see MessageConstants
 * @see MessageQuery
 */
public class MessageQueryDefinition extends LazyPulseQueryDefinition<MessageType> {

    public MessageQueryDefinition() {

        setTypes(Arrays.asList(MessageType.values()));

        addProperty(MessageConstants.NEW_PROPERTY_ID, Boolean.class, true, true, false);
        addProperty(MessageConstants.SUBJECT_PROPERTY_ID, String.class, null, true, false);
        addProperty(MessageConstants.TYPE_PROPERTY_ID, MessageType.class, MessageType.UNKNOWN, true, false);
        addProperty(MessageConstants.TEXT_PROPERTY_ID, String.class, null, true, false);
        addProperty(MessageConstants.SENDER_PROPERTY_ID, String.class, null, true, false);
        addProperty(MessageConstants.DATE_PROPERTY_ID, Date.class, null, true, true);
    }
}
