/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.app.contacts;

import info.magnolia.jcr.util.NodeTypes;

/**
 * Contact defined NodeTypes.
 */
public class ContactNodeType {
    /**
     * Represents the nodeType mgnl:contact.
     */
    public static class Contact {
        // Node Type Name
        public static final String NAME = NodeTypes.MGNL_PREFIX + "contact";

        // Property Name
        public static final String CONTACT_PROPERTY_CITY = "city";
        public static final String CONTACT_PROPERTY_COUNTRY = "country";
        public static final String CONTACT_PROPERTY_EMAIL = "email";
        public static final String CONTACT_PROPERTY_FIRST_NAME = "firstName";
        public static final String CONTACT_PROPERTY_LAST_NAME = "lastName";
        public static final String CONTACT_PROPERTY_MOBILE_PHONE_NR = "mobilePhoneNr";
        public static final String CONTACT_PROPERTY_OFFICE_FAX_NR = "officeFaxNr";
        public static final String CONTACT_PROPERTY_OFFICE_PHONE_NR = "officePhoneNr";
        public static final String CONTACT_PROPERTY_ORGANIZATION_NAME = "organizationName";
        public static final String CONTACT_PROPERTY_ORGANIZATION_UNIT_NAME = "organizationUnitName";
        public static final String CONTACT_PROPERTY_PHOTO_ALT_TEXT = "photoAltText";
        public static final String CONTACT_PROPERTY_PHOTO_CAPTION = "photoCaption";
        public static final String CONTACT_PROPERTY_SALUTATION = "salutation";
        public static final String CONTACT_PROPERTY_STREET_ADDRESS = "streetAddress";
        public static final String CONTACT_PROPERTY_WEBSITE = "website";
        public static final String CONTACT_PROPERTY_ZIP_CODE = "zipCode";
        public static final String CONTACT_IMAGE_NODE_NAME = "photo";
    }
}
