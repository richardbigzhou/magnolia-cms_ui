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

import com.google.inject.Inject;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.api.action.ActionBase;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * Opens a preview of the selected page.
 */
public class PreviewPageAction extends ActionBase<PreviewPageActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(PreviewPageAction.class);

    private final JcrItemNodeAdapter nodeItemToPreview;

    private LocationController locationController;

    /**
     * Instantiates a new preview page action.
     *
     * @param definition the definition
     * @param nodeItemToPreview the node to preview
     */
    @Inject
    public PreviewPageAction(PreviewPageActionDefinition definition, JcrItemNodeAdapter nodeItemToPreview, LocationController locationController) {
        super(definition);
        this.locationController = locationController;
        this.nodeItemToPreview = nodeItemToPreview;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {

            if (!NodeUtil.isNodeType(nodeItemToPreview.getNode(), NodeTypes.Content.NAME)) {
                return;
            }

            DetailLocation location = new DetailLocation("pages", "detail", DetailView.ViewType.VIEW, nodeItemToPreview.getNode().getPath());

            log.debug("token is {}", location.getParameter());

            locationController.goTo(location);

        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }
}
