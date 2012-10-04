package info.magnolia.ui.app.showcase.main;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

public class VaadinViewImpl implements VaadinView {

    private static final long serialVersionUID = 4937209277244291844L;
    
    Layout layout;
    
    public VaadinViewImpl() {
        layout = new VerticalLayout();
        layout.addComponent(getLabelPreviews());
        layout.addComponent(getProgressIndicatorPreviews());
        layout.addComponent(getButtonPreviews());
        layout.addComponent(getTextFieldPreviews());
        layout.addComponent(getCheckboxPreviews());
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }
    
    private Layout getCheckboxPreviews() {
        Layout grid = getPreviewLayout("Checkboxes and radiobuttons");
        CheckBox checkbox = new CheckBox();
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
        
        grid.addComponent(checkbox);
        grid.addComponent(checkboxcaption);
        grid.addComponent(group);
        grid.addComponent(checkGroup);
        return grid;
    }
    
    private Layout getLabelPreviews() {
        Layout grid = getPreviewLayout("Labels");

        Label label = new Label(
                "<h4>Paragraph Header</h4>Plain text, lorem ipsum dolor sit amet consectetur amit.",
                Label.CONTENT_XHTML);
        label.setWidth("200px");
        grid.addComponent(label);

        label = new Label(
                "Big plain text, lorem ipsum dolor sit amet consectetur amit.");
        label.setWidth("200px");
        label.setStyleName("big");
        grid.addComponent(label);

        label = new Label(
                "Small plain text, lorem ipsum dolor sit amet consectetur amit.");
        label.setWidth("200px");
        label.setStyleName("small");
        grid.addComponent(label);

        label = new Label(
                "Tiny plain text, lorem ipsum dolor sit amet consectetur amit.");
        label.setWidth("200px");
        label.setStyleName("tiny");
        grid.addComponent(label);

        label = new Label("<h1>Top Level Header</h1>", Label.CONTENT_XHTML);
        label.setSizeUndefined();
        grid.addComponent(label);
        label.setDescription("Label.addStyleName(\"h1\");<br>or<br>new Label(\"&lt;h1&gt;Top Level Header&lt;/h1&gt;\", Label.CONTENT_XHTML);");

        label = new Label("<h2>Second Header</h2>", Label.CONTENT_XHTML);
        label.setSizeUndefined();
        grid.addComponent(label);
        label.setDescription("Label.addStyleName(\"h2\");<br>or<br>new Label(\"&lt;h2&gt;Second Header&lt;/h2&gt;\", Label.CONTENT_XHTML);");

        label = new Label("<h3>Subtitle</h3>", Label.CONTENT_XHTML);
        label.setSizeUndefined();
        grid.addComponent(label);
        label.setDescription("Label.addStyleName(\"h3\");<br>or<br>new Label(\"&lt;h3&gt;Subtitle&lt;/h3&gt;\", Label.CONTENT_XHTML);");

        label = new Label(
                "<h4>Paragraph Header</h4>Plain text, lorem ipsum dolor sit amet consectetur amit.",
                Label.CONTENT_XHTML);
        label.setWidth("200px");
        label.setStyleName("color");
        grid.addComponent(label);

        label = new Label(
                "Big plain text, lorem ipsum dolor sit amet consectetur amit.");
        label.setWidth("200px");
        label.setStyleName("big color");
        grid.addComponent(label);

        label = new Label(
                "Small plain text, lorem ipsum dolor sit amet consectetur amit.");
        label.setWidth("200px");
        label.setStyleName("small color");
        grid.addComponent(label);

        label = new Label(
                "Tiny plain text, lorem ipsum dolor sit amet consectetur amit.");
        label.setWidth("200px");
        label.setStyleName("tiny color");
        grid.addComponent(label);

        label = new Label("Top Level Header");
        label.setSizeUndefined();
        label.setStyleName("h1 color");
        grid.addComponent(label);

        label = new Label("Second Header");
        label.setSizeUndefined();
        label.setStyleName("h2 color");
        grid.addComponent(label);

        label = new Label("Subtitle");
        label.setSizeUndefined();
        label.setStyleName("h3 color");
        grid.addComponent(label);

        label = new Label("Warning text, lorem ipsum dolor sit.");
        label.setStyleName("warning");
        grid.addComponent(label);

        label = new Label("Error text, lorem ipsum dolor.");
        label.setStyleName("error");
        grid.addComponent(label);

        label = new Label("Big warning text");
        label.setStyleName("big warning");
        grid.addComponent(label);

        label = new Label("Big error text");
        label.setStyleName("big error");
        grid.addComponent(label);

        label = new Label("Loading text...");
        label.setStyleName("h3 loading");
        grid.addComponent(label);

        return grid;
    }
    
