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

import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.task.Task;
import info.magnolia.task.definition.TaskDefinition;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.AbstractPulseDetailPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailActionExecutor;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionRegistry;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.pulse.task.TaskDetailPresenter;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang.time.FastDateFormat;

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
        view.setTitle(getDefinition().getTitle()); //-> i18nize
    }

    @Override
    protected BeanItem<T> asBeanItem() {
        return new TaskItem(getItem());
    }

    private static final class TaskItem<T extends Task> extends BeanItem<T> {

        private static final DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        public TaskItem(T bean) {
            super(bean);
            for (Entry<String, Object> entry : getBean().getContent().entrySet()) {
                addItemProperty(entry.getKey(), DefaultPropertyUtil.newDefaultProperty(String.class, parseValue(entry.getValue())));
            }
        }

        private String parseValue(Object value) {
            String string = String.valueOf(value);
            try {
                Date date = DATE_PARSER.parse(string);
                return FastDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, new Locale(MgnlContext.getUser().getLanguage())).format(date);
            } catch (ParseException e) {
                // not a date
                return string;
            }
        }
    }
}
