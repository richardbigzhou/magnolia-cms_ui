/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.AbstractMgnlElementProcessor;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.CommentProcessor;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.ElementProcessor;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.MgnlElementProcessorFactory;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.ProcessException;
import info.magnolia.ui.vaadin.gwt.client.editor.event.ComponentStartMoveEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.ComponentStopMoveEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.EditAreaEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.EditAreaEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.event.EditComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.EditComponentEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.event.FrameNavigationEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.FrameNavigationEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.event.NewAreaEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.NewAreaEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.event.NewComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.NewComponentEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SelectElementEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SelectElementEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SortComponentEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.event.SortComponentEventHandler;
import info.magnolia.ui.vaadin.gwt.client.editor.jsni.event.FrameLoadedEvent;
import info.magnolia.ui.vaadin.gwt.client.editor.jsni.scroll.ElementScrollPositionPreserver;
import info.magnolia.ui.vaadin.gwt.client.editor.model.Model;
import info.magnolia.ui.vaadin.gwt.client.editor.model.ModelImpl;
import info.magnolia.ui.vaadin.gwt.client.editor.model.focus.FocusModel;
import info.magnolia.ui.vaadin.gwt.client.editor.model.focus.FocusModelImpl;
import info.magnolia.ui.vaadin.gwt.client.rpc.PageEditorClientRpc;
import info.magnolia.ui.vaadin.gwt.client.rpc.PageEditorServerRpc;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;
import info.magnolia.ui.vaadin.gwt.client.widget.PageEditorView;
import info.magnolia.ui.vaadin.gwt.client.widget.PageEditorViewImpl;
import info.magnolia.ui.vaadin.gwt.client.widget.dnd.MoveWidget;

