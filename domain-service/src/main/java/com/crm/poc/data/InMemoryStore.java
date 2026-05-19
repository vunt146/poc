package com.crm.poc.data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InMemoryStore<T> {

    private final Map<String, T> store = new ConcurrentHashMap<>();
    private final java.util.function.Function<T, String> idExtractor;

    public InMemoryStore(java.util.function.Function<T, String> idExtractor) {
        this.idExtractor = idExtractor;
    }

    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<T> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public T save(T entity) {
        String id = idExtractor.apply(entity);
        store.put(id, entity);
        return entity;
    }

    public void delete(String id) {
        store.remove(id);
    }

    public List<T> findBy(Predicate<T> predicate) {
        return store.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public void saveAll(List<T> entities) {
        entities.forEach(this::save);
    }

    public long count() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }
}
