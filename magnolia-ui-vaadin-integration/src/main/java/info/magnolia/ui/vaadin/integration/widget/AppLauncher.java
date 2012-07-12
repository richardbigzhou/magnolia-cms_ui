/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.vaadin.integration.widget;

import info.magnolia.ui.vaadin.integration.widget.client.applauncher.VAppLaucnher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gson.Gson;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;

/**
 * Server side of AppLauncher. 
 */

@SuppressWarnings("serial")
@ClientWidget(value = VAppLaucnher.class, loadStyle = LoadStyle.EAGER)
public class AppLauncher extends AbstractComponent implements ServerSideHandler {

    private Map<String, AppSection> appSections = new HashMap<String, AppLauncher.AppSection>();
    
    private ServerSideProxy proxy = new ServerSideProxy(this) {
        {
            register("appActivated", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    final String appName = String.valueOf(params[0]);
                    fireAppChangedEvent(appName);
                }
            });
        }
    };

    private boolean isAttached = false;
    
    
    public AppLauncher() {
        super();
        setSizeFull();
        setImmediate(true);
    }

    public void addAppSection(String caption, String color, boolean isPermanent) {
        final AppSection section = new AppSection(caption, color, isPermanent); 
        appSections.put(caption, section);
        if (isAttached) {
            proxy.call("addSection", new Gson().toJson(section));     
        }
    }

    public void addAppTile(String caption, String icon, String sectionId) {
        final AppSection section = appSections.get(sectionId);
        if (section != null) {
            final AppTile tile = new AppTile(caption, icon);
            section.addAppTile(tile);
            if (isAttached) {
                proxy.call("addAppThumbnail", new Gson().toJson(tile), sectionId);   
            }   
        }
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        proxy.paintContent(target);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        proxy.changeVariables(source, variables);
    }

    @Override
    public Object[] initRequestFromClient() {
        for (final AppSection section : appSections.values()) {
            addAppSection(section.caption, section.backgroundColor, section.isPermanent);
            for (final AppTile tile : section.getAppTiles()) {
                addAppTile(tile.caption, tile.icon, section.caption);
            }
        }
        return new Object[] {};
    }

    @Override
    public void attach() {
        super.attach();
        isAttached = true;
    }
    
    @Override
    public void detach() {
        super.detach();
        isAttached = false;
        for (final AppSection section : appSections.values()) {
            section.getAppTiles().clear();
        }
        appSections.clear();
    }
    
    @Override
    public void callFromClient(String method, Object[] params) {
        throw new RuntimeException("Unknown call from client: " + method);
    }

    /**
     * Category changed event.
     */
    public static class AppActivatedEvent extends Component.Event {

        public static final java.lang.reflect.Method ON_APP_ACTIVATED;

        static {
            try {
                ON_APP_ACTIVATED = AppActivationListener.class.getDeclaredMethod("onAppActivated", new Class[] { AppActivatedEvent.class });
            } catch (final java.lang.NoSuchMethodException e) {
                throw new java.lang.RuntimeException(e);
            }
        }

        private final String appName;
        
        public AppActivatedEvent(Component source, String appName) {
            super(source);
            this.appName = appName;
        }
        
        public String getAppName() {
            return appName;
        }

    }

    /**
     * App activation listener. 
     */
    public interface AppActivationListener {
        public void onAppActivated(final AppActivatedEvent event);
    }

    public void addAppActivationListener(final AppActivationListener listener) {
        addListener("app_activated", AppActivatedEvent.class, listener, AppActivatedEvent.ON_APP_ACTIVATED);
    }

    public void removeAppActivationListener(final AppActivationListener listener) {
        removeListener("app_activated", AppActivatedEvent.class, listener);
    }
    
    private void fireAppChangedEvent(String appName) {
        fireEvent(new AppActivatedEvent(this, appName));
    }
    

    public void setAppActive(String appName, boolean isActive) {
        proxy.call("setAppActive", appName, isActive);
    }
    
    /**
     * Represents one tile in the AppLauncher. 
     */
    public static class AppTile implements Serializable {
        
        private String caption;
        
        private String icon;
        
        public AppTile(String caption, String icon) {
            this.caption = caption;
            this.icon = icon;
        }
        
        public String getCaption() {
            return caption;
        }
        
        public String getIcon() {
            return icon;
        }
    }
    
    /**
     * Represents a section of tiles in the AppLauncher. 
     */
    public static class AppSection implements Serializable {
        
        private transient List<AppTile> appTiles = new ArrayList<AppTile>();
        
        private String caption;
        
        private String backgroundColor;
        
        private boolean isPermanent;
        
        public AppSection(String caption, String backgroundColor, boolean isPermanent) {
            this.caption = caption;
            this.backgroundColor = backgroundColor;
            this.isPermanent = isPermanent;
        }
        
        public void addAppTile(final AppTile tile) {
            appTiles.add(tile);
        }
        
        
        public String getCaption() {
            return caption;
        }
        
        public String getBackgroundColor() {
            return backgroundColor;
        }
        
        public List<AppTile> getAppTiles() {
            return appTiles;
        }
        
        public boolean isPermanent() {
            return isPermanent;
        }
    }
    
    
}
