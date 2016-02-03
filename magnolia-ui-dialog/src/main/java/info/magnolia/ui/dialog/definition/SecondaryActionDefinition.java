/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.dialog.definition;

/**
 * Simple definition interface that describes a secondary action for {@link info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenter}.
 */
public class SecondaryActionDefinition {

    public SecondaryActionDefinition() {
    }

    public SecondaryActionDefinition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof SecondaryActionDefinition) {
            SecondaryActionDefinition that = (SecondaryActionDefinition) o;
            return name == null ? that.name == null : name.equals(that.name);
        }

        if (o instanceof String) {
            String oName = (String)o;
            return name == null ? oName == null : name.equals(oName);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
