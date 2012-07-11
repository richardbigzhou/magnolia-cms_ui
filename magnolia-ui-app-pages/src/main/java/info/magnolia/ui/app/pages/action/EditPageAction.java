/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.pages.action;

import info.magnolia.ui.admincentral.workbench.ContentWorkbench;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;

import javax.inject.Inject;

/**
 * Opens a page for editing.
 */
public class EditPageAction extends ActionBase<EditPageActionDefinition> {


    private final ContentWorkbench workbench;
    private final LocationController locationController;
    private final static String TOKEN = "pageeditor";

    @Inject
    public EditPageAction(final EditPageActionDefinition definition, ContentWorkbench workbench, LocationController locationController) {
        super(definition);
        this.workbench = workbench;
        this.locationController = locationController;

    }

    @Override
    public void execute() throws ActionExecutionException {
        String nodePath = workbench.getSelectedItemId();
        Location pageEditorLocation = new DefaultLocation("app", "pages", TOKEN + ":" + nodePath);
        locationController.goTo(pageEditorLocation);
    }
}
