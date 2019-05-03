package sg.dex.starfish.impl.memory;

import java.util.HashMap;
import java.util.Map;
import clojure.lang.IFn;
import clojure.lang.Keyword;

import sg.dex.starfish.Asset;
import sg.dex.starfish.Job;
import sg.dex.starfish.Operation;
import sg.dex.starfish.impl.memory.AMemoryOperation;

public class ClojureOperation extends AMemoryOperation implements Operation{

    private IFn function;
	
    protected ClojureOperation(String meta, MemoryAgent memoryAgent, IFn function) {
        super(meta,memoryAgent);
        this.function=function;
    }
	
    public static ClojureOperation create(String meta, MemoryAgent memoryAgent, IFn function) {
        return new ClojureOperation(meta,memoryAgent,function);
    }

    private Map<Keyword,Asset> makeParamMap(Map<String,Asset> params){
        Map<Keyword,Asset> kparams=new HashMap<>(params.size());
        for (Map.Entry<String,Asset> e: params.entrySet()) {
            Keyword k=Keyword.intern(e.getKey());
            Asset v=e.getValue();
            kparams.put(k,v);
        }
        return kparams;
    }

    public Job invokeAsync(Map<String,Asset> params){
        Map<Keyword,Asset> kparams=makeParamMap(params);
        return (Job) function.invoke(kparams);
    }

    public Map<String,Object> invokeResult(Map<String, Object> params){
        return null;
    }
    public Job invoke(Map<String,Asset> params){
        Map<Keyword,Asset> kparams=makeParamMap(params);
        return (Job) function.invoke(kparams);
    }

}
