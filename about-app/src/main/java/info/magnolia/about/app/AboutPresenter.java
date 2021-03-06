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

import static info.magnolia.about.app.AboutView.*;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.pddescriptor.ProductDescriptorExtractor;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.objectfactory.Components;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * The AboutPresenter.
 */
public class AboutPresenter {

    private static final Logger log = LoggerFactory.getLogger(AboutPresenter.class);

    static final String COMMUNITY_EDITION_I18N_KEY = "about.app.main.communityEdition";
    static final String INSTANCE_AUTHOR_I18N_KEY = "about.app.main.instance.author";
    static final String INSTANCE_PUBLIC_I18N_KEY = "about.app.main.instance.public";
    static final String UNKNOWN_PROPERTY_I18N_KEY = "about.app.main.unknown";

    protected final SimpleTranslator i18n;
    private final String unknownProperty;

    private final AboutView view;
    private final ProductDescriptorExtractor productDescriptorExtractor;
    private final ServerConfiguration serverConfiguration;
    private final MagnoliaConfigurationProperties magnoliaProperties;

    /**
     * @deprecated since 5.5, get view data through {@link #getInstallationInfo()}
     */
    @Deprecated
    protected Item viewData = new PropertysetItem();

    @Inject
    public AboutPresenter(AboutView view, ServerConfiguration serverConfiguration, MagnoliaConfigurationProperties magnoliaProperties, SimpleTranslator i18n, ProductDescriptorExtractor productDescriptorExtractor) {
        this.view = view;
        this.productDescriptorExtractor = productDescriptorExtractor;
        this.serverConfiguration = serverConfiguration;
        this.magnoliaProperties = magnoliaProperties;
        this.i18n = i18n;
        this.unknownProperty = i18n.translate(UNKNOWN_PROPERTY_I18N_KEY);
    }

    /**
     * @deprecated Since 5.4.3, use {@link #AboutPresenter(AboutView, ServerConfiguration, MagnoliaConfigurationProperties, SimpleTranslator, ProductDescriptorExtractor)}
     */
    @Deprecated
    public AboutPresenter(AboutView view, ServerConfiguration serverConfiguration, MagnoliaConfigurationProperties magnoliaProperties, SimpleTranslator i18n) {
        this(view, serverConfiguration, magnoliaProperties, i18n, Components.getComponent(ProductDescriptorExtractor.class));
    }

    public AboutView start() {
        Item viewData = getInstallationInfo();
        // preserve compatibility
        if (!this.viewData.getItemPropertyIds().isEmpty()) {
            this.viewData.getItemPropertyIds()
                    .forEach(propertyId -> viewData.addItemProperty(propertyId, AboutPresenter.this.viewData.getItemProperty(propertyId)));
        }
        view.setDataSource(viewData);
        return view;
    }

    protected Item getInstallationInfo() {
        PropertysetItem viewData = new PropertysetItem();
        // Magnolia information
        String mgnlEdition = getEditionName();
        String mgnlVersion = productDescriptorExtractor.get(ProductDescriptorExtractor.VERSION_NUMBER);
        String authorInstance = serverConfiguration.isAdmin() ?
                i18n.translate(INSTANCE_AUTHOR_I18N_KEY) :
                i18n.translate(INSTANCE_PUBLIC_I18N_KEY);

        // System information
        String osInfo = String.format("%s %s (%s)",
                magnoliaProperties.getProperty("os.name"),
                magnoliaProperties.getProperty("os.version"),
                magnoliaProperties.getProperty("os.arch"));
        String javaInfo = String.format("%s (build %s)",
                magnoliaProperties.getProperty("java.version"),
                magnoliaProperties.getProperty("java.runtime.version"));
        String serverInfo = MgnlContext.getWebContext().getServletContext().getServerInfo();

        String dbInfo = null;
        String dbDriverInfo = null;
        Connection connection = null;
        try {
            String connectionString[] = getConnectionString();

            String repoHome = magnoliaProperties.getProperty("magnolia.repositories.home");
            String repoName = getRepoName();
            connectionString[0] = StringUtils.replace(connectionString[0], "${wsp.home}", repoHome + "/" + repoName + "/workspaces/default");
            if (connectionString[0].startsWith("jdbc:")) {
                // JDBC url
                connection = DriverManager.getConnection(connectionString[0], connectionString[1], connectionString[2]);
            } else if (connectionString[0].startsWith("java:")) {
                // JNDI url
                Context initialContext = new InitialContext();
                DataSource datasource = (DataSource) initialContext.lookup(connectionString[0]);
                if (datasource != null) {
                    connection = datasource.getConnection();
                } else {
                    log.debug("Failed to lookup datasource.");
                }
            }
            if (connection != null) {
                DatabaseMetaData meta = connection.getMetaData();
                dbInfo = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
                if (dbInfo.toLowerCase().contains("mysql")) {
                    String engine = getMySQLEngineInfo(connection, connectionString);
                    if (engine != null) {
                        dbInfo += engine;
                    }
                }
                dbDriverInfo = meta.getDriverName() + " " + meta.getDriverVersion();
            } else {
                dbInfo = i18n.translate(UNKNOWN_PROPERTY_I18N_KEY);
            }
        } catch (NamingException e) {
            log.debug("Failed obtain DB connection through JNDI with {}", e.getMessage(), e);
        } catch (SQLException e) {
            log.debug("Failed to read DB and driver info from connection with {}", e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.debug("Failed to understand DB connection URL with {}", e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // ignore, nothing we can do
                }
            }
        }

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

        // Prepare information for the view
        addItemProperty(viewData, MAGNOLIA_EDITION_KEY, mgnlEdition);
        addItemProperty(viewData, MAGNOLIA_VERSION_KEY, mgnlVersion);
        addItemProperty(viewData, MAGNOLIA_INSTANCE_KEY, authorInstance);
        addItemProperty(viewData, OS_INFO_KEY, osInfo);
        addItemProperty(viewData, JAVA_INFO_KEY, javaInfo);
        addItemProperty(viewData, SERVER_INFO_KEY, serverInfo);
        addItemProperty(viewData, JCR_INFO_KEY, jcrInfo);
        addItemProperty(viewData, DB_INFO_KEY, dbInfo);
        addItemProperty(viewData, DB_DRIVER_INFO_KEY, dbDriverInfo);

        return viewData;
    }

