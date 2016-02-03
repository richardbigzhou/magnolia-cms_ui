/**
 * This file Copyright (c) 2011-2016 Magnolia International
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

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import info.magnolia.i18nsystem.I18nText;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.ui.Table;

/**
 * Defines a column that displays the value of a boolean property with an icon or simple text.
 */
public class BooleanPropertyColumnDefinition extends AbstractColumnDefinition {

    private DisplayMode displayMode = DisplayMode.ICON_ONLY;
    private String falseIcon;
    private String trueIcon = "icon-tick";
    private String falseLabel;
    private String trueLabel;

    public BooleanPropertyColumnDefinition() {
        setFormatterClass(BooleanPropertyColumnFormatter.class);
        setDisplayInChooseDialog(false);
        setWidth(46);
    }

    @Override
    public Class<?> getType() {
        return Boolean.class;
    }

    /**
     * Specifies the display mode for representing the value of the boolean property.
     */
    public static enum DisplayMode {

        /** Generate an icon based on configured properties {@link BooleanPropertyColumnDefinition#getTrueIcon() trueIcon} and {@link BooleanPropertyColumnDefinition#getFalseIcon() falseIcon}. If blank nothing is rendered. */
        ICON_ONLY,

        /** Generate a label based on configured properties {@link BooleanPropertyColumnDefinition#getTrueLabel() trueLabel} and {@link BooleanPropertyColumnDefinition#getFalseLabel() falseLabel}. If blank nothing is rendered. */
        TEXT_ONLY,

        /** Generate both an icon and a label based on {@link BooleanPropertyColumnDefinition}'s configured properties. */
        ICON_AND_TEXT
    }

    /**
     * Generates xhtml cell content (icon and/or text) for the {@link BooleanPropertyColumnDefinition}.
     */
    public static class BooleanPropertyColumnFormatter extends AbstractColumnFormatter<BooleanPropertyColumnDefinition> {

        @Inject
        public BooleanPropertyColumnFormatter(BooleanPropertyColumnDefinition definition) {
            super(definition);
        }

        @Override
        public String generateCell(Table source, Object itemId, Object columnId) {
            StringBuilder sb = new StringBuilder();

            // get property value
            Property<?> property = source.getContainerProperty(itemId, columnId);
            boolean value = false;
            if (property == null) {
                // no property found or its value was null, i.e. keep it false here
            } else if (Boolean.class.isAssignableFrom(property.getType())) {
                value = ((Boolean) property.getValue()).booleanValue();
            } else if (String.class.isAssignableFrom(property.getType())) {
                value = Boolean.parseBoolean((String) property.getValue());
            }

            // generate cell content
            if (definition.getDisplayMode() != DisplayMode.TEXT_ONLY) {
                if (value && StringUtils.isNotBlank(definition.getTrueIcon())) {
                    sb.append("<span class=\"").append(escapeHtml4(definition.getTrueIcon())).append("\"></span>");
                } else if (!value && StringUtils.isNotBlank(definition.getFalseIcon())) {
                    sb.append("<span class=\"").append(escapeHtml4(definition.getFalseIcon())).append("\"></span>");
                }
            }
            if (definition.getDisplayMode() != DisplayMode.ICON_ONLY) {
                if (value && StringUtils.isNotBlank(definition.getTrueLabel())) {
                    sb.append("<span>").append(escapeHtml4(definition.getTrueLabel())).append("</span>");
                } else if (!value && StringUtils.isNotBlank(definition.getFalseLabel())) {
                    sb.append("<span>").append(escapeHtml4(definition.getFalseLabel())).append("</span>");
                }
            }

            return StringUtils.isNotBlank(sb) ? sb.toString() : null;
        }
    }

    /**
     * Defines the display mode for representing the value of the boolean property.
     *
     * @see DisplayMode
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    /**
     * Defines the icon to display when the value of the boolean property is <code>false</code>.
     */
    public String getFalseIcon() {
        return falseIcon;
    }

    public void setFalseIcon(String falseIcon) {
        this.falseIcon = falseIcon;
    }

    /**
     * Defines the icon to display when the value of the boolean property is <code>true</code>.
     * Default value is <code>"icon-tick"</code>
     */
    public String getTrueIcon() {
        return trueIcon;
    }

    public void setTrueIcon(String trueIcon) {
        this.trueIcon = trueIcon;
    }

    /**
     * Defines the text to display when the value of the boolean property is <code>false</code>.
     */
    @I18nText
    public String getFalseLabel() {
        return falseLabel;
    }

    public void setFalseLabel(String falseLabel) {
        this.falseLabel = falseLabel;
    }

    /**
     * Defines the text to display when the value of the boolean property is <code>true</code>.
     */
    @I18nText
    public String getTrueLabel() {
        return trueLabel;
    }

    public void setTrueLabel(String trueLabel) {
        this.trueLabel = trueLabel;
    }
}
