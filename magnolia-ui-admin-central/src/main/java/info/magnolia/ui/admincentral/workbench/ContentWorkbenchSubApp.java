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
import info.magnolia.ui.admincentral.app.content.ContentAppDescriptor;
import info.magnolia.ui.admincentral.event.ContentChangedEvent;
import info.magnolia.ui.admincentral.jcr.view.ContentPresenter;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.LoginException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The workbench is a core component of AdminCentral. It represents the main hub through which users
 * can interact with JCR data. It is compounded by three main sub-components:
 * <ul>
 * <li>a configurable data grid.
 * <li>a configurable function toolbar on top of the data grid, providing operations such as
 * switching from tree to list view or performing searches on data.
 * <li>a configurable action bar on the right hand side, showing the available operations for the
 * given workspace and the selected item.
 * </ul>
 *
 * <p>
 * Its main configuration point is the {@link WorkbenchDefinition} through which one defines the JCR
 * workspace to connect to, the columns/properties to display, the available actions and so on.
 */
public class ContentWorkbenchSubApp implements SubApp, ContentWorkbenchView.Listener {

    private static final Logger log = LoggerFactory.getLogger(ContentWorkbenchSubApp.class);

    private final WorkbenchDefinition workbenchDefinition;

    private final ContentWorkbenchView view;

    private final EventBus eventBus;

    private final Shell shell;

    private final WorkbenchActionFactory actionFactory;

    private final Map<String, ActionDefinition> actions = new HashMap<String, ActionDefinition>();

    final ContentPresenter contentPresenter;

    @Inject
    public ContentWorkbenchSubApp(final AppContext context, final ContentWorkbenchView view, final EventBus eventbus, final Shell shell, final WorkbenchActionFactory actionFactory, final ContentPresenter contentPresenter) {
        this.view = view;
        this.eventBus = eventbus;
        this.shell = shell;
        this.actionFactory = actionFactory;
        this.contentPresenter = contentPresenter;

        workbenchDefinition = ((ContentAppDescriptor) context.getAppDescriptor()).getWorkbench();

        eventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                // this should go into the presenter of the treegrid
                view.refresh();
            }
        });
    }

    /**
     * TODO dlipp - id is currently ignored: to be checked whether we can really drop it!
     */
    public void initWorkbench(final String id) {
        contentPresenter.initContentView(view);
        view.setListener(this);
        view.initActionbar(workbenchDefinition.getActionbar());
    }

    /**
     * TODO dlipp - what should happen in here and who's responsibility is it to call it? Maybe the above initWorkbench can be dropped in favor of this method?
     */
    @Override
    public View start() {
        return view;
    }

    public String getSelectedItemId() {
        return contentPresenter.getSelectedItemId();
    }

    @Override
    public String getCaption() {
        return "Content-Workbench";
    }


    //
    // ACTIONBAR PRESENTER
    //

    @Override
    public Map<String, ActionDefinition> getActions() {
        return actions;
    }

    @Override
    public void addAction(String actionName, ActionDefinition actionDefinition) {
        if (StringUtils.isNotBlank(actionName)) {
            actions.put(actionName, actionDefinition);
        }
    }

    @Override
    public void onActionbarItemClicked(final String actionName) {
        ActionDefinition actionDefinition = getActions().get(actionName);
        if (actionDefinition != null) {
            try {
                Session session = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace());
                final String selectedItemId = getSelectedItemId();
                if (selectedItemId == null || !session.itemExists(selectedItemId)) {
                    log.debug("{} does not exist anymore. Was it just deleted? Resetting path to root...", selectedItemId);
                }
                final javax.jcr.Item item = session.getItem(selectedItemId);
                Action action = actionFactory.createAction(actionDefinition, item, this);
                action.execute();
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

    public ContentWorkbenchView asView() {
        return view;
    }
}
