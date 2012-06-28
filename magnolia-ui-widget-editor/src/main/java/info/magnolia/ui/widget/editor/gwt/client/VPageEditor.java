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
import info.magnolia.ui.widget.editor.gwt.client.jsni.JavascriptUtils;
import info.magnolia.ui.widget.editor.gwt.client.model.ModelStorage;
import info.magnolia.ui.widget.editor.gwt.client.widget.dnd.LegacyDragAndDrop;

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
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.Window.ScrollHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;


/**
 * Vaadin implementation of PageEditor client side (Presenter).
 * TODO fgrilli: this class badly needs clean up and refactoring.
 */
@SuppressWarnings("serial")
public class VPageEditor extends FlowPanel implements Paintable, VPageEditorView.Presenter, ClientSideHandler {

    protected String paintableId;

    protected ApplicationConnection client;

    private final VPageEditorView view;

    private static String locale;
    private final static ModelStorage model = ModelStorage.getInstance();

    private LinkedList<MgnlElement> mgnlElements = new LinkedList<MgnlElement>();

    // In case we're in preview mode, we will stop processing the document, after the pagebar has been injected.
    private static boolean keepProcessing = true;
    private static boolean isPreview = false;

    private final EventBus eventBus;

    private Frame iframe = new Frame();

    private Document contentDocument;

