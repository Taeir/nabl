package mb.nabl2.terms.unification;

import static mb.nabl2.terms.build.TermBuild.B;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.metaborg.util.Ref;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.ListTerms;
import mb.nabl2.terms.Terms;
import mb.nabl2.terms.matching.MaybeNotInstantiatedBool;
import mb.nabl2.util.Set2;

public abstract class BaseUnifier implements IUnifier, Serializable {

    private static final long serialVersionUID = 42L;

    protected abstract java.util.Map<ITermVar, ITermVar> reps();

    protected abstract java.util.Map<ITermVar, ITerm> terms();

    ///////////////////////////////////////////
    // unifier functions
    ///////////////////////////////////////////

    @Override public boolean isEmpty() {
        return reps().isEmpty() && terms().isEmpty();
    }

    @Override public int size() {
        return reps().size() + terms().size();
    }

    @Override public boolean contains(ITermVar var) {
        return reps().containsKey(var) || terms().containsKey(var);
    }

    @Override public Set<ITermVar> repSet() {
        return ImmutableSet.copyOf(reps().values());
    }

    @Override public Set<ITermVar> varSet() {
        return Sets.union(reps().keySet(), terms().keySet());
    }

    @Override public Set<ITermVar> freeVarSet() {
        final Set<ITermVar> freeVars = Sets.newHashSet();
        reps().values().stream().filter(var -> !contains(var)).forEach(freeVars::add);
        terms().values().stream().flatMap(term -> term.getVars().elementSet().stream()).filter(var -> !contains(var))
                .forEach(freeVars::add);
        return freeVars;
    }

    @Override public boolean isCyclic() {
        return isCyclic(varSet());
    }

    ///////////////////////////////////////////
    // equals
    ///////////////////////////////////////////

    @Override public boolean equals(Object other) {
        if(other == null) {
            return false;
        }
        if(other == this) {
            return true;
        }
        if(!(other instanceof IUnifier)) {
            return false;
        }
        final IUnifier that = (IUnifier) other;
        return equals(that);
    }

    public boolean equals(IUnifier other) {
        return new Equals(other).apply();
    }

    private class Equals {

        public final IUnifier other;
        public final BiMap<ITermVar, ITermVar> freeMap = HashBiMap.create();
        public final Multimap<ITermVar, ITermVar> instMap = HashMultimap.create();
        public final Deque<ITermVar> vars = Lists.newLinkedList();

        public Equals(IUnifier other) {
            this.other = other;
        }

        public boolean apply() {
            if(isFinite() != other.isFinite()) {
                return false;
            } else {
                vars.addAll(varSet());
                vars.addAll(other.varSet());
                while(!vars.isEmpty()) {
                    final ITermVar var = vars.pop();
                    if(!equalVars(var, var)) {
                        return false;
                    }
                }
                return true;
            }
        }

        private boolean equalTerms(ITerm thisTerm, ITerm thatTerm) {
            // @formatter:off
            return thisTerm.match(Terms.cases(
                applThis -> thatTerm.match(Terms.<Boolean>cases()
                    .appl(applThat -> applThis.getArity() == applThat.getArity() &&
                                       applThis.getOp().equals(applThat.getOp()) &&
                                       equals(applThis.getArgs(), applThat.getArgs()))
                    .var(varThat -> equalTermVar(applThis, varThat))
                    .otherwise(t -> false)
                ),
                listThis -> thatTerm.match(Terms.<Boolean>cases()
                    .list(listThat -> listThis.match(ListTerms.cases(
                        consThis -> listThat.match(ListTerms.<Boolean>cases()
                            .cons(consThat -> {
                                return equalTerms(consThis.getHead(), consThat.getHead()) &&
                                equalTerms(consThis.getTail(), consThat.getTail());
                            })
                            .var(varThat -> equalTermVar(consThis, varThat))
                            .otherwise(l -> false)
                        ),
                        nilThis -> listThat.match(ListTerms.<Boolean>cases()
                            .nil(nilThat -> true)
                            .var(varThat -> equalTermVar(nilThis, varThat))
                            .otherwise(l -> false)
                        ),
                        varThis -> listThat.match(ListTerms.<Boolean>cases()
                            .var(varThat -> equalVars(varThis, varThat))
                            .otherwise(termThat -> equalVarTerm(varThis, termThat))
                        )
                    )))
                    .var(varThat -> equalTermVar(listThis, varThat))
                    .otherwise(t -> false)
                ),
                stringThis -> thatTerm.match(Terms.<Boolean>cases()
                    .string(stringThat -> stringThis.getValue().equals(stringThat.getValue()))
                    .var(varThat -> equalTermVar(stringThis, varThat))
                    .otherwise(t -> false)
                ),
                integerThis -> thatTerm.match(Terms.<Boolean>cases()
                    .integer(integerThat -> integerThis.getValue() == integerThat.getValue())
                    .var(varThat -> equalTermVar(integerThis, varThat))
                    .otherwise(t -> false)
                ),
                blobThis -> thatTerm.match(Terms.<Boolean>cases()
                    .blob(blobThat -> blobThis.getValue().equals(blobThat.getValue()))
                    .var(varThat -> equalTermVar(blobThis, varThat))
                    .otherwise(t -> false)
                ),
                varThis -> thatTerm.match(Terms.<Boolean>cases()
                    // match var before term, or term will always match
                    .var(varThat -> equalVars(varThis, varThat))
                    .otherwise(termThat -> equalVarTerm(varThis, termThat))
                )
            ));
            // @formatter:on
        }

