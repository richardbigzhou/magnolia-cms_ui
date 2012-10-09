package info.magnolia.ui.app.showcase.main;

import info.magnolia.ui.widget.dialog.BaseDialog;
import info.magnolia.ui.widget.dialog.DialogView;
import info.magnolia.ui.widget.dialog.FormDialog;
import info.magnolia.ui.widget.dialog.FormSection;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
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
import com.vaadin.ui.Button.ClickEvent;

public class FormsViewImpl implements FormsView {

    private static final long serialVersionUID = -6955085822490659756L;
    
    CssLayout layout = new CssLayout();
    FormDialog dlg = new FormDialog();

    private Listener listener;
    
    public FormsViewImpl() {
        layout.setMargin(true, true, false, true);
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
        createDialog();
    }

    private void createDialog() {
        dlg.setListener(new DialogView.Listener(){

            @Override
            public void executeAction(String actionName) {
                
            }

            @Override
            public void closeDialog() {
                listener.onCloseDialog();
            }
            
        });
        
        dlg.setDialogDescription("Components in a dialog");
        CssLayout layout = new CssLayout();
        createComponents(layout);

        dlg.addDialogSection("", createSection(layout));
    }
    
    private FormSection createSection(Component component) {
        FormSection section = new FormSection();
        section.addComponent(component);
        return section;
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

    @Override
    public BaseDialog asBaseDialog() {
        return dlg;
    }
    
}
