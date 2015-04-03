/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.form.tab.connector;

import com.vaadin.shared.ui.Connect;
import info.magnolia.ui.vaadin.form.tab.MagnoliaFormTab;
import info.magnolia.ui.vaadin.gwt.client.form.tab.widget.FormTabWidget;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.tab.connector.MagnoliaTabConnector;

/**
 * Client-side connector, counter-part for {@link MagnoliaFormTab}.
 */
@Connect(MagnoliaFormTab.class)
public class FormTabConnector extends MagnoliaTabConnector {

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public FormTabState getState() {
        return (FormTabState) super.getState();
    }

    @Override
    protected FormTabState createState() {
        return new FormTabState();
    }

    @Override
    public FormTabWidget getWidget() {
        return (FormTabWidget) super.getWidget();
    }

    @Override
    protected FormTabWidget createWidget() {
        return new FormTabWidget(this);
    }
}
