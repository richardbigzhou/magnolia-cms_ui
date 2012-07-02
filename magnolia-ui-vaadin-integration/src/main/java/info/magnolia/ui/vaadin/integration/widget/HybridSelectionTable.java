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
package info.magnolia.ui.vaadin.integration.widget;

import info.magnolia.ui.vaadin.integration.widget.client.VHybridSelectionTable;

import java.util.Collection;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ChameleonTheme;

/**
 * Hybrid selection table.
 * @author p4elkin
 *
 */
@SuppressWarnings("serial")
@ClientWidget(value = VHybridSelectionTable.class, loadStyle = LoadStyle.EAGER)
public class HybridSelectionTable extends Table {

    private final static String CHECKBOX_COLUMN_ID = "CB";
    
    public HybridSelectionTable() {
        super();
        addStyleName("v-hybrid-selection-table");
        addStyleName(ChameleonTheme.TABLE_BORDERLESS);
        setSelectable(true);
        setImmediate(true);
        setMultiSelect(true);
    }
    
    @Override
    public void setContainerDataSource(Container newDataSource) {
        final Container current = items;
        super.setContainerDataSource(newDataSource);
        if (current != null) {
            addContainerProperty(CHECKBOX_COLUMN_ID, String.class, "");
            setColumnHeader(CHECKBOX_COLUMN_ID, "");
            setColumnWidth(CHECKBOX_COLUMN_ID, 20);
        }
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
            boolean selected = (Boolean)variables.get("toggleSelection");
            String key = String.valueOf(variables.get("toggledRowId"));
            final Object id = itemIdMapper.get(key);
            if (selected) {
                select(id);
            } else {
                unselect(id);
            }
        }
    }
}
