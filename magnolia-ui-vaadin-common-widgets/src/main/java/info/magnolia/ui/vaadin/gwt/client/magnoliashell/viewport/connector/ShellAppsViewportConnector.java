package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ShellAppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellAppsViewport;

import com.vaadin.shared.ui.Connect;

@Connect(ShellAppsViewport.class)
public class ShellAppsViewportConnector extends ViewportConnector {
    
    @Override
    protected ViewportWidget createWidget() {
        return new ShellAppsViewportWidget();
    }
}
