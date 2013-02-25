/**
 * This file Copyright (c) 2013 Magnolia International
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

package info.magnolia.ui.vaadin.theme.sampler;

import java.util.Arrays;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

/**
 * The Application UI for developing the theme for Magnolia 5.
 */
@Theme("themesampler")
public class ThemeSamplerUI extends UI {

    private CssLayout layout = new CssLayout();
    private Label titleLabel = new Label("Magnolia Theme Sampler");

    private FormLayout formLayout = new FormLayout();
    private Label label = new Label("Label");
    private TextField textField = new TextField("TextField", "This is a TextField");
    private PasswordField passwordField = new PasswordField("PasswordField", "h@¢km3!ƒU©àN");
    private ComboBox comboBox = new ComboBox("ComboBox", Arrays.asList("Option A", "Option B"));
    private DateField dateField = new DateField("DateField");
    private Button saveButton = new Button("Save changes");
    private Button cancelButton = new Button("Discard");

    @Override
    protected void init(VaadinRequest request) {
        initLayout();
    }

    private void initLayout() {

        comboBox.setInvalidAllowed(false);
        comboBox.setNullSelectionAllowed(false);

        saveButton.addStyleName("commit");

        formLayout.addComponent(label);
        formLayout.addComponent(textField);
        formLayout.addComponent(passwordField);
        formLayout.addComponent(comboBox);
        formLayout.addComponent(dateField);
        formLayout.addComponent(cancelButton);
        formLayout.addComponent(saveButton);

        titleLabel.addStyleName("title");

        layout.addStyleName("main-layout");
        layout.addComponent(titleLabel);
        layout.addComponent(formLayout);

        setContent(layout);
    }
}
