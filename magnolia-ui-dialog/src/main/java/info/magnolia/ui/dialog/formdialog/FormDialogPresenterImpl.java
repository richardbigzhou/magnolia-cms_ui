/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
import info.magnolia.ui.api.availability.AvailabilityChecker;
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
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Presenter for forms opened inside dialogs.
 */
public class FormDialogPresenterImpl extends BaseDialogPresenter implements FormDialogPresenter, EditorValidator {

    private static final Logger log = LoggerFactory.getLogger(FormDialogPresenterImpl.class);

    private EditorCallback callback;

    private DialogDefinitionRegistry dialogDefinitionRegistry;

    private ContentConnector contentConnector;

    private AvailabilityChecker checker;

    private FormBuilder formBuilder;

    private FormView formView;

    private Item item;

    /**
     * Constructor backwards compatible with pre-5.3 versions.
     * @deprecated since version 5.3.1, use {@link #FormDialogPresenterImpl(DialogDefinitionRegistry, FormBuilder, ComponentProvider, DialogActionExecutor, FormView, I18nizer, SimpleTranslator, AvailabilityChecker, ContentConnector)} instead.
     */
    @Deprecated
    public FormDialogPresenterImpl(DialogDefinitionRegistry dialogDefinitionRegistry, FormBuilder formBuilder, ComponentProvider componentProvider, DialogActionExecutor executor, FormView view, I18nizer i18nizer, SimpleTranslator i18n) {
        this(dialogDefinitionRegistry, formBuilder, componentProvider, executor, view, i18nizer, i18n, componentProvider.getComponent(AvailabilityChecker.class), componentProvider.getComponent(ContentConnector.class));
    }

    @Inject
    public FormDialogPresenterImpl(final DialogDefinitionRegistry dialogDefinitionRegistry, FormBuilder formBuilder, ComponentProvider componentProvider, DialogActionExecutor executor, FormView view, I18nizer i18nizer, SimpleTranslator i18n, AvailabilityChecker checker, ContentConnector contentConnector) {
        super(componentProvider, executor, view, i18nizer, i18n);
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.formBuilder = formBuilder;
        this.checker = checker;
        this.contentConnector = contentConnector;
        this.componentProvider = componentProvider;
        this.formView = view;
    }

    @Override
    public DialogView start(Item item, FormDialogDefinition dialogDefinition, UiContext uiContext, EditorCallback callback, ContentConnector contentConnector) {
        this.contentConnector = contentConnector;
        return start(item, dialogDefinition, uiContext, callback);
    }

    @Override
    public DialogView start(Item item, String dialogId, final UiContext uiContext, EditorCallback callback) {
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
     * @param item
     * @param dialogDefinition
     * @param uiContext
     */
    @Override
    public DialogView start(Item item, FormDialogDefinition dialogDefinition, final UiContext uiContext, EditorCallback callback) {
        this.callback = callback;
        this.item = item;

        super.start(dialogDefinition, uiContext);
        getExecutor().setDialogDefinition(getDefinition());
        buildView(getDefinition());

        final OverlayCloser overlayCloser = uiContext.openOverlay(getView(), getView().getModalityLevel());
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
        List<ActionDefinition> actions = new ArrayList<ActionDefinition>(getDefinition().getActions().values());
        Iterator<ActionDefinition> it = actions.iterator();
        Object itemId = contentConnector.getItemId(item);
        if (itemId != null) {
            while (it.hasNext()) {
                ActionDefinition action = it.next();
                if (!checker.isAvailable(action.getAvailability(), Arrays.asList(itemId))) {
                    it.remove();
                }
            }
        } else {
            log.info("Could not resolve itemId for item {}:{}, will not restrict availability of dialog actions.", item.getClass().getName(), item);
        }
        return actions;
    }

    @Override
    protected Object[] getActionParameters(String actionName) {
        return new Object[] { this, item, callback };
    }

    @Override
    protected FormDialogDefinition getDefinition() {
        return (FormDialogDefinition) super.getDefinition();
    }

    /**
     * @deprecated is a hack and should not be used. See MGNLUI-2207.
     */
    private boolean isMessageKey(final String text) {
        return !text.contains(" ") && text.contains(".") && !text.endsWith(".");
    }

    @Override
    protected void onCancel () {
        if (callback != null) {
            callback.onCancel();
        }
    }
}
