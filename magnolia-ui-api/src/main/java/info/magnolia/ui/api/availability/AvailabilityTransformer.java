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
package info.magnolia.ui.api.availability;

import info.magnolia.jcr.node2bean.PropertyTypeDescriptor;
import info.magnolia.jcr.node2bean.TransformationState;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.availability.voters.DefaultJCRAvailabilityRules;
import info.magnolia.voting.Voter;

import java.util.Map;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prepares {@link Voter}'s for {@link VoterBasedAvailability} definitions.
 * Converts simple properties of 'availability' node into voter objects
 * based on {@link DefaultJCRAvailabilityRules} type.
 */
public class AvailabilityTransformer extends Node2BeanTransformerImpl {

    private Logger log = LoggerFactory.getLogger(getClass());

    public static final String CRITERIAS = "criterias";

    @Override
    public void setProperty(TypeMapping mapping, TransformationState state, PropertyTypeDescriptor descriptor, Map<String, Object> values) throws RepositoryException {
        Object currentBean = state.getCurrentBean();
        if (CRITERIAS.equalsIgnoreCase(descriptor.getName()) && (currentBean instanceof VoterBasedAvailability)) {
            VoterBasedAvailability availability = (VoterBasedAvailability)currentBean;
            availability.getCriterias().clear();
            for (DefaultJCRAvailabilityRules rule : DefaultJCRAvailabilityRules.values()) {
                Object arg = values.get(rule.toString());
                if (arg == null) {
                    arg = rule.getDefaultValue() == null ? new Object() : rule.getDefaultValue();
                }
                try {
                    Voter voter = Components.newInstance(rule.getVoterClass(), arg);
                    availability.addCriteria(voter);
                } catch (Exception e) {
                    log.error("Failed to create voter: " + e.getMessage() + " for arg: " + arg, e);
                }
            }
        }
        super.setProperty(mapping, state, descriptor, values);
    }
}
