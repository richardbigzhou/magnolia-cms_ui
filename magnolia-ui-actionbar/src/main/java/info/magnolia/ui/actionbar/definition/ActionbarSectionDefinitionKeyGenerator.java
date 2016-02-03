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
package info.magnolia.ui.actionbar.definition;

import info.magnolia.i18nsystem.AbstractI18nKeyGenerator;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.SubAppDescriptor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * An I18n key generator for the actionbar section labels.
 */
public class ActionbarSectionDefinitionKeyGenerator extends AbstractI18nKeyGenerator<ActionbarSectionDefinition> {

    /**
     * Will generate keys for the message bundle in the following form <code> &lt;app-name&gt;.&lt;sub-app-name&gt;.actionbar.sections.&lt;section-name&gt;[.name of getter or field annotated with {@link info.magnolia.i18nsystem.I18nText}]</code>.
     */
    @Override
    protected void keysFor(List<String> keys, ActionbarSectionDefinition sectionDefinition, AnnotatedElement el) {
        Object root = getRoot(sectionDefinition);
        if (!(root instanceof AppDescriptor)) {
            // not an app descriptor, but can be MessageView with an ID
            try {
                final Method getId = root.getClass().getMethod("getId");
                String messageViewId = (String) getId.invoke(root);
                addKey(keys, messageViewId.replace(':', '.').replace('/', '.'), "actionbar", "sections", sectionDefinition.getName(), fieldOrGetterName(el));
            } catch (NoSuchMethodException e) {
                // not a MessageView - log this?
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e); // TODO MGNLUI-2031
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e); // TODO MGNLUI-2031
            }

            return;
        }
        AppDescriptor appDescriptor = (AppDescriptor) root;
        SubAppDescriptor subAppDescriptor = null;
        List<?> ancestors = getAncestors(sectionDefinition);
        for (Object ancestor : ancestors) {
            if (ancestor instanceof SubAppDescriptor) {
                subAppDescriptor = (SubAppDescriptor) ancestor;
                break;
            }
        }
        final String appName = appDescriptor.getName();
        final String sectionName = sectionDefinition.getName();
        final String subappName = subAppDescriptor != null ? subAppDescriptor.getName() : "";
        addKey(keys, appName, subappName, "actionbar", "sections", sectionName, fieldOrGetterName(el));
    }


}
