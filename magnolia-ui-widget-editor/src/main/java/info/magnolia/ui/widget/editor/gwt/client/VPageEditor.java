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
package info.magnolia.ui.widget.editor.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Composite;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;
import info.magnolia.ui.widget.editor.gwt.client.dom.Comment;
import info.magnolia.ui.widget.editor.gwt.client.dom.MgnlElement;
import info.magnolia.ui.widget.editor.gwt.client.dom.processor.AbstractMgnlElementProcessor;
import info.magnolia.ui.widget.editor.gwt.client.dom.processor.CommentProcessor;
import info.magnolia.ui.widget.editor.gwt.client.dom.processor.ElementProcessor;
import info.magnolia.ui.widget.editor.gwt.client.dom.processor.MgnlElementProcessorFactory;
import info.magnolia.ui.widget.editor.gwt.client.event.DeleteComponentEvent;
import info.magnolia.ui.widget.editor.gwt.client.event.DeleteComponentEventHandler;
import info.magnolia.ui.widget.editor.gwt.client.event.EditComponentEvent;
import info.magnolia.ui.widget.editor.gwt.client.event.EditComponentEventHandler;
import info.magnolia.ui.widget.editor.gwt.client.event.NewAreaEvent;
import info.magnolia.ui.widget.editor.gwt.client.event.NewAreaEventHandler;
import info.magnolia.ui.widget.editor.gwt.client.event.NewComponentEvent;
import info.magnolia.ui.widget.editor.gwt.client.event.NewComponentEventHandler;
import info.magnolia.ui.widget.editor.gwt.client.event.SelectElementEvent;
import info.magnolia.ui.widget.editor.gwt.client.event.SelectElementEventHandler;
import info.magnolia.ui.widget.editor.gwt.client.event.SortComponentEvent;
import info.magnolia.ui.widget.editor.gwt.client.event.SortComponentEventHandler;
import info.magnolia.ui.widget.editor.gwt.client.jsni.event.FrameLoadedEvent;
import info.magnolia.ui.widget.editor.gwt.client.model.Model;
import info.magnolia.ui.widget.editor.gwt.client.model.ModelImpl;
import info.magnolia.ui.widget.editor.gwt.client.model.focus.FocusModel;
import info.magnolia.ui.widget.editor.gwt.client.model.focus.FocusModelImpl;
import info.magnolia.ui.widget.editor.gwt.client.widget.controlbar.PageBar;
import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import java.util.List;


/**
 * Vaadin implementation of PageEditor client side.
 */
public class VPageEditor extends Composite implements VPageEditorView.Listener, Paintable, ClientSideHandler {


    private static final String PAGE_EDITOR_CSS = "/VAADIN/themes/admincentraltheme/pageeditor.css";


    private final VPageEditorView view;
    private final Model model;
    private final EventBus eventBus;
    private final FocusModel focusModel;

    protected ApplicationConnection client;
    private ClientSideProxy proxy;
    private String paintableId;

    private VPageEditorParameters pageEditorParameters;

   
    public VPageEditor() {
        this.eventBus = new SimpleEventBus();

        this.view = new VPageEditorViewImpl(eventBus);
        this.model = new ModelImpl();
        this.focusModel = new FocusModelImpl(eventBus, model);


        initWidget(view.asWidget());

        view.setListener(this);

        registerDomEventHandlers();
        registerEditorEventHandlers();


        
        proxy = new ClientSideProxy(this) {
            {
                register("refresh", new Method() {
                    @Override
                    public void invoke(String methodName, Object[] params) {
                        view.reload();
                    }
                });

                register("load", new Method() {
                    @Override
                    public void invoke(String methodName, Object[] params) {
                        String json = String.valueOf(params[0]);
                        pageEditorParameters = VPageEditorParameters.parse(json);
                        view.setUrl(pageEditorParameters.getContextPath() + pageEditorParameters.getNodePath());
                    }
                });
            }
        };

    }

    @Override
    public boolean initWidget(Object[] objects) {
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        VConsole.error("Unhandled RPC call from server: " + method);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        this.paintableId = uidl.getId();
        if (client.updateComponent(this, uidl, true)) {
            return;
        }

        proxy.update(this, uidl, client);
    }

