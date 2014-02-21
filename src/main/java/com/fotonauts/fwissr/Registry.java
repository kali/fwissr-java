package com.fotonauts.fwissr;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.fotonauts.fwissr.source.Source;

public class Registry {

    private static int DEFAULT_REFRESH_PERIOD = 30;
    private long refreshPeriodMS;
    
    private SmarterMap registry = new SmarterMap();
    private List<Source> sources = new LinkedList<>();

    public Registry(SmarterMap params) {
        refreshPeriodMS = 1000 * (params.containsKey("refresh_period") ? ((Integer) (params.get("refresh_period")))
                : DEFAULT_REFRESH_PERIOD);
    }

    public synchronized void addSource(Source source) {
        sources.add(source);
        if(registry.isFrozen()) {
            reload();
        } else {
            registry.mergeAll(source.getConf());
        }
    }

    private synchronized void reload() {
        reset();
        load();
    }

    private synchronized void load() {
        registry = new SmarterMap();
        for(Source source: sources)
            registry.mergeAll(source.getConf());
        
    }

    private synchronized void reset() {
        registry = new SmarterMap();
        for(Source source: sources)
            source.reset();
    }

    public long getRefreshPeriodMS() {
        return refreshPeriodMS;
    }

    public int getRefreshPeriod() {
        return (int) (refreshPeriodMS / 1000);
    }
    
    public synchronized SmarterMap getRegistry() {
        registry.freeze();
        return registry;
    }

    public Serializable get(String key) {
        String[] keyAsArray = key.split("/");
        if(keyAsArray.length > 0 && keyAsArray[0].equals(""))
            keyAsArray = Arrays.copyOfRange(keyAsArray, 1, keyAsArray.length);
        Serializable current = getRegistry();
        for(String k: keyAsArray) {
            current = ((SmarterMap) current).get(k);
            if(current == null) 
                return null;
        }
        return current;
    }

    public String dump() {
        return getRegistry().dump();
    }
}