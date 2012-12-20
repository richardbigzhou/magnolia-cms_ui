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
package info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.connector;

import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutClientRpc;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutServerRpc;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.widget.ThumbnailWidget;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.widget.VLazyThumbnailLayout;
import info.magnolia.ui.vaadin.gwt.client.pinch.MagnoliaPinchStartEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapEvent;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapHandler;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractComponentConnector;

/**
 * ThumbnailLayoutConnector.
 */
public class ThumbnailLayoutConnector extends AbstractComponentConnector {

    private final ThumbnailLayoutServerRpc rpc = RpcProxy.create(ThumbnailLayoutServerRpc.class, this);
    
    private final StateChangeHandler sizeChangeHandler = new StateChangeHandler() {
        @Override
        public void onStateChanged(StateChangeEvent stateChangeEvent) {
            getWidget().setThumbnailSize(getState().size.width, getState().size.height);
        }
    };
    
    @Override
    protected void init() {
        super.init();
        addStateChangeHandler("thumbnailSize", sizeChangeHandler);
        getWidget().addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final Element element = event.getNativeEvent().getEventTarget().cast();
                final ThumbnailWidget thumbnail = Util.findWidget(element, ThumbnailWidget.class);
                if (thumbnail != null) {
                    getWidget().setSelectedThumbnail(thumbnail);
                    rpc.onThumbnailSelected(thumbnail.getId());
                }
            }
        }, ClickEvent.getType());
        
        getWidget().addHandler(new MultiTapHandler() {
            @Override
            public void onMultiTap(MultiTapEvent event) {
                int x = event.getTouchStarts().get(0).get(0).getPageX();
                int y = event.getTouchStarts().get(0).get(0).getPageY();
                final Element element = Util.getElementFromPoint(x,y);
                final ThumbnailWidget thumbnail = Util.findWidget(element, ThumbnailWidget.class);
                rpc.onThumbnailDoubleClicked(thumbnail.getId());
            }
        }, MultiTapEvent.getType());
        
        getWidget().addHandler(new MagnoliaPinchStartEvent.Handler() {
            @Override
            public void onPinchStart(MagnoliaPinchStartEvent event) {
                getWidget().clear();
                rpc.clear();
            }
        }, MagnoliaPinchStartEvent.TYPE);
        
        registerRpc(ThumbnailLayoutClientRpc.class, new ThumbnailLayoutClientRpc() {
            @Override
            public void clear() {
                getWidget().reset();
            }

            @Override
            public void addThumbnails(List<String> ids) {
                final Map<String, String> urls = new HashMap<String, String>();
                for (final String id : ids) {
                    urls.put(id, getResourceUrl(id));
                }
                getWidget().addImages(urls);
            }
        });
    }
    
    @Override
    public VLazyThumbnailLayout getWidget() {
        return (VLazyThumbnailLayout)super.getWidget();
    }
    
    @Override
    public ThumbnailLayoutState getState() {
        return (ThumbnailLayoutState)super.getState();
    }
    
    @Override
    protected VLazyThumbnailLayout createWidget() {
        final VLazyThumbnailLayout layout = new VLazyThumbnailLayout();
        layout.setThumbnailService(new ThumbnailService() {
            @Override
            public void loadThumbnails(int amount) {
                rpc.loadThumbnails(amount);
            }
        });
        return layout;
    }
    
    /**
     * Serves Thumbnails.
     */
    public interface ThumbnailService {
        void loadThumbnails(int amount);
    }
}
