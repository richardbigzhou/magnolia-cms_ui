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
package info.magnolia.ui.dialog.registry;

import info.magnolia.config.registry.DefinitionMetadata;
import info.magnolia.config.registry.DefinitionMetadataBuilder;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.DefinitionProviderWrapper;
import info.magnolia.config.registry.DefinitionRawView;
import info.magnolia.config.registry.Registry;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Adapter for using legacy DialogDefinitionProvider as DefinitionProvider&lt;DialogDefinition&gt;.
 */
public class LegacyDialogDefinitionProviderAdapter implements DefinitionProvider<DialogDefinition> {

    private DialogDefinitionProvider provider;

    public LegacyDialogDefinitionProviderAdapter(DialogDefinitionProvider provider) {
        this.provider = provider;
    }

    @Override
    public DefinitionMetadata getMetadata() {

        final String id = provider.getId();
        final String module = StringUtils.substringBefore(id, ":");
        final String relativeLocation = StringUtils.substringAfter(id, ":");
        final String name = relativeLocation.indexOf('/') != -1 ? StringUtils.substringAfterLast(relativeLocation, "/") : relativeLocation;

        return DefinitionMetadataBuilder.usingModuleAndRelativePathAsId()
                .type(DefinitionTypes.DIALOG)
                .name(name)
                .module(module)
                .relativeLocation(relativeLocation).build();
    }

    @Override
    public DefinitionRawView getRaw() {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public List<String> getErrorMessages() {
        return Collections.emptyList();
    }

    @Override
    public DialogDefinition get() {
        try {
            return provider.getDialogDefinition();
        } catch (RegistrationException e) {
//            throw new Registry.InvalidDefinitionException(e); // protected access, can't create it
            throw new RuntimeException(e);
        }
    }

    public Class<? extends FormDialogPresenter> getPresenterClass() throws RegistrationException {
        return provider.getPresenterClass();
    }

    /**
     * Uses reflection to navigate down a chain of DefinitionProviderWrappers to find a wrapped
     * LegacyDialogDefinitionProviderAdapter.
     */
    @SuppressWarnings("unchecked")
    public static LegacyDialogDefinitionProviderAdapter unwrap(DefinitionProvider<DialogDefinition> provider) {
        try {
            Field field = DefinitionProviderWrapper.class.getDeclaredField("delegate");
            field.setAccessible(true);

            while (provider instanceof DefinitionProviderWrapper) {
                provider = (DefinitionProviderWrapper)field.get(provider);
            }

            if (provider instanceof LegacyDialogDefinitionProviderAdapter) {
                return (LegacyDialogDefinitionProviderAdapter) provider;
            }

        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException ignored) {
        }

        return null;
    }
}
