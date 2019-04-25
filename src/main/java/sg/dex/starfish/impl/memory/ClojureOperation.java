package sg.dex.starfish.impl.memory;

import java.util.HashMap;
import java.util.Map;
import clojure.lang.IFn;
import clojure.lang.Keyword;

import sg.dex.starfish.Asset;
import sg.dex.starfish.impl.memory.MemoryOperation;

public class ClojureOperation extends MemoryOperation {

	private IFn function;
	
	protected ClojureOperation(Map<String,Object> meta, IFn function) {
		super(meta);
		this.function=function;
	}
	
	public static ClojureOperation create(Map<String,Object> meta, IFn function) {
		return new ClojureOperation(meta,function);
	}

	@Override
	protected Asset compute(Map<String, Asset> params) {
		HashMap<Keyword,Asset> kparams=new HashMap<>(params.size());
		for (Map.Entry<String,Asset> e: params.entrySet()) {
			Keyword k=Keyword.intern(e.getKey());
			Asset v=e.getValue();
			kparams.put(k,v);
		}
		return (Asset) function.invoke(kparams);
	}

}
