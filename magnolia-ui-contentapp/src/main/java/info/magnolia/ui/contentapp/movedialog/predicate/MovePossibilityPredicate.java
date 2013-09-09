package info.magnolia.ui.contentapp.movedialog.predicate;


import com.vaadin.data.Item;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/8/13
 * Time: 2:36 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MovePossibilityPredicate {

    private List<? extends Item> candidates;

    protected DropConstraint constraint;

    protected MovePossibilityPredicate(DropConstraint constraint, List<? extends Item> candidates) {
        this.constraint = constraint;
        this.candidates = candidates;
    }

    public boolean isMovePossible(Item hostCandidate) {
        boolean isPossible = true;
        Iterator<? extends Item> it = candidates.iterator();
        while (it.hasNext() && isPossible) {
            Item item = it.next();
            isPossible &= checkItem(item, hostCandidate);
        }
        return isPossible;
    }

    protected abstract boolean checkItem(Item item, Item hostCandidate);
}