    /**
     * Adds a property to the given Item data-source; blank values will be translated as 'unknown'.
     */
    protected void addItemProperty(Item viewData, String key, String value) {
        viewData.addItemProperty(key, new ObjectProperty<>(StringUtils.defaultIfBlank(value, unknownProperty)));
    }

    /**
     * @deprecated since 5.5, use {@link #addItemProperty(Item, String, String)} instead
     */
    @Deprecated
    protected void addViewProperty(String key, String value) {
        viewData.addItemProperty(key, new ObjectProperty<>(StringUtils.defaultIfBlank(value, unknownProperty)));
    }

    protected AboutView getView() {
        return view;
    }

    /**
     * Returns the name of the edition.
     */
    protected String getEditionName() {
        // Hard code this in CE edition - value will be correctly populated for EE in EnterpriseAboutPresenter
        return i18n.translate(COMMUNITY_EDITION_I18N_KEY);
    }

    private String getMySQLEngineInfo(Connection connection, String[] connectionString) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SHOW TABLE STATUS FROM `" + StringUtils.substringAfterLast(connectionString[0], "/") + "`;");
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String engine = resultSet.getString("Engine");
                return " (" + engine + ")";
            }
        } catch (SQLException e) {
            // can't get extra info, oh well
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return null;
    }

    String[] getConnectionString() {
        File config = null;
        // Assuming, the path to the repository-config.-file is configured relative, starting with WEB-INF.
        // Otherwise, assuming it's an absolute path for this config. (See JIRA MGNLUI-3163)
        String configuredPath = magnoliaProperties.getProperty("magnolia.repositories.jackrabbit.config");
        if (configuredPath != null) {
            if (configuredPath.startsWith("WEB-INF")) {
                config = new File(magnoliaProperties.getProperty("magnolia.app.rootdir") + "/" + configuredPath);
            } else {
                config = new File(configuredPath);
            }
        }
        // No special handling here if the config (file) is null or not existing.
        // If the path is wrong or not set, Magnolia won't start up properly and it won't be possible to launch the About-app.

        final String[] connectionString = new String[] { "", "", "" };
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(config, new DefaultHandler() {
                private boolean inPM;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    super.startElement(uri, localName, qName, attributes);
                    if ("PersistenceManager".equals(qName) || "DataSource".equals(qName)) {
                        inPM = true;
                    }
                    if (inPM && "param".equals(qName)) {
                        if ("url".equals(attributes.getValue("name"))) {
                            connectionString[0] = attributes.getValue("value");
                        }
                        if ("user".equals(attributes.getValue("name"))) {
                            connectionString[1] = attributes.getValue("value");
                        }
                        if ("password".equals(attributes.getValue("name"))) {
                            connectionString[2] = attributes.getValue("value");
                        }
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    super.endElement(uri, localName, qName);
                    if ("PersistenceManager".equals(localName) || "DataSource".equals(qName)) {
                        inPM = false;
                    }
                }
            });
            return connectionString;
        } catch (Exception e) {
            log.debug("Failed to obtain DB connection info with {}", e.getMessage(), e);
        }
        return connectionString;
    }

    String getRepoName() {
        String repoConfigPath = magnoliaProperties.getProperty("magnolia.repositories.config");
        File config = new File(magnoliaProperties.getProperty("magnolia.app.rootdir") + "/" + repoConfigPath);
        final String[] repoName = new String[1];
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(config, new DefaultHandler() {
                private boolean inRepo;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    super.startElement(uri, localName, qName, attributes);
                    if ("RepositoryMapping".equals(qName)) {
                        inRepo = true;
                    }
                    if (inRepo && "Map".equals(qName)) {
                        if ("config".equals(attributes.getValue("name"))) {
                            repoName[0] = attributes.getValue("repositoryName");
                        }
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    super.endElement(uri, localName, qName);
                    if ("RepositoryMapping".equals(localName)) {
                        inRepo = false;
                    }
                }
            });
            return repoName[0];
        } catch (Exception e) {
            log.debug("Failed to obtain repository configuration info with {}", e.getMessage(), e);
        }
        return null;
    }
}
