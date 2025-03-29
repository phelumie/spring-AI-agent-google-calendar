package com.ajisegiri.google_calendar;

import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDataStoreFactory implements DataStoreFactory {

    @Override
    public <V extends Serializable> DataStore<V> getDataStore(String id) {
        return new InMemoryDataStore<>(this, id);
    }

    private static class InMemoryDataStore<V extends Serializable> extends AbstractDataStore<V> {

        private final Map<String, V> map = new ConcurrentHashMap<>();

        protected InMemoryDataStore(DataStoreFactory dataStoreFactory, String id) {
            super(dataStoreFactory, id);
        }

        @Override
        public Set<String> keySet() {
            return Collections.unmodifiableSet(map.keySet());
        }

        @Override
        public Collection<V> values() {
            return Collections.unmodifiableCollection(map.values());
        }

        @Override
        public V get(String key) {
            return map.get(key);
        }

        @Override
        public DataStore<V> set(String key, V value) {
            map.put(key, value);
            return this;
        }

        @Override
        public DataStore<V> clear() {
            map.clear();
            return this;
        }

        @Override
        public DataStore<V> delete(String key) {
            map.remove(key);
            return this;
        }
    }
}