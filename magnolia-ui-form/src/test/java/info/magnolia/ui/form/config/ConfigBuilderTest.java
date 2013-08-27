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
package info.magnolia.ui.form.config;

import static org.junit.Assert.*;

import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiFieldDefinition;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.PasswordFieldDefinition;
import info.magnolia.ui.form.field.definition.PropertyBuilder;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.definition.TwinColSelectFieldDefinition;
import info.magnolia.ui.form.field.property.CustomPropertyType;
import info.magnolia.ui.form.field.property.PropertyHandler;
import info.magnolia.ui.form.field.property.basic.BasicProperty;
import info.magnolia.ui.form.field.property.basic.BasicPropertyHandler;
import info.magnolia.ui.form.field.property.basic.OptionGroupPropertyHandler;
import info.magnolia.ui.form.field.property.basic.TwinSelectPropertyHandler;
import info.magnolia.ui.form.field.property.composite.CompositeProperty;
import info.magnolia.ui.form.field.property.composite.SimplePropertyCompositeHandler;
import info.magnolia.ui.form.field.property.composite.SwitchableSimplePropertyCompositeHandler;
import info.magnolia.ui.form.field.property.multi.MultiProperty;
import info.magnolia.ui.form.field.property.multi.MultiValuesPropertyMultiHandler;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

import org.junit.Test;

/**
 * .
 */
public class ConfigBuilderTest {

    private String label = "label";
    private String i18nBasename = "i18nBasename";
    private boolean i18n = true;
    private String description = "description";
    private String type = "String";
    private boolean required = true;
    private String requiredErrorMessage = "requiredErrorMessage";
    private boolean readOnly = true;
    private String defaultValue = "defaultValue";
    private String styleName = "styleName";

    @Test
    public void testBasicTextCodeFieldBuilder() {
        // GIVEN
        BasicTextCodeFieldBuilder builder = new BasicTextCodeFieldBuilder("BasicTextCodeFieldBuilder");
        initCommonAttributes(builder, true);
        // Specific to definition
        builder.language("java");

        // WHEN

        // THEN
        checkCommonAttributes(builder);
        assertEquals("java", builder.definition().getLanguage());
    }

    @Test
    public void testTextFieldBuilder() {
        // GIVEN
        TextFieldBuilder builder = new TextFieldBuilder("TextFieldBuilder");
        initCommonAttributes(builder, true);
        // Specific to definition
        builder.rows(10).maxLength(100);

        // WHEN

        // THEN
        checkCommonAttributes(builder);
        assertEquals(10, builder.definition().getRows());
        assertEquals(100, builder.definition().getMaxLength());
    }

    @Test
    public void testCheckboxFieldBuilder() {
        // GIVEN
        CheckboxFieldBuilder builder = new CheckboxFieldBuilder("CheckboxFieldBuilder");
        initCommonAttributes(builder, true);
        // Specific to definition
        builder.buttonLabel("buttonLabel");

        // WHEN

        // THEN
        checkCommonAttributes(builder);
        assertEquals("buttonLabel", builder.definition().getButtonLabel());
    }

    @Test
    public void testStaticFieldBuilder() {
        // GIVEN
        StaticFieldBuilder builder = new StaticFieldBuilder("StaticFieldBuilder");
        initCommonAttributes(builder, true);
        // Specific to definition
        builder.value("value");

        // WHEN

        // THEN
        checkCommonAttributes(builder);
        assertEquals("value", builder.definition().getValue());
    }

    @Test
    public void testRichTextFieldBuilder() {
        // GIVEN
        RichTextFieldBuilder builder = new RichTextFieldBuilder("RichTextFieldBuilder");
        initCommonAttributes(builder, true);
        // WHEN

        // THEN
        checkCommonAttributes(builder);
    }

