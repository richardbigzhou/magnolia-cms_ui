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

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.ContentAppDescriptor;
import info.magnolia.ui.admincentral.content.view.ContentPresenter;
import info.magnolia.ui.admincentral.event.ActionbarClickEvent;
import info.magnolia.ui.admincentral.event.ContentChangedEvent;
import info.magnolia.ui.admincentral.event.DoubleClickEvent;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.thumbnail.AbstractThumbnailProvider;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.widget.actionbar.ActionbarView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Embedded;


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
@SuppressWarnings("serial")
public class ContentWorkbenchPresenter implements ContentWorkbenchView.Listener {

    protected final static String IMAGE_NODE_NAME = AbstractThumbnailProvider.ORIGINAL_IMAGE_NODE_NAME;

    private static final Logger log = LoggerFactory.getLogger(ContentWorkbenchPresenter.class);

    private final WorkbenchDefinition workbenchDefinition;

    private final ContentWorkbenchView view;

    private final EventBus admincentralEventBus;

    private final EventBus appEventBus;

    private final Shell shell;

    private final WorkbenchActionFactory actionFactory;

    private final ContentPresenter contentPresenter;

    private final ActionbarPresenter actionbarPresenter;

    @Inject
    public ContentWorkbenchPresenter(final AppContext appContext, final ContentWorkbenchView view, @Named("admincentral") final EventBus admincentralEventBus, @Named("app") final EventBus appEventBus, final Shell shell, final WorkbenchActionFactory actionFactory, final ContentPresenter contentPresenter, final ActionbarPresenter actionbarPresenter) {
        this.view = view;
        this.admincentralEventBus = admincentralEventBus;
        this.appEventBus = appEventBus;
        this.shell = shell;
        this.actionFactory = actionFactory;
        this.contentPresenter = contentPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.workbenchDefinition = ((ContentAppDescriptor) appContext.getAppDescriptor()).getWorkbench();
    }

    public ContentWorkbenchView start() {
        contentPresenter.initContentView(view);
        view.setListener(this);

        ActionbarView actionbar = actionbarPresenter.start(workbenchDefinition.getActionbar(), actionFactory);
        view.setActionbarView(actionbar);

        bindHandlers();

        return view;
    }

    private void bindHandlers() {
        admincentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                // this should go into the content presenter
                view.refresh();
            }
        });

        appEventBus.addHandler(ActionbarClickEvent.class, new ActionbarClickEvent.Handler() {

            @Override
            public void onActionbarItemClicked(ActionbarClickEvent event) {
                try {
                    ActionDefinition actionDefinition = event.getActionDefinition();
                    actionbarPresenter.createAndExecuteAction(actionDefinition, workbenchDefinition.getWorkspace(), getSelectedItemId());
                } catch (ActionExecutionException e) {
                    log.error("An error occurred while executing an action.", e);
                }
            }
        });

        appEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                // refresh action bar (context sensitivity)
                if (event.getPath() != null) {
                    actionbarPresenter.enable("delete");
                } else {
                    actionbarPresenter.disable("delete");
                }

                // if you want to enable/disable actions or groups
                // actionbarPresenter.enable(...);
                // actionbarPresenter.disable(...);
                // actionbarPresenter.disableGroup(...);
                // actionbarPresenter.enableGroup(...);

                // refresh preview
                if (event.getPath() == null) {
                    actionbarPresenter.setPreview(null);
                } else {

                    final Node parentNode = SessionUtil.getNode(event.getWorkspace(), event.getPath());
                    try {

                        if (!parentNode.hasNode(IMAGE_NODE_NAME)) {
                            actionbarPresenter.setPreview(null);
                            return;
                        }

                        final Node node = parentNode.getNode(IMAGE_NODE_NAME);
                        final byte[] pngData = IOUtils.toByteArray(node.getProperty(JcrConstants.JCR_DATA).getBinary().getStream());
                        final String nodeType = node.getProperty(FileProperties.CONTENT_TYPE).getString();

                        Resource imageResource = new StreamResource(
                            new StreamResource.StreamSource() {

                                @Override
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(pngData);

                                }
                            }, "", ContentWorkbenchPresenter.this.getView().asVaadinComponent().getApplication()) {

                            @Override
                            public String getMIMEType() {
                                return nodeType;
                            }
                        };

                        Embedded preview = new Embedded(null, imageResource);
                        actionbarPresenter.setPreview(preview);
                    } catch (RepositoryException e) {
                        log.error(e.getMessage(), e);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }

                }
            }
        });

        appEventBus.addHandler(DoubleClickEvent.class, new DoubleClickEvent.Handler() {

            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                executeDefaultAction();
            }
        });
    }

    /**
     * @see ContentPresenter#getSelectedItemId()
     */
    public String getSelectedItemId() {
        return contentPresenter.getSelectedItemId();
    }

    public ContentWorkbenchView getView() {
        return view;
    }

    /**
     * Executes the workbench's default action, as configured in the defaultAction property.
     */
    public void executeDefaultAction() {
        ActionDefinition defaultActionDef = actionbarPresenter.getDefaultActionDefinition();
        try {
            actionbarPresenter.createAndExecuteAction(defaultActionDef, workbenchDefinition.getWorkspace(), getSelectedItemId());
        } catch (ActionExecutionException e) {
            log.error("An error occurred while executing an action.", e);
        }
    }

    public void selectPath(String path) {
        this.view.selectPath(path);
    }
}
