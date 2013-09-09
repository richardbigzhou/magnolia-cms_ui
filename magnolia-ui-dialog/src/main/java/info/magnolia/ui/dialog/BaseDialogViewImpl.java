package info.magnolia.ui.dialog;

import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionpresenter.view.DialogActionView;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent.Handler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/3/13
 * Time: 11:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class BaseDialogViewImpl extends Panel implements DialogView {

    private BaseDialog dialog;

    private Set<DialogCloseHandler> dialogCloseHandlers = new HashSet<DialogCloseHandler>();

    private View contentView;

    private DialogActionView actionView;

    public BaseDialogViewImpl() {
        this(new BaseDialog());
    }

    public BaseDialogViewImpl(BaseDialog dialog) {
        this.dialog = dialog;

        setContent(this.dialog);
        // We use Panel to keep keystroke events scoped within the currently focused component.
        // Without it, if you have more than one dialog open,
        // i.e. in different apps running at the same time, then all open
        // dialogs would react to the keyboard event sent on the dialog currently having the focus.
        //setWidth(Sizeable.SIZE_UNDEFINED, Unit.PIXELS);
        setWidth("720px");
        setHeight(100, Unit.PERCENTAGE); // Required for dynamic dialog shrinking upon window resize.
        this.dialog.addDialogCloseHandler(new Handler() {
            @Override
            public void onClose(DialogCloseEvent event) {
                close();
            }
        });
        this.dialog.setSizeFull();
        this.dialog.setStyleName("dialog-panel");
    }

    @Override
    public void setDescriptionVisible(boolean isDescriptionVisible) {
        dialog.setDescriptionVisibility(isDescriptionVisible);
    }

    @Override
    public void setDescription(String description) {
        dialog.setDialogDescription(description);
    }

    @Override
    public void setCaption(String caption) {
        dialog.setCaption(caption);
    }

    @Override
    public void setContent(View content) {
        this.contentView = content;
        dialog.setContent(content.asVaadinComponent());
    }

    @Override
    public void close() {
        DialogCloseHandler[] handlers = dialogCloseHandlers.toArray(new DialogCloseHandler[dialogCloseHandlers.size()]);
        for (final DialogCloseHandler handler : handlers) {
            handler.onDialogClose(BaseDialogViewImpl.this);
        }
        dialogCloseHandlers.clear();
    }

    @Override
    public void setClosable(boolean isClosable) {
        if (isClosable) {
            dialog.showCloseButton();
        }
    }

    @Override
    public void addShortcut(ShortcutListener shortcut) {
        addShortcutListener(shortcut);
    }

    @Override
    public void removeShortcut(ShortcutListener shortcut) {
        removeShortcutListener(shortcut);
    }

    @Override
    public void addDialogCloseHandler(DialogCloseHandler handler) {
        if (handler != null) {
            dialogCloseHandlers.add(handler);
        }
    }

    @Override
    public void removeDialogCloseHandler(DialogCloseHandler handler) {
        dialogCloseHandlers.remove(handler);
    }

    @Override
    public void setActionView(DialogActionView actionView) {
        this.actionView = actionView;
        dialog.setFooterToolbar(actionView.asVaadinComponent());
    }

    @Override
    public void attach() {
        super.attach();
        focus();
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public View getContentView() {
        return contentView;
    }

    @Override
    public DialogActionView getActionView() {
        return actionView;
    }

    protected BaseDialog getDialog() {
        return this.dialog;
    }
}
