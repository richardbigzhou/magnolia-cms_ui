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

import info.magnolia.event.EventBus;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.framework.event.ChooseDialogEventBus;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.workbench.ContentView.ViewType;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemSelectedEvent;
import info.magnolia.ui.workbench.event.SearchEvent;

import javax.inject.Inject;
import javax.inject.Named;

import com.vaadin.data.Item;

/**
 * Factory for creating workbench choose dialog presenters.
 */
public class WorkbenchChooseDialogPresenter extends BaseDialogPresenter implements ChooseDialogPresenter {

    private Item currentValue = null;

    private Listener listener;

    private final ChooseDialogView chooseDialogView;

    private final WorkbenchPresenter workbenchPresenter;

    private final EventBus eventBus;

    private WorkbenchDefinition workbenchDefinition;

    private ImageProviderDefinition imageProviderDefinition;

    @Inject
    public WorkbenchChooseDialogPresenter(ChooseDialogView view, WorkbenchPresenter workbenchPresenter, final @Named(ChooseDialogEventBus.NAME) EventBus eventBus) {
        super(view);
        this.chooseDialogView = view;
        this.workbenchPresenter = workbenchPresenter;
        this.eventBus = eventBus;

        showCloseButton();
        bindHandlers();
    }

    private void bindHandlers() {

        eventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {
            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                currentValue = event.getItem();
            }
        });

        eventBus.addHandler(SearchEvent.class, new SearchEvent.Handler() {
            @Override
            public void onSearch(SearchEvent event) {
                workbenchPresenter.doSearch(event.getSearchExpression());
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

    public void setWorkbenchDefinition(WorkbenchDefinition workbenchDefinition) {
        this.workbenchDefinition = workbenchDefinition;
    }

    public void setImageProviderDefinition(ImageProviderDefinition imageProviderDefinition) {
        this.imageProviderDefinition = imageProviderDefinition;
    }

    @Override
    public ChooseDialogView start() {
        WorkbenchView view = workbenchPresenter.start(workbenchDefinition, imageProviderDefinition, eventBus);
        view.setViewType(ViewType.TREE);

        chooseDialogView.setContent(view);
        return chooseDialogView;
    }

    @Override
    public Item getValue() {
        return currentValue;
    }
}
