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
package info.magnolia.m5admincentral.dialog.builder;

import info.magnolia.m5admincentral.dialog.DialogView;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.TabDefinition;

import com.google.inject.Inject;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;

/**
 * DialogBuilder.
 *
 * @author ejervidalo
 */
public class DialogBuilder {

    @Inject
    DialogView view;
    /**
     * @param dialogDefinition
     * @param dialogPresenter
     * @return
     */
    public DialogView build(DialogDefinition dialogDefinition, DialogView.Presenter dialogPresenter) {

        view.setPresenter(dialogPresenter);

        for (TabDefinition tabDefinition : dialogDefinition.getTabs()) {
            String tabName = tabDefinition.getName();
            Panel inputFields = new Panel();

            for (FieldDefinition field : tabDefinition.getFields()) {
                inputFields.addComponent(new TextArea());
            }

            view.addTab(inputFields, tabName);

        }
        return view;

    }
}
