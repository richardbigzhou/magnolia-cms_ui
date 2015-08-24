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
package info.magnolia.ui.form.field.upload;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.upload.basic.BasicUploadField;
import info.magnolia.ui.imageprovider.ImageProvider;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.TargetDetails;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.StreamVariable.StreamingStartEvent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Html5File;

/**
 * Tests for {@link AbstractUploadField}.
 */
public class AbstractUploadFieldTest {

    private BasicUploadFieldDefinition definition;
    private BasicUploadField basicUploadField;
    private DragAndDropEvent event;
    private Html5File file;

    @Before
    public void setUp() {
        definition = new BasicUploadFieldDefinition();
        basicUploadField = new BasicUploadField(mock(ImageProvider.class), mock(UiContext.class), definition, mock(SimpleTranslator.class));
        basicUploadField.setPropertyDataSource(mock(Property.class));
        DragAndDropWrapper.WrapperTransferable transferable = mock(DragAndDropWrapper.WrapperTransferable.class);
        Html5File[] files = new Html5File[1];
        file = mock(Html5File.class);
        doCallRealMethod().when(file).setStreamVariable(any(StreamVariable.class));
        doCallRealMethod().when(file).getStreamVariable();
        files[0] = file;
        when(transferable.getFiles()).thenReturn(files);
        event = new DragAndDropEvent(transferable, mock(TargetDetails.class));
    }

    @Test
    public void testDropWithEmptyMimeType() {
        //GIVEN
        definition.setAllowedMimeTypePattern("zip");
        definition.setAllowedFileExtensionPattern(".*.zip");
        definition.setFallbackMimeType("zip");
        StreamingStartEvent startEvent = mock(StreamingStartEvent.class);
        when(startEvent.getFileName()).thenReturn("test.zip");
        when(startEvent.getMimeType()).thenReturn("");

        //WHEN
        basicUploadField.drop(event);
        file.getStreamVariable().streamingStarted(startEvent);

        //THEN
        assertFalse("Mimetype was set to fallback value and upload started.", file.getStreamVariable().isInterrupted());
    }
}