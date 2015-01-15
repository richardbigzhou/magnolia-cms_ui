/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.openesignforms.ckeditor.CKEditorConfig;

import com.google.gson.Gson;

/**
 * Extends CKEditorConfig by defining more
 * coherent toolbar API and configuring custom plugins.
 */
public class MagnoliaRichTextFieldConfig extends CKEditorConfig {

    private List<String> events = new ArrayList<String>();

    private Map<String, String> externalPlugins = new HashMap<String, String>();

    /**
     * Adds list of toolbar groups. Groups are placed in one
     * row if space allows.
     */
    public void addToolbarLine(List<ToolbarGroup> toolbars) {
        Gson gson = new Gson();
        String json = gson.toJson(toolbars);
        addCustomToolbarLine(json.substring(1, json.length() - 1));
    }

    /**
     * Add CKEditor event name that server side will listen.
     * Only event names added to configuration will be listened.
     *
     * @param eventName This must match in client side. e.g: editor.fire('eventName')
     */
    public void addListenedEvent(String eventName) {
        events.add(eventName);
    }

    public String[] getListenedEvents() {
        return events.toArray(new String[0]);
    }

    /**
     * @deprecated Since 5.3.4, use {@link #addExternalPlugin(String, String)} and {@link #addToExtraPlugins(String)} separately.
     * However, do not use {@link #addToExtraPlugins(String)} if you're using a custom CKEditor config.js file — otherwise you won't be able to override config.extraPlugins there.
     */
    @Deprecated
    public void addPlugin(String pluginName, String source) {
        addToExtraPlugins(pluginName);
        externalPlugins.put(pluginName, source);
    }

    public void addExternalPlugin(String pluginName, String source) {
        externalPlugins.put(pluginName, source);
    }

    /**
     * @deprecated Since 5.3.4, renamed to {@link #getExternalPlugins()}.
     */
    public Map<String, String> getServerPlugins() {
        return Collections.unmodifiableMap(externalPlugins);
    }

    public Map<String, String> getExternalPlugins() {
        return externalPlugins;
    }

    /**
     * Bean class for toolbar group.
     */
    public static class ToolbarGroup {

        private String name;

        private List<String> items;

        public ToolbarGroup(String groupname, String[] toolbarbuttons) {
            this.name = groupname;
            this.items = new ArrayList<String>();
            for (String item : toolbarbuttons) {
                this.items.add(item);
            }
        }

        public String getName() {
            return name;
        }

        public void addItem(String item) {
            items.add(item);
        }
    }
}
