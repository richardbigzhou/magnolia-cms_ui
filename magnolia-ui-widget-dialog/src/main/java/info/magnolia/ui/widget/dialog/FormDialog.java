/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.dialog;

import info.magnolia.ui.vaadin.widget.tabsheet.MagnoliaTabSheet;
import info.magnolia.ui.widget.dialog.gwt.client.dialoglayout.VFormDialog;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Field;

/**
 * FormDialog.
 *
 */
@ClientWidget(value = VFormDialog.class, loadStyle = LoadStyle.EAGER)
public class FormDialog extends BaseDialog implements FormDialogView {

    private MagnoliaTabSheet tabSheet = new MagnoliaTabSheet() {
        @Override
        public MagnoliaDialogTab addTab(final String caption, final ComponentContainer c) {
            if (c instanceof FormSection) {
                final MagnoliaDialogTab tab = new MagnoliaDialogTab(caption, (FormSection)c);
                tab.setSizeUndefined();
                tab.setClosable(false);
                doAddTab(tab);
                return tab;                    
            }
            return null;
        }
    };
    
    private Item itemDatasource;
    
    private List<Field> fields = new LinkedList<Field>();
    
    public FormDialog() { 
        super.setContent(tabSheet);
        tabSheet.showAllTab(true, "Show all");
        tabSheet.setHeight("500px");
    }
    
    @Override
    public void addField(Field field) {
        fields.add(field);
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }
    
    @Override
    public boolean isValid() {
        boolean res = true;
        for (Field field : getFields()) {
            res &= field.isValid();
        }
        return res;
    }
    
    @Override
    public void showValidation(boolean isVisible) {
        final Iterator<Component> it = tabSheet.getComponentIterator();
        while (it.hasNext()) {
            final Component c = it.next();
            if (c instanceof FormSection) {
                ((FormSection)c).setValidationVisible(isVisible);
            }
        }
    }
    
    @Override
    public void setItemDataSource(Item newDataSource) {
        this.itemDatasource = newDataSource;
    }

    @Override
    public Item getItemDataSource() {
        return itemDatasource;
    }

    @Override
    public void addDialogSection(String tabName, FormSection inputFields) {
        tabSheet.addTab(tabName, inputFields);
    }
    
    @Override
    protected Component createDefaultContent() {
        return tabSheet;
    }

    @Override
    public FormDialog asVaadinComponent() {
        return this;
    }

    @Override
    public void setShowAllEnabled(boolean enabled) {
        tabSheet.showAllTab(true, "Show All");
    };
    
}
