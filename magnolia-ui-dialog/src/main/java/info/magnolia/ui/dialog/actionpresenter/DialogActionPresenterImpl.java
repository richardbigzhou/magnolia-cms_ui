package info.magnolia.ui.dialog.actionpresenter;

import com.vaadin.event.ShortcutListener;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionpresenter.definition.ActionRendererDefinition;
import info.magnolia.ui.dialog.actionpresenter.definition.EditorActionPresenterDefinition;
import info.magnolia.ui.dialog.actionpresenter.renderer.ActionRenderer;
import info.magnolia.ui.dialog.actionpresenter.view.DialogActionView;
import info.magnolia.ui.dialog.definition.SecondaryActionDefinition;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 12:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class DialogActionPresenterImpl implements DialogActionPresenter {

    private final DialogActionView view;

    private final ComponentProvider componentProvider;

    private final EditorActionExecutor actionExecutor;

    private ActionParameterProvider actionParameterProvider;

    @Inject
    public DialogActionPresenterImpl(DialogActionView view, ComponentProvider componentProvider, EditorActionExecutor actionExecutor) {
        this.view = view;
        this.componentProvider = componentProvider;
        this.actionExecutor = actionExecutor;
    }

    @Override
    public DialogActionView start(Iterable<ActionDefinition> actions, EditorActionPresenterDefinition definition, final ActionParameterProvider parameterProvider) {
        this.actionParameterProvider = parameterProvider;
        actionExecutor.setActions(actions);
        for (ActionDefinition action : actions) {
            ActionRendererDefinition actionPresenterDef = definition.getActionRenderers().get(action.getName());
            ActionRenderer actionRenderer = actionPresenterDef == null ?
                    componentProvider.getComponent(ActionRenderer.class):
                    componentProvider.newInstance(actionPresenterDef.getPresenterClass(), action);
            final View actionView = actionRenderer.start(action, new ActionListener() {
                @Override
                public void onActionFired(String actionName, Object... actionContextParams) {
                    Object[] providedParameters = parameterProvider.getActionParameters(actionName);
                    Object[] combinedParameters = new Object[providedParameters.length + actionContextParams.length];
                    System.arraycopy(providedParameters, 0, combinedParameters, 0, providedParameters.length);
                    System.arraycopy(actionContextParams, 0, combinedParameters, providedParameters.length, actionContextParams.length);
                    executeAction(actionName, combinedParameters);
                }
            });
            if (definition.getSecondaryActions().contains(new SecondaryActionDefinition(action.getName()))) {
                view.addSecondaryAction(actionView);
            } else {
                view.addPrimaryAction(actionView);
            }
        }
        return view;
    }

    @Override
    public ShortcutListener bindShortcut(final String actionName, int keyCode, int... modifiers) {
        return new ShortcutListener("", keyCode, modifiers) {
            @Override
            public void handleAction(Object sender, Object target) {
                executeAction(actionName, actionParameterProvider.getActionParameters(actionName));
            }
        };
    }

    protected void executeAction(String actionName, Object[] combinedParameters) {
        try {
            actionExecutor.execute(actionName, combinedParameters);
        } catch (ActionExecutionException e) {
            handleActionExecutionException(actionName, e);
        }
    }

    protected void handleActionExecutionException(String actionName, ActionExecutionException e) {
        throw new RuntimeException("Could not execute action: " + actionName, e);
    }
}
