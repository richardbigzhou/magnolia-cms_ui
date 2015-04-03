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
package info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.connector;

import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutClientRpc;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutServerRpc;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.shared.ThumbnailData;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.widget.LazyThumbnailLayoutWidget;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.widget.ThumbnailWidget;
import info.magnolia.ui.vaadin.gwt.client.pinch.MagnoliaPinchStartEvent;
import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.user.client.Element;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapEvent;
import com.googlecode.mgwt.dom.client.recognizer.tap.MultiTapHandler;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

/**
 * ThumbnailLayoutConnector.
 */
@Connect(LazyThumbnailLayout.class)
public class LazyThumbnailLayoutConnector extends AbstractComponentConnector {

    private final ThumbnailLayoutServerRpc rpc = RpcProxy.create(ThumbnailLayoutServerRpc.class, this);

    @Override
    protected void init() {
        super.init();
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

        getWidget().addDomHandler(new ContextMenuHandler() {

            @Override
            public void onContextMenu(ContextMenuEvent event) {
                final Element element = event.getNativeEvent().getEventTarget().cast();
                final ThumbnailWidget thumbnail = Util.findWidget(element, ThumbnailWidget.class);
                if (thumbnail != null) {
                    getWidget().setSelectedThumbnail(thumbnail);
                    rpc.onThumbnailRightClicked(thumbnail.getId(), event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
                }
            }
        }, ContextMenuEvent.getType());

        getWidget().addHandler(new MultiTapHandler() {
            @Override
            public void onMultiTap(MultiTapEvent event) {
                int x = event.getTouchStarts().get(0).get(0).getPageX();
                int y = event.getTouchStarts().get(0).get(0).getPageY();
                final Element element = Util.getElementFromPoint(x, y);
                final ThumbnailWidget thumbnail = Util.findWidget(element, ThumbnailWidget.class);
                rpc.onThumbnailDoubleClicked(thumbnail.getId());
            }
        }, MultiTapEvent.getType());

        getWidget().addHandler(new MagnoliaPinchStartEvent.Handler() {
            @Override
            public void onPinchStart(MagnoliaPinchStartEvent event) {
                getWidget().clear();
                rpc.clearThumbnails();
            }
        }, MagnoliaPinchStartEvent.TYPE);

        registerRpc(ThumbnailLayoutClientRpc.class, new ThumbnailLayoutClientRpc() {

            @Override
            public void addThumbnails(List<ThumbnailData> thumbnails) {
                getWidget().addImages(thumbnails, LazyThumbnailLayoutConnector.this);
            }

            @Override
            public void setSelected(String thumbnailId) {
                getWidget().setSelectedThumbnail(thumbnailId);
            }
        });

        getLayoutManager().addElementResizeListener(getWidget().getElement(), new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
                getWidget().generateStubs();
            }
        });
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        if (getState().lastQueried == null) {
            getWidget().reset();
        }
        getWidget().setThumbnailSize(getState().size.width, getState().size.height);
        getWidget().setThumbnailAmount(getState().thumbnailAmount);
    }

    @Override
    public LazyThumbnailLayoutWidget getWidget() {
        return (LazyThumbnailLayoutWidget) super.getWidget();
    }

    @Override
    public ThumbnailLayoutState getState() {
        return (ThumbnailLayoutState) super.getState();
    }

    @Override
    protected LazyThumbnailLayoutWidget createWidget() {
        final LazyThumbnailLayoutWidget layout = new LazyThumbnailLayoutWidget();
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
