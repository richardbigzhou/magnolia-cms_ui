/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.init.MagnoliaConfigurationProperties;

import javax.inject.Inject;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * The AboutPresenter.
 */
public class AboutPresenter {

    private static final Logger log = LoggerFactory.getLogger(AboutPresenter.class);

    private final AboutView view;

    private final ServerConfiguration serverConfiguration;
    private final MagnoliaConfigurationProperties magnoliaProperties;

    private final SimpleTranslator i18n;

    @Inject
    public AboutPresenter(AboutView view, ServerConfiguration serverConfiguration, MagnoliaConfigurationProperties magnoliaProperties, SimpleTranslator i18n) {
        this.view = view;
        this.serverConfiguration = serverConfiguration;
        this.magnoliaProperties = magnoliaProperties;
        this.i18n = i18n;
    }

    public AboutView start() {

        // magnolia information
        LicenseFileExtractor licenseProperties = LicenseFileExtractor.getInstance();
        String mgnlEdition = licenseProperties.get(LicenseFileExtractor.EDITION);
        String mgnlVersion = licenseProperties.get(LicenseFileExtractor.VERSION_NUMBER);
        String authorInstance = serverConfiguration.isAdmin() ?
                i18n.translate("about.app.main.instance.author") :
                i18n.translate("about.app.main.instance.public");

        // system information
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

        // feed the view
        PropertysetItem item = new PropertysetItem();
        item.addItemProperty(AboutView.MAGNOLIA_EDITION_KEY, new ObjectProperty<String>(mgnlEdition));
        item.addItemProperty(AboutView.MAGNOLIA_VERSION_KEY, new ObjectProperty<String>(mgnlVersion));
        item.addItemProperty(AboutView.MAGNOLIA_INSTANCE_KEY, new ObjectProperty<String>(authorInstance));
        item.addItemProperty(AboutView.OS_INFO_KEY, new ObjectProperty<String>(osInfo));
        item.addItemProperty(AboutView.JAVA_INFO_KEY, new ObjectProperty<String>(javaInfo));
        item.addItemProperty(AboutView.SERVER_INFO_KEY, new ObjectProperty<String>(serverInfo));
        item.addItemProperty(AboutView.JCR_INFO_KEY, new ObjectProperty<String>(jcrInfo));
        view.setDataSource(item);

        return view;
    }

}
