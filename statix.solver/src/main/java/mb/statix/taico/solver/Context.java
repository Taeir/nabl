package mb.statix.taico.solver;

import static mb.statix.taico.util.TOverrides.hashMap;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

import mb.nabl2.terms.ITerm;
import mb.statix.constraints.CTrue;
import mb.statix.scopegraph.terms.Scope;
import mb.statix.solver.IConstraint;
import mb.statix.solver.log.NullDebugContext;
import mb.statix.spec.Spec;
import mb.statix.taico.dependencies.Dependencies;
import mb.statix.taico.dependencies.DependencyManager;
import mb.statix.taico.incremental.Flag;
import mb.statix.taico.incremental.changeset.IChangeSet;
import mb.statix.taico.incremental.manager.IncrementalManager;
import mb.statix.taico.incremental.strategy.IncrementalStrategy;
import mb.statix.taico.module.IModule;
import mb.statix.taico.module.ModuleManager;
import mb.statix.taico.module.ModulePaths;
import mb.statix.taico.scopegraph.IMInternalScopeGraph;
import mb.statix.taico.scopegraph.reference.ModuleDelayException;
import mb.statix.taico.solver.coordinator.ISolverCoordinator;
import mb.statix.taico.solver.state.IMState;

/**
 * Class to represent the context. The context keeps track of all information necessary for
 * modular solving.
 */
public class Context implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final transient IncrementalStrategy strategy;
    private final Spec spec;
    private final ModuleManager manager = new ModuleManager();
    private final DependencyManager<?> dependencies;
    private final IncrementalManager incrementalManager;
    private transient ISolverCoordinator coordinator;
    private transient Context oldContext;
    private transient IChangeSet changeSet;
    private transient Map<String, IConstraint> initConstraints;
    
