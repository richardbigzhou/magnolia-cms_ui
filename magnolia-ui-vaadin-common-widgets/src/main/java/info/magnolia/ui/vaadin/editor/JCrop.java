/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.vaadin.editor;

import info.magnolia.ui.vaadin.gwt.client.editor.shared.SelectionArea;
import info.magnolia.ui.vaadin.gwt.client.jcrop.JCropState;

import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.util.ReflectTools;

/**
 * JCropField.
 */
@JavaScript({"js/jquery.min.js", "js/jquery.color.js", "js/jquery.Jcrop.js", "js/jcrop_connector.js"})
@StyleSheet("css/jquery.Jcrop.css")
public class JCrop extends AbstractJavaScriptExtension {

    /**
     * JCropEvent.
     */
    public static class JCropEvent extends Component.Event {

        private final SelectionArea area;

        public JCropEvent(Component source, SelectionArea area) {
            super(source);
            this.area = area;
        }

        public SelectionArea getArea() {
            return area;
        }

    }

    /**
     * JCropSelectionEvent.
     */
    public static class JCropSelectionEvent extends JCropEvent {
        public JCropSelectionEvent(Component source, SelectionArea area) {
            super(source, area);
        }
    }

    /**
     * JCropReleaseEvent.
     */
    public static class JCropReleaseEvent extends JCropEvent {
        public JCropReleaseEvent(Component source, SelectionArea area) {
            super(source, area);
        }
    }

    /**
     * SelectionListener.
     */
    public interface SelectionListener {
        public static String EVENT_ID = "jcrop_sl";
        public static Method EVENT_METHOD = ReflectTools.findMethod(SelectionListener.class, "onSelected", JCropSelectionEvent.class);

        void onSelected(JCropSelectionEvent e);
    }

    /**
     * ReleaseListener.
     */
    public interface ReleaseListener {
        public static String EVENT_ID = "jcrop_rl";
        public static Method EVENT_METHOD = ReflectTools.findMethod(ReleaseListener.class, "onRelease", JCropReleaseEvent.class);

        void onRelease(JCropReleaseEvent e);
    }

    public void addSelectionListener(SelectionListener listener) {
        addListener(SelectionListener.EVENT_ID, JCropSelectionEvent.class, listener, SelectionListener.EVENT_METHOD);
    }

    public void addReleaseListener(ReleaseListener listener) {
        addListener(ReleaseListener.EVENT_ID, JCropReleaseEvent.class, listener, ReleaseListener.EVENT_METHOD);
    }

    public JCrop() {
        addFunction("doOnSelect", new JavaScriptFunction() {
            @Override
            public void call(JSONArray args) throws JSONException {
                fireEvent(new JCropSelectionEvent((Component) getParent(), AreaFromJSON(args.getJSONObject(0))));
            }
        });

        addFunction("doOnRelease", new JavaScriptFunction() {
            @Override
            public void call(JSONArray args) throws JSONException {
                fireEvent(new JCropReleaseEvent((Component) getParent(), AreaFromJSON(args.getJSONObject(0))));
            }
        });
    }

    protected SelectionArea AreaFromJSON(JSONObject json) {
        try {
            return new SelectionArea(json.getInt("x"), json.getInt("y"), json.getInt("w"), json.getInt("w"));
        } catch (JSONException e) {
            return new SelectionArea();
        }
    }

    @Override
    public void attach() {
        super.attach();
        ((Component) getParent()).addStyleName("croppable" + getConnectorId());
    }

    @Override
    public JCropState getState() {
        return (JCropState) super.getState();
    }

    @Override
    protected Class<Image> getSupportedParentType() {
        return Image.class;
    }

    public void setAspectRatio(double aspectRatio) {
        getState().aspectRatio = aspectRatio;
    }

    public void setCropEnabled(boolean isOn) {
        callFunction(isOn ? "on" : "off");
    }

    public void enable() {
        callFunction("enable");
    }

    public void disable() {
        callFunction("disable");
    }
}
