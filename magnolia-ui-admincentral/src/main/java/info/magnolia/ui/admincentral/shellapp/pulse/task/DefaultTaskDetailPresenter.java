/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.task;

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.task.Task;
import info.magnolia.task.definition.TaskDefinition;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.AbstractPulseDetailPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailActionExecutor;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionRegistry;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.dialog.formdialog.FormBuilder;

import javax.inject.Inject;

import com.vaadin.data.util.BeanItem;

/**
 * The task detail presenter.
 * 
 * @param <D> generic {@link TaskDefinition}.
 * @param <T> generic {@link Task}.
 */
public class DefaultTaskDetailPresenter<D extends TaskDefinition, T extends Task> extends AbstractPulseDetailPresenter<T> implements TaskDetailPresenter {

    private D definition;

    @Inject
    public DefaultTaskDetailPresenter(PulseDetailView view, D definition, T task, AvailabilityChecker checker, PulseDetailActionExecutor itemActionExecutor,
            ItemViewDefinitionRegistry itemViewDefinitionRegistry, FormBuilder formbuilder, ActionbarPresenter actionbarPresenter,
            I18nizer i18nizer) {
        super(task, view, itemActionExecutor, checker, itemViewDefinitionRegistry, formbuilder, actionbarPresenter, i18nizer);
        this.definition = definition;
        this.item = task;
    }

    public D getDefinition() {
        return definition;
    }

    @Override
    protected String getItemViewName() {
        return definition.getTaskView();
    }

    @Override
    protected void setItemViewTitle(PulseDetailView view) {
        view.setTitle(getDefinition().getTitle());
    }

    @Override
    protected BeanItem<T> asBeanItem() {
        return new TaskItem(getItem(), getDefinition());
    }
}
