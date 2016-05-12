/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.about.app.config;

import info.magnolia.about.app.mapping.VirtualURIMappingViewImpl;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.ui.vaadin.layout.SmallAppLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.util.AbstractBeanContainer;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.HtmlRenderer;

/**
 * View implementation.
 */
@StyleSheet("vaadin://about-app.css")
public class ConfigInfoViewImpl extends SmallAppLayout implements ConfigInfoView {

    private static final String CONFIG = "config";
    private static final String VALUE = "value";
    private static final String CONFIG_HTML = "configHtml";
    private static final String VALUE_HTML = "valueHtml";

    private final SimpleTranslator i18n;
    private final ServerConfiguration serverConfiguration;
    private final MagnoliaConfigurationProperties magnoliaConfigurationProperties;

    @Inject
    public ConfigInfoViewImpl(SimpleTranslator i18n, ServerConfiguration serverConfiguration, MagnoliaConfigurationProperties magnoliaConfigurationProperties) {
        this.i18n = i18n;
        this.serverConfiguration = serverConfiguration;
        this.magnoliaConfigurationProperties = magnoliaConfigurationProperties;

        addSection(setUpLayout());
    }

    private VerticalLayout setUpLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setStyleName("about-app");

        Label sectionTitle = new Label(i18n.translate("about.app.config.configinfo.title"));
        sectionTitle.addStyleName("section-title");

        layout.addComponent(sectionTitle);

        Label serverDataTitle = new Label(i18n.translate("about.app.config.configinfo.serverdata.title"));
        serverDataTitle.addStyleName("fieldset-title");
        serverDataTitle.setHeight(20, Unit.PIXELS);

        layout.addComponent(serverDataTitle);

        String isAdminServer = String.valueOf(this.serverConfiguration.isAdmin());
        Label isAdminServerLabel = new Label(i18n.translate("about.app.config.configinfo.serverdata.isadmin") + ": " + isAdminServer);
        isAdminServerLabel.setHeight(40, Unit.PIXELS);

        layout.addComponent(isAdminServerLabel);

        Label systemDataTitle = new Label(i18n.translate("about.app.config.configinfo.systemdata.title"));
        systemDataTitle.addStyleName("fieldset-title");
        systemDataTitle.setHeight(30, Unit.PIXELS);
        layout.addComponent(systemDataTitle);

        Grid systemDataGrid = setUpSystemDataGrid();

        layout.addComponent(systemDataGrid);
        layout.setExpandRatio(systemDataGrid, 1f);

        layout.setSizeFull();

        return layout;
    }

    private Grid setUpSystemDataGrid() {
        List<ConfigInfoBean> configInfoBeans = new ArrayList<>();
        for (String config : magnoliaConfigurationProperties.getKeys()) {
            String value = magnoliaConfigurationProperties.getProperty(config);
            String configHtml = String.format(VirtualURIMappingViewImpl.SPAN_TEMPLATE, config, config);
            String valueHtml = String.format(VirtualURIMappingViewImpl.SPAN_TEMPLATE, value, value);
            configInfoBeans.add(new ConfigInfoBean(config, value, configHtml, valueHtml));
        }

        Collections.sort(configInfoBeans, new Comparator<ConfigInfoBean>() {
            @Override
            public int compare(ConfigInfoBean configInfoBean1, ConfigInfoBean configInfoBean2) {
                return configInfoBean1.getConfig().compareTo(configInfoBean2.getConfig());
            }
        });

        BeanItemContainer<ConfigInfoBean> container = new BeanItemContainer<>(ConfigInfoBean.class, configInfoBeans);

        Grid grid = new Grid(container);

        grid.getDefaultHeaderRow().setStyleName("about-app-header");
        grid.setWidth(100, Unit.PERCENTAGE);
        grid.setHeight(100, Unit.PERCENTAGE);

        grid.removeColumn(CONFIG);
        grid.removeColumn(VALUE);

        grid.getColumn(CONFIG_HTML)
                .setWidth(250)
                .setHeaderCaption(i18n.translate("about.app.config.configinfo.systemdata.config"))
                .setRenderer(new HtmlRenderer());

        grid.getColumn(VALUE_HTML)
                .setWidth(570)
                .setHeaderCaption(i18n.translate("about.app.config.configinfo.systemdata.value"))
                .setRenderer(new HtmlRenderer());

        grid.setSelectionMode(Grid.SelectionMode.NONE);

        setGridFilter(grid);

        grid.setCellStyleGenerator(new Grid.CellStyleGenerator() {
            @Override
            public String getStyle(Grid.CellReference cellReference) {
                if (CONFIG_HTML.equals(cellReference.getPropertyId())) {
                    return "about-app-grid-config-cell";
                }
                if (VALUE_HTML.equals(cellReference.getPropertyId())) {
                    return "about-app-grid-value-cell";
                }
                return null;
            }
        });

        grid.setSizeFull();

        return grid;
    }

    private void setGridFilter(final Grid grid) {
        final AbstractBeanContainer container = (AbstractBeanContainer) grid.getContainerDataSource();

        // Create a header row to hold column filters
        Grid.HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.setStyleName("about-app-grid-header");

        // Set up a filter for all columns
        for (final Object pid : container.getContainerPropertyIds()) {
            if (pid.equals(CONFIG) || pid.equals(VALUE)) {
                continue;
            }
            Grid.HeaderCell cell = filterRow.getCell(pid);

            // Have an input field to use for filter
            TextField filterField = new TextField();
            filterField.setWidth(100, Unit.PERCENTAGE);

            // Update filter when the filter input is changed
            filterField.addTextChangeListener(new FieldEvents.TextChangeListener() {
                @Override
                public void textChange(FieldEvents.TextChangeEvent event) {
                    String propertyId;
                    if (pid.equals(CONFIG_HTML) || pid.equals(VALUE_HTML)) {
                        propertyId = StringUtils.removeEnd((String) pid, "Html");
                    } else {
                        propertyId = (String) pid;
                    }
                    container.removeContainerFilters(propertyId);

                    // (Re)create the filter if necessary
                    if (!event.getText().isEmpty()) {
                        container.addContainerFilter(new SimpleStringFilter(propertyId, event.getText(), true, false));
                    }
                }
            });
            cell.setComponent(filterField);
        }
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    /**
     * Config info bean class.
     */
    public static class ConfigInfoBean {
        private final String config;
        private final String value;
        private final String configHtml;
        private final String valueHtml;

        public ConfigInfoBean(String config, String value, String configHtml, String valueHtml) {
            this.config = config;
            this.value = value;
            this.configHtml = configHtml;
            this.valueHtml = valueHtml;
        }

        public String getConfig() {
            return config;
        }

        public String getValue() {
            return value;
        }

        public String getConfigHtml() {
            return configHtml;
        }

        public String getValueHtml() {
            return valueHtml;
        }
    }
}
