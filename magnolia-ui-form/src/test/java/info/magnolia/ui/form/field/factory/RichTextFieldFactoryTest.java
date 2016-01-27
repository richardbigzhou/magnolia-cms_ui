/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.form.field.factory;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.definition.RichTextFieldDefinition;
import info.magnolia.ui.vaadin.gwt.client.richtext.VMagnoliaRichTextField;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.richtext.MagnoliaRichTextField;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Field;
import com.vaadin.util.CurrentInstance;

public class RichTextFieldFactoryTest extends AbstractFieldFactoryTestCase<RichTextFieldDefinition> {

    private RichTextFieldFactory richTextFieldFactory;
    private AppController appController;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        appController = mock(AppController.class);
        SimpleTranslator i18n = mock(SimpleTranslator.class);
        richTextFieldFactory = new RichTextFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport, appController, i18n);
        richTextFieldFactory.setComponentProvider(componentProvider);

        CurrentInstance.set(VaadinRequest.class, mock(VaadinRequest.class));
    }

    @Test
    public void getField() throws Exception {
        // GIVEN

        // WHEN
        Field field = richTextFieldFactory.createField();

        // THEN
        assertThat(field, instanceOf(MagnoliaRichTextField.class));
        assertThat(((AbstractJcrNodeAdapter) baseItem).getChildren().size(), is(0));
    }

    @Test
    public void openChooseDialogWithPreviouslyStoredPath() throws Exception {
        // GIVEN
        MagnoliaRichTextField field = (MagnoliaRichTextField) richTextFieldFactory.createFieldComponent();
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put(VMagnoliaRichTextField.VAR_EVENT_PREFIX + "mgnlGetLink", "{'workspace' :'website', 'path': '/travel'}");
        }};

        // WHEN
        field.changeVariables(null, variables);

        // THEN
        verify(appController, atLeastOnce()).openChooseDialog(anyString(), any(UiContext.class), eq("/travel"), any(ChooseDialogCallback.class));
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        RichTextFieldDefinition fieldDefinition = new RichTextFieldDefinition();
        fieldDefinition.setName(propertyName);
        this.definition = fieldDefinition;
    }

}
