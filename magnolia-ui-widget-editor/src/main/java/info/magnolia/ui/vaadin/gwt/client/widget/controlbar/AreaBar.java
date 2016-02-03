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
package info.magnolia.ui.vaadin.gwt.client.widget.controlbar;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlArea;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.listener.AreaListener;

import com.google.gwt.user.client.ui.Label;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchEndHandler;
import com.googlecode.mgwt.ui.client.widget.touch.TouchDelegate;

/**
 * Control bar for areas. Injected at the beginning of an area.
 */
public class AreaBar extends AbstractBar {


    private final AreaListener listener;

    public AreaBar(MgnlArea mgnlElement) {
        super(mgnlElement);
        this.listener = mgnlElement;

        this.addStyleName(AREA_CLASS_NAME);
        initLayout();
    }

    @Override
    protected String getLabel() {
        return listener.getLabel();
    }

    @Override
    protected void createControls() {
        if (listener.hasAddButton()) {
            final Label add = new Label();
            add.setStyleName(ICON_CLASS_NAME);
            add.addStyleName(ADD_CLASS_NAME);

            TouchDelegate td = new TouchDelegate(add);
            td.addTouchEndHandler(new TouchEndHandler() {
                @Override
                public void onTouchEnd(TouchEndEvent touchEndEvent) {
                    listener.createOptionalArea();
                }
            });

            addButton(add);
        }

        if (listener.hasEditButton()) {
            final Label edit = new Label();
            edit.setStyleName(ICON_CLASS_NAME);
            edit.addStyleName(EDIT_CLASS_NAME);

            TouchDelegate td = new TouchDelegate(edit);
            td.addTouchEndHandler(new TouchEndHandler() {
                @Override
                public void onTouchEnd(TouchEndEvent touchEndEvent) {
                    listener.editArea();

                }
            });

            addButton(edit);
        }

    }

}
