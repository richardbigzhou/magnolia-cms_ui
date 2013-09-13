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
package info.magnolia.ui.api.action;

import info.magnolia.i18n.AbstractI18nKeyGenerator;
import info.magnolia.i18n.I18nKeyGenerator;
import info.magnolia.i18n.NullKeyGenerator;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.SubAppDescriptor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.reflections.ReflectionUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * An I18n key generator for the actionbar actions (labels, descriptions, errors, etc.).
 */
public class ActionDefinitionKeyGenerator extends AbstractI18nKeyGenerator<ActionDefinition> {

    /**
     * Will generate keys for the message bundle in the following form <code> &lt;app-name&gt;.&lt;sub-app-name&gt;.actions.&lt;action-name&gt;[.name of getter or field annotated with {@link info.magnolia.i18n.I18nText}]</code>.
     */
    @Override
    protected void keysFor(List<String> keys, ActionDefinition actionDefinition, AnnotatedElement el) {
        final Object root = getRoot(actionDefinition);

        if (root instanceof AppDescriptor) {
            final AppDescriptor appDescriptor = (AppDescriptor) root;
            // TODO MGNLUI-2083
            Object parent = getParentViaCast(actionDefinition);
            if (parent instanceof SubAppDescriptor) {
                final SubAppDescriptor subAppDescriptor = (SubAppDescriptor) parent;
                addKey(keys, appDescriptor.getName(), subAppDescriptor.getName(), "actions", actionDefinition.getName(), fieldOrGetterName(el));
            } else {
                addKey(keys, appDescriptor.getName(), "chooseDialog", "actions", actionDefinition.getName(), fieldOrGetterName(el));
            }
        } else {
            final List<String> ancestorKeys = getKeysfromAncestors(actionDefinition, el, root);
            if (ancestorKeys.isEmpty()) {
                String idOrName = getIdOrNameForUnknownRoot(root);
                if (idOrName == null) {
                    addKey(keys, "actions", actionDefinition.getName(), fieldOrGetterName(el));
                } else {
                    addKey(keys, idOrName, "actions", actionDefinition.getName(), fieldOrGetterName(el));
                }
            } else {
                addKey(keys, StringUtils.join(ancestorKeys, '.'), "actions", actionDefinition.getName(), fieldOrGetterName(el));
            }
        }
    }

    private List<String> getKeysfromAncestors(final ActionDefinition actionDefinition, final AnnotatedElement el, final Object root) {
        final List<I18nKeyGenerator> keyGenerators = getAncestorKeyGenerators(actionDefinition);

        final List<String> ancestorKeys = new ArrayList<String>();

        for (I18nKeyGenerator keygen : keyGenerators) {
            if (keygen instanceof NullKeyGenerator) {
                continue;
            }

            final String[] keysTmp = keygen.keysFor(null, root, el);
            Collection<String> ancestorGeneratedKeys = Collections2.filter(Arrays.asList(keysTmp), new Predicate<String>() {

                @Override
                public boolean apply(String input) {
                    if (StringUtils.isNotBlank(input) && !input.endsWith(".label")) {
                        return true;
                    }
                    return false;
                }
            });
            if (ancestorGeneratedKeys.isEmpty()) {
                continue;
            }
            final String key = ancestorGeneratedKeys.iterator().next();
            ancestorKeys.add(key);
        }
        return ancestorKeys;
    }

    private String getIdOrNameForUnknownRoot(Object root) {
        @SuppressWarnings("unchecked")
        final Set<Method> methods = ReflectionUtils.getMethods(root.getClass(), new Predicate<Method>() {

            @Override
            public boolean apply(Method input) {
                if ("getId".equals(input.getName()) || "getName".equals(input.getName())) {
                    return true;
                }
                return false;
            }
        });
        try {
            if (methods.isEmpty()) {
                return null;
            }
            String idOrName = (String) methods.iterator().next().invoke(root);
            return idOrName.replaceAll(":", ".");
        } catch (IllegalArgumentException e) {
            // TODO how do we handle this?
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO how do we handle this?
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO how do we handle this?
            e.printStackTrace();
        }
        return null;
    }
}
