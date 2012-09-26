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
package info.magnolia.ui.widget.dialog;

import info.magnolia.ui.widget.dialog.gwt.client.dialoglayout.VBaseDialog;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * BaseDialog.
 */
@ClientWidget(value = VBaseDialog.class, loadStyle = LoadStyle.EAGER)
public class BaseDialog extends AbstractComponent implements ServerSideHandler, AbstractDialog {

    /**
     * Base interface for an MagnoliaDialogView listener.
     */
    public interface Listener {
        /**
         * Execute a specific action {@link info.magnolia.ui.model.action.Action}.
         */
        void executeAction(String actionName);

        /**
         * Close current Dialog.
         */
        void closeDialog();
    }
    
    private Component content; 
    
    private Listener listener;
    
    private ServerSideProxy proxy = new ServerSideProxy(this) {{
        register("fireAction", new Method() {

            @Override
            public void invoke(String methodName, Object[] params) {
                final String actionName = String.valueOf(params[0]);
                listener.executeAction(actionName);
            }
        });
        register("closeDialog", new Method() {

            @Override
            public void invoke(String methodName, Object[] params) {
                listener.closeDialog();
            }
        }); 
    }};
    
    private Map<String, String> actionMap = new HashMap<String, String>();
    
    private boolean isAttached = false;

    private String description;
    
    public BaseDialog() {
        setImmediate(true);
        setContent(createDefaultContent());
    }

    
    public void setListener(Listener listener) {
        this.listener = listener;
    }
    
    public void setContent(Component content) {
        final Component actualContent = content == null ? createDefaultContent() : content;
        if (actualContent != this.content) {
            if (this.content != null) {
                this.content.setParent(null);    
            }
            this.content = actualContent;
            actualContent.setParent(this);
        }
    }
    
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        content.paint(target);
        proxy.paintContent(target);
    }
    
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        proxy.changeVariables(source, variables);
    }
    
    public Component getContent() {
        return content;
    }
    
    @Override
    public void setCaption(String caption) {
        content.setCaption(caption);
    }
    
    @Override
    public void attach() {
        this.isAttached = true;
        super.attach();
        content.attach();
    }
    
    @Override
    public void detach() {
        this.isAttached = false;
        super.detach();
        content.detach();
    }
    
    @Override
    public void addAction(String actionName, String actionLabel) {
        actionMap.put(actionName, actionLabel);
        if (isAttached) {
            proxy.call("addAction", actionName, actionLabel);   
        }
    }
    
    @Override
    public void setDialogDescription(String description) {
        this.description = description;
        if (isAttached) {
            proxy.call("setDescription", description);   
        }
    }
    
    protected Component createDefaultContent() {
        return new VerticalLayout();
    }

    @Override
    public Object[] initRequestFromClient() {
        if (description != null) {
            proxy.call("setDescription", description);
        }
        for (final Map.Entry<String, String> entry : actionMap.entrySet()) {
            proxy.call("addAction", entry.getKey(), entry.getValue());   
        }
        return new Object[] {};
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        throw new RuntimeException("Unknown call from client " + method);
    }

}
