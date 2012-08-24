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
package info.magnolia.ui.widget.actionbar.gwt.client;

import java.util.Map;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ui.Icon;


/**
 * View interface of client-side action bar.
 */
public interface VActionbarView extends HasWidgets, IsWidget {

    /**
     * Gets the section widgets inside this action bar view.
     * 
     * @return the sections
     */
    Map<String, VActionbarSection> getSections();

    /**
     * Sets the presenter.
     * 
     * @param presenter the new presenter
     */
    void setPresenter(Presenter presenter);

    /**
     * Adds a section to this action bar.
     * 
     * @param sectionParams the section parameters
     */
    void addSection(VActionbarSectionJSO sectionParams);

    /**
     * Removes the section from this action bar.
     * 
     * @param sectionName the section name
     */
    void removeSection(String sectionName);

    /**
     * Adds an action item to this action bar.
     * 
     * @param actionParams the action parameters
     * @param icon the icon ui object
     * @param groupName the group name
     * @param sectionName the section name
     * 
     * use {@link #addAction(VActionbarItemJSO, String, String)} instead.
     */
    @Deprecated
    void addAction(VActionbarItemJSO actionParams, Icon icon, String groupName, String sectionName);

    /**
     * Adds an action item to this action bar.
     * 
     * @param actionParams the action parameters including the icon CSS class name
     * @param groupName the group name
     * @param sectionName the section name
     */
    void addAction(VActionbarItemJSO actionParams, String groupName, String sectionName);

    /**
     * Checks if given widget is a child of this component.
     * 
     * @param component the component
     * @return true, if successful
     */
    boolean hasChildComponent(Widget component);

    /**
     * Presenter for the Actionbar view.
     */
    interface Presenter {

        void triggerAction(String actionToken);

        void changeFullScreen(boolean isFullScreen);

        void forceLayout();

        void setOpened(boolean opened);
    }

}
