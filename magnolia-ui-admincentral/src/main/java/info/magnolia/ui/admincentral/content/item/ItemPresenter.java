/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.content.item;

import info.magnolia.ui.admincentral.workbench.ItemWorkbenchView;

import javax.inject.Inject;

/**
 * ItemPresenter.
 */
public class ItemPresenter {

    private ItemView view;

    @Inject
    public ItemPresenter(ItemView view) {
        this.view = view;
    }

    public ItemView start() {
        return view;
    }

    public void initContentView(ItemWorkbenchView parentView) {
        /*if (workbenchDefinition == null) {
            throw new IllegalArgumentException("Trying to init a workbench but got null definition.");
        }
        log.debug("Initializing workbench {}...", workbenchDefinition.getName());

        for (final ContentView.ViewType type : ContentView.ViewType.values()) {
            final ContentView contentView = contentViewBuilder.build(workbenchDefinition, type);
            contentView.setListener(this);
            contentView.select(StringUtils.defaultIfEmpty(workbenchDefinition.getPath(), "/"));
            parentView.addContentView(type, contentView);
        }

        if (StringUtils.isBlank(workbenchDefinition.getWorkspace())) {
            throw new IllegalStateException(workbenchDefinition.getName()
                    + " workbench definition must specify a workspace to connect to. Please, check your configuration.");
        }
        */

        parentView.addItemView(ItemView.ViewType.VIEW, view);


        parentView.setViewType(ItemView.ViewType.VIEW);
    }
}
