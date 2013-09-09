package info.magnolia.ui.dialog.actionarea.definition;

import info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenter;
import info.magnolia.ui.dialog.definition.SecondaryActionDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link EditorActionAreaDefinition}.
 */
public class ConfiguredEditorActionAreaDefinition extends ConfiguredActionAreaDefinition implements EditorActionAreaDefinition {

    private List<SecondaryActionDefinition> secondaryActions = new ArrayList<SecondaryActionDefinition>();

    public ConfiguredEditorActionAreaDefinition() {
        setPresenterClass(EditorActionAreaPresenter.class);
    }

    @Override
    public List<SecondaryActionDefinition> getSecondaryActions() {
        return secondaryActions;
    }

    public void setSecondaryActions(List<SecondaryActionDefinition> secondaryActions) {
        this.secondaryActions = secondaryActions;
    }

    @Override
    public Class<? extends EditorActionAreaPresenter> getPresenterClass() {
        return (Class<? extends EditorActionAreaPresenter>) super.getPresenterClass();
    }
}
