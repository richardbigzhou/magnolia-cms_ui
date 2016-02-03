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
package info.magnolia.ui.contentapp.choosedialog;

import info.magnolia.event.EventBus;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.ContentView.ViewType;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;
import info.magnolia.ui.workbench.event.SearchEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Factory for creating workbench choose dialog presenters.
 */
public class WorkbenchChooseDialogPresenter extends BaseDialogPresenter implements ChooseDialogPresenter {

    private static final Logger log = LoggerFactory.getLogger(WorkbenchChooseDialogPresenter.class);

    private Item currentValue = null;

    private String selectedItemId;

    private Listener listener;

    private final ChooseDialogView chooseDialogView;

    private final WorkbenchPresenter workbenchPresenter;

    private final EventBus eventBus;

    private WorkbenchDefinition workbenchDefinition;

    private ImageProviderDefinition imageProviderDefinition;

    private WorkbenchView workbenchView;

    @Inject
    public WorkbenchChooseDialogPresenter(ChooseDialogView view, WorkbenchPresenter workbenchPresenter, final @Named(ChooseDialogEventBus.NAME) EventBus eventBus) {
        super(view);
        this.chooseDialogView = view;
        this.workbenchPresenter = workbenchPresenter;
        this.eventBus = eventBus;

        showCloseButton();
        bindHandlers();
    }

    /**
     * Set the selected itemId. <br>
     * If selectedItemId is a path, get the id for the path.
     */
    public void setSelectedItemId(String selectedItemId) {
        try {
            if (StringUtils.isBlank(selectedItemId)) {
                return;
            }
            this.selectedItemId = JcrItemUtil.getItemId(workbenchDefinition.getWorkspace(), selectedItemId);
            if (StringUtils.isBlank(this.selectedItemId) && JcrItemUtil.itemExists(workbenchDefinition.getWorkspace(), selectedItemId)) {
                this.selectedItemId = selectedItemId;
            }

        } catch (RepositoryException e) {
            log.warn("Unable to set the selected item", selectedItemId, e);
        }
    }

    /**
     * Set in the View the already selected itemId.
     */
    private void select(String itemId) {
        try {
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
        }
    }

    private void bindHandlers() {

        eventBus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                currentValue = event.getFirstItem();
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
        workbenchView = workbenchPresenter.start(workbenchDefinition, imageProviderDefinition, eventBus);
        workbenchView.setMultiselect(false);
        workbenchView.setViewType(ViewType.TREE);
        chooseDialogView.setCaption(workbenchDefinition.getName());
        chooseDialogView.setContent(workbenchView);
        if (StringUtils.isNotBlank(selectedItemId)) {
            select(selectedItemId);
        }
        return chooseDialogView;
    }

    @Override
    public Item getValue() {
        return currentValue;
    }
}