    private ClientSideProxy proxy = new ClientSideProxy(this) {
        {
            register("addAction", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {

                }
            });
        }
    };

    public VPageEditor() {
        eventBus = new SimpleEventBus();
        this.view = new VPageEditorViewImpl(eventBus);
        view.setPresenter(this);
        iframe.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
                IFrameElement frameElement = IFrameElement.as(iframe.getElement());
                contentDocument = frameElement.getContentDocument();
                //other handlers are initialized here b/c we need to know the document inside the iframe.
                initHandlers();
                //make sure we process  html only when the document inside the iframe is loaded.
                process(contentDocument);
            }
        });

        final Element iframeElement = iframe.getElement();
        iframeElement.setAttribute("width", "100%");
        iframeElement.setAttribute("height", "100%");
        iframeElement.setAttribute("allowTransparency", "true");
        iframeElement.setAttribute("frameborder", "0");
        add(iframe);

    }

    private void initHandlers() {
        iframe.addDomHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {

                model.getFocusModel().onMouseUp((Element)event.getNativeEvent().getEventTarget().cast());
                event.stopPropagation();
            }
        }, MouseUpEvent.getType());

        iframe.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                    //if we're moving an element abort move
                    if(LegacyDragAndDrop.isMoving()) {
                        LegacyDragAndDrop.moveComponentReset();
                    } else {
                        //VPageEditor.enablePreview(!isPreview);
                    }
                    event.preventDefault();
                }
            }
        }, KeyDownEvent.getType());



        iframe.addDomHandler(new MouseMoveHandler() {

            @Override
            public void onMouseMove(MouseMoveEvent event) {

                Element moveElement = contentDocument.getElementById("mgnlEditorMoveDiv");

                if (moveElement != null) {
                    int x = event.getClientX() + Window.getScrollLeft();
                    int y = event.getClientY() + 15 + Window.getScrollTop();
                    moveElement.getStyle().setTop(y, Unit.PX);
                    moveElement.getStyle().setLeft(x, Unit.PX);
                }
            }
        }, MouseMoveEvent.getType());

        // save x/y positon
        Window.addWindowScrollHandler(new ScrollHandler() {

            @Override
            public void onWindowScroll(ScrollEvent event) {
                String value = event.getScrollLeft() + ":" + event.getScrollTop();
                JavascriptUtils.setEditorPositionCookie(value);
            }
        });
    }

    /**
     * Helper to return translated src-attribute from embedded's UIDL
     * Copied verbatim from Vaadin's VEmbedded class.
     */
    private String getSrc(UIDL uidl, ApplicationConnection client) {
        String url = client.translateVaadinUri(uidl.getStringAttribute("src"));
        if (url == null) {
            return "";
        }
        return url;
    }


    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        this.paintableId = uidl.getId();
        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        iframe.getElement().setId(paintableId);
        iframe.setUrl(getSrc(uidl, client));


        proxy.update(this, uidl, client);
    }

    private void process(final Document document) {
        //TODO how will we handle preview in 5.0?
        /*String mgnlVersion = Window.Location.getParameter(MGNL_VERSION_PARAMETER);
        if(mgnlVersion != null) {
            return false;
        }

        String mgnlChannel = Window.Location.getParameter(MGNL_CHANNEL_PARAMETER);
        boolean isMobile = "smartphone".equals(mgnlChannel) || "tablet".equals(mgnlChannel);

        if(isMobile) {
            GWT.log("Found " + mgnlChannel + " in request, post processing links...");
            postProcessLinksOnMobilePreview(Document.get().getDocumentElement(), mgnlChannel);
            return false;
        }*/

        JavascriptUtils.setWindowLocation(Window.Location.getPath());
        JavascriptUtils.getCookiePosition();
        locale = JavascriptUtils.detectCurrentLocale();
        //inject editor stylesheet inside head of doc contained in iframe.
        HeadElement head = HeadElement.as(document.getElementsByTagName("head").getItem(0));
        LinkElement cssLink = document.createLinkElement();
        cssLink.setType("text/css");
        cssLink.setRel("stylesheet");
        cssLink.setHref("VAADIN/widgetsets/info.magnolia.ui.vaadin.widgetset.MagnoliaWidgetSet/editor/styles.css");

        head.insertFirst(cssLink);
        long startTime = System.currentTimeMillis();
        processDocument(document.getDocumentElement(), null);
        processMgnlElements();

        GWT.log("Time spent to process cms comments: " + (System.currentTimeMillis() - startTime) + "ms");

        JavascriptUtils.getCookieContentId();
        JavascriptUtils.resetEditorCookies();

        //GWT.log("Running onPageEditorReady callbacks...");
        //onPageEditorReady();
    }
    @Override
    public boolean initWidget(Object[] params) {
        //TODO this seems never to be called
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        VConsole.error("Unhandled RPC call from server: " + method);
    }


    public static ModelStorage getModel() {
        return model;
    }

    private void processDocument(Node node, MgnlElement mgnlElement) {
        if(keepProcessing) {
            for (int i = 0; i < node.getChildCount(); i++) {
                Node childNode = node.getChild(i);
                if (childNode.getNodeType() == Comment.COMMENT_NODE) {

                    try {
                        mgnlElement = CommentProcessor.process(childNode, mgnlElement);
                    } catch (IllegalArgumentException e) {
                        GWT.log("Not CMSComment element, skipping: " + e.toString());
                    } catch (Exception e) {
                        GWT.log("Caught undefined exception: " + e.toString());
                    }
                } else if (childNode.getNodeType() == Node.ELEMENT_NODE && mgnlElement != null) {
                    ElementProcessor.process(childNode, mgnlElement);
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
                    MgnlElementProcessor processor = MgnlElementProcessorFactory.getProcessor(mgnlElement);
                    processor.process();
                } catch (IllegalArgumentException e) {
                    GWT.log("MgnlFactory could not instantiate class. The element is neither an area nor component.");
                }
            }
        }

    }

    //FIXME submitting forms still renders website channel and edit bars
    private void postProcessLinksOnMobilePreview(Element root, String channel) {
        NodeList<Element> anchors = root.getElementsByTagName("a");

        final String mobilePreviewParams = "";//MGNL_CHANNEL_PARAMETER+"="+channel+"&"+ MGNL_PREVIEW_PARAMETER+"=true";

        for (int i = 0; i < anchors.getLength(); i++) {
            AnchorElement anchor = AnchorElement.as(anchors.getItem(i));

            GWT.log("Starting to process link " + anchor.getHref());

            if(JavascriptUtils.isEmpty(anchor.getHref())) {
                continue;
            }
            String manipulatedHref = anchor.getHref().replaceFirst(Window.Location.getProtocol() + "//" + Window.Location.getHost(), "");
            String queryString = Window.Location.getQueryString() != null ? Window.Location.getQueryString() : "";

            GWT.log("query string is " + queryString);

            String queryStringRegex =  queryString.replaceFirst("\\?", "\\\\?");
            manipulatedHref = manipulatedHref.replaceFirst(queryStringRegex, "");
            int indexOfHash = manipulatedHref.indexOf("#");

            if(indexOfHash != -1) {
                manipulatedHref = manipulatedHref.substring(indexOfHash);
            } else {
                if(!queryString.contains(mobilePreviewParams)) {
                    if(queryString.startsWith("?")) {
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
            form.setAction(form.getAction().concat("?"+ mobilePreviewParams));
        }
    }


    private native void onPageEditorReady() /*-{
        var callbacks = $wnd.mgnl.PageEditor.onPageEditorReadyCallbacks
        if(typeof callbacks != 'undefined') {
             for(var i=0; i < callbacks.length; i++) {
                callbacks[i].apply()
             }
         }
    }-*/;

    public static boolean isPreview() {
        return false;
    }

    public static void setKeepProcessing(boolean process) {
        keepProcessing = process;
    }

}
