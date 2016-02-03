/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.pages.app.editor;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.pages.app.editor.event.ComponentMoveEvent;
import info.magnolia.pages.app.editor.event.NodeSelectedEvent;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.vaadin.editor.PageEditorListener;
import info.magnolia.ui.vaadin.editor.PageEditorView;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presenter for the server side {@link PageEditorView}.
 * Serves multiple methods for actions triggered from the page editor.
 */
public class PageEditorPresenter implements PageEditorListener {

    private static final Logger log = LoggerFactory.getLogger(PageEditorPresenter.class);

    private final ActionExecutor actionExecutor;
    private final PageEditorView view;
    private final EventBus subAppEventBus;
    private final SubAppContext subAppContext;
    private final SimpleTranslator i18n;

    private AbstractElement selectedElement;
    private boolean moving = false;

    @Inject
    public PageEditorPresenter(final ActionExecutor actionExecutor, PageEditorView view, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus,
            SubAppContext subAppContext, SimpleTranslator i18n) {
        this.actionExecutor = actionExecutor;
        this.view = view;
        this.subAppEventBus = subAppEventBus;
        this.subAppContext = subAppContext;
        this.i18n = i18n;
        registerHandlers();
    }

    public PageEditorView start() {
        view.setListener(this);
        return view;
    }

    private void registerHandlers() {
        subAppEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                    view.refresh();
            }
        });
        subAppEventBus.addHandler(ComponentMoveEvent.class, new ComponentMoveEvent.Handler() {
            @Override
            public void onMove(ComponentMoveEvent event) {
                moving = event.isStart();
                if (moving) {
                    view.startMoveComponent();
                }
                else if (event.isServerSide()) {
                    view.cancelMoveComponent();
                }
            }
        });

    }

    @Override
    public void onElementSelect(AbstractElement selectedElement) {
        this.selectedElement = selectedElement;
        subAppEventBus.fireEvent(new NodeSelectedEvent(selectedElement));
    }

    @Override
    public void onAction(String actionName, Object... args) {
        try {
            actionExecutor.execute(actionName, args);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, i18n.translate("pages.pageEditorPresenter.actionExecutionError.message"), e.getMessage());
            log.error("An error occurred while executing action [{}]", actionName, e);
            subAppContext.getAppContext().sendLocalMessage(error);
        }
    }

    public AbstractElement getSelectedElement() {
        return selectedElement;
    }

    public void loadPageEditor(PageEditorParameters parameters) {
        view.load(parameters);
    }

    public void updateParameters(PageEditorParameters parameters) {
        view.update(parameters);
    }

    public boolean isMoving() {
        return moving;
    }
}
