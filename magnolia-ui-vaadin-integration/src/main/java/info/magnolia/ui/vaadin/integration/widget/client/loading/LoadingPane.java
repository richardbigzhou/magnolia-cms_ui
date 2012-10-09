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
package info.magnolia.ui.vaadin.integration.widget.client.loading;

import info.magnolia.ui.vaadin.integration.widget.client.icon.GwtLoadingIcon;

import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays a loading indicator at the top of the middle of the area to which it is attached.
 * Also inhibits interaction by covering the area with an invisible div.
 * To use - call the appendTo method with widget it should cover.
 */
public class LoadingPane{

   private final GwtLoadingIcon loadingIcon = new GwtLoadingIcon();
   private final Element iconPanel = DOM.createDiv();
   private final Element loadingIconPositioner = DOM.createDiv();
   private final Element loadingModalityCurtain = DOM.createDiv();

    public LoadingPane(){
        loadingModalityCurtain.setClassName("loading-modality-curtain");
        loadingIconPositioner.setClassName("loading-icon-positioner");
        iconPanel.setClassName("loading-icon-panel");

        iconPanel.appendChild(loadingIcon.getElement());
        loadingIconPositioner.appendChild(iconPanel);
    }

    public void appendTo(Widget parent){
        parent.getElement().appendChild(loadingModalityCurtain);
        parent.getElement().appendChild(loadingIconPositioner);
    }

    public void hide(){
        loadingModalityCurtain.getStyle().setVisibility(Visibility.HIDDEN);
        loadingIconPositioner.getStyle().setVisibility(Visibility.HIDDEN);
    }

    public void show(){
        loadingModalityCurtain.getStyle().setVisibility(Visibility.VISIBLE);
        loadingIconPositioner.getStyle().setVisibility(Visibility.VISIBLE);
    }

};
