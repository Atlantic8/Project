package com.example.atlantic8.projectspartan;

import android.graphics.Bitmap;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Atlantic8 on 2016/2/27 0027.
 */
public class MemoryCache {
    private Map<String, SoftReference<Bitmap>> cache=Collections.synchronizedMap(new HashMap<String, SoftReference<Bitmap>>());

    public MemoryCache() {}

    public Bitmap get(String id) {
        if (!cache.containsKey(id))
            return null;
        SoftReference<Bitmap> ref = cache.get(id);
        return ref.get();
    }

    public void put(String id, Bitmap bitmap) {
        cache.put(id, new SoftReference<Bitmap>(bitmap));
    }

    public void clear() {
        cache.clear();
    }
}
