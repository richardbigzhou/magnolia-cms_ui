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
package info.magnolia.ui.form.field.factory;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.FormItem;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.validator.registry.FieldValidatorFactoryFactory;
import info.magnolia.ui.api.view.View;

import com.vaadin.ui.Field;

/**
 * A FieldFactory is responsible for creating Vaadin {@link Field}s configured based on their
 * associated FieldDefinition.
 * <p>
 * FieldFactory and {@link FieldDefinition} are linked together using {@link FieldFactoryFactory}.
 * <p>
 * Implementations of this interface will be instantiated with the
 * <ul>
 * <li>{@link info.magnolia.ui.form.field.definition.FieldDefinition} they should use for configuration
 * <li>{@link com.vaadin.data.Item} they will be operating on additional injected constructor arguments.
 * </ul>
 * <strong>FieldFactory are responsible to create/initialize Items that are by the Vaadin Field.</strong>
 *
 * @see FieldDefinition
 * @see FieldFactoryFactory
 */
public interface FieldFactory extends FormItem {

    /**
     * Creates and initializes a Vaadin {@link Field} component.
     */
    Field<?> createField();

    View getView();

    FieldDefinition getFieldDefinition();

    void setFieldValidatorFactoryFactory(FieldValidatorFactoryFactory fieldValidatorFactoryFactory);

    void setI18nContentSupport(I18nContentSupport i18nContentSupport);

    void setComponentProvider(ComponentProvider componentProvider);
}
