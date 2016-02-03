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

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.editor.PageEditorView;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;
import info.magnolia.ui.workbench.StatusBarView;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * PageEditorViewImpl.
 */
public class PagesEditorSubAppViewImpl implements PagesEditorSubAppView {

    // the purpose of this wrapper is to keep keyboard events scoped to it
    private final Panel wrapper = new Panel();

    private final HorizontalLayout root = new HorizontalLayout();

    private final VerticalLayout container = new VerticalLayout();

    private Listener listener;

    private PageEditorView pageEditor;

    private ActionbarView actionBar;

    private final CssLayout actionBarWrapper = new CssLayout();

    private PageBarView pageBarView;

    private StatusBarView statusBarView;

    private final SubAppContext subAppContext;

    private final SimpleTranslator i18n;
    private ServerConfiguration serverConfiguration;

    private HorizontalLayout activationStatus;

    /**
     * @deprecated since 5.2.4 - use info.magnolia.pages.app.editor.PagesEditorSubAppViewImpl#PagesEditorSubAppViewImpl(info.magnolia.ui.vaadin.editor.pagebar.PageBarView, info.magnolia.ui.api.app.SubAppContext, info.magnolia.i18nsystem.SimpleTranslator) instead.
     */
    @Deprecated
    public PagesEditorSubAppViewImpl(PageBarView pageBarView) {
        this(pageBarView, Components.getComponent(SubAppContext.class), Components.getComponent(SimpleTranslator.class), Components.getComponent(ServerConfiguration.class));
    }

    @Inject
    public PagesEditorSubAppViewImpl(PageBarView pageBarView, SubAppContext subAppContext, SimpleTranslator i18n, ServerConfiguration serverConfiguration) {
        this.pageBarView = pageBarView;
        this.subAppContext = subAppContext;
        this.i18n = i18n;
        this.serverConfiguration = serverConfiguration;

        root.setSizeFull();
        root.setStyleName("pageeditor");
        root.addComponent(container);
        root.setExpandRatio(container, 1);
        root.setSpacing(true);
        root.setMargin(false);

        container.setSizeFull();
        container.addStyleName("editor");

        actionBarWrapper.setHeight(100, Unit.PERCENTAGE);
        actionBarWrapper.addStyleName("actionbar");
        root.addComponent(actionBarWrapper);
        root.setExpandRatio(actionBarWrapper, 0);

        wrapper.setSizeFull();
        wrapper.setContent(root);
        wrapper.addShortcutListener(new ShortcutListener("", ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                listener.onEscape();
            }
        });
        wrapper.focus();

    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
        this.pageBarView.setListener(listener);
    }

    @Override
    public void setPageBarView(PageBarView pageBarView) {
        this.pageBarView = pageBarView;
        container.addComponentAsFirst(pageBarView.asVaadinComponent());
    }

    @Override
    public void setPageEditorView(PageEditorView pageEditor) {
        this.pageEditor = pageEditor;
        container.addComponent(pageEditor.asVaadinComponent(), 1);
        container.setExpandRatio(pageEditor.asVaadinComponent(), 1f);
    }

    @Override
    public void setActionbarView(final ActionbarView actionBar) {
        Component c = actionBar.asVaadinComponent();
        Component old = actionBarWrapper.getComponentCount() != 0 ? actionBarWrapper.getComponent(0) : null;
        if (old == null) {
            actionBarWrapper.addComponent(c);
        } else {
            actionBarWrapper.replaceComponent(old, c);
        }
        this.actionBar = actionBar;
    }

    @Override
    public void setStatusBarView(StatusBarView statusBarView) {
        this.statusBarView = statusBarView;
        if (serverConfiguration.isAdmin()) {
            DetailLocation location = DetailLocation.wrap(this.subAppContext.getLocation());
            String nodePath = location.getNodePath();
            HorizontalLayout status = getActivationStatus(nodePath);
            if (activationStatus != null) {
                this.statusBarView.removeComponent(activationStatus);
            }
            this.statusBarView.addComponent(status, Alignment.MIDDLE_CENTER);
            this.activationStatus = status;
        }
        container.addComponent(this.statusBarView.asVaadinComponent());
    }

    @Override
    public Component asVaadinComponent() {
        return wrapper;
    }

    @Override
    public void hideActionbar(boolean hide) {
        if (actionBar != null) {
            actionBar.asVaadinComponent().setVisible(!hide);
        }
    }

    @Override
    public void setContentView(View view) {

    }

    private HorizontalLayout getActivationStatus(String nodePath) {
        Integer status;
        String icon = "activation-status ";
        String text = i18n.translate("pages.editPage.statusBar.unpublished");
        try {
            Node node = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE).getNode(nodePath);
            status = NodeTypes.Activatable.getActivationStatus(node);
        } catch (RepositoryException e) {
            status = NodeTypes.Activatable.ACTIVATION_STATUS_NOT_ACTIVATED;
        }

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

        Label iconLabel = new Label();
        iconLabel.addStyleName(icon);

        Label textLabel = new Label();
        textLabel.addStyleName("activationstatus");
        textLabel.setValue(text);

        HorizontalLayout layout = new HorizontalLayout();
        layout.addStyleName("statusbar");
        layout.addComponent(iconLabel);
        layout.addComponent(textLabel);

        return layout;
    }

}
