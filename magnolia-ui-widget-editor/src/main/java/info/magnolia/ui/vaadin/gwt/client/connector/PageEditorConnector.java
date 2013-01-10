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
package info.magnolia.ui.vaadin.gwt.client.connector;

import info.magnolia.ui.vaadin.editor.PageEditor;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.CmsNode;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.Comment;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.AbstractMgnlElementProcessor;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.CommentProcessor;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.ElementProcessor;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.MgnlElementProcessorFactory;
import info.magnolia.ui.vaadin.gwt.client.editor.event.DeleteComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.DeleteComponentEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.event.EditComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.EditComponentEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.event.NewAreaEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.NewAreaEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.event.NewComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.NewComponentEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SelectElementEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SelectElementEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SortComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SortComponentEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.jsni.event.FrameLoadedEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.model.Model;
import info.magnolia.ui.vaadin.gwt.client.editor.model.ModelImpl;
import info.magnolia.ui.vaadin.gwt.client.editor.model.focus.FocusModel;
import info.magnolia.ui.vaadin.gwt.client.editor.model.focus.FocusModelImpl;
import info.magnolia.ui.vaadin.gwt.client.rpc.PageEditorClientRpc;
import info.magnolia.ui.vaadin.gwt.client.rpc.PageEditorServerRpc;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;
import info.magnolia.ui.vaadin.gwt.client.widget.PageEditorView;
import info.magnolia.ui.vaadin.gwt.client.widget.PageEditorViewImpl;
import info.magnolia.ui.vaadin.gwt.client.widget.controlbar.PageBar;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

/**
 * PageEditorConnector.
 */
@Connect(PageEditor.class)
public class PageEditorConnector extends AbstractComponentConnector implements PageEditorView.Listener {

    private static final String PAGE_EDITOR_CSS = "/VAADIN/themes/admincentraltheme/pageeditor.css";

    private PageEditorServerRpc rpc = RpcProxy.create(PageEditorServerRpc.class, this);

    private EventBus eventBus = new SimpleEventBus();

    private PageEditorView view;

    private Model model;

    private FocusModel focusModel;

