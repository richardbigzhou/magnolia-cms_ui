package info.magnolia.ui.app.showcase.main;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class FormsViewImpl implements FormsView {

    private static final long serialVersionUID = -6955085822490659756L;
    
    Layout layout = new VerticalLayout();
    
    public FormsViewImpl() {
        layout.setMargin(true, true, false, true);
        layout.addComponent(new Label("The fields available in a Magnolia" +
        		" Form or Dialog. Configurable by repository or code."));
        
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

}
