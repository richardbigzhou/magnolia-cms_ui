/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.widget.jquerywrapper.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * Helper class that holds the animation settings.
 * @author p4elkin
 *
 */
public class AnimationSettings {
    
    private final Map<String, Object> properties = new HashMap<String, Object>();
   
    private Callbacks callbacks = Callbacks.create();
    
    public void setProperty(final String properyName, final Object value) {
        properties.put(properyName, value);
    }
    
    public void addCallback(final JQueryCallback callback) {
        callbacks.add(callback);
    }
    
    public void setCallbacks(final Callbacks callbacks) {
        this.callbacks = callbacks;
    }
    
    final JavaScriptObject getCallbacks() {
        return callbacks;
    }
    
    JavaScriptObject asJSO() {
        final JSONObject parameter = new JSONObject();
        if (properties !=null){
        for (String key : properties.keySet()) {
            String value = String.valueOf(properties.get(key));
            parameter.put(key, new JSONString(value));
        }
        }
        return parameter.getJavaScriptObject();
    }
    
}
