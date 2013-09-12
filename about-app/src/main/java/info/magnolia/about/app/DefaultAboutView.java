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

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.context.MgnlContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.ui.vaadin.layout.SmallAppLayout;

import javax.inject.Inject;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;

/**
 * Default Vaadin implementation of the {@link AboutView} interface, for the community edition.
 */
public class DefaultAboutView implements AboutView {

    private static final Logger log = LoggerFactory.getLogger(DefaultAboutView.class);

    private Listener listener;

    protected final SmallAppLayout root = new SmallAppLayout();

    private final ServerConfiguration serverConfiguration;
    private final MagnoliaConfigurationProperties magnoliaProperties;

    @Inject
    public DefaultAboutView(ServerConfiguration serverConfiguration, MagnoliaConfigurationProperties magnoliaProperties) {
        this.serverConfiguration = serverConfiguration;
        this.magnoliaProperties = magnoliaProperties;

        root.setDescription("The about app shows an overview of the installed Magnolia version and the environment it runs in.");

        root.addSection(createInstallationSection());
    }

    private Component createInstallationSection() {

        LicenseFileExtractor licenseProperties = LicenseFileExtractor.getInstance();
        String magnoliaVersion = licenseProperties.get(LicenseFileExtractor.VERSION_NUMBER);
        String authorInstance = serverConfiguration.isAdmin() ? "Author instance" : "Public instance";
        String osInfo = String.format("%s %s (%s)",
                magnoliaProperties.getProperty("os.name"),
                magnoliaProperties.getProperty("os.version"),
                magnoliaProperties.getProperty("os.arch"));
        String javaInfo = String.format("%s (build %s)",
                magnoliaProperties.getProperty("java.version"),
                magnoliaProperties.getProperty("java.runtime.version"));
        String serverInfo = MgnlContext.getWebContext().getServletContext().getServerInfo();

        String jcrInfo;
        try {
            Repository repo = JcrUtils.getRepository();
            jcrInfo = String.format("%s %s",
                    repo.getDescriptor("jcr.repository.name"),
                    repo.getDescriptor("jcr.repository.version"));
        } catch (RepositoryException e) {
            log.debug("JCR repository information is not available", e);
            jcrInfo = "-";
        }

        FormLayout layout = new FormLayout();

        Label sectionTitle = new Label("Installation information");
        sectionTitle.addStyleName("section-title");
        layout.addComponent(sectionTitle);

        layout.addComponent(createFieldsetTitle("Magnolia"));
        layout.addComponent(createField(LicenseFileExtractor.EDITION, "Edition", licenseProperties.get(LicenseFileExtractor.EDITION)));
        layout.addComponent(createField("magnoliaVersion", "Version (bundle)", magnoliaVersion));
        layout.addComponent(createField("authorInstance", "Instance", authorInstance));

        Component environmentTitle = createFieldsetTitle("Environment");
        environmentTitle.addStyleName("spacer");
        layout.addComponent(environmentTitle);
        layout.addComponent(createField("osInfo", "Operating system", osInfo));
        layout.addComponent(createField("javaInfo", "Java version", javaInfo));
        layout.addComponent(createField("serverInfo", "Application server", serverInfo));
        layout.addComponent(createField("jcrInfo", "Repository", jcrInfo));

        return layout;
    }

    protected Component createFieldsetTitle(String title) {
        Label fieldsetTitle = new Label(title);
        fieldsetTitle.addStyleName("fieldset-title");
        return fieldsetTitle;
    }

    protected Component createField(String key, String caption, String value) {
        Label field = new Label();
        field.setCaption(caption);
        field.setPropertyDataSource(new ObjectProperty<String>(value, String.class, true));
        return field;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        return root;
    }

}