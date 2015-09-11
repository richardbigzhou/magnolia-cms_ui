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
package info.magnolia.ui.form.field.factory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.upload.UploadReceiver;
import info.magnolia.ui.form.field.upload.basic.BasicUploadField;
import info.magnolia.ui.imageprovider.ImageProvider;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.BasicUploadFieldFactory}.
 */
public class BasicUploadFieldFactoryTest extends AbstractFieldFactoryTestCase<BasicUploadFieldDefinition> {

    protected BasicUploadFieldFactory basicUploadBuilder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // no need to initialize a specic temp dir for tests, it'll be target/tmp by default

        UiContext uiContext = mock(UiContext.class);
        ImageProvider imageProvider = mock(ImageProvider.class);
        SimpleTranslator translator = mock(SimpleTranslator.class);
        ComponentsTestUtil.setInstance(SimpleTranslator.class, translator);
        ComponentProvider componentProvider = new MockComponentProvider();
        EventBus eventBus = mock(EventBus.class);
        basicUploadBuilder = new BasicUploadFieldFactory(definition, baseItem, imageProvider, uiContext, translator, componentProvider, eventBus);
    }

    @Test
    public void testGetField() throws Exception {
        // GIVEN

        // WHEN
        Field field = basicUploadBuilder.createField();

        // THEN
        assertEquals(true, field instanceof BasicUploadField);
    }

    @Test
    public void testEmptyLayout() throws Exception {
        // GIVEN
        BasicUploadField field = (BasicUploadField) basicUploadBuilder.createField();
        Upload upload = new Upload();
        FailedEvent event = new FailedEvent(upload, "filename", "MIMEType", 0l);

        // WHEN
        field.uploadFinished(event);

        // THEN
        CssLayout layout = field.getCssLayout();
        assertEquals(2, layout.getComponentCount());
        assertTrue(layout.getComponent(0) instanceof Upload);
        assertTrue(layout.getComponent(1) instanceof Label);
        assertTrue(((Label) layout.getComponent(1)).getStyleName().contains("upload-text"));
    }

    @Test
    public void testCompletedLayout() throws Exception {
        // GIVEN
        BasicUploadField field = (BasicUploadField) basicUploadBuilder.createField();
        Upload upload = new Upload();
        UploadReceiver receiver = mock(UploadReceiver.class);
        when(receiver.getFileName()).thenReturn("filename.jpg");
        upload.setReceiver(receiver);
        FinishedEvent event = new FinishedEvent(upload, "filename.jpg", "MIMEType", 0l);

        // WHEN
        field.uploadFinished(event);

        // THEN
        CssLayout layout = field.getCssLayout();
        assertEquals(3, layout.getComponentCount());
        assertTrue(layout.getComponent(0) instanceof FormLayout);
        assertTrue(((FormLayout) layout.getComponent(0)).getStyleName().contains("file-details"));
        assertTrue(layout.getComponent(1) instanceof HorizontalLayout);
        HorizontalLayout horizontalLayout = (HorizontalLayout) layout.getComponent(1);
        assertEquals(2, horizontalLayout.getComponentCount());
        assertTrue(horizontalLayout.getComponent(0) instanceof Upload);
        assertTrue(layout.getComponent(2) instanceof Label);
        assertTrue(((Label) layout.getComponent(2)).getStyleName().contains("preview-image"));
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        BasicUploadFieldDefinition fieldDefinition = new BasicUploadFieldDefinition();
        fieldDefinition.setName(propertyName);
        this.definition = fieldDefinition;
    }

}
