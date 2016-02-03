/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.vaadin.overlay;

import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.vaadin.icon.CompositeIcon;
import info.magnolia.ui.vaadin.icon.ErrorIcon;
import info.magnolia.ui.vaadin.icon.InfoIcon;
import info.magnolia.ui.vaadin.icon.NullIcon;
import info.magnolia.ui.vaadin.icon.WarningIcon;

/**
 * Enumeration of configured MessageStyleTypes.
 */
public enum MessageStyleTypeEnum implements MessageStyleType {

    UNKNOWN("", "", NullIcon.class),
    ERROR("Error", "msgerror", ErrorIcon.class),
    WARNING("Warning", "msgwarn", WarningIcon.class),
    INFO("Info", "msginfo", InfoIcon.class);

    private String caption;
    private String cssClass;
    private Class<? extends CompositeIcon> iconClass;

    private MessageStyleTypeEnum(final String caption, final String cssClass, final Class<? extends CompositeIcon> iconClass) {
        this.caption = caption;
        this.cssClass = cssClass;
        this.iconClass = iconClass;
    }

    @Override
    public String toString() {
        return caption;
    }

    @Override
    public String getCssClass() {
        return cssClass;
    }

    @Override
    public Class<?> getIconClass() {
        return iconClass;
    }

}
