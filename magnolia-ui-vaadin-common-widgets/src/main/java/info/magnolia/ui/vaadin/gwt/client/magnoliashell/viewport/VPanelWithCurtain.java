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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport;

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * Panel that has a modality curtain when active.
 *
 */
public class VPanelWithCurtain extends ComplexPanel {

    private final Element modalityCurtain = DOM.createDiv();

    public VPanelWithCurtain() {
        modalityCurtain.addClassName("green-modality-curtain");
        modalityCurtain.getStyle().setVisibility(Visibility.HIDDEN);
    }

    public Element getModalityCurtain() {
        return modalityCurtain;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        RootPanel.get().getElement().appendChild(modalityCurtain);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        RootPanel.get().getElement().removeChild(modalityCurtain);
    }

    public void showCurtain() {
        final JQueryWrapper jq = JQueryWrapper.select(modalityCurtain);
        jq.setCss("visibility", "visible");
        jq.setCss("zIndex", String.valueOf(JQueryWrapper.select(this).cssInt("zIndex") - 1));
    }

    @Override
    public void removeFromParent() {
        super.removeFromParent();
        hideCurtain();
    }

    public void hideCurtain() {
        JQueryWrapper.select(modalityCurtain).setCss("visibility", "hidden");
    }
}
