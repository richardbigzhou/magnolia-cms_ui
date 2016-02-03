/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.vaadin.switcher;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ContainerOrderedWrapper;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * This class is a {@link CustomField} which wraps a {@link ComboBox} and additionally provides 2 arrows to "navigate" -
 * actually to select the previous or the next item from the available set.
 * Additionally, the Switcher can display a description of the currently selected item.
 *
 * Its Layout looks like<br/>
 *
 * <b>&lt;&lt; | combobox v | &gt;&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  description </b>
 */
public class Switcher extends CustomField<Object> implements Container.Viewer {

    /**
     * The constant for the color-variation "green" (which is default).
     */
    public static final String VARIATION_GREEN = "green";

    /**
     * The constant for the color-variation "black".
     */
    public static final String VARIATION_BLACK = "black";

    private static final String cssGreenVariationClassname = "greenSwitcher";

    private static final String cssBlackVariationClassname = "blackSwitcher";

    private static final String cssSwitcherArrowClassname = "switcherArrow";

    private static final String cssSwitcherBorderClassname = "switcherBorder";

    private CssLayout comboLayout = new CssLayout();

    private HorizontalLayout mainLayout = new HorizontalLayout();

    private String descriptionPropertyName;

    private String colorVariation;

    private Button back, forward;

    private ComboBox combobox;

    private Label descriptionLabel;

    /**
     * Constructs a Switcher by a given collection. The collection is converted into a container during instantiation.
     * @param options
     */
    public Switcher(Collection<?> options) {
        this(createContainer(options), VARIATION_GREEN);
    }

    /**
     * Constructs a Switcher by a given {@link Collection} and a given color-variant.
     * The collection is converted into a container during instantiation.
     *
     * @param options
     * @param colorVariation
     */
    public Switcher(Collection<?> options, String colorVariation) {
        this(createContainer(options), colorVariation);
    }

    /**
     * Constructs a Switcher by a given {@link Container} using the default color-variation (VARIATION_GREEN).
     * @param container
     */
    public Switcher(Container container) {
        this(container, VARIATION_GREEN);
    }


    /**
     * Constructs a Switcher by a given {@link Container} and colorVariant ("green" or "black", "green" is default).
     * @param container
     * @param colorVariation
     */
    public Switcher(Container container, String colorVariation) {
        this.colorVariation = colorVariation;

        // instantiate the combobox with the container
        if (!(container instanceof Container.Ordered)) {
            container = new ContainerOrderedWrapper(container);
        }

        combobox = new ComboBox("", container);
        combobox.setImmediate(true);
        combobox.setTextInputAllowed(false);

    }

    /**
     * Sets the name of the property which is used as the value of the description of an item.<br/>
     * If it's not set, a description cannot be showed.<br/>
     * When set, the passed {@link Container} should have set the property name descriptionPropertyName.
     *
     * @param descriptionPropertyName
     */
    public void setDescriptionPropertyName(String descriptionPropertyName) {
        this.descriptionPropertyName = descriptionPropertyName;
    }

    /**
     * Adds the css style which ensures that the switcher comes with border on top and on bottom, if set true.
     * When set to false, the style gets removed.
     * @param withBorder
     */
    public void withBorder(boolean withBorder){
        if(withBorder){
            mainLayout.addStyleName(cssSwitcherBorderClassname);
        }else{
            mainLayout.removeStyleName(cssSwitcherBorderClassname);
        }
    }


    @Override
    protected Component initContent() {
        construct();
        addHandlers();
        mainLayout.setSizeFull();

        return mainLayout;
    }

    /**
     * Allow or disallow empty selection by the user; it applies #setNullSelectionAllowed on underlying {@link ComboBox}.
     * @param nullSelectionAllowed
     */
    public void setNullSelectionAllowed(boolean nullSelectionAllowed) {
        combobox.setNullSelectionAllowed(nullSelectionAllowed);
    }


    /**
     * Sets the item id that represents null value of this select;
     * it applies #setNullSelectionItemId on underlying {@link ComboBox}.
     * @param item
     */
    public void setNullSelectionItemId(Object item) {
        combobox.setNullSelectionItemId(item);
    }

    /**
     * Sets the item caption property of the underlying {@link ComboBox}.
     *
     * @param propertyId the id of the property.
     */
    public void setItemCaptionPropertyId(String propertyId) {
        combobox.setItemCaptionPropertyId(propertyId);
    }

    /**
     * Sets the visible value of the property.
     * <p>The value of the select is the selected item id.</p>
     * Sets the value to the underlying {@link ComboBox}.
     */
    @Override
    public void setValue(Object newValue) {
        if (newValue == null) {
            return;
        }
        combobox.setValue(newValue);
    }

    /**
     * Sets the next value - if the current value is not the last available in the set.
     */
    public void goForward() {
        Object currentItemId = combobox.getValue();
        if (getContainerDataSource().isLastId(currentItemId)) {
            return;
        }
        setValue(getContainerDataSource().nextItemId(currentItemId));
    }


    /**
     * Sets the previous value - if the current value is not the first available in the set.
     */
    public void goBack() {
        Object currentItemId = combobox.getValue();
        if (getContainerDataSource().isFirstId(currentItemId)) {
            return;
        }
        setValue(getContainerDataSource().prevItemId(currentItemId));
    }

