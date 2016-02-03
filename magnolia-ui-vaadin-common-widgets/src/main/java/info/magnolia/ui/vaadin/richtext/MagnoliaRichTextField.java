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

import org.apache.commons.lang3.StringUtils;
import org.vaadin.openesignforms.ckeditor.CKEditorTextField;

import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;


/**
 * Extended CKEditorTextField for custom made Magnolia plugins.
 * By default CKEditor wrapper for Vaadin does not allow
 * custom events between CKEditor plugins and server.
 */

public class MagnoliaRichTextField extends CKEditorTextField {

    private String fireEvent;
    private String fireEventValue;
    private String[] customEvents;
    private Map<String, String> serverPlugins;
    private List<PluginListener> listeners = new ArrayList<PluginListener>();
    private MagnoliaRichTextFieldConfig config;

    public MagnoliaRichTextField() {
        super();
    }

    public void addListener(PluginListener listener) {
        listeners.add(listener);
    }

    public MagnoliaRichTextField(MagnoliaRichTextFieldConfig config) {
        super(config);
        this.config = config;
        serverPlugins = config.getExternalPlugins();
        customEvents = config.getListenedEvents();
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);

        if(isEmpty()) {
            this.setValue(null, true);
        }

        if (config != null && config.getListenedEvents().length > 0) {
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
        markAsDirty();
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

        if (serverPlugins != null) {
            target.addAttribute(VMagnoliaRichTextField.VAR_SERVERPLUGINS, serverPlugins);
        }

        // tell client that server is interested of these events
        if (customEvents != null) {
            target.addAttribute(VMagnoliaRichTextField.VAR_EVENTNAMES, customEvents);
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

    @Override
    protected boolean isEmpty() {
        return StringUtils.isEmpty(getValue());
    }
}
