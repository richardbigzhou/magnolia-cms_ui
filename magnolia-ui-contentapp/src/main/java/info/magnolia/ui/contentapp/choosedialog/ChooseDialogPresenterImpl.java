/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.contentapp.choosedialog;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.overlay.OverlayLayer;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.dialog.action.DialogActionExecutor;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent.Handler;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.vaadin.integration.NullItem;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Factory for creating workbench choose dialog presenters.
 */
public class ChooseDialogPresenterImpl extends BaseDialogPresenter implements ChooseDialogPresenter {

    private static final Logger log = LoggerFactory.getLogger(ChooseDialogPresenterImpl.class);

    private final ChooseDialogView chooseDialogView;

    private FieldFactoryFactory fieldFactoryFactory;

    private ComponentProvider componentProvider;

    private I18nContentSupport i18nContentSupport;

    private String selectedItemId;

    private Item item;

    private DialogActionExecutor actionExecutor;

    private ChooseDialogCallback callback;

    @Inject
    public ChooseDialogPresenterImpl(
            ChooseDialogView view,
            FieldFactoryFactory fieldFactoryFactory,
            ComponentProvider componentProvider,
            I18nContentSupport i18nContentSupport,
            DialogActionExecutor actionExecutor) {
        super(view);
        this.chooseDialogView = view;
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.i18nContentSupport = i18nContentSupport;
        this.actionExecutor = actionExecutor;

        showCloseButton();
    }

    /**
     * Set the selected itemId. <br>
     * If selectedItemId is a path, get the id for the path.
     */
    public void setSelectedItemId(String selectedItemId) {
        /*try {
            if (StringUtils.isBlank(selectedItemId)) {
                return;
            }
            this.selectedItemId = JcrItemUtil.getItemId(workbenchDefinition.getWorkspace(), selectedItemId);
            if (StringUtils.isBlank(this.selectedItemId) && JcrItemUtil.itemExists(workbenchDefinition.getWorkspace(), selectedItemId)) {
                this.selectedItemId = selectedItemId;
            }

        } catch (RepositoryException e) {
            log.warn("Unable to set the selected item", selectedItemId, e);
        } */
        this.selectedItemId = selectedItemId;
    }

    /**
     * Set in the View the already selected itemId.
     */
    private void select(String itemId) {
        /*try {
            // restore selection
            if (JcrItemUtil.itemExists(workbenchDefinition.getWorkspace(), itemId)) {
                List<String> ids = new ArrayList<String>(1);
                ids.add(itemId);
                workbenchView.getSelectedView().select(ids);
                javax.jcr.Item jcrItem = JcrItemUtil.getJcrItem(workbenchDefinition.getWorkspace(), itemId);

                if (jcrItem.isNode()) {
                    currentValue = new JcrNodeAdapter((Node) jcrItem);
                } else {
                    currentValue = new JcrPropertyAdapter((Property) jcrItem);
                }
            }
        } catch (RepositoryException e) {
            log.warn("Unable to get node or property [{}] for selection", itemId, e);
        }     */
    }


    @Override
    public ChooseDialogView start(ChooseDialogCallback callback, ChooseDialogDefinition definition, OverlayLayer overlayLayer) {
        this.callback = callback;
        final FieldFactory formField = fieldFactoryFactory.createFieldFactory(definition.getField(), new NullItem());
        formField.setComponentProvider(componentProvider);
        formField.setI18nContentSupport(i18nContentSupport);
        final Field<?> field = formField.createField();
        if (field.getType().isAssignableFrom(Item.class)) {
            if (field instanceof AbstractComponent) {
                ((AbstractComponent) field).setImmediate(true);
            }
            field.addValueChangeListener(new ValueChangeListener() {
                @Override
                public void valueChange(ValueChangeEvent event) {
                    item = (Item)event.getProperty().getValue();
                }
            });

            chooseDialogView.setCaption(field.getCaption());
            chooseDialogView.setContent(new View() {
                @Override
                public Component asVaadinComponent() {
                    return field;
                }
            });

            if (StringUtils.isNotBlank(selectedItemId)) {
                select(selectedItemId);
            }

            final OverlayCloser closer = overlayLayer.openOverlay(chooseDialogView);
            addDialogCloseHandler(new Handler() {
                @Override
                public void onClose(DialogCloseEvent event) {
                    closer.close();
                    getBaseDialog().removeDialogCloseHandler(this);
                }
            });
            actionExecutor.setDialogDefinition(definition);
            initActions(definition);
            return chooseDialogView;
        } else {
            throw new IllegalArgumentException("TODO: HANDLE ME BETTER");
        }
    }

    private void initActions(ChooseDialogDefinition definition) {

        for (final ActionDefinition action : definition.getActions().values()) {
            addAction(action.getName(), action.getLabel(), new DialogActionListener() {
                @Override
                public void onActionExecuted(final String actionName) {
                    try {
                        if (item != null) {
                            actionExecutor.execute(actionName, item,  callback);
                        } else {
                            actionExecutor.execute(actionName, callback);
                        }
                        chooseDialogView.close();
                    } catch (ActionExecutionException e) {
                        throw new RuntimeException("Could not execute action: " + actionName, e);
                    }
                }
            });
        }
    }
}
