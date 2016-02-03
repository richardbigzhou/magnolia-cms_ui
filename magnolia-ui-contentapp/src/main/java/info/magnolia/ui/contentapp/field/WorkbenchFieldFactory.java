/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.contentapp.field;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.DefaultContentConnector;
import info.magnolia.ui.workbench.WorkbenchPresenter;

import javax.inject.Inject;
import javax.inject.Named;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;

/**
 * Factory capable of producing {@link WorkbenchField}.
 */
public class WorkbenchFieldFactory extends AbstractFieldFactory<WorkbenchFieldDefinition, Object> {

    private final WorkbenchFieldDefinition definition;

    private final WorkbenchPresenter workbenchPresenter;
    private final ContentConnector contentConnector;

    private final EventBus chooseDialogEventBus;
    private final EventBus admincentralEventBus;

    @Inject
    public WorkbenchFieldFactory(

            WorkbenchFieldDefinition definition,
            Item relatedFieldItem,
            WorkbenchPresenter workbenchPresenter,
            ContentConnector contentConnector,
            @Named(ChooseDialogEventBus.NAME) EventBus chooseDialogEventBus,
            @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus) {

        super(definition, relatedFieldItem);
        this.definition = definition;
        this.workbenchPresenter = workbenchPresenter;
        this.contentConnector = contentConnector;
        this.chooseDialogEventBus = chooseDialogEventBus;
        this.admincentralEventBus = admincentralEventBus;
    }

    /**
     * @deprecated since 5.3.4, replaced with {@link #WorkbenchFieldFactory(WorkbenchFieldDefinition, Item, WorkbenchPresenter, ContentConnector, EventBus, EventBus)}.
     * Constructor should not be called explicitly, but rather use IoC anyway.
     */
    @Deprecated
    public WorkbenchFieldFactory(
            WorkbenchFieldDefinition definition,
            Item relatedFieldItem,
            WorkbenchPresenter workbenchPresenter,
            @Named(ChooseDialogEventBus.NAME) EventBus eventBus) {
        this(definition, relatedFieldItem, workbenchPresenter, new DefaultContentConnector(), eventBus, null);
    }

    @Override
    protected Field<Object> createFieldComponent() {
        final WorkbenchField workbenchField = new WorkbenchField(definition.getWorkbench(), definition.getImageProvider(), workbenchPresenter, chooseDialogEventBus);

        // Workaround for choose-dialogs to listen to ContentChangeEvents
        // Ideally this goes in ChooseDialogPresenter but it should be relocated to ui-contentapp first.
        if (admincentralEventBus != null) {
            admincentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

                @Override
                public void onContentChanged(ContentChangedEvent event) {
                    Object itemId = event.getItemId();
                    if (contentConnector.canHandleItem(itemId)) {
                        workbenchField.getPresenter().refresh();
                        workbenchField.getPresenter().select(itemId);
                    }
                }
            });
        }
        return workbenchField;
    }
}
