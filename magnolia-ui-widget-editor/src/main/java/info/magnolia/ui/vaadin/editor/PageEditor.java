/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.ui.vaadin.gwt.client.connector.PageEditorState;
import info.magnolia.ui.vaadin.gwt.client.rpc.PageEditorClientRpc;
import info.magnolia.ui.vaadin.gwt.client.rpc.PageEditorServerRpc;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;

import java.util.Map;

import com.google.gson.Gson;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;

/**
 * PageEditor widget server side implementation.
 */
public class PageEditor extends AbstractComponent implements PageEditorView {

    private PageEditorView.Listener listener;

    private String PAGE_ELEMENT = "cms:page";
    
    private String AREA_ELEMENT = "cms:area";
    
    private String COMPONENT_ELEMENT = "cms:component";

    public PageEditor() {
        setSizeFull();
        setImmediate(true);
    }

    @Override
    public void setListener(PageEditorView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void load(PageEditorParameters parameters) {
        getState().parameters = parameters;
    }

    @Override
    public void init() {
        registerRpc(new PageEditorServerRpc() {
            
            @Override
            public void sortComponent(String workspace, String parentPath, String sourcePath, String targetPath, String order) {
                listener.sortComponent(workspace, parentPath, sourcePath, targetPath, order);
            }
            
            @Override
            public void selectElement(String type, Map<String, String> attributes) {
                AbstractElement element = resolveElement(type, attributes);
                listener.selectElement(element);
            }
            
            @Override
            public void newComponent(String workspace, String eventType, String availableComponents) {
                listener.newComponent(workspace, eventType, availableComponents);
            }
            
            @Override
            public void newArea(String workspace, String nodeType, String path) {
                listener.newArea(workspace, nodeType, path);
            }
            
            @Override
            public void editComponent(String workspace, String eventType, String dialog) {
                listener.editComponent(workspace, eventType, dialog);
            }
            
            @Override
            public void deleteComponent(String workspace, String path) {
                listener.deleteComponent(workspace, path);
                
            }
        });

    }
    
    @Override
    protected PageEditorState getState() {
        return (PageEditorState)super.getState();
    }

    private AbstractElement resolveElement(String type, Map<String, String> attributes) {
        AbstractElement element = null;
        Gson gson = new Gson();
        if (type.equals(PAGE_ELEMENT)) {
            element = gson.fromJson(gson.toJson(attributes), PageElement.class);
        }
        else if (type.equals(AREA_ELEMENT)) {
            element = gson.fromJson(gson.toJson(attributes), AreaElement.class);
        }
        else if (type.equals(COMPONENT_ELEMENT)) {
            element = gson.fromJson(gson.toJson(attributes), ComponentElement.class);
        }
        return element;
    }

    @Override
    public void refresh() {
        getRpcProxy(PageEditorClientRpc.class).refresh();
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }
}
