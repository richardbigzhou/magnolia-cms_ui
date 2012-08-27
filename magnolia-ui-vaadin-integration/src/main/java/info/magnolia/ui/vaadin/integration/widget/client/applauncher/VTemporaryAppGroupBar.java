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
package info.magnolia.ui.vaadin.integration.widget.client.applauncher;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;


/**
 * The holder of the temporary app group expanders.
 * 
 */
public class VTemporaryAppGroupBar extends FlowPanel {

    private final Map<Element, VTemporaryAppTileGroup> groupMap = new HashMap<Element, VTemporaryAppTileGroup>();

    private VTemporaryAppTileGroup currentOpenGroup = null;

    private final TouchDelegate touchDelegate = new TouchDelegate(this);

    public VTemporaryAppGroupBar() {
        super();
        addStyleName("app-list");
        addStyleName("sections");
        construct();
    }

    private void construct() {
        /*
        DOM.sinkEvents(getElement(), Event.MOUSEEVENTS);
        addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final Element target = event.getNativeEvent().getEventTarget().cast();
                final VTemporaryAppTileGroup group = groupMap.get(target);
                closeCurrentOpenExpander();
                if (group != null) {
                    if (currentOpenGroup != group) {
                        if (currentOpenGroup != null) {
                            currentOpenGroup.closeSection();
                        }
                        group.showSection();
                        currentOpenGroup = group;
                        openExpander(target);
                    } else {
                        currentOpenGroup.closeSection();
                        currentOpenGroup = null;
                    }
                }
            }
        }, ClickEvent.getType());
        */

        touchDelegate.addTouchStartHandler(new TouchStartHandler() {

            @Override
            public void onTouchStart(TouchStartEvent event) {
                final Element target = event.getNativeEvent().getEventTarget().cast();
                final VTemporaryAppTileGroup group = groupMap.get(target);
                closeCurrentOpenExpander();
                if (group != null) {
                    if (currentOpenGroup != group) {
                        if (currentOpenGroup != null) {
                            currentOpenGroup.closeSection();
                        }
                        group.showSection();
                        currentOpenGroup = group;
                        openExpander(target);
                    } else {
                        currentOpenGroup.closeSection();
                        currentOpenGroup = null;
                    }
                }

            }
        });
    }

    protected void openExpander(Element target) {
        target.removeClassName("closed");
        target.addClassName("open");
    }

    protected void closeCurrentOpenExpander() {
        if (currentOpenGroup != null) {
            for (Entry<Element, VTemporaryAppTileGroup> entry : groupMap.entrySet()) {
                if (currentOpenGroup == entry.getValue()) {
                    entry.getKey().addClassName("closed");
                    entry.getKey().removeClassName("open");
                    break;
                }
            }
        }
    }

    public void addGroup(String caption, VAppTileGroup group) {
        if (group instanceof VTemporaryAppTileGroup) {
            final Element groupThumbnail = DOM.createDiv();
            groupThumbnail.addClassName("item");
            groupThumbnail.addClassName("section");
            groupThumbnail.addClassName("closed");

            if (group.isClientGroup()) {
                groupThumbnail.addClassName("client-group");
                groupThumbnail.getStyle().setColor(group.getColor());
            } else {
                groupThumbnail.getStyle().setBackgroundColor(group.getColor());
            }

            final Element label = DOM.createSpan();
            label.addClassName("label");
            label.setInnerText(caption);

            groupThumbnail.appendChild(label);

            groupMap.put(groupThumbnail, (VTemporaryAppTileGroup) group);
            getElement().appendChild(groupThumbnail);
        }
    }

}
