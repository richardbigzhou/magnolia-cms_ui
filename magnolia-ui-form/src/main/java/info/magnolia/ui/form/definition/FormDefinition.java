/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.form.definition;

import info.magnolia.i18nsystem.I18nable;
import info.magnolia.i18nsystem.I18nText;
import java.util.List;

/**
 * Defines a form.
 *
 * @see TabDefinition
 * @see info.magnolia.ui.form.field.definition.FieldDefinition
 */
@I18nable(keyGenerator = FormDefinitionKeyGenerator.class)
public interface FormDefinition {

    @I18nText
    String getLabel();

    /**
     * This is support for old I18n mechanism where you need to provide keys for {@link #getLabel()} ()} and {@link #getDescription()}.
     * The new i18n mechanism generates the keys via {@link FormDefinitionKeyGenerator} automatically.
     *
     * @deprecated since 5.4.5. I18nBasename is deprecated and will be removed in a future version.
     */
    @Deprecated
    String getI18nBasename();

    @I18nText
    String getDescription();

    List<TabDefinition> getTabs();

}
