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
package info.magnolia.about.app.mapping;

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
import info.magnolia.cms.beans.config.DefaultVirtualURIMapping;
import info.magnolia.cms.beans.config.VirtualURIManager;
import info.magnolia.cms.beans.config.VirtualURIMapping;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.vaadin.layout.SmallAppLayout;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * View implementation.
 */
@StyleSheet("vaadin://about-app.css")
public class VirtualURIMappingViewImpl extends SmallAppLayout implements VirtualURIMappingView {

    public static final String SPAN_TEMPLATE = "<span title=\"%s\">%s</span>";

    private static final String MAPPING_CLASS = "mappingClass";
    private static final String FROM_URI = "fromURI";
    private static final String TO_URI = "toURI";
    private static final String MAPPING_CLASS_HTML = "mappingClassHtml";
    private static final String FROM_URI_HTML = "fromURIHtml";
    private static final String TO_URI_HTML = "toURIHtml";

    private final SimpleTranslator i18n;
    private final VirtualURIManager virtualURIManager;

    @Inject
    public VirtualURIMappingViewImpl(SimpleTranslator i18n, VirtualURIManager virtualURIManager) {
        this.i18n = i18n;
        this.virtualURIManager = virtualURIManager;
        addSection(setUpLayout());
    }

    private VerticalLayout setUpLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setStyleName("about-app");

        Label sectionTitle = new Label(i18n.translate("about.app.mapping.virtual.uri.mappings.title"));
        sectionTitle.addStyleName("section-title");

        layout.addComponent(sectionTitle);

        Grid dataGrid = setUpDataGrid();

        layout.addComponent(dataGrid);
        layout.setExpandRatio(dataGrid, 1f);

        layout.setSizeFull();
        return layout;
    }

    private Grid setUpDataGrid() {
        List<VirtualURIMappingBean> virtualURIMappingBeans = new ArrayList<>();
        for (VirtualURIMapping virtualURIMapping : virtualURIManager.getURIMappings()) {
            if (virtualURIMapping instanceof DefaultVirtualURIMapping) {
                DefaultVirtualURIMapping defaultVirtualURIMapping = (DefaultVirtualURIMapping) virtualURIMapping;

                String mappingClass = defaultVirtualURIMapping.getClass().getSimpleName();
                String fromURI = defaultVirtualURIMapping.getFromURI();
                String toURI = defaultVirtualURIMapping.getToURI();
                String mappingClassHtml = String.format(SPAN_TEMPLATE, mappingClass, mappingClass);
                String fromURIHtml = String.format(SPAN_TEMPLATE, fromURI, fromURI);
                String toURIHtml = String.format(SPAN_TEMPLATE, toURI, toURI);

                virtualURIMappingBeans.add(new VirtualURIMappingBean(mappingClass, fromURI, toURI, mappingClassHtml, fromURIHtml, toURIHtml));
            } else {
                String virtualURIMapToStr = virtualURIMapping.toString();
                String virtualURIMapToStrHtml = String.format(SPAN_TEMPLATE, virtualURIMapToStr, virtualURIMapToStr);
                virtualURIMappingBeans.add(new VirtualURIMappingBean(virtualURIMapToStr, "", "", virtualURIMapToStrHtml, "", ""));
            }
        }

        Collections.sort(virtualURIMappingBeans, new Comparator<VirtualURIMappingBean>() {
            @Override
            public int compare(VirtualURIMappingBean virtualURIMappingBean1, VirtualURIMappingBean virtualURIMappingBean2) {
                return virtualURIMappingBean1.getMappingClass().compareTo(virtualURIMappingBean2.getMappingClass());
            }
        });

        BeanItemContainer<VirtualURIMappingBean> container = new BeanItemContainer<>(VirtualURIMappingBean.class, virtualURIMappingBeans);
        Grid grid = new Grid(container);

        grid.getDefaultHeaderRow().setStyleName("about-app-header");
        grid.setWidth(100, Unit.PERCENTAGE);
        grid.setHeight(100, Unit.PERCENTAGE);

        grid.removeColumn(MAPPING_CLASS);
        grid.removeColumn(FROM_URI);
        grid.removeColumn(TO_URI);

        grid.setColumnOrder(MAPPING_CLASS_HTML, FROM_URI_HTML, TO_URI_HTML);

        grid.getColumn(MAPPING_CLASS_HTML)
                .setWidth(200)
                .setHeaderCaption(i18n.translate("about.app.mapping.virtual.uri.mappings.class"))
                .setRenderer(new HtmlRenderer());

        grid.getColumn(FROM_URI_HTML)
                .setWidth(250)
                .setHeaderCaption(i18n.translate("about.app.mapping.virtual.uri.mappings.fromURI"))
                .setRenderer(new HtmlRenderer());

        grid.getColumn(TO_URI_HTML)
                .setWidth(375)
                .setHeaderCaption(i18n.translate("about.app.mapping.virtual.uri.mappings.toURI"))
                .setRenderer(new HtmlRenderer());

        grid.setSelectionMode(Grid.SelectionMode.NONE);

        setGridFilter(grid);

        grid.setCellStyleGenerator(new Grid.CellStyleGenerator() {
            @Override
            public String getStyle(Grid.CellReference cellReference) {
                if (MAPPING_CLASS_HTML.equals(cellReference.getPropertyId())) {
                    return "about-app-grid-class-cell";
                }
                if (FROM_URI_HTML.equals(cellReference.getPropertyId()) || TO_URI_HTML.equals(cellReference.getPropertyId())) {
                    return "about-app-grid-uri-cell";
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
            if (pid.equals(MAPPING_CLASS) || pid.equals(FROM_URI) || pid.equals(TO_URI)) {
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
                    if (pid.equals(MAPPING_CLASS_HTML) || pid.equals(FROM_URI_HTML) || pid.equals(TO_URI_HTML)) {
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
     * Virtual URI mapping bean class.
     */
    public static class VirtualURIMappingBean {
        private final String mappingClass;
        private final String fromURI;
        private final String toURI;
        private final String mappingClassHtml;
        private final String fromURIHtml;
        private final String toURIHtml;

        public VirtualURIMappingBean(String mappingClass, String fromURI, String toURI, String mappingClassHtml, String fromURIHtml, String toURIHtml) {
            this.mappingClass = mappingClass;
            this.fromURI = fromURI;
            this.toURI = toURI;
            this.mappingClassHtml = mappingClassHtml;
            this.fromURIHtml = fromURIHtml;
            this.toURIHtml = toURIHtml;
        }

        public String getMappingClass() {
            return mappingClass;
        }

        public String getFromURI() {
            return fromURI;
        }

        public String getToURI() {
            return toURI;
        }

        public String getMappingClassHtml() {
            return mappingClassHtml;
        }

        public String getFromURIHtml() {
            return fromURIHtml;
        }

        public String getToURIHtml() {
            return toURIHtml;
        }
    }
}
