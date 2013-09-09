package info.magnolia.ui.contentapp.movedialog.predicate;

import com.vaadin.data.Item;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/8/13
 * Time: 2:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class MoveBeforePossibilityPredicate extends MovePossibilityPredicate {

    public MoveBeforePossibilityPredicate(DropConstraint constraint, List<? extends Item> candidates) {
        super(constraint, candidates);
    }

    @Override
    protected boolean checkItem(Item item, Item hostCandidate) {
        if (hostCandidate instanceof JcrItemAdapter) {
            JcrItemAdapter jcrItem = (JcrItemAdapter) hostCandidate;
            try {
                if (jcrItem.getJcrItem().getParent() == null) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return constraint.allowedBefore(item, hostCandidate);
    }
}
