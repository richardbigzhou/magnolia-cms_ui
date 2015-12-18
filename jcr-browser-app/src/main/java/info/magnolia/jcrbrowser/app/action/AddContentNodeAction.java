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
package info.magnolia.jcrbrowser.app.action;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.framework.action.AddNodeAction;
import info.magnolia.ui.framework.action.AddNodeActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.util.PropertysetItem;

/**
 * Opens a form dialog which allows for populating the new item properties such as:
 * <ul>
 * <li>node name</li>
 * <li>node type</li>
 * </ul>
 *
 * Should the dialog be successfully populated - a new node is created via a delegate {@link AddNodeAction}.
 *
 * @see AddContentNodeActionDefinition
 */
public class AddContentNodeAction extends AbstractAction<AddContentNodeActionDefinition> {

    public static final String NAME_PID = "name";
    public static final String TYPE_PID = "type";
    private final JcrItemAdapter item;
    private final UiContext uiContext;
    private final FormDialogPresenterFactory formDialogPresenterFactory;
    private final SimpleTranslator i18n;
    private final EventBus eventBus;

    @Inject
    public AddContentNodeAction(AddContentNodeActionDefinition definition, JcrItemAdapter item, UiContext uiContext, FormDialogPresenterFactory formDialogPresenterFactory, SimpleTranslator i18n, @Named(AdmincentralEventBus.NAME) EventBus eventBus) {
        super(definition);
        this.item = item;
        this.uiContext = uiContext;
        this.formDialogPresenterFactory = formDialogPresenterFactory;
        this.i18n = i18n;
        this.eventBus = eventBus;
    }

    @Override
    public void execute() throws ActionExecutionException {
        String dialogName = getDefinition().getDialogName();
        if (StringUtils.isBlank(dialogName)) {
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, i18n.translate("ui-framework.actions.no.dialog.definition", getDefinition().getName()));
            return;
        }

        final FormDialogPresenter formDialogPresenter = formDialogPresenterFactory.createFormDialogPresenter(dialogName);

        if (formDialogPresenter == null) {
            uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, i18n.translate("ui-framework.actions.dialog.not.registered", dialogName));
            return;
        }

        final PropertysetItem nodeTypeInfo = new PropertysetItem();
        formDialogPresenter.start(nodeTypeInfo, getDefinition().getDialogName(), uiContext, new EditorCallback() {

            @Override
            public void onSuccess(String actionName) {
                final AddNodeActionDefinition addNodeActionDefinition = new AddNodeActionDefinition();

                final String nodeName = (String) nodeTypeInfo.getItemProperty(NAME_PID).getValue();
                final String nodeType = (String) nodeTypeInfo.getItemProperty(TYPE_PID).getValue();

                addNodeActionDefinition.setBaseName(nodeName);
                addNodeActionDefinition.setNodeType(nodeType);

                try {
                    new AddNodeAction(addNodeActionDefinition, item, eventBus).execute();
                } catch (ActionExecutionException e) {
                    uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, i18n.translate("jcr-browser.actions.node.creation.failed", nodeName, nodeType, e.getMessage()));
                } finally {
                    formDialogPresenter.closeDialog();
                }

            }

            @Override
            public void onCancel() {
                formDialogPresenter.closeDialog();
            }
        });
    }
}
