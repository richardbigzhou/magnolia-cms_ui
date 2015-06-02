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
package info.magnolia.ui.workbench.column.definition;

import info.magnolia.i18nsystem.I18nText;
import info.magnolia.i18nsystem.I18nable;

/**
 * Defines a column in a workbench.
 *
 * @see info.magnolia.ui.workbench.definition.WorkbenchDefinition
 */
@I18nable(keyGenerator = ColumnDefinitionKeyGenerator.class)
public interface ColumnDefinition {

    String getName();

    String getPropertyName();

    @I18nText
    String getLabel();

    /**
     * Sets columns width (in pixels). See {@link #getExpandRatio()}.
     */
    int getWidth();

    /**
     * Expand ratios can be defined to customize the way how excess space is divided among columns.
     * A table can have excess space if it has its width defined and there is horizontally more
     * space than columns consume naturally. Excess space is the space that is not used by columns
     * with explicit width (see {@link #getWidth()}) or with natural width (no width nor expand
     * ratio).
     */
    float getExpandRatio();

    boolean isSortable();

    Class<? extends ColumnFormatter> getFormatterClass();

    /**
     * Defines the alignment for the column TODO COMPLETE....
     */
    String getAlignment();

    /**
     * Defines the style classes for the column TODO COMPLETE....
     */
    String getStyleClass();

    Class<?> getType();

    boolean isDisplayInChooseDialog();

    /**
     * Returns whether this column and therefore the underlying JCR property it represents is to be
     * included in searches. Its value is <code>true</code> by default.
     */
    boolean isSearchable();

    /**
     * Returns whether this column should be editable if workbench uses inplace editing.
     *
     * @return true, if column is editable
     * @see info.magnolia.ui.workbench.definition.WorkbenchDefinition#isEditable()
     */
    boolean isEditable();

    /**
     * If false - the column will not be displayed.
     * useful for turning a column off when extending a columns configuration.
     *
     * @return
     */
    boolean isEnabled();

    /**
     * Returns the AvailabilityRule object for this subject.
     */
    Class<? extends ColumnAvailabilityRule> getRuleClass();
}
