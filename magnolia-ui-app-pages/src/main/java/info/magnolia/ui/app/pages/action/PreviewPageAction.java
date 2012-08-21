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
import info.magnolia.ui.app.pages.PagesApp;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;


/**
 * The Class PreviewPageAction. Opens a full screen preview of the selected page.
 */
public class PreviewPageAction extends ActionBase<PreviewPageActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(PreviewPageAction.class);

    private final Node nodeToPreview;

    private LocationController locationController;

    private boolean full;

    /**
     * Instantiates a new preview page action.
     *
     * @param definition the definition
     * @param nodeToPreview the node to preview
     */
    @Inject
    public PreviewPageAction(PreviewPageActionDefinition definition, LocationController locationController, Node nodeToPreview) {
        super(definition);
        this.locationController = locationController;
        this.nodeToPreview = nodeToPreview;
        this.full =  definition.isFull();
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            final String path = nodeToPreview.getPath();
            final String token = PagesApp.EDITOR_TOKEN + ":" + (full ? PagesApp.PREVIEW_FULL_TOKEN: PagesApp.PREVIEW_TOKEN) + ";" + path;
            log.debug("token is {}", token);
            locationController.goTo(new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, "pages", token));
        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }
}
