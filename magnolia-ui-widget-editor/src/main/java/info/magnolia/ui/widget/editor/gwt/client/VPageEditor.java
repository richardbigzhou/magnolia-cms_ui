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

import info.magnolia.ui.widget.editor.gwt.client.dom.Comment;
import info.magnolia.ui.widget.editor.gwt.client.dom.MgnlElement;
import info.magnolia.ui.widget.editor.gwt.client.dom.processor.CommentProcessor;
import info.magnolia.ui.widget.editor.gwt.client.dom.processor.ElementProcessor;
import info.magnolia.ui.widget.editor.gwt.client.dom.processor.MgnlElementProcessor;
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
import info.magnolia.ui.widget.editor.gwt.client.jsni.JavascriptUtils;
import info.magnolia.ui.widget.editor.gwt.client.model.Model;
import info.magnolia.ui.widget.editor.gwt.client.model.ModelImpl;
import info.magnolia.ui.widget.editor.gwt.client.model.focus.FocusModel;
import info.magnolia.ui.widget.editor.gwt.client.model.focus.FocusModelImpl;

import java.util.LinkedList;
import java.util.List;

import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Frame;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;


/**
 * Vaadin implementation of PageEditor client side. TODO fgrilli: this class badly needs clean up
 * and refactoring.
 */
public class VPageEditor extends Composite implements VPageEditorView.Listener, Paintable, ClientSideHandler {

    // In case we're in preview mode, we will stop processing the document, after the pagebar has
    // been injected.
    private static boolean keepProcessing = true;

    private static String locale;

    private final Model model;

    private final EventBus eventBus;

    private final ClientSideProxy proxy;

    private static VPageEditorView view;

    protected ApplicationConnection client;

    protected String paintableId;

    private final FocusModel focusModel;

    private String contextPath;

    private String pagePath;

    private boolean preview = false;

