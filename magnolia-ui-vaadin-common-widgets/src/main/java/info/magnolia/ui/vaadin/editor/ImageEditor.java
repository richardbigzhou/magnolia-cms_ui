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
package info.magnolia.ui.vaadin.editor;

import info.magnolia.ui.vaadin.gwt.client.editor.VImageEditor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 * Server side component for the ImageEditor widget.
 */
@ClientWidget(VImageEditor.class)
public class ImageEditor extends AbstractComponent implements ServerSideHandler {

    private static final int DEFAULT_MIN_DIMENSION = 200;

    private int minDimension = DEFAULT_MIN_DIMENSION;

    private int marginsPx = 20;

    private boolean isCropAspectRatioLocked = false;

    private boolean isAttached = false;

    private String fileName = "";

    private String mimeType = "";

    private final ServerSideProxy proxy = new ServerSideProxy(this) {
        {
            register("croppedAreaReady", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    final CropArea area = new CropArea(getInt(params[0]), getInt(params[1]), getInt(params[2]), getInt(params[3]));
                    for (final CropListener listener : listeners) {
                        listener.onCrop(area);
                    }
                }
            });
        }

        public int getInt(Object intObj) {
            return (Integer) intObj;
        }
    };

    private Resource source;

    private final List<CropListener> listeners = new ArrayList<CropListener>();

    private boolean isCropping = false;

    public ImageEditor() {
        setImmediate(true);
    }

    public void addCropListener(CropListener listener) {
        listeners.add(listener);
    }

    public void setMarginsPx(int marginsPx) {
        this.marginsPx = marginsPx;
        callOnceIfAttached("setMarginsPx", marginsPx);
    }

    public void setSource(Resource source) {
        this.source = source;
        callOnceIfAttached("setSource", this.source);
    }

    public void setCropping(boolean isCropping) {
        this.isCropping = isCropping;
        callOnceIfAttached("setCropping", isCropping);
    }

    public void fetchCropArea() {
        callOnceIfAttached("fetchCropArea");
    }

    public void setCropAspectRatioLocked(boolean isLocked) {
        this.isCropAspectRatioLocked = isLocked;
        callOnceIfAttached("lockAspectRatio", isLocked);
    }

    public void setMinDimension(int minDimension) {
        this.minDimension = minDimension;
        callOnceIfAttached("setMinDimension", minDimension);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        callOnceIfAttached("setFileName", fileName);
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
        callOnceIfAttached("setMimeType", mimeType);
    }

    public boolean isCropping() {
        return isCropping;
    }

    public boolean isAspectRatioLocked() {
        return this.isCropAspectRatioLocked;
    }

    private void callOnceIfAttached(String methodName, Object... params) {
        if (isAttached) {
            proxy.callOnce(methodName, params);
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
        proxy.callOnce("setSource", this.source);
        proxy.callOnce("setCropping", isCropping);
        proxy.callOnce("setMinDimension", minDimension);
        proxy.callOnce("setMarginsPx", marginsPx);
        proxy.callOnce("setFileName", fileName);
        proxy.callOnce("setMimeType", mimeType);
        if (isAspectRatioLocked()) {
            proxy.callOnce("lockAspectRatio", isCropAspectRatioLocked);
        }
        return new Object[] {};
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        throw new RuntimeException("Unhandled call from client: " + method);
    }

    @Override
    public void attach() {
        super.attach();
        this.isAttached = true;
    }

    @Override
    public void detach() {
        super.detach();
        this.isAttached = false;
    }

    /**
     * CropListener.
     */
    public interface CropListener {
        void onCrop(final CropArea area);
    }

    /**
     * CropArea.
     */
    public static class CropArea implements Serializable {
        private final int top;

        private final int left;

        private final int width;

        private final int height;

        public CropArea(int left, int top, int width, int height) {
            this.top = top;
            this.left = left;
            this.width = width;
            this.height = height;
        }

        public int getTop() {
            return top;
        }

        public int getLeft() {
            return left;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public String toString() {
            return "[" + "w:" + getWidth() + "h:" + getHeight() + "l:" + getLeft() + "t:" + getTop() + "]";
        }
    }
}
