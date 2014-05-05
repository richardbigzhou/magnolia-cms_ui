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
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemActionExecutor;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionRegistry;
import info.magnolia.ui.api.availability.AvailabilityChecker;
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
 */
public final class TaskPresenter extends ItemPresenter<Task> {

    private TasksManager tasksManager;
    private SimpleTranslator i18n;

    @Inject
    public TaskPresenter(ItemView view, TasksManager tasksManager, AvailabilityChecker checker, ItemActionExecutor itemActionExecutor, ItemViewDefinitionRegistry itemViewDefinitionRegistry, FormBuilder formbuilder, ActionbarPresenter actionbarPresenter, I18nizer i18nizer, SimpleTranslator i18n) {
        super(view, itemActionExecutor, checker, itemViewDefinitionRegistry, formbuilder, actionbarPresenter, i18nizer);
        this.tasksManager = tasksManager;
        this.i18n = i18n;
    }

    @Override
    protected String getItemViewName(Task item) {
        return "pages:" + item.getName();
    }

    @Override
    protected void setItemViewTitle(Task task, ItemView view) {
        String subject = (String) task.getContent().get("subject");
        String repo = (String) task.getContent().get("repository");
        String path = (String) task.getContent().get("path");

        // fallback to task name in case subject is not available
        view.setTitle(subject != null ? i18n.translate(subject, repo, path) : task.getName());
    }

    @Override
    protected Task getPulseItemById(String itemId) {
        return tasksManager.getTaskById(itemId);
    }

    @Override
    protected BeanItem<Task> asBeanItem(Task item) {
        return new TaskItem(item);
    }

    private static final class TaskItem extends BeanItem<Task> {

        private static final DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        public TaskItem(Task bean) {
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
