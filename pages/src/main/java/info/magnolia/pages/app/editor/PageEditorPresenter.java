/**
 * This file Copyright (c) 2012-2014 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.link.LinkUtil;
import info.magnolia.pages.app.editor.event.ComponentMoveEvent;
import info.magnolia.pages.app.editor.event.NodeSelectedEvent;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.vaadin.editor.PageEditorListener;
import info.magnolia.ui.vaadin.editor.PageEditorView;
import info.magnolia.ui.vaadin.editor.gwt.shared.PlatformType;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presenter for the server side {@link PageEditorView}.
 * Serves multiple methods for actions triggered from the page editor.
 */
@Singleton
public class PageEditorPresenter implements PageEditorListener {

    private static final Logger log = LoggerFactory.getLogger(PageEditorPresenter.class);
    public static final String VERSION_PARAMETER = "mgnlVersion";
    public static final String PREVIEW_PARAMETER = "mgnlPreview";
    public static final String CHANNEL_PARAMETER = "mgnlChannel";

    private final ActionExecutor actionExecutor;
    private final PageEditorView view;
    private final EventBus subAppEventBus;
    private final SubAppContext subAppContext;
    private final SimpleTranslator i18n;
    private final I18NAuthoringSupport i18NAuthoringSupport;

    private AbstractElement selectedElement;
    private boolean moving = false;
    private Listener listener;
    private PageEditorParameters parameters;
    private PlatformType platformType = PlatformType.DESKTOP;

    @Inject
    public PageEditorPresenter(final ActionExecutor actionExecutor, PageEditorView view, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus,
            SubAppContext subAppContext, SimpleTranslator i18n, I18NAuthoringSupport i18NAuthoringSupport) {
        this.actionExecutor = actionExecutor;
        this.view = view;
        this.subAppEventBus = subAppEventBus;
        this.subAppContext = subAppContext;
        this.i18n = i18n;
        this.i18NAuthoringSupport = i18NAuthoringSupport;
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

    public void loadPageEditor() {
        view.load(parameters);
    }

    public void updateParameters() {
        view.update(parameters);
    }

    public boolean isMoving() {
        return moving;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public PageEditorParameters getParameters() {
        return parameters;
    }

    public void updateParameters(DetailLocation location) {
        DetailView.ViewType viewType = location.getViewType();

        boolean isPreview = DetailView.ViewType.VIEW.equals(viewType);

        this.parameters = new PageEditorParameters(MgnlContext.getContextPath(), location.getNodePath(), isPreview);
        this.parameters.setPlatformType(platformType);

        try {
            Node node = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE).getNode(location.getNodePath());
            String uri = i18NAuthoringSupport.createI18NURI(node, listener.getCurrentLocale());
            StringBuffer sb = new StringBuffer(uri);

            if (isPreview) {

                LinkUtil.addParameter(sb, PREVIEW_PARAMETER, Boolean.toString(true));
            } else {
                // reset channel
                this.platformType = PlatformType.DESKTOP;
                this.parameters.setPlatformType(platformType);
                listener.setPlatFormType(platformType);

                LinkUtil.addParameter(sb, PREVIEW_PARAMETER, Boolean.toString(false));
            }

            LinkUtil.addParameter(sb, CHANNEL_PARAMETER, platformType.getId());

            if (location.hasVersion()) {
                LinkUtil.addParameter(sb, VERSION_PARAMETER, location.getVersion());
            }
            uri = sb.toString();
            this.parameters.setUrl(uri);
        } catch (RepositoryException e) {
            log.error("Could not get page node from location object.", e);
        }
    }

    public boolean isLocationChanged(DetailLocation location) {
        DetailView.ViewType viewType = location.getViewType();
        String path = location.getNodePath();

        if (parameters != null && (parameters.getNodePath().equals(path) && parameters.isPreview() == DetailView.ViewType.VIEW.equals(viewType)) && !location.hasVersion()) {
            return false;
        }
        return true;
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public void setPlatformType(PlatformType platformType) {
        this.platformType = platformType;
    }

    /**
     * Listener interface to call {@link PageEditorPresenter}.
     */
    interface Listener {
        void onMove();

        void setPlatFormType(PlatformType platFormType);

        Locale getCurrentLocale();
    }
}
