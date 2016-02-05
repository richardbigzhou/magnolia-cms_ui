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
package info.magnolia.ui.workbench.column.definition;

import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.i18n.AbstractAppKeyGenerator;
import info.magnolia.ui.workbench.definition.ConfiguredContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

/**
 * Key generator for {@link ColumnDefinition}. Generates key in form &lt;app-name&gt;.&lt;sub-app-name&gt;.views.&lt;view-name&gt;.&lt;column-name&gt;.&lt;property-name&gt;.
 */
public class ColumnDefinitionKeyGenerator extends AbstractAppKeyGenerator<ColumnDefinition> {

    private static final String WORKBENCH = "workbench";
    private static final String CONTENT_VIEWS = "contentViews";
    private static final String COLUMNS = "columns";

    @Override
    protected void keysFor(List<String> keys, ColumnDefinition columnDefinition, AnnotatedElement el) {
        Object root = getRoot(columnDefinition);
        if (!(root instanceof AppDescriptor)) {
            // TODO handle ChooseDialog if needed
            return;
        }
        AppDescriptor appDescriptor = (AppDescriptor) root;
        SubAppDescriptor subAppDescriptor = null;
        ContentPresenterDefinition contentPresenterDefinition = null;
        List<?> ancestors = getAncestors(columnDefinition);
        for (Object ancestor : ancestors) {
            if (ancestor instanceof SubAppDescriptor) {
                subAppDescriptor = (SubAppDescriptor) ancestor;
                continue;
            }
            if (ancestor instanceof ContentPresenterDefinition) {
                contentPresenterDefinition = (ContentPresenterDefinition) ancestor;
                continue;
            }
        }
        final String appName = appDescriptor.getName();
        final String viewName = this.getViewName(contentPresenterDefinition);
        final String columnName = columnDefinition.getName();
        final String fieldOrGetterName = fieldOrGetterName(el);
        if (subAppDescriptor != null) {
            // sub-app specific, view-specific
            addKey(keys, false, APPS, appName, SUB_APPS, subAppDescriptor.getName(), WORKBENCH, CONTENT_VIEWS, viewName, COLUMNS, columnName, fieldOrGetterName);
            addKey(keys, false, SUB_APPS, subAppDescriptor.getName(), WORKBENCH, CONTENT_VIEWS, viewName, COLUMNS, columnName, fieldOrGetterName);
            addKey(keys, false, WORKBENCH, CONTENT_VIEWS, viewName, COLUMNS, columnName, fieldOrGetterName);

            // app specific, view-specific
            addKey(keys, appName, subAppDescriptor.getName(), "views", contentPresenterDefinition.getViewType(), columnName, fieldOrGetterName); //deprecated
            addKey(keys, appName, subAppDescriptor.getName(), "views", columnName, fieldOrGetterName); //deprecated
        } else {
            addKey(keys, false, APPS, appName, CONTENT_VIEWS, viewName, COLUMNS, columnName, fieldOrGetterName);
            addKey(keys, false, CONTENT_VIEWS, viewName, COLUMNS, columnName, fieldOrGetterName);
        }
        addKey(keys, false, COLUMNS, columnName, fieldOrGetterName(el));

        //deprecated:
        // sub-app specific, all views
        addKey(keys, appName, "views", contentPresenterDefinition.getViewType(), columnName, fieldOrGetterName); //deprecated
        // app specific, all views
        addKey(keys, appName, "views", columnName, fieldOrGetterName(el));
        // all apps, view-specific
        addKey(keys, "views", contentPresenterDefinition.getViewType(), columnName, fieldOrGetterName);
        // all apps, all views
        addKey(keys, "views", columnName, fieldOrGetterName(el));
        //end of deprecated keys
    }

    private String getViewName(ContentPresenterDefinition definition) {
        if (definition instanceof ConfiguredContentPresenterDefinition) {
            return ((ConfiguredContentPresenterDefinition) definition).getName();
        }
        return null;
    }
}