import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.BrowserInfo;
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

    private Logger log = Logger.getLogger(getClass().getName());

    private static final String PAGE_EDITOR_CSS = "/VAADIN/themes/ui-app-pages/page-editor.css";

    private final PageEditorServerRpc rpc = RpcProxy.create(PageEditorServerRpc.class, this);

    private final EventBus eventBus = new SimpleEventBus();

    private PageEditorView view;
    private MoveWidget moveWidget;

    private Model model;

    private FocusModel focusModel;
    private ElementProcessor elementProcessor;
    private CommentProcessor commentProcessor;

    @Override
    protected void init() {
        super.init();
        this.model = new ModelImpl();
        this.focusModel = new FocusModelImpl(eventBus, model);
        this.elementProcessor = new ElementProcessor(eventBus, model);
        this.commentProcessor = new CommentProcessor();

        addStateChangeHandler(new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                PageEditorParameters params = getState().parameters;
                view.setUrl(params.getUrl());
                if (params.isPreview()) {
                    view.getFrame().addStyleName("iframe-preloader");
                }
            }
        });

        registerRpc(PageEditorClientRpc.class, new PageEditorClientRpc() {

            @Override
            public void refresh() {
                view.reload();
            }

            @Override
            public void startMoveComponent() {
                MgnlComponent component = model.getSelectedComponent();
                if (component != null) {
                    component.doStartMove(false);
                    model.setMoving(true);
                    if (!BrowserInfo.get().isTouchDevice()) {
                        Element element = DOM.clone(model.getSelectedComponent().getControlBar().getElement(), true);
                        moveWidget = new MoveWidget(element);
                        moveWidget.attach(view.getFrame(), component.getWidth(), component.getHeight());
                    }
                }
            }

            @Override
            public void cancelMoveComponent() {
                eventBus.fireEvent(new ComponentStopMoveEvent(null, true));
            }
        });

        eventBus.addHandler(FrameLoadedEvent.TYPE, new FrameLoadedEvent.Handler() {
            @Override
            public void handle(FrameLoadedEvent event) {
                model.reset();
                Document document = event.getFrame().getContentDocument();
                process(document);

                view.initKeyEventListeners();

                if (!getState().parameters.isPreview()) {
                    view.initDomEventListeners();
                    focusModel.init();
                } else {
                    focusModel.select(model.getRootPage());
                }
            }
        });

        eventBus.addHandler(FrameNavigationEvent.TYPE, new FrameNavigationEventHandler() {
            @Override
            public void onFrameUrlChanged(FrameNavigationEvent frameUrlChangedEvent) {
                String path = frameUrlChangedEvent.getPath();

                final String platformId = getState().parameters.getPlatformType().getId();
                path += path.indexOf('?') == -1 ? "?" : "&";
                path += "mgnlChannel=" + platformId;

                final boolean isPreview = getState().parameters.isPreview();
                path += "&mgnlPreview=" + isPreview;

                view.setUrl(path);
            }
        });

        eventBus.addHandler(SelectElementEvent.TYPE, new SelectElementEventHandler() {
            @Override
            public void onSelectElement(SelectElementEvent selectElementEvent) {
                AbstractElement element = selectElementEvent.getElement();
                if (element instanceof PageElement) {
                    rpc.selectPage((PageElement) selectElementEvent.getElement());
                } else if (element instanceof AreaElement) {
                    rpc.selectArea((AreaElement) selectElementEvent.getElement());
                } else if (element instanceof ComponentElement) {
                    rpc.selectComponent((ComponentElement) selectElementEvent.getElement());
                }
                view.resetScrollTop();
            }
        });

        eventBus.addHandler(NewAreaEvent.TYPE, new NewAreaEventHandler() {
            @Override
            public void onNewArea(NewAreaEvent newAreaEvent) {
                rpc.newArea(newAreaEvent.getAreaElement());
            }
        });

        eventBus.addHandler(NewComponentEvent.TYPE, new NewComponentEventHandler() {
            @Override
            public void onNewComponent(NewComponentEvent newComponentEvent) {
                rpc.newComponent(newComponentEvent.getParentAreaElement());
            }
        });

        eventBus.addHandler(EditAreaEvent.TYPE, new EditAreaEventHandler() {
            @Override
            public void onEditArea(EditAreaEvent editAreaEvent) {
                rpc.editArea(editAreaEvent.getAreaElement());
            }
        });

        eventBus.addHandler(EditComponentEvent.TYPE, new EditComponentEventHandler() {
            @Override
            public void onEditComponent(EditComponentEvent editComponentEvent) {
                rpc.editComponent(editComponentEvent.getComponentElement());
            }
        });

        eventBus.addHandler(SortComponentEvent.TYPE, new SortComponentEventHandler() {
            @Override
            public void onSortComponent(SortComponentEvent sortComponentEvent) {
                rpc.sortComponent(sortComponentEvent.getAreaElement());
            }
        });

        eventBus.addHandler(ComponentStartMoveEvent.TYPE, new ComponentStartMoveEvent.CompnentStartMoveEventHandler() {
            @Override
            public void onStart(ComponentStartMoveEvent componentStartMoveEvent) {
                rpc.startMoveComponent();
            }
        });

        eventBus.addHandler(ComponentStopMoveEvent.TYPE, new ComponentStopMoveEvent.ComponentStopMoveEventHandler() {
            @Override
            public void onStop(ComponentStopMoveEvent componentStopMoveEvent) {
                if (!componentStopMoveEvent.isServerSide()) {
                    rpc.stopMoveComponent();
                }
                if (moveWidget != null && moveWidget.isAttached()) {
                    moveWidget.detach();
                }
                model.setMoving(false);
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
        final MgnlComponent currentlySelected = model.getSelectedComponent();
        final MgnlElement elementToSelect = model.getMgnlElement(element);
        final MgnlComponent componentToPreserve = (elementToSelect != null && elementToSelect.isComponent()) ? (MgnlComponent) elementToSelect : currentlySelected;

        ElementScrollPositionPreserver scrollPositionPreserver = null;
        if (componentToPreserve != null) {
            scrollPositionPreserver = new ElementScrollPositionPreserver(componentToPreserve);
        }

        focusModel.selectElement(element);

        if (scrollPositionPreserver != null) {
            final ElementScrollPositionPreserver preserver = scrollPositionPreserver;
            preserver.restorePosition();
        }
    }

    private void process(final Document document) {
        try {
            injectEditorStyles(document);
            long startTime = System.currentTimeMillis();
            processDocument(document, null);
            processMgnlElements();
            GWT.log("Time spent to process cms comments: " + (System.currentTimeMillis() - startTime) + "ms");
        } catch (ProcessException e) {
            rpc.onError(e.getErrorType(), e.getTagName());
            GWT.log("Error while processing comment: " + e.getTagName() + " due to " + e.getErrorType());
            consoleLog("Error while processing comment: " + e.getTagName() + " due to " + e.getErrorType()); // log also into browser console
        }
    }

    private void injectEditorStyles(final Document document) {
        HeadElement head = HeadElement.as(document.getElementsByTagName("head").getItem(0));
        LinkElement cssLink = document.createLinkElement();
        cssLink.setType("text/css");
        cssLink.setRel("stylesheet");
        cssLink.setHref(getState().parameters.getContextPath() + PAGE_EDITOR_CSS);
        head.insertFirst(cssLink);
    }

    private void processDocument(Node node, MgnlElement mgnlElement) throws ProcessException {
        if (mgnlElement == null && model.getRootPage() != null) {
            mgnlElement = model.getRootPage();
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            Node childNode = node.getChild(i);
            if (childNode.getNodeType() == Comment.COMMENT_NODE) {
                try {
                    mgnlElement = commentProcessor.process(model, eventBus, childNode, mgnlElement);
                } catch (ProcessException e) {
                    throw e;
                } catch (IllegalArgumentException e) {
                    GWT.log("Not CMSComment element, skipping: " + e.toString());
                } catch (Exception e) {
                    GWT.log("Caught undefined exception: " + e.toString());
                    consoleLog("Caught undefined exception: " + e.toString()); // log also into browser console
                }
            } else if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = childNode.cast();
                elementProcessor.process(element, mgnlElement, getState().parameters.isPreview());
            }
            processDocument(childNode, mgnlElement);
        }
    }

    private void processMgnlElements() {
        CmsNode root = model.getRootPage();
        if (model.getRootPage() == null) {
            log.warning("Could not find any Magnolia cms:page tag, this might be a static page; not injecting page-editor bars.");
            return;
        }

        List<CmsNode> elements = root.getDescendants();
        elements.add(root);
        for (CmsNode element : elements) {
            try {
                AbstractMgnlElementProcessor processor = MgnlElementProcessorFactory.getProcessor(model, element.asMgnlElement());
                processor.process();
            } catch (IllegalArgumentException e) {
                GWT.log("MgnlFactory could not instantiate class. The element is neither an area nor component.");
            } catch (Exception e) {
                final String errorMessage = "Error when processing editor components for '" + element.asMgnlElement().getAttribute("path") +
                        "'. It's possible that the template script for this area or for some subcomponent is incorrect. Please check that all HTML tags are closed properly.\n"
                        + e.toString();
                GWT.log(errorMessage);
                consoleLog(errorMessage); // log also into browser console
            }
        }
    }

    void consoleLog(String message) {
        log.info("PageEditor: " + message);
    }

}
