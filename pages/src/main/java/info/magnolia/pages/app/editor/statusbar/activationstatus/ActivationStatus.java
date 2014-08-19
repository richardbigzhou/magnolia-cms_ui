/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.pages.app.editor.statusbar.activationstatus;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.pages.app.editor.event.NodeSelectedEvent;
import info.magnolia.pages.app.editor.statusbar.StatusBarPresenter;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * An extension displaying the current activation status of the page inside the status bar. Returns null on start, in
 * case this is not the admin instance.
 */
public class ActivationStatus {

    private final SimpleTranslator i18n;
    private final ServerConfiguration serverConfiguration;
    private final ContentConnector contentConnector;
    private final EventBus admincentralEventBus;
    private final EventBus subAppEventBus;

    private ActivationStatusView view;
    private StatusBarPresenter listener;
    private ComponentProvider componentProvider;

    @Inject
    public ActivationStatus(SimpleTranslator i18n, ServerConfiguration serverConfiguration, ContentConnector contentConnector,
                            final @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus, ComponentProvider componentProvider) {
        this.i18n = i18n;
        this.serverConfiguration = serverConfiguration;
        this.contentConnector = contentConnector;
        this.admincentralEventBus = admincentralEventBus;
        this.subAppEventBus = subAppEventBus;
        this.componentProvider = componentProvider;
    }

    public View start(DetailLocation location) {
        if (serverConfiguration.isAdmin()) {
            registerHandlers();
            this.view = componentProvider.getComponent(ActivationStatusView.class);
            updateActivationStatus(location.getNodePath());
        }
        return view;
    }

    private int getActivationStatus(String nodePath) {
        int status = NodeTypes.Activatable.ACTIVATION_STATUS_NOT_ACTIVATED;
        try {
            Node node = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE).getNode(nodePath);
            status = NodeTypes.Activatable.getActivationStatus(node);
        } catch (RepositoryException e) {
            // page has no activation status
        }
        return status;
    }


    protected void updateActivationStatus(String nodePath) {
        if (view != null) {
            int status = getActivationStatus(nodePath);

            String icon = "activation-status ";
            String text = i18n.translate("pages.editPage.statusBar.unpublished");
            switch (status) {
            case NodeTypes.Activatable.ACTIVATION_STATUS_MODIFIED:
                icon += "color-yellow icon-status-orange";
                text = i18n.translate("pages.editPage.statusBar.modified");
                break;
            case NodeTypes.Activatable.ACTIVATION_STATUS_ACTIVATED:
                icon += "color-green icon-status-green";
                text = i18n.translate("pages.editPage.statusBar.published");
                break;
            default:
                icon += "color-red icon-status-red";
            }

            view.setIconStyle(icon);
            view.setActivationStatus(text);
        }
    }

    private void registerHandlers() {
        subAppEventBus.addHandler(NodeSelectedEvent.class, new NodeSelectedEvent.Handler() {
            @Override
            public void onItemSelected(NodeSelectedEvent event) {
                AbstractElement element = event.getElement();
                if (element instanceof PageElement) {
                    updateActivationStatus(event.getElement().getPath());
                }
            }
        });

        subAppEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                DetailLocation location = listener.getCurrentLocation();
                updateActivationStatus(location.getNodePath());
            }
        });

        admincentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                if (contentConnector.canHandleItem(event.getItemId())) {
                    DetailLocation location = listener.getCurrentLocation();
                    updateActivationStatus(location.getNodePath());
                }
            }
        });
    }

    public void setListener(StatusBarPresenter listener) {
        this.listener = listener;
    }

}
