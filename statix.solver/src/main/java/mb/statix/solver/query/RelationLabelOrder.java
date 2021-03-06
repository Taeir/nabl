package mb.statix.solver.query;

import mb.nabl2.relations.IRelation;
import mb.nabl2.terms.ITerm;
import mb.statix.scopegraph.reference.LabelOrder;
import mb.statix.scopegraph.reference.ResolutionException;

public class RelationLabelOrder implements LabelOrder<ITerm> {

    private final IRelation<ITerm> labelOrd;

    public RelationLabelOrder(IRelation<ITerm> labelOrd) {
        this.labelOrd = labelOrd;
    }

    @Override public boolean lt(ITerm l1, ITerm l2) throws ResolutionException, InterruptedException {
        return labelOrd.contains(l1, l2);
    }

}