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
package info.magnolia.ui.vaadin.editor.pagebar;

import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.vaadin.editor.gwt.shared.PlatformType;

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

    private AbstractSelect languageSelector;

    private ComboBox platformSelector = new ComboBox();

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
        for (PlatformType type : PlatformType.values()) {
            platformSelector.addItem(type);
        }
        platformSelector.setNullSelectionAllowed(false);
        platformSelector.setTextInputAllowed(false);
        platformSelector.setImmediate(true);
        platformSelector.setSizeUndefined();
        platformSelector.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (listener != null) {
                    listener.platformSelected((PlatformType)event.getProperty().getValue());
                }
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
                }
            });
        }

        this.platformSelector.setValue(PlatformType.DESKTOP);
        this.pageNameLabel.setSizeUndefined();
        this.pageNameLabel.addStyleName("title");

        root.addComponent(pageNameLabel);
        if (languageSelector != null) {
            root.addComponent(languageSelector);
        }
        root.addComponent(platformSelector);
    }

    @Override
    public void setPageName(String pageTitle, String path) {
        String label = pageTitle.toUpperCase() + "  -  " + path;
        this.pageNameLabel.setValue(label);
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
        platformSelector.setVisible(isPreview);
        if (isPreview) {
            root.addStyleName("preview");
        } else {
            root.removeStyleName("preview");
        }
    }

    @Override
    public void setPlatFormType(PlatformType targetPreviewPlatform) {
        platformSelector.setValue(targetPreviewPlatform);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

}
