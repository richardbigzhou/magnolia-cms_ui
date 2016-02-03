/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.framework.app;

import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTab;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTabSheet;

import org.vaadin.cssinject.CSSInject;

import com.vaadin.server.ClientConnector.AttachEvent;
import com.vaadin.server.ClientConnector.AttachListener;
import com.vaadin.server.KeyMapper;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;

/**
 * View used to give all apps a uniform look-and-feel.
 *
 * @see info.magnolia.ui.api.app.AppView
 */
public class DefaultAppView implements AppView {

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
        public void closeTab(MagnoliaTab tab) {
            super.closeTab(tab);
            String key = mapper.key(tab);
            listener.onClose(key);
            mapper.remove(tab);
        }
    };

    public DefaultAppView() {
        super();
        tabsheet.setSizeFull();
        tabsheet.addStyleName("app");
    }

    @Override
    public String addSubAppView(View view, String caption, boolean closable) {
        final MagnoliaTab tab = tabsheet.addTab(caption, view != null ? view.asVaadinComponent() : null);
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
    public void updateCaption(String instanceId, String caption) {
        mapper.get(instanceId).setCaption(caption);
    }

    @Override
    public void setTheme(String themeName) {
        String stylename = String.format("app-%s", themeName);
        final String themeUrl = String.format("../%s/styles.css", themeName);

        final Component vaadinComponent = asVaadinComponent();
        vaadinComponent.addStyleName(stylename);
        vaadinComponent.addAttachListener(new AttachListener() {

            @Override
            public void attach(AttachEvent event) {
                ThemeResource res = new ThemeResource(themeUrl);
                CSSInject cssInject = new CSSInject(vaadinComponent.getUI());
                cssInject.addStyleSheet(res);
            }
        });
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
    public View getSubAppViewContainer(final String instanceId) {

        return new View(){

            @Override
            public Component asVaadinComponent() {
                return mapper.get(instanceId);
            }

        };
    }

    @Override
    public void closeSubAppView(String instanceId) {
        tabsheet.closeTabFromServer(mapper.get(instanceId));
    }

    @Override
    public void setAppLogo(String logo, String bgcolor) {
        tabsheet.setLogo(logo, bgcolor);
    }

    @Override
    public void setAppName(String name) {
        tabsheet.setName(name);
    }

}
