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
package info.magnolia.ui.admincentral.shellapp.pulse.task;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.task.Task;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.ConfiguredItemViewDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.ItemViewDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailActionExecutor;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionRegistry;
import info.magnolia.ui.admincentral.shellapp.pulse.task.definition.ConfiguredTaskUiDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.task.definition.TaskUiDefinition;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link DefaultTaskDetailPresenter}.
 */
public class DefaultTaskDetailPresenterTest {
    private DefaultTaskDetailPresenter presenter;
    private PulseDetailView view;
    private ItemViewDefinitionRegistry registry;
    private FormBuilder formbuilder;

    @Before
    public void setUp() {
        ConfiguredTaskUiDefinition definition = new ConfiguredTaskUiDefinition();
        definition.setTaskView("pages:publish");
        definition.setTitle("Test title");

        Task task = new Task();
        task.setComment("test comment");
        task.setContent(new HashMap<String, Object>() {{
            put("property1", "value1");
            put("property2", "value2");
        }});

        this.view = mock(PulseDetailView.class);
        this.registry = mock(ItemViewDefinitionRegistry.class);

        I18nizer i18n = mock(I18nizer.class);

        when(i18n.decorate(any(ItemViewDefinition.class))).thenAnswer(new Answer<ItemViewDefinition>() {
            @Override
            public ItemViewDefinition answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (ItemViewDefinition) args[0];
            }
        });
        formbuilder = mock(FormBuilder.class);
        presenter = new DefaultTaskDetailPresenter<TaskUiDefinition, Task>(view, definition, task, null, mock(PulseDetailActionExecutor.class), registry, formbuilder, mock(ActionbarPresenter.class), i18n);
    }

    @Test
    public void testGetItemViewFromDefinition() throws Exception {
        // Given

        // WHEN
        String taskView = presenter.getItemViewName();

        // THEN
        assertThat(taskView, is("pages:publish"));
    }

    @Test
    public void testViewTitleFromDefinition() throws Exception {
        // Given

        // WHEN
        presenter.setItemViewTitle(view);

        // THEN
        verify(view, times(1)).setTitle(eq("Test title"));
    }

    @Test
    public void testBeanPropertiesResolvedFromFieldDefs() throws Exception {
        // Given
        ConfiguredItemViewDefinition definition = new ConfiguredItemViewDefinition();

        definition.setForm(new ConfiguredFormDefinition() {{
            setTabs(new LinkedList<TabDefinition>() {{
                add(new ConfiguredTabDefinition() {{
                    setFields(new LinkedList<FieldDefinition>() {{
                        ConfiguredFieldDefinition field1 = new ConfiguredFieldDefinition();
                        field1.setName("comment");

                        ConfiguredFieldDefinition field2 = new ConfiguredFieldDefinition();
                        field2.setName("content.property1");

                        ConfiguredFieldDefinition field3 = new ConfiguredFieldDefinition();
                        field3.setName("content.property2");
                        add(field1);
                        add(field2);
                        add(field3);
                    }});
                }});
            }}
            );
        }});

        definition.setActions(new HashMap<String, ActionDefinition>());
        when(registry.get(anyString())).thenReturn(definition);

        // WHEN
        presenter.start();

        // THEN
        verify(formbuilder, times(1)).buildView(eq(definition.getForm()), argThat(new TaskItemMatcher()));
    }

    private class TaskItemMatcher extends BaseMatcher<TaskItem> {

        @Override
        public boolean matches(Object item) {
            TaskItem taskItem = (TaskItem) item;
            Collection<String> ids = (Collection<String>) taskItem.getItemPropertyIds();
            return ids.containsAll(Arrays.asList(new String[] {"content.property1", "content.property2", "comment"}));
        }

        @Override
        public void describeTo(Description description) {

        }
    }
}
