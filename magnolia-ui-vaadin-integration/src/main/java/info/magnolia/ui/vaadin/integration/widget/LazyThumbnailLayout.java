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
package info.magnolia.ui.vaadin.integration.widget;

import info.magnolia.ui.vaadin.integration.widget.client.VLazyThumbnailLayout;
import info.magnolia.ui.vaadin.integration.widget.serializer.ResourceSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gson.GsonBuilder;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 * Lazy layout of asset thumbnails.
 * 
 */
@ClientWidget(VLazyThumbnailLayout.class)
public class LazyThumbnailLayout extends AbstractComponent implements ServerSideHandler {

    private int thumbnailsAmount = 1000;

    private int thumbnailWidth = 0;

    private int thumbnailHeight = 0;

    private ThumbnailProvider provider;

    private ArrayList<Resource> resources = new ArrayList<Resource>() {
        @Override
        public Resource get(int index) {
            if (size() <= index) {
                return null;
            }
            return super.get(index);
        };
    };

    private ServerSideProxy proxy = new ServerSideProxy(this) {
        {
            register("loadThumbnails", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final GsonBuilder gson = new GsonBuilder().registerTypeAdapter(ExternalResource.class, new ResourceSerializer());
                    int amount = (Integer) params[0];
                    final List<Resource> resources = provider.getThumbnails(amount);
                    proxy.call("addThumbnails", gson.create().toJson(resources));
                    LazyThumbnailLayout.this.resources.addAll(resources);
                }
            });
        }
    };

    public LazyThumbnailLayout(final ThumbnailProvider provider) {
        setImmediate(true);
        setProvider(provider);
    }

    private void setProvider(ThumbnailProvider provider) {
        this.provider = provider;
        setThumbnailAmount(provider.getThumbnailsAmount());
    }

    public void setThumbnailAmount(int thumbnailAmount) {
        thumbnailAmount = thumbnailAmount > 0 ? thumbnailAmount : 0;
        proxy.callOnce("setThumbnailAmount", thumbnailAmount);
    }

    public void setThumbnailSize(int width, int height) {
        this.thumbnailWidth = width;
        this.thumbnailHeight = height;
        proxy.callOnce("setThumbnailSize", width, height);
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
        proxy.callOnce("setThumbnailSize", thumbnailWidth, thumbnailHeight);
        proxy.callOnce("setThumbnailAmount", thumbnailsAmount);
        return new Object[] {};
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        throw new RuntimeException();
    }

    /**
     * Interface for the providers of the actual thumbnails.
     */
    public interface ThumbnailProvider {

        int getThumbnailsAmount();

        List<Resource> getThumbnails(int amount);

    }

}
