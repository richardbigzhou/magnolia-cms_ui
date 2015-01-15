/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.dialog;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.dialog.formdialog.FormView;
import info.magnolia.ui.form.FormItem;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.vaadin.form.FormSection;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.List;
import java.util.Locale;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Field;
import com.vaadin.ui.HasComponents;

/**
 * FormBuilderTest.
 */
public class FormBuilderTest {

    private MockSession session;
    private Locale locale;

    @Before
    public void setUp() {
        session = new MockSession(RepositoryConstants.WEBSITE);
        MockContext ctx = new MockContext();
        ctx.addSession(RepositoryConstants.WEBSITE, session);
        MgnlContext.setInstance(ctx);
    }

    @Test
    public void testBuildFormRespectAuthorLocale() {
        // GIVEN
        ConfiguredFieldDefinition fieldDefinition = new ConfiguredFieldDefinition();
        fieldDefinition.setI18n(true);
        ConfiguredTabDefinition tabDefinition = new ConfiguredTabDefinition();
        tabDefinition.addField(fieldDefinition);
        ConfiguredFormDefinition formDefinition = new ConfiguredFormDefinition();
        formDefinition.addTab(tabDefinition);

        JcrItemAdapter item = new JcrNodeAdapter(session.getRootNode());
        FormItem parent = null;

        DummyI18NAuthoringSupport i18nAuthoringSupport = new DummyI18NAuthoringSupport();
        i18nAuthoringSupport.setAuthorLocale(new Locale("de"));
        FieldFactoryFactory fieldFactoryFactory = mock(FieldFactoryFactory.class);
        FormBuilder builder = new FormBuilder(fieldFactoryFactory, i18nAuthoringSupport, null);
        FormView view = mock(FormView.class);

        // WHEN
        builder.buildForm(view, formDefinition, item, parent);

        // THEN
        verify(view).setCurrentLocale(locale);
    }

    @Test
    public void buildFormFindsNestedI18nAwareFields() {
        // GIVEN
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        CompositeFieldDefinition compositeField = new CompositeFieldDefinition();
        TextFieldDefinition textField = new TextFieldDefinition();
        textField.setI18n(true);
        compositeField.addField(textField);
        tab.addField(compositeField);
        form.addTab(tab);

        JcrItemAdapter item = new JcrNodeAdapter(session.getRootNode());
        FieldFactoryFactory fieldFactoryFactory = mock(FieldFactoryFactory.class);
        I18NAuthoringSupport i18nAuthoringSupport = mock(I18NAuthoringSupport.class);
        FormBuilder builder = new FormBuilder(fieldFactoryFactory, i18nAuthoringSupport, null);
        FormView view = mock(FormView.class);

        // WHEN
        builder.buildForm(view, form, item, null);

        // THEN
        verify(view).setAvailableLocales(anyList());
    }

    @Test
    public void testBuildReducedFormSkipEmptyTabs() {
        // GIVEN
        ConfiguredTabDefinition emptyTabDefinition = new ConfiguredTabDefinition();
        emptyTabDefinition.setLabel("emptyTab");

        ConfiguredTabDefinition nonEmptyTabDefinition = new ConfiguredTabDefinition();
        nonEmptyTabDefinition.setLabel("nonEmptyTab");
        nonEmptyTabDefinition.addField(new ConfiguredFieldDefinition());

        ConfiguredFormDefinition formDefinition = new ConfiguredFormDefinition();
        formDefinition.addTab(emptyTabDefinition);
        formDefinition.addTab(nonEmptyTabDefinition);

        Field field = mock(Field.class);
        FieldFactory fieldFactory = mock(FieldFactory.class);
        when(fieldFactory.createField()).thenReturn(field);
        FieldFactoryFactory fieldFactoryFactory = mock(FieldFactoryFactory.class);
        when(fieldFactoryFactory.createFieldFactory(any(FieldDefinition.class), anyObject())).thenReturn(fieldFactory);

        FormBuilder builder = new FormBuilder(fieldFactoryFactory, null, null);
        FormView view = mock(FormView.class);

        // WHEN
        builder.buildReducedForm(formDefinition, view, null, mock(FormItem.class));

        // THEN
        verify(view).addFormSection(eq("nonEmptyTab"), any(FormSection.class));
        verify(view, times(0)).addFormSection(eq("emptyTab"), any(FormSection.class));
    }

    private class DummyI18NAuthoringSupport implements I18NAuthoringSupport {

        @Override
        public List<Locale> getAvailableLocales(Node node) {
            return null;
        }

        /**
         * Returns the default locale for the given page, area or component node.
         *
         * TODO: create interface method in {@link info.magnolia.ui.api.i18n.I18NAuthoringSupport}
         */
        public Locale getDefaultLocale(Node node) {
            return null;
        }

        @Override
        public void i18nize(HasComponents fieldContainer, Locale locale) {
        }

        @Override
        public String createI18NURI(Node node, Locale locale) {
            return null;
        }

        @Override
        public Locale getAuthorLocale() {
            return locale;
        }

        public void setAuthorLocale(Locale loc) {
            locale = loc;
        }

    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }
}
