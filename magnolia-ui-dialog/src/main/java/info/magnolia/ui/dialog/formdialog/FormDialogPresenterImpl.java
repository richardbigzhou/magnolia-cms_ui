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
package info.magnolia.ui.dialog.formdialog;

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.dialog.Dialog;
import info.magnolia.ui.dialog.DialogCloseHandler;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.actionarea.DialogActionExecutor;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;

/**
 * Presenter for forms opened inside dialogs.
 */
public class FormDialogPresenterImpl extends BaseDialogPresenter implements FormDialogPresenter, EditorValidator {

    private EditorCallback callback;

    private DialogDefinitionRegistry dialogDefinitionRegistry;

    private FormBuilder formBuilder;

    private FormView formView;

    private Item item;

    @Inject
    public FormDialogPresenterImpl(final DialogDefinitionRegistry dialogDefinitionRegistry, FormBuilder formBuilder, ComponentProvider componentProvider, DialogActionExecutor executor, FormView view, I18nizer i18nizer, SimpleTranslator i18n) {
        super(componentProvider, executor, view, i18nizer, i18n);
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.formBuilder = formBuilder;
        this.componentProvider = componentProvider;
        this.formView = view;
    }

    @Override
    public DialogView start(final Item item, String dialogId, final UiContext uiContext, EditorCallback callback) {
        try {
            FormDialogDefinition dialogDefinition = dialogDefinitionRegistry.getDialogDefinition(dialogId);
            return start(item, dialogDefinition, uiContext, callback);
        } catch (RegistrationException e) {
            throw new RuntimeException("No dialogDefinition found for " + dialogId, e);
        }
    }

    /**
     * Returns a {@link DialogView} containing {@link FormView} as content.
     * <ul>
     * <li>Sets the created {@link FormView} as content of the created {@link DialogView}.</li>
     * </ul>
     *
     * @param item passed on to{@link info.magnolia.ui.dialog.formdialog.FormDialogPresenter}
     * @param dialogDefinition
     * @param uiContext
     */
    @Override
    public DialogView start(final Item item, FormDialogDefinition dialogDefinition, final UiContext uiContext, EditorCallback callback) {
        start(dialogDefinition, uiContext);
        this.callback = callback;
        this.item = item;

        getExecutor().setDialogDefinition(getDefinition());

        buildView((FormDialogDefinition) getDefinition());

        final OverlayCloser overlayCloser = uiContext.openOverlay(getView());
        getView().addDialogCloseHandler(new DialogCloseHandler() {
            @Override
            public void onDialogClose(DialogView dialogView) {
                overlayCloser.close();
            }
        });
        getView().setClosable(true);
        return getView();
    }

    private void buildView(FormDialogDefinition dialogDefinition) {
        final Dialog dialog = new Dialog(dialogDefinition);

        formBuilder.buildForm(getView(), dialogDefinition.getForm(), item, dialog);

        final String description = dialogDefinition.getDescription();
        final String label = dialogDefinition.getLabel();

        if (StringUtils.isNotBlank(description) && !isMessageKey(description)) {
            getView().setDescription(description);
        }

        if (StringUtils.isNotBlank(label) && !isMessageKey(label)) {
            getView().setCaption(label);
        }
    }

    @Override
    public FormView getView() {
        return formView;
    }

    @Override
    public void showValidation(boolean visible) {
        getView().showValidation(visible);
    }

    @Override
    public boolean isValid() {
        return getView().isValid();
    }

    @Override
    protected DialogActionExecutor getExecutor() {
        return (DialogActionExecutor) super.getExecutor();
    }

    @Override
    protected Iterable<ActionDefinition> filterActions() {
        List<ActionDefinition> result = new LinkedList<ActionDefinition>();
        boolean isJcrItemAdapter = (item instanceof JcrItemAdapter);
        for (ActionDefinition action : getDefinition().getActions().values()) {
            if (!isJcrItemAdapter || getExecutor().isAvailable(action.getName(), ((JcrItemAdapter) item).getJcrItem())) {
                result.add(action);
            }
        }
        return result;
    }

    @Override
    protected Object[] getActionParameters(String actionName) {
        return new Object[] { this, item, callback };
    }

    /**
     * @deprecated is a hack and should not be used. See MGNLUI-2207.
     */
    private boolean isMessageKey(final String text) {
        return !text.contains(" ") && text.contains(".") && !text.endsWith(".");
    }

}
