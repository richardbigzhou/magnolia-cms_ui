package info.magnolia.ui.mediaeditor.action;

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/4/13
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class InternalMediaEditorAction extends AbstractAction<InternalMediaEditorActionDefinition> {

    @Inject
    public InternalMediaEditorAction(InternalMediaEditorActionDefinition definition) {
        super(definition);
    }

    @Override
    public void execute() throws ActionExecutionException {
    }
}
