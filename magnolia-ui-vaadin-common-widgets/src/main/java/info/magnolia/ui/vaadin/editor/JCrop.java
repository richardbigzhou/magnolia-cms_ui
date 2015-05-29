/**
 * This file Copyright (c) 2010-2015 Magnolia International
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

import info.magnolia.ui.vaadin.editor.CroppableImage.JCropReleaseEvent;
import info.magnolia.ui.vaadin.editor.CroppableImage.JCropSelectionEvent;
import info.magnolia.ui.vaadin.gwt.client.jcrop.JCropState;
import info.magnolia.ui.vaadin.gwt.shared.jcrop.SelectionArea;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.JavaScriptFunction;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;


/**
 * An {@link Image} extension that operates JCrop JQuery plugin ({@link http://deepliquid.com/content/Jcrop.html}).
 */
@JavaScript({ "jquery.color.js", "jquery.Jcrop.min.js", "jcrop_connector.js" })
public class JCrop extends AbstractJavaScriptExtension {

    private JreJsonFactory jsonFactory = new JreJsonFactory();

    @Override
    public CroppableImage getParent() {
        return (CroppableImage)super.getParent();
    }

    public JCrop(JCropHandler handler) {
        addFunction("doOnSelect", new JavaScriptFunction() {
            @Override
            public void call(JsonArray args) {
                SelectionArea area = AreaFromJSON(args.getObject(0));
                getState(false).selection = area;
                getParent().fireEvent(new JCropSelectionEvent(getParent(), area));
            }
        });

        addFunction("doOnRelease", new JavaScriptFunction() {
            @Override
            public void call(JsonArray args) {
                getState().selection = null;
                getParent().fireEvent(new JCropReleaseEvent(getParent()));
            }
        });

        addFunction("onCreated", new JavaScriptFunction() {
            @Override
            public void call(JsonArray args) {
                getState(false).isValid = true;
            }
        });
    }

    protected SelectionArea AreaFromJSON(JsonObject json) {
            return new SelectionArea(
                    (int)json.getNumber("x"),
                    (int)json.getNumber("y"),
                    (int)json.getNumber("w"),
                    (int)json.getNumber("h"));
    }

    @Override
    public void attach() {
        super.attach();
        getParent().addStyleName("croppable" + getConnectorId());
        if (getState().selectionStatusComponent != null) {
            ((Component)getState().selectionStatusComponent).addStyleName("crop-status" + getConnectorId());
        }
    }

    @Override
    protected JCropState getState(boolean markAsDirty) {
        return (JCropState)super.getState(markAsDirty);
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

    public void animateTo(SelectionArea area) {
        callFunction("animateTo", jsonFactory.create(area.toString()));
    }

    public boolean isCropVisible() {
        return getState().isVisible;
    }

    public void setCropVisible(boolean isVisible) {
        getState().isVisible = isVisible;
    }

    public void enable() {
        callFunction("enable");
    }

    public void disable() {
        callFunction("disable");
    }

    public void setSelectionStatusComponent(Component c) {
        if (getState().selectionStatusComponent != null) {
            ((Component)getState().selectionStatusComponent).removeStyleName("");
        }
        getState().selectionStatusComponent = c;
        if (getSession() != null) {
            c.addStyleName("crop-status" + getConnectorId());
        }
    }

    public void setBackgroundColor(String color) {
        getState().backgroundColor = color;
    }

    public void setBackgroundOpacity(double opacity) {
        getState().backgroundOpacity = opacity;
    }

    public void setMinHeight(int height) {
        getState().minHeight = height;
    }

    public void setMaxHeight(int height) {
        getState().maxHeight = height;
    }

    public void setMaxWidth(int width) {
        getState().maxWidth = width;
    }

    public void setMinWidth(int width) {
        getState().minWidth = width;
    }

    public void setEnabled(boolean enabled) {
        getState().enabled = enabled;
    }

    public void invalidate() {
        getState().isValid = false;
    }

    public void setTrueHeight(int height) {
        getState().trueHeight = height;
    }

    public void setTrueWidth(int width) {
        getState().trueWidth = width;
    }

    public void select(SelectionArea area) {
        getState().selection = area;
    }
}