    private void registerDomEventHandlers() {

        eventBus.addHandler(FrameLoadedEvent.TYPE, new FrameLoadedEvent.Handler() {

            @Override
            public void handle(FrameLoadedEvent event) {
                if (pageEditorParameters.isPreview()) {
                    return;
                }

                view.initSelectionListener();

                Document document = event.getFrameDocument();
                process(event.getFrameDocument());

                if (model.getRootPage().getControlBar() != null) {
                    ((PageBar)model.getRootPage().getControlBar()).setPageTitle(document.getTitle());
                }
                focusModel.init();
            }

        });
    }
    private void registerEditorEventHandlers() {

        eventBus.addHandler(SelectElementEvent.TYPE, new SelectElementEventHandler() {

            @Override
            public void onSelectElement(SelectElementEvent selectElementEvent) {
                proxy.call("selectElement", selectElementEvent.getType(), selectElementEvent.getJson());
            }

        });

        eventBus.addHandler(NewAreaEvent.TYPE, new NewAreaEventHandler() {
            @Override
            public void onNewArea(NewAreaEvent newAreaEvent) {
                proxy.call("newArea", newAreaEvent.getWorkSpace(), newAreaEvent.getNodeType(), newAreaEvent.getPath());
            }
        });

        eventBus.addHandler(NewComponentEvent.TYPE, new NewComponentEventHandler() {
            @Override
            public void onNewComponent(NewComponentEvent newComponentEvent) {
                proxy.call("newComponent", newComponentEvent.getWorkSpace(), newComponentEvent.getPath(), newComponentEvent.getAvailableComponents());
            }
        });

        eventBus.addHandler(EditComponentEvent.TYPE, new EditComponentEventHandler() {
            @Override
            public void onEditComponent(EditComponentEvent editComponentEvent) {
                proxy.call("editComponent", editComponentEvent.getWorkspace(), editComponentEvent.getPath(), editComponentEvent.getDialog());
            }
        });

        eventBus.addHandler(DeleteComponentEvent.TYPE, new DeleteComponentEventHandler() {
            @Override
            public void onDeleteComponent(DeleteComponentEvent deleteComponentEvent) {
                proxy.call("deleteComponent", deleteComponentEvent.getWorkspace(), deleteComponentEvent.getPath());
            }
        });
        eventBus.addHandler(SortComponentEvent.TYPE, new SortComponentEventHandler() {
            @Override
            public void onSortComponent(SortComponentEvent sortComponentEvent) {
                proxy.call("sortComponent", sortComponentEvent.getWorkspace(), sortComponentEvent.getParentPath(), sortComponentEvent.getSourcePath(), sortComponentEvent.getTargetPath(), sortComponentEvent.getOrder());
            }
        });
    }

    public Model getModel() {
        return model;
    }

    @Override
    public void selectElement(final Element element) {
        focusModel.selectElement(element);
    }

    private void injectEditorStyles(final Document document) {
        HeadElement head = HeadElement.as(document.getElementsByTagName("head").getItem(0));
        LinkElement cssLink = document.createLinkElement();
        cssLink.setType("text/css");
        cssLink.setRel("stylesheet");
        cssLink.setHref(pageEditorParameters.getContextPath() + PAGE_EDITOR_CSS);
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
        if (mgnlElement == null && model.getRootPage() != null) {
            mgnlElement = model.getRootPage();
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            Node childNode = node.getChild(i);
            if (childNode.getNodeType() == Comment.COMMENT_NODE) {

                try {
                    mgnlElement = CommentProcessor.process(model, childNode, mgnlElement);
                } catch (IllegalArgumentException e) {
                    GWT.log("Not CMSComment element, skipping: " + e.toString());
                } catch (Exception e) {
                    GWT.log("Caught undefined exception: " + e.toString());
                }
            } else if (childNode.getNodeType() == Node.ELEMENT_NODE && mgnlElement != null && !mgnlElement.isPage()) {
                ElementProcessor.process(model, childNode, mgnlElement);
            }

            processDocument(childNode, mgnlElement);
        }

    }

/*    private void processMgnlElements() {
        List<MgnlElement> rootElements = new LinkedList<MgnlElement>(getModel().getRootAreas());
        for (MgnlElement root : rootElements) {
            LinkedList<MgnlElement> elements = new LinkedList<MgnlElement>();
            elements.add(root);
            elements.addAll(root.getDescendants());

            for (MgnlElement mgnlElement : elements) {
                try {
                    AbstractMgnlElementProcessor processor = MgnlElementProcessorFactory.getProcessor(model, eventBus, mgnlElement);
                    processor.process();
                } catch (IllegalArgumentException e) {
                    GWT.log("MgnlFactory could not instantiate class. The element is neither an area nor component.");
                }
            }
        }

    }*/

    private void processMgnlElements() {
        MgnlElement root = model.getRootPage();
        List<MgnlElement> elements = root.getDescendants();
        elements.add(root);
        for (MgnlElement element : elements) {

            try {
                AbstractMgnlElementProcessor processor = MgnlElementProcessorFactory.getProcessor(model, eventBus, element);
                processor.process();
            } catch (IllegalArgumentException e) {
                GWT.log("MgnlFactory could not instantiate class. The element is neither an area nor component.");
            }
        }
    }

}
