package mb.statix.taico.incremental.manager;

public class IncrementalManager {
    protected volatile Object phase;
    protected boolean initPhase = true;
    
    @SuppressWarnings("unchecked")
    public <T> T getPhase() {
        return (T) phase;
    }
    
    public void setPhase(Object phase) {
        this.phase = phase;
    }
    
    public boolean isInitPhase() {
        return initPhase;
    }
    
    public void finishInitPhase() {
        initPhase = true;
    }
}
