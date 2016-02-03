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
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.overlay.AlertCallback;
import info.magnolia.ui.vaadin.editor.PageEditorListener;
import info.magnolia.ui.vaadin.editor.PageEditorView;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ErrorType;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;

/**
 * Presenter for the server side {@link PageEditorView}.
 * Serves multiple methods for actions triggered from the page editor.
 */
@Singleton
public class PageEditorPresenter implements PageEditorListener {

    private static final Logger log = LoggerFactory.getLogger(PageEditorPresenter.class);

    private final ActionExecutor actionExecutor;
    private final PageEditorView view;
    private final EventBus subAppEventBus;
    private final SubAppContext subAppContext;
    private final SimpleTranslator i18n;

    private AbstractElement selectedElement;
    private boolean moving = false;
    private Listener listener;

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
        subAppEventBus.addHandler(ComponentMoveEvent.class, new ComponentMoveEvent.Handler() {
            @Override
            public void onMove(ComponentMoveEvent event) {
                moving = event.isStart();
                if (moving) {
                    view.startMoveComponent();
                } else if (event.isServerSide()) {
                    view.cancelMoveComponent();
                }

                listener.onMove();
            }
        });
    }

    @Override
    public void onElementSelect(AbstractElement selectedElement) {
        this.selectedElement = selectedElement;
        subAppEventBus.fireEvent(new NodeSelectedEvent(selectedElement));
    }

    @Override
    public void onError(ErrorType errorType, String... parameters) {
        if (errorType == null) {
            throw new IllegalArgumentException("ErrorType must be one of ErrorType.values().");
        }

        String key = String.format("pages.templateErrorAlert.%s.message", CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, errorType.name()));
        String message = i18n.translate(key, parameters);
        subAppContext.openAlert(MessageStyleTypeEnum.WARNING, i18n.translate("pages.templateErrorAlert.title"), message, i18n.translate("button.ok"), new AlertCallback() {
            @Override
            public void onOk() {
                // Do nothing.
            }
        });
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

    public void refresh() {
        view.refresh();
    }

    public boolean isMoving() {
        return moving;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Listener interface to call {@link PageEditorPresenter}.
     */
    interface Listener {
        void onMove();
    }
}
