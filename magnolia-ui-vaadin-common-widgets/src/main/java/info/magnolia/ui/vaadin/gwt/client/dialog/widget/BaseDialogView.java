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
package info.magnolia.ui.vaadin.gwt.client.dialog.widget;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a basic interface for the widgets that display their content in a dialog.
 */
public interface BaseDialogView extends IsWidget, HasWidgets {

    void setActions(Map<String, String> actionMap, List<String> actionOrder, String defaultButtonName);

    void setDescription(String description);

    void setCaption(String caption);

    void showCloseButton();

    void setContent(Widget contentWidget);

    void setHeaderToolbar(Widget headerToolbarWidget);

    void setFooterToolbar(Widget footerToolbarWidget);

    void setPresenter(Presenter presenter);

    Presenter getPresenter();

    /**
     * Presenter.
     */
    public interface Presenter {

        void fireAction(String action);

        void closeDialog();

        void setDescriptionVisibility(boolean isVisible);
    }

}
