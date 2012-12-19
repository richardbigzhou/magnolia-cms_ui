package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.MagnoliaShellConnector.ViewportType;

import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.AbstractLayoutState;

public class ViewportState extends AbstractLayoutState {

    public ViewportType type;
    
    public Connector activeComponent;
}
