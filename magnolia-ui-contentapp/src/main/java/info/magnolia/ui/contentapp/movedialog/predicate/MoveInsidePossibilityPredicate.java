package info.magnolia.ui.contentapp.movedialog.predicate;

import com.vaadin.data.Item;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/8/13
 * Time: 2:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class MoveInsidePossibilityPredicate extends MoveAfterPossibilityPredicate {

    public MoveInsidePossibilityPredicate(DropConstraint constraint, List<? extends Item> candidates) {
        super(constraint, candidates);
    }

    @Override
    protected boolean checkItem(Item item, Item hostCandidate) {
        return constraint.allowedAsChild(item, hostCandidate);
    }
}
