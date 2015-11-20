/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.vaadin.server;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.CompositeErrorMessage;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;

public class ErrorMessageUtilTest {
    private ErrorMessage foo;
    private ErrorMessage bar;

    @Before
    public void setUp() {
        foo = new UserError("foo");
        bar = new UserError("bar");
    }

    @Test
    public void findCausesForComposite() throws Exception {
        // GIVEN
        ErrorMessage composite = new CompositeErrorMessage(foo, bar);

        // WHEN
        List<ErrorMessage> causes = ErrorMessageUtil.getCauses(composite);

        // THEN
        assertThat(causes, containsInAnyOrder(foo, bar));
    }

    @Test
    public void findCausesMessagesForComposite() throws Exception {
        // GIVEN
        ErrorMessage composite = new CompositeErrorMessage(foo, bar);

        // WHEN
        List<String> causes = ErrorMessageUtil.getCausesMessages(composite);

        // THEN
        assertThat(causes, containsInAnyOrder("foo", "bar"));
    }

    @Test
    public void findCausesForErrorMessage() throws Exception {
        // GIVEN

        // WHEN
        List<ErrorMessage> causes = ErrorMessageUtil.getCauses(foo);

        // THEN
        assertThat(causes, hasItem(foo));
    }

    @Test
    public void findCausesMessagesForErrorMessage() throws Exception {
        // GIVEN

        // WHEN
        List<String> causes = ErrorMessageUtil.getCausesMessages(foo);

        // THEN
        assertThat(causes, hasItem("foo"));
    }
}
