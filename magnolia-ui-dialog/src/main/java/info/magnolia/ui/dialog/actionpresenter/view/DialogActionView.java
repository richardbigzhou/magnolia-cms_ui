package info.magnolia.ui.dialog.actionpresenter.view;

import info.magnolia.ui.api.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 12:37 AM
 * To change this template use File | Settings | File Templates.
 */
public interface DialogActionView extends View {

    void addPrimaryAction(View actionView);

    void addSecondaryAction(View actionView);

    void removeAllActions();
}
