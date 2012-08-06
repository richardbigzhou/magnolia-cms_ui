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

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Embedded;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.ContentAppDescriptor;
import info.magnolia.ui.admincentral.content.view.ContentPresenter;
import info.magnolia.ui.admincentral.dialog.action.EditDialogActionDefinition;
import info.magnolia.ui.admincentral.event.ActionbarClickEvent;
import info.magnolia.ui.admincentral.event.ContentChangedEvent;
import info.magnolia.ui.admincentral.event.DoubleClickEvent;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
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
import info.magnolia.ui.widget.actionbar.ActionbarView;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


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

    final ContentPresenter contentPresenter;

    final ActionbarPresenter actionbarPresenter;

    final static String PHOTO_NODE_NAME = "photo";


    @Inject
    public ContentWorkbenchSubApp(final AppContext context, final ContentWorkbenchView view, final EventBus eventbus, final Shell shell, final WorkbenchActionFactory actionFactory, final ContentPresenter contentPresenter, final ActionbarPresenter actionbarPresenter) {
        this.view = view;
        this.eventBus = eventbus;
        this.shell = shell;
        this.actionFactory = actionFactory;
        this.contentPresenter = contentPresenter;
        this.actionbarPresenter = actionbarPresenter;

        workbenchDefinition = ((ContentAppDescriptor) context.getAppDescriptor()).getWorkbench();
        contentPresenter.initContentView(view);
        view.setListener(this);
        actionbarPresenter.initActionbar(workbenchDefinition.getActionbar());
        view.setActionbarView(actionbarPresenter.start());

        bindHandlers();
    }

    private void bindHandlers() {
        eventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                // this should go into the content presenter
                view.refresh();
            }
        });

        eventBus.addHandler(ActionbarClickEvent.class, new ActionbarClickEvent.Handler() {

            @Override
            public void onActionbarItemClicked(ActionbarClickEvent event) {

                ActionDefinition actionDefinition = event.getActionDefinition();
                createAndExecuteAction(actionDefinition);
            }
        });

        eventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            private final String[] previews = new String[]{"img/previews/about-200.png", "img/previews/demo-project-200.png"};

            private final String[] previewAlts = new String[]{"About page", "Demo Project"};

            private final String[] pageActions = new String[]{
                "addSubpage",
                "deletePage",
                "previewPage",
                "editPage",
                "editPageProperties",
                "movePage",
                "duplicatePage"};

            private final String[] contactsActions = new String[]{
                "newContact",
                "editContact",
                "delete"};

            private int previewIndex = 0;

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                // refresh action bar (context sensitivity)
                if (event.getPath() != null) {
                    actionbarPresenter.enable("deletePage");
                } else {
                    actionbarPresenter.disable("deletePage");
                }

                // too lazy to add dead weight to ActionbarPresenter...
                String[] actions = event.getWorkspace().equals("website") ? pageActions : contactsActions;
                ActionbarView actionbar = actionbarPresenter.start();
                if (previewIndex == 0) {
                    actionbar.disableGroup(String.valueOf(previewIndex));
                    actionbar.enable(actions[(int) (Math.random() * actions.length)]);
                } else {
                    actionbar.enableGroup(String.valueOf(previewIndex));
                    actionbar.disable(actions[(int) (Math.random() * actions.length)]);
                }

                // refresh preview like this
                // you can show images using vaadin Embedded widget, or use any other widget
                if (event.getPath() == null) {
                    actionbarPresenter.setPreview(null);
                } else {

                    final Node parentNode = SessionUtil.getNode(event.getWorkspace(), event.getPath());
                    try {

                        if(!parentNode.hasNode(PHOTO_NODE_NAME)) {
                            actionbarPresenter.setPreview(null);
                            return;
                        }

                        final Node node= parentNode.getNode(PHOTO_NODE_NAME);
                        final byte[] pngData = IOUtils.toByteArray(node.getProperty(JcrConstants.JCR_DATA).getBinary().getStream());
                        final String nodeType = node.getProperty(FileProperties.CONTENT_TYPE).getString();

                        Resource imageResource = new StreamResource(
                                new StreamResource.StreamSource() {
                                    @Override
                                    public InputStream getStream() {
                                        return new ByteArrayInputStream(pngData);

                                    }
                                }, "", ContentWorkbenchSubApp.this.asView().asVaadinComponent().getApplication()){
                            @Override
                            public String getMIMEType() {
                                    return nodeType;
                            }
                        };

                        Embedded preview = new Embedded(null, imageResource);
                        actionbarPresenter.setPreview(preview);
                    } catch (RepositoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            }
        });

        eventBus.addHandler(DoubleClickEvent.class, new DoubleClickEvent.Handler() {

            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                EditDialogActionDefinition editDialogActionDefinition = new EditDialogActionDefinition();
                editDialogActionDefinition.setDialogName("ui-pages-app:pages");
                createAndExecuteAction(editDialogActionDefinition);
            }
        });
    }

    @Override
    public View start() {
        return view;
    }

    /**
     * @see ContentPresenter#getSelectedItemId()
     */
    public String getSelectedItemId() {
        return contentPresenter.getSelectedItemId();
    }

    @Override
    public String getCaption() {
        return "Content-Workbench";
    }

    public ContentWorkbenchView asView() {
        return view;
    }

    private void createAndExecuteAction(final ActionDefinition actionDefinition) {
        if (actionDefinition == null) {
            log.warn("Action definition cannot be null. Will do nothing.");
        }
        try {
            final Session session = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace());
            String selectedItemId = getSelectedItemId();
            if (selectedItemId == null || !session.itemExists(selectedItemId)) {
                log.debug("{} does not exist anymore. Was it just deleted? Resetting path to root...", selectedItemId);
                selectedItemId = "/";
            }
            final javax.jcr.Item item = session.getItem(selectedItemId);
            final Action action = this.actionFactory.createAction(actionDefinition, item);
            action.execute();
        } catch (PathNotFoundException e) {
            this.shell.showError("Can't execute action.\n" + e.getMessage(), e);
        } catch (LoginException e) {
            this.shell.showError("Can't execute action.\n" + e.getMessage(), e);
        } catch (RepositoryException e) {
            shell.showError("Can't execute action.\n" + e.getMessage(), e);
        } catch (ActionExecutionException e) {
            this.shell.showError("Can't execute action.\n" + e.getMessage(), e);
        }
    }

}