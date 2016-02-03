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
package info.magnolia.ui.vaadin.gwt.client.applauncher.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * The permanent App Group.
 */
public class VPermanentAppTileGroup extends VAppTileGroup {

    private String caption;

    final Element sectionEl = DOM.createDiv();

    public VPermanentAppTileGroup(String caption, String color) {
        super(color);
        this.caption = caption;
        construct();
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    private void createSectionItem() {
        final Element sectionEl = DOM.createDiv();
        final Element sectionLabel = DOM.createSpan();

        sectionEl.appendChild(sectionLabel);
        sectionEl.addClassName("item");
        sectionEl.addClassName("section");

        sectionLabel.addClassName("sectionLabel");
        if (caption != null && !caption.contains("\u00AD")) {
            sectionLabel.addClassName("wordwrap");
        }
        sectionLabel.setInnerText(caption);
        sectionEl.getStyle().setBackgroundColor(getColor());
        getElement().appendChild(sectionEl);
    }

    @Override
    protected void construct() {
        createSectionItem();
    }

    public Element getSectionEl() {
        return sectionEl;
    }

}
