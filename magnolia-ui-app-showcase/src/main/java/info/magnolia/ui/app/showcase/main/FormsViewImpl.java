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
package info.magnolia.ui.app.showcase.main;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Implementation for forms example.
 */
public class FormsViewImpl implements FormsView {

    CssLayout layout = new CssLayout();

    private Listener listener;

    public FormsViewImpl() {
        /**
         * TODO: HANDLE MARGINS IN CSS.
         */
        //layout.setMargin(true, true, false, true);
        layout.setSizeFull();
        layout.addComponent(new Label("The fields available in a Magnolia" +
                " Form or Dialog. Configurable by repository or code."));

        layout.addComponent(new Button("View in dialog", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                listener.onViewInDialog();
            }
        }));

        createComponents(layout);
    }

    private void createComponents(Layout layout) {
        layout.addComponent(createRow("Static text",
                new Label("Lorem ipsum dolor sit amet, consectetur adipisicing elit"))
        );
        layout.addComponent(createRow("Commit button in a form", createSendButton()));
        layout.addComponent(createRow("Reset button in a form", createResetButton()));
        layout.addComponent(
            createRow("Link",
                new Link(
                    "magnolia-cms.com",
                    new ExternalResource(
                            "http://www.magnolia-cms.com"
                    )
                )
            )
        );
        layout.addComponent(createRow("Text field", new TextField()));
        layout.addComponent(createRow("Text area", new TextArea()));
        layout.addComponent(createRow("Password field", new PasswordField()));
        layout.addComponent(createRow("Checkbox", new CheckBox()));
        layout.addComponent(createRow("Radio button group", createRadioButtonGroup(false)));
        layout.addComponent(createRow("Checkbox group", createRadioButtonGroup(true)));
        layout.addComponent(createRow("Select",createSelect()));
        layout.addComponent(createRow("Date field", new DateField()));
    }

    private OptionGroup createRadioButtonGroup(boolean multiSelect) {
        OptionGroup group = new OptionGroup("Option group");
        group.addItem("First");
        group.addItem("Second");
        group.addItem("Third");
        group.setMultiSelect(multiSelect);

        return group;
    }

    private Layout createRow(String caption, Component content) {
        Layout layout = getPreviewLayout(caption);
        layout.addComponent(content);
        return layout;
    }


    private Select createSelect() {
        Select select = new Select();
        select.setNullSelectionAllowed(false);
        select.addItem("item one");
        select.addItem("item two");
        select.addItem("item three");
        return select;
    }

    private NativeButton createSendButton() {
        NativeButton sendButton = new NativeButton("Send");
        sendButton.addStyleName("btn-dialog");
        sendButton.addStyleName("btn-dialog-commit");
        return sendButton;
    }

    private NativeButton createResetButton() {
        NativeButton resetButton = new NativeButton("Reset");
        resetButton.addStyleName("btn-dialog");
        resetButton.addStyleName("btn-dialog-cancel");
        return resetButton;
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    private Layout getPreviewLayout(String caption) {
        GridLayout layout = new GridLayout(2, 1);
        layout.setWidth("100%");
        layout.setSpacing(true);
        layout.setMargin(true);
        layout.addComponent(new Label(caption));
        return layout;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

}
