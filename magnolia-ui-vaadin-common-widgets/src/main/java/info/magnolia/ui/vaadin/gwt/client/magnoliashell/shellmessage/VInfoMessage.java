/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.MagnoliaShellView;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * Info message.
 */
public class VInfoMessage extends VDetailedShellMessage {

    private Element detailsExpanderEl = DOM.createElement("span");

    public VInfoMessage(MagnoliaShellView shell, String topic, String message, String id) {
        super(shell, topic, message, id);
        addStyleName("info");
    }

    @Override
    protected void construct() {
        super.construct();

        detailsExpanderEl.setInnerText("[MORE]");  //TODO-TRANSLATE, but not with Message-Util
        detailsExpanderEl.setClassName("details-expander");
        getHeader().appendChild(detailsExpanderEl);

    }

    @Override
    protected void onMessageClicked(Element targetEl) {
        if (detailsExpanderEl == targetEl) {
            getShell().navigateToMessageDetails(getId());
        }
        super.onMessageClicked(targetEl);
    }

    @Override
    protected String getMessageTypeCaption() {
        return "Info: ";  //TODO-TRANSLATE, but not with Message-geUUtil
    }
}
