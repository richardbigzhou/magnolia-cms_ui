/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.richtext;

import java.util.Arrays;
import java.util.List;

import org.vaadin.openesignforms.ckeditor.widgetset.client.ui.CKEditor;
import org.vaadin.openesignforms.ckeditor.widgetset.client.ui.VCKEditorTextField;

import com.google.gwt.user.client.Timer;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.UIDL;
import com.vaadin.client.ValueMap;

/**
 * Magnolia rich text field adds an ability to custom plugins to communicate
 * with the server. This was not possible with the add-on out of the box.
 */
public class VMagnoliaRichTextField extends VCKEditorTextField implements VMagnoliaRichTextEditor.Listener {

    public static final String VAR_EVENTNAMES = "eventnames";
    public static final String VAR_SERVERPLUGINS = "serverplugins";
    public static final String VAR_EVENT_PREFIX = "pluginEvent:";
    public static final String VAR_FIRE_PLUGIN_EVENT = "firePluginEvent";
    public static final String VAR_FIRE_PLUGIN_EVENT_VALUE = "firePluginEventValue";

    private VMagnoliaRichTextEditor editor;
    private List<String> pluginEvents;
    private ValueMap customPlugins;
    private boolean immediate;

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        // Make sure external plugins are assigned before the loading command.
        if (uidl.hasAttribute(VAR_SERVERPLUGINS)) {
            customPlugins = uidl.getMapAttribute(VAR_SERVERPLUGINS);
        }

        // list of plugin events that server is interested of handling.
        if (uidl.hasAttribute(VAR_EVENTNAMES)) {
            pluginEvents = Arrays.asList(uidl.getStringArrayAttribute(VAR_EVENTNAMES));
        }

        if (uidl.hasAttribute(ATTR_IMMEDIATE)) {
            immediate = uidl.getBooleanAttribute(ATTR_IMMEDIATE);
        }

        super.updateFromUIDL(uidl, client);

        // Server wants to send an event to a plugin, we must do this after super value update.
        if (uidl.hasAttribute(VAR_FIRE_PLUGIN_EVENT) && this.editor != null) {
            this.editor.fire(
                    uidl.getStringAttribute(VAR_FIRE_PLUGIN_EVENT),
                    uidl.getStringAttribute(VAR_FIRE_PLUGIN_EVENT_VALUE)
                    );
        }
    }

    @Override
    protected CKEditor loadEditor(String inPageConfig) {
        // Register external plugins
        if (customPlugins != null && customPlugins.getKeySet() != null && !customPlugins.getKeySet().isEmpty()) {
            for (String plugin : customPlugins.getKeySet()) {
                addExternalPlugin(plugin, customPlugins.getString(plugin));
            }
        }

        // Set convenience base path for registering external plugins in custom config.js
        setVaadinDirUrl(clientToServer.getConfiguration().getVaadinDirUrl());

        // Load editor
        editor = (VMagnoliaRichTextEditor) super.loadEditor(inPageConfig);
        return editor;
    }

    private native void setVaadinDirUrl(String vaadinDirUrl) /*-{
        $wnd.CKEDITOR.vaadinDirUrl = vaadinDirUrl;
    }-*/;

    private native void addExternalPlugin(String pluginName, String path) /*-{
        $wnd.CKEDITOR.plugins.addExternal(pluginName, path, 'plugin.js');
    }-*/;

    /**
     * Add plugin listeners when instance is ready.
     */
    @Override
    public void onInstanceReady() {
        super.onInstanceReady();

        // Add plugin listeners
        if (pluginEvents != null && !pluginEvents.isEmpty()) {
            for (String eventName : pluginEvents) {
                editor.addPluginListener(eventName, this);
            }
        }
    }

    @Override
    public void onPluginEvent(String eventName, String data) {
        if (pluginEvents.contains(eventName)) {
            clientToServer.updateVariable(paintableId, VAR_EVENT_PREFIX + eventName, data == null ? "" : data, true);
        }
    }

    /**
     * Override VCKEditorTextField's default behavior, defer update in case field is immediate.
     */
    @Override
    public void onChange() {
        if (editor != null && !editor.isReadOnly()) {
            clientToServer.updateVariable(paintableId, VAR_TEXT, editor.getData(), false);
            if (immediate) {
                valueUpdateTimer.schedule(200);
            }
        }
    }

    private Timer valueUpdateTimer = new Timer() {
        @Override
        public void run() {
            clientToServer.sendPendingVariableChanges();
        }
    };

    @Override
    public void doResize() {
        super.doResize();
    }
}
