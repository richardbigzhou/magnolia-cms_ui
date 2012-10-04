package info.magnolia.ui.app.showcase.main;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class FormsViewImpl implements FormsView {

    private static final long serialVersionUID = -6955085822490659756L;

    @Override
    public Component asVaadinComponent() {
        return new Label("i am forms demo");
    }

}
