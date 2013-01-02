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

import static org.junit.Assert.assertEquals;

import info.magnolia.ui.vaadin.integration.terminal.IconFontResource;

import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

public class ResourceSerializerTest {

    private final String iconClassName = "icon-test";
    private final String themeResourceName = "path/to/myResource.svg";

    @Test
    public void testSerialize_IconFontResource() {
        // GIVEN
        IconFontResource resource = new IconFontResource(iconClassName);
        DummyResourceWrapper wrapperObject = new DummyResourceWrapper(resource);

        ResourceSerializer serializer = new ResourceSerializer();
        GsonBuilder gson = new GsonBuilder().registerTypeAdapter(Resource.class, serializer);

        String key = "resource";
        // equals to DummyResourceWrapper private field name; Gson serializes based on reflection
        String value = ResourceSerializer.RESOURCE_URI_SCHEME_ICONFONT + iconClassName;
        String expectedJson = "{\"" + key + "\":\"" + value + "\"}";

        // WHEN
        String json = gson.create().toJson(wrapperObject);

        // THEN
        assertEquals(expectedJson, json);
    }

    @Test
    public void testSerialize_ThemeResource() {
        // GIVEN
        ThemeResource resource = new ThemeResource(themeResourceName);
        DummyResourceWrapper wrapperObject = new DummyResourceWrapper(resource);

        ResourceSerializer serializer = new ResourceSerializer();
        GsonBuilder gson = new GsonBuilder().registerTypeAdapter(Resource.class, serializer);

        String key = "resource";
        // equals to DummyResourceWrapper private field name; Gson serializes based on reflection
        String value = ResourceSerializer.RESOURCE_URI_SCHEME_THEME + themeResourceName;
        String expectedJson = "{\"" + key + "\":\"" + value + "\"}";

        // WHEN
        String json = gson.create().toJson(wrapperObject);

        // THEN
        assertEquals(expectedJson, json);
    }

    private static class DummyResourceWrapper {

        private final Resource resource;

        public DummyResourceWrapper(Resource resource) {
            this.resource = resource;
        }
    }

}
