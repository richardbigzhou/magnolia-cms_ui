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
package info.magnolia.ui.model.field.builder;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import info.magnolia.ui.model.field.validation.definition.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.model.field.validation.definition.EmailValidatorDefinition;
import info.magnolia.ui.model.field.validation.definition.RegexpValidatorDefinition;

/**
 * Tests for {@link ValidatorConfig}.
 */
public class ValidatorConfigTest {

    private ValidatorConfig validators = new ValidatorConfig();

    @Test
    public void testDigitsOnly() {

        GenericValidatorBuilder builder = validators.digitsOnly();
        RegexpValidatorDefinition definition = (RegexpValidatorDefinition) builder.exec();

        assertTrue(definition.getPattern().equals("[0-9]+"));
    }

    @Test
    public void testRegexp() {

        GenericValidatorBuilder builder = validators.regexp("test-pattern");
        RegexpValidatorDefinition definition = (RegexpValidatorDefinition) builder.exec();

        assertTrue(definition.getPattern().equals("test-pattern"));
    }

    @Test
    public void testEmail() {

        GenericValidatorBuilder builder = validators.email();
        ConfiguredFieldValidatorDefinition definition = builder.exec();

        assertTrue(definition instanceof EmailValidatorDefinition);
    }
}
