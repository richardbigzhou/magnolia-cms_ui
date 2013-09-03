/**
 * This file Copyright (c) 2011-2013 Magnolia International
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
package info.magnolia.ui.contentapp.field;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import info.magnolia.ui.workbench.WorkbenchPresenter;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/27/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkbenchFieldFactory extends AbstractFieldFactory<WorkbenchFieldDefinition, Object> {

    private WorkbenchFieldDefinition definition;

    private WorkbenchPresenter workbenchPresenter;

    @Inject
    public WorkbenchFieldFactory(
            WorkbenchFieldDefinition definition,
            Item relatedFieldItem,
            WorkbenchPresenter workbenchPresenter) {
        super(definition, relatedFieldItem);
        this.definition = definition;
        this.workbenchPresenter = workbenchPresenter;
    }

    @Override
    protected Field<Object> createFieldComponent() {
        return new WorkbenchField(definition.getWorkbench(), definition.getImageProvider(), workbenchPresenter);
    }


    /*HorizontalLayout additionalContorlsWrapper = new HorizontalLayout();
    additionalContorlsWrapper.addComponent(new Button("Test", new Button.ClickListener() {
        @Override
        public void buttonClick(Button.ClickEvent event) {
            targetLayer.openOverlay(new View() {
                @Override
                public Component asVaadinComponent() {
                    SubAppDescriptor descriptor = getAppContext().getDefaultSubAppDescriptor();

                    Node root = null;
                    try {
                        root = MgnlContext.getInstance().getJCRSession("dam").getNode("/");
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                    FieldFactoryFactory ff = provider.get().getComponent(FieldFactoryFactory.class);
                    Item item = new JcrNewNodeAdapter(root, "mgnl:asset");
                    DamUploadFieldDefinition def = new DamUploadFieldDefinition();
                    DamUploadFieldFactory damFactory = (DamUploadFieldFactory) ff.createFieldFactory(def, item);

                    DamUploadField<?> field = (DamUploadField<?>) damFactory.createField();
                    field.setWidth("500px");
                    field.setHeight("500px");
                    return field;
                }
            }, OverlayLayer.ModalityLevel.LIGHT);
        }
    }));*/
}
