package info.magnolia.ui.contentapp.movedialog;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.contentapp.movedialog.view.MoveDialogActionView;
import info.magnolia.ui.dialog.actionpresenter.ActionParameterProvider;
import info.magnolia.ui.dialog.actionpresenter.DialogActionPresenterImpl;
import info.magnolia.ui.dialog.actionpresenter.EditorActionExecutor;
import info.magnolia.ui.dialog.actionpresenter.definition.EditorActionPresenterDefinition;
import info.magnolia.ui.dialog.actionpresenter.view.DialogActionView;
import info.magnolia.ui.framework.action.MoveLocation;

import javax.inject.Inject;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/7/13
 * Time: 8:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoveDialogActionPresenterImpl extends DialogActionPresenterImpl implements MoveDialogActionPresenter {

    @Inject
    public MoveDialogActionPresenterImpl(MoveDialogActionView view, ComponentProvider componentProvider, EditorActionExecutor actionExecutor) {
        super(view, componentProvider, actionExecutor);
    }

    @Override
    public DialogActionView start(Iterable<ActionDefinition> actions, EditorActionPresenterDefinition definition, ActionParameterProvider parameterProvider, UiContext uiContext) {
        return super.start(actions, definition, parameterProvider, uiContext);
    }

    @Override
    public void setPossibleMoveLocations(Set<MoveLocation> possibleMoveLocations) {
        for (MoveLocation location : MoveLocation.values()) {
            getView().setActionEnabled(location.name(), possibleMoveLocations.contains(location));
        }
    }

    @Override
    protected MoveDialogActionView getView() {
        return (MoveDialogActionView) super.getView();
    }
}
