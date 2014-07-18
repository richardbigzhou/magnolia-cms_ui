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
package info.magnolia.ui.workbench.autosuggest;

import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.vaadin.autosuggest.AutoSuggestTextField;
import info.magnolia.ui.workbench.tree.InplaceEditingFieldFactory;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Field;

/**
 * The AutoSuggestInplaceEditingFieldFactory is responsible for creating input fields with auto-suggestions displayed in a drop-down in table cells for inplace-editing.
 */
public class AutoSuggestInplaceEditingFieldFactory extends InplaceEditingFieldFactory {

    private AutoSuggester autoSuggester = null;

    public AutoSuggestInplaceEditingFieldFactory() {
        this.autoSuggester = null;
    }

    public void setAutoSuggester(AutoSuggester autoSuggester) {
        this.autoSuggester = autoSuggester;
    }

    @Override
    protected Field<?> createFieldByPropertyType(Object itemId, Object propertyId, Class<?> type) {
        if (type == null) {
            return null;
        }
        Field<?> field = new AutoSuggestTextField();
        // FIXME MGNLUI-1855 To remove once Vaadin 7.2 will be used. Currently we need to assign converter for properties with type Long because otherwise Vaadin assigns incompatible StringToNumberConverter.
        if (Long.class.equals(type)) {
            ((AbstractTextField) field).setConverter(new StringToLongConverter());
        }
        return field;
    }
}
