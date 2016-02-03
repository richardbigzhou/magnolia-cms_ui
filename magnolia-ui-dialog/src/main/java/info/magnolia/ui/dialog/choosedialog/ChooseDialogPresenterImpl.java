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
package info.magnolia.ui.dialog.choosedialog;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.dialog.DialogCloseHandler;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.actionarea.DialogActionExecutor;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.vaadin.integration.NullItem;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

/**
 * Factory for creating workbench choose dialog presenters.
 */
public class ChooseDialogPresenterImpl extends BaseDialogPresenter implements ChooseDialogPresenter {

    private static final Logger log = LoggerFactory.getLogger(ChooseDialogPresenterImpl.class);

    private FieldFactoryFactory fieldFactoryFactory;

    private I18nContentSupport i18nContentSupport;

    private Item item;

    private ChooseDialogCallback callback;

    private Field<Object> field;

    @Inject
    public ChooseDialogPresenterImpl(
            FieldFactoryFactory fieldFactoryFactory,
            ComponentProvider componentProvider,
            I18nContentSupport i18nContentSupport,
            DialogActionExecutor executor,
            ChooseDialogView view,
            I18nizer i18nizer,
            SimpleTranslator i18n) {
        super(componentProvider, executor, view, i18nizer, i18n);
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.i18nContentSupport = i18nContentSupport;
    }

    @Override
    public ChooseDialogView start(DialogDefinition definition, UiContext uiContext) {
        getExecutor().setDialogDefinition(definition);
        return (ChooseDialogView)super.start(definition, uiContext);
    }

    @Override
    public ChooseDialogView start(ChooseDialogCallback callback, ChooseDialogDefinition definition, UiContext appContext, String selectedItemId) {
        start(definition, appContext);
        this.callback = callback;
        final FieldFactory formField = fieldFactoryFactory.createFieldFactory(definition.getField(), new NullItem());
        formField.setComponentProvider(componentProvider);
        formField.setI18nContentSupport(i18nContentSupport);
        this.field = (Field<Object>) formField.createField();
        if (field.getType().isAssignableFrom(Item.class)) {
            if (field instanceof AbstractComponent) {
                ((AbstractComponent) field).setImmediate(true);
            }
            field.addValueChangeListener(new ValueChangeListener() {
                @Override
                public void valueChange(ValueChangeEvent event) {
                    item = (Item) event.getProperty().getValue();
                }
            });
            getView().setCaption(definition.getLabel());
            getView().setContent(new View() {
                @Override
                public Component asVaadinComponent() {
                    return field;
                }
            });

            if (StringUtils.isNotBlank(selectedItemId)) {
                field.setValue(selectedItemId);
            }

            final OverlayCloser closer = appContext.openOverlay(getView());
            getView().setCaption(definition.getLabel());
            getView().addDialogCloseHandler(new DialogCloseHandler() {
                @Override
                public void onDialogClose(DialogView dialogView) {
                    closer.close();
                }
            });
            getView().setClosable(true);
            return getView();
        } else {
            log.error("Configured field type is compatible with choose dialogs (com.vaadin.data.Item is required). Choose dialog will not be created.");
            return null;
        }
    }

    @Override
    public ChooseDialogView getView() {
        return (ChooseDialogView) super.getView();
    }

    @Override
    public Object[] getActionParameters(String actionName) {
        return new Object[] { actionName, ChooseDialogPresenterImpl.this, field, getView(), callback, item != null ? item : new NullItem()};
    }

    @Override
    protected DialogActionExecutor getExecutor() {
        return (DialogActionExecutor)super.getExecutor();
    }
}
