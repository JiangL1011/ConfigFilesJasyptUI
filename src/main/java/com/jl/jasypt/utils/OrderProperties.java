package com.jl.jasypt.utils;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author 蒋领
 * @date 2019年05月31日
 */
public class OrderProperties extends Properties {
    private static final long serialVersionUID = 2960466877138258213L;

    private final LinkedHashSet<Object> keys = new LinkedHashSet<>();

    @Override
    public Set<Object> keySet() {
        return keys;
    }

    @Override
    public Object put(Object key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }
}
