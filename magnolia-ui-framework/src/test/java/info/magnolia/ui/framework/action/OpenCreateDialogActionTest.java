/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.framework.action;

import static org.mockito.Mockito.*;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link info.magnolia.ui.framework.action.OpenCreateDialogAction}.
 */
public class OpenCreateDialogActionTest {

    private FormDialogPresenterFactory formDialogPresenterFactory;
    private UiContext uiContext;
    private SimpleTranslator i18n;
	private OpenCreateDialogActionDefinition actionDefinition;

    @Before
    public void setUp() throws Exception {
        formDialogPresenterFactory = mock(FormDialogPresenterFactory.class);
        uiContext = mock(UiContext.class);
        i18n = mock(SimpleTranslator.class);
        actionDefinition =  new OpenCreateDialogActionDefinition();
    }

    @Test
    public void missingDialogDefinition() throws Exception {
        // GIVEN
        actionDefinition.setName("testAction");

        when(i18n.translate("ui-framework.actions.no.dialog.definition", "testAction")).thenReturn("No dialog defined for action: testAction");

        OpenCreateDialogAction action = new OpenCreateDialogAction(actionDefinition, null, formDialogPresenterFactory, uiContext, mock(EventBus.class), mock(JcrContentConnector.class), i18n);

        // WHEN
        action.execute();

        // THEN
        verify(uiContext).openNotification(MessageStyleTypeEnum.ERROR, false, "No dialog defined for action: testAction");
    }

    @Test
    public void dialogIsNotRegistered() throws Exception {
        // GIVEN
        actionDefinition.setName("testAction");
        actionDefinition.setDialogName("testDialog");

        when(i18n.translate("ui-framework.actions.dialog.not.registered", "testDialog")).thenReturn("Dialog [testDialog] is not registered.");
        when(formDialogPresenterFactory.createFormDialogPresenter("testDialog")).thenReturn(null);

        OpenCreateDialogAction action = new OpenCreateDialogAction(actionDefinition, null, formDialogPresenterFactory, uiContext, mock(EventBus.class), mock(JcrContentConnector.class), i18n);

        // WHEN
        action.execute();

        // THEN
        verify(uiContext).openNotification(MessageStyleTypeEnum.ERROR, false, "Dialog [testDialog] is not registered.");
    }

    @Test
    public void setNullParentIdToDefaultItem() throws Exception {
        // GIVEN
        ContentConnector contentConnector = mock(ContentConnector.class);

        OpenCreateDialogAction action = new OpenCreateDialogAction(actionDefinition, null, formDialogPresenterFactory, uiContext, mock(EventBus.class), contentConnector, i18n);

        // WHEN
        action.execute();

        // THEN
        verify(contentConnector).getDefaultItemId();
    }
}
