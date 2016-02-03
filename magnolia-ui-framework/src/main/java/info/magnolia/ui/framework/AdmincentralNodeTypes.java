/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.framework;

import info.magnolia.jcr.util.NodeTypes;

/**
 * Constants and convenience methods for node types introduced by Admincentral.
 */
public class AdmincentralNodeTypes {

    /**
     * Represents the node type mgnl:systemMessage.
     */
    public static class SystemMessage {

        public static final String NAME = NodeTypes.MGNL_PREFIX + "systemMessage";

        public static final String ID = "id";
        public static final String TIMESTAMP = "timestamp";
        public static final String MESSAGETYPE = "messagetype";
        public static final String SUBJECT = "subject";
        public static final String MESSAGE = "message";
        public static final String CLEARED = "cleared";
        public static final String SENDER = "sender";
    }

    /**
     * Represents the node type mgnl:favorite.
     */
    public static class Favorite {

        public static final String NAME = NodeTypes.MGNL_PREFIX + "favorite";

        public static final String URL = "url";
        public static final String TITLE = "title";
        public static final String ICON = "icon";
        public static final String GROUP = "group";
    }

    /**
     * Represents the node type mgnl:favoriteGroup.
     */
    public static class FavoriteGroup {

        public static final String NAME = Favorite.NAME + "Group";
    }
}