    @Test
    public void testPasswordFieldBuilder() {
        // GIVEN
        PasswordFieldBuilder builder = new PasswordFieldBuilder("PasswordFieldBuilder");
        initCommonAttributes(builder, true);
        // Specific to definition
        builder.verification(true).verificationMessage("verificationMessage").verificationErrorMessage("verificationErrorMessage");
        // WHEN

        // THEN
        checkCommonAttributes(builder);
        PasswordFieldDefinition definition = (PasswordFieldDefinition) builder.definition();
        assertEquals(true, definition.isVerification());
        assertEquals("verificationMessage", definition.getVerificationMessage());
        assertEquals("verificationErrorMessage", definition.getVerificationErrorMessage());
    }

    @Test
    public void testBasicUploadFieldBuilder() {
        // GIVEN
        BasicUploadFieldBuilder builder = new BasicUploadFieldBuilder("BasicUploadFieldBuilder");
        initCommonAttributes(builder, true);
        // Specific to definition
        builder.binaryNodeName("binaryNodeName").maxUploadSize(100l).allowedMimeTypePattern("*.gif").editFileName(true).editFileFormat(false).selectAnotherCaption("selectAnotherCaption");
        builder.userInterruption("userInterruption").fileDetailFormatCaption("fileDetailFormatCaption").fileDetailHeaderCaption("fileDetailHeaderCaption").fileDetailNameCaption("fileDetailNameCaption").fileDetailSizeCaption("fileDetailSizeCaption");
        builder.fileDetailSourceCaption("fileDetailSourceCaption").selectNewCaption("selectNewCaption").successNoteCaption("successNoteCaption");

        // WHEN

        // THEN
        checkCommonAttributes(builder);
        BasicUploadFieldDefinition definition = (BasicUploadFieldDefinition) builder.definition();

        assertEquals("binaryNodeName", definition.getBinaryNodeName());
        assertEquals(100l, definition.getMaxUploadSize());
        assertEquals("*.gif", definition.getAllowedMimeTypePattern());
        assertEquals(true, definition.isEditFileName());
        assertEquals(false, definition.isEditFileFormat());
        assertEquals("selectAnotherCaption", definition.getSelectAnotherCaption());
        assertEquals("userInterruption", definition.getUserInterruption());
        assertEquals("fileDetailFormatCaption", definition.getFileDetailFormatCaption());
        assertEquals("fileDetailHeaderCaption", definition.getFileDetailHeaderCaption());
        assertEquals("fileDetailNameCaption", definition.getFileDetailNameCaption());
        assertEquals("fileDetailSizeCaption", definition.getFileDetailSizeCaption());
        assertEquals("fileDetailSourceCaption", definition.getFileDetailSourceCaption());
        assertEquals("selectNewCaption", definition.getSelectNewCaption());
        assertEquals("successNoteCaption", definition.getSuccessNoteCaption());

    }

    @Test
    public void testLinkFieldBuilder() {
        // GIVEN
        LinkFieldBuilder builder = new LinkFieldBuilder("LinkFieldBuilder");
        initCommonAttributes(builder, true);
        // Specific to definition
        builder.targetPropertyToPopulate("targetPropertyToPopulate").targetWorkspace("targetWorkspace").targetTreeRootPath("targetTreeRootPath").appName("appName");
        builder.buttonSelectNewLabel("buttonSelectNewLabel").buttonSelectOtherLabel("buttonSelectOtherLabel");

        // WHEN

        // THEN
        checkCommonAttributes(builder);
        LinkFieldDefinition definition = (LinkFieldDefinition) builder.definition();
        assertEquals("targetPropertyToPopulate", definition.getTargetPropertyToPopulate());
        assertEquals("targetWorkspace", definition.getTargetWorkspace());
        assertEquals("targetTreeRootPath", definition.getTargetTreeRootPath());
        assertEquals("appName", definition.getAppName());
        assertEquals("buttonSelectNewLabel", definition.getButtonSelectNewLabel());
        assertEquals("buttonSelectOtherLabel", definition.getButtonSelectOtherLabel());
    }

