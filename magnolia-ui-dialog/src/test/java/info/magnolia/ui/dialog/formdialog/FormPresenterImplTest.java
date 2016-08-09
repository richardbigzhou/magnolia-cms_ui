/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.dialog.formdialog;


import static info.magnolia.test.hamcrest.ExecutionMatcher.throwsNothing;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.hamcrest.Execution;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.FormItem;
import info.magnolia.ui.form.config.FieldConfig;
import info.magnolia.ui.form.config.FormConfig;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.factory.TextFieldFactory;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.form.FormSection;
import info.magnolia.ui.vaadin.form.FormViewReduced;

import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.TextField;

public class FormPresenterImplTest {

    private ConfiguredFormDefinition formDefinition;
    private FormPresenterImpl formPresenter;
    private FormViewReduced formView;
    private Item item;
    private SubAppContext subAppContext;
    private FieldFactoryFactory fieldFactoryFactory;
    private Locale locale;
    private ComponentProvider componentProvider;

    @Before
    public void setUp() throws Exception {
        this.locale = Locale.ENGLISH;
        I18NAuthoringSupport i18NAuthoringSupport = mock(I18NAuthoringSupport.class);
        final SimpleTranslator i18n = mock(SimpleTranslator.class);

        final FieldConfig fieldConfig = new FieldConfig();
        final FormConfig formConfig = new FormConfig();

        this.formDefinition = formConfig.form().tabs(
                formConfig.tab("tab1").label("tab1_label").fields(
                        fieldConfig.text("textField")
                )
        ).definition();

        prepareFieldFactoryFactory();
        prepareSubAppContext();
        ComponentsTestUtil.setInstance(I18NAuthoringSupport.class, i18NAuthoringSupport);
        this.componentProvider = mock(ComponentProvider.class);
        doReturn(mock(BasicTransformer.class)).when(componentProvider).newInstance(eq(BasicTransformer.class), anyVararg());
        this.item = new PropertysetItem();
        FormBuilder formBuilder = new FormBuilder(fieldFactoryFactory, i18NAuthoringSupport, subAppContext, componentProvider);
        this.formPresenter = new FormPresenterImpl(formBuilder, subAppContext);
        this.formView = new ItemFormView(i18n);
    }

    @Test
    public void presentsView() throws Exception {
        // WHEN
        this.formPresenter.presentView(formView, formDefinition, item, mock(FormItem.class));

        // THEN
        final List<FormSection> formSections = formView.getFormSections();
        assertThat(formSections.size(), equalTo(1));
        assertThat(formSections.get(0).getName(), equalTo("tab1"));
        assertThat(formSections.get(0).getCaption(), equalTo("tab1_label"));
        assertThat(formSections.get(0).iterator().next(), instanceOf(TextField.class));
    }

    @Test
    public void handlesLocaleChanges() throws Exception {
        // WHEN
        this.formPresenter.presentView(formView, formDefinition, item, mock(FormItem.class));

        // THEN
        assertThat(formView.getFormSections().get(0).iterator().next().getLocale(), equalTo(Locale.ENGLISH));

        // WHEN
        this.formPresenter.setLocale(Locale.GERMAN);

        // THEN
        final List<FormSection> formSections = formView.getFormSections();
        assertThat(formSections.get(0).iterator().next().getLocale(), equalTo(Locale.GERMAN));
    }

    @Test
    public void emptyTabsDoNotThrownNPE() throws Exception {
        // GIVEN
        final FieldConfig fieldConfig = new FieldConfig();
        final FormConfig formConfig = new FormConfig();
        final FormDefinition formDefinition = formConfig.form()
                .tabs(formConfig.tab("tab1").label("tab1_label").fields(fieldConfig.text("textField")))
                .tabs(formConfig.tab("tab2").label("tab2_label"))
                .definition();

        // WHEN / THEN
        assertThat(new Execution() {
            @Override
            public void evaluate() throws Exception {
                formPresenter.presentView(formView, formDefinition, item, mock(FormItem.class));
            }
        }, throwsNothing());
    }

    private void prepareFieldFactoryFactory() {
        this.fieldFactoryFactory = mock(FieldFactoryFactory.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final TextFieldDefinition textFieldDefinition = (TextFieldDefinition) formDefinition.getTabs().get(0).getFields().get(0);
                final TextFieldFactory fieldFactory = new TextFieldFactory(textFieldDefinition, item, null, null);
                fieldFactory.setComponentProvider(componentProvider);
                fieldFactory.setLocale(subAppContext.getAuthoringLocale());
                return fieldFactory;
            }
        }).when(fieldFactoryFactory).createFieldFactory(any(FieldDefinition.class), anyVararg());
    }

    private void prepareSubAppContext() {
        this.subAppContext = mock(SubAppContext.class);
        final Answer setAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock inv) throws Throwable {
                locale = (Locale) inv.getArguments()[0];
                return null;
            }
        };
        doAnswer(setAnswer).when(subAppContext).setAuthoringLocale(any(Locale.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return locale;
            }
        }).when(subAppContext).getAuthoringLocale();
    }
}