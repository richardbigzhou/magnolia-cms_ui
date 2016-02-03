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
package info.magnolia.ui.framework.action;

import static org.mockito.Mockito.*;

import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;

import org.junit.Before;
import org.junit.Test;

public class EditorCallbackActionTest {

    private EditorCallbackAction action;

    private EditorCallback callback;

    private EditorValidator validator;

    private EditorCallbackActionDefinition definition = new EditorCallbackActionDefinition();

    @Before
    public void setUp() throws Exception {
        this.callback = mock(EditorCallback.class);
        this.validator = mock(EditorValidator.class);
        this.action = new EditorCallbackAction(definition, callback, validator);
    }

    @Test
    public void validatedCallbackFiredIfValid() throws Exception {
        // GIVEN
        definition.setValidationEnabled(true);
        doReturn(true).when(validator).isValid();

        // WHEN
        definition.setCallSuccess(true);
        action.execute();
        // THEN
        verify(callback).onSuccess(anyString());

        // WHEN
        definition.setCallSuccess(false);
        action.execute();
        // THEN
        verify(callback).onCancel();
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void validatedCallbackNotFiredIfNotValid() throws Exception {
        // GIVEN
        definition.setValidationEnabled(true);
        doReturn(false).when(validator).isValid();

        // WHEN
        definition.setCallSuccess(true);
        action.execute();

        definition.setCallSuccess(false);
        action.execute();

        // THEN
        verifyZeroInteractions(callback);
    }

    @Test
    public void nonValidatedCallbackFiresNoMatterWhat() throws Exception {
        // GIVEN
        definition.setValidationEnabled(false);

        // WHEN
        definition.setCallSuccess(true);
        action.execute();
        // THEN
        verify(callback).onSuccess(anyString());

        // WHEN
        definition.setCallSuccess(false);
        action.execute();
        // THEN
        verify(callback).onCancel();
        verifyNoMoreInteractions(callback);
    }
}