    @Override
    protected void init() {
        super.init();
        this.model = new ModelImpl();
        this.focusModel = new FocusModelImpl(eventBus, model);
        addStateChangeHandler(new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                PageEditorParameters params = getState().parameters;
                view.setUrl(params.getContextPath() + params.getNodePath());
            }
        });

        registerRpc(PageEditorClientRpc.class, new PageEditorClientRpc() {
            @Override
            public void refresh() {
                view.reload();
            }
        });

        eventBus.addHandler(FrameLoadedEvent.TYPE, new FrameLoadedEvent.Handler() {
            @Override
            public void handle(FrameLoadedEvent event) {
                model.reset();
                if (!getState().parameters.isPreview()) {
                    view.initSelectionListener();
                    Document document = event.getFrameDocument();
                    process(event.getFrameDocument());
                    if (model.getRootPage().getControlBar() != null) {
                        ((PageBar) model.getRootPage().getControlBar()).setPageTitle(document.getTitle());
                    }
                    focusModel.init();
                }
            }
        });

        eventBus.addHandler(SelectElementEvent.TYPE, new SelectElementEventHandler() {
            @Override
            public void onSelectElement(SelectElementEvent selectElementEvent) {
                rpc.selectElement(selectElementEvent.getType(), selectElementEvent.getAttributes());
            }
        });

        eventBus.addHandler(NewAreaEvent.TYPE, new NewAreaEventHandler() {
            @Override
            public void onNewArea(NewAreaEvent newAreaEvent) {
                rpc.newArea(newAreaEvent.getWorkSpace(), newAreaEvent.getNodeType(), newAreaEvent.getPath());
            }
        });

        eventBus.addHandler(NewComponentEvent.TYPE, new NewComponentEventHandler() {
            @Override
            public void onNewComponent(NewComponentEvent newComponentEvent) {
                rpc.newComponent(newComponentEvent.getWorkSpace(), newComponentEvent.getPath(), newComponentEvent.getAvailableComponents());
            }
        });

        eventBus.addHandler(EditComponentEvent.TYPE, new EditComponentEventHandler() {
            @Override
            public void onEditComponent(EditComponentEvent editComponentEvent) {
                rpc.editComponent(editComponentEvent.getWorkspace(), editComponentEvent.getPath(), editComponentEvent.getDialog());
            }
        });

        eventBus.addHandler(DeleteComponentEvent.TYPE, new DeleteComponentEventHandler() {
            @Override
            public void onDeleteComponent(DeleteComponentEvent deleteComponentEvent) {
                rpc.deleteComponent(deleteComponentEvent.getWorkspace(), deleteComponentEvent.getPath());
            }
        });
        eventBus.addHandler(SortComponentEvent.TYPE, new SortComponentEventHandler() {
            @Override
            public void onSortComponent(SortComponentEvent sortComponentEvent) {
                rpc.sortComponent(sortComponentEvent.getWorkspace(), sortComponentEvent.getParentPath(), sortComponentEvent.getSourcePath(), sortComponentEvent.getTargetPath(), sortComponentEvent.getOrder());
            }
        });
    }

    @Override
    protected Widget createWidget() {
        this.view = new PageEditorViewImpl(eventBus);
        this.view.setListener(this);
        return view.asWidget();
    }

    @Override
    public PageEditorState getState() {
        return (PageEditorState) super.getState();
    }

    @Override
    public void selectElement(Element element) {
        focusModel.selectElement(element);
    }

    private void injectEditorStyles(final Document document) {
        HeadElement head = HeadElement.as(document.getElementsByTagName("head").getItem(0));
        LinkElement cssLink = document.createLinkElement();
        cssLink.setType("text/css");
        cssLink.setRel("stylesheet");
        cssLink.setHref(getState().parameters.getContextPath() + PAGE_EDITOR_CSS);
        head.insertFirst(cssLink);
    }

    private void process(final Document document) {
        injectEditorStyles(document);
        long startTime = System.currentTimeMillis();
        processDocument(document.getDocumentElement(), null);
        processMgnlElements();
        GWT.log("Time spent to process cms comments: " + (System.currentTimeMillis() - startTime) + "ms");
    }

    private void processDocument(Node node, MgnlElement mgnlElement) {
        boolean proceed = true;
        if (mgnlElement == null && model.getRootPage() != null) {
            mgnlElement = model.getRootPage();
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            Node childNode = node.getChild(i);
            if (childNode.getNodeType() == Comment.COMMENT_NODE) {
                try {
                    CommentProcessor processor = new CommentProcessor();
                    mgnlElement = processor.process(model, childNode, mgnlElement);
                } catch (IllegalArgumentException e) {
                    GWT.log("Not CMSComment element, skipping: " + e.toString());
                } catch (Exception e) {
                    GWT.log("Caught undefined exception: " + e.toString());
                }
            } else if (childNode.getNodeType() == Node.ELEMENT_NODE && mgnlElement != null && !mgnlElement.isPage()) {
                proceed = ElementProcessor.process(model, childNode, mgnlElement);
            }
            if (proceed) {
                processDocument(childNode, mgnlElement);
            }
        }

    }

    private void processMgnlElements() {
        CmsNode root = model.getRootPage();
        List<CmsNode> elements = root.getDescendants();
        elements.add(root);
        for (CmsNode element : elements) {
            try {
                AbstractMgnlElementProcessor processor = MgnlElementProcessorFactory.getProcessor(model, eventBus, element.asMgnlElement());
                processor.process();
            } catch (IllegalArgumentException e) {
                GWT.log("MgnlFactory could not instantiate class. The element is neither an area nor component.");
            }
        }
    }

}
