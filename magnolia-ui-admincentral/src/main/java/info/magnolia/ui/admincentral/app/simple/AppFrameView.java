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
package info.magnolia.ui.admincentral.app.simple;

import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.AppView;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTab;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTabSheet;

import javax.inject.Inject;

import com.vaadin.server.KeyMapper;
import com.vaadin.ui.Component;

/**
 * View used to give all apps a uniform look-and-feel.
 */
public class AppFrameView implements AppView {

    private final MagnoliaShell shell;

    private Listener listener;

    private KeyMapper<MagnoliaTab> mapper = new KeyMapper<MagnoliaTab>();

    private final MagnoliaTabSheet tabsheet = new MagnoliaTabSheet() {

        @Override
        public void setActiveTab(MagnoliaTab tab) {
            super.setActiveTab(tab);
            String key = mapper.key(tab);
            listener.onFocus(key);
        }

        @Override
        protected void closeTab(MagnoliaTab tab) {
            super.closeTab(tab);
            String key = mapper.key(tab);
            listener.onClose(key);
            mapper.remove(tab);
        }
    };

    @Inject
    public AppFrameView(final Shell shell) {
        super();
        this.shell = (MagnoliaShell) shell;
        tabsheet.setSizeFull();
        tabsheet.addStyleName("app");
    }

    @Override
    public String addSubAppView(View view, String caption, boolean closable) {
        final MagnoliaTab tab = tabsheet.addTab(caption, view.asVaadinComponent());
        tab.setClosable(closable);
        if(tabsheet.getActiveTab() != tab) {
            tabsheet.setActiveTab(tab);
        }
        String key = mapper.key(tab);
        return key;
    }

    @Override
    public void setActiveSubAppView(String instanceId) {
        tabsheet.setActiveTab(mapper.get(instanceId));
    }

    @Override
    public String getActiveSubAppView() {
        return mapper.key(tabsheet.getActiveTab());
    }

    @Override
    public void setFullscreen(boolean fullscreen) {
        tabsheet.setFullscreen(fullscreen);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public MagnoliaTabSheet asVaadinComponent() {
        return tabsheet;
    }

    @Override
    public void setModalOnActiveSubApp(Component modalComponent) {
        Component modalityParent = tabsheet.getActiveTab();
        shell.openModal(modalComponent, modalityParent);

    }

    @Override
    public void setModal(Component modalComponent) {
        Component modalityParent = tabsheet;
        shell.openModal(modalComponent, modalityParent);
    }

    @Override
    public void clearModal(Component modalComponent) {
        shell.closeModal(modalComponent);
    }
}
