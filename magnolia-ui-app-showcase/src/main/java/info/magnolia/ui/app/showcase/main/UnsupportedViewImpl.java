package info.magnolia.ui.app.showcase.main;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class UnsupportedViewImpl implements UnsupportedView {

    private static final long serialVersionUID = 4074959561953183899L;

    @Override
    public Component asVaadinComponent() {
        return new Label("unsupported component demo");
    }

}
