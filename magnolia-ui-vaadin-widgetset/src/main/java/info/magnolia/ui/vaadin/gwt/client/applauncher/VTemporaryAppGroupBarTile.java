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
package info.magnolia.ui.vaadin.gwt.client.applauncher;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;

/**
 * A tile representing a Temporary App Group - displayed in the Temporary App Group Bar.
 *
 */
public class VTemporaryAppGroupBarTile extends FlowPanel {

    private Element element;
    private VTemporaryAppGroupBar groupBar;
    private VAppTileGroup group;
    private VTemporaryAppGroupBarTile that;

    public VTemporaryAppGroupBarTile(String caption, VAppTileGroup groupParam, VTemporaryAppGroupBar groupBarParam){
        super();
        groupBar = groupBarParam;
        group = groupParam;
        that = this;

        if (group instanceof VTemporaryAppTileGroup) {
            constructDOM(caption);
            bindHandlers();
        }
    }


    private void constructDOM(String caption){

        element = this.getElement();
        element.addClassName("item");
        element.addClassName("section");
        element.addClassName("closed");

        /*if (group.isClientGroup()) {
            element.addClassName("client-group");
        } else {
        }*/

        final Element label = DOM.createSpan();
        label.addClassName("label");
        label.setInnerText(caption);

        element.appendChild(label);

    }


    private void bindHandlers() {

        DOM.sinkEvents(element, Event.MOUSEEVENTS);
        addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                getElement().addClassName("hover");
            }
        }, MouseOverEvent.getType());

        addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                getElement().removeClassName("hover");
            }
        }, MouseOutEvent.getType());


        final TouchDelegate touchDelegate = new TouchDelegate(this);

        touchDelegate.addTouchEndHandler(new TouchEndHandler() {

            @Override
            public void onTouchEnd(TouchEndEvent event) {
                groupBar.handleTileClick((VTemporaryAppTileGroup)group, that);
            }
        });

    }



    public void openExpander() {

        element.removeClassName("hover");

        element.removeClassName("closed");
        element.addClassName("open");

        if (group.isClientGroup()){
            element.getStyle().setColor(group.getColor());
            element.getStyle().setBackgroundColor("white");
        }else{
            element.getStyle().setColor("white");
            element.getStyle().setBackgroundColor(group.getColor());
        }
    }

    public void closeExpander(){
        element.addClassName("closed");
        element.removeClassName("open");
        element.getStyle().clearBackgroundColor();
        element.getStyle().clearColor();
    }


}
