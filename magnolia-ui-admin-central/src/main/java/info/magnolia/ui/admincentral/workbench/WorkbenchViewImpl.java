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
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.jcr.view.JcrView.ViewType;
import info.magnolia.ui.admincentral.jcr.view.builder.JcrViewBuilderProvider;
import info.magnolia.ui.admincentral.tree.action.TreeAction;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.admincentral.workbench.event.ItemSelectedEvent;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.model.workbench.registry.WorkbenchDefinitionRegistry;
import info.magnolia.ui.widget.actionbar.ActionButton;
import info.magnolia.ui.widget.actionbar.Actionbar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;


/**
 * TODO write javadoc.
 * @version $Id$
 */
@SuppressWarnings("serial")
public class WorkbenchViewImpl extends CustomComponent implements WorkbenchView {

    private static final Logger log = LoggerFactory.getLogger(WorkbenchViewImpl.class);

    private final VerticalLayout root = new VerticalLayout();

    private Presenter presenter;

    private final HorizontalLayout split = new HorizontalLayout();

    private final HorizontalLayout toolbar = new HorizontalLayout();

    private JcrView jcrView;

    private final JcrViewBuilderProvider jcrViewBuilderProvider;

    private final WorkbenchDefinitionRegistry workbenchRegistry;

    private final WorkbenchActionFactory actionFactory;

    protected String path = "/";

    private final Shell shell;

    private final EventBus eventBus;

    private Item selectedItem;

    private final Map<String, ActionbarItemDefinition> actions = new LinkedHashMap<String, ActionbarItemDefinition>();

    private final JcrView.Presenter jcrPresenter = new JcrView.Presenter() {

        @Override
        public void onItemSelection(Item item) {
            if (item == null) {
                log.warn("Got null javax.jcr.Item. No ItemSelectedEvent will be fired.");
                return;
            }
            try {
                // FIXME this seemed to be triggered twice both for click row event and tableValue
                // change even when no value has changed and only a click happened on table, see
                // info.magnolia.ui.admincentral.tree.view.TreeViewImpl.TreeViewImpl
                // and jcrBrowser internal obj registering for those events.
                selectedItem = item;
                log.info("javax.jcr.Item at {} was selected. Firing ItemSelectedEvent...", item.getPath());
                eventBus.fireEvent(new ItemSelectedEvent(item.getSession().getWorkspace().getName(), item.getPath()));
            }
            catch (RepositoryException e) {
                shell.showError("An error occurred while selecting a row in the data grid", e);
            }
        };
    };

    @Inject
    public WorkbenchViewImpl(WorkbenchDefinitionRegistry workbenchRegistry, Shell shell, JcrViewBuilderProvider jcrViewBuilderProvider, WorkbenchActionFactory actionFactory, EventBus bus) {
        super();
        this.shell = shell;
        this.jcrViewBuilderProvider = jcrViewBuilderProvider;
        this.workbenchRegistry = workbenchRegistry;
        this.actionFactory = actionFactory;
        this.eventBus = bus;

        setSizeFull();
        root.setSizeFull();
        construct();
        setCompositionRoot(root);
    }

    @Override
    public void initWorkbench(final String id) {
        // load the workbench specific configuration if existing
        final WorkbenchDefinition workbenchDefinition;
        try {
            workbenchDefinition = workbenchRegistry.get(id);
        }
        catch (RegistrationException e) {
            log.error("An error occurred while trying to get workbench [{}] in the registry", id, e);
            shell.showError("An error occurred while trying to get workbench [" + id + "] in the registry", e);
            return;
        }
        jcrView = jcrViewBuilderProvider.getBuilder().build(workbenchDefinition, ViewType.TREE);

        jcrView.setPresenter(jcrPresenter);
        jcrView.select(path);
        jcrView.asVaadinComponent();
        split.addComponent(jcrView.asVaadinComponent());

        List<MenuItemDefinition> actions = buildActions(workbenchDefinition);
        // TODO provide actionBar with actions

        Actionbar bar = buildActionbar(workbenchDefinition.getActionbar());

        split.addComponent(bar);
        split.setExpandRatio(jcrView.asVaadinComponent(), 1f);
    }

    private List<MenuItemDefinition> buildActions(final WorkbenchDefinition workbenchDefinition) {
        final Item item;
        try {
            String normalizedPath = (workbenchDefinition.getPath()).replaceAll("//", "/");
            item = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace()).getItem(normalizedPath);

        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

        List<MenuItemDefinition> defs = workbenchDefinition.getActions();

        List<MenuItemDefinition> menuItemDefinitions = new ArrayList<MenuItemDefinition>();
        for (MenuItemDefinition menuDefinition : defs) {
            log.debug("adding definition for menu {}", menuDefinition.getName());
            // TODO an optimization here would be to use reflection to test if the action implements
            // TreeAction, instantiating it only to test this is a waste
            Action action = actionFactory.createAction(menuDefinition.getActionDefinition(), item);

            if (action instanceof TreeAction) {
                final TreeAction treeAction = (TreeAction) action;
                try {
                    if (treeAction.isAvailable(item)) {
                        menuItemDefinitions.add(menuDefinition);
                    }
                }
                catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
            }
            else {
                menuItemDefinitions.add(menuDefinition);
            }
        }
        return menuItemDefinitions;
    }

    private Actionbar buildActionbar(ActionbarDefinition actionbarDefinition) {
        Actionbar actionbar = new Actionbar();

        for (ActionbarSectionDefinition section : actionbarDefinition.getSections()) {
            for (ActionbarGroupDefinition group : section.getGroups()) {
                for (ActionbarItemDefinition item : group.getItems()) {

                    ActionButton button = new ActionButton(item.getLabel());
                    button.setIcon(new ThemeResource(item.getIcon()));

                    final String actionName = item.getName();
                    button.setActionName(actionName);
                    button.setGroupName(group.getName());
                    button.setSectionTitle(section.getTitle());

                    button.addListener(new ClickListener() {

                        @Override
                        public void buttonClick(ClickEvent event) {
                            System.out.println("actionbar item clicked");
                            getPresenter().onActionbarItemClicked(actionName);
                        }
                    });
                    actionbar.addComponent(button);
                    actions.put(actionName, item);
                }
            }
        }

        // actionbar.setDefinition(actionbarDefinition);
        return actionbar;
    }

    @Override
    public void executeAction(final String actionName) {
        ActionbarItemDefinition actionbarItemDefinition = actions.get(actionName);
        if (actionbarItemDefinition != null && selectedItem != null) {
            ActionDefinition actionDefinition = actionbarItemDefinition.getActionDefinition();
            if (actionDefinition != null) {
                Action action = actionFactory.createAction(actionDefinition, selectedItem);
                try {
                    action.execute();
                }
                catch (ActionExecutionException e) {
                    shell.showError("Can't execute action.\n" + e.getMessage(), e);
                }
            }
        }
    }

    private void construct() {
        split.setSizeFull();
        toolbar.setSizeUndefined();
        toolbar.addComponent(new Button("Tree"));
        toolbar.addComponent(new Button("List"));
        root.addComponent(toolbar);
        root.addComponent(split);
        root.setExpandRatio(split, 1f);
    }

    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

}