        private boolean equalVarTerm(final ITermVar thisVar, final ITerm thatTerm) {
            if(hasTerm(thisVar)) {
                return equalTerms(findTerm(thisVar), thatTerm);
            }
            return false;
        }

        private boolean equalTermVar(final ITerm thisTerm, final ITermVar thatVar) {
            if(other.hasTerm(thatVar)) {
                return equalTerms(thisTerm, other.findTerm(thatVar));
            }
            return false;
        }

        private boolean equalVars(ITermVar thisVar, ITermVar thatVar) {
            final ITermVar thisRep = findRep(thisVar);
            final ITermVar thatRep = other.findRep(thatVar);
            final boolean result;
            if(hasTerm(thisRep) && other.hasTerm(thatRep)) {
                if(instMap.containsEntry(thisRep, thatRep)) {
                    result = true;
                } else {
                    instMap.put(thisRep, thatRep);
                    vars.addAll(Arrays.asList(thisVar, thisRep, thatVar, thatRep));
                    result = equalTerms(findTerm(thisRep), other.findTerm(thatRep));
                }
            } else if(!hasTerm(thisRep) && !other.hasTerm(thatRep)) {
                if(freeMap.containsKey(thisRep) && freeMap.containsValue(thatRep)) {
                    result = freeMap.get(thisRep).equals(thatRep);
                } else if(freeMap.containsKey(thisRep) || freeMap.containsValue(thatRep)) {
                    result = false;
                } else {
                    freeMap.put(thisRep, thatRep);
                    vars.addAll(Arrays.asList(thisVar, thisRep, thatVar, thatRep));
                    result = true;
                }
            } else {
                result = false;
            }
            return result;
        }

        private boolean equals(final Iterable<ITerm> thisTerms, final Iterable<ITerm> thatTerms) {
            Iterator<ITerm> itLeft = thisTerms.iterator();
            Iterator<ITerm> itRight = thatTerms.iterator();
            while(itLeft.hasNext()) {
                if(!itRight.hasNext()) {
                    return false;
                }
                if(!equalTerms(itLeft.next(), itRight.next())) {
                    return false;
                }
            }
            if(itRight.hasNext()) {
                return false;
            }
            return true;
        }

    }

    @Override public int hashCode() {
        return Objects.hash(isFinite(), reps(), terms()); // FIXME: not exactly equivalent to equals implementation
    }

