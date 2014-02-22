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
package info.magnolia.ui.api.availability.voters;

import info.magnolia.voting.Voter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.vaadin.data.Item;

/**
 * {@link Voter} implementation used for action availability verification based on Vaadin {@link Item}.
 * Capable of dealing with single items, item arrays, collections and maps (item should be the value and
 * value set is evaluated). Actual verification is done in
 * {@link AbstractActionAvailabilityVoter#isAvailableForItem(com.vaadin.data.Item)}.
 */
public abstract class AbstractActionAvailabilityVoter implements Voter {

    private boolean isMultiple = false;

    @Override
    public int vote(Object value) {
        boolean isAvailable = true;
        if (value instanceof Item) {
            isAvailable =  isAvailableForItem((Item)value);
        } else if (value instanceof Item[]) {
            isAvailable = isAvailableForItems(Arrays.asList((Item[]) value));
        } else if (value instanceof Collection) {
            isAvailable = isAvailableForItems((Collection<?>) value);
        } else if (value instanceof Map) {
            isAvailable = isAvailableForItems(((Map) value).values());
        }
        return isAvailable ? 1 : 0;
    }

    protected boolean isAvailableForItems(Collection<?> items) {
        isMultiple = items.size() > 1;
        boolean isAvailableForAll = true;
        Iterator<?> it = items.iterator();
        while (isAvailableForAll && it.hasNext()) {
            final Object object = it.next();
            if (object instanceof Item || object == null) {
                final Item item = (Item) object;
                isAvailableForAll &= isAvailableForItem(item);
            }
        }
        return isAvailableForAll;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    protected abstract boolean isAvailableForItem(Item item);

    protected boolean isMultiple() {
        return isMultiple;
    }
}
