/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.pages.action;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.actionbar.builder.ActionbarBuilder;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchView;
import info.magnolia.ui.app.pages.PageEditorSubApp;
import info.magnolia.ui.app.pages.PageEditorTabView;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.widget.actionbar.Actionbar;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Opens a page for editing.
 */
public class EditPageAction extends ActionBase<EditPageActionDefinition> {

    private final Node selectedNode;

    private final ContentWorkbenchView.Presenter presenter;

    private final ActionbarDefinition pageEditorActionbar;

    private final ComponentProvider componentProvider;
    @Inject
    public EditPageAction(final EditPageActionDefinition definition, final Node selectedNode, final ContentWorkbenchView.Presenter presenter, ComponentProvider componentProvider) {
        super(definition);
        this.selectedNode = selectedNode;
        this.presenter = presenter;
        this.componentProvider = componentProvider;
        this.pageEditorActionbar = PagesActionbarDefinitionProvider.getPageEditorActionbarDefinition();
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {

            Actionbar actionBar =  ActionbarBuilder.build(pageEditorActionbar, presenter);

            final PageEditorTabView view = new PageEditorTabView(componentProvider, selectedNode, actionBar);

            PageEditorSubApp pageEditorSubApp = new PageEditorSubApp(view);

            presenter.onOpenNewView(pageEditorSubApp, new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, "app:pages:", view.getCaption()));

        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }
}
