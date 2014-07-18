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
package info.magnolia.ui.workbench.autosuggest;

import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.tree.TreePresenter;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AutoSuggestTreePresenter is responsible for creating, configuring and updating a tree of items with auto-suggest according to the workbench definition.
 */
public class AutoSuggestTreePresenter extends TreePresenter {

    private static final Logger log = LoggerFactory.getLogger(AutoSuggestTreePresenter.class);

    @Inject
    public AutoSuggestTreePresenter(AutoSuggestTreeView view, ComponentProvider componentProvider) {
        super(view, componentProvider);
    }

    @Override
    public AutoSuggestTreeView start(WorkbenchDefinition workbenchDefinition, EventBus eventBus, String viewTypeName, ContentConnector contentConnector) {
        AutoSuggestTreeView view = (AutoSuggestTreeView) super.start(workbenchDefinition, eventBus, viewTypeName, contentConnector);

        // Configure the AutoSuggestTreeViewImpl with the AutoSuggester implementation specified by the user in the configuration tree
        ContentPresenterDefinition presenterDefinition = this.getPresenterDefinition();
        if (presenterDefinition != null && presenterDefinition instanceof AutoSuggestTreePresenterDefinition) {
            Class<? extends AutoSuggester> autoSuggesterClass = ((AutoSuggestTreePresenterDefinition) presenterDefinition).getAutoSuggesterClass();
            try {
                view.setAutoSuggester(autoSuggesterClass.newInstance());
            } catch (Exception e) {
                view.setAutoSuggester(null);
                log.warn("Could not instantiate AutoSuggester implementation class " + autoSuggesterClass.getName() + " in AutoSuggestTreePresenter.");
            }
        }

        return view;
    }
}
