/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.pages.app.field;

import info.magnolia.objectfactory.Components;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Builds the {@linkplain info.magnolia.ui.form.field.definition.SelectFieldDefinition select field} for selecting component templates.
 * <p>
 * Available {@linkplain SelectFieldOptionDefinition options} are created according to the currently selected area in {@link PageEditorPresenter}.
 */
public class ComponentSelectorFieldFactory extends SelectFieldFactory<ComponentSelectorDefinition> {

    private static final Logger log = LoggerFactory.getLogger(ComponentSelectorFieldFactory.class);

    private final TemplateDefinitionRegistry templateRegistry;
    private final PageEditorPresenter pageEditorPresenter;

    @Inject
    public ComponentSelectorFieldFactory(ComponentSelectorDefinition definition, Item relatedFieldItem, TemplateDefinitionRegistry templateRegistry, PageEditorPresenter pageEditorPresenter) {
        super(definition, relatedFieldItem);
        this.templateRegistry = templateRegistry;
        this.pageEditorPresenter = pageEditorPresenter;
    }

    /**
     * @deprecated since 5.3.8, use {@link #ComponentSelectorFieldFactory(ComponentSelectorDefinition, Item, TemplateDefinitionRegistry, PageEditorPresenter)} instead.
     */
    @Deprecated
    public ComponentSelectorFieldFactory(ComponentSelectorDefinition definition, Item relatedFieldItem) {
        this(definition, relatedFieldItem, Components.getComponent(TemplateDefinitionRegistry.class), Components.getComponent(PageEditorPresenter.class));
    }

    /**
     * Returns the available templates based on the current area.
     */
    @Override
    public List<SelectFieldOptionDefinition> getSelectFieldOptionDefinition() {
        List<SelectFieldOptionDefinition> res = new ArrayList<SelectFieldOptionDefinition>();

        if (!(pageEditorPresenter.getSelectedElement() instanceof AreaElement)) {
            log.warn("Cannot get available components, selected element {} is not an area.", pageEditorPresenter.getSelectedElement());
            return res;
        }

        AreaElement area = (AreaElement) pageEditorPresenter.getSelectedElement();
        String availableComponents = area.getAvailableComponents();

        String[] tokens = availableComponents.split(",");
        for (String token : tokens) {
            try {
                TemplateDefinition templateDefinition = templateRegistry.getTemplateDefinition(token);

                SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
                option.setValue(templateDefinition.getId());
                option.setLabel(TemplateSelectorFieldFactory.getI18nTitle(templateDefinition));
                res.add(option);
            } catch (RegistrationException e) {
                log.error("Could not get TemplateDefinition for id '{}'.", token, e);
            }
        }

        return res;
    }

    @Override
    protected Class<?> getDefaultFieldType() {
        return String.class;
    }

}
