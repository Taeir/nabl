package org.metaborg.meta.nabl2.solver;

import java.util.List;

import org.metaborg.meta.nabl2.scopegraph.INameResolution;
import org.metaborg.meta.nabl2.scopegraph.IScopeGraph;
import org.metaborg.meta.nabl2.scopegraph.terms.Label;
import org.metaborg.meta.nabl2.scopegraph.terms.Occurrence;
import org.metaborg.meta.nabl2.scopegraph.terms.Scope;
import org.metaborg.meta.nabl2.terms.generic.TermIndex;
import org.metaborg.meta.nabl2.unification.IUnifier;

public interface ISolution {

    List<Message> getErrors();

    List<Message> getWarnings();

    List<Message> getNotes();

    IScopeGraph<Scope,Label,Occurrence> getScopeGraph();

    INameResolution<Scope,Label,Occurrence> getNameResolution();

    IProperties<Occurrence> getDeclProperties();

    IProperties<TermIndex> getAstProperties();

    IRelations getRelations();

    IUnifier getUnifier();

}