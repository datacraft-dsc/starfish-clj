package sg.dex.starfish.impl.memory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import clojure.lang.IFn;
import sg.dex.starfish.Job;
import sg.dex.starfish.Operation;

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
    public Job invokeAsync(final Map<String,Object> params){
        @SuppressWarnings("unchecked")
        CompletableFuture<Map<String,Object>> cf = CompletableFuture.supplyAsync(() ->
                (Map<String, Object>) function.invoke((Object)params)
        );
        return MemoryJob.create(cf);
    }

    @Override
    public Job invoke(Map<String, Object> params) {
        return invokeAsync(params);
    }

    @Override
    protected Map<String, Object> compute(Map<String, Object> params) {
        throw new UnsupportedOperationException("Use invokeXXX instead");
    }

}
