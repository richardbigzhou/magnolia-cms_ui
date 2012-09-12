/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.field.translator;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.vaadin.addon.propertytranslator.PropertyTranslator;

/**
 * {@link PropertyTranslator} used to encode and decode password fields see {link Base64}.
 * In general, if the translation is not possible, return emptyString.
 */
@SuppressWarnings("unchecked")
public class Base64Translator extends PropertyTranslator {

    public Base64Translator() {
    }

    /**
     * Decode.
     */
    @Override
    public Object translateFromDatasource(Object encoded) {
        if(StringUtils.isBlank((String)encoded)) {
            return StringUtils.EMPTY;
        }
        return new String(Base64.decodeBase64(((String)encoded).getBytes()));
    }
    /**
     * Encode.
     */
    @Override
    public Object translateToDatasource(Object decoded) throws Exception {
        if(StringUtils.isBlank((String)decoded)) {
            return StringUtils.EMPTY;
        }
        return  new String(Base64.encodeBase64(((String)decoded).getBytes()));
    }

}
