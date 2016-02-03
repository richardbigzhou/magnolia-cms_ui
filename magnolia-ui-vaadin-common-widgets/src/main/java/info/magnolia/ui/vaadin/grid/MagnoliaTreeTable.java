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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Validator;
import com.vaadin.shared.ui.treetable.TreeTableState;
import com.vaadin.ui.TreeTable;

/**
 * VMagnoliaTreeTable.
 */
public class MagnoliaTreeTable extends TreeTable {

    private static Logger log = LoggerFactory.getLogger(MagnoliaTreeTable.class);

    public MagnoliaTreeTable() {
        addStyleName("v-magnolia-table");
        setCacheRate(4);
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

    @Override
    public void setContainerDataSource(Container newDataSource) {
        super.setContainerDataSource(newDataSource);

        // enforce partial updates - those were disabled in Vaadin 7 but they are safe as long as we don't generate vaadin components in table cells.
        try {
            Field f = TreeTable.class.getDeclaredField("containerSupportsPartialUpdates");
            f.setAccessible(true);
            f.setBoolean(this, true);
        } catch (Exception e) {
            log.warn("Could not enable partial-updates in tree.", e);
        }
    }
}
