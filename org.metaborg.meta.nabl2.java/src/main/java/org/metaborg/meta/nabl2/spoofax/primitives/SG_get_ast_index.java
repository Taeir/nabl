package org.metaborg.meta.nabl2.spoofax.primitives;

import org.metaborg.meta.nabl2.stratego.StrategoTermIndex;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SG_get_ast_index extends AbstractPrimitive {

    public SG_get_ast_index() {
        super(SG_get_ast_index.class.getSimpleName(), 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        StrategoTermIndex index = StrategoTermIndex.get(env.current());
        if (index == null) {
            return false;
        }
        env.setCurrent(index.toTerm(env.getFactory()));
        return true;
    }

}