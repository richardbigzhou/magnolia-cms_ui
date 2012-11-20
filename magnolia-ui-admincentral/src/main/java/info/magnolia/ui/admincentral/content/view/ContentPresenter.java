/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.content.view;

import info.magnolia.ui.admincentral.app.content.ContentAppDescriptor;
import info.magnolia.ui.admincentral.content.view.ContentView.ViewType;
import info.magnolia.ui.admincentral.content.view.builder.ContentViewBuilder;
import info.magnolia.ui.admincentral.event.ItemDoubleClickedEvent;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchView;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;


/**
 * Presenter for ContentView.
 */
public class ContentPresenter implements ContentView.Listener {

    private static final Logger log = LoggerFactory.getLogger(ContentPresenter.class);

    private final EventBus subAppEventBus;

    private final Shell shell;

    private final String workspaceName;

    private final ContentViewBuilder contentViewBuilder;

    protected WorkbenchDefinition workbenchDefinition;

    private String selectedItemPath;

    @Inject
    public ContentPresenter(final ContentViewBuilder contentViewBuilder, final AppContext context, @Named("subapp") final EventBus subAppEventBus, final Shell shell) {
        this.contentViewBuilder = contentViewBuilder;
        this.subAppEventBus = subAppEventBus;
        this.shell = shell;

        final ContentAppDescriptor appDescriptor = ((ContentAppDescriptor) context.getAppDescriptor());
        this.workbenchDefinition = appDescriptor.getWorkbench();
        this.workspaceName = appDescriptor.getWorkbench().getWorkspace();
    }

    public void initContentView(ContentWorkbenchView parentView) {
        if (workbenchDefinition == null) {
            throw new IllegalArgumentException("Trying to init a workbench but got null definition.");
        }
        log.debug("Initializing workbench {}...", workbenchDefinition.getName());

        for (final ViewType type : ViewType.values()) {
            final ContentView contentView = contentViewBuilder.build(workbenchDefinition, type);
            contentView.setListener(this);
            contentView.select(StringUtils.defaultIfEmpty(workbenchDefinition.getPath(), "/"));
            parentView.addContentView(type, contentView);
        }

        if (StringUtils.isBlank(workbenchDefinition.getWorkspace())) {
            throw new IllegalStateException(workbenchDefinition.getName()
                + " workbench definition must specify a workspace to connect to. Please, check your configuration.");
        }

        parentView.setViewType(ViewType.TREE);
    }

    @Override
    public void onItemSelection(Item item) {
        if (item == null) {
            log.debug("Got null com.vaadin.data.Item. ItemSelectedEvent will be fired with null path.");
            selectedItemPath = null;
            subAppEventBus.fireEvent(new ItemSelectedEvent(workspaceName, null));
            return;
        }
        try {
            selectedItemPath = ((JcrItemAdapter) item).getPath();
            log.debug("com.vaadin.data.Item at {} was selected. Firing ItemSelectedEvent...", selectedItemPath);
            subAppEventBus.fireEvent(new ItemSelectedEvent(workspaceName, (JcrItemAdapter) item));
        } catch (Exception e) {
            shell.showError("An error occurred while selecting a row in the data grid", e);
        }
    }

    /**
     * @return the path of the vaadin item currently selected in the currently active
     * {@link ContentView}. It is equivalent to javax.jcr.Item#getPath().
     * @see JcrItemAdapter#getPath()
     */
    public String getSelectedItemPath() {
        return selectedItemPath;
    }

    @Override
    public void onDoubleClick(Item item) {
        if (item != null) {
            try {
                selectedItemPath = ((JcrItemAdapter) item).getPath();
                log.debug("com.vaadin.data.Item at {} was double clicked. Firing ItemDoubleClickedEvent...", selectedItemPath);
                subAppEventBus.fireEvent(new ItemDoubleClickedEvent(workspaceName, selectedItemPath));
            } catch (Exception e) {
                shell.showError("An error occurred while double clicking on a row in the data grid", e);
            }
        } else {
            log.warn("Got null com.vaadin.data.Item. No event will be fired.");
        }
    }
}
