package info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.MagnoliaShellConnector.ViewportType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.ShellAppLauncher.ShellAppType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.AbstractLayoutState;

public class MagnoliaShellState extends AbstractLayoutState {

    public List<String> runningAppNames = new ArrayList<String>();
    
    public List<String> registeredAppNames = new ArrayList<String>();
    
    public Map<ShellAppType, Integer> indications = new HashMap<ShellAppType, Integer>();
    
    public Map<ViewportType, Connector> viewports = new EnumMap<ViewportType, Connector>(ViewportType.class);
    
    public Connector activeViewport = null;
}
