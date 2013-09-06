package info.magnolia.ui.framework.overlay;

import com.vaadin.ui.Component;
import info.magnolia.ui.api.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 5:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class ViewAdapter implements View {

    private Component component;

    public ViewAdapter(Component component) {
        this.component = component;
    }

    @Override
    public Component asVaadinComponent() {
        return component;
    }
}
