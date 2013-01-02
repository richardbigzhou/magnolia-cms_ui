/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.admincentral.content.view.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: use ui settings?
 * Provides the ContentViewBuilder defined in the AdminCentralModule.
 */
public class ContentViewBuilderProviderImpl implements ContentViewBuilderProvider {

    // private UISettings uiSettings;

    // content2bean
    private List<ContentViewBuilder> contentViewBuilders = new ArrayList<ContentViewBuilder>();

    /**
     * Is needed so that we can make a proxy (reloading configuration).
     * TODO: is this really necessary?
     */
    public ContentViewBuilderProviderImpl() {
    }

    /*
     * @Inject
     * public ContentViewBuilderProviderImpl(User user, UISettings uiSettings) {
     * this.user = user;
     * this.uiSettings = uiSettings;
     * }
     */

    @Override
    public ContentViewBuilder getBuilder() {
        // FIXME: use user and uiSettings
        return contentViewBuilders.get(0);
    }

    public void setContentViewBuilders(List<ContentViewBuilder> contentViewBuilders) {
        this.contentViewBuilders = contentViewBuilders;
    }

    public List<ContentViewBuilder> getContentViewBuilders() {
        return contentViewBuilders;
    }

    public void addContentViewBuilder(ContentViewBuilder contentViewBuilder) {
        this.contentViewBuilders.add(contentViewBuilder);
    }
}
