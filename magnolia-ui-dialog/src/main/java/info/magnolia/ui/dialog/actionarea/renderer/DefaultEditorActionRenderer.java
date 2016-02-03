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
package info.magnolia.ui.dialog.actionarea.renderer;

import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionarea.ActionListener;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.dialog.BaseDialog;

import java.util.HashMap;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

/**
 * Default implementation of {@link ActionRenderer}. Simply wraps a button.
 */
public class DefaultEditorActionRenderer implements ActionRenderer {

    @Override
    public View start(final ActionDefinition definition, final ActionListener listener) {
        return new DefaultActionView(definition.getLabel(), definition.getName(), listener);
    }

    private static class DefaultActionView implements View {

        private Button button = null;

        private DefaultActionView(final String label, final String name, final ActionListener listener) {
            ClickListener clickListener = new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    listener.onActionFired(name, new HashMap<String, Object>());
                    // make sure we don't trigger useless validation for all form fields when the action is 'cancel'.
                    if (!BaseDialog.CANCEL_ACTION_NAME.equals(name) && listener instanceof EditorValidator && !((EditorValidator) listener).isValid()) {
                        // have to re-enable button since validation failed
                        button.setEnabled(true);
                    }
                }
            };
            this.button = new Button(label, clickListener);
            this.button.addStyleName(name);
            this.button.addStyleName("btn-dialog");
            this.button.addStyleName("webkit-fix");
            this.button.setDisableOnClick(true);
        }

        @Override
        public Component asVaadinComponent() {
            return button;
        }

    }
}
