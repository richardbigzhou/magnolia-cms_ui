/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.vaadin.integration.serializer;

import info.magnolia.ui.vaadin.integration.terminal.IconFontResource;

import java.lang.reflect.Type;

import org.apache.commons.lang.SerializationException;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vaadin.Application;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;

/**
 * Gson custom serializer to serialize vaadin resources with appropriate uri.
 *
 * @see JsonPaintTarget#addAttribute(String, Resource)
 */
@SuppressWarnings("deprecation")
public class ResourceSerializer implements JsonSerializer<Resource> {

    public static final String RESOURCE_URI_SCHEME_ICONFONT = "iconfont://";
    public static final String RESOURCE_URI_SCHEME_THEME = "theme://";

    @Override
    public JsonElement serialize(Resource src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }

        if (src instanceof ExternalResource) {
            return new JsonPrimitive(((ExternalResource) src).getURL());

        } else if (src instanceof IconFontResource) {
            return new JsonPrimitive(RESOURCE_URI_SCHEME_ICONFONT + ((IconFontResource) src).getCssClassName());

        } else if (src instanceof ApplicationResource) {
            final ApplicationResource r = (ApplicationResource) src;
            final Application a = r.getApplication();
            if (a == null) {
                throw new SerializationException("Application not specified for resource " + src.getClass().getName());
            }
            return new JsonPrimitive(a.getRelativeLocation(r));

        } else if (src instanceof ThemeResource) {
            return new JsonPrimitive(RESOURCE_URI_SCHEME_THEME + ((ThemeResource) src).getResourceId());
        }

        return new JsonPrimitive(src.toString());
    }
}