/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.form.widget;

import info.magnolia.ui.vaadin.gwt.client.icon.widget.IconWidget;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * This class is an {@code IconWidget} which can be toggled.
 * It shows a "?" which indicates some help.
 * <br/>
 * Note, that there is no corresponding Vaadin-server-class available.
 */
public class HelpIconWidget extends IconWidget {

    private static final String CLASSNAME = "help-icon";
    private static final String highlighted = "highlighted";
    private static final String helpWithCircleClassName = "icon-help-l";

    private Element innerSpan1 = DOM.createSpan();
    private Element innerSpan2 = DOM.createSpan();
    private Element innerSpan3 = DOM.createSpan();

    /**
     * Creates a HelpIconWidget; right after instantiation it is not highlighted.<br/>
     * (See {@code #setHighlighted})
     */
    public HelpIconWidget() {
        super();

        addStyleName(CLASSNAME);
        getElement().appendChild(innerSpan1);

        innerSpan1.addClassName(helpWithCircleClassName);
        innerSpan2.getStyle().setMarginLeft(-1, Style.Unit.EM);
        innerSpan2.addClassName("icon-shape-circle");
        innerSpan3.getStyle().setMarginLeft(-1, Style.Unit.EM);
        innerSpan3.addClassName("icon-help-mark");
    }


    /**
     * Change the visual state to show the icon more or less obtrusive.
     */
    public void setHighlighted(boolean highlight) {
        if (highlight) {
            getElement().addClassName(highlighted);
            innerSpan1.removeClassName(helpWithCircleClassName);
            innerSpan1.addClassName("icon-shape-circle-plus");
            getElement().appendChild(innerSpan2);
            getElement().appendChild(innerSpan3);
        } else {
            getElement().removeClassName(highlighted);
            innerSpan1.addClassName(helpWithCircleClassName);
            innerSpan1.removeClassName("icon-shape-circle-plus");
            getElement().removeChild(innerSpan2);
            getElement().removeChild(innerSpan3);
        }
    }

}