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
package info.magnolia.ui.api.action;

import info.magnolia.i18nsystem.I18nKeyGenerator;
import info.magnolia.i18nsystem.NullKeyGenerator;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.i18n.AbstractAppKeyGenerator;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * An I18n key generator for the actionbar actions (labels, descriptions, errors, etc.).
 */
public class ActionDefinitionKeyGenerator extends AbstractAppKeyGenerator<ActionDefinition> {

    private static final String ACTIONS = "actions";
    private static final String DIALOGS = "dialogs";

    /**
     * Will generate keys for the message bundle in the following form <code> &lt;app-name&gt;.&lt;sub-app-name&gt;.actions.&lt;action-name&gt;[.name of getter or field annotated with {@link info.magnolia.i18nsystem.I18nText}]</code>.
     * <p>
     * Also generates "default" keys for all actions. Still unique keys for those actions are generated in case one wants to override the default ones. For example, if your app has a commit action defined like this <code>/modules/my-module/apps/my-app/subApps/detail/actions/commit</code> a fallback key will in the form <code>actions.commit</code> will be generated besides a <code>my-module.detail.actions.commit</code> key.
     */
    @Override
    protected void keysFor(List<String> keys, ActionDefinition actionDefinition, AnnotatedElement el) {
        final String fieldOrGetterName = fieldOrGetterName(el);
        final String actionName = actionDefinition.getName();

        final Object root = getRoot(actionDefinition);

        if (root instanceof AppDescriptor) {
            final AppDescriptor appDescriptor = (AppDescriptor) root;

            Object parent = getParentViaCast(actionDefinition);
            if (parent instanceof SubAppDescriptor) {
                final SubAppDescriptor subAppDescriptor = (SubAppDescriptor) parent;
                addKey(keys, false, APPS, appDescriptor.getName(), SUB_APPS, subAppDescriptor.getName(), ACTIONS, actionName, fieldOrGetterName);
                addKey(keys, false, SUB_APPS, subAppDescriptor.getName(), ACTIONS, actionName, fieldOrGetterName);
                addKey(keys, appDescriptor.getName(), subAppDescriptor.getName(), ACTIONS, actionName, fieldOrGetterName); //deprecated
            } else {
                addKey(keys, false, APPS, appDescriptor.getName(), CHOOSE_DIALOG, ACTIONS, actionName, fieldOrGetterName);
                addKey(keys, false, CHOOSE_DIALOG, ACTIONS, actionName, fieldOrGetterName);
                addKey(keys, appDescriptor.getName(), CHOOSE_DIALOG, ACTIONS, actionName, fieldOrGetterName); //deprecated
            }
        } else {
            final List<String> ancestorKeys = getKeysFromAncestors(actionDefinition, el, root);
            if (ancestorKeys.isEmpty()) {
                final String rawIdOrName = getIdOrNameForUnknownRoot(actionDefinition, false);
                final String moduleName = getModuleName(rawIdOrName);
                final String idOrNameNoModuleName = getIdWithoutModuleName(rawIdOrName);
                final String idOrName = keyify(rawIdOrName);
                if (idOrName != null) {
                    if (moduleName != null) {
                        addKey(keys, false, moduleName, DIALOGS, idOrNameNoModuleName, ACTIONS, actionName, fieldOrGetterName);
                    }
                    addKey(keys, false, DIALOGS, idOrNameNoModuleName, ACTIONS, actionName, fieldOrGetterName);
                    addKey(keys, idOrName, ACTIONS, actionName, fieldOrGetterName); //deprecated
                    if (StringUtils.isNotEmpty(idOrNameNoModuleName)) {
                        addKey(keys, idOrNameNoModuleName, ACTIONS, actionName, fieldOrGetterName); //deprecated
                    }
                }
            } else {
                addKey(keys, false, StringUtils.join(ancestorKeys, '.'), ACTIONS, actionName, fieldOrGetterName);
                addKey(keys, StringUtils.join(ancestorKeys, '.'), ACTIONS, actionName, fieldOrGetterName); //deprecated
            }
        }
        // add a fallback key for all actions
        addKey(keys, ACTIONS, actionName, fieldOrGetterName);
    }

    private List<String> getKeysFromAncestors(final ActionDefinition actionDefinition, final AnnotatedElement el, final Object root) {
        final List<I18nKeyGenerator> keyGenerators = getAncestorKeyGenerators(actionDefinition);

        final List<String> ancestorKeys = new ArrayList<>();

        for (I18nKeyGenerator keygen : keyGenerators) {
            if (keygen instanceof NullKeyGenerator) {
                continue;
            }

            final String[] keysTmp = keygen.keysFor(null, root, el);
            Collection<String> ancestorGeneratedKeys = Collections2.filter(Arrays.asList(keysTmp), new Predicate<String>() {

                @Override
                public boolean apply(String input) {
                    if (StringUtils.isNotBlank(input)) {
                        return true;
                    }
                    return false;
                }
            });
            if (ancestorGeneratedKeys.isEmpty()) {
                continue;
            }
            final String key = ancestorGeneratedKeys.iterator().next();
            ancestorKeys.add(StringUtils.substringBefore(key, ".label"));
        }
        return ancestorKeys;
    }
}
