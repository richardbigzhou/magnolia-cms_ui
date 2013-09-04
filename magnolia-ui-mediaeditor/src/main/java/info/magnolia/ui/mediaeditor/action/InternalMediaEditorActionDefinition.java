package info.magnolia.ui.mediaeditor.action;

import info.magnolia.ui.api.action.ConfiguredActionDefinition;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/4/13
 * Time: 12:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class InternalMediaEditorActionDefinition extends ConfiguredActionDefinition {

    private boolean isApplyAction = true;

    public InternalMediaEditorActionDefinition() {}

    public InternalMediaEditorActionDefinition(String id, String label, boolean isApplyAction) {
        setLabel(label);
        setName(id);
        setApplyAction(isApplyAction);
    }

    public boolean isApplyAction() {
        return isApplyAction;
    }

    public void setApplyAction(boolean applyAction) {
        isApplyAction = applyAction;
    }
}
