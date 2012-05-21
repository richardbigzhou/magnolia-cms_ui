/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.m5.dialog.gwt.client;


import java.util.HashMap;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * GWT implementation of MagnoliaShell client side (the view part basically).
 * @author apchelintcev
 *
 */
public class VDialogGWT extends FlowPanel {

    private TabPanel tabs = new TabPanel();
    HashMap<String, Panel> tabMapping = new HashMap<String, Panel>();

    private FlowPanel header = new FlowPanel();
    private FlowPanel actions = new FlowPanel();
    public VDialogGWT() {
        setStylePrimaryName("dialog-panel");
        this.tabs.setStyleName("dialog-tabs");
        this.actions.setStyleName("dialog-controls");
        add(tabs);
        add(actions);
    }

    void addTab(String name) {
        Panel tabView = new FlowPanel();

        this.tabs.add(tabView, name);
        tabMapping.put(name, tabView);
    }

    private void addActions() {

    }

    void addField(String tabName, String fieldLabel) {
        Panel tab = tabMapping.get(tabName);
        InlineLabel label = new InlineLabel(fieldLabel);
        TextBox input = new TextBox();
        input.setText("Some Input");
        tab.add(label);
        tab.add(input);
    }
}
