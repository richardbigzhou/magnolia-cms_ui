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
package info.magnolia.ui.vaadin.richtext;

import info.magnolia.ui.vaadin.gwt.client.richtext.VMagnoliaRichTextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vaadin.openesignforms.ckeditor.CKEditorTextField;
import org.vaadin.openesignforms.ckeditor.widgetset.client.ui.VCKEditorTextField;

import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;


/**
 * Extended CKEditorTextField for custom made Magnolia plugins.
 * By default CKEditor wrapper for Vaadin does not allow
 * custom events between CKEditor plugins and server.
 */

public class MagnoliaRichTextField extends CKEditorTextField {

    private String fireEvent = null;
    private String fireEventValue = null;
    private String[] customEvents = null;
    private Map<String, String> serverPlugins = null;
    private List<PluginListener> listeners = new ArrayList<PluginListener>();
    private MagnoliaRichTextFieldConfig config = null;

    public MagnoliaRichTextField() {
        super();
    }

    public void addListener(PluginListener listener) {
        listeners.add(listener);
    }

    public MagnoliaRichTextField(MagnoliaRichTextFieldConfig config) {
        super(config);
        this.config = config;
        serverPlugins = config.getServerPlugins();
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);

        if (config != null && config.getListenedEvents().length > 0) {
            // Editor is ready
            if (variables.containsKey(VCKEditorTextField.VAR_VERSION)) {
                customEvents = config.getListenedEvents();

                requestRepaint();
            }

            // See if client sends events
            for (String eventName : config.getListenedEvents()) {
                String eventNameResolved = VMagnoliaRichTextField.VAR_EVENT_PREFIX + eventName;
                if (variables.containsKey(eventNameResolved)) {
                    for (PluginListener listener : listeners) {
                        listener.onPluginEvent(eventName, variables.get(eventNameResolved).toString());
                    }
                }
            }
        }
    }

    /**
     * Send event to CKEditor instance.
     *
     * @param event Event name that plugin can attach with: editor.on('event', function(e) {...});
     * @param value Additional event data. Plugin can access this by e.data
     */
    public void firePluginEvent(String event, String value) {
        fireEvent = event;
        fireEventValue = value;
        requestRepaint();
    }

    /**
     * Send event to CKEditor instance. Only event. No additional event data.
     */
    public void firePluginEvent(String event) {
        firePluginEvent(event, "");
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        // tell client that server is interested of these events
        if (customEvents != null) {
            target.addAttribute(VMagnoliaRichTextField.VAR_EVENTNAMES, customEvents);
            customEvents = null;
        }

        if (serverPlugins != null) {
            target.addAttribute(VMagnoliaRichTextField.VAR_SERVERPLUGINS, serverPlugins);
            serverPlugins = null;
        }

        // send event to plugin
        if (fireEvent != null && fireEventValue != null) {
            target.addAttribute(VMagnoliaRichTextField.VAR_FIRE_PLUGIN_EVENT, fireEvent);
            target.addAttribute(VMagnoliaRichTextField.VAR_FIRE_PLUGIN_EVENT_VALUE, fireEventValue);
            fireEvent = null;
            fireEventValue = null;
        }
    }

    /**
     * Event handler listener for plugin connectivity.
     */
    public interface PluginListener {
        void onPluginEvent(String eventName, String value);
    }
}
