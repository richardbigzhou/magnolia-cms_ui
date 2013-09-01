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
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.workbench.event.SearchEvent;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Factory for creating workbench choose dialog presenters.
 */
public class ChooseDialogPresenterImpl extends BaseDialogPresenter implements ChooseDialogPresenter {

    private static final Logger log = LoggerFactory.getLogger(ChooseDialogPresenterImpl.class);


    private Listener listener;

    private final ChooseDialogView chooseDialogView;

    private final EventBus eventBus;

    private FieldFactoryFactory fieldFactoryFactory;

    private ChooseDialogDefinition definition;

    private ComponentProvider componentProvider;

    private I18nContentSupport i18nContentSupport;

    private String selectedItemId;

    private Item item;

    @Inject
    public ChooseDialogPresenterImpl(
            ChooseDialogView view,
            @Named(ChooseDialogEventBus.NAME) EventBus eventBus,
            FieldFactoryFactory fieldFactoryFactory,
            ChooseDialogDefinition definition,
            ComponentProvider componentProvider,
            I18nContentSupport i18nContentSupport,
            Item item) {
        super(view);
        this.chooseDialogView = view;
        this.eventBus = eventBus;
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.definition = definition;
        this.componentProvider = componentProvider;
        this.i18nContentSupport = i18nContentSupport;
        this.item = item;

        showCloseButton();
        bindHandlers();
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

    private void bindHandlers() {

        eventBus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                item = event.getFirstItem();
            }
        });

        eventBus.addHandler(SearchEvent.class, new SearchEvent.Handler() {
            @Override
            public void onSearch(SearchEvent event) {
                //workbenchPresenter.doSearch(event.getSearchExpression());
            }
        });

        addActionCallback(WorkbenchChooseDialogView.CANCEL_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                closeDialog();
            }
        });

        addActionCallback(WorkbenchChooseDialogView.COMMIT_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                closeDialog();
            }
        });

        addDialogCloseHandler(new BaseDialog.DialogCloseEvent.Handler() {
            @Override
            public void onClose(BaseDialog.DialogCloseEvent event) {
                getBaseDialog().removeDialogCloseHandler(this);
                listener.onClose();
            }
        });
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }


    @Override
    public ChooseDialogView start() {
        final FieldFactory formField = fieldFactoryFactory.createFieldFactory(definition.getField(), item);
        formField.setComponentProvider(componentProvider);
        formField.setI18nContentSupport(i18nContentSupport);
        final Field<?> field = formField.createField();
        if (field.getType().isAssignableFrom(Item.class)) {
            if (field instanceof AbstractComponent) {
                ((AbstractComponent) field).setImmediate(true);
            }

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
            return chooseDialogView;
        } else {
            throw new IllegalArgumentException("TODO: HANDLE ME BETTER");
        }
    }

    @Override
    public Item getValue() {
        return item;
    }
}
