/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
package info.magnolia.ui.contentapp.movedialog;

import com.rits.cloning.Cloner;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import info.magnolia.event.EventBus;
import info.magnolia.event.ResettableEventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.field.WorkbenchField;
import info.magnolia.ui.contentapp.movedialog.action.MoveNodeActionDefinition;
import info.magnolia.ui.contentapp.movedialog.predicate.MoveAfterPossibilityPredicate;
import info.magnolia.ui.contentapp.movedialog.predicate.MoveBeforePossibilityPredicate;
import info.magnolia.ui.contentapp.movedialog.predicate.MoveInsidePossibilityPredicate;
import info.magnolia.ui.contentapp.movedialog.predicate.MovePossibilityPredicate;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.dialog.DialogCloseHandler;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.actionarea.definition.ConfiguredEditorActionAreaDefinition;
import info.magnolia.ui.dialog.definition.BaseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredBaseDialogDefinition;
import info.magnolia.ui.dialog.definition.SecondaryActionDefinition;
import info.magnolia.ui.framework.action.MoveLocation;
import info.magnolia.ui.framework.overlay.ViewAdapter;
import info.magnolia.ui.imageprovider.definition.ConfiguredImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Implementation of {@link MoveDialogPresenter}.
 */
public class MoveDialogPresenterImpl extends BaseDialogPresenter implements MoveDialogPresenter {

    private DialogView dialogView;

    private EventBus eventBus = new ResettableEventBus(new SimpleEventBus());

    private WorkbenchPresenter workbenchPresenter;

    private UiContext uiContext;

    private List<JcrNodeAdapter> nodesToMove;

    private Map<MoveLocation, ActionDefinition> actionMap = new HashMap<MoveLocation, ActionDefinition>();

    private Map<MoveLocation, MovePossibilityPredicate> possibilityPredicates = new HashMap<MoveLocation, MovePossibilityPredicate>();

    private DropConstraint constraint;

    private MoveActionCallback callback;

    private JcrNodeAdapter currentHostCandidate;

    @Inject
    public MoveDialogPresenterImpl(ComponentProvider componentProvider, DialogView dialogView, WorkbenchPresenter workbenchPresenter, UiContext uiContext) {
        super(componentProvider);
        this.dialogView = dialogView;
        this.workbenchPresenter = workbenchPresenter;
        this.uiContext = uiContext;
    }

    @Override
    protected DialogView initView() {
        dialogView.asVaadinComponent().setStyleName("choose-dialog");
        return dialogView;
    }

    @Override
    public Object[] getActionParameters(String actionName) {
        return new Object[]{nodesToMove, callback, currentHostCandidate};
    }

    @Override
    public DialogView start(BrowserSubAppDescriptor subAppDescriptor, List<JcrNodeAdapter> nodesToMove, MoveActionCallback callback) {
        final ConfiguredWorkbenchDefinition workbenchDefinition =
            (ConfiguredWorkbenchDefinition) new Cloner().deepClone(subAppDescriptor.getWorkbench());

        ConfiguredImageProviderDefinition imageProviderDefinition =
            (ConfiguredImageProviderDefinition) new Cloner().deepClone(subAppDescriptor.getImageProvider());

        workbenchDefinition.setIncludeProperties(false);
        workbenchDefinition.setDialogWorkbench(true);

        this.nodesToMove = nodesToMove;
        this.constraint = componentProvider.newInstance(workbenchDefinition.getDropConstraintClass());
        this.callback = callback;

        initActions();
        initMovePossibilityPredicates();

        final WorkbenchField field = new WorkbenchField(
                workbenchDefinition,
                imageProviderDefinition,
                workbenchPresenter,
                eventBus);

        dialogView.setContent(new ViewAdapter(field));
        field.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                currentHostCandidate = (event.getProperty().getValue() == null) ? null: (JcrNodeAdapter) event.getProperty().getValue();
                updatePossibleMoveLocations(currentHostCandidate);

            }
        });

        BaseDialogDefinition dialogDefinition = prepareDialogDefinition();
        dialogView.setCaption(dialogDefinition.getLabel());
        dialogView.addDialogCloseHandler(new DialogCloseHandler() {
            @Override
            public void onDialogClose(DialogView dialogView) {
                ((ResettableEventBus)eventBus).reset();
            }
        });
        super.start(dialogDefinition, uiContext);
        updatePossibleMoveLocations(null);
        return dialogView;
    }

    private void initMovePossibilityPredicates() {
        possibilityPredicates.put(MoveLocation.AFTER, new MoveAfterPossibilityPredicate(constraint, nodesToMove));
        possibilityPredicates.put(MoveLocation.BEFORE, new MoveBeforePossibilityPredicate(constraint, nodesToMove));
        possibilityPredicates.put(MoveLocation.INSIDE, new MoveInsidePossibilityPredicate(constraint, nodesToMove));

    }

    private void updatePossibleMoveLocations(Item possibleHost) {
        Set<MoveLocation> possibleLocations = new HashSet<MoveLocation>();
        if (possibleHost != null) {
            Iterator<Entry<MoveLocation, MovePossibilityPredicate>> it = possibilityPredicates.entrySet().iterator();
            while (it.hasNext()) {
                Entry<MoveLocation, MovePossibilityPredicate> entry = it.next();
                if (entry.getValue().isMovePossible(possibleHost)) {
                    possibleLocations.add(entry.getKey());
                }
            }
        }
        getActionPresenter().setPossibleMoveLocations(possibleLocations);
    }

    private void initActions() {
        for (MoveLocation location : MoveLocation.values()) {
            ConfiguredActionDefinition definition = new MoveNodeActionDefinition(location);
            definition.setName(location.name());
            definition.setLabel(location.name());
            actionMap.put(location, definition);
        }
    }

    private BaseDialogDefinition prepareDialogDefinition() {
        ConfiguredBaseDialogDefinition def = new ConfiguredBaseDialogDefinition();
        def.setLabel("Move");
        def.setId("move:dialog");
        for (MoveLocation location : MoveLocation.values()) {
            def.addAction(actionMap.get(location));
        }

        ConfiguredEditorActionAreaDefinition actionPresenterDefinition = new ConfiguredEditorActionAreaDefinition();
        actionPresenterDefinition.setPresenterClass(MoveDialogActionPresenter.class);

        List<SecondaryActionDefinition> secondaryActions = new LinkedList<SecondaryActionDefinition>();
        secondaryActions.add(new SecondaryActionDefinition(MoveLocation.BEFORE.name()));
        secondaryActions.add(new SecondaryActionDefinition(MoveLocation.AFTER.name()));
        actionPresenterDefinition.setSecondaryActions(secondaryActions);

        def.setActionArea(actionPresenterDefinition);
        return def;
    }

    @Override
    public MoveDialogActionPresenter getActionPresenter() {
        return (MoveDialogActionPresenter) super.getActionPresenter();
    }
}
