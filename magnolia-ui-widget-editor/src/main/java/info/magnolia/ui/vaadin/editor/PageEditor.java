/**
 * This file Copyright (c) 2011-2015 Magnolia International
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
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ErrorType;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;

import com.vaadin.ui.AbstractComponent;

/**
 * PageEditor widget server side implementation.
 * Uses the {@link PageEditorListener} interface to call back to page editor presenter.
 */
public class PageEditor extends AbstractComponent {


    private PageEditorListener listener;

    public PageEditor() {
        setSizeFull();
        setImmediate(true);

        registerRpc(new PageEditorServerRpc() {

            @Override
            public void selectPage(PageElement element) {
                listener.onElementSelect(element);
            }

            @Override
            public void selectArea(AreaElement element) {
                listener.onElementSelect(element);
            }

            @Override
            public void selectComponent(ComponentElement element) {
                listener.onElementSelect(element);
            }

            @Override
            public void newComponent(AreaElement areaElement) {
                // make sure pageEditorPresenter's selection is in sync for further use
                listener.onElementSelect(areaElement);
                listener.onAction(PageEditorListener.ACTION_ADD_COMPONENT, areaElement);
            }

            @Override
            public void sortComponent(AreaElement areaElement) {
                listener.onAction(PageEditorListener.ACTION_SORT_COMPONENT, areaElement);
            }

            @Override
            public void newArea(AreaElement areaElement) {
                listener.onAction(PageEditorListener.ACTION_ADD_AREA, areaElement);
            }

            @Override
            public void editComponent(ComponentElement element) {
                listener.onAction(PageEditorListener.ACTION_EDIT_COMPONENT, element);
            }

            @Override
            public void editArea(AreaElement element) {
                listener.onAction(PageEditorListener.ACTION_EDIT_ELEMENT, element);
            }

            @Override
            public void startMoveComponent() {
                listener.onAction(PageEditorListener.ACTION_START_MOVE_COMPONENT);
            }

            @Override
            public void stopMoveComponent() {
                listener.onAction(PageEditorListener.ACTION_STOP_MOVE_COMPONENT);
            }

            @Override
            public void onError(ErrorType errorType, String... parameters) {
                listener.onError(errorType, parameters);
            }
        });
    }

    /**
     * Load the page editor with the parameters sent to client by state.
     */
    public void load(PageEditorParameters parameters) {
        getState().parameters = parameters;
    }

    /**
     * Silently update the parameters in the state.
     */
    public void update(PageEditorParameters parameters) {
        getState(false).parameters = parameters;
    }

    @Override
    protected PageEditorState getState() {
        return (PageEditorState) super.getState();
    }

    @Override
    protected PageEditorState getState(boolean markAsDirty) {
        return (PageEditorState) super.getState(markAsDirty);
    }

    public void refresh() {
        getRpcProxy(PageEditorClientRpc.class).refresh();
    }

    public void startMoveComponent() {
        getRpcProxy(PageEditorClientRpc.class).startMoveComponent();
    }

    public void cancelMoveComponent() {
        getRpcProxy(PageEditorClientRpc.class).cancelMoveComponent();
    }

    public void setListener(PageEditorListener listener) {
        this.listener = listener;
    }
}
