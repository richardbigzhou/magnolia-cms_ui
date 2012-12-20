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

import info.magnolia.ui.vaadin.gwt.client.editor.connector.ImageSelectorState;
import info.magnolia.ui.vaadin.gwt.client.editor.rpc.ImageSelectorClientRpc;
import info.magnolia.ui.vaadin.gwt.client.editor.rpc.ImageSelectorServerRpc;
import info.magnolia.ui.vaadin.gwt.client.editor.shared.SelectionArea;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the ImageEditor widget.
 */
public class ImageEditor extends AbstractComponent {

    private final List<CropListener> listeners = new ArrayList<CropListener>();

    public ImageEditor() {
        setImmediate(true);
        registerRpc(new ImageSelectorServerRpc() {
            @Override
            public void onSelectedAreaReady(SelectionArea area) {
                for (final CropListener listener : listeners) {
                    listener.onCrop(area);
                }
            }
        });
    }

    public void addCropListener(CropListener listener) {
        listeners.add(listener);
    }

    public void setMarginsPx(int marginsPx) {
        getState().marginsPx = marginsPx;
    }

    public void setSource(Resource source) {
        setResource("source", source);
    }

    public void setCropping(boolean isCropping) {
        getState().isCropping = isCropping;
    }

    public void fetchCropArea() {
        getRpcProxy(ImageSelectorClientRpc.class).fetchSelectedArea();
    }

    public void setCropAspectRatioLocked(boolean isLocked) {
        getState().isCropAspectRatioLocked = isLocked;
    }

    public void setMinDimension(int minDimension) {
        getState().minDimension = minDimension;
    }

    public void setFileName(String fileName) {
        getState().fileName = fileName;
    }

    public void setMimeType(String mimeType) {
        getState().mimeType = mimeType;
    }

    public boolean isCropping() {
        return getState(false).isCropping;
    }

    public boolean isAspectRatioLocked() {
        return getState(false).isCropAspectRatioLocked;
    }

    @Override
    protected ImageSelectorState getState() {
        return (ImageSelectorState) super.getState();
    }

    @Override
    protected ImageSelectorState getState(boolean markDirty) {
        return (ImageSelectorState) super.getState(markDirty);
    }

    /**
     * CropListener.
     */
    public interface CropListener {
        void onCrop(final SelectionArea area);
    }
}
