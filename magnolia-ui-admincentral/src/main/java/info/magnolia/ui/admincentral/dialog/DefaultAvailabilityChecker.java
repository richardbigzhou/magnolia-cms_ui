/**
 * This file Copyright (c) 2013-2014 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.admincentral.dialog;

import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;

import java.util.List;

/**
 * Stub implementation of {@link AvailabilityChecker} interface, always returns true.
 */
public class DefaultAvailabilityChecker implements AvailabilityChecker {

    @Override
    public boolean isAvailable(AvailabilityDefinition definition, List<Object> ids) {
        return true;
    }
}
