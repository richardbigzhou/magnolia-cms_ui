/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.workbench.contenttool.search;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.vaadin.extension.ShortcutProtector;
import info.magnolia.ui.vaadin.icon.Icon;

import java.util.Arrays;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TextField;

/**
 * Implementation of {@link SearchContentToolView}.
 */
public class SearchContentToolViewImpl extends CssLayout implements SearchContentToolView {

    private final SimpleTranslator i18n;

    private TextField searchField;

    private SearchContentToolView.Listener listener;

    private final Property.ValueChangeListener searchFieldListener = new Property.ValueChangeListener() {

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            listener.onSearch(searchField.getValue());

            boolean hasSearchContent = !searchField.getValue().isEmpty();
            if (hasSearchContent) {
                addStyleName("has-content");
            } else {
                removeStyleName("has-content");
            }
            searchField.focus();
        }
    };

    @Inject
    public SearchContentToolViewImpl(SimpleTranslator i18n) {
        this.i18n = i18n;
        Button clearSearchBoxButton = new Button();
        clearSearchBoxButton.setStyleName("m-closebutton");
        clearSearchBoxButton.addStyleName("icon-delete-search");
        clearSearchBoxButton.addStyleName("searchbox-clearbutton");
        clearSearchBoxButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                searchField.setValue("");
            }
        });

        Icon searchIcon = new Icon("search");
        searchIcon.addStyleName("searchbox-icon");

        Icon searchArrow = new Icon("arrow2_s");
        searchArrow.addStyleName("searchbox-arrow");

        searchField = buildSearchField();

        setVisible(true);
        addComponent(searchField);
        addComponent(clearSearchBoxButton);
        addComponent(searchIcon);
        addComponent(searchArrow);
        setStyleName("searchbox");
    }

    private TextField buildSearchField() {
        final TextField field = new TextField();
        ShortcutProtector.extend(field, Arrays.asList(ShortcutAction.KeyCode.ENTER));
        final String inputPrompt = i18n.translate("toolbar.search.prompt");

        field.setInputPrompt(inputPrompt);
        field.setSizeUndefined();
        field.addStyleName("searchfield");

        // TextField has to be immediate to fire value changes when pressing Enter, avoiding ShortcutListener overkill.
        field.setImmediate(true);
        field.addValueChangeListener(searchFieldListener);

        field.addFocusListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(FieldEvents.FocusEvent event) {
                // put the cursor at the end of the field
                TextField tf = (TextField) event.getSource();
                tf.setCursorPosition(tf.getValue().length());
            }
        });

        // No blur handler.
        return field;
    }

    @Override
    public void setListener(SearchContentToolView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setSearchQuery(String searchQuery) {
        if (searchField == null) {
            return;
        }
        // turn off value change listener, so that presenter does not think there was user input and searches again
        searchField.removeValueChangeListener(searchFieldListener);
        if (StringUtils.isNotBlank(searchQuery)) {
            searchField.setValue(searchQuery);
            searchField.focus();
        } else {
            searchField.setValue("");
            removeStyleName("has-content");
        }
        searchField.addValueChangeListener(searchFieldListener);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }
}
