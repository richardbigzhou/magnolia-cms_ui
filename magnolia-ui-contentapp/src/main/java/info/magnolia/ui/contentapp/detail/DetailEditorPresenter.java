/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.contentapp.detail;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.definition.EditorDefinition;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.HashMap;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Panel;

/**
 * Presenter for the workbench displayed in the {@link info.magnolia.ui.contentapp.detail.DetailSubApp}.
 * Contains the {@link ActionbarPresenter} for handling action events and the {@link DetailPresenter} for displaying the actual item.
 */
public class DetailEditorPresenter implements DetailEditorView.Listener, ActionbarPresenter.Listener {

    private static final Logger log = LoggerFactory.getLogger(DetailEditorPresenter.class);

    private final ActionExecutor actionExecutor;
    private final AppContext appContext;
    private final SubAppContext subAppContext;
    private final DetailEditorView view;
    private final DetailPresenter detailPresenter;
    private final ActionbarPresenter actionbarPresenter;
    private final DetailSubAppDescriptor subAppDescriptor;
    private final EditorDefinition editorDefinition;
    private final VersionManager versionManager;
    private final SimpleTranslator i18n;
    private String nodePath;

    @Inject
    public DetailEditorPresenter(final ActionExecutor actionExecutor, final SubAppContext subAppContext, final DetailEditorView view, final DetailPresenter detailPresenter, final ActionbarPresenter actionbarPresenter, final VersionManager versionManager, final SimpleTranslator i18n) {
        this.actionExecutor = actionExecutor;
        this.view = view;
        this.detailPresenter = detailPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.appContext = subAppContext.getAppContext();
        this.subAppContext = subAppContext;
        this.subAppDescriptor = (DetailSubAppDescriptor) subAppContext.getSubAppDescriptor();
        this.editorDefinition = subAppDescriptor.getEditor();
        this.versionManager = versionManager;
        this.i18n = i18n;
    }

    /**
     * @deprecated since 5.1 - use {@link DetailEditorPresenter(ActionExecutor, SubAppContext, DetailEditorView, DetailPresenter, ActionbarPresenter, VersionManager, SimpleTranslator)} instead.
     */
    @Deprecated
    public DetailEditorPresenter(final ActionExecutor actionExecutor, final SubAppContext subAppContext, final DetailEditorView view, final DetailPresenter detailPresenter, final ActionbarPresenter actionbarPresenter, final SimpleTranslator i18n) {
        this.actionExecutor = actionExecutor;
        this.view = view;
        this.detailPresenter = detailPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.appContext = subAppContext.getAppContext();
        this.subAppContext = subAppContext;
        this.subAppDescriptor = (DetailSubAppDescriptor) subAppContext.getSubAppDescriptor();
        this.editorDefinition = subAppDescriptor.getEditor();
        this.versionManager = Components.getComponent(VersionManager.class);
        this.i18n = i18n;
    }

    public View start(String nodePath, DetailView.ViewType viewType) {
        return start(nodePath, viewType, null);
    }

    public View start(String nodePath, DetailView.ViewType viewType, String versionName) {
        view.setListener(this);
        this.nodePath = nodePath;
        JcrNodeAdapter item;
        try {
            Session session = MgnlContext.getJCRSession(editorDefinition.getWorkspace());
            if (session.nodeExists(nodePath) && session.getNode(nodePath).getPrimaryNodeType().getName().equals(editorDefinition.getNodeType().getName())) {
                Node node = SessionUtil.getNode(editorDefinition.getWorkspace(), nodePath);
                // Get versioned item if version name was provided
                // Only show version if in VIEW mode
                if (StringUtils.isNotEmpty(versionName) && DetailView.ViewType.VIEW.equals(viewType)) {
                    Version version = versionManager.getVersion(node, versionName);
                    item = new JcrNodeAdapter(version.getFrozenNode());
                } else {
                    item = new JcrNodeAdapter(node);
                }
            } else {
                String parentPath = StringUtils.substringBeforeLast(nodePath, "/");
                parentPath = parentPath.isEmpty() ? "/" : parentPath;
                Node parent = session.getNode(parentPath);
                item = new JcrNewNodeAdapter(parent, editorDefinition.getNodeType().getName());
            }
        } catch (RepositoryException e) {
            log.warn("Not able to create an Item based on the following path {} ", nodePath, e);
            throw new RuntimeException(e);
        }

        DetailView itemView = detailPresenter.start(editorDefinition, item, viewType);

        view.setItemView(itemView);
        actionbarPresenter.setListener(this);
        ActionbarView actionbar = actionbarPresenter.start(subAppDescriptor.getActionbar());

        view.setActionbarView(actionbar);

        detailPresenter.addShortcut(new CloseEditorAfterConfirmationShortcutListener(KeyCode.ESCAPE, itemView));
        detailPresenter.addShortcut(new CommitDialogShortcutListener(KeyCode.ENTER));

        return view;
    }

