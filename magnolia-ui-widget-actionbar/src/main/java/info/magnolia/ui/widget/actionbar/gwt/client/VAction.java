/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.widget.actionbar.gwt.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;


/**
 * The Class VAction, which displays a single action with label and icon within an action group.
 */
public class VAction extends SimplePanel {

    private static final String CLASSNAME = "v-action";

    private static final String LABEL_CLASSNAME = "label";

    private final Element labelElement = DOM.createSpan();

    /**
     * Instantiates a new action bar group.
     */
    public VAction() {
        super(DOM.createElement("li"));
        init();
    }

    /**
     * Instantiates a new action bar group with given label.
     */
    public VAction(String label) {
        super(DOM.createElement("li"));
        labelElement.setInnerText(label);
        init();
    }

    private void init() {
        setStylePrimaryName(CLASSNAME);
        labelElement.setClassName(LABEL_CLASSNAME);
        getElement().appendChild(labelElement);
        InlineLabel secondLabel = new InlineLabel("-YEAH!");
        secondLabel.setStylePrimaryName(CLASSNAME);
        secondLabel.addStyleDependentName(LABEL_CLASSNAME);
        add(secondLabel);
    }

    public String getLabel() {
        return labelElement.getInnerText();
    }

    /**
     * Updates the action label.
     * 
     * @param label the label
     */
    public void updateLabel(String label) {
        labelElement.setInnerText(label);
    }

}
