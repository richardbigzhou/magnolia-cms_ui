/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.pages.app.action;

import info.magnolia.commands.CommandsManager;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.browser.action.RestoreItemPreviousVersionAction;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.List;

import javax.inject.Named;
import javax.jcr.Node;

/**
 * Restores the previous version of a page using a command.
 */
public class RestorePreviousVersionAction extends RestoreItemPreviousVersionAction<RestorePreviousVersionActionDefinition> {

    private final LocationController locationController;

    public RestorePreviousVersionAction(RestorePreviousVersionActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n, LocationController locationController) {
        super(definition, item, commandsManager, eventBus, uiContext, i18n);
        this.locationController = locationController;
    }

    public RestorePreviousVersionAction(RestorePreviousVersionActionDefinition definition, List<JcrItemAdapter> items, CommandsManager commandsManager, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n, LocationController locationController) {
        super(definition, items, commandsManager, eventBus, uiContext, i18n);
        this.locationController = locationController;
    }


    @Override
    protected void onPostExecute() throws Exception {
        super.onPostExecute();

        Node node = (Node) getCurrentItem().getJcrItem();
        boolean restoreMultiple = getItems().size() > 1 || NodeUtil.getNodes(node, NodeTypes.Page.NAME).iterator().hasNext();

        // Show preview only if one page is restored
        if (((RestorePreviousVersionActionDefinition) getDefinition()).isShowPreview() && !restoreMultiple) {
            DetailLocation location = new DetailLocation("pages", "detail", DetailView.ViewType.EDIT, node.getPath(), "");
            locationController.goTo(location);
        }
    }
}
