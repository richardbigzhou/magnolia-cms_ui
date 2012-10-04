package info.magnolia.ui.app.showcase.main;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class VaadinViewImpl implements VaadinView {

    private static final long serialVersionUID = 4937209277244291844L;

    @Override
    public Component asVaadinComponent() {
        // TODO Auto-generated method stub
        return new Label("i am vaadin demo");
    }

}