    @Test
    public void testMultiFieldBuilder() {
        // GIVEN
        MultiFieldBuilder builder = new MultiFieldBuilder("MultiFieldBuilder");
        initCommonAttributes(builder, false);
        // Specific to definition
        builder.buttonSelectRemoveLabel("buttonSelectRemoveLabel").buttonSelectAddLabel("buttonSelectAddLabel");

        // WHEN

        // THEN
        checkCommonAttributes(builder);
        MultiFieldDefinition definition = (MultiFieldDefinition) builder.definition();
        assertEquals("buttonSelectRemoveLabel", definition.getButtonSelectRemoveLabel());
        assertEquals("buttonSelectAddLabel", definition.getButtonSelectAddLabel());
        assertNotNull(definition.getPropertyBuilder());
        PropertyBuilder propertyBuilder = definition.getPropertyBuilder();
        assertEquals(MultiProperty.class.getName(),propertyBuilder.getPropertyType().getName());
        assertEquals(MultiValuesPropertyMultiHandler.class.getName(),propertyBuilder.getPropertyHandler().getName());
    }

    @Test
    public void testTwinColSelectFieldBuilder() {
        // GIVEN
        TwinColSelectFieldBuilder builder = new TwinColSelectFieldBuilder("TwinColSelectFieldBuilder");
        initCommonAttributes(builder, false);
        // Specific to definition
        builder.leftColumnCaption("leftColumnCaption").rightColumnCaption("rightColumnCaption");

        // WHEN

        // THEN
        checkCommonAttributes(builder);
        TwinColSelectFieldDefinition definition = (TwinColSelectFieldDefinition) builder.definition();
        assertEquals("leftColumnCaption", definition.getLeftColumnCaption());
        assertEquals("rightColumnCaption", definition.getRightColumnCaption());
        assertNotNull(definition.getPropertyBuilder());
        PropertyBuilder propertyBuilder = definition.getPropertyBuilder();
        assertEquals(BasicProperty.class.getName(), propertyBuilder.getPropertyType().getName());
        assertEquals(TwinSelectPropertyHandler.class.getName(), propertyBuilder.getPropertyHandler().getName());
    }

    @Test
    public void testOptionGroupFieldBuilder() {
        // GIVEN
        OptionGroupFieldBuilder builder = new OptionGroupFieldBuilder("OptionGroupFieldBuilder");
        initCommonAttributes(builder, false);
        // Specific to definition
        builder.multiselect(true);

        // WHEN

        // THEN
        checkCommonAttributes(builder);
        OptionGroupFieldDefinition definition = (OptionGroupFieldDefinition) builder.definition();
        assertEquals(true, definition.isMultiselect());
        assertNotNull(definition.getPropertyBuilder());
        PropertyBuilder propertyBuilder = definition.getPropertyBuilder();
        assertEquals(BasicProperty.class.getName(), propertyBuilder.getPropertyType().getName());
        assertEquals(OptionGroupPropertyHandler.class.getName(), propertyBuilder.getPropertyHandler().getName());
    }

    @Test
    public void testSwitchableFieldBuilder() {
        // GIVEN
        SwitchableFieldBuilder builder = new SwitchableFieldBuilder("SwitchableFieldBuilder");
        initCommonAttributes(builder, false);
        // Specific to definition
        OptionBuilder optionBuilder1 = new OptionBuilder();
        OptionBuilder optionBuilder2 = new OptionBuilder();
        BasicTextCodeFieldBuilder tfb1 = new BasicTextCodeFieldBuilder("tfb1");
        BasicTextCodeFieldBuilder tfb2 = new BasicTextCodeFieldBuilder("tfb2");
        builder.options(optionBuilder1, optionBuilder2).fields(tfb1, tfb2).selectionType("radio");

        // WHEN

        // THEN
        checkCommonAttributes(builder);
        SwitchableFieldDefinition definition = (SwitchableFieldDefinition) builder.definition();
        assertEquals("radio", definition.getSelectionType());
        assertEquals(tfb1.definition(), definition.getFields().get(0));
        assertEquals(tfb2.definition(), definition.getFields().get(1));
        assertEquals(optionBuilder1.definition(), definition.getOptions().get(0));
        assertEquals(optionBuilder2.definition(), definition.getOptions().get(1));
        assertNotNull(definition.getPropertyBuilder());
        PropertyBuilder propertyBuilder = definition.getPropertyBuilder();
        assertEquals(CompositeProperty.class.getName(), propertyBuilder.getPropertyType().getName());
        assertEquals(SwitchableSimplePropertyCompositeHandler.class.getName(), propertyBuilder.getPropertyHandler().getName());
    }

