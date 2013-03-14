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
package info.magnolia.ui.contentapp.item;

import info.magnolia.event.EventBus;
import info.magnolia.ui.admincentral.form.FormPresenter;
import info.magnolia.ui.admincentral.form.FormPresenterFactory;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.event.AdminCentralEventBusConfigurer;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.vaadin.form.FormView;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Presenter for the item displayed in the {@link info.magnolia.ui.contentapp.workbench.ItemWorkbenchPresenter}. Takes
 * care of building and switching between the right {@link ItemView.ViewType}.
 */
public class ItemPresenter {

    private SubAppContext subAppContext;
    private final EventBus eventBus;

    private final ItemView view;

    private final FormPresenterFactory formPresenterFactory;

    private FormDefinition formDefinition;

    private JcrNodeAdapter item;

    @Inject
    public ItemPresenter(SubAppContext subAppContext, final @Named(AdminCentralEventBusConfigurer.EVENT_BUS_NAME) EventBus eventBus, ItemView view, FormPresenterFactory formPresenterFactory) {
        this.subAppContext = subAppContext;
        this.eventBus = eventBus;
        this.view = view;
        this.formPresenterFactory = formPresenterFactory;
    }

    public ItemView start(FormDefinition formDefinition, final JcrNodeAdapter item, ItemView.ViewType viewType) {
        this.formDefinition = formDefinition;
        this.item = item;

        setItemView(viewType);
        return view;
    }

    private void setItemView(ItemView.ViewType viewType) {
        final FormPresenter formPresenter = formPresenterFactory.createFormPresenterByDefinition(formDefinition);

        switch (viewType) {
        case VIEW:
        case EDIT:
        default:
            final FormView formView = formPresenter.start(item, new FormPresenter.Callback() {

                @Override
                public void onCancel() {
                    //setItemView(ItemView.ViewType.VIEW);
                    subAppContext.close();
                }

                @Override
                public void onSuccess(String actionName) {
                    eventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), item.getPath()));
                    //setItemView(ItemView.ViewType.VIEW);
                    subAppContext.close();
                }
            });
            view.setItemView(formView.asVaadinComponent(), viewType);
            break;

        }

    }
}
