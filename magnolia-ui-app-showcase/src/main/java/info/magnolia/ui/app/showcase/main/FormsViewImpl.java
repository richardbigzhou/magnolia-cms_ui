package info.magnolia.ui.app.showcase.main;

import com.vaadin.ui.Component;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.NativeButton;

import com.vaadin.ui.VerticalLayout;

public class FormsViewImpl implements FormsView {

    private static final long serialVersionUID = -6955085822490659756L;
    
    Layout layout = new VerticalLayout();
    
    public FormsViewImpl() {
        layout.setMargin(true, true, false, true);
        layout.addComponent(new Label("The fields available in a Magnolia" +
        		" Form or Dialog. Configurable by repository or code."));
        
        layout.addComponent(getButtonPreviews());
        
    }

    private Layout getButtonPreviews() {
        NativeButton sendButton = new NativeButton("Commit button");
        sendButton.addStyleName("btn-dialog");
        sendButton.addStyleName("btn-dialog-commit");
        
        NativeButton resetButton = new NativeButton("Cancel button");
        resetButton.addStyleName("btn-dialog");
        resetButton.addStyleName("btn-dialog-cancel");
        
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setCaption("Buttons in a form");
        buttons.addStyleName("buttons");
        buttons.setSpacing(true);
        buttons.addComponent(sendButton);
        buttons.addComponent(resetButton);
        return buttons;
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }
    
}