    Layout getProgressIndicatorPreviews() {
        Layout grid = getPreviewLayout("Progress Indicators");

        ProgressIndicator pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setCaption("Normal");
        grid.addComponent(pi);

        pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setCaption("ProgressIndicator.setStyleName(\"small\")");
        pi.setStyleName("small");
        grid.addComponent(pi);

        pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setCaption("ProgressIndicator.setStyleName(\"big\")");
        pi.setStyleName("big");
        grid.addComponent(pi);

        pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setIndeterminate(true);
        pi.setCaption("Indeterminate, style \"bar\"");
        pi.setStyleName("bar");
        grid.addComponent(pi);

        pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setIndeterminate(true);
        pi.setCaption("Indeterminate, style \"small bar\"");
        pi.setStyleName("small bar");
        grid.addComponent(pi);

        pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setIndeterminate(true);
        pi.setCaption("Indeterminate, style \"big bar\"");
        pi.setStyleName("big bar");
        grid.addComponent(pi);

        pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setCaption("Indeterminate, default style");
        pi.setIndeterminate(true);
        grid.addComponent(pi);

        pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setCaption("Indeterminate, style \"big\"");
        pi.setStyleName("big");
        pi.setIndeterminate(true);
        grid.addComponent(pi);

        pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setCaption("Disabled");
        pi.setEnabled(false);
        grid.addComponent(pi);

        pi = new ProgressIndicator(0.5f);
        pi.setPollingInterval(100000000);
        pi.setCaption("Indeterminate bar disabled");
        pi.setIndeterminate(true);
        pi.setStyleName("bar");
        pi.setEnabled(false);
        grid.addComponent(pi);

        return grid;
    }
    
