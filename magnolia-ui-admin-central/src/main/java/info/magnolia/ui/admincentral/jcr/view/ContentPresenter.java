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
package info.magnolia.ui.admincentral.jcr.view;

import javax.inject.Inject;

import info.magnolia.ui.admincentral.app.content.ContentAppDescriptor;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.admincentral.jcr.view.ContentView.ViewType;
import info.magnolia.ui.admincentral.jcr.view.builder.ContentViewBuilderProvider;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchView;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Presenter for ContentView.
 */
public class ContentPresenter implements ContentView.Listener {

    private static final Logger log = LoggerFactory.getLogger(ContentPresenter.class);

    private final EventBus eventBus;
    private final Shell shell;
    private final String workspaceName;
    private final ContentViewBuilderProvider contentViewBuilderProvider;
    private final WorkbenchDefinition workbenchDefinition;

    private String selectedItemId;

    @Inject
    public ContentPresenter(final ContentViewBuilderProvider contentViewBuilderProvider, final AppContext context, final EventBus eventbus, final Shell shell) {
        this.contentViewBuilderProvider = contentViewBuilderProvider;
        this.eventBus = eventbus;
        this.shell = shell;
        this. workbenchDefinition = ((ContentAppDescriptor) context.getAppDescriptor()).getWorkbench();

        this.workspaceName = ((ContentAppDescriptor) context.getAppDescriptor()).getWorkbench().getWorkspace();
    }

    public void initContentView(ContentWorkbenchView parentView) {
        if (workbenchDefinition == null) {
            throw new IllegalArgumentException("Trying to init a workbench but got null definition.");
        }
        log.debug("Initializing workbench {}...", workbenchDefinition.getName());

        for (final ViewType type : ViewType.values()) {
            final ContentView contentView = contentViewBuilderProvider.getBuilder().build(workbenchDefinition, type);
            contentView.setListener(this);
            contentView.select(StringUtils.defaultIfEmpty(workbenchDefinition.getPath(), "/"));
            parentView.addContentView(type, contentView);
        }

        if (StringUtils.isBlank(workbenchDefinition.getWorkspace())) {
            throw new IllegalStateException(workbenchDefinition.getName()
                + " workbench definition must specify a workspace to connect to. Please, check your configuration.");
        }

        parentView.setGridType(ViewType.TREE);
    }

    @Override
    public void onItemSelection(Item item) {
        if (item == null) {
            log.warn("Got null javax.jcr.Item. No ItemSelectedEvent will be fired.");
            return;
        }
        try {
            selectedItemId = ((JcrItemAdapter) item).getItemId();
            log.debug("javax.jcr.Item at {} was selected. Firing ItemSelectedEvent...", selectedItemId);
            eventBus.fireEvent(new ItemSelectedEvent(workspaceName, selectedItemId));
        } catch (Exception e) {
            // TODO dlipp - to check - throwing an Exception should be sufficient...
            shell.showError("An error occurred while selecting a row in the data grid", e);
        }
    }

    public String getSelectedItemId() {
        return selectedItemId;
    }

}
