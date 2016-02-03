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
package info.magnolia.ui.form.field.property;

import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

/**
 * SingleProperty implementation of {@link MultiValueHandler}.<br>
 * Store the list of values in a single property as a concatenation of string with a ',' separator.<br>
 * Retrieve the single property as a List of String.
 */
public class SingleValueHandler extends AbstractBasePropertyValueHandler {

    private JcrNodeAdapter parent;
    private String propertyName;

    @Inject
    public SingleValueHandler(JcrNodeAdapter parent, String propertyName) {
        this.parent = parent;
        this.propertyName = propertyName;
    }

    @Override
    public void setValue(List<String> newValue) {
        DefaultProperty<String> property = getOrCreateProperty(String.class, "", parent, propertyName);
        property.setValue(StringUtils.join(newValue, ","));
    }

    @Override
    public List<String> getValue() {
        DefaultProperty<String> property = getOrCreateProperty(String.class, "", parent, propertyName);
        String value = property.getValue();
        return Arrays.asList(value.split(","));
    }
}
