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
package info.magnolia.ui.vaadin.editor.pagebar;

import info.magnolia.ui.model.i18n.I18NAuthoringSupport;

import java.util.Locale;

import javax.inject.Inject;

import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

/**
 * Implements {@link PageBarView}.
 */
public class PageBarViewImpl extends CustomComponent implements PageBarView {

    private CssLayout root = new CssLayout();

    private Label pageNameLabel = new Label();

    private Label settingsStatus = new Label();

    private AbstractSelect languageSelector;

    private AbstractSelect platformSelector = new ComboBox();

    private PageBarView.Listener listener;

    private I18NAuthoringSupport i18NAuthoringSupport;

    @Inject
    public PageBarViewImpl(I18NAuthoringSupport i18NAuthoringSupport) {
        super();
        this.i18NAuthoringSupport = i18NAuthoringSupport;
        setCompositionRoot(root);
        construct();
    }

    private void construct() {
        root.addStyleName("pagebar");
        platformSelector.addItem("Desktop");
        platformSelector.addItem("Mobile");
        platformSelector.addItem("Tablet");
        platformSelector.setNullSelectionAllowed(false);

        platformSelector.setSizeUndefined();
        platformSelector.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (listener != null) {
                    listener.platformSelected(String.valueOf(event.getProperty().getValue()));
                }
                updateStatusLabel();
            }
        });


        this.languageSelector = i18NAuthoringSupport.getLanguageChooser();
        if (languageSelector != null) {
            languageSelector.setSizeUndefined();
            languageSelector.addValueChangeListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    if (listener != null) {
                        listener.languageSelected((Locale) event.getProperty().getValue());
                    }
                    updateStatusLabel();
                }
            });
        }

        this.platformSelector.setValue("Desktop");
        this.platformSelector.setEnabled(false);
        this.pageNameLabel.setSizeUndefined();
        this.pageNameLabel.addStyleName("title");

        this.settingsStatus.addStyleName("status");
        settingsStatus.setSizeUndefined();

        root.addComponent(pageNameLabel);
        if (languageSelector != null) {
            root.addComponent(languageSelector);
        }
        root.addComponent(platformSelector);
        root.addComponent(settingsStatus);
    }

    private void updateStatusLabel() {
        settingsStatus.setValue(platformSelector.getValue() + " - " + String.valueOf(languageSelector.getValue()));
    }

    @Override
    public void setPageName(String pageName) {
        this.pageNameLabel.setValue(pageName.toUpperCase());
    }

    @Override
    public void setListener(PageBarView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setCurrentLanguage(Locale locale) {
        if (languageSelector != null) {
            languageSelector.setValue(locale);
        }
    }

    @Override
    public void togglePreviewMode(boolean isPreview) {
        if (languageSelector != null) {
            languageSelector.setVisible(!isPreview);
        }
        platformSelector.setVisible(!isPreview);

        settingsStatus.setVisible(isPreview);

        if (isPreview) {
            root.addStyleName("preview");
        } else {
            root.removeStyleName("preview");
        }
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

}
