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
package info.magnolia.ui.form.config;

import static org.junit.Assert.*;

import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.Layout;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.PasswordFieldDefinition;
import info.magnolia.ui.form.field.definition.RichTextFieldDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.definition.TwinColSelectFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.form.field.transformer.basic.ListToSetTransformer;
import info.magnolia.ui.form.field.transformer.composite.CompositeTransformer;
import info.magnolia.ui.form.field.transformer.composite.SwitchableTransformer;
import info.magnolia.ui.form.field.transformer.multi.MultiValueTransformer;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;

import org.junit.Test;

/**
 * Collection of tests for builders in info.magnolia.ui.form.config.
 * As the builders are pretty simple and as the tests need some common setUp,
 * we didn't go for one unit-test per builder.
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
    public void testCodeFieldBuilder() {
        // GIVEN
        CodeFieldBuilder builder = new CodeFieldBuilder("CodeFieldBuilder");
        initCommonAttributes(builder, true);

        // WHEN
        builder.language("java");

        // THEN
        checkCommonAttributes(builder);
        assertEquals("java", builder.definition().getLanguage());
    }

    @Test
    public void testTextFieldBuilder() {
        // GIVEN
        TextFieldBuilder builder = new TextFieldBuilder("TextFieldBuilder");
        initCommonAttributes(builder, true);

        // WHEN
        builder.rows(10).maxLength(100);

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

        // WHEN
        builder.buttonLabel("buttonLabel");

        // THEN
        checkCommonAttributes(builder);
        assertEquals("buttonLabel", builder.definition().getButtonLabel());
    }

    @Test
    public void testStaticFieldBuilder() {
        // GIVEN
        StaticFieldBuilder builder = new StaticFieldBuilder("StaticFieldBuilder");
        initCommonAttributes(builder, true);

        // WHEN
        builder.value("value");

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
        builder.images(true);

        // THEN
        checkCommonAttributes(builder);
        RichTextFieldDefinition definition = builder.definition();
        assertTrue(definition.isImages());
    }

    @Test
    public void testPasswordFieldBuilder() {
        // GIVEN
        PasswordFieldBuilder builder = new PasswordFieldBuilder("PasswordFieldBuilder");
        initCommonAttributes(builder, true);

        // WHEN
        builder.verification(true).verificationMessage("verificationMessage").verificationErrorMessage("verificationErrorMessage");

        // THEN
        checkCommonAttributes(builder);
        PasswordFieldDefinition definition = builder.definition();
        assertEquals(true, definition.isVerification());
        assertEquals("verificationMessage", definition.getVerificationMessage());
        assertEquals("verificationErrorMessage", definition.getVerificationErrorMessage());
    }

    @Test
    public void testBasicUploadFieldBuilder() {
        // GIVEN
        BasicUploadFieldBuilder builder = new BasicUploadFieldBuilder("BasicUploadFieldBuilder");
        initCommonAttributes(builder, true);

        builder.binaryNodeName("binaryNodeName").maxUploadSize(100l).allowedMimeTypePattern("*.gif").editFileName(true).editFileFormat(false).selectAnotherCaption("selectAnotherCaption");
        builder.userInterruption("userInterruption").fileDetailFormatCaption("fileDetailFormatCaption").fileDetailHeaderCaption("fileDetailHeaderCaption").fileDetailNameCaption("fileDetailNameCaption").fileDetailSizeCaption("fileDetailSizeCaption");
        builder.allowedFallbackMimeType("fallbackMime").allowedFileExtensionPattern("*.jpg");

        // WHEN
        builder.fileDetailSourceCaption("fileDetailSourceCaption").selectNewCaption("selectNewCaption").successNoteCaption("successNoteCaption");

        // THEN
        checkCommonAttributes(builder);
        BasicUploadFieldDefinition definition = builder.definition();

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
        assertEquals("fallbackMime", definition.getFallbackMimeType());
        assertEquals("*.jpg", definition.getAllowedFileExtensionPattern());

    }

    @Test
    public void testLinkFieldBuilder() {
        // GIVEN
        LinkFieldBuilder builder = new LinkFieldBuilder("LinkFieldBuilder");
        initCommonAttributes(builder, true);

        builder.targetPropertyToPopulate("targetPropertyToPopulate").targetWorkspace("targetWorkspace").targetTreeRootPath("targetTreeRootPath").appName("appName");

        // WHEN
        builder.buttonSelectNewLabel("buttonSelectNewLabel").buttonSelectOtherLabel("buttonSelectOtherLabel");

        // THEN
        checkCommonAttributes(builder);
        LinkFieldDefinition definition = builder.definition();
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
        MultiValueFieldBuilder builder = new MultiValueFieldBuilder("MultiFieldBuilder");
        initCommonAttributes(builder, false);

        // WHEN
        builder.buttonSelectRemoveLabel("buttonSelectRemoveLabel").buttonSelectAddLabel("buttonSelectAddLabel");

        // THEN
        checkCommonAttributes(builder);
        MultiValueFieldDefinition definition = builder.definition();
        assertEquals("buttonSelectRemoveLabel", definition.getButtonSelectRemoveLabel());
        assertEquals("buttonSelectAddLabel", definition.getButtonSelectAddLabel());
        assertNotNull(definition.getTransformerClass());
        assertEquals(MultiValueTransformer.class.getName(), definition.getTransformerClass().getName());
    }

    @Test
    public void testTwinColSelectFieldBuilder() {
        // GIVEN
        TwinColSelectFieldBuilder builder = new TwinColSelectFieldBuilder("TwinColSelectFieldBuilder");
        initCommonAttributes(builder, false);

        // WHEN
        builder.leftColumnCaption("leftColumnCaption").rightColumnCaption("rightColumnCaption");

        // THEN
        checkCommonAttributes(builder);
        TwinColSelectFieldDefinition definition = builder.definition();
        assertEquals("leftColumnCaption", definition.getLeftColumnCaption());
        assertEquals("rightColumnCaption", definition.getRightColumnCaption());
        assertNotNull(definition.getTransformerClass());
        assertEquals(ListToSetTransformer.class.getName(), definition.getTransformerClass().getName());
    }

    @Test
    public void testOptionGroupFieldBuilder() {
        // GIVEN
        OptionGroupFieldBuilder builder = new OptionGroupFieldBuilder("OptionGroupFieldBuilder");
        initCommonAttributes(builder, false);

        // WHEN
        builder.multiselect(true);

        // THEN
        checkCommonAttributes(builder);
        OptionGroupFieldDefinition definition = builder.definition();
        assertEquals(true, definition.isMultiselect());
        assertNotNull(definition.getTransformerClass());
        assertEquals(ListToSetTransformer.class.getName(), definition.getTransformerClass().getName());
    }

    @Test
    public void testSwitchableFieldBuilder() {
        // GIVEN
        SwitchableFieldBuilder builder = new SwitchableFieldBuilder("SwitchableFieldBuilder");
        initCommonAttributes(builder, false);

        OptionBuilder optionBuilder1 = new OptionBuilder();
        OptionBuilder optionBuilder2 = new OptionBuilder();
        CodeFieldBuilder tfb1 = new CodeFieldBuilder("tfb1");
        CodeFieldBuilder tfb2 = new CodeFieldBuilder("tfb2");

        // WHEN
        builder.options(optionBuilder1, optionBuilder2).fields(tfb1, tfb2).selectionType("radio");

        // THEN
        checkCommonAttributes(builder);
        SwitchableFieldDefinition definition = builder.definition();
        assertEquals("radio", definition.getSelectionType());
        assertEquals(tfb1.definition(), definition.getFields().get(0));
        assertEquals(tfb2.definition(), definition.getFields().get(1));
        assertEquals(optionBuilder1.definition(), definition.getOptions().get(0));
        assertEquals(optionBuilder2.definition(), definition.getOptions().get(1));
        assertNotNull(definition.getTransformerClass());
        assertEquals(SwitchableTransformer.class.getName(), definition.getTransformerClass().getName());
    }

    @Test
    public void testCompositeFieldBuilder() {
        // GIVEN
        CompositeFieldBuilder builder = new CompositeFieldBuilder("CompositeFieldBuilder");
        initCommonAttributes(builder, false);

        CodeFieldBuilder tfb1 = new CodeFieldBuilder("tfb1");
        CodeFieldBuilder tfb2 = new CodeFieldBuilder("tfb2");
        builder.fields(tfb1, tfb2).layout(Layout.vertical);

        // WHEN
        builder.fields(tfb1, tfb2);

        // THEN
        checkCommonAttributes(builder);
        CompositeFieldDefinition definition = builder.definition();
        assertEquals(tfb1.definition(), definition.getFields().get(0));
        assertEquals(tfb2.definition(), definition.getFields().get(1));
        assertNotNull(definition.getTransformerClass());
        assertEquals(CompositeTransformer.class.getName(), definition.getTransformerClass().getName());
        assertEquals(Layout.vertical, definition.getLayout());
    }

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
        assertNotNull(definition.getTransformerClass());
    }

    private void initCommonAttributes(AbstractFieldBuilder builder, boolean overrideTransformerClass) {
        builder.label(label).i18nBasename(i18nBasename).i18n(i18n).description(description);
        builder.type(type).required(required).requiredErrorMessage(requiredErrorMessage);
        builder.readOnly(readOnly).defaultValue(defaultValue).styleName(styleName);
        ConfiguredFieldValidatorDefinition validatorDefinition = new ConfiguredFieldValidatorDefinition();
        validatorDefinition.setErrorMessage("validatorErrorMessage");
        builder.validator(validatorDefinition);
        if (overrideTransformerClass) {
            builder.transformerClass((Class<? extends Transformer<?>>) (Object) BasicTransformer.class);
        }
    }
}
