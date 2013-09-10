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
package info.magnolia.ui.dialog;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.Context;
import info.magnolia.i18n.ContextLocaleProvider;
import info.magnolia.i18n.LocaleProvider;
import info.magnolia.i18n.TranslationService;
import info.magnolia.i18n.proxytoys.ProxytoysI18nizer;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.ui.dialog.action.DialogActionExecutor;
import info.magnolia.ui.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.FormBuilder;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.vaadin.dialog.FormDialogView;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for FormDialogPresenterImpl. ATM only testing the correct calls of the {@link TranslationService}.
 */
public class FormDialogPresenterImplTest {

    private FormDialogView view;
    private DialogDefinitionRegistry dialogDefinitionRegistry;
    private DialogActionExecutor actionExecutor;
    private FormBuilder formBuilder;
    private FormDialogPresenterImpl presenter;

    private TranslationService service;
    private ConfiguredDialogDefinition def;

    @Before
    public void setUp() throws Exception {
        // components for the old i18n API
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);

        // locale
        Context ctx = mock(Context.class);
        // current context locale
        when(ctx.getLocale()).thenReturn(new Locale("en"));

        service = new TestTranslationService();
        ProxytoysI18nizer i18nizer = new ProxytoysI18nizer(service, new ContextLocaleProvider(ctx));

        view = mock(FormDialogView.class);
        dialogDefinitionRegistry = mock(DialogDefinitionRegistry.class);
        actionExecutor = mock(DialogActionExecutor.class);
        formBuilder = mock(FormBuilder.class);
        presenter = new FormDialogPresenterImpl(view, dialogDefinitionRegistry, actionExecutor, formBuilder, i18nizer);

    }

    @Test
    public void testFormLabel() throws Exception {
        // GIVEN
        // dialog
        def = getBasicDialogDefinition();
        // form
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        def.setForm(form);

        // WHEN
        DialogDefinition decoratedDialogDefinition = presenter.decorateForI18n(def);

        // THEN
        assertEquals("translated with key [dialogID.label] and basename [null] and locale [en]", decoratedDialogDefinition.getForm().getLabel());
    }

    @Test
    public void testTabLabel() throws Exception {
        // GIVEN
        // dialog
        def = getBasicDialogDefinition();
        // form
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        def.setForm(form);
        // tab
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("tab1");
        form.addTab(tab);

        // WHEN
        DialogDefinition decoratedDialogDefinition = presenter.decorateForI18n(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.label] and basename [null] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getLabel());
    }

    @Test
    public void testFieldLabel() throws Exception {
        // GIVEN
        // dialog
        def = getBasicDialogDefinition();
        // form
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        def.setForm(form);
        // tab
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("tab1");
        form.addTab(tab);
        // field
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("field1");
        tab.addField(field);

        // WHEN
        DialogDefinition decoratedDialogDefinition = presenter.decorateForI18n(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.label] and basename [null] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getLabel());
    }

    @Test
    public void testFieldDescription() throws Exception {
        // GIVEN
        // dialog
        def = getBasicDialogDefinition();
        // form
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        def.setForm(form);
        // tab
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("tab1");
        form.addTab(tab);
        // field
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("field1");
        tab.addField(field);

        // WHEN
        DialogDefinition decoratedDialogDefinition = presenter.decorateForI18n(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.description] and basename [null] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getDescription());
    }

    @Test
    public void testFieldLabelWithAllBasenames() throws Exception {
        // GIVEN
        // dialog
        def = getBasicDialogDefinition();
        def.setI18nBasename("basenameD");
        // form
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        form.setI18nBasename("basenameF");
        def.setForm(form);
        // tab
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("tab1");
        tab.setI18nBasename("basenameT");
        form.addTab(tab);
        // field
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("field1");
        field.setI18nBasename("basenameField");
        tab.addField(field);

        // WHEN
        DialogDefinition decoratedDialogDefinition = presenter.decorateForI18n(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.label] and basename [basenameField] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getLabel());
    }

    @Test
    public void testFieldLabelWithTabBasename() throws Exception {
        // GIVEN
        // dialog
        def = getBasicDialogDefinition();
        def.setI18nBasename("basenameD");
        // form
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        form.setI18nBasename("basenameF");
        def.setForm(form);
        // tab
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("tab1");
        tab.setI18nBasename("basenameT");
        form.addTab(tab);
        // field
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("field1");
        tab.addField(field);

        // WHEN
        DialogDefinition decoratedDialogDefinition = presenter.decorateForI18n(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.label] and basename [basenameT] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getLabel());
    }

    @Test
    public void testFieldLabelWithFormBasename() throws Exception {
        // GIVEN
        // dialog
        def = getBasicDialogDefinition();
        def.setI18nBasename("basenameD");
        // form
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        form.setI18nBasename("basenameF");
        def.setForm(form);
        // tab
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("tab1");
        form.addTab(tab);
        // field
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("field1");
        tab.addField(field);

        // WHEN
        DialogDefinition decoratedDialogDefinition = presenter.decorateForI18n(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.label] and basename [basenameF] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getLabel());
    }

    @Test
    public void testFieldLabelWithDialogBasename() throws Exception {
        // GIVEN
        // dialog
        def = getBasicDialogDefinition();
        def.setI18nBasename("basenameD");
        // form
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        def.setForm(form);
        // tab
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("tab1");
        form.addTab(tab);
        // field
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("field1");
        tab.addField(field);

        // WHEN
        DialogDefinition decoratedDialogDefinition = presenter.decorateForI18n(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.label] and basename [basenameD] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getLabel());
    }

    private ConfiguredDialogDefinition getBasicDialogDefinition() {
        ConfiguredDialogDefinition cdd = new ConfiguredDialogDefinition();
        cdd.setId("dialogID");
        return cdd;
    }

    /**
     * Test translation service.
     */
    public static class TestTranslationService implements TranslationService {

        @Override
        public String translate(LocaleProvider localeProvider, String basename, String[] keys) {
            return "translated with key [" + keys[0] + "] and basename [" + basename + "] and locale [" + localeProvider.getLocale() + "]";
        }
    }

}
