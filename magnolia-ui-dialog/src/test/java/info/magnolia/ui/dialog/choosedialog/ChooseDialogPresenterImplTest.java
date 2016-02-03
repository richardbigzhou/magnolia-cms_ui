/**
 * This file Copyright (c) 2015-2016 Magnolia International
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.Arrays;

import org.junit.Test;

import com.vaadin.data.Item;


public class ChooseDialogPresenterImplTest {

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
}
