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
package info.magnolia.ui.dialog;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.i18nsystem.FixedLocaleProvider;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.OverlayLayer.ModalityLevel;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionarea.DialogActionExecutor;
import info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenter;
import info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenterImpl;
import info.magnolia.ui.dialog.actionarea.renderer.ActionRenderer;
import info.magnolia.ui.dialog.actionarea.renderer.DefaultEditorActionRenderer;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaView;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaViewImpl;
import info.magnolia.ui.dialog.definition.ConfiguredDialogDefinition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.event.Action;
import com.vaadin.event.ActionManager;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.KeyMapper;

/**
 * Test class for {@link BaseDialogPresenter}.
 */
public class BaseDialogPresenterTest {

    private ComponentProvider componentProvider = mock(ComponentProvider.class);

    private UiContext uiContext = mock(UiContext.class);

    private DialogActionExecutor executor = new DialogActionExecutor(componentProvider);

    private TestBaseDialogViewImpl view = new TestBaseDialogViewImpl();

    private EditorActionAreaView actionAreaView = new EditorActionAreaViewImpl();

    private TestEditorActionAreaPresenterImpl actionAreaPresenter = new TestEditorActionAreaPresenterImpl(actionAreaView);

    private ConfiguredDialogDefinition definition = new ConfiguredDialogDefinition();

    private ToggleableAction action1;

    private ToggleableAction action2;

    private I18nizer i18nizer = new ProxytoysI18nizer(new TestTranslationService(), new FixedLocaleProvider(Locale.ENGLISH));

    private SimpleTranslator i18n = mock(SimpleTranslator.class);

    private BaseDialogPresenter presenter = new BaseDialogPresenter(componentProvider, executor, view, i18nizer, i18n);

    @Before
    public void setUp() throws Exception {
        when(componentProvider.newInstance(EditorActionAreaPresenter.class)).thenReturn(actionAreaPresenter);
        when(componentProvider.getComponent(ActionRenderer.class)).thenReturn(new DefaultEditorActionRenderer());

        initializeActions();

        definition.setId("testDialog");

        this.executor.setDialogDefinition(definition);
    }

    @Test
    public void testGetView() throws Exception {
        // WHEN
        presenter.start(definition, uiContext);

        // THEN
        assertEquals(presenter.getView(), this.view);
    }

    @Test
    public void testGetActionArea() throws Exception {
        // WHEN
        presenter.start(definition, uiContext);

        // THEN
        assertEquals(presenter.getActionArea(), this.actionAreaPresenter);
    }

    @Test
    public void testAddShortcut() throws Exception {
        // WHEN
        presenter.addShortcut("action1", ShortcutAction.KeyCode.T, new int[0]);
        presenter.start(definition, uiContext);
        view.getActionManager().handleAction("action1");

        // THEN
        assert (action1.isExecuted());
    }

    @Test
    public void testStart() throws Exception {
        // WHEN
        View view = presenter.start(definition, uiContext);

        // THEN
        assertEquals(view, this.view);
    }

    @Test
    public void testFilterActions() throws Exception {
        // WHEN
        presenter.start(definition, uiContext);

        // THEN
        Collection<?> result = (Collection<?>) presenter.filterActions();
        assertEquals(result.size(), 2);
    }

    @Test
    public void testGetActionParameters() throws Exception {
        // WHEN
        Object[] params = presenter.getActionParameters("");

        // THEN
        assertEquals(params.length, 1);
        assertEquals(params[0], presenter);
    }

    @Test
    public void testOnActionFired() throws Exception {
        // WHEN
        presenter.start(definition, uiContext);
        presenter.onActionFired("action1");

        // THEN
        assertTrue(action1.isExecuted());
    }

    @Test
    public void testExecuteAction() throws Exception {
        // WHEN
        presenter.start(definition, uiContext);
        presenter.executeAction("action1", new Object[0]);

        // THEN
        assertTrue(action1.isExecuted());
    }

    @Test
    public void testModalityLevelIsSetToView() throws Exception {
        // GIVEN
        definition.setModalityLevel(ModalityLevel.LIGHT);
        assertEquals(ModalityLevel.STRONG, view.getModalityLevel());

        // WHEN
        DialogView view = presenter.start(definition, uiContext);

        // THEN
        assertEquals(ModalityLevel.LIGHT, view.getModalityLevel());
    }

    /**
     * Test translation service.
     */
    public static class TestTranslationService implements TranslationService {

        @Override
        public String translate(LocaleProvider localeProvider, String basename, String[] keys) {
            return "translated with key [" + keys[0] + "] and basename [" + basename + "] and locale [" + localeProvider.getLocale() + "]";
        }

        @Override
        public String translate(LocaleProvider localeProvider, String[] keys) {
            return "translated with key [" + keys[0] + "] and locale [" + localeProvider.getLocale() + "]";
        }

        @Override
        public void reloadMessageBundles() {
        }
    }

    private static class ToggleableAction extends AbstractAction<ConfiguredActionDefinition> {

        private boolean isExecuted = false;

        protected ToggleableAction(ConfiguredActionDefinition definition) {
            super(definition);
        }

        @Override
        public void execute() throws ActionExecutionException {
            this.isExecuted = true;
        }

        private boolean isExecuted() {
            return isExecuted;
        }

    }

    private static class TestBaseDialogViewImpl extends BaseDialogViewImpl {

        private TestActionManager mgr = new TestActionManager();

        @Override
        public TestActionManager getActionManager() {
            return mgr;
        }

        private static class TestActionManager extends ActionManager {

            private Map<String, String> nameToKey = new HashMap();

            KeyMapper<Action> mapper = new KeyMapper<Action>();

            KeyMapper<Action> getMapper() {
                return mapper;
            }

            void handleAction(String action) {
                handleAction(getMapper().get(nameToKey.get(action)), null, null);
            }

            @Override
            public <T extends Action & Action.Listener> void addAction(T action) {
                super.addAction(action);
                nameToKey.put(action.getCaption(), mapper.key(action));
            }

        }
    }

    private class TestEditorActionAreaPresenterImpl extends EditorActionAreaPresenterImpl {

        public TestEditorActionAreaPresenterImpl(EditorActionAreaView actionAreaView) {
            super(actionAreaView, BaseDialogPresenterTest.this.componentProvider);
        }

        @Override
        public EditorActionAreaView getView() {
            return super.getView();
        }

    }

    private void initializeActions() {
        ConfiguredActionDefinition actionDef1 = new ConfiguredActionDefinition();
        actionDef1.setName("action1");
        actionDef1.setImplementationClass(ToggleableAction.class);

        ConfiguredActionDefinition actionDef2 = new ConfiguredActionDefinition();
        actionDef2.setName("action2");
        actionDef2.setImplementationClass(ToggleableAction.class);
        this.definition.addAction(actionDef1);
        this.definition.addAction(actionDef2);

        this.action1 = new ToggleableAction(actionDef1);
        this.action2 = new ToggleableAction(actionDef2);

        when(componentProvider.newInstance(ToggleableAction.class, actionDef1, presenter)).thenReturn(action1);
        when(componentProvider.newInstance(ToggleableAction.class, actionDef2, presenter)).thenReturn(action2);
    }

}
