/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.ui.dialog.callback;

import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.dialog.DialogPresenter;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import org.junit.Test;

public class ContentChangedEditorCallbackTest {

    private DialogPresenter dialogPresenter = mock(DialogPresenter.class);
    private ContentConnector contentConnector = mock(ContentConnector.class);
    private EventBus eventBus = mock(EventBus.class);
    private EditorCallback callback = new ContentChangedEditorCallback(dialogPresenter, eventBus, null, contentConnector);

    @Test
    public void onSuccess() {
        //GIVEN
        when(contentConnector.canHandleItem(anyObject())).thenReturn(true);

        //WHEN
        callback.onSuccess(null);

        //THEN
        verify(dialogPresenter).closeDialog();
        verify(eventBus).fireEvent(any(ContentChangedEvent.class));
    }

    @Test
    public void onCancel() {
        //GIVEN

        //WHEN
        callback.onCancel();

        //THEN
        verify(dialogPresenter).closeDialog();
    }
}
