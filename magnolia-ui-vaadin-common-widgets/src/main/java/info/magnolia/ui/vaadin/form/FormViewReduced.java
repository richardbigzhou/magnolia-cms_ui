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
package info.magnolia.ui.vaadin.form;


import info.magnolia.ui.api.view.View;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;

/**
 * Interface for a basic form view.
 */
public interface FormViewReduced extends View, Item.Editor {

    void addField(Field<?> field);

    void setDescriptionVisibility(boolean isVisible);

    void addFormSection(String tabName, FormSection inputFields);

    void showValidation(boolean isVisible);

    void setShowAllEnabled(boolean enabled);

    boolean isValid();

    List<FormSection> getFormSections();

    /**
     * @deprecated since 5.4.2 - use {@link #getFormSections()} instead.
     */
    @Deprecated
    Collection<Field<?>> getFields();

    void setListener(Listener listener);

    /**
     * Listener interface.
     */
    interface Listener {

        void localeChanged(Locale newLocale);
    }
}
