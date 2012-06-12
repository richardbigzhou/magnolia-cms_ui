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
package info.magnolia.ui.admincentral.workbench;

import info.magnolia.context.MgnlContext;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.admincentral.workbench.event.ItemSelectedEvent;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.model.workbench.registry.WorkbenchDefinitionRegistry;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;
import info.magnolia.ui.vaadin.intergration.jcr.JcrItem;
import info.magnolia.ui.widget.dialog.event.DialogCommitEvent;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.ComponentContainer;


/**
 * TODO write javadoc.
 * @version $Id$
 */
@SuppressWarnings("serial")
public class Workbench implements IsVaadinComponent, WorkbenchView.Presenter {

    private static final Logger log = LoggerFactory.getLogger(Workbench.class);

    private WorkbenchDefinition workbenchDefinition;

    private final WorkbenchDefinitionRegistry workbenchRegistry;

    private final WorkbenchView view;

    private final EventBus eventBus;

    private final MagnoliaShell shell;

    private final WorkbenchActionFactory actionFactory;

    private String selectedItemPath;

    @Inject
    public Workbench(final WorkbenchView view, final EventBus eventbus, final MagnoliaShell shell, final WorkbenchDefinitionRegistry workbenchRegistry, final WorkbenchActionFactory actionFactory) {
        super();
        this.view = view;
        this.eventBus = eventbus;
        this.shell = shell;
        this.workbenchRegistry = workbenchRegistry;
        this.actionFactory = actionFactory;

        view.setPresenter(this);
        eventbus.addHandler(DialogCommitEvent.class, new DialogCommitEvent.Handler() {

            @Override
            public void onDialogCommit(DialogCommitEvent event) {
                try {
                    final Node node = ((JcrItem) event.getItem()).getNode();
                    node.getSession().save();
                    view.refreshNode(node);
                }
                catch (RepositoryException e) {
                    log.error("Node update failed with exception: {}", e.getMessage());
                }
            }
        });
    }

    public void initWorkbench(final String id) {
        // load the workbench specific configuration if existing
        try {
            workbenchDefinition = workbenchRegistry.get(id);
        } catch (RegistrationException e) {
            shell.showError("An error occurred while trying to get workbench [" + id + "] in the registry", e);
            return;
        }
        view.initWorkbench(workbenchDefinition);
    }

    @Override
    public ComponentContainer asVaadinComponent() {
        return view;
    }

    @Override
    public void onActionbarItemClicked(ActionDefinition actionDefinition) {
        if (actionDefinition != null) {
            try {
                Session session = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace());
                if(!session.itemExists(selectedItemPath)) {
                    log.debug("{} does not exist anymore. Was it just deleted? Resetting path to root...", selectedItemPath);
                    selectedItemPath = "/";
                }
                Item item = session.getItem(selectedItemPath);
                Action action = actionFactory.createAction(actionDefinition, item);
                action.execute();
                view.refresh();
            } catch (PathNotFoundException e) {
                shell.showError("Can't execute action.\n" + e.getMessage(), e);
            } catch (LoginException e) {
                shell.showError("Can't execute action.\n" + e.getMessage(), e);
            } catch (RepositoryException e) {
                shell.showError("Can't execute action.\n" + e.getMessage(), e);
            } catch (ActionExecutionException e) {
                shell.showError("Can't execute action.\n" + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onItemSelected(Item item) {
        if (item == null) {
            log.warn("Got null javax.jcr.Item. No ItemSelectedEvent will be fired.");
            return;
        }
        try {
            // FIXME this seemed to be triggered twice both for click row event and tableValue
            // change even when no value has changed and only a click happened on table, see
            // info.magnolia.ui.admincentral.tree.view.TreeViewImpl.TreeViewImpl
            // and jcrBrowser internal obj registering for those events.
            selectedItemPath = item.getPath();
            log.info("javax.jcr.Item at {} was selected. Firing ItemSelectedEvent...", item.getPath());
            eventBus.fireEvent(new ItemSelectedEvent(item.getSession().getWorkspace().getName(), item.getPath()));
        } catch (RepositoryException e) {
            shell.showError("An error occurred while selecting a row in the data grid", e);
        }
    }

}