    @Test
    public void testCompositeFieldBuilder() {
        // GIVEN
        CompositeFieldBuilder builder = new CompositeFieldBuilder("CompositeFieldBuilder");
        initCommonAttributes(builder, false);
        // Specific to definition
        BasicTextCodeFieldBuilder tfb1 = new BasicTextCodeFieldBuilder("tfb1");
        BasicTextCodeFieldBuilder tfb2 = new BasicTextCodeFieldBuilder("tfb2");
        builder.fields(tfb1, tfb2);

        // WHEN

        // THEN
        checkCommonAttributes(builder);
        CompositeFieldDefinition definition = (CompositeFieldDefinition) builder.definition();
        assertEquals(tfb1.definition(), definition.getFields().get(0));
        assertEquals(tfb2.definition(), definition.getFields().get(1));
        assertNotNull(definition.getPropertyBuilder());
        PropertyBuilder propertyBuilder = definition.getPropertyBuilder();
        assertEquals(CompositeProperty.class.getName(), propertyBuilder.getPropertyType().getName());
        assertEquals(SimplePropertyCompositeHandler.class.getName(), propertyBuilder.getPropertyHandler().getName());
    }

    /**
     * Common builder test.
     */
    private void checkCommonAttributes(AbstractFieldBuilder builder) {
        ConfiguredFieldDefinition definition = builder.definition();
        assertEquals(label, definition.getLabel());
        assertEquals(i18nBasename, definition.getI18nBasename());
        assertEquals(i18n, definition.isI18n());
        assertEquals(description, definition.getDescription());
        assertEquals(type, definition.getType());
        assertEquals(required, definition.isRequired());
        assertEquals(requiredErrorMessage, definition.getRequiredErrorMessage());
        assertEquals(readOnly, definition.isReadOnly());
        assertEquals(defaultValue, definition.getDefaultValue());
        assertEquals(styleName, definition.getStyleName());
        assertNotNull(definition.getValidators());
        assertTrue(definition.getValidators().get(0) instanceof ConfiguredFieldValidatorDefinition);
        assertEquals("validatorErrorMessage", ((ConfiguredFieldValidatorDefinition) definition.getValidators().get(0)).getErrorMessage());
        assertNotNull(definition.getPropertyBuilder());
    }

    /**
     * Init common builder attributes.
     */
    @SuppressWarnings("unchecked")
    private void initCommonAttributes(AbstractFieldBuilder builder, boolean overridePropertyType) {
        builder.label(label).i18nBasename(i18nBasename).i18n(i18n).description(description);
        builder.type(type).required(required).requiredErrorMessage(requiredErrorMessage);
        builder.readOnly(readOnly).defaultValue(defaultValue).styleName(styleName);
        ConfiguredFieldValidatorDefinition validatorDefinition = new ConfiguredFieldValidatorDefinition();
        validatorDefinition.setErrorMessage("validatorErrorMessage");
        builder.validator(validatorDefinition);
        if (overridePropertyType) {
            PropertyBuilder propertyBuilder = new PropertyBuilder();
            propertyBuilder.setPropertyType((Class<? extends CustomPropertyType<?>>) (Object) BasicProperty.class);
            propertyBuilder.setPropertyHandler((Class<? extends PropertyHandler<?>>) (Object) BasicPropertyHandler.class);
            builder.propertyBuilder(propertyBuilder);
        }
    }

}
