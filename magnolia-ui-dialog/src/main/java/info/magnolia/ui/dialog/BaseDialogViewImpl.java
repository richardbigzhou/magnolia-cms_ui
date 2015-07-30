/**
 * This file Copyright (c) 2010-2015 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.dialog;

import info.magnolia.ui.api.overlay.OverlayLayer.ModalityLevel;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaView;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent.Handler;
import info.magnolia.ui.vaadin.dialog.BaseDialog.WideEvent;

import java.util.LinkedHashSet;
import java.util.Set;

import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

/**
 * Base implementation of {@link DialogView}.
 */
public class BaseDialogViewImpl extends Panel implements DialogView {

    public static final int DIALOG_WIDTH = 720;

    private BaseDialog dialog;

    private Set<DialogCloseHandler> dialogCloseHandlers = new LinkedHashSet<DialogCloseHandler>();

    private View contentView;

    private EditorActionAreaView actionAreaView;

    private ModalityLevel modalityLevel = ModalityLevel.STRONG;

    private boolean isClosed = false;

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

        setWidth(DIALOG_WIDTH, Unit.PIXELS);
        setHeight(100, Unit.PERCENTAGE); // Required for dynamic dialog shrinking upon window resize.

        this.dialog.addDialogCloseHandler(new Handler() {
            @Override
            public void onClose(DialogCloseEvent event) {
                close();
            }
        });

        this.dialog.addWideHandler(new BaseDialog.WideEvent.Handler() {

            @Override
            public void onWideChanged(WideEvent event) {
                handleSetWide(event.isWide());
            }
        });

        this.dialog.setSizeFull();
        this.dialog.setStyleName("dialog-panel");
    }

    private void handleSetWide(boolean isWide){
        if (isWide){
            setWidth(100, Unit.PERCENTAGE);
            addStyleName("wide");
        }else{
            setWidth(DIALOG_WIDTH, Unit.PIXELS);
            removeStyleName("wide");
        }
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
        // Some handler could call #close() method once again, but the handlers should be called only once -
        // all the subsequent calls should be avoided.
        if (!isClosed) {
            isClosed = true;
            for (final DialogCloseHandler handler : dialogCloseHandlers) {
                handler.onDialogClose(BaseDialogViewImpl.this);
            }
            dialogCloseHandlers.clear();
        }
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
    public void setActionAreaView(EditorActionAreaView actionAreaView) {
        this.actionAreaView = actionAreaView;
        dialog.setFooterToolbar(actionAreaView.asVaadinComponent());
    }

    @Override
    public void attach() {
        super.attach();
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
    public EditorActionAreaView getActionAreaView() {
        return actionAreaView;
    }

    protected BaseDialog getDialog() {
        return this.dialog;
    }

    /**
     * @return the current modality or {@link ModalityLevel#STRONG} if not set.
     */
    @Override
    public ModalityLevel getModalityLevel() {
        return this.modalityLevel;
    }

    @Override
    public void setModalityLevel(ModalityLevel modalityLevel) {
        this.modalityLevel = modalityLevel;
        if (modalityLevel == ModalityLevel.LIGHT) {
            dialog.removeStyleName("dialog-panel");
            dialog.addStyleName("light");
            setWidth("620px");
        }
        dialog.setModalityLevel(modalityLevel.getName());
    }

    @Override
    public void setWide(boolean isWide) {
        handleSetWide(isWide);
        dialog.setWideOnClient(isWide);
    }
}
