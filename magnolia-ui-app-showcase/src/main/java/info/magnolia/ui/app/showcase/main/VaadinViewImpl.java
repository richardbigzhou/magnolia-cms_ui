package info.magnolia.ui.app.showcase.main;

import java.util.Date;

import info.magnolia.ui.vaadin.integration.widget.grid.MagnoliaTable;
import info.magnolia.ui.vaadin.integration.widget.grid.MagnoliaTreeTable;
import info.magnolia.ui.vaadin.widget.tabsheet.ShellTabSheet;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSplitPanel;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;

public class VaadinViewImpl implements VaadinView {

    private static final long serialVersionUID = 4937209277244291844L;
    
    Layout layout;
    
    public VaadinViewImpl() {
        layout = new VerticalLayout();
        layout.setWidth("100%");
        layout.setMargin(true, true, false, true);
        layout.addComponent(new Label(
                "The UI elements available in the Vaadin framework that " +
                "are recommended for use with Magnolia. These fields " +
                "can be easily added to your app and support Vaadin " +
                "interaction. Many of these elements are also available " +
                "within Magnolia Forms/Dialogs."));
        layout.addComponent(getLabelPreviews());
        layout.addComponent(getProgressIndicatorPreviews());
        layout.addComponent(getImagePreviews());
        layout.addComponent(getButtonPreviews());
        layout.addComponent(getTextFieldPreviews());
        layout.addComponent(getCheckboxPreviews());
//        layout.addComponent(getMagnoliaTabSheetPreviews());
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
    
    private Layout getMagnoliaTabSheetPreviews() {
        Layout grid = getPreviewLayout("Magnolia tabsheet");
        
        ShellTabSheet tabsheet = new ShellTabSheet();
        tabsheet.setSizeFull();

        ComponentContainer tab1 = tabsheet.addTab("first tab");
        ComponentContainer tab2 = tabsheet.addTab("second tab");
        tab1.addComponent(new Label("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."));
        tab2.addComponent(new Label("Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."));
        grid.addComponent(tabsheet);
        return grid;
    }
    
    private Layout getMagnoliaPreviews() {
        Layout grid = getPreviewLayout("Magnolia tables and trees");
        MagnoliaTable table = new MagnoliaTable();
        table.addContainerProperty("first", String.class, "first");
        table.addContainerProperty("second", Integer.class, 1);
        table.addContainerProperty("thid", Date.class, new Date());
        for(int loop=0; loop < 3; loop++) {
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
        for(int loop=0; loop < 3; loop++) {
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
    

    private Layout getSplitPreviews() {
        Layout grid = getPreviewLayout("Split panels");

        AbstractSplitPanel panel = new VerticalSplitPanel();
        panel.setWidth("230px");
        panel.setHeight("130px");
        grid.addComponent(panel);

        panel = new VerticalSplitPanel();
        panel.setWidth("230px");
        panel.setHeight("130px");
        panel.setStyleName("small");
        grid.addComponent(panel);

        panel = new HorizontalSplitPanel();
        panel.setWidth("230px");
        panel.setHeight("130px");
        grid.addComponent(panel);

        return grid;
    }
    
    class DemoPanel extends Panel {

        private static final long serialVersionUID = 1215861781775905773L;

        DemoPanel() {
            super();
            setWidth("230px");
            setHeight("120px");
            addComponent(new Label(
                    "<h4>Panel content</h4>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin malesuada volutpat vestibulum. Quisque elementum quam sed sem ultrices lobortis. Pellentesque non ligula ac dolor posuere tincidunt sed eu mi. Integer mattis fringilla nulla, ut cursus mauris scelerisque eu. Etiam bibendum placerat euismod. Nam egestas adipiscing orci sed tristique. Sed vitae enim nisi. Sed ac vehicula ipsum. Nulla quis quam nisi. Proin interdum lacus ipsum, at tristique nibh. Curabitur at ipsum sem. Donec venenatis aliquet neque, sit amet cursus lectus condimentum et. In mattis egestas erat, non cursus metus consectetur ac. Pellentesque eget nisl tellus.",
                    Label.CONTENT_XHTML));
        }

        DemoPanel(String caption) {
            this();
            setCaption(caption);
        }
    }
    
    private Layout getPanelPreviews() {
        Layout grid = getPreviewLayout("Panels");

        Panel panel = new DemoPanel("Panel");
        panel.setIcon(new ThemeResource("../runo/icons/16/document.png"));
        grid.addComponent(panel);

        panel = new DemoPanel();
        grid.addComponent(panel);

        panel = new DemoPanel();
        panel.setStyleName("borderless");
        grid.addComponent(panel);


        return grid;
    }
    
    private Layout getSliderPreviews() {
        Layout grid = getPreviewLayout("Sliders");

        Slider s = new Slider();
        s.setWidth("200px");
        try {
            s.setValue(50);
            grid.addComponent(s);
            
            s = new Slider();
            s.setOrientation(Slider.ORIENTATION_VERTICAL);
            s.setHeight("70px");
            s.setValue(50);
        }
        catch (ValueOutOfBoundsException e) {
           
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
    
    private Layout getLabelPreviews() {
        Layout grid = getPreviewLayout("Labels");

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

        button = new Button("Link style");
        button.setStyleName(Button.STYLE_LINK);
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

        tf = new TextField();
        tf.setInputPrompt("Multiline");
        tf.setRows(4);
        grid.addComponent(tf);
        
        RichTextArea rich = new RichTextArea("Rich text area");
        grid.addComponent(rich);

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
