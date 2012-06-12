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
package info.magnolia.ui.admincentral.app.testapp;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

/**
* @version $Id$
*/
public class SimpleDummyAppViewImpl implements SimpleDummyAppView, IsVaadinComponent {

    private SimpleDummyAppView.Presenter presenter;
    private final VerticalLayout tableContainer;

    SimpleDummyAppViewImpl() {
        tableContainer = new VerticalLayout();
        Label label = new Label("<center>Dummy App</center>", Label.CONTENT_XHTML);
        tableContainer.addComponent(label);

        Button dialog = new Button("Gimme more tabs!", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                presenter.onButtonClick();
            }
        });
        tableContainer.addComponent(dialog);
    }

    public void setPresenter(SimpleDummyAppView.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String getCaption() {
        return "SimpleDummy";
    }

    @Override
    public Component asVaadinComponent() {
        return tableContainer;
    }
}
