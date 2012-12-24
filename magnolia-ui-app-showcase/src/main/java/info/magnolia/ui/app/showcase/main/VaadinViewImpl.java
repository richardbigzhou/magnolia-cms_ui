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

import info.magnolia.ui.vaadin.grid.MagnoliaTable;
import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;
import info.magnolia.ui.vaadin.gwt.client.shared.icon.IconParameters;
import info.magnolia.ui.vaadin.icon.BadgeIcon;
import info.magnolia.ui.vaadin.icon.ErrorIcon;
import info.magnolia.ui.vaadin.icon.HelpIcon;
import info.magnolia.ui.vaadin.icon.Icon;
import info.magnolia.ui.vaadin.icon.InfoIcon;
import info.magnolia.ui.vaadin.icon.LoadingIcon;
import info.magnolia.ui.vaadin.icon.WarningIcon;

import java.util.Date;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSplitPanel;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.themes.BaseTheme;


/**
 * Implementation for Vaadin component showcase view.
 */
public class VaadinViewImpl implements VaadinView {

    private final VerticalLayout layout;
   
    public VaadinViewImpl() {
        layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setWidth("100%");
        /**
         * TODO: handle margins in CSS style sheet.
         */
        //layout.setMargin(true, true, false, true);
        layout.addComponent(new Label(
            "The UI elements available in the Vaadin framework that " +
                "are recommended for use with Magnolia. These fields " +
                "can be easily added to your app and support Vaadin " +
                "interaction. Many of these elements are also available " +
                "within Magnolia Forms/Dialogs."));
        layout.addComponent(getLabelPreviews());
        layout.addComponent(getIconsPreview());
        layout.addComponent(getProgressIndicatorPreviews());
        layout.addComponent(getImagePreviews());
        layout.addComponent(getButtonPreviews());
        layout.addComponent(getTextFieldPreviews());
        layout.addComponent(getCheckboxPreviews());
        layout.addComponent(getSelectPreviews());
        // layout.addComponent(getMagnoliaTabSheetPreviews());
        layout.addComponent(getMagnoliaPreviews());
        layout.addComponent(getTreePreviews());
        layout.addComponent(getSliderPreviews());
        layout.addComponent(getPanelPreviews());
        layout.addComponent(getSplitPreviews());
        layout.addComponent(getPopupViewPreviews());
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    private Layout getImagePreviews() {
        Layout layout = new VerticalLayout();
        layout.setCaption("External resources");
        Embedded image = new Embedded("", new ThemeResource("img/logo-magnolia.svg"));
        image.setWidth("300px");
        image.setHeight("100px");

        layout.addComponent(image);
        return layout;
    }

    private Layout getMagnoliaPreviews() {
        Layout grid = getPreviewLayout("Magnolia table and tree table");
        MagnoliaTable table = new MagnoliaTable();
        table.addContainerProperty("first", String.class, "first");
        table.addContainerProperty("second", Integer.class, 1);
        table.addContainerProperty("thid", Date.class, new Date());
        for (int loop = 0; loop < 3; loop++) {
            table.addItem();
        }
        table.setHeight("200px");
        grid.addComponent(table);

        MagnoliaTreeTable tree = new MagnoliaTreeTable();
        HierarchicalContainer container = new HierarchicalContainer();

        container.addContainerProperty("first", String.class, "first");
        container.addContainerProperty("second", Integer.class, 1);
        container.addContainerProperty("thid", Date.class, new Date());

        tree.setContainerDataSource(container);
        Object root = container.addItem();
        for (int loop = 0; loop < 3; loop++) {
            Object itemId = container.addItem();
            container.setParent(itemId, root);
        }
        tree.setCollapsed(root, false);
        tree.setHeight("200px");
        grid.addComponent(tree);

        return grid;
    }

    private Layout getPopupViewPreviews() {
        Layout grid = getPreviewLayout("Popup views");

        Label content = new Label("Simple popup content");
        content.setSizeUndefined();
        PopupView pv = new PopupView("Default popup", content);
        grid.addComponent(pv);

        return grid;
    }

    private Layout getCheckboxPreviews() {
        Layout grid = getPreviewLayout("Checkboxes and radiobuttons");
        CheckBox checkboxcaption = new CheckBox("with caption text");

        OptionGroup group = new OptionGroup("Option group");
        group.addItem("First");
        group.addItem("Second");
        group.addItem("Third");

        OptionGroup checkGroup = new OptionGroup("Option group with multi select");
        checkGroup.setMultiSelect(true);
        checkGroup.addItem("First");
        checkGroup.addItem("Second");
        checkGroup.addItem("Third");

        grid.addComponent(checkboxcaption);
        grid.addComponent(group);
        grid.addComponent(checkGroup);
        return grid;
    }

    static void addSelectItems(AbstractSelect s, boolean selectFirst, int num) {
        s.setNullSelectionAllowed(false);
        for (int i = 0; i < num; i++) {
            s.addItem("Item " + i);
        }
        if (selectFirst) {
            s.select(s.getItemIds().iterator().next());
        }
    }

    private Layout getSelectPreviews() {
        Layout grid = getPreviewLayout("Selects");

        ComboBox combo = new ComboBox();
        addSelectItems(combo, true, 100);
        grid.addComponent(combo);

        NativeSelect s = new NativeSelect();
        addSelectItems(s, true, 10);
        grid.addComponent(s);

        return grid;
    }

    private Layout getSplitPreviews() {
        Layout grid = getPreviewLayout("Split panels");

        AbstractSplitPanel panel = new VerticalSplitPanel();
        panel.setWidth("230px");
        panel.setHeight("130px");
        grid.addComponent(panel);

        panel = new HorizontalSplitPanel();
        panel.setWidth("230px");
        panel.setHeight("130px");
        grid.addComponent(panel);

        return grid;
    }

    /**
     * Private class to show custom panel content.
     */
    private class DemoPanel extends Panel {

        DemoPanel() {
            super();
            setWidth("230px");
            setHeight("120px");
            setContent(new Label(
                "<h4>Panel content</h4>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin malesuada volutpat vestibulum. Quisque elementum quam sed sem ultrices lobortis. Pellentesque non ligula ac dolor posuere tincidunt sed eu mi. Integer mattis fringilla nulla, ut cursus mauris scelerisque eu. Etiam bibendum placerat euismod. Nam egestas adipiscing orci sed tristique. Sed vitae enim nisi. Sed ac vehicula ipsum. Nulla quis quam nisi. Proin interdum lacus ipsum, at tristique nibh. Curabitur at ipsum sem. Donec venenatis aliquet neque, sit amet cursus lectus condimentum et. In mattis egestas erat, non cursus metus consectetur ac. Pellentesque eget nisl tellus.",
                ContentMode.HTML));
        }
    }

    private Layout getPanelPreviews() {
        Layout grid = getPreviewLayout("Panels");

        DemoPanel panel = new DemoPanel();
        grid.addComponent(panel);

        return grid;
    }

    private Layout getSliderPreviews() {
        Layout grid = getPreviewLayout("Sliders");

        Slider s = new Slider();
        s.setWidth("200px");
        try {
            s.setValue(50d);
            grid.addComponent(s);

            s = new Slider();
            s.setOrientation(SliderOrientation.VERTICAL);
            s.setHeight("70px");
            s.setValue(50d);
        } catch (ValueOutOfBoundsException e) {

        }
        grid.addComponent(s);

        return grid;
    }

    Tree tree;

    private Layout getTreePreviews() {
        Layout grid = getPreviewLayout("Trees");
        tree = new Tree();
        tree.setImmediate(true);
        tree.setSizeFull();
        // we'll use a property for caption instead of the item id ("value"),
        // so that multiple items can have the same caption
        tree.addContainerProperty("caption", String.class, "");
        tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
        tree.setItemCaptionPropertyId("caption");
        for (int i = 1; i <= 3; i++) {
            final Object id = addCaptionedItem("Division " + i, null);
            tree.expandItem(id);
            addCaptionedItem("Team A", id);
            addCaptionedItem("Team B", id);
            tree.setItemIcon(id, new ThemeResource(
                "../runo/icons/16/folder.png"));
        }
        grid.addComponent(tree);
        return grid;
    }

    private Object addCaptionedItem(String caption, Object parent) {
        // add item, let tree decide id
        final Object id = tree.addItem();
        // get the created item
        final Item item = tree.getItem(id);
        // set our "caption" property
        final Property p = item.getItemProperty("caption");
        p.setValue(caption);
        if (parent != null) {
            tree.setChildrenAllowed(parent, true);
            tree.setParent(id, parent);
            tree.setChildrenAllowed(id, false);
        }
        return id;
    }

    private Layout getIconsPreview() {
        Layout layout = getPreviewLayout("Magnolia Icons");

        CssLayout layout1 = new CssLayout();
        Label label1 = new Label("<h4>icon css class</h4>", ContentMode.HTML);
        label1.setWidth(150, Unit.PIXELS);
        layout1.addComponent(label1);
        layout1.addComponent(new Icon("view"));
        layout1.addComponent(new Icon("edit"));
        layout1.addComponent(new Icon("search"));

        CssLayout layout2 = new CssLayout();
        Label label2 = new Label("<h4>icon size</h4>", ContentMode.HTML);
        label2.setWidth(150, Unit.PIXELS);
        layout2.addComponent(label2);
        layout2.addComponent(new Icon("search", 16));
        layout2.addComponent(new Icon("search"));
        layout2.addComponent(new Icon("search", 48));

        CssLayout layout3 = new CssLayout();
        Label label3 = new Label("<h4>icon color</h4>", ContentMode.HTML);
        label3.setWidth(150, Unit.PIXELS);
        layout3.addComponent(label3);
        layout3.addComponent(new Icon("search", Icon.COLOR_INFO));
        layout3.addComponent(new Icon("search", Icon.COLOR_GREEN_BADGE));
        layout3.addComponent(new Icon("search", Icon.COLOR_HELP));
        layout3.addComponent(new Icon("search", Icon.COLOR_WARNING));
        layout3.addComponent(new Icon("search", Icon.COLOR_ERROR));
        layout3.addComponent(new Icon("search", "#c09"));

        CssLayout layout4 = new CssLayout();
        Label label4 = new Label("<h4>composite icons</h4>", ContentMode.HTML);
        label4.setWidth(150, Unit.PIXELS);
        layout4.addComponent(label4);
        layout4.addComponent(new InfoIcon());
        layout4.addComponent(new WarningIcon());
        layout4.addComponent(new ErrorIcon());
        layout4.addComponent(new HelpIcon());

        CssLayout layout5 = new CssLayout();
        Label label5 = new Label("<h4>badge icon</h4>", ContentMode.HTML);
        label5.setWidth(150, Unit.PIXELS);
        layout5.addComponent(label5);
        for (int m = 0; m <= 12; m++) {
            BadgeIcon badge;
            if (m % 2 == 0) {
                badge = new BadgeIcon();
            } else {
                badge = new BadgeIcon(IconParameters.DEFAULT_SIZE, "#fff", Icon.COLOR_GREEN_BADGE, true);
            }
            badge.setValue(fib(m));
            layout5.addComponent(badge);
        }

        CssLayout layout6 = new CssLayout();
        Label label6 = new Label("<h4>spinner icon</h4>", ContentMode.HTML);
        label6.setWidth(150, Unit.PIXELS);
        layout6.addComponent(label6);
        layout6.addComponent(new LoadingIcon());

        layout.addComponent(layout1);
        layout.addComponent(layout2);
        layout.addComponent(layout3);
        layout.addComponent(layout4);
        layout.addComponent(layout5);
        layout.addComponent(layout6);
        return layout;
    }

    private int fib(int n) {
        if (n <= 1) {
            return n;
        } else {
            return fib(n - 1) + fib(n - 2);
        }
    }

    private Layout getLabelPreviews() {
        Layout grid = getPreviewLayout("Static text");

        Label label = new Label(
            "Plain text, lorem ipsum dolor sit amet consectetur amit.");

        grid.addComponent(label);

        label = new Label("Warning text, lorem ipsum dolor sit.");
        label.setStyleName("warning");
        grid.addComponent(label);

        label = new Label("Error text, lorem ipsum dolor.");
        label.setStyleName("error");
        grid.addComponent(label);

        return grid;
    }

    private Layout getProgressIndicatorPreviews() {
        Layout grid = getPreviewLayout("Progress Indicators");

        ProgressIndicator pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setCaption("Normal");
        grid.addComponent(pi);

        pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setCaption("Indeterminate");
        pi.setIndeterminate(true);
        grid.addComponent(pi);

        return grid;
    }

    private Layout getButtonPreviews() {
        Layout grid = getPreviewLayout("Buttons");

        Button button = new Button("Button");
        grid.addComponent(button);


        button = new Button("Button with link style");
        button.setStyleName(BaseTheme.BUTTON_LINK);
        grid.addComponent(button);

        button = new Button();
        button.setIcon(new ThemeResource("../runo/icons/16/user.png"));
        grid.addComponent(button);

        Link l = new Link("Link: magnolia-cms.com", new ExternalResource(
            "http://www.magnolia-cms.com"));
        grid.addComponent(l);

        return grid;
    }

    private Layout getTextFieldPreviews() {
        Layout grid = getPreviewLayout("Text fields");

        TextField tf = new TextField();
        tf.setValue("Text field");
        grid.addComponent(tf);

        PasswordField pw = new PasswordField();
        pw.setInputPrompt("Password");
        grid.addComponent(pw);

        TextArea ta = new TextArea();
        ta.setInputPrompt("Multiline");
        grid.addComponent(ta);

        return grid;
    }

    private Layout getPreviewLayout(String caption) {
        GridLayout grid = new GridLayout(3, 1);
        grid.setWidth("100%");
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setCaption(caption);
        return grid;
    }

}
