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
package info.magnolia.ui.vaadin.gwt.client.editorlike.connector;

import info.magnolia.ui.vaadin.gwt.client.editorlike.widget.EditorLikeView;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractSingleComponentContainerConnector;

/**
 * EditorLikeComponentConnector.
 * @param <U> 
 * @param <T> 
 */
public abstract class EditorLikeComponentConnector
    <U extends EditorLikeView.Presenter, 
     T extends EditorLikeView<U>>
    extends AbstractSingleComponentContainerConnector {

    private T view;
    
    private U presenter;
    
    @Override
    protected void init() {
        super.init();
        addStateChangeHandler("caption", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                view.setCaption(getState().caption);
            }
        });
        
        addStateChangeHandler("dialogDescription", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                view.setCaption(getState().componentDescription);
            }
        });
        
        addStateChangeHandler("actions", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                updateActionsFromState();
            }
        });
    }
    
    @Override
    protected EditorLikeComponentState createState() {
        return new EditorLikeComponentState();
    }
    
    @Override
    public EditorLikeComponentState getState() {
        return (EditorLikeComponentState)super.getState();
    }
    
    @Override
    public void updateCaption(ComponentConnector connector) {
        view.setCaption(connector.getState().caption);
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        updateContent();
    }
    
    protected void updateActionsFromState() {
        view.setActions(getState().actions);
    }

    protected void updateContent() {
        final ComponentConnector content = getContent();
        if (content != null) {
            this.view.setContent(content.getWidget());
        }
    }

    @Override
    protected Widget createWidget() {
        this.view = createView();
        this.presenter = createPresenter();
        this.view.setPresenter(presenter);
        return view.asWidget();
    }
    
    protected abstract T createView();
    
    protected abstract U createPresenter();
    
    protected T getView() {
        return view;
    }
    
    protected U getPresenter() {
        return presenter;
    }
}
