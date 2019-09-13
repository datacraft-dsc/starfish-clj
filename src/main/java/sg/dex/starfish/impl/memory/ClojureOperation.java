package sg.dex.starfish.impl.memory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import sg.dex.starfish.Job;
import sg.dex.starfish.Operation;

/**
 * Class implementing the starfish-java operation interface that wraps a Clojure function.
 * 
 * @author Mike
 *
 */
public class ClojureOperation extends AMemoryOperation implements Operation{

    private final IFn function;

    protected ClojureOperation(String meta, MemoryAgent memoryAgent, IFn function) {
        super(meta,memoryAgent);
        this.function=function;
    }

    public static ClojureOperation create(String meta, MemoryAgent memoryAgent, IFn function) {
        return new ClojureOperation(meta,memoryAgent,function);
    }

    @Override
    public Job invokeAsync(final Map<String,Object> params) {
        @SuppressWarnings("unchecked")
        CompletableFuture<Map<String,Object>> cf = CompletableFuture.supplyAsync(() ->
                compute(params)
        );
        return MemoryJob.create(cf);
    }

    @Override
    public Job invoke(Map<String, Object> params) {
        return invokeAsync(params);
    }

    /**
     * Computes the result synchronously with the current thread.
     */
    @SuppressWarnings("unchecked")
	@Override
    protected Map<String, Object> compute(Map<String, Object> params) {
    	// convert string keys in params to a Clojure map of keywords to values
    	IPersistentMap cparams = PersistentHashMap.EMPTY;
    	for (Map.Entry<String,Object> e : params.entrySet()) {
    		String paramName=e.getKey();
    		Keyword k=Keyword.intern(null, paramName);
    		cparams=cparams.assoc(k, e.getValue());
    	}
    	
    	Map<String, Object> results= (Map<String, Object>) function.invoke(cparams);
    	return results;
    }

}