    public VPageEditor() {
        this.eventBus = new SimpleEventBus();
        this.view = new VPageEditorViewImpl(eventBus);
        this.model = new ModelImpl();
        this.focusModel = new FocusModelImpl(eventBus, model);

        view.setListener(this);
        registerEventHandlers();

        initWidget(view.asWidget());

        proxy = new ClientSideProxy(this) {

            {
                register("refresh", new Method() {

                    @Override
                    public void invoke(String methodName, Object[] params) {
                        reloadIFrame(view.getIframe().getElement());
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
        getInitParameters(uidl);

        view.getIframe().getElement().setId(paintableId);
        view.getIframe().setUrl(getContextPath() + getPagePath());

        proxy.update(this, uidl, client);
    }

    public Model getModel() {
        return model;
    }

    /**
     * Helper to return the contextPath sent from server.
     */
    private void getInitParameters(UIDL uidl) {

        String contextPath = uidl.getStringAttribute("contextPath");
        if (contextPath == null) {
            contextPath = "";
        }
        String pagePath = uidl.getStringAttribute("nodePath");
        if (pagePath == null) {
            pagePath = "";
        }

        this.contextPath = contextPath;
        this.pagePath = pagePath;
        this.preview = uidl.getBooleanAttribute("preview");

    }

    public void onMouseUp(final Element element) {
        focusModel.onMouseUp(element);
    }

    private native void initNativeHandlers(Element element)
    /*-{
        if (element != 'undefined') {
            var ref = this;
            element.contentDocument.onmouseup = function(event) {
                ref.@info.magnolia.ui.widget.editor.gwt.client.VPageEditor::onMouseUp(Lcom/google/gwt/dom/client/Element;)(event.target);
                event.stopPropagation();
            }
        }
    }-*/;

    private void registerEventHandlers() {

        eventBus.addHandler(SelectElementEvent.TYPE, new SelectElementEventHandler() {

            @Override
            public void onSelectElement(SelectElementEvent selectElementEvent) {
                proxy.call("selectElement", selectElementEvent.getWorkspace(), selectElementEvent.getPath(), selectElementEvent.getDialog());
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
                proxy.call(
                    "sortComponent",
                    sortComponentEvent.getWorkspace(),
                    sortComponentEvent.getParentPath(),
                    sortComponentEvent.getSourcePath(),
                    sortComponentEvent.getTargetPath(),
                    sortComponentEvent.getOrder());
            }
        });
    }

    private void injectEditorStyles(final Document document) {
        HeadElement head = HeadElement.as(document.getElementsByTagName("head").getItem(0));
        LinkElement cssLink = document.createLinkElement();
        cssLink.setType("text/css");
        cssLink.setRel("stylesheet");
        cssLink.setHref(contextPath + "/VAADIN/themes/admincentraltheme/pageeditor.css");
        head.insertFirst(cssLink);
    }

    private native void onPageEditorReady()
    /*-{
        var callbacks = $wnd.mgnl.PageEditor.onPageEditorReadyCallbacks
        if(typeof callbacks != 'undefined') {
            for(var i=0; i < callbacks.length; i++) {
                callbacks[i].apply();
            }
        }
    }-*/;

    protected native void reloadIFrame(Element iframeElement)
    /*-{
        iframeElement.contentWindow.location.reload(true);
    }-*/;

    // FIXME submitting forms still renders website channel and edit bars
    private void postProcessLinksOnMobilePreview(Element root, String channel) {
        NodeList<Element> anchors = root.getElementsByTagName("a");

        final String mobilePreviewParams = "";// MGNL_CHANNEL_PARAMETER+"="+channel+"&"+
                                              // MGNL_PREVIEW_PARAMETER+"=true";

        for (int i = 0; i < anchors.getLength(); i++) {
            AnchorElement anchor = AnchorElement.as(anchors.getItem(i));

            GWT.log("Starting to process link " + anchor.getHref());

            if (JavascriptUtils.isEmpty(anchor.getHref())) {
                continue;
            }
            String manipulatedHref = anchor.getHref().replaceFirst(Window.Location.getProtocol() + "//" + Window.Location.getHost(), "");
            String queryString = Window.Location.getQueryString() != null ? Window.Location.getQueryString() : "";

            GWT.log("query string is " + queryString);

            String queryStringRegex = queryString.replaceFirst("\\?", "\\\\?");
            manipulatedHref = manipulatedHref.replaceFirst(queryStringRegex, "");
            int indexOfHash = manipulatedHref.indexOf("#");

            if (indexOfHash != -1) {
                manipulatedHref = manipulatedHref.substring(indexOfHash);
            } else {
                if (!queryString.contains(mobilePreviewParams)) {
                    if (queryString.startsWith("?")) {
                        queryString += "&" + mobilePreviewParams;
                    } else {
                        queryString = "?" + mobilePreviewParams;
                    }
                }
                manipulatedHref += queryString;
            }
            GWT.log("Resulting link is " + manipulatedHref);
            anchor.setHref(manipulatedHref);
        }
        NodeList<Element> forms = root.getElementsByTagName("form");

        for (int i = 0; i < forms.getLength(); i++) {
            FormElement form = FormElement.as(forms.getItem(i));
            form.setAction(form.getAction().concat("?" + mobilePreviewParams));
        }
    }

    private void process(final Document document) {

        injectEditorStyles(document);

        long startTime = System.currentTimeMillis();
        processDocument(document.getDocumentElement(), null);
        processMgnlElements();

        GWT.log("Time spent to process cms comments: " + (System.currentTimeMillis() - startTime) + "ms");

    }

    private void processDocument(Node node, MgnlElement mgnlElement) {
        if (keepProcessing) {
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
                } else if (childNode.getNodeType() == Node.ELEMENT_NODE && mgnlElement != null) {
                    ElementProcessor.process(model, childNode, mgnlElement);
                }

                processDocument(childNode, mgnlElement);
            }
        }
    }

    private void processMgnlElements() {
        List<MgnlElement> rootElements = new LinkedList<MgnlElement>(getModel().getRootElements());
        for (MgnlElement root : rootElements) {
            LinkedList<MgnlElement> elements = new LinkedList<MgnlElement>();
            elements.add(root);
            elements.addAll(root.getDescendants());

            for (MgnlElement mgnlElement : elements) {
                try {
                    MgnlElementProcessor processor = MgnlElementProcessorFactory.getProcessor(model, eventBus, mgnlElement);
                    processor.process();
                } catch (IllegalArgumentException e) {
                    GWT.log("MgnlFactory could not instantiate class. The element is neither an area nor component.");
                }
            }
        }

    }

    @Override
    public void onFrameLoaded(Frame iframe) {
        if (preview) {
            return;
        }
        Element element = iframe.getElement();
        initNativeHandlers(element);

        IFrameElement frameElement = IFrameElement.as(element);
        Document contentDocument = frameElement.getContentDocument();
        process(contentDocument);
        focusModel.toggleRootAreaBar(true);

    }

    public String getPagePath() {
        return pagePath;
    }

    public String getContextPath() {
        return contextPath;
    }
}
