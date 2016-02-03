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
package info.magnolia.ui.workbench.column.definition;

import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.api.app.registry.ConfiguredSubAppDescriptor;
import info.magnolia.ui.workbench.definition.ConfiguredContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ColumnDefinitionKeyGenerator}.
 */
public class ColumnDefinitionKeyGeneratorTest {

    private ConfiguredAppDescriptor app;
    private TestSubAppDef subapp;
    private ConfiguredWorkbenchDefinition wb;
    private ConfiguredContentPresenterDefinition view;
    private AbstractColumnDefinition column;

    private ColumnDefinitionKeyGenerator generator;

    @Before
    public void setUp() {
        // genrator
        generator = new ColumnDefinitionKeyGenerator();
        // initialize
        column = new AbstractColumnDefinition() {
            // nothing
        };
        column.setName("<column-name>");
        view = new ConfiguredContentPresenterDefinition();
        view.setViewType("<view-name>");
        wb = new ConfiguredWorkbenchDefinition();
        subapp = new TestSubAppDef();
        subapp.setName("<subapp>");
        app = new ConfiguredAppDescriptor();
        app.setName("<app>");
        // create the tree
        Map<String, SubAppDescriptor> subapps = new HashMap<String, SubAppDescriptor>();
        subapps.put("<subapp>", subapp);
        app.addSubApp(subapp);
        subapp.setWorkbench(wb);
        List<ContentPresenterDefinition> views = new ArrayList<ContentPresenterDefinition>(1);
        views.add(view);
        wb.addContentView(view);
        List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>(1);
        columns.add(column);
        view.addColumn(column);
    }

    @Test
    public void keysForColumnLabelWithinSubapp() throws SecurityException, NoSuchMethodException {
        // GIVEN
        Method method = column.getClass().getMethod("getLabel");
        // decorate
        I18nizer i18nizer = new ProxytoysI18nizer(mock(TranslationService.class), mock(LocaleProvider.class));
        app = i18nizer.decorate(app);

        // WHEN
        String[] keys = generator.keysFor((String) null, ((TestSubAppDef) app.getSubApps().get("<subapp>")).getWorkbench().getContentViews().get(0).getColumns().get(0), method);

        // THEN
        assertThat(keys, arrayContaining(
                "<app>.<subapp>.views.<view-name>.<column-name>.label",
                "<app>.<subapp>.views.<view-name>.<column-name>",
                "<app>.<subapp>.views.<column-name>.label",
                "<app>.<subapp>.views.<column-name>",

                "<app>.views.<view-name>.<column-name>.label",
                "<app>.views.<view-name>.<column-name>",
                "<app>.views.<column-name>.label",
                "<app>.views.<column-name>",

                "views.<view-name>.<column-name>.label",
                "views.<view-name>.<column-name>",
                "views.<column-name>.label",
                "views.<column-name>"
        ));
    }

    @Test
    public void keysForColumnLabelWithoutSubapp() throws SecurityException, NoSuchMethodException {
        // GIVEN
        Method method = column.getClass().getMethod("getLabel");
        DummyTypeWithNoSubapp appDescriptor = new DummyTypeWithNoSubapp(wb);
        appDescriptor.setName("<app>");
        // decorate
        I18nizer i18nizer = new ProxytoysI18nizer(mock(TranslationService.class), mock(LocaleProvider.class));
        appDescriptor = i18nizer.decorate(appDescriptor);

        // WHEN
        String[] keys = generator.keysFor((String) null, appDescriptor.getWB().getContentViews().get(0).getColumns().get(0), method);

        // THEN
        assertThat(keys, arrayContaining(
                "<app>.views.<view-name>.<column-name>.label",
                "<app>.views.<view-name>.<column-name>",
                "<app>.views.<column-name>.label",
                "<app>.views.<column-name>",

                "views.<view-name>.<column-name>.label",
                "views.<view-name>.<column-name>",
                "views.<column-name>.label",
                "views.<column-name>"
        ));
    }

    private static class TestSubAppDef extends ConfiguredSubAppDescriptor {
        private WorkbenchDefinition wb;

        public TestSubAppDef() {
            super();
        }

        public WorkbenchDefinition getWorkbench() {
            return wb;
        }

        public void setWorkbench(WorkbenchDefinition workbench) {
            this.wb = workbench;
        }
    }

    private static class DummyTypeWithNoSubapp extends ConfiguredAppDescriptor {
        private final WorkbenchDefinition wb;

        public DummyTypeWithNoSubapp(WorkbenchDefinition wb) {
            this.wb = wb;
        }

        public WorkbenchDefinition getWB() {
            return wb;
        }
    }
}