    ///////////////////////////////////////////
    // toString
    ///////////////////////////////////////////

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for(ITermVar var : terms().keySet()) {
            sb.append(first ? " " : ", ");
            first = false;
            sb.append(var);
            sb.append(" |-> ");
            sb.append(terms().get(var));
        }
        for(ITermVar var : reps().keySet()) {
            sb.append(first ? " " : ", ");
            first = false;
            sb.append(var);
            sb.append(" |-> ");
            sb.append(reps().get(var));
        }
        sb.append(first ? "}" : " }");
        return sb.toString();
    }

    ///////////////////////////////////////////
    // findTerm(ITerm) / findRep(ITerm)
    ///////////////////////////////////////////

    @Override public boolean hasTerm(ITermVar var) {
        return terms().containsKey(findRep(var));
    }

    @Override public ITerm findTerm(ITerm term) {
        return term.match(Terms.<ITerm>cases().var(var -> {
            final ITermVar rep = findRep(var);
            return terms().getOrDefault(rep, rep);
        }).otherwise(t -> t));
    }

    ///////////////////////////////////////////
    // findRecursive(ITerm)
    ///////////////////////////////////////////

    @Override public ITerm findRecursive(final ITerm term) {
        return findTermRecursive(term, Sets.newHashSet(), Maps.newHashMap());
    }

    private ITerm findTermRecursive(final ITerm term, final Set<ITermVar> stack,
            final java.util.Map<ITermVar, ITerm> visited) {
        return term.match(Terms.cases(
        // @formatter:off
            appl -> B.newAppl(appl.getOp(), findRecursiveTerms(appl.getArgs(), stack, visited), appl.getAttachments()),
            list -> findListTermRecursive(list, stack, visited),
            string -> string,
            integer -> integer,
            blob -> blob,
            var -> findVarRecursive(var, stack, visited)
            // @formatter:on
        ));
    }

    private IListTerm findListTermRecursive(IListTerm list, final Set<ITermVar> stack,
            final java.util.Map<ITermVar, ITerm> visited) {
        Deque<IListTerm> elements = Lists.newLinkedList();
        while(list != null) {
            list = list.match(ListTerms.cases(
            // @formatter:off
                cons -> {
                    elements.push(cons);
                    return cons.getTail();
                },
                nil -> {
                    elements.push(nil);
                    return null;
                },
                var -> {
                    elements.push(var);
                    return null;
                }
                // @formatter:on
            ));
        }
        Ref<IListTerm> instance = new Ref<>();
        while(!elements.isEmpty()) {
            instance.set(elements.pop().match(ListTerms.<IListTerm>cases(
            // @formatter:off
                cons -> B.newCons(findTermRecursive(cons.getHead(), stack, visited), instance.get(), cons.getAttachments()),
                nil -> nil,
                var -> (IListTerm) findVarRecursive(var, stack, visited)
                // @formatter:on
            )));
        }
        return instance.get();
    }

    private ITerm findVarRecursive(final ITermVar var, final Set<ITermVar> stack,
            final java.util.Map<ITermVar, ITerm> visited) {
        final ITermVar rep = findRep(var);
        final ITerm instance;
        if(!visited.containsKey(rep)) {
            stack.add(rep);
            visited.put(rep, null);
            final ITerm term = terms().get(rep);
            instance = term != null ? findTermRecursive(term, stack, visited) : rep;
            visited.put(rep, instance);
            stack.remove(rep);
            return instance;
        } else if(stack.contains(rep)) {
            throw new IllegalArgumentException("Recursive terms cannot be fully instantiated.");
        } else {
            instance = visited.get(rep);
        }
        return instance;
    }

    private Iterable<ITerm> findRecursiveTerms(final Iterable<ITerm> terms, final Set<ITermVar> stack,
            final java.util.Map<ITermVar, ITerm> visited) {
        List<ITerm> instances = Lists.newArrayList();
        for(ITerm term : terms) {
            instances.add(findTermRecursive(term, stack, visited));
        }
        return instances;
    }

    ///////////////////////////////////////////
    // areEqual(ITerm, ITerm)
    ///////////////////////////////////////////

    @Override public MaybeNotInstantiatedBool areEqual(final ITerm left, final ITerm right) {
        return equalTerms(left, right, Sets.newHashSet(), Maps.newHashMap());
    }

    private MaybeNotInstantiatedBool equalTerms(final ITerm left, final ITerm right, final Set<Set2<ITermVar>> stack,
            final java.util.Map<Set2<ITermVar>, Boolean> visited) {
        // @formatter:off
        return left.match(Terms.<MaybeNotInstantiatedBool>cases(
            applLeft -> right.match(Terms.<MaybeNotInstantiatedBool>cases()
                .appl(applRight -> {
                    if(applLeft.getArity() == applRight.getArity() &&
                            applLeft.getOp().equals(applRight.getOp())) {
                        return equals(applLeft.getArgs(), applRight.getArgs(), stack, visited);
                    } else {
                        return MaybeNotInstantiatedBool.ofResult(false);
                    }
                 })
                .var(varRight -> equalVarTerm(varRight, applLeft, stack, visited))
                .otherwise(t -> MaybeNotInstantiatedBool.ofResult(false))
            ),
            listLeft -> right.match(Terms.<MaybeNotInstantiatedBool>cases()
                .list(listRight -> listLeft.match(ListTerms.<MaybeNotInstantiatedBool>cases(
                    consLeft -> listRight.match(ListTerms.<MaybeNotInstantiatedBool>cases()
                        .cons(consRight -> {
                            return equalTerms(consLeft.getHead(), consRight.getHead(), stack, visited).flatMap(m -> {
                                if(m) {
                                    return equalTerms(consLeft.getTail(), consRight.getTail(), stack, visited);
                                } else {
                                    return MaybeNotInstantiatedBool.ofResult(false);
                                }
                            });
                        })
                        .var(varRight -> equalVarTerm(varRight, consLeft, stack, visited))
                        .otherwise(l -> MaybeNotInstantiatedBool.ofResult(false))
                    ),
                    nilLeft -> listRight.match(ListTerms.<MaybeNotInstantiatedBool>cases()
                        .nil(nilRight -> MaybeNotInstantiatedBool.ofResult(true))
                        .var(varRight -> equalVarTerm(varRight, nilLeft, stack, visited))
                        .otherwise(l -> MaybeNotInstantiatedBool.ofResult(false))
                    ),
                    varLeft -> listRight.match(ListTerms.<MaybeNotInstantiatedBool>cases()
                        .var(varRight -> equalVars(varLeft, varRight, stack, visited))
                        .otherwise(termRight -> equalVarTerm(varLeft, termRight, stack, visited))
                    )
                )))
                .var(varRight -> equalVarTerm(varRight, listLeft, stack, visited))
                .otherwise(t -> MaybeNotInstantiatedBool.ofResult(false))
            ),
            stringLeft -> right.match(Terms.<MaybeNotInstantiatedBool>cases()
                .string(stringRight -> MaybeNotInstantiatedBool.ofResult(stringLeft.getValue().equals(stringRight.getValue())))
                .var(varRight -> equalVarTerm(varRight, stringLeft, stack, visited))
                .otherwise(t -> MaybeNotInstantiatedBool.ofResult(false))
            ),
            integerLeft -> right.match(Terms.<MaybeNotInstantiatedBool>cases()
                .integer(integerRight -> MaybeNotInstantiatedBool.ofResult(integerLeft.getValue() == integerRight.getValue()))
                .var(varRight -> equalVarTerm(varRight, integerLeft, stack, visited))
                .otherwise(t -> MaybeNotInstantiatedBool.ofResult(false))
            ),
            blobLeft -> right.match(Terms.<MaybeNotInstantiatedBool>cases()
                .blob(blobRight -> MaybeNotInstantiatedBool.ofResult(blobLeft.getValue().equals(blobRight.getValue())))
                .var(varRight -> equalVarTerm(varRight, blobLeft, stack, visited))
                .otherwise(t -> MaybeNotInstantiatedBool.ofResult(false))
            ),
            varLeft -> right.match(Terms.<MaybeNotInstantiatedBool>cases()
                // match var before term, or term will always match
                .var(varRight -> equalVars(varLeft, varRight, stack, visited))
                .otherwise(termRight -> equalVarTerm(varLeft, termRight, stack, visited))
            )
        ));
        // @formatter:on
    }

    private MaybeNotInstantiatedBool equalVarTerm(final ITermVar var, final ITerm term, final Set<Set2<ITermVar>> stack,
            final java.util.Map<Set2<ITermVar>, Boolean> visited) {
        final ITermVar rep = findRep(var);
        if(terms().containsKey(rep)) {
            return equalTerms(terms().get(rep), term, stack, visited);
        } else {
            return MaybeNotInstantiatedBool.ofNotInstantiated(rep);
        }
    }

    private MaybeNotInstantiatedBool equalVars(final ITermVar left, final ITermVar right,
            final Set<Set2<ITermVar>> stack, final java.util.Map<Set2<ITermVar>, Boolean> visited) {
        final ITermVar leftRep = findRep(left);
        final ITermVar rightRep = findRep(right);
        if(leftRep.equals(rightRep)) {
            return MaybeNotInstantiatedBool.ofResult(true);
        }
        final Set2<ITermVar> pair = Set2.of(leftRep, rightRep);
        final MaybeNotInstantiatedBool equal;
        if(!visited.containsKey(pair)) {
            stack.add(pair);
            visited.put(pair, null);
            final ITerm leftTerm = terms().get(leftRep);
            final ITerm rightTerm = terms().get(rightRep);
            if(leftTerm == null && rightTerm == null) {
                return MaybeNotInstantiatedBool.ofNotInstantiated(leftRep, rightRep);
            } else if(leftTerm == null) {
                return MaybeNotInstantiatedBool.ofNotInstantiated(leftRep);
            } else if(rightTerm == null) {
                return MaybeNotInstantiatedBool.ofNotInstantiated(rightRep);
            }
            equal = equalTerms(leftTerm, rightTerm, stack, visited);
            equal.onResult(eq -> {
                visited.put(pair, eq);
            });
            stack.remove(pair);
        } else if(stack.contains(pair)) {
            equal = MaybeNotInstantiatedBool.ofResult(false);
        } else {
            equal = MaybeNotInstantiatedBool.ofResult(visited.get(pair));
        }
        return equal;
    }

    private MaybeNotInstantiatedBool equals(final Iterable<ITerm> lefts, final Iterable<ITerm> rights,
            final Set<Set2<ITermVar>> stack, final java.util.Map<Set2<ITermVar>, Boolean> visited) {
        Iterator<ITerm> itLeft = lefts.iterator();
        Iterator<ITerm> itRight = rights.iterator();
        while(itLeft.hasNext()) {
            if(!itRight.hasNext()) {
                return MaybeNotInstantiatedBool.ofResult(false);
            }
            final MaybeNotInstantiatedBool result = equalTerms(itLeft.next(), itRight.next(), stack, visited);
            if(!result.orElse(false)) {
                return result;
            }
        }
        if(itRight.hasNext()) {
            return MaybeNotInstantiatedBool.ofResult(false);
        }
        return MaybeNotInstantiatedBool.ofResult(true);
    }

    ///////////////////////////////////////////
    // isCyclic(ITerm)
    ///////////////////////////////////////////

    @Override public boolean isCyclic(final ITerm term) {
        return isCyclic(term.getVars().elementSet(), Sets.newHashSet(), Maps.newHashMap());
    }

    protected boolean isCyclic(final Set<ITermVar> vars) {
        return isCyclic(vars, Sets.newHashSet(), Maps.newHashMap());
    }

    private boolean isCyclic(final Set<ITermVar> vars, final Set<ITermVar> stack,
            final java.util.Map<ITermVar, Boolean> visited) {
        return vars.stream().anyMatch(var -> isCyclic(var, stack, visited));
    }

    private boolean isCyclic(final ITermVar var, final Set<ITermVar> stack,
            final java.util.Map<ITermVar, Boolean> visited) {
        final boolean cyclic;
        final ITermVar rep = findRep(var);
        if(!visited.containsKey(rep)) {
            stack.add(rep);
            visited.put(rep, null);
            final ITerm term = terms().get(rep);
            cyclic = term != null ? isCyclic(term.getVars().elementSet(), stack, visited) : false;
            visited.put(rep, cyclic);
            stack.remove(rep);
        } else if(stack.contains(rep)) {
            cyclic = true;
        } else {
            cyclic = visited.get(rep);
        }
        return cyclic;
    }

    ///////////////////////////////////////////
    // isGround(ITerm)
    ///////////////////////////////////////////

    @Override public boolean isGround(final ITerm term) {
        return isGround(term.getVars().elementSet(), Sets.newHashSet(), Maps.newHashMap());
    }

    private boolean isGround(final Set<ITermVar> vars, final Set<ITermVar> stack,
            final java.util.Map<ITermVar, Boolean> visited) {
        return vars.stream().allMatch(var -> isGround(var, stack, visited));
    }

    private boolean isGround(final ITermVar var, final Set<ITermVar> stack,
            final java.util.Map<ITermVar, Boolean> visited) {
        final boolean ground;
        final ITermVar rep = findRep(var);
        if(!visited.containsKey(rep)) {
            stack.add(rep);
            visited.put(rep, null);
            final ITerm term = terms().get(rep);
            ground = term != null ? isGround(term.getVars().elementSet(), stack, visited) : false;
            visited.put(rep, ground);
            stack.remove(rep);
        } else if(stack.contains(rep)) {
            ground = true;
        } else {
            ground = visited.get(rep);
        }
        return ground;
    }

    ///////////////////////////////////////////
    // getVars(ITerm)
    ///////////////////////////////////////////

    @Override public Set<ITermVar> getVars(final ITerm term) {
        final Set<ITermVar> vars = Sets.newHashSet();
        getVars(term.getVars().elementSet(), Lists.newLinkedList(), Sets.newHashSet(), vars);
        return vars;
    }

    private void getVars(final Set<ITermVar> tryVars, final LinkedList<ITermVar> stack, final Set<ITermVar> visited,
            Set<ITermVar> vars) {
        tryVars.stream().forEach(var -> getVars(var, stack, visited, vars));
    }

    private void getVars(final ITermVar var, final LinkedList<ITermVar> stack, final Set<ITermVar> visited,
            Set<ITermVar> vars) {
        final ITermVar rep = findRep(var);
        if(!visited.contains(rep)) {
            visited.add(rep);
            stack.push(rep);
            final ITerm term = terms().get(rep);
            if(term != null) {
                getVars(term.getVars().elementSet(), stack, visited, vars);
            } else {
                vars.add(rep);
            }
            stack.pop();
        } else {
            final int index = stack.indexOf(rep); // linear
            if(index >= 0) {
                vars.addAll(stack.subList(0, index + 1));
            }
        }
    }

    ///////////////////////////////////////////
    // size(ITerm)
    ///////////////////////////////////////////

    @Override public TermSize size(final ITerm term) {
        return size(term, Sets.newHashSet(), Maps.newHashMap());
    }

    private TermSize size(final ITerm term, final Set<ITermVar> stack,
            final java.util.Map<ITermVar, TermSize> visited) {
        return term.match(Terms.cases(
        // @formatter:off
            appl -> TermSize.ONE.add(sizes(appl.getArgs(), stack, visited)),
            list -> size(list, stack, visited),
            string -> TermSize.ONE,
            integer -> TermSize.ONE,
            blob -> TermSize.ONE,
            var -> size(var, stack, visited)
            // @formatter:on
        ));
    }

    private TermSize size(IListTerm list, final Set<ITermVar> stack, final java.util.Map<ITermVar, TermSize> visited) {
        final Ref<TermSize> size = new Ref<>(TermSize.ZERO);
        while(list != null) {
            list = list.match(ListTerms.cases(
            // @formatter:off
                cons -> {
                    size.set(size.get().add(TermSize.ONE).add(size(cons.getHead(), stack, visited)));
                    return cons.getTail();
                },
                nil -> {
                    size.set(size.get().add(TermSize.ONE));
                    return null;
                },
                var -> {
                    size.set(size.get().add(size(var, stack, visited)));
                    return null;
                }
                // @formatter:on
            ));
        }
        return size.get();
    }

    private TermSize size(final ITermVar var, final Set<ITermVar> stack,
            final java.util.Map<ITermVar, TermSize> visited) {
        final ITermVar rep = findRep(var);
        final TermSize size;
        if(!visited.containsKey(rep)) {
            stack.add(rep);
            visited.put(rep, null);
            final ITerm term = terms().get(rep);
            size = term != null ? size(term, stack, visited) : TermSize.ZERO;
            visited.put(rep, size);
            stack.remove(rep);
            return size;
        } else if(stack.contains(rep)) {
            size = TermSize.INF;
        } else {
            size = visited.get(rep);
        }
        return size;
    }

    private TermSize sizes(final Iterable<ITerm> terms, final Set<ITermVar> stack,
            final java.util.Map<ITermVar, TermSize> visited) {
        TermSize size = TermSize.ZERO;
        for(ITerm term : terms) {
            size = size.add(size(term, stack, visited));
        }
        return size;
    }

    ///////////////////////////////////////////
    // toString(ITerm)
    ///////////////////////////////////////////

    @Override public String toString(final ITerm term) {
        return toString(term, Maps.newHashMap(), Maps.newHashMap(), -1);
    }

    @Override public String toString(final ITerm term, int n) {
        if(n < 0) {
            throw new IllegalArgumentException("Depth must be positive, but is " + n);
        }
        return toString(term, Maps.newHashMap(), Maps.newHashMap(), n);
    }

    private String toString(final ITerm term, final java.util.Map<ITermVar, String> stack,
            final java.util.Map<ITermVar, String> visited, final int maxDepth) {
        if(maxDepth == 0) {
            return "…";
        }
        // @formatter:off
        return term.match(Terms.cases(
            appl -> appl.getOp() + "(" + toStrings(appl.getArgs(), stack, visited, maxDepth - 1) + ")",
            list -> toString(list, stack, visited, maxDepth),
            string -> string.toString(),
            integer -> integer.toString(),
            blob -> blob.toString(),
            var -> toString(var, stack, visited, maxDepth)
        ));
        // @formatter:on
    }

    private String toString(IListTerm list, final java.util.Map<ITermVar, String> stack,
            final java.util.Map<ITermVar, String> visited, final int maxDepth) {
        if(maxDepth == 0) {
            return "…";
        }
        final StringBuilder sb = new StringBuilder();
        final AtomicBoolean tail = new AtomicBoolean();
        sb.append("[");
        while(list != null) {
            list = list.match(ListTerms.cases(
            // @formatter:off
                cons -> {
                    if(tail.getAndSet(true)) {
                        sb.append(",");
                    }
                    sb.append(toString(cons.getHead(), stack, visited, maxDepth - 1));
                    return cons.getTail();
                },
                nil -> {
                    return null;
                },
                var -> {
                    sb.append("|");
                    sb.append(toString(var, stack, visited, maxDepth - 1));
                    return null;
                }
                // @formatter:on
            ));
        }
        sb.append("]");
        return sb.toString();
    }

    private String toString(final ITermVar var, final java.util.Map<ITermVar, String> stack,
            final java.util.Map<ITermVar, String> visited, final int maxDepth) {
        if(maxDepth == 0) {
            return "…";
        }
        final ITermVar rep = findRep(var);
        final String toString;
        if(!visited.containsKey(rep)) {
            stack.put(rep, null);
            visited.put(rep, null);
            final ITerm term = terms().get(rep);
            if(term != null) {
                final String termString = toString(term, stack, visited, maxDepth);
                toString = (stack.get(rep) != null ? "μ" + stack.get(rep) + "." : "") + termString;
            } else {
                toString = rep.toString();
            }
            visited.put(rep, toString);
            stack.remove(rep);
            return toString;
        } else if(stack.containsKey(rep)) {
            final String muVar;
            if(stack.get(rep) == null) {
                muVar = "X" + stack.values().stream().filter(v -> v != null).count();
                stack.put(rep, muVar);
            } else {
                muVar = stack.get(rep);
            }
            toString = muVar;
        } else {
            toString = visited.get(rep);
        }
        return toString;
    }

    private String toStrings(final Iterable<ITerm> terms, final java.util.Map<ITermVar, String> stack,
            final java.util.Map<ITermVar, String> visited, final int maxDepth) {
        final StringBuilder sb = new StringBuilder();
        final AtomicBoolean tail = new AtomicBoolean();
        for(ITerm term : terms) {
            if(tail.getAndSet(true)) {
                sb.append(",");
            }
            sb.append(toString(term, stack, visited, maxDepth));
        }
        return sb.toString();
    }

    ///////////////////////////////////////////
    // class Result
    ///////////////////////////////////////////

    protected static class Result<T> implements IUnifier.Immutable.Result<T> {

        private final T result;
        private final IUnifier.Immutable unifier;

        public Result(T result, IUnifier.Immutable unifier) {
            this.result = result;
            this.unifier = unifier;
        }

        @Override public T result() {
            return result;
        }

        @Override public IUnifier.Immutable unifier() {
            return unifier;
        }

    }

    protected static class _RigidVarsException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public final RigidVarsException exception;

        public _RigidVarsException(ITermVar... vars) {
            this(new RigidVarsException(vars));
        }

        public _RigidVarsException(RigidVarsException exception) {
            super("rigid vars", exception, false, false);
            this.exception = exception;
        }

    }


}