    public View update(DetailLocation location) {
        return this.start(location.getNodePath(), location.getViewType(), location.getVersion());
    }

    public String getNodePath() {
        return nodePath;
    }

    public ActionbarPresenter getActionbarPresenter() {
        return actionbarPresenter;
    }

    @Override
    public void onViewTypeChanged(final DetailView.ViewType viewType) {
        // eventBus.fireEvent(new ViewTypeChangedEvent(viewType));
    }

    @Override
    public void onActionbarItemClicked(String actionName) {
        try {
            Session session = MgnlContext.getJCRSession(editorDefinition.getWorkspace());
            final javax.jcr.Item item = session.getItem(nodePath);
            if (item.isNode()) {
                actionExecutor.execute(actionName, new JcrNodeAdapter((Node)item));
            } else {
                throw new IllegalArgumentException("Selected value is not a node. Can only operate on nodes.");
            }

        } catch (RepositoryException e) {
            Message error = new Message(MessageType.ERROR, i18n.translate("ui-contentapp.detailEditorPresenter.error.repository", nodePath), e.getMessage());
            appContext.sendLocalMessage(error);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, i18n.translate("ui-contentapp.error.action.execution"), e.getMessage());
            appContext.sendLocalMessage(error);
        }
    }

    @Override
    public String getLabel(String actionName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
        return actionDefinition != null ? actionDefinition.getLabel() : null;
    }

    @Override
    public String getIcon(String actionName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
        return actionDefinition != null ? actionDefinition.getIcon() : null;
    }

    /**
     * A shortcut listener which opens a confirmation to confirm closing the DetailEditor.
     */
    protected class CloseEditorAfterConfirmationShortcutListener extends ShortcutListener {

        private final View itemView;

        public CloseEditorAfterConfirmationShortcutListener(int keyCode, View itemView, int... modifierKey) {
            super("", keyCode, modifierKey);
            this.itemView = itemView;
        }

        @Override
        public void handleAction(Object sender, Object target) {
            subAppContext.openConfirmation(
                    MessageStyleTypeEnum.WARNING, i18n.translate("ui-contentapp.detailEditorPresenter.closeConfirmation.title"), i18n.translate("ui-dialog.closeConfirmation.body"), i18n.translate("ui-dialog.closeConfirmation.confirmButton"), i18n.translate("ui-dialog.cancelButton"), false,
                    new ConfirmationCallback() {
                        @Override
                        public void onSuccess() {
                            detailPresenter.onActionFired(BaseDialog.CANCEL_ACTION_NAME, new HashMap<String, Object>());
                        }

                        @Override
                        public void onCancel() {
                            if (itemView.asVaadinComponent() instanceof Panel) {
                                ((Panel) itemView.asVaadinComponent()).focus();
                            }
                        }
                    });
        }
    }

    /**
     * A shortcut listener used to commit the DetailEditor if a text area does not have focus.
     */
    protected class CommitDialogShortcutListener extends ShortcutListener {

        public CommitDialogShortcutListener(int keyCode, int... modifierKey) {
            super("", keyCode, modifierKey);
        }

        @Override
        public void handleAction(Object sender, Object target) {
            // textareas are excluded on the client-side, see 'EnterFriendlyShortcutActionHandler', used in PanelConnector
            detailPresenter.onActionFired(BaseDialog.COMMIT_ACTION_NAME, new HashMap<String, Object>());
        }
    }

}
