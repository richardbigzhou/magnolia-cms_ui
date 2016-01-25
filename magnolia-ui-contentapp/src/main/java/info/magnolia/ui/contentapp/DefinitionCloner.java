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
package info.magnolia.ui.contentapp;

import info.magnolia.context.Context;
import info.magnolia.objectfactory.annotation.LazySingleton;
import info.magnolia.objectfactory.annotation.LocalScoped;
import info.magnolia.objectfactory.annotation.SessionScoped;

import java.util.Map;

import javax.inject.Singleton;

import com.rits.cloning.Cloner;

/**
 * Up until Magnolia 5.4, we were using {@link Cloner} directly. To avoid duplicating configuration code everywhere,
 * we're introducing this wrapper which pre-configures it to work nicely with our definition objects.
 *
 * Specifically, it prevents cloning {@link Context}.
 *
 * Note: Initially we wanted to avoid cloning all proxy {@link net.sf.cglib.proxy.Callback} fields of definitions, which we
 * use extensively in the i18n mechanism. But that leads to the cloned object to still delegate method calls to the original
 * due to proxy specifics.
 */
public class DefinitionCloner extends Cloner {

    public DefinitionCloner() {
        this.dontCloneInstanceOf(Context.class); //not every context is marked with an annotation, e.g. info.magnolia.context.WebContextFactoryImpl.createWebContext(), so we need to exclude all implementations
    }

    /**
     * Doesn't clone classes with {@link Singleton}, {@link LazySingleton}, {@link SessionScoped} or {@link LocalScoped} annotation.
     */
    @Override
    protected <T> T cloneInternal(T o, Map<Object, Object> clones) throws IllegalAccessException {
        if (o == null) return null;
        if (o.getClass().getAnnotation(Singleton.class) != null || o.getClass().getAnnotation(LazySingleton.class) != null || o.getClass().getAnnotation(SessionScoped.class) != null || o.getClass().getAnnotation(LocalScoped.class) != null) {
            return o; //do not clone Magnolia components, this expect annotation to be set on the component and does not cover the cases when annotation on the component is not present.
        }
        return super.cloneInternal(o, clones);
    }
}
