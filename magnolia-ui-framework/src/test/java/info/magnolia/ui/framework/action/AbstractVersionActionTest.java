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
package info.magnolia.ui.framework.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.cms.core.version.VersionInfo;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;

import java.text.MessageFormat;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Basic test for the {@link AbstractVersionAction}.
 */
public class AbstractVersionActionTest {

    private SimpleTranslator simpleTranslator = mock(SimpleTranslator.class);
    private TestVersionAction action = new TestVersionAction(mock(ActionDefinition.class), mock(LocationController.class), mock(UiContext.class), mock(FormDialogPresenter.class), simpleTranslator);

    @Test
    public void testVersionLabelWithComment() {
        // GIVEN
        Date date = new Date();
        VersionInfo versionInfo = new VersionInfo("1.0", date, "superuser", "test");
        when(simpleTranslator.translate("test")).thenReturn("translated test");

        // WHEN
        String versionLabel = action.getVersionLabel(versionInfo);

        // THEN
        Assert.assertEquals(MessageFormat.format("1.0 ({0}) (superuser: translated test)", versionInfo.getVersionDate()), versionLabel);
    }

    @Test
    public void testVersionLabelWithOutComment() {
        // GIVEN
        Date date = new Date();
        VersionInfo versionInfo = new VersionInfo("1.0", date, "superuser");

        // WHEN
        String versionLabel = action.getVersionLabel(versionInfo);

        // THEN
        Assert.assertEquals(MessageFormat.format("1.0 ({0}) (superuser)", versionInfo.getVersionDate()), versionLabel);
    }

    /**
     * Test implementation of {@link AbstractVersionAction}.
     */
    private class TestVersionAction extends AbstractVersionAction {

        protected TestVersionAction(ActionDefinition definition, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, SimpleTranslator i18n) {
            super(definition, locationController, uiContext, formDialogPresenter, i18n);
        }

        @Override
        protected Class getBeanItemClass() {
            return null;
        }

        @Override
        protected FormDialogDefinition buildNewComponentDialog() throws ActionExecutionException, RepositoryException {
            return null;
        }

        @Override
        protected Node getNode() throws RepositoryException {
            return null;
        }

        @Override
        protected Location getLocation() throws ActionExecutionException {
            return null;
        }

    }

}
