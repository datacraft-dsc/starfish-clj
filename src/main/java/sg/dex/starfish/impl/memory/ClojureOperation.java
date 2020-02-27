package sg.dex.starfish.impl.memory;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import sg.dex.starfish.Job;
import sg.dex.starfish.Operation;
import sg.dex.starfish.impl.remote.RemoteAgent;
import sg.dex.starfish.impl.remote.RemoteJob;
import sg.dex.starfish.impl.remote.RemoteOperation;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Class implementing the starfish-java operation interface that wraps a Clojure function.
 *
 * @author Mike
 */
public class ClojureOperation extends RemoteOperation implements Operation {

    private final IFn function;

    protected ClojureOperation(String meta, RemoteAgent agent, IFn function) {
        super(agent, meta);
        this.function = function;
    }

    public static ClojureOperation create(String meta, RemoteAgent agent, IFn function) {
        return new ClojureOperation(meta, agent, function);
    }

    @Override
    public Job invokeAsync(final Map<String, Object> params) {
        IPersistentMap cparams = PersistentHashMap.EMPTY;
        for (Map.Entry<String, Object> e : params.entrySet()) {
            String paramName = e.getKey();
            Keyword k = Keyword.intern(null, paramName);
            cparams = cparams.assoc(k, e.getValue());
        }

        return agent.invokeAsync(this, (Map) cparams);
    }

    @Override
    public Job invoke(Map<String, Object> params) {
        return invokeAsync(params);
    }

}
