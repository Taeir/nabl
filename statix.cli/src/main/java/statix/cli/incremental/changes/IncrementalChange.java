package statix.cli.incremental.changes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

import mb.nabl2.util.collections.HashTrieRelation3;
import mb.nabl2.util.collections.IRelation3;
import statix.cli.StatixData;
import statix.cli.StatixParse;
import statix.cli.TestRandomness;

public abstract class IncrementalChange {
    protected static final IRelation3.Transient<String, String, IncrementalChange> ALL = HashTrieRelation3.Transient.of();
    
    private String sort;
    private String group;
    
    public IncrementalChange(String group, String sort) {
        this.group = group;
        this.sort = sort;
        ALL.put(group, sort, this);
        ALL.put(group, "*", this);
        ALL.put("*", sort, this);
        ALL.put("*", "*", this);
    }
    
    public String getSort() {
        return sort;
    }
    
    public String getGroup() {
        return group;
    }
    
    public boolean onSource() {
        return this instanceof IIncrementalSourceChange || this instanceof IIncrementalFileChange || this instanceof IIncrementalOptionChange;
    }
    
    public boolean onAST() {
        return this instanceof IIncrementalASTChange;
    }
    
    //---------------------------------------------------------------------------------------------
    
    /**
     * Called to parse the given file, applying this incremental change to it.
     * 
     * @param data
     *      the statix data
     * @param parse
     *      the statix parse
     * @param random
     *      the randomness for parsing
     * @param file
     *      the file to parse
     * 
     * @return
     *      the parse unit for the file
     * 
     * @throws MetaborgException
     *      If the given file cannot be parsed.
     * @throws NotApplicableException
     *      If this incremental change cannot be applied to this file.
     * @throws UnsupportedOperationException
     *      If {@link #supportsParse()} returns false.
     */
    public ISpoofaxParseUnit parse(StatixData data, StatixParse parse, TestRandomness random, String file) throws NotApplicableException, MetaborgException {
        if (!supportsParse()) throw new UnsupportedOperationException("Changing existing files is not supported by " + getClass().getSimpleName());
        return parse.parse(file);
    }
    
    /**
     * @param data
     *      the statix data
     * @param parse
     *      the statix parse
     * @param random
     *      the randomness
     * 
     * @return
     *      the parse unit for the newly created file
     * 
     * @throws MetaborgException
     *      If something went wrong with parsing.
     * @throws UnsupportedOperationException
     *      If {@link #supportsCreate()} returns false.
     */
    public ISpoofaxParseUnit create(StatixData data, StatixParse parse, TestRandomness random) throws MetaborgException {
        if (!supportsCreate()) throw new UnsupportedOperationException("Changing existing files is not supported by " + getClass().getSimpleName());
        
        throw new MetaborgException("Change " + getClass().getSimpleName() + " reports that it supports creation, but doesnt override the method!");
    }
    
    /**
     * @return
     *      true if this change can be applied to existing files, false otherwise
     */
    public boolean supportsParse() {
        return true;
    }
    
    /**
     * @return
     *      true if this change can create new files, false otherwise
     */
    public boolean supportsCreate() {
        return false;
    }
    
    //---------------------------------------------------------------------------------------------
    
    /**
     * Parses the given input string and adds it to the given list.
     * 
     * @param changes
     *      the accumulator
     * @param input
     *      the input
     * 
     * @throws IllegalArgumentException
     *      If the given input does not map to at least one change.
     */
    public static void parse(List<IncrementalChange> changes, String input) {
        String[] parts = input.split(":", 2);
        String group = parts[0];
        String name = parts.length == 2 ? parts[1] : "*";
        
        Set<IncrementalChange> set = ALL.get(group, name);
        if (set.isEmpty()) throw new IllegalArgumentException("String " + input + " does not match any incremental changes");
        changes.addAll(set);
    }

    /**
     * Parses the given list of string changes to their corresponding {@link IncrementalChange}.
     * 
     * @param changes
     *      the list of changes
     * 
     * @return
     *      the list of incremental changes corresponding to the given list of changes
     */
    public static List<IncrementalChange> parse(List<String> changes) {
        List<IncrementalChange> tbr = new ArrayList<>();
        for (String s : changes) {
            parse(tbr, s);
        }
        return tbr;
    }
    
    /**
     * @return
     *      all available options
     */
    public static String listAllOptions() {
        return ALL.get("*", "*").stream().map(c -> c.group + ":" + c.sort).collect(Collectors.joining(", "));
    }
    
    //---------------------------------------------------------------------------------------------
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + group.hashCode();
        result = prime * result + sort.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof IncrementalChange)) return false;
        
        IncrementalChange other = (IncrementalChange) obj;
        if (!group.equals(other.group)) return false;
        if (!sort.equals(other.sort)) return false;
        return true;
    }

    @Override
    public String toString() {
        return group + ":" + sort;
    }
}