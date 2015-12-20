/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.jcrbrowser.app.contenttools;

import info.magnolia.event.EventBus;
import info.magnolia.jcrbrowser.app.SystemPropertiesVisibilityToggledEvent;
import info.magnolia.jcrbrowser.app.contentconnector.JcrBrowserContentConnector;
import info.magnolia.jcrbrowser.app.workbench.JcrBrowserWorkbenchPresenter;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.workbench.contenttool.ContentToolPresenter;

import javax.inject.Inject;

import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;

/**
 * Workbench {@link ContentToolPresenter content tool presenter} which primarily allows for altering the JCR workspace
 * browsed via {@link JcrBrowserWorkbenchPresenter}. In addition the tool provides a toggle for turning the display of system properties in
 * content views on and off.
 *
 * @see JcrBrowserWorkbenchPresenter
 * @see SystemPropertiesVisibilityToggledEvent
 * @see info.magnolia.jcrbrowser.app.contentviews.JcrBrowserTreePresenter
 */
public class JcrBrowserContextTool implements ContentToolPresenter {

    private final JcrBrowserContextToolView view;
    private final EventBus eventBus;
    private final RepositoryManager repositoryManager;
    private final JcrBrowserContentConnector relatedContentConnector;
    private final JcrBrowserWorkbenchPresenter workbenchPresenter;

    @Inject
    public JcrBrowserContextTool(JcrBrowserContextToolView view, EventBus eventBus, RepositoryManager repositoryManager, ContentConnector relatedContentConnector, JcrBrowserWorkbenchPresenter workbenchPresenter) {
        this.view = view;
        this.eventBus = eventBus;
        this.repositoryManager = repositoryManager;
        this.relatedContentConnector = (JcrBrowserContentConnector) relatedContentConnector;
        this.workbenchPresenter = workbenchPresenter;
    }

    @Override
    public View start() {
        final ObjectProperty<String> workspaceNameProperty = new ObjectProperty<>(relatedContentConnector.getContentConnectorDefinition().getWorkspace());
        final ObjectProperty<Boolean> includeSystemPropertiesProperty = new ObjectProperty<>(false);

        view.setWorkspaceOptions(populateWorkspaceNames());
        view.setWorkspaceNameProperty(workspaceNameProperty);
        view.setSystemPropertiesInclusionProperty(includeSystemPropertiesProperty);
        view.setEnabled(!workbenchPresenter.isHostedInDialog());

        workspaceNameProperty.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                relatedContentConnector.getContentConnectorDefinition().setWorkspace(workspaceNameProperty.getValue());
                workbenchPresenter.refresh();
            }
        });

        includeSystemPropertiesProperty.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                eventBus.fireEvent(new SystemPropertiesVisibilityToggledEvent(includeSystemPropertiesProperty.getValue()));
            }
        });

        return view;
    }

    private IndexedContainer populateWorkspaceNames() {
        final IndexedContainer container = new IndexedContainer();
        for (final String workspaceName : repositoryManager.getWorkspaceNames()) {
            container.addItem(workspaceName);
        }
        return container;
    }
}
