/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.actionbar.definition;

import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;

/**
 * This deprecated definition is for backwards compatibility of misconfigured content-apps, since Magnolia 5.3.
 *
 * <p>Concretely, the <em>multiple</em> property for section availability is barely ever configured (not even for
 * sections specifically intended for multi-selection). Therefore, this hack makes sure the actionbar never goes blank
 * upon multi-selection, while relying of *action-availability* for acting on multi-selection.
 *
 * <p>Note that, as of 5.4.3, this definition is (still) the default implementation of
 * {@link info.magnolia.ui.actionbar.definition.ActionbarSectionDefinition ActionbarSectionDefinition},
 * until further improvements.
 *
 * @deprecated since 5.4.3, you may use (configure) the regular {@link ConfiguredActionbarSectionDefinition},
 * and set the 'multiple' property to true in config where applicable.
 */
@Deprecated
public class DeprecatedActionbarSectionDefinition extends ConfiguredActionbarSectionDefinition {

    @Override
    public void setAvailability(AvailabilityDefinition availability) {
        // FIXME This is plain wrong, availability for multiple selection matches sections for which it is not configured (i.e. set to false)
        // But we have to keep it for the time being because some content apps rely on this incorrect behavior.
        ((ConfiguredAvailabilityDefinition) availability).setMultiple(true);
        super.setAvailability(availability);
    }
}
