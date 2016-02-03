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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vaadin.openesignforms.ckeditor.widgetset.client.ui.CKEditorService;
import org.vaadin.openesignforms.ckeditor.widgetset.client.ui.VCKEditorTextField;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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
    protected VMagnoliaRichTextEditor editor;
    public List<String> pluginEvents;
    ValueMap customPlugins = null;

    public VMagnoliaRichTextField() {
        super();
        pluginEvents = new ArrayList<String>();
        loadCKEditor();
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);

        /*
         * External plugins has to be loaded after CKEDITOR instance
         * have been created but before editor instance is created.
         * Store external plugins into member and let CKEditor loader
         * to pick up them. Server side component has the responsibility
         * to send this attribute early enough.
         */
        if (uidl.hasAttribute(VAR_SERVERPLUGINS)) {
            customPlugins = uidl.getMapAttribute(VAR_SERVERPLUGINS);
        }

        // list of plugin events that server is interested of handling.
        if (uidl.hasAttribute(VAR_EVENTNAMES) && this.editor != null) {
            pluginEvents = Arrays.asList(uidl.getStringArrayAttribute(VAR_EVENTNAMES));

            for (String eventName : pluginEvents) {
                this.editor.addListener(this, eventName);
            }
        }

        // Server wants to send an event to a plugin.
        if (uidl.hasAttribute(VAR_FIRE_PLUGIN_EVENT) && this.editor != null) {
            this.editor.fire(
                    uidl.getStringAttribute(VAR_FIRE_PLUGIN_EVENT),
                    uidl.getStringAttribute(VAR_FIRE_PLUGIN_EVENT_VALUE)
            );
        }
    }

    private static native void loadExternalPlugin(String pluginName, String path) /*-{    
                                                                                  $wnd.CKEDITOR.plugins.addExternal( pluginName, path, 'plugin.js' );
                                                                                  }-*/;

    /**
     * Will be invoked from CK plugins.
     */
    @Override
    public void onPluginEvent(String eventName, String data) {
        if (pluginEvents.contains(eventName)) {
            clientToServer.updateVariable(
                    paintableId,
                    VAR_EVENT_PREFIX + eventName,
                    data == null ? "" : data,
                    true);
        }
    }

    /**
     * Initializes CKEditor if not done already and get editor instance
     * injected.
     */
    private void loadCKEditor() {
        if (!CKEditorService.libraryReady()) {
            CKEditorService.loadLibrary(new ScheduledCommand() {
                @Override
                public void execute() {
                    loadPlugins();
                    injectEditorTo(VMagnoliaRichTextField.this);
                }
            });
        } else {
            loadPlugins();
            injectEditorTo(this);
        }
    }

    /**
     * Load plugins from custom path.
     */
    private void loadPlugins() {
        if (customPlugins != null) {
            for (String key : customPlugins.getKeySet()) {
                loadExternalPlugin(key, customPlugins.getString(key));
            }
        }
    }

    protected void setEditor(JavaScriptObject editor) {
        this.editor = (VMagnoliaRichTextEditor) editor;
    }

    /*
     * This method hides a hack. Base class owns privately an instance of
     * CKEditor editor field. Editor object needs to be accessed or else it
     * would not be possible to handle events from custom made CKEditor
     * plugins. As a workaround this method adds a listener for creation of
     * editor objects and if an editor is created inside DIV element with id
     * matching paintableId member of this class, then this editor is the one
     * from the base class => we got access to the private member we need.
     */
    private static native void injectEditorTo(final VMagnoliaRichTextField listener)
    /*-{        
        var createdEvent = function(e) {            
            var listenerInstanceId = listener.@info.magnolia.ui.vaadin.gwt.client.richtext.VMagnoliaRichTextField::getPaintableId()();
            var editorInstanceId = e.editor.element.getId();
            if(listenerInstanceId == editorInstanceId) {
                listener.@info.magnolia.ui.vaadin.gwt.client.richtext.VMagnoliaRichTextField::setEditor(Lcom/google/gwt/core/client/JavaScriptObject;)(e.editor);
                
                e.editor.on('destroy', function(e) {
                    listener.@info.magnolia.ui.vaadin.gwt.client.richtext.VMagnoliaRichTextField::setEditor(Lcom/google/gwt/core/client/JavaScriptObject;)(null);
                });
            }
        };

        $wnd.CKEDITOR.on('instanceCreated', createdEvent);
     }-*/;

    /*
     * Needed by the hack above
     */
    private String getPaintableId() {
        return paintableId;
    }
}
