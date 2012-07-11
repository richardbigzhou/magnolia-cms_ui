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

import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;

/**
 * Server side of AppLauncher.
 * 
 */
@SuppressWarnings("serial")
@ClientWidget(value = VAppLaucnher.class, loadStyle = LoadStyle.EAGER)
public class AppLauncher extends AbstractComponent implements ServerSideHandler {

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

    public AppLauncher() {
        super();
        setSizeFull();
        setImmediate(true);
        /*addGroup("EDIT", "#9A3332", true);
        addGroup("MANAGE", "#4B8E9E", true);
        addAppThumbnail("Pages", "icon-assets", "EDIT");
        addAppThumbnail("Templates", "icon-templates", "MANAGE");

        addMinorGroup("TOOLS", "#537800");
        addAppThumbnail("Packager", "icon-packager", "TOOLS");
        addAppThumbnail("Backup", "icon-backup", "TOOLS");
        addAppThumbnail("Packager1", "icon-packager", "TOOLS");
        addAppThumbnail("Backup1", "icon-backup", "TOOLS");
        addAppThumbnail("Packager2", "icon-packager", "TOOLS");
        addAppThumbnail("Backup2", "icon-backup", "TOOLS");

        addMinorGroup("MIGROS", "#537800");
        addAppThumbnail("Packager44", "icon-packager", "MIGROS");
        addAppThumbnail("Backup44", "icon-backup", "MIGROS");
        addAppThumbnail("Packager55", "icon-packager", "MIGROS");
        addAppThumbnail("Backup55", "icon-backup", "MIGROS");
        addAppThumbnail("Packager66", "icon-packager", "MIGROS");
        addAppThumbnail("Backup66", "icon-backup", "MIGROS");*/
    }

    public void addGroup(String caption, String color, boolean isMajor) {
        if (isMajor) {
            proxy.call("addGroup", caption, color);     
        } else {
            proxy.call("addMinorGroup", caption, color);   
        }
       
    }

    public void addAppThumbnail(String caption, String style, String groupId) {
        proxy.call("addAppThumbnail", caption, style, groupId);
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
        return new Object[] {};
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
}
