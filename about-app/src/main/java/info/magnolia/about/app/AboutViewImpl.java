/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.about.app;

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.vaadin.layout.SmallAppLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;

/**
 * Default Vaadin implementation of the {@link AboutView} interface, for the community edition.
 */
public class AboutViewImpl implements AboutView {

    protected final SmallAppLayout root = new SmallAppLayout();

    private Item dataSource;

    private Map<String, Property.Viewer> dataBindings = new HashMap<String, Property.Viewer>();

    public AboutViewImpl() {
        root.setDescription(MessagesUtil.get("about.app.main.description", MESSAGES_BASENAME));
        root.addSection(createInstallationSection());
    }

    private Component createInstallationSection() {

        // build and bind fields
        Component mgnlEdition = buildAndBind(AboutView.MAGNOLIA_EDITION_KEY, MessagesUtil.get("about.app.main.mgnledition", MESSAGES_BASENAME));
        Component mgnlVersion = buildAndBind(AboutView.MAGNOLIA_VERSION_KEY, MessagesUtil.get("about.app.main.mgnlversion", MESSAGES_BASENAME));
        Component mgnlInstance = buildAndBind(AboutView.MAGNOLIA_INSTANCE_KEY, MessagesUtil.get("about.app.main.instance", MESSAGES_BASENAME));

        Component osInfo = buildAndBind(AboutView.OS_INFO_KEY, MessagesUtil.get("about.app.main.os", MESSAGES_BASENAME));
        Component javaInfo = buildAndBind(AboutView.JAVA_INFO_KEY, MessagesUtil.get("about.app.main.java", MESSAGES_BASENAME));
        Component serverInfo = buildAndBind(AboutView.SERVER_INFO_KEY, MessagesUtil.get("about.app.main.server", MESSAGES_BASENAME));
        Component jcrInfo = buildAndBind(AboutView.JCR_INFO_KEY, MessagesUtil.get("about.app.main.repository", MESSAGES_BASENAME));

        // layout
        FormLayout layout = new FormLayout();

        Label sectionTitle = new Label(MessagesUtil.get("about.app.main.installation.title", MESSAGES_BASENAME));
        sectionTitle.addStyleName("section-title");
        layout.addComponent(sectionTitle);

        layout.addComponent(createFieldsetTitle(MessagesUtil.get("about.app.main.magnolia.title", MESSAGES_BASENAME)));
        layout.addComponent(mgnlEdition);
        layout.addComponent(mgnlVersion);
        layout.addComponent(mgnlInstance);

        Component environmentTitle = createFieldsetTitle(MessagesUtil.get("about.app.main.environment.title", MESSAGES_BASENAME));
        environmentTitle.addStyleName("spacer");
        layout.addComponent(environmentTitle);
        layout.addComponent(osInfo);
        layout.addComponent(javaInfo);
        layout.addComponent(serverInfo);
        layout.addComponent(jcrInfo);

        return layout;
    }

    protected Component createFieldsetTitle(String title) {
        Label fieldsetTitle = new Label(title);
        fieldsetTitle.addStyleName("fieldset-title");
        return fieldsetTitle;
    }

    protected Component buildAndBind(String key, String caption) {
        Label field = new Label();
        field.setCaption(caption);
        dataBindings.put(key, field);
        return field;
    }

    @Override
    public void setDataSource(Item item) {
        this.dataSource = item;
        refresh();
    }

    private void refresh() {
        for (Entry<String, Property.Viewer> entry : dataBindings.entrySet()) {
            Property.Viewer field = entry.getValue();
            Property<?> property = dataSource.getItemProperty(entry.getKey());
            field.setPropertyDataSource(property);
        }
    }

    @Override
    public Component asVaadinComponent() {
        return root;
    }

}