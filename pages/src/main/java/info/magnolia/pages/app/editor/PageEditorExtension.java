/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.pages.app.editor;

import info.magnolia.ui.api.context.UiContext;

import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.Extension;

/**
 * Page editor extensions, i.e. non visual Vaadin components which are attached to the page editor subapp view, need to implement this interface
 * in order to be loaded. Extensions need to be configured at <code>/modules/pages/apps/pages/subApps/detail/extensions</code>. The order of loading is the
 * same as it appears in the configuration tree.
 */
public interface PageEditorExtension extends Extension {

    /**
     * Called upon page editor subapp start.
     * 
     * @see com.vaadin.server.AbstractExtension.
     */
    void onStart(PagesEditorSubAppView view, String nodePath, UiContext uiContext);

    /**
     * Called upon page editor subapp stop.
     */
    void onStop();

    /**
     * Called when page editor is in preview mode.
     */
    void onPreview();

    /**
     * Called when page editor is in edit mode.
     */
    void onEdit();

    /**
     * An extension will typically "add itself" to the connector by using its protected extend(..) method.
     */
    void addTo(AbstractClientConnector connector);
}
