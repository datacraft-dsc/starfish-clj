package sg.dex.starfish.impl.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import clojure.lang.IFn;
import clojure.lang.Keyword;

import sg.dex.starfish.Asset;
import sg.dex.starfish.Job;
import sg.dex.starfish.Operation;
import sg.dex.starfish.impl.memory.AMemoryOperation;

public class ClojureOperation extends AMemoryOperation implements Operation{

    private final IFn function;
	
    protected ClojureOperation(String meta, MemoryAgent memoryAgent, IFn function) {
        super(meta,memoryAgent);
        this.function=function;
    }
	
    public static ClojureOperation create(String meta, MemoryAgent memoryAgent, IFn function) {
        return new ClojureOperation(meta,memoryAgent,function);
    }

    private Map<Keyword,Object> makeParamMap(Map<String,Object> params){
        Map<Keyword,Object> kparams=new HashMap<>(params.size());
        for (Map.Entry<String,Object> e: params.entrySet()) {
            Keyword k=Keyword.intern(e.getKey());
            Object v=e.getValue();
            kparams.put(k,v);
        }
        return kparams;
    }

    @Override
	public Job invokeAsync(final Map<String,Object> params){
    	@SuppressWarnings("unchecked")
		CompletableFuture<Map<String,Object>> cf = CompletableFuture.supplyAsync(() -> 
    		(Map<String, Object>) function.invoke((Object)params)
        );
        return MemoryJob.create(cf);
    }

    @SuppressWarnings("unchecked")
	@Override
	public Map<String,Object> invokeResult(Map<String, Object> params){
        return (Map<String, Object>) function.invoke(params);
    }

	@Override
	public Job invoke(Map<String, Object> params) {
		return invokeAsync(params);
	}

}
