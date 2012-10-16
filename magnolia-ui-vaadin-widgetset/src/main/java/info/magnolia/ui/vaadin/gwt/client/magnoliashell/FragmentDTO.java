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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.VMainLauncher.ShellAppType;

/**
 * Helper class for holding the parsed info from the fragment.
 */
public class FragmentDTO {
    
    /**
     * Enum for the types of fragments used within MagnoliaShell.
     */
    public enum FragmentType {
        APP,
        SHELL_APP
    }
    
    private FragmentType type = FragmentType.SHELL_APP;
    
    private String prefix = "";
    
    private String token = "";
    
    protected FragmentDTO() {
    }
    
    public static FragmentDTO fromFragment(final String fragment) {
        final FragmentDTO dto = new FragmentDTO();
        String type = extractType(fragment);
        if (type.equals("shell")) {
            dto.type = FragmentType.SHELL_APP;
            dto.prefix = ShellAppType.getTypeByFragmentId(extractPrefix(fragment));
            dto.token = extractToken(fragment);
        } else if (type.equals("app")) {
            dto.type = FragmentType.APP;
            dto.prefix = extractPrefix(fragment);
            dto.token = extractToken(fragment);
        }
        return dto;
    }

    public FragmentType getType() {
        return type;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getToken() {
        return token;
    }

    // These methods are duplicated from info.magnolia.ui.framework.location.DefaultLocation

    public static String extractType(String fragment) {
        int i = fragment.indexOf(':');
        return i != -1 ? fragment.substring(0, i) : fragment;
    }

    public static String extractPrefix(String fragment) {
        int i = fragment.indexOf(':');
        if (i == -1) {
            return "";
        }
        int j = fragment.indexOf(':', i + 1);
        return j != -1 ? fragment.substring(i + 1, j) : fragment.substring(i + 1);
    }

    public static String extractToken(String fragment) {
        int i = fragment.indexOf(':');
        if (i == -1) {
            return "";
        }
        int j = fragment.indexOf(':', i + 1);
        if (j == -1) {
            return "";
        }
        return fragment.substring(j + 1);
    }
}