    private Layout getButtonPreviews() {
        Layout grid = getPreviewLayout("Buttons");

        Button button = new Button("Button");
        grid.addComponent(button);

        button = new Button("Default");
        button.setStyleName("default");
        grid.addComponent(button);

        button = new Button("Small");
        button.setStyleName("small");
        grid.addComponent(button);

        button = new Button("Small Default");
        button.setStyleName("small default");
        grid.addComponent(button);

        button = new Button("Big");
        button.setStyleName("big");
        grid.addComponent(button);

        button = new Button("Big Default");
        button.setStyleName("big default");
        grid.addComponent(button);

        button = new Button("Disabled");
        button.setEnabled(false);
        grid.addComponent(button);

        button = new Button("Disabled default");
        button.setEnabled(false);
        button.setStyleName("default");
        grid.addComponent(button);

        button = new Button("Link style");
        button.setStyleName(Button.STYLE_LINK);
        grid.addComponent(button);

        button = new Button("Disabled link");
        button.setStyleName(Button.STYLE_LINK);
        button.setEnabled(false);
        grid.addComponent(button);

        button = new Button("120px overflows out of the button");
        button.setIcon(new ThemeResource("../runo/icons/16/document.png"));
        button.setWidth("120px");
        grid.addComponent(button);

        button = new Button("Small");
        button.setStyleName("small");
        button.setIcon(new ThemeResource("../runo/icons/16/document.png"));
        grid.addComponent(button);

        button = new Button("Big");
        button.setStyleName("big");
        button.setIcon(new ThemeResource("../runo/icons/16/document.png"));
        grid.addComponent(button);

        button = new Button("Big Default");
        button.setStyleName("big default");
        button.setIcon(new ThemeResource("../runo/icons/32/document-txt.png"));
        grid.addComponent(button);

        button = new Button("Big link");
        button.setStyleName(Button.STYLE_LINK + " big");
        button.setIcon(new ThemeResource("../runo/icons/32/document.png"));
        grid.addComponent(button);

        button = new Button("Borderless");
        button.setStyleName("borderless");
        button.setIcon(new ThemeResource("../runo/icons/32/note.png"));
        grid.addComponent(button);

        button = new Button("Borderless icon on top");
        button.setStyleName("borderless icon-on-top");
        button.setIcon(new ThemeResource("../runo/icons/32/note.png"));
        grid.addComponent(button);

        button = new Button("Icon on top");
        button.setStyleName("icon-on-top");
        button.setIcon(new ThemeResource("../runo/icons/32/users.png"));
        grid.addComponent(button);

        button = new Button("Wide Default");
        button.setStyleName("wide default");
        grid.addComponent(button);

        button = new Button("Wide");
        button.setStyleName("wide");
        grid.addComponent(button);

        button = new Button("Tall");
        button.setStyleName("tall");
        grid.addComponent(button);

        button = new Button("Wide, Tall & Big");
        button.setStyleName("wide tall big");
        grid.addComponent(button);

        button = new Button("Icon on right");
        button.setStyleName("icon-on-right");
        button.setIcon(new ThemeResource("../runo/icons/16/document.png"));
        grid.addComponent(button);

        button = new Button("Big icon");
        button.setStyleName("icon-on-right big");
        button.setIcon(new ThemeResource("../runo/icons/16/document.png"));
        grid.addComponent(button);

        button = new Button("Toggle (down)");
        button.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                if (event.getButton().getStyleName().endsWith("down")) {
                    event.getButton().removeStyleName("down");
                } else {
                    event.getButton().addStyleName("down");
                }
            }
        });
        button.addStyleName("down");
        grid.addComponent(button);
        button.setDescription(button.getDescription()
                + "<br><strong>Stylename switching logic must be done separately</strong>");

        button = new Button();
        button.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                if (event.getButton().getStyleName().endsWith("down")) {
                    event.getButton().removeStyleName("down");
                } else {
                    event.getButton().addStyleName("down");
                }
            }
        });
        button.addStyleName("icon-only");
        button.addStyleName("down");
        button.setIcon(new ThemeResource("../runo/icons/16/user.png"));
        grid.addComponent(button);
        button.setDescription(button.getDescription()
                + "<br><strong>Stylename switching logic must be done separately</strong>");

        Link l = new Link("Link: vaadin.com", new ExternalResource(
                "http://vaadin.com"));
        grid.addComponent(l);

        l = new Link("Link: vaadin.com", new ExternalResource(
                "http://vaadin.com"));
        l.setIcon(new ThemeResource("../runo/icons/32/globe.png"));
        grid.addComponent(l);

        return grid;
    }
    
    private Layout getTextFieldPreviews() {
        Layout grid = getPreviewLayout("Text fields");

        TextField tf = new TextField();
        tf.setValue("Text field");
        grid.addComponent(tf);

        tf = new TextField();
        tf.setValue("Small field");
        tf.setStyleName("small");
        grid.addComponent(tf);

        tf = new TextField();
        tf.setValue("Big field");
        tf.setStyleName("big");
        tf.setComponentError(new UserError("Test error"));
        grid.addComponent(tf);

        tf = new TextField();
        tf.setInputPrompt("Search field");
        tf.setStyleName("search");
        grid.addComponent(tf);

        tf = new TextField();
        tf.setInputPrompt("Small search");
        tf.setStyleName("search small");
        grid.addComponent(tf);

        tf = new TextField();
        tf.setInputPrompt("Big search");
        tf.setStyleName("search big");
        grid.addComponent(tf);

        tf = new TextField("Error");
        tf.setComponentError(new UserError("Test error"));
        grid.addComponent(tf);

        tf = new TextField();
        tf.setInputPrompt("Error");
        tf.setComponentError(new UserError("Test error"));
        grid.addComponent(tf);

        tf = new TextField();
        tf.setInputPrompt("Small error");
        tf.setStyleName("small");
        tf.setComponentError(new UserError("Test error"));
        grid.addComponent(tf);

        tf = new TextField();
        tf.setInputPrompt("Multiline");
        tf.setRows(4);
        grid.addComponent(tf);

        tf = new TextField();
        tf.setInputPrompt("Small multiline");
        tf.setStyleName("small");
        tf.setRows(4);
        grid.addComponent(tf);

        tf = new TextField();
        tf.setInputPrompt("Big multiline");
        tf.setStyleName("big");
        tf.setRows(4);
        grid.addComponent(tf);

        return grid;
    }
    
    GridLayout getPreviewLayout(String caption) {
        GridLayout grid = new GridLayout(3, 1);
        grid.setWidth("100%");
        grid.setSpacing(true);
        grid.setMargin(true);
        grid.setCaption(caption);
        return grid;
    }

}
