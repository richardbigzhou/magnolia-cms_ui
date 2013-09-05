/**
 * This file Copyright (c) 2013 Magnolia International
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

import info.magnolia.ui.form.definition.AbstractFormKeyGenerator;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.definition.TabDefinition;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * An {@link I18nKeyGenerator} for {@link FieldDefinition}.
 */
public class FieldDefinitionKeyGenerator extends AbstractFormKeyGenerator<FieldDefinition> {
    @Override
    protected void keysFor(List<String> list, FieldDefinition field, AnnotatedElement el) {
        final TabDefinition tab = getParentViaCast(field);
        final String tabName = tab.getName();
        final FormDefinition formDef = getParentViaCast(tab);
        final String dialogID = getParentId(formDef);

        final String fieldName = field.getName();
        final String property = fieldOrGetterName(el);
        addKey(list, dialogID, tabName, fieldName, property);
        // <dialogId>.<tabName>.<fieldName>.<property>
        // <dialogId>.<tabName>.<fieldName>
        addKey(list, dialogID, fieldName, property);
        // <dialogId>.<fieldName>.<property>
        addKey(list, fieldName, property);
        // <fieldName>.<property>
    }

    @Override
    public String messageBundleNameFor(FieldDefinition def) {
        if (def.getI18nBasename() != null) {
            return def.getI18nBasename();
        } else {
            final TabDefinition tab = getParentViaCast(def);
            if (tab.getI18nBasename() != null) {
                return tab.getI18nBasename();
            } else {
                final FormDefinition formDef = getParentViaCast(tab);
                if (formDef.getI18nBasename() != null) {
                    return formDef.getI18nBasename();
                } else {
                    return getParentDialogMessageBundleName(formDef);
                }
            }
        }
    }

    private String getParentDialogMessageBundleName(FormDefinition form) {
        final Object dialog = getParentViaCast(form);
        String messageBundleName = null;
        try {
            final Method getId = dialog.getClass().getMethod("getI18nBasename");
            messageBundleName = (String) getId.invoke(dialog);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e); // TODO MGNLUI-2031
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e); // TODO MGNLUI-2031
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // TODO MGNLUI-2031
        }
        return messageBundleName;
    }
}
