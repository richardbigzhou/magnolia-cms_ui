package info.magnolia.ui.framework.overlay.confirmationdialog;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.LightDialog;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 5:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfirmationDialogView extends Panel implements View {

    private BaseDialog dialog = new LightDialog();

    private Button confirmButton = new Button();

    private Button cancelButton = new Button();

    public ConfirmationDialogView(final ConfirmationCallback callback) {
        HorizontalLayout footer = new HorizontalLayout();
        footer.addComponent(confirmButton);
        footer.addComponent(cancelButton);
        footer.setComponentAlignment(confirmButton, Alignment.MIDDLE_RIGHT);
        footer.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);
        dialog.setFooterToolbar(footer);
        confirmButton.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                callback.onSuccess();
            }
        });

        cancelButton.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                callback.onCancel();
            }
        });
    }


    @Override
    public Component asVaadinComponent() {
        return this;
    }
}
