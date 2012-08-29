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

import com.google.gson.Gson;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;
import info.magnolia.ui.widget.editor.gwt.client.VPageEditor;
import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import java.io.Serializable;
import java.util.Map;

/**
 * PageEditor widget server side implementation.
 */
@SuppressWarnings("serial")
@ClientWidget(value = VPageEditor.class, loadStyle = ClientWidget.LoadStyle.EAGER)
public class PageEditor extends AbstractComponent implements PageEditorView, ServerSideHandler {

    /**
     * Source of the embedded object.
     */

    private boolean preview = false;

    private PageEditorView.Listener listener;

    protected ServerSideProxy proxy;
    private String PAGE_ELEMENT = "cms:page";
    private String AREA_ELEMENT = "cms:area";
    private String COMPONENT_ELEMENT = "cms:component";
    private PageEditorParameters parameters;

    public PageEditor() {
        setSizeFull();
        setImmediate(true);
    }

    @Override
    public void setListener(PageEditorView.Listener listener) {
        this.listener = listener;
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
    public void load(PageEditorParameters parameters) {
        this.parameters = parameters;
        Gson gson = new Gson();
        String json = gson.toJson(parameters);
        proxy.call("load", json);
        requestRepaint();
    }

    @Override
    public void init() {

        proxy = new ServerSideProxy(this) {

            {
                register("selectElement", new Method() {

                    @Override
                    public void invoke(String methodName, Object[] params) {
                        final String type = String.valueOf(params[0]);
                        final String json = String.valueOf(params[1]);

                        AbstractElement element = resolveElement(type, json);

                        listener.selectElement(element);
                    }
                });
                register("editComponent", new Method() {

                    @Override
                    public void invoke(String methodName, Object[] params) {
                        final String workspace = String.valueOf(params[0]);
                        final String path = String.valueOf(params[1]);
                        final String dialog = String.valueOf(params[2]);
                        listener.editComponent(workspace, path, dialog);
                    }
                });
                register("newArea", new Method() {

                    @Override
                    public void invoke(String methodName, Object[] params) {
                        final String workspace = String.valueOf(params[0]);
                        final String nodeType = String.valueOf(params[1]);
                        final String path = String.valueOf(params[2]);
                        listener.newArea(workspace, nodeType, path);
                    }

                });
                register("newComponent", new Method() {

                    @Override
                    public void invoke(String methodName, Object[] params) {
                        final String workspace = String.valueOf(params[0]);
                        final String path = String.valueOf(params[1]);
                        final String availableComponents = String.valueOf(params[2]);
                        listener.newComponent(workspace, path, availableComponents);
                    }

                });
                register("deleteComponent", new Method() {

                    @Override
                    public void invoke(String methodName, Object[] params) {
                        final String workspace = String.valueOf(params[0]);
                        final String path = String.valueOf(params[1]);
                        listener.deleteComponent(workspace, path);
                    }
                });
                register("sortComponent", new Method() {

                    @Override
                    public void invoke(String methodName, Object[] params) {
                        final String workspace = String.valueOf(params[0]);
                        final String parentPath = String.valueOf(params[1]);
                        final String source = String.valueOf(params[2]);
                        final String target = String.valueOf(params[3]);
                        final String order = String.valueOf(params[4]);
                        listener.sortComponent(workspace, parentPath, source, target, order);
                    }
                });
            }
        };

    }

    private AbstractElement resolveElement(String type, String json) {
        AbstractElement element = null;
        Gson gson = new Gson();
        if (type.equals(PAGE_ELEMENT)) {
            element = gson.fromJson(String.valueOf(json), PageElement.class);
        }
        else if (type.equals(AREA_ELEMENT)) {
            element = gson.fromJson(String.valueOf(json), AreaElement.class);
        }
        else if (type.equals(COMPONENT_ELEMENT)) {
            element = gson.fromJson(String.valueOf(json), ComponentElement.class);
        }
        return element;
    }

    @Override
    public void refresh() {
        proxy.call("refresh");
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public Object[] initRequestFromClient() {
        return new Object[]{};

    }

    @Override
    public void callFromClient(String method, Object[] params) {
        System.out.println("Client called " + method);
    }

    /**
     * Class for GSON serialization of area elements.
     */
    public static class AreaElement extends AbstractElement {

        private String availableComponents;

        public AreaElement(String workspace, String path, String dialog, String availableComponents) {
            super(workspace, path, dialog);
            this.availableComponents = availableComponents;
        }

        public String getAvailableComponents() {
            return availableComponents;
        }
    }

    /**
     * Class for GSON serialization of area elements.
     */
    public static class ComponentElement extends AbstractElement {

        public ComponentElement(String workspace, String path, String dialog) {
            super(workspace, path, dialog);
        }

    }

    /**
     * Class for GSON serialization of area elements.
     */
    public static class PageElement extends AbstractElement {

        public PageElement(String workspace, String path, String dialog) {
            super(workspace, path, dialog);
        }

    }

    /**
     *  AbstractElement.
     */
    public static abstract class AbstractElement implements Serializable {

        private String workspace;

        public void setWorkspace(String workspace) {
            this.workspace = workspace;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setDialog(String dialog) {
            this.dialog = dialog;
        }

        private String path;
        private String dialog;

        public AbstractElement(String workspace, String path, String dialog) {
            this.workspace = workspace;
            this.path = path;
            this.dialog = dialog;
        }

        public String getWorkspace() {
            return workspace;
        }

        public String getPath() {
            return path;
        }

        public String getDialog() {
            return dialog;
        }
    }
}
