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

import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.i18n.AbstractAppKeyGenerator;
import info.magnolia.ui.form.definition.AbstractFormKeyGenerator;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.definition.TabDefinition;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link info.magnolia.i18nsystem.I18nKeyGenerator} for {@link FieldDefinition}.
 */
public class FieldDefinitionKeyGenerator extends AbstractFormKeyGenerator<FieldDefinition> {

    private static final Logger log = LoggerFactory.getLogger(FieldDefinitionKeyGenerator.class);

    private static final String CHOOSE_DIALOG_DEFINITION = "ChooseDialogDefinition";

    @Override
    protected void keysFor(List<String> list, FieldDefinition field, AnnotatedElement el) {
        Object parent = getParentViaCast(field);
        final String fieldName = field.getName().replace(':', '-');
        final String fieldOrGetterName = fieldOrGetterName(el);

        if (parent != null && isChooseDialog(parent.getClass())) {
            // handle choose dialog
            final AppDescriptor app = (AppDescriptor) getRoot(field);
            addKey(list, false, AbstractAppKeyGenerator.APPS, app.getName(), AbstractAppKeyGenerator.CHOOSE_DIALOG, FIELDS, fieldName, fieldOrGetterName);
            addKey(list, false, AbstractAppKeyGenerator.CHOOSE_DIALOG, FIELDS, fieldName, fieldOrGetterName);
            addKey(list, app.getName(), AbstractAppKeyGenerator.CHOOSE_DIALOG, FIELDS, fieldName, fieldOrGetterName); //deprecated
        } else {
            final Deque<String> parentNames = new LinkedList<>();
            while (parent != null && !(parent instanceof TabDefinition)) {
                String parentName = getParentName(parent);
                if (parentName != null) {
                    parentNames.addFirst(parentName);
                }
                parent = getParentViaCast(parent);
            }
            final String parentKeys = StringUtils.join(parentNames, "." + FIELDS + ".");
            final String parentKeyPart = StringUtils.join(parentNames, '.').replace(':', '-'); //deprecated

            if (parent instanceof TabDefinition) {

                TabDefinition tab = (TabDefinition) parent;
                final String tabName = tab.getName();
                final FormDefinition formDef = getParentViaCast(tab);
                final String rawDialogID = getIdOrNameForUnknownRoot(formDef, false);
                final String dialogID = keyify(rawDialogID);
                final String dialogIdNoModuleName = getIdWithoutModuleName(rawDialogID);
                final String moduleName = getModuleName(rawDialogID);

                // in case of a field in field
                if (parentNames.size() > 0) {
                    if (moduleName != null) {
                        addKey(list, false, moduleName, DIALOGS, dialogIdNoModuleName, FORM, TABS, tabName, FIELDS, parentKeys, FIELDS, fieldName, fieldOrGetterName);
                    }
                    addKey(list, false, DIALOGS, dialogIdNoModuleName, FORM, TABS, tabName, FIELDS, parentKeys, FIELDS, fieldName, fieldOrGetterName);
                    addKey(list, false, FORM, TABS, tabName, FIELDS, parentKeys, FIELDS, fieldName, fieldOrGetterName);

                    // <dialogId>.<tabName>.<parentFieldNames_separated_by_dots>.<fieldName>.<property>
                    // <dialogId>.<tabName>.<parentFieldNames_separated_by_dots>.<fieldName> (in case of property==label)
                    addKey(list, true, dialogID, tabName, parentKeyPart, fieldName, fieldOrGetterName); //deprecated
                } else {
                    if (moduleName != null) {
                        addKey(list, false, moduleName, DIALOGS, dialogIdNoModuleName, FORM, TABS, tabName, FIELDS, fieldName, fieldOrGetterName);
                    }
                    addKey(list, false, DIALOGS, dialogIdNoModuleName, FORM, TABS, tabName, FIELDS, fieldName, fieldOrGetterName);
                }
                addKey(list, false, FORM, TABS, tabName, FIELDS, fieldName, fieldOrGetterName);

                //deprecated:
                // <dialogId>.<tabName>.<fieldName>.<property>
                // <dialogId>.<tabName>.<fieldName> (in case of property==label)
                addKey(list, dialogID, tabName, fieldName, fieldOrGetterName);
                // <tabName>.<fieldName> (in case of property==label)
                addKey(list, tabName, fieldName, fieldOrGetterName);
                // <dialogId>.<fieldName>.<property>
                // <dialogId>.<fieldName> (in case property==label)
                addKey(list, dialogID, fieldName, fieldOrGetterName);
                String[] parts = StringUtils.split(dialogID, ".");
                if (parts.length > 1) {
                    String dialogIDNoModuleName = parts[parts.length - 1];
                    addKey(list, dialogIDNoModuleName, fieldName, fieldOrGetterName);
                    addKey(list, dialogIDNoModuleName, tabName, fieldName, fieldOrGetterName);
                }
                //end of deprecated keys
            } else {
                // In case we didn't encounter parent tab definition - we simply generated a key based on dot-separated parent names
                addKey(list, false, parentKeyPart, FIELDS, fieldName, fieldOrGetterName);
                addKey(list, parentKeyPart, fieldName, fieldOrGetterName); //deprecated
            }
            addKey(list, false, FIELDS, fieldName, fieldOrGetterName);
        }
    }

    /**
     * Dirty hack, as the ChooseDialogDefinition is defined in dependent module.
     */
    private boolean isChooseDialog(Class<?> clazz) {
        /**
         * We can't really use something smarter than the current implementation due to following reasons:
         * -  Fetching the ChooseDialogDefinition class with reflection and using Class#isAssignableFrom is practically impossible to test
         * (test classes can't access ChooseDialogDefinition).
         * - Using String#endsWith() is not feasible because enhancer appends its suffix to the class name.
         */
        return clazz.getSimpleName().contains(CHOOSE_DIALOG_DEFINITION);
    }

    /**
     * TODO - this method has to be considered to be added to the parent class API.
     *
     * @see <a href="http://jira.magnolia-cms.com/browse/MGNLUI-2824/>
     */
    private String getParentName(Object parent) {
        try {
            Method getNameMethod = parent.getClass().getMethod("getName");
            return (String) getNameMethod.invoke(parent);
        } catch (IllegalAccessException e) {
            log.warn("Cannot obtain name of parent object: " + e.getMessage());
        } catch (SecurityException e) {
            log.warn("Cannot obtain name of parent object: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            log.warn("Cannot obtain name of parent object: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Cannot obtain name of parent object: " + e.getMessage());
        } catch (InvocationTargetException e) {
            log.warn("Cannot obtain name of parent object: " + e.getMessage());
        }

        return null;
    }
}
