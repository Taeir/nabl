package org.metaborg.meta.nabl2.solver;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

public class Fresh implements Serializable {

    private static final long serialVersionUID = 42L;

    private final Map<String,Integer> counters;

    public Fresh() {
        counters = Maps.newHashMap();
    }

    public String fresh(String base) {
        // to prevent accidental name clashes, ensure the base contains no dashes,
        // and then use dashes as our connecting character.
        base = base.replaceAll("-", "_");
        int k = counters.getOrDefault(base, 0) + 1;
        counters.put(base, k);
        return base + "-" + k;
    }

    public void reset() {
        counters.clear();
    }

}