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
package info.magnolia.ui.widget.editor;


import info.magnolia.ui.widget.editor.gwt.client.VPageEditor;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;

/**
 * PageEditor widget server side implementation.
 */
@SuppressWarnings("serial")
@ClientWidget(value=VPageEditor.class)
public class PageEditor extends AbstractComponent implements PageEditorView, ServerSideHandler {

    private Presenter presenter;

    /**
     * Source of the embedded object.
     */
    private Resource source;

    public PageEditor(final Resource source) {
        this.source = source;
        setCaption("");
    }


    protected ServerSideProxy proxy = new ServerSideProxy(this) {
        {
            register("fireAction", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final String actionName = String.valueOf(params[0]);
                    presenter.executeAction(actionName);
                }
            });
        }
    };

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public Object[] initRequestFromClient() {
        return new Object[] {};
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        if (getSource() != null) {
            target.addAttribute("src", getSource());
        }
    }

    protected Resource getSource() {
        return source;
    }

}
