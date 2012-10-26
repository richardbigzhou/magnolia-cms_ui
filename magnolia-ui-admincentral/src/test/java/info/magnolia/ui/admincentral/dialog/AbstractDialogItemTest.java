/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.dialog;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AbstractDialogItemTest {

    @Before
    public void setUp() {
        // Node2Bean setup
        TypeMappingImpl typeMapping = new TypeMappingImpl();
        Node2BeanTransformer transformer = new Node2BeanTransformerImpl();
        ComponentsTestUtil.setInstance(Node2BeanProcessor.class, new Node2BeanProcessorImpl(typeMapping, transformer));

        DefaultMessagesManager manager = new DefaultMessagesManager();
        ComponentsTestUtil.setInstance(MessagesManager.class, manager);
        SystemContext systemContext = mock(SystemContext.class);
        when(systemContext.getLocale()).thenReturn(Locale.ENGLISH);
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);
        MockContext ctx = new MockContext();
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testEnsureMessagesContainsKeysDefinedByUIModules() throws Exception {
         //GIVEN
         AbstractDialogItem dummyDialogItem = new AbstractDialogItem() {

            @Override
            protected String getI18nBasename() {
                return "dummy.basename";
            }
        };

        //WHEN validation.message.required is a key defined by the ui-model module
        String message = dummyDialogItem.getMessages().get("validation.message.required");

        //THEN
        assertFalse(message.startsWith("???"));
    }
}
