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
package info.magnolia.ui.vaadin.gwt.client.editor.connector;

import info.magnolia.ui.vaadin.editor.ImageEditor;
import info.magnolia.ui.vaadin.gwt.client.editor.rpc.ImageSelectorClientRpc;
import info.magnolia.ui.vaadin.gwt.client.editor.rpc.ImageSelectorServerRpc;
import info.magnolia.ui.vaadin.gwt.client.editor.widget.VImageSelector;

import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

/**
 * ImageSelectorConnector.
 */
@Connect(ImageEditor.class)
public class ImageSelectorConnector extends AbstractComponentConnector {

    private final ImageSelectorServerRpc rpc = RpcProxy.create(ImageSelectorServerRpc.class, this);
    
    @Override
    protected void init() {
        super.init();
        registerRpc(ImageSelectorClientRpc.class, new ImageSelectorClientRpc() {
            @Override
            public void fetchSelectedArea() {
                rpc.onSelectedAreaReady(getWidget().getSelectionArea());
            }
        });
        
        getLayoutManager().addElementResizeListener(getWidget().getElement(), new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
                getWidget().adjustWidth(e.getLayoutManager().getOuterWidth(e.getElement()));
                getWidget().adjustHeight(e.getLayoutManager().getOuterHeight(e.getElement()));
            }
        });
    }
    
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        if (getResourceUrl("source") != null) {
            getWidget().setSource(getResourceUrl("source"));
        }
        
        getWidget().setIsCropping(getState().isCropping);
    }
    
    
    @Override
    public VImageSelector getWidget() {
        return (VImageSelector)super.getWidget();
    }
    
    @Override
    public ImageSelectorState getState() {
        return (ImageSelectorState)super.getState();
    }
    
}
