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
package info.magnolia.ui.form.field.factory;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppLifecycleEvent;
import info.magnolia.ui.api.app.AppLifecycleEventHandler;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.upload.UploadReceiver;
import info.magnolia.ui.form.field.upload.basic.BasicUploadField;
import info.magnolia.ui.imageprovider.ImageProvider;

import javax.inject.Inject;
import javax.inject.Named;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;

/**
 * Creates and configures a Basic UploadField.
 */
public class BasicUploadFieldFactory extends AbstractFieldFactory<BasicUploadFieldDefinition, UploadReceiver> {

    private final ImageProvider imageProvider;
    private UiContext uiContext;
    private final SimpleTranslator i18n;
    private final ComponentProvider componentProvider;
    private EventBus admincentralEventBus;

    @Inject
    public BasicUploadFieldFactory(BasicUploadFieldDefinition definition, Item relatedFieldItem, ImageProvider imageProvider, UiContext uiContext, SimpleTranslator i18n, ComponentProvider componentProvider, @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus) {
        super(definition, relatedFieldItem);
        this.imageProvider = imageProvider;
        this.uiContext = uiContext;
        this.i18n = i18n;
        this.componentProvider = componentProvider;
        this.admincentralEventBus = admincentralEventBus;
    }

    @Override
    protected Field<UploadReceiver> createFieldComponent() {
        // Create Upload Filed.
        BasicUploadField<UploadReceiver> uploadField = new BasicUploadField<UploadReceiver>(imageProvider, uiContext, definition, i18n);
        this.admincentralEventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {
            @Override
            public void onAppStopped(AppLifecycleEvent event) {
                field.getValue().getFile().delete();
            }
        });
        return uploadField;
    }

    /**
     * Initialize the transformer.
     */
    @Override
    protected Transformer<?> initializeTransformer(Class<? extends Transformer<?>> transformerClass) {
        return this.componentProvider.newInstance(transformerClass, item, definition, UploadReceiver.class);
    }
}
