/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.actionbar.widget;

import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarItem;
import info.magnolia.ui.vaadin.gwt.client.actionbar.shared.ActionbarSection;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * View interface of client-side action bar.
 */
public interface ActionbarWidgetView extends HasWidgets, IsWidget {

    /**
     * Gets the section widgets inside this action bar view.
     *
     * @return the sections
     */
    Map<String, ActionbarSectionWidget> getSections();

    /**
     * Sets the presenter.
     *
     * @param presenter the new presenter
     */
    void setPresenter(Presenter presenter);

    void setSections(Collection<ActionbarSection> sections);

    void setVisibleSections(Collection<ActionbarSection> visibleSections);

    void setDisabledActions(Collection<ActionbarItem> enabledActions);

    void setSectionPreview(String sectionName, String previewUrl);

    /**
     * Update the classes on the actions so that they are positioned correctly.
     * Necessary to handle when sections are dynamically changed like in the page editor.
     */
    void refreshActionsPositionsTablet();

    boolean isOpen();

    void setOpen(boolean isOpen);

    void updateLayout();

    /**
     * Presenter for the Actionbar view.
     */
    interface Presenter {

        void triggerAction(String actionToken);

        void forceLayout();

        void setOpened(boolean opened);

        boolean isDeviceTablet();

        String getIconResourceURL(String actionName);
    }
}
