/**
 * This file Copyright (c) 2013-2014 Magnolia International
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
package info.magnolia.sample.app.error;

import info.magnolia.context.MgnlContext;

import javax.inject.Inject;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * View implementation of the error subapp.
 */
public class SampleErrorViewImpl implements SampleErrorView {

    private HorizontalLayout layout = new HorizontalLayout();

    @Inject
    public SampleErrorViewImpl() {
        layout.setSizeFull();

        RedButton redButton = new RedButton("Throw NullPointerException");
        DeathButton deathButton = new DeathButton("Throw NullPointerException in repaint cycle");
        redButton.setIcon(new ExternalResource(MgnlContext.getWebContext().getContextPath() + "/.resources/big-red-button.png"));
        deathButton.setIcon(new ExternalResource(MgnlContext.getWebContext().getContextPath() + "/.resources/big-death-button.png"));
        redButton.setStyleName(BaseTheme.BUTTON_LINK);
        deathButton.setStyleName(BaseTheme.BUTTON_LINK);
        layout.addComponent(redButton);
        layout.addComponent(deathButton);
        layout.setComponentAlignment(redButton, Alignment.MIDDLE_RIGHT);
        layout.setComponentAlignment(deathButton, Alignment.MIDDLE_LEFT);
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    /**
     * The Class RedButton.
     */
    public class RedButton extends Button {

        public RedButton(String description) {
            super();
            setDescription(description);
            addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    ((String) null).length(); // Null-pointer exception
                }
            });
        }
    }

    /**
     * The Class DeathButton.
     */
    public class DeathButton extends Button {

        private String npeField = "panic";

        public DeathButton(String description) {
            super();
            setDescription(description);
            addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    npeField = null;
                    markAsDirty();
                    npeField.length(); // Null-pointer exception
                }
            });
        }

        @Override
        public String getConnectorId() {
            npeField.length(); // Null-pointer exception
            return super.getConnectorId();
        }
    }
}