//    private Map<String, MSolverResult> solverResults = hashMap();
    private Map<String, IMState> states = hashMap();
    
    private Context(IncrementalStrategy strategy, Spec spec) {
        this.strategy = strategy;
        this.spec = spec;
        this.incrementalManager = strategy.createManager();
        this.dependencies = strategy.createDependencyManager();
    }
    
    /**
     * NOTE: This method should only be used by the strategy.
     * 
     * @return
     *      the old context
     */
    public Optional<Context> getOldContext() {
        return Optional.ofNullable(oldContext);
    }
    
    /**
     * @return
     *      the constraints
     */
    public Map<String, IConstraint> getInitialConstraints() {
        return initConstraints;
    }
    
    /**
     * Retrieves the initialization constraints for the modules for which they were not provided.
     * 
     * @param context
     *      the context
     * @param moduleConstraints
     *      a map from module NAMES to constraints
     * 
     * @return
     *      the given map
     * 
     */
    protected Map<String, IConstraint> fixInitConstraints(Map<String, IConstraint> moduleConstraints) {
        for (Entry<String, IConstraint> entry : moduleConstraints.entrySet()) {
            if (!(entry.getValue() instanceof CTrue)) continue;
            
            String childNameOrId = entry.getKey();

            //Scope substitution does not have to occur here, since the global scope remains constant.
            //If there is no constraint available, use the initialization constraint for the child
            if (oldContext == null) {
                throw new IllegalStateException("Encountered a module without initialization but no previous context is available: " + childNameOrId);
            }
            
            IModule child = oldContext.getModuleByNameOrId(childNameOrId);
            if (child == null) {
                throw new IllegalStateException("Encountered a module without initialization that was not present in the previous context: " + childNameOrId);
            }

            entry.setValue(child.getInitialization());
        }
        return moduleConstraints;
    }
    
    public Spec getSpec() {
        return spec;
    }
    
    public IChangeSet getChangeSet() {
        return changeSet;
    }
    
    // --------------------------------------------------------------------------------------------
    // Modules
    // --------------------------------------------------------------------------------------------
    
    public ModuleManager getModuleManager() {
        return manager;
    }
    
    /**
     * 
     * @param module
     * 
     * @see ModuleManager#addModule(IModule)
     */
    public void addModule(IModule module) {
        manager.addModule(module);
    }
    
    public IModule getChildModuleByName(IModule requester, String name) throws ModuleDelayException {
        String id = ModulePaths.build(requester.getId(), name);
        
        if (isInitPhase()) return getModuleUnchecked(id);
        
        if (!getIncrementalManager().isAllowedAccess(requester.getId(), id)) {
            throw new ModuleDelayException(id);
        }
        
        return strategy.getChildModule(this, oldContext, requester, id);
    }
    
    /**
     * TODO: Unchecked access.
     * @param name
     *      the name of the module
     * @param level
     *      the level on which to find the module
     * 
     * @return
     *      the given module, or null if no module with the given name exists
     * 
     * @throws IllegalStateException
     *      If the given name is not unique on its level
     */
    public IModule getModuleByName(String name, int level) {
        IModule module = manager.getModuleByName(name, level);
        if (module != null) return module;
        
        return oldContext == null ? null : oldContext.manager.getModuleByName(name, level);
    }
    
    public IModule getModule(IModule requester, String id) throws ModuleDelayException {
        return getModule(requester.getId(), id);
    }
    
    public IModule getModule(String requester, String id) throws ModuleDelayException {
        if (isInitPhase()) return getModuleUnchecked(id);
        
        if (!getIncrementalManager().isAllowedAccess(requester, id)) {
            throw new ModuleDelayException(id);
        }
        
        //TODO Also do the first part based on the strategy, to allow the strategy to delay.
        return strategy.getModule(this, oldContext, requester, id);
    }
    
    /**
     * Gets the module with the given id, without checking with the strategy. If the given module
     * exists or existed in the previous context, it is returned.
     * 
     * @param id
     *      the id of the module
     * 
     * @return
     *      the module with the given id, or null if no such module exists nor existed
     */
    public IModule getModuleUnchecked(String id) {
        IModule module = manager.getModule(id);
        if (module != null) return module;
        
        if (oldContext == null) return null;
        
        module = oldContext.manager.getModule(id);
        
        //If the module was removed, don't return it
        if (changeSet.removed().contains(module)) return null;
        return module;
    }
    
    /**
     * @param id
     *      the id of the module
     * 
     * @return
     *      the module with the given id from the previous analysis
     */
    public IModule getOldModule(String id) {
        if (oldContext == null) return null;
        return oldContext.manager.getModule(id);
    }
    
    /**
     * @return
     *      the root module
     */
    public IModule getRootModule() {
        if (coordinator != null) return coordinator.getRootModule();
        return manager.getModulesOnLevel(0).values().stream().findFirst().get();
    }
    
    /**
     * @return
     *      a set with all modules
     * 
     * @see ModuleManager#getModules()
     */
    public Set<IModule> getModules() {
        return manager.getModules();
    }
    
    /**
     * @return
     *      a map from module ID to module
     * 
     * @see ModuleManager#getModulesAndIds()
     */
    public Map<String, IModule> getModulesAndIds() {
        return manager.getModulesAndIds();
    }
    
    /**
     * @return
     *      a map from module NAME to module
     * 
     * @see ModuleManager#getModulesOnLevel(int)
     */
    public Map<String, IModule> getModulesOnLevel(int level) {
        //TODO IMPORTANT does not include old modules
        return manager.getModulesOnLevel(level);
    }
    
    /**
     * If a module id is supplied, gets the module with the given id.
     * Otherwise, if the given string is a name, the module at the <b>first level</b> with the
     * given name is requested.
     * <p>
     * This method returns modules from the current context only (not from the old context).
     * 
     * @param nameOrId
     *      the name or id of the module
     * 
     * @return
     *      the module, or null if no such module exists
     */
    public IModule getModuleByNameOrId(String nameOrId) {
        IModule module;
        if (ModulePaths.containsPathSeparator(nameOrId)) {
            module = manager.getModule(nameOrId);
        } else {
            module = manager.getModuleByName(nameOrId, 1);
        }
        
        return module;
    }
    
    /**
     * Transfers the given module from the old context to the next context.
     * 
     * @param oldModule
     *      the old module
     * 
     * @return
     *      the state for the module
     * 
     * @throws IllegalStateException
     *      If the old module could not be transferred
     */
    public IMState transferModule(IModule oldModule) {
        if (oldContext == null) throw new IllegalStateException("Cannot transfer module " + oldModule + ": there is no old context to transfer from!");
        if (!oldContext.manager.hasModule(oldModule.getId())) throw new IllegalStateException("Cannot transfer module " + oldModule + ": module is unknown in the old context!");
        if (manager.hasModule(oldModule.getId())) throw new IllegalStateException("Cannot transfer module " + oldModule + ": there is already a new module with the same id!");
        
        IMState state = reuseOldState(oldModule);
        transferDependencies(oldModule.getId());
        return state;
    }
    
    // --------------------------------------------------------------------------------------------
    // States
    // --------------------------------------------------------------------------------------------
    
    /**
     * @param module
     *      the id of the module
     * 
     * @return
     *      the state associated with the given module in the current context
     */
    public IMState getState(String moduleId) {
        return states.get(moduleId);
    }
    
    /**
     * @param module
     *      the module
     * 
     * @return
     *      the state associated with the given module in the current context
     */
    public IMState getState(IModule module) {
        return states.get(module.getId());
    }
    
    /**
     * Sets the state for the given module.
     * 
     * @param module
     *      the module
     * @param state
     *      the state
     */
    public void setState(IModule module, IMState state) {
        IMState old = states.put(module.getId(), state);
        if (old != null) System.err.println("Overridden state of " + module);
    }
    
    /**
     * Reuses the state of an old module (module from the old context). The reused state is copied
     * and the copy is set as current state of the given module. The given module is also added to
     * this context if it was not already present.
     * 
     * @param oldModule
     *      the module
     * 
     * @return
     *      the copied state for the module
     */
    public IMState reuseOldState(IModule oldModule) {
        if (oldContext == null) throw new IllegalStateException("Old context is null!");
        
        IMState oldState = oldContext.getState(oldModule);
        if (oldState == null) throw new IllegalStateException("Old state of the module is null!");
        
        IMState newState = oldState.copy();
        setState(oldModule, newState);
        if (!manager.hasModule(oldModule.getId())) addModule(oldModule);
        return newState;
    }
    
    // --------------------------------------------------------------------------------------------
    // Scope Graphs
    // --------------------------------------------------------------------------------------------
    
    /**
     * @param id
     *      the id of the module
     * 
     * @return
     *      the scope graph of the given module in the current context
     */
    public IMInternalScopeGraph<Scope, ITerm, ITerm> getScopeGraph(String id) {
        IMState state = getState(id);
        return state == null ? null : state.scopeGraph();
    }
    
    /**
     * @param id
     *      the id of the module
     * 
     * @return
     *      the scope graph of the given module in the previous context
     */
    public IMInternalScopeGraph<Scope, ITerm, ITerm> getOldScopeGraph(String id) {
        if (oldContext == null) return null;
        
        return oldContext.getScopeGraph(id);
    }
    
    // --------------------------------------------------------------------------------------------
    // Dependencies
    // --------------------------------------------------------------------------------------------
    
    /**
     * Gets the dependencies object for the given module. If the given module does not currently
     * have a dependencies object, a new dependencies object will be created.
     * 
     * @param moduleId
     *      the id of the module
     * 
     * @return
     *      the dependencies object for the given module
     */
    @SuppressWarnings("unchecked")
    public <T extends Dependencies> T getDependencies(String moduleId) {
        return (T) dependencies.getDependencies(moduleId);
    }
    
    /**
     * Gets the dependencies object for the given module. If the given module does not currently
     * have a dependencies object, a new dependencies object will be created.
     * 
     * @param module
     *      the module
     * 
     * @return
     *      the dependencies object for the given module
     */
    public <T extends Dependencies> T getDependencies(IModule module) {
        return getDependencies(module.getId());
    }
    
    /**
     * Gets the dependencies of the given module in the previous context.
     * If there is no old context or if the old context does not know about the given module, then
     * this method will return null.
     * 
     * TODO Ensure that the dependencies are initialized.
     * 
     * @param moduleId
     *      the id of the module
     * 
     * @return
     *      the dependencies of the given module
     */
    @SuppressWarnings("unchecked")
    public <T extends Dependencies> T getOldDependencies(String moduleId) {
        if (oldContext == null || !oldContext.getModuleManager().hasModule(moduleId)) return null;
        
        return (T) oldContext.dependencies.getDependencies(moduleId);
    }
    
    /**
     * Transfers dependencies for this module from the old context to the new context.
     * 
     * @param moduleId
     *      the id of the module
     * 
     * @return
     *      the transferred dependencies
     *      
     * @throws IllegalStateException
     *      If there is no old context, or the old context is not aware of the given module.
     */
    public <T extends Dependencies> T transferDependencies(String moduleId) {
        if (oldContext == null) throw new IllegalStateException("The old context is null!");
        T old = getOldDependencies(moduleId);
        if (old == null) throw new IllegalStateException("The given module is unknown in the old context.");
        
        getDependencyManager().setDependencies(moduleId, old.copy());
        return old;
    }
    
    /**
     * Resets the dependencies of the module with the given id.
     * 
     * @param moduleId
     *      the id of the module
     * 
     * @return
     *      the new dependencies
     */
    @SuppressWarnings("unchecked")
    public <T extends Dependencies> T resetDependencies(String moduleId) {
        return (T) dependencies.resetDependencies(moduleId);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Dependencies> DependencyManager<T> getDependencyManager() {
        return (DependencyManager<T>) dependencies;
    }
    
    // --------------------------------------------------------------------------------------------
    // Phase
    // --------------------------------------------------------------------------------------------
    
    public <T> T getPhase() {
        return incrementalManager.getPhase();
    }
    
    public void setPhase(Object phase) {
        incrementalManager.setPhase(phase);
        
    }
    
    public boolean isInitPhase() {
        return incrementalManager.isInitPhase();
    }
    
    public void finishInitPhase() {
        incrementalManager.finishInitPhase();
    }
    
    @SuppressWarnings("unchecked")
    public <T extends IncrementalManager> T getIncrementalManager() {
        return (T) incrementalManager;
    }
    
    // --------------------------------------------------------------------------------------------
    // Solver coordinator
    // --------------------------------------------------------------------------------------------
    
    public ISolverCoordinator getCoordinator() {
        return coordinator;
    }
    
    public void setCoordinator(ISolverCoordinator coordinator) {
        this.coordinator = coordinator;
        coordinator.setContext(this);
    }
    
    // --------------------------------------------------------------------------------------------
    // Solver results
    // --------------------------------------------------------------------------------------------
    
//    public void addResult(String moduleId, MSolverResult result) {
//        solverResults.put(moduleId, result);
//    }
//    
//    public MSolverResult getResult(String moduleId) {
//        return solverResults.get(moduleId);
//    }
//    
//    public Map<String, MSolverResult> getResults() {
//        return new HashMap<>(solverResults);
//    }

    public Map<IModule, MSolverResult> getResults() {
        return coordinator.getResults();
    }
    
    // --------------------------------------------------------------------------------------------
    // Context transfer
    // --------------------------------------------------------------------------------------------

    /**
     * Commits the changes with regards to the previous context.
     * This call copies over any information from the old context that is relevant and then removes
     * the links to the old context and the change set, finalizing this context.
     */
    public void commitChanges() {
//        //We need to determine if a module has been reused.
//        for (IModule module : oldContext.manager.getModules()) {
//            if (changeSet.removed().contains(module)) continue;
//            
//            //If we have a new version for the module already, skip it
//            if (manager.hasModule(module.getId())) continue;
//            
//            assert module.getTopCleanliness() == ModuleCleanliness.CLEAN : "module flag should be clean if it is not in the new context";
//            module.setFlag(Flag.CLEAN);
//            //TODO The module should get the new context and stuff.
//            manager.addModule(module);
//        }
        
        //For all modules for which we have state, migrate the module itself as well
        for (IMState state : states.values()) {
            if (!manager.hasModule(state.owner().getId())) {
                System.err.println("Migrating module " + state.owner() + ": state is present, but module is not in current context!");
                addModule(state.owner());
            }
        }
        
        //Transfer all dependencies that are not present yet, create dependencies for other modules
        for (IModule module : manager._getModules()) {
            String id = module.getId();
            if (!dependencies.hasDependencies(id)) {
                System.err.println("There are no dependencies for module " + id + "!!!");
            }
        }
        
        //Clean the world
        for (IModule module : getModules()) {
            module.setFlag(Flag.CLEAN);
        }
        
        for (IModule module : Sets.difference(oldContext.manager.getModules(), getModules())) {
            System.err.println("Removed module " + module);
        }
        oldContext = null;
        changeSet = null;
        //TODO probably need more here
    }
    
    // --------------------------------------------------------------------------------------------
    // Object methods
    // --------------------------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "Context(" + hashCode() + ") [strategy=" + strategy
                + ", manager=" + manager
                + ", oldContext=" + oldContext
                + ", changeSet=" + changeSet
                + ", incrementalManager=" + incrementalManager + "]";
//                + ", solverResults=" + solverResults + "]";
    }
    
    // --------------------------------------------------------------------------------------------
    // Creation
    // --------------------------------------------------------------------------------------------
    
    /**
     * Creates a context for when a clean build is requested or no previous results are available.
     * 
     * @param strategy
     *      the incremental strategy to use
     * @param spec
     *      the spec
     * 
     * @return
     *      the new context
     */
    public static Context initialContext(IncrementalStrategy strategy, Spec spec) {
        Context newContext = new Context(strategy, spec);
        setContext(newContext);
        return newContext;
    }

    /**
     * Creates a context for when a previous context is available as well as the changeset with
     * regards to that previous context.
     * 
     * @param strategy
     *      the incremental strategy to employ
     * @param previousContext
     *      the previous context
     * @param changeSet
     *      the changeset
     * 
     * @return
     *      the new context
     */
    public static Context incrementalContext(
            IncrementalStrategy strategy, Context previousContext, IMState previousRootState,
            IChangeSet changeSet, Map<String, IConstraint> initConstraints, Spec spec) {
        Context newContext = new Context(strategy, spec);
        newContext.oldContext = previousContext;
        newContext.changeSet = changeSet;
        newContext.initConstraints = newContext.fixInitConstraints(initConstraints);
        
        //TODO IMPORTANT validate that the state used here is the correct one (should it be the one stored, or the one in the context corresponding to the root?)
        IMState newState = newContext.transferModule(previousRootState.getOwner());
        newContext.resetDependencies(newState.owner().getId()); //Reset dependencies of the top level
        //TODO Important we need to reset the dependants as well
        setContext(newContext);
        
        //Prune removed children
        for (IModule child : changeSet.removed()) {
            newState.scopeGraph().removeChild(child);
        }
        
        ModuleSolver.topLevelSolver(newState, null, new NullDebugContext()); //TODO Does not happen in the clean context, why?
        return newContext;
    }
    
    // --------------------------------------------------------------------------------------------
    // Thread specific accessors
    // --------------------------------------------------------------------------------------------
    private static final ThreadLocal<Context> currentContextThreadSensitive = new ThreadLocal<>();
    private static Context currentContext;
    
    private static transient final ThreadLocal<IModule> currentModuleThreadSensitive = new ThreadLocal<>();
    
    /**
     * @return
     *      the current context
     */
    public static Context context() {
        return currentContext;
    }
    
    public static void setContext(Context context) {
        currentContext = context;
//        currentContextThreadSensitive.set(context);
    }
    
    public static void setThreadSensitiveContext(Context context) {
        currentContextThreadSensitive.set(context);
    }
    
    public static Context getThreadSensitiveContext() {
        return currentContextThreadSensitive.get();
    }
    
    /**
     * <b>Thread sensitive:</b> this method changes its behavior based on the calling thread.<p>
     * 
     * Sets the current module of the calling thread.
     * 
     * @param module
     *      the module
     */
    public static void setCurrentModule(IModule module) {
        currentModuleThreadSensitive.set(module);
    }
    
    /**
     * <b>Thread sensitive:</b> this method changes its behavior based on the calling thread.<p>
     * 
     * The module belonging to the solver of the current thread. If called from a thread not
     * associated to a solver, this method returns null.
     * 
     * @return
     *      the current module
     */
    public static IModule getCurrentModule() {
        return currentModuleThreadSensitive.get();
    }
    
//    public static Context getThreadSensitiveContext(Context context) {
//        return currentContextThreadSensitive.get();
//    }
    // --------------------------------------------------------------------------------------------
    // Serialization
    // --------------------------------------------------------------------------------------------
    
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
    
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        System.out.println("Serializing context " + this);
        stream.defaultWriteObject();
    }
    
}