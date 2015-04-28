/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.ui.form.field.transformer.composite.CompositeTransformer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Configures a composite field, i.e. a composition of {@link info.magnolia.ui.form.field.definition.FieldDefinition fields},
 * presented in a horizontal or vertical layout.
 */
public class CompositeFieldDefinition extends ConfiguredFieldDefinition {

    private static final Logger log = LoggerFactory.getLogger(CompositeFieldDefinition.class);

    private List<ConfiguredFieldDefinition> fields = new ArrayList<ConfiguredFieldDefinition>();
    private Layout layout = Layout.horizontal;

    /**
     * Set default {@link info.magnolia.ui.form.field.transformer.Transformer}.
     */
    public CompositeFieldDefinition() {
        setTransformerClass(CompositeTransformer.class);
    }

    public List<ConfiguredFieldDefinition> getFields() {
        return fields;
    }

    public void setFields(List<ConfiguredFieldDefinition> fields) {
        this.fields = fields;
    }

    public void addField(ConfiguredFieldDefinition field) {
        this.fields.add(field);
    }

    /**
     * Returns the names of the fields.
     */
    public List<String> getFieldNames() {
        return Lists.transform(fields, new Function<ConfiguredFieldDefinition, String>() {
            @Override
            public String apply(ConfiguredFieldDefinition fieldDefinition) {
                return fieldDefinition.getName();
            }
        });
    }

    /**
     * Deprecated since 5.3.9, please use {@link CompositeFieldDefinition#getFieldNames()} instead.
     */
    @Deprecated
    public List<String> getFieldsName() {
        return getFieldNames();
    }

    /**
     * Deprecated since 5.3.9. It is not supported to add only a field name.
     */
    @Deprecated
    public void addFieldName(String fieldName) {
        log.warn("CompositeFieldDefinition#addFieldName is deprecated and has no effect. Please adjust your code accordingly.");
    }

    /**
     * @return desired select part layout.
     */
    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

}
