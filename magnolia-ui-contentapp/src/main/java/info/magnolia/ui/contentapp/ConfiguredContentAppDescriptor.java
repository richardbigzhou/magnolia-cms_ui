package info.magnolia.ui.contentapp;

import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/1/13
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfiguredContentAppDescriptor extends ConfiguredAppDescriptor implements ContentAppDescriptor {

    private ChooseDialogDefinition chooseDialog = new ConfiguredChooseDialogDefinition();

    @Override
    public ChooseDialogDefinition getChooseDialog() {
        return chooseDialog;
    }

    public void setChooseDialog(ChooseDialogDefinition chooseDialog) {
        this.chooseDialog = chooseDialog;
    }
}
