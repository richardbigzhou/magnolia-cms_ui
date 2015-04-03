/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.vaadin.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * The SmallAppLayout, offering space for a header component and multiple sections stacked vertically.
 */
public class SmallAppLayout extends VerticalLayout {

    private Property<String> description = new ObjectProperty<String>("");

    private VerticalLayout root = this;

    private CssLayout sectionsLayout = new CssLayout();

    private Label descriptionLabel = new Label();

    public SmallAppLayout() {
        root.addStyleName("smallapp");
        root.setSizeFull();

        descriptionLabel.addStyleName("description");
        descriptionLabel.setContentMode(ContentMode.HTML);
        descriptionLabel.setConverter(new StringToHtmlConverter());
        descriptionLabel.setPropertyDataSource(description);
        descriptionLabel.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (StringUtils.isNotBlank((String) event.getProperty().getValue())) {
                    descriptionLabel.setVisible(true);
                } else {
                    descriptionLabel.setVisible(false);
                }
            }
        });

        root.addComponent(descriptionLabel);
        root.setExpandRatio(descriptionLabel, 0);

        sectionsLayout.addStyleName("smallapp-sections");
        sectionsLayout.setSizeFull();
        root.addComponent(sectionsLayout);
        root.setExpandRatio(sectionsLayout, 1);
    }

    @Override
    public String getDescription() {
        return description.getValue();
    }

    @Override
    public void setDescription(String description) {
        this.description.setValue(description);
    }

    public List<Component> getSections() {
        Iterator<Component> it = sectionsLayout.iterator();
        List<Component> sections = new ArrayList<Component>();
        while (it.hasNext()) {
            sections.add(it.next());
        }
        return sections;
    }

    public void addSection(Component section) {
        section.addStyleName("smallapp-section");
        sectionsLayout.addComponent(section);
    }

    public void removeSection(Component section) {
        sectionsLayout.removeComponent(section);
    }

    /**
     * The StringToHtmlConverter, currently only turns carriage-returns into HTML br elements.
     */
    private class StringToHtmlConverter implements Converter<String, String> {

        @Override
        public String convertToModel(String value, Class<? extends String> targetType, Locale locale) throws com.vaadin.data.util.converter.Converter.ConversionException {
            return value.replaceAll("<br/>", "\n");
        }

        @Override
        public String convertToPresentation(String value, Class<? extends String> targetType, Locale locale) throws com.vaadin.data.util.converter.Converter.ConversionException {
            if (value != null) {
                return value.replaceAll("\n", "<br/>");
            }
            return null;
        }

        @Override
        public Class<String> getModelType() {
            return String.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }

    }

}
