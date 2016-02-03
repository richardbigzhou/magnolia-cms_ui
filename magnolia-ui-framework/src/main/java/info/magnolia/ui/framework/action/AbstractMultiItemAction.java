/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.framework.action;

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Ordering;

/**
 * Abstract multi-item Action that defines the default behavior.
 * 
 * @param <D> the action definition type
 */
public abstract class AbstractMultiItemAction<D extends ActionDefinition> extends AbstractAction<D> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final List<JcrItemAdapter> items;
    private Map<JcrItemAdapter, Exception> failedItems;
    private final UiContext uiContext;
    // the item that is currently BEING processed
    private JcrItemAdapter currentItem;

    protected AbstractMultiItemAction(D definition, JcrItemAdapter item, UiContext uiContext) {
        super(definition);
        this.items = new ArrayList<JcrItemAdapter>(1);
        this.items.add(item);
        this.uiContext = uiContext;
    }

    protected AbstractMultiItemAction(D definition, List<JcrItemAdapter> items, UiContext uiContext) {
        super(definition);
        this.items = items;
        this.uiContext = uiContext;
    }

    /**
     * Executes the action on ONE item.
     */
    protected abstract void executeOnItem(JcrItemAdapter item) throws Exception;

    /**
     * Returns the message to display, if the execution succeeds on ALL items. May return <code>null</code>,
     * if the implementing action handles the user notification on its own.
     */
    protected abstract String getSuccessMessage();

    /**
     * Returns the message to display, if the execution fails on at least ONE item. May return <code>null</code>,
     * if the implementing action handles the user notification on its own.
     */
    protected abstract String getFailureMessage();

    @Override
    public void execute() throws ActionExecutionException {
        failedItems = new LinkedHashMap<JcrItemAdapter, Exception>();

        for (JcrItemAdapter item : getSortedItems(getItemComparator())) {
            this.currentItem = item;
            try {
                executeOnItem(item);
            } catch (Exception ex) {
                failedItems.put(item, ex);
            }
        }
        this.currentItem = null;

        if (failedItems.isEmpty()) {
            String message = getSuccessMessage();
            if (StringUtils.isNotBlank(message)) {
                uiContext.openNotification(MessageStyleTypeEnum.INFO, true, message);
            }
        } else {
            String message = getErrorNotification();
            if (StringUtils.isNotBlank(message)) {
                uiContext.openNotification(MessageStyleTypeEnum.ERROR, false, message);
            }
        }
    }

    protected String getErrorNotification() {
        String failureMessage = getFailureMessage();
        if (failureMessage != null) {
            StringBuilder notification = new StringBuilder(failureMessage);
            notification.append("<ul>");
            for (JcrItemAdapter item : failedItems.keySet()) {
                Exception ex = failedItems.get(item);
                notification.append("<li>").append("<b>");
                notification.append(JcrItemUtil.getItemPath(item.getJcrItem())).append("</b>: ").append(ex.getMessage());
                notification.append("</li>");
            }
            notification.append("</ul>");
            return notification.toString();
        }
        return null;
    }

    protected List<JcrItemAdapter> getItems() {
        return this.items;
    }

    /**
     * @return the sorted Items list based on the desired {@link Comparator}.
     */
    protected List<JcrItemAdapter> getSortedItems(Comparator<JcrItemAdapter> comparator) {
        return Ordering.from(comparator).sortedCopy(this.items);
    }

    protected UiContext getUiContext() {
        return this.uiContext;
    }

    protected Map<JcrItemAdapter, Exception> getFailedItems() {
        return this.failedItems;
    }

    /**
     * Returns the item that is currently <b>being</b> processed - i.e. <code>null</code> if the {@link #execute()} method is not running.
     */
    protected JcrItemAdapter getCurrentItem() {
        return this.currentItem;
    }

    /**
     * This method should be used <b>only in tests</b> (when the test does not call the {@link #execute()} method but e.g. only the {@link AbstractCommandAction#onPreExecute()} so the current item hasn't been set.
     */
    protected void setCurrentItem(JcrItemAdapter item) {
        this.currentItem = item;
    }

    /**
     * Create a {@link Comparator} used to sort {@link JcrItemAdapter}.
     * First element of the list are child items:
     * - /a/b.property
     * - /a/b
     * - /a
     * - /
     */
    protected Comparator<JcrItemAdapter> getItemComparator() {
        return new Comparator<JcrItemAdapter>() {
            @Override
            public int compare(JcrItemAdapter o1, JcrItemAdapter o2) {
                try {
                    int res = o2.getJcrItem().getDepth() - o1.getJcrItem().getDepth();
                    return res;
                } catch (RepositoryException e) {
                    return 0;
                }
            }
        };
    }
}
