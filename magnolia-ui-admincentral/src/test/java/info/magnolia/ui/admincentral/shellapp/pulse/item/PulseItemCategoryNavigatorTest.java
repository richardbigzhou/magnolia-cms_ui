/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.item;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategoryNavigator;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategoryNavigator.ItemCategoryTab;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * PulseItemCategoryNavigatorTest.
 */
public class PulseItemCategoryNavigatorTest {
    private PulseItemCategoryNavigator categoryNavigator;
    private SystemContext sysCtx;
    private Context ctx;
    private SimpleTranslator i18n;

    @Before
    public void setUp() throws Exception {

        ctx = mock(Context.class);
        // current context locale
        MgnlContext.setInstance(ctx);

        sysCtx = mock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, sysCtx);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        // configure node2bean because its processor is injected into DefaultMessagesManager constructor
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);

        i18n = mock(SimpleTranslator.class);
        when(i18n.translate("pulse.messages.problems")).thenReturn("Problems");
        categoryNavigator = new PulseItemCategoryNavigator(i18n, false, false);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void categoryLabelWithCurrentLocaleAsEnglish() throws Exception {
        // WHEN
        ItemCategoryTab tab = categoryNavigator.new ItemCategoryTab(PulseItemCategory.PROBLEM);
        // THEN
        assertEquals("Problems", tab.getLabel());
    }
}
