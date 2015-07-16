/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.dialog.choosedialog;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.overlay.OverlayLayer;
import info.magnolia.ui.dialog.actionarea.DialogActionExecutor;
import info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenter;
import info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenterImpl;
import info.magnolia.ui.dialog.actionarea.renderer.ActionRenderer;
import info.magnolia.ui.dialog.actionarea.renderer.DefaultEditorActionRenderer;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaView;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaViewImpl;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.factory.TextFieldFactory;
import info.magnolia.ui.form.fieldtype.definition.ConfiguredFieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.definition.FieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionProvider;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionRegistry;
import info.magnolia.ui.form.validator.registry.FieldValidatorFactoryFactory;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;

import junit.framework.TestCase;

/**
 * Test for {@link  ChooseDialogPresenterImplTest}.
 */
public class ChooseDialogPresenterImplTest extends TestCase {

    private UiContext uiContext = mock(UiContext.class);
    private ChooseDialogPresenterImpl presenter;
    private ConfiguredChooseDialogDefinition configuredChooseDialogDefinition = new ConfiguredChooseDialogDefinition();
    private ComponentProvider componentProvider = mock(ComponentProvider.class);

    @Override
    @Before
    public void setUp() throws Exception {
        I18nContentSupport i18nContentSupport = mock(I18nContentSupport.class);
        JcrContentConnector contentConnector = mock(JcrContentConnector.class);
        FieldTypeDefinitionRegistry fieldDefinitionRegistery = createFieldTypeRegistery();
        FieldFactoryFactory fieldFactoryFactory = new FieldFactoryFactory(componentProvider, fieldDefinitionRegistery, mock(FieldValidatorFactoryFactory.class));
        DialogActionExecutor executor = new DialogActionExecutor(componentProvider);
        SimpleTranslator i18n = mock(SimpleTranslator.class);
        EditorActionAreaView actionAreaView = new EditorActionAreaViewImpl();
        TestEditorActionAreaPresenterImpl actionAreaPresenter = new TestEditorActionAreaPresenterImpl(actionAreaView);
        final String workspaceName = "workspace";
        ChooseDialogView view = new ChooseDialogViewImpl();
        I18nizer i18nizer = new ProxytoysI18nizer(mock(TranslationService.class), mock(LocaleProvider.class));
        TextFieldFactory textFieldFactory = mock(TextFieldFactory.class);
        ConfiguredJcrContentConnectorDefinition configuredJcrContentConnectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        OverlayCloser overlayCloser = mock(OverlayCloser.class);

        presenter = new ChooseDialogPresenterImpl(fieldFactoryFactory, componentProvider, i18nContentSupport, executor, view, i18nizer, i18n, contentConnector);
        when(componentProvider.newInstance(EditorActionAreaPresenter.class)).thenReturn(actionAreaPresenter);
        Field<String> field = mock(Field.class);
        when(textFieldFactory.createField()).thenReturn(field);
        when(componentProvider.newInstance(eq(TextFieldFactory.class), any(), any())).thenReturn(textFieldFactory);
        when(componentProvider.getComponent(ActionRenderer.class)).thenReturn(new DefaultEditorActionRenderer());
        configuredJcrContentConnectorDefinition.setWorkspace(workspaceName);
        when(contentConnector.getContentConnectorDefinition()).thenReturn(configuredJcrContentConnectorDefinition);
        view.setActionAreaView(actionAreaView);
        when(uiContext.openOverlay(view, OverlayLayer.ModalityLevel.STRONG)).thenReturn(overlayCloser);
    }

    @Test
    public void testCloseHandlerIsInvokedOnCloseDialog() throws Exception {
        // WHEN
        ChooseDialogCallback callback = mock(ChooseDialogCallback.class);
        configuredChooseDialogDefinition.setField(new TextFieldDefinition());
        presenter.start(callback, configuredChooseDialogDefinition, uiContext, "test");
        presenter.closeDialog();

        //THEN
        verify(callback, times(1)).onCancel();
    }

    @Test
    public void useDefaultItemIdWhenChosenItemIsNull() throws Exception {
        // GIVEN
        ContentConnector contentConnector = mock(ContentConnector.class);

        final Object defaultItemId = new Object();
        when(contentConnector.getDefaultItemId()).thenReturn(defaultItemId);

        final Item defaultItem = mock(Item.class);
        when(defaultItem.toString()).thenReturn("default item");
        when(contentConnector.getItem(eq(defaultItemId))).thenReturn(defaultItem);

        final Object anotherId = new Object();
        final Item nonDefaultItem = mock(Item.class);
        when(nonDefaultItem.toString()).thenReturn("non-default item");
        when(contentConnector.getItem(eq(anotherId))).thenReturn(nonDefaultItem);

        ChooseDialogPresenterImpl chooseDialogPresenterImpl = new ChooseDialogPresenterImpl(null, null, null, null, null, null, null, contentConnector);

        // WHEN
        Object[] actionParams = chooseDialogPresenterImpl.getActionParameters("foo");

        // THEN
        assertThat(Arrays.asList(actionParams), hasItem(defaultItem));
    }

    private class TestEditorActionAreaPresenterImpl extends EditorActionAreaPresenterImpl {

        public TestEditorActionAreaPresenterImpl(EditorActionAreaView formView) {
            super(formView, ChooseDialogPresenterImplTest.this.componentProvider);
        }

        @Override
        public EditorActionAreaView getView() {
            return super.getView();
        }

    }

    private FieldTypeDefinitionRegistry createFieldTypeRegistery() {
        FieldTypeDefinitionRegistry registery = new FieldTypeDefinitionRegistry();

        ConfiguredFieldTypeDefinition textFieldDefinition = new ConfiguredFieldTypeDefinition();
        textFieldDefinition.setDefinitionClass(TextFieldDefinition.class);
        textFieldDefinition.setFactoryClass(TextFieldFactory.class);
        registery.register(new TestFieldTypeDefinitionProvider("text", textFieldDefinition));


        return registery;
    }

    private class TestFieldTypeDefinitionProvider implements FieldTypeDefinitionProvider {

        private String id;
        private FieldTypeDefinition definition;

        public TestFieldTypeDefinitionProvider(String id, FieldTypeDefinition definition) {
            this.id = id;
            this.definition = definition;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public FieldTypeDefinition getFieldTypeDefinition() throws RegistrationException {
            return definition;
        }

    }
}
