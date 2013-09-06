package info.magnolia.ui.dialog.actionpresenter;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionpresenter.definition.ActionRendererDefinition;
import info.magnolia.ui.dialog.actionpresenter.definition.DialogActionPresenterDefinition;
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

    @Inject
    public DialogActionPresenterImpl(DialogActionView view, ComponentProvider componentProvider) {
        this.view = view;
        this.componentProvider = componentProvider;
    }

    @Override
    public DialogActionView start(Iterable<ActionDefinition> actions, DialogActionPresenterDefinition definition, ActionListener listener) {
        for (ActionDefinition action : actions) {
            ActionRendererDefinition actionPresenterDef = definition.getActionRenderers().get(action.getName());
            ActionRenderer actionRenderer = actionPresenterDef == null ?
                    componentProvider.getComponent(ActionRenderer.class):
                    componentProvider.newInstance(actionPresenterDef.getPresenterClass(), action);
            final View actionView = actionRenderer.start(action, listener);
            if (definition.getSecondaryActions().contains(new SecondaryActionDefinition(action.getName()))) {
                view.addSecondaryAction(actionView);
            } else {
                view.addPrimaryAction(actionView);
            }
        }
        return view;
    }
}
