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
package info.magnolia.ui.form.field.upload.basic;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * Main test class for {@link BasicUploadField}.
 */
public class BasicUploadFieldTest {

    private LocaleProvider localeProvider;
    private TranslationService translationService;
    private SimpleTranslator simpleTranslator;

    @Before
    public void setUp() throws Exception {
        localeProvider = mock(LocaleProvider.class);
        when(localeProvider.getLocale()).thenReturn(Locale.ENGLISH);
        translationService = mock(TranslationService.class);
        simpleTranslator = new SimpleTranslator(translationService, localeProvider);

        setupTranslationServiceWith("field.upload.media.image", "image");
        setupTranslationServiceWith("field.upload.media.application", "application");

        setupTranslationServiceWith("field.upload.file.detail.header", "File detail");
        setupTranslationServiceWith("field.upload.note.success", "Your file has been uploaded successfully<br>{0}");

        setupTranslationServiceWith("field.upload.file.detail.header.media", "{0} detail");
        setupTranslationServiceWith("field.upload.note.success.media", "Your {0} has been uploaded successfully<br>{1}");
    }

    @Test
    public void testGetCaptionForEmptyCaption() {
        // GIVEN
        BasicUploadField<BasicFileItemWrapper> uploadField = new BasicUploadField<BasicFileItemWrapper>(null, null, null, null, new BasicUploadFieldDefinition(), simpleTranslator);

        // WHEN
        String caption1 = "";
        String caption2 = null;

        // THEN
        assertEquals("", uploadField.getCaption(caption1, null));
        assertEquals("", uploadField.getCaption(caption2, null));
    }

    @Test
    public void testGetCaptionForNormalField() {
        // GIVEN
        BasicUploadField<BasicFileItemWrapper> uploadField = new BasicUploadField<BasicFileItemWrapper>(null, null, null, null, new BasicUploadFieldDefinition(), simpleTranslator);

        // WHEN
        uploadField.captionExtension = "";

        // THEN
        assertEquals("File detail", uploadField.getCaption("field.upload.file.detail.header", null));
        assertEquals("Your file has been uploaded successfully<br>/root/file", uploadField.getCaption("field.upload.note.success", new String[] { "/root/file" }));
    }

    @Test
    public void testGetCaptionForImageField() {
        // GIVEN
        BasicUploadField<BasicFileItemWrapper> uploadField = new BasicUploadField<BasicFileItemWrapper>(null, null, null, null, new BasicUploadFieldDefinition(), simpleTranslator);

        // WHEN
        uploadField.captionExtension = "image";

        // THEN
        assertEquals("image detail", uploadField.getCaption("field.upload.file.detail.header", null));
        assertEquals("Your image has been uploaded successfully<br>/root/image", uploadField.getCaption("field.upload.note.success", new String[] { "/root/image" }));
    }

    @Test
    public void testGetCaptionForApplicationField() {
        // GIVEN
        BasicUploadField<BasicFileItemWrapper> uploadField = new BasicUploadField<BasicFileItemWrapper>(null, null, null, null, new BasicUploadFieldDefinition(), simpleTranslator);

        // WHEN
        uploadField.captionExtension = "application";

        // THEN
        assertEquals("application detail", uploadField.getCaption("field.upload.file.detail.header", null));
        assertEquals("Your application has been uploaded successfully<br>/root/application", uploadField.getCaption("field.upload.note.success", new String[] { "/root/application" }));
    }

    private void setupTranslationServiceWith(String expectedKey, String desiredResult) {
        when(translationService.translate(same(localeProvider), eq(new String[] { expectedKey }))).thenReturn(desiredResult);
    }
}