    /**
     * This method adds a {@link Property.ValueChangeListener} to the underlying ComboBox.<br/>
     * Such a ValueChangeListener would be notified not only when a user is changing the value directly with the wrapped {@link ComboBox},
     * but also fired when the arrows (back, forward) are used.
     */
    @Override
    public void addValueChangeListener(Property.ValueChangeListener valueChangeListener) {
        combobox.addValueChangeListener(valueChangeListener);
    }


    /**
     * Gets the current data source of the field, if any (actually from its underlying {@link ComboBox}).
     * @return
     */
    @Override
    public Property getPropertyDataSource() {
        return combobox.getPropertyDataSource();
    }


    /**
     * Sets the specified Property as the data source for the field on the underlying {@link ComboBox}.<br/>
     * For more details see the javadoc of {@link ComboBox#setPropertyDataSource}.
     * @param newDataSource
     */
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        combobox.setPropertyDataSource(newDataSource);
    }

    /**
     * Sets the {@link Converter} used to convert the field value to property data
     * source type. The converter must have a presentation type that matches the
     * field type.
     *
     * @param converter
     */
    @Override
    public void setConverter(Converter<Object, ?> converter) {
        combobox.setConverter(converter);
    }


    /**
     * Returns the selected item id.
     * @return Object
     */
    @Override
    protected Object getInternalValue() {
        return combobox.getValue();
    }

    @Override
    public Class<?> getType() {
        return Object.class;
    }

    /**
     * Sets the {@link Container} that serves as the data source of the viewer (on the underlying {@link ComboBox}).
     * @param newDataSource
     */
    @Override
    public void setContainerDataSource(Container newDataSource) {
        combobox.setContainerDataSource(newDataSource);
    }


    /**
     * Returns the {@link Container}.
     * @return
     */
    @Override
    public Container.Ordered getContainerDataSource() {
        return (Container.Ordered) combobox.getContainerDataSource();
    }

    // Creates the options container and adds the given options to it
    private static Container createContainer(Collection<?> options) {
        final Container container = new IndexedContainer();
        if (options != null) {
            for (final Iterator<?> i = options.iterator(); i.hasNext(); ) {
                container.addItem(i.next());
            }
        }
        return container;
    }

    // This method is instantiating the sub-components and assembling the widget.
    private void construct() {
        mainLayout.setPrimaryStyleName("switcher");
        mainLayout.addStyleName(VARIATION_GREEN.equals(colorVariation) ? cssGreenVariationClassname : cssBlackVariationClassname);

        // back-arrow
        back = new Button("");
        back.setPrimaryStyleName(cssSwitcherArrowClassname);
        back.addStyleName("icon-arrow2_w");
        mainLayout.addComponent(back);

        // combobox with descriptionLabel below wrapped in verticalLayout
        //
        comboLayout.setWidth(100, Unit.PERCENTAGE);
        combobox.setWidth(100, Unit.PERCENTAGE);
        comboLayout.addComponent(combobox);
        // descriptionLabel
        descriptionLabel = new Label("");
        descriptionLabel.setPrimaryStyleName("switcherItemDescription");
        descriptionLabel.addStyleName("descriptionLabel");
        comboLayout.addComponent(descriptionLabel);

        mainLayout.addComponent(comboLayout);
        mainLayout.setExpandRatio(comboLayout, 1.0f);

        // forward arrow
        forward = new Button("");
        forward.setPrimaryStyleName(cssSwitcherArrowClassname);
        forward.addStyleName("icon-arrow2_e");
        mainLayout.addComponent(forward);

        updateButtonState(getValue());
        updateItemDescription();
    }

    // This method just adds or removes styles indicating the the button doesn't work and cannot be used at this state of the switcher
    private void setButtonEnabled(Button button, boolean isEnabled) {
        if (isEnabled) {
            button.removeStyleName("disabled");
            button.removeStyleName("v-button-disabled");
            button.removeStyleName("switcher-back-disabled");
        } else {
            button.addStyleName("disabled");
        }
    }

    private void addHandlers() {
        back.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                goBack();
            }
        });

        forward.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                goForward();
            }
        });

        combobox.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateButtonState(event.getProperty().getValue());
                updateItemDescription();
                fireValueChange(true);
            }
        });
    }

    private void updateButtonState(Object newValue) {
        if (newValue == null) {
            setButtonEnabled(back, false);
            setButtonEnabled(forward, false);
            return;
        }

        Object first = getContainerDataSource().firstItemId();
        Object last = getContainerDataSource().lastItemId();
        if (!newValue.equals(first) && !newValue.equals(last)) {
            setButtonEnabled(back, true);
            setButtonEnabled(forward, true);
        } else if (newValue.equals(first)) {
            setButtonEnabled(back, false);
            setButtonEnabled(forward, true);
        } else {
            setButtonEnabled(back, true);
            setButtonEnabled(forward, false);
        }
    }

    private void updateItemDescription() {
        String description = null;
        final Object itemId = getValue();
        final Item item = getContainerDataSource().getItem(itemId);

        boolean isDescriptionBlank = true;
        if (item != null) {
            Property property = item.getItemProperty(descriptionPropertyName);

            if (property != null && property.getValue() != null) {
                description = property.getValue().toString();
            }
            isDescriptionBlank = StringUtils.isBlank(description);
            descriptionLabel.setValue(isDescriptionBlank ? "" : description);
        }

        if (isDescriptionBlank) {
            mainLayout.removeStyleName("has-description");
        } else {
            mainLayout.addStyleName("has-description");
        }

        descriptionLabel.setVisible(!isDescriptionBlank);
    }
}
