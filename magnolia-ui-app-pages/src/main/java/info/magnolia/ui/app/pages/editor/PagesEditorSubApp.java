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
package info.magnolia.ui.app.pages.editor;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.app.pages.PagesApp;
import info.magnolia.ui.app.pages.action.PagesActionbarDefinitionProvider;
import info.magnolia.ui.framework.app.AbstractSubApp;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.widget.actionbar.ActionbarView;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * PagesEditorSubApp.
 */
public class PagesEditorSubApp extends AbstractSubApp implements PagesEditorView.Listener {

    private final PagesEditorView view;

    private final EventBus appEventBus;

    private final PageEditorPresenter pageEditorPresenter;

    private PageEditorParameters parameters;

    private final ActionbarPresenter actionbarPresenter;

    private String caption;

    @Inject
    public PagesEditorSubApp(PagesEditorView view, @Named("app") EventBus appEventBus, PageEditorPresenter pageEditorPresenter, ActionbarPresenter actionbarPresenter) {
        this.view = view;
        this.appEventBus = appEventBus;
        this.pageEditorPresenter = pageEditorPresenter;
        this.actionbarPresenter = actionbarPresenter;

        bindHandlers();
    }

    private void bindHandlers() {
        appEventBus.addHandler(ComponentSelectedEvent.class, new ComponentSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ComponentSelectedEvent event) {
                // TODO 20120730 mgeljic, review whether presenter should be a proxy for every
                // single actionbar widget feature
                if (event.getPath() != null) {
                    actionbarPresenter.hideSection("Pages");
                    actionbarPresenter.hideSection("Areas");
                    actionbarPresenter.showSection("Components");
                }
            }
        });
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setParameters(PageEditorParameters parameters) {
        this.parameters = parameters;
        this.caption = parameters.getNodePath();
    }

    @Override
    public View start(Location location) {

        String path = getEditorPath(location);
        if (path == null)
            path = "/";

        setParameters(new PageEditorParameters(MgnlContext.getContextPath(), path));
        pageEditorPresenter.setParameters(parameters);

        view.setListener(this);
        view.setPageEditor(pageEditorPresenter.start());

        ActionbarView actionbar = actionbarPresenter.start(PagesActionbarDefinitionProvider.getPageEditorActionbarDefinition());
        actionbarPresenter.hideSection("Areas");
        actionbarPresenter.hideSection("Components");
        actionbarPresenter.showSection("Pages");

        view.setActionbarView(actionbar);

        return view;
    }

    private String getEditorPath(Location location) {
        String token = ((DefaultLocation) location).getToken();
        String[] parts = token.split(";");
        if (parts.length < 2) {
            return null;
        }
        if (!parts[0].equals(PagesApp.EDITOR_TOKEN)) {
            return null;
        }
        return parts[1];
    }

}
