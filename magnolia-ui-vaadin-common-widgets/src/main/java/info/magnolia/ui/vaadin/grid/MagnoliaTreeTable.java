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
package info.magnolia.ui.vaadin.grid;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.shared.ui.treetable.TreeTableState;
import com.vaadin.ui.Component;
import com.vaadin.ui.TreeTable;

/**
 * VMagnoliaTreeTable.
 */
public class MagnoliaTreeTable extends TreeTable {

    private static Logger log = LoggerFactory.getLogger(MagnoliaTreeTable.class);

    public MagnoliaTreeTable(Container dataSource) {
        super(null, dataSource);
        addStyleName("v-magnolia-table");
        setCacheRate(4);
    }

    public MagnoliaTreeTable() {
        this(new HierarchicalContainer());
    }

    /**
     * This method is made public in order to be able to delegate the call to
     * tree tables' {@link HierarchicalStrategy} instead of the actual container
     * which can be very slow.
     */
    @Override
    public int indexOfId(Object itemId) {
        return super.indexOfId(itemId);
    }

    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property<?> property) {
        String result = super.formatPropertyValue(rowId, colId, property);
        return StringEscapeUtils.escapeHtml(result);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        if (variables.containsKey("selectAll")) {
            boolean selectAll = (Boolean) variables.get("selectAll");
            if (selectAll) {
                Collection<?> ids = getItemIds();
                for (final Object id : ids) {
                    select(id);
                }
            } else {
                setValue(null);
            }
        }

        if (variables.containsKey("toggleSelection")) {
            boolean selected = (Boolean) variables.get("toggleSelection");
            String key = String.valueOf(variables.get("toggledRowId"));
            final Object id = itemIdMapper.get(key);
            if (selected) {
                select(id);
            } else {
                unselect(id);
            }
        }
    }

    /**
     * Make registerComponent public so that inplace-editing fields can be added to table.
     */
    @Override
    public void registerComponent(Component component) {
        super.registerComponent(component);
    }

    /**
     * MGNLUI-729 Overridden so that table is not marked as dirty without changes. This was made to prevent selection from being sent over again.
     * Beware, this breaks the normal state update mechanism and might require that you mark it as dirty explicitly.
     */
    @Override
    protected TreeTableState getState() {
        return (TreeTableState) getState(false);
    }

    /**
     * MGNLUI-962 Overridden to fulfill AbstractField's repaintIsNotNeeded, super impl returns empty collection instead.
     */
    @Override
    public Collection<Validator> getValidators() {
        return null;
    }

    /**
     * @return <code>true</code> if itemId is a descendant of parentId, <code>false</code> otherwise.
     */
    public boolean isDescendantOf(final Object itemId, final Object parentId) {
        Hierarchical container = getContainerDataSource();
        Object id = itemId;
        while (!container.isRoot(id)) {
            id = container.getParent(id);
            if (id.equals(parentId)) {
                return true;
            }
        }
        return false;
    }
}
