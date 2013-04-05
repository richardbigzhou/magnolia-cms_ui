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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.BaseTheme;

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
    private TextField wideTextField = new TextField("Wide TextField", "This is a wide TextField");
    private PasswordField passwordField = new PasswordField("PasswordField", "h@¢km3!ƒU©àN");
    private TextArea textArea = new TextArea("TextArea", "Look at me, I'm a long text!\nI even have line breaks!");
    private ComboBox comboBox = new ComboBox("ComboBox", Arrays.asList("Avengers", "Bullit"));
    private ComboBox paginatedComboBox = new ComboBox("Paginated ComboBox", Arrays.asList("Asteroid", "Basel", "Chainsaw", "Divine", "Easter", "Family", "Georgia",
            "Hammersmith", "Ipswitch", "Jackrabbit", "Kornhaus", "Liver", "Magnolia", "Noteworthy", "Opium", "Panacotta", "Quadrant", "Responsive Responsive Responsive",
            "Shark attack", "Trivial", "Unicorn", "Volkswagen", "Warsau", "Xiaoyu", "Yeast", "Zero"));
    private NativeSelect nativeSelect = new NativeSelect("NativeSelect", paginatedComboBox.getContainerDataSource());
    private DateField dateField = new DateField("DateField");
    private InlineDateField inlineDateField = new InlineDateField("InlineDateField");
    private PopupDateField popupDateField = new PopupDateField("PopupDateField");

    private CssLayout buttonLayout = new CssLayout();
    private Button button = new Button("Button");
    private Button commitButton = new Button("Commit Button");
    private NativeButton nativeButton = new NativeButton("Native Button");
    private NativeButton styledNativeButton = new NativeButton("Styled Native");
    private NativeButton styledNativeCommitButton = new NativeButton("Native Commit");
    private Button linkButton = new Button("Link Button");


    @Override
    protected void init(VaadinRequest request) {
        initLayout();
    }

    private void initLayout() {

        wideTextField.setWidth(400, Unit.PIXELS);

        nativeSelect.setWidth(200, Unit.PIXELS);
        comboBox.setInvalidAllowed(false);
        comboBox.setNullSelectionAllowed(false);
        comboBox.setTextInputAllowed(false);
        paginatedComboBox.setWidth(400, Unit.PIXELS);
        inlineDateField.setTimeZone(TimeZone.getTimeZone("UTC"));
        inlineDateField.setLocale(Locale.US);
        popupDateField.setTextFieldEnabled(false);
        try {
            popupDateField.setValue(new SimpleDateFormat("dd/MM/yyyy").parse("20/06/2013"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        buttonLayout.addStyleName("buttons");
        commitButton.addStyleName("commit");
        styledNativeButton.addStyleName("btn-dialog");
        styledNativeCommitButton.addStyleName("btn-form commit");
        nativeButton.setDescription("This button is a NativeButton which has not received any additional style, so it still looks... well, native!");
        linkButton.setStyleName(BaseTheme.BUTTON_LINK);

        buttonLayout.addComponent(button);
        buttonLayout.addComponent(commitButton);
        buttonLayout.addComponent(styledNativeButton);
        buttonLayout.addComponent(styledNativeCommitButton);
        buttonLayout.addComponent(nativeButton);
        buttonLayout.addComponent(linkButton);

        formLayout.addComponent(label);
        formLayout.addComponent(textField);
        formLayout.addComponent(wideTextField);
        formLayout.addComponent(passwordField);
        formLayout.addComponent(textArea);
        formLayout.addComponent(comboBox);
        formLayout.addComponent(paginatedComboBox);
        formLayout.addComponent(nativeSelect);
        formLayout.addComponent(dateField);
        formLayout.addComponent(inlineDateField);
        formLayout.addComponent(popupDateField);
        formLayout.addComponent(buttonLayout);

        titleLabel.addStyleName("title");

        layout.addStyleName("main-layout");
        layout.addComponent(titleLabel);
        layout.addComponent(formLayout);

        setContent(layout);
    }

}
