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
package info.magnolia.ui.form.field.factory;

import static info.magnolia.ui.vaadin.integration.jcr.ModelConstants.JCR_NAME;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import info.magnolia.ui.form.field.definition.CodeFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;

import java.lang.reflect.Method;

import org.junit.Test;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.client.AceEditorState;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Field;

public class CodeFieldFactoryTest extends AbstractFieldFactoryTestCase<CodeFieldDefinition> {

    private CodeFieldFactory factory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        factory = new CodeFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport);
        factory.setComponentProvider(componentProvider);
    }

    @Test
    public void createCodeField() {
        // GIVEN
        // WHEN
        Field<String> field = factory.createField();

        // THEN
        assertThat(field, instanceOf(AceEditor.class));
    }

    @Test
    public void createCodeFieldChangeValue() {
        // GIVEN
        baseItem.addItemProperty(propertyName, new DefaultProperty<>(String.class, "private String s"));
        Field<String> field = factory.createField();
        assertEquals("private String s", field.getValue());

        // WHEN
        field.setValue("new Value");

        // THEN
        assertEquals("new Value", baseItem.getItemProperty(propertyName).getValue());
    }

    @Test
    public void createFieldSetsHeightAccordingToDefinition() {
        // GIVEN
        definition.setHeight(500);

        // WHEN
        AceEditor aceEditor = (AceEditor) factory.createField();

        // THEN
        assertThat(aceEditor.getHeight(), is(500f));
    }

    @Test
    public void createFieldSetsModeIfLanguageIsPresent() throws Exception {
        // GIVEN
        definition.setLanguage("freemarker");

        // WHEN
        AceEditor aceEditor = (AceEditor) factory.createField();
        AceEditorState aceEditorState = editorStateFor(aceEditor);

        // THEN
        assertThat(aceEditorState.mode, is(AceMode.ftl.toString()));
    }

    @Test
    public void createFieldResolvesAceModeFromFileExtension() throws Exception {
        // GIVEN
        definition.setFileNameProperty(JCR_NAME);
        baseItem.addItemProperty(JCR_NAME, new ObjectProperty<>("test.ftl"));

        // WHEN
        AceEditor aceEditor = (AceEditor) factory.createField();
        AceEditorState aceEditorState = editorStateFor(aceEditor);

        // THEN
        assertThat(aceEditorState.mode, is(AceMode.ftl.toString()));
    }

    @Test
    public void createFieldResolvesAceModeFromAdditionalMappingsIfAceModeDoesntMatch() throws Exception {
        // GIVEN
        definition.setFileNameProperty(JCR_NAME);
        baseItem.addItemProperty(JCR_NAME, new ObjectProperty<>("test.hh"));

        // WHEN
        AceEditor aceEditor = (AceEditor) factory.createField();
        AceEditorState aceEditorState = editorStateFor(aceEditor);

        // THEN
        assertThat(aceEditorState.mode, is(AceMode.c_cpp.toString()));
    }

    @Test
    public void createFieldFavoursLanguageOverFileNameProperty() throws Exception {
        // GIVEN
        definition.setLanguage("freemarker");
        definition.setFileNameProperty(JCR_NAME);
        baseItem.addItemProperty(JCR_NAME, new ObjectProperty<>("test.ada"));

        // WHEN
        AceEditor aceEditor = (AceEditor) factory.createField();
        AceEditorState aceEditorState = editorStateFor(aceEditor);

        // THEN
        assertThat(aceEditorState.mode, is(AceMode.ftl.toString()));
    }

    @Test
    public void createFieldUsesDefaultModeIfLanguageAndFileNamePropertyAreNotPresent() throws Exception {
        // GIVEN

        // WHEN
        AceEditor aceEditor = (AceEditor) factory.createField();
        AceEditorState aceEditorState = editorStateFor(aceEditor);

        // THEN
        assertThat(aceEditorState.mode, is(AceMode.text.toString()));
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        CodeFieldDefinition fieldDefinition = new CodeFieldDefinition();
        fieldDefinition.setName(propertyName);
        this.definition = fieldDefinition;
    }

    // We have to use reflection because AceEditor doesn't expose its AceMode for assertions.
    private AceEditorState editorStateFor(AceEditor aceEditor) throws Exception {
        Class<? extends AceEditor> aClass = aceEditor.getClass();
        Method method = aClass.getDeclaredMethod("getState");
        method.setAccessible(true);
        return (AceEditorState) method.invoke(aceEditor);
    }
}
