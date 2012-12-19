package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.magnoliashell.viewport.AppsViewport;

import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.shared.ui.Connect;

@Connect(AppsViewport.class)
public class AppsViewportConnector extends ViewportConnector {

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        /*if (RootPanel.get().getWidgetIndex(preloader) >= 0) {
            new Timer() {

                @Override
                public void run() {
                    RootPanel.get().remove(preloader);
                }
            }.schedule(500);*/
    }
    
    @Override
    protected ViewportWidget createWidget() {
        return new AppsViewportWidget();
    }

}
