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
package info.magnolia.ui.dialog.formdialog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.dialog.BaseDialogPresenterTest;
import info.magnolia.ui.dialog.actionarea.DialogActionExecutor;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for FormDialogPresenterImpl. ATM only testing the correct calls of the {@link TranslationService}.
 * TODO: these tests should be moved. They really only just test the integration of DialogDefinitions and "child" objects with the I18nSystem framework.
 */
public class FormDialogPresenterImplTest {

    private DialogDefinitionRegistry dialogDefinitionRegistry;
    private FormBuilder formBuilder;
    private FormDialogPresenterImpl presenter;

    private ProxytoysI18nizer i18nizer;

    @Before
    public void setUp() throws Exception {
        final TranslationService service = new BaseDialogPresenterTest.TestTranslationService();
        final LocaleProvider localeProvider = new LocaleProvider() {
            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }
        };
        i18nizer = new ProxytoysI18nizer(service, localeProvider);

        dialogDefinitionRegistry = mock(DialogDefinitionRegistry.class);
        formBuilder = mock(FormBuilder.class);
        presenter = new FormDialogPresenterImpl(dialogDefinitionRegistry, formBuilder, mock(ComponentProvider.class), mock(DialogActionExecutor.class), mock(FormView.class), i18nizer, mock(SimpleTranslator.class), mock(AvailabilityChecker.class), mock(ContentConnector.class));
    }

    @Test
    public void testFormLabel() throws Exception {
        // GIVEN
        // dialog
        ConfiguredFormDialogDefinition def = getBasicDialogDefinition();
        // form
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        def.setForm(form);

        // WHEN
        ConfiguredFormDialogDefinition decoratedDialogDefinition = i18nizer.decorate(def);

        // THEN
        assertEquals("translated with key [dialogID.label] and basename [null] and locale [en]", decoratedDialogDefinition.getForm().getLabel());
    }

    @Test
    public void testTabLabel() throws Exception {
        // GIVEN
        // dialog
        ConfiguredFormDialogDefinition def = getBasicDialogDefinition();
        // form
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        def.setForm(form);
        // tab
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("tab1");
        form.addTab(tab);

        // WHEN
        ConfiguredFormDialogDefinition decoratedDialogDefinition = i18nizer.decorate(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.label] and basename [null] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getLabel());
    }

    @Test
    public void testFieldLabel() throws Exception {
        // GIVEN
        // dialog
        ConfiguredFormDialogDefinition def = getBasicDialogDefinition();
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
        ConfiguredFormDialogDefinition decoratedDialogDefinition = i18nizer.decorate(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.label] and basename [null] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getLabel());
    }

    @Test
    public void testFieldDescription() throws Exception {
        // GIVEN
        // dialog
        ConfiguredFormDialogDefinition def = getBasicDialogDefinition();
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
        ConfiguredFormDialogDefinition decoratedDialogDefinition = i18nizer.decorate(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.description] and basename [null] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getDescription());
    }

    @Test
    public void testFieldLabelWithAllBasenames() throws Exception {
        // GIVEN
        // dialog
        ConfiguredFormDialogDefinition def = getBasicDialogDefinition();
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
        ConfiguredFormDialogDefinition decoratedDialogDefinition = i18nizer.decorate(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.label] and basename [basenameField] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getLabel());
    }

    @Test
    public void testFieldLabelWithTabBasename() throws Exception {
        // GIVEN
        // dialog
        ConfiguredFormDialogDefinition def = getBasicDialogDefinition();
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
        ConfiguredFormDialogDefinition decoratedDialogDefinition = i18nizer.decorate(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.label] and basename [basenameT] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getLabel());
    }

    @Test
    public void testFieldLabelWithFormBasename() throws Exception {
        // GIVEN
        // dialog
        ConfiguredFormDialogDefinition def = getBasicDialogDefinition();
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
        ConfiguredFormDialogDefinition decoratedDialogDefinition = i18nizer.decorate(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.label] and basename [basenameF] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getLabel());
    }

    @Test
    public void testFieldLabelWithDialogBasename() throws Exception {
        // GIVEN
        // dialog
        ConfiguredFormDialogDefinition def = getBasicDialogDefinition();
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
        ConfiguredFormDialogDefinition decoratedDialogDefinition = i18nizer.decorate(def);

        // THEN
        assertEquals("translated with key [dialogID.tab1.field1.label] and basename [basenameD] and locale [en]", decoratedDialogDefinition.getForm().getTabs().get(0).getFields().get(0).getLabel());
    }

    @Test
    public void configuredKeyOverridesGeneratedKey() throws Exception {
        // GIVEN
        // dialog
        ConfiguredFormDialogDefinition def = getBasicDialogDefinition();
        def.setLabel("foo.bar");
        // form
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        form.setLabel("baz.qux");
        def.setForm(form);

        // WHEN
        ConfiguredFormDialogDefinition decoratedDialogDefinition = i18nizer.decorate(def);

        // THEN
        assertEquals("translated with key [foo.bar] and basename [null] and locale [en]", decoratedDialogDefinition.getLabel());
        assertEquals("translated with key [baz.qux] and basename [null] and locale [en]", decoratedDialogDefinition.getForm().getLabel());
    }

    private ConfiguredFormDialogDefinition getBasicDialogDefinition() {
        ConfiguredFormDialogDefinition cdd = new ConfiguredFormDialogDefinition();
        cdd.setId("dialogID");
        return cdd;
    }
}
