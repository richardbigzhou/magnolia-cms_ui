/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.mediaeditor.action.availability;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.availability.AvailabilityRuleDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class {@link MediaEditorAvailabilityChecker}
 */
public class MediaEditorAvailabilityCheckerTest {

    private ComponentProvider componentProvider;

    private MediaEditorAvailabilityChecker mediaEditorAvailabilityChecker;

    private ConfiguredAvailabilityDefinition availabilityDefinition;
    private Collection<AvailabilityRuleDefinition> rules;

    @Before
    public void setUp() throws Exception {
        componentProvider = mock(ComponentProvider.class);

        mediaEditorAvailabilityChecker = new MediaEditorAvailabilityChecker(componentProvider);

        availabilityDefinition = new ConfiguredAvailabilityDefinition();
        rules = new ArrayList<>();
        availabilityDefinition.setRules(rules);


    }

    @Test
    public void testIsAvailable() {
        // GIVEN
        ConfiguredAvailabilityRuleDefinition availabilityRuleDefinition = new ConfiguredAvailabilityRuleDefinition();
        availabilityRuleDefinition.setImplementationClass(AbstractMediaEditorAvailabilityRule.class);
        rules.add(availabilityRuleDefinition);

        AbstractMediaEditorAvailabilityRule rule = mock(AbstractMediaEditorAvailabilityRule.class);
        when(rule.isAvailable()).thenReturn(true);

        when(componentProvider.newInstance(AbstractMediaEditorAvailabilityRule.class, availabilityRuleDefinition)).thenReturn(rule);

        // WHEN
        boolean isAvailable = mediaEditorAvailabilityChecker.isAvailable(availabilityDefinition);

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void testIsNotAvailable() {
        // GIVEN
        ConfiguredAvailabilityRuleDefinition availabilityRuleDefinition = new ConfiguredAvailabilityRuleDefinition();
        availabilityRuleDefinition.setImplementationClass(AbstractMediaEditorAvailabilityRule.class);
        rules.add(availabilityRuleDefinition);

        AbstractMediaEditorAvailabilityRule rule = mock(AbstractMediaEditorAvailabilityRule.class);
        when(rule.isAvailable()).thenReturn(false);

        when(componentProvider.newInstance(AbstractMediaEditorAvailabilityRule.class, availabilityRuleDefinition)).thenReturn(rule);

        // WHEN
        boolean isAvailable = mediaEditorAvailabilityChecker.isAvailable(availabilityDefinition);

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testIsNotAvailableWhenMoreRulesAreDefined() {
        // GIVEN
        ConfiguredAvailabilityRuleDefinition availabilityRuleDefinitionTrue = new ConfiguredAvailabilityRuleDefinition();
        availabilityRuleDefinitionTrue.setImplementationClass(AbstractMediaEditorAvailabilityRule.class);
        rules.add(availabilityRuleDefinitionTrue);

        AbstractMediaEditorAvailabilityRule ruleTrue = mock(AbstractMediaEditorAvailabilityRule.class);
        when(ruleTrue.isAvailable()).thenReturn(true);

        when(componentProvider.newInstance(AbstractMediaEditorAvailabilityRule.class, availabilityRuleDefinitionTrue)).thenReturn(ruleTrue);

        ConfiguredAvailabilityRuleDefinition availabilityRuleDefinitionFalse = new ConfiguredAvailabilityRuleDefinition();
        availabilityRuleDefinitionFalse.setImplementationClass(AbstractMediaEditorAvailabilityRule.class);
        rules.add(availabilityRuleDefinitionFalse);

        AbstractMediaEditorAvailabilityRule ruleFalse = mock(AbstractMediaEditorAvailabilityRule.class);
        when(ruleFalse.isAvailable()).thenReturn(false);

        when(componentProvider.newInstance(AbstractMediaEditorAvailabilityRule.class, availabilityRuleDefinitionFalse)).thenReturn(ruleFalse);

        // WHEN
        boolean isAvailable = mediaEditorAvailabilityChecker.isAvailable(availabilityDefinition);

        // THEN
        assertFalse(isAvailable);
    }
}
