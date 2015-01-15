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
package info.magnolia.ui.form.field.definition;

import info.magnolia.i18nsystem.I18nable;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.validator.definition.FieldValidatorDefinition;
import info.magnolia.i18nsystem.I18nText;

import java.util.List;

import com.vaadin.data.util.converter.Converter;

/**
 * Defines a field within a dialog.
 *
 * @see FieldDefinition
 * @see FieldValidatorDefinition
 */
@I18nable(keyGenerator = FieldDefinitionKeyGenerator.class)
public interface FieldDefinition {

    /**
     * Determines the name of the data property where the value entered by the user is stored.
     */
    String getName();

    /**
     * Makes the field mandatory.
     */
    boolean isRequired();

    /**
     * Error message text displayed in case of required = true.
     */
    @I18nText
    String getRequiredErrorMessage();

    /**
     * The type of this field when stored in a JCR repository expressed as a JCR property type name.
     *
     * @see javax.jcr.PropertyType
     */
    String getType();

    /**
     * Text displayed as field label.
     */
    @I18nText
    String getLabel();

    /**
     * Message bundle for localized field labels.
     */
    String getI18nBasename();

    /**
     * Description displayed to the user when clicking on the Info Button.
     */
    @I18nText
    String getDescription();

    /**
     * Pre-filled value displayed in the field. The value can be overwritten by the user.
     */
    String getDefaultValue();

    /**
     * Determines if a Field Property can be changed.
     */
    boolean isReadOnly();

    /**
     * Enables i18n authoring support.
     * This allows authors to write foreign language or regionally targeted content.
     * A two-letter language identifier (en, ge, fr etc.) is displayed on controls
     * where i18n is set to true.
     */
    boolean isI18n();

    /**
     * Define a specific Field styleName. This style definition will be added to the Field Style by calling
     * AbstractComponent.addStyleName.
     */
    String getStyleName();

    List<FieldValidatorDefinition> getValidators();

    Class<? extends Transformer<?>> getTransformerClass();

    Class<? extends Converter<?, ?>> getConverterClass();
}
