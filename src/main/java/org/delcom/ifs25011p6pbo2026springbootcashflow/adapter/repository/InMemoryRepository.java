package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/** Basis generik repository in-memory (untuk unit test). */
public abstract class InMemoryRepository<T> implements IRepository<T, Integer> {

    private final Map<Integer, T> store = new LinkedHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger();

    protected abstract int idOf(T entity);

    protected abstract String entityName();

    protected synchronized int nextId() {
        return sequence.incrementAndGet();
    }

    protected synchronized void put(T entity) {
        store.put(idOf(entity), entity);
    }

    protected synchronized List<T> snapshot() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized Optional<T> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return snapshot();
    }

    @Override
    public synchronized T update(T entity) throws EntityNotFoundException {
        if (!store.containsKey(idOf(entity))) {
            throw new EntityNotFoundException(entityName(), idOf(entity));
        }
        store.put(idOf(entity), entity);
        return entity;
    }

    @Override
    public synchronized void deleteById(Integer id) throws EntityNotFoundException {
        if (store.remove(id) == null) {
            throw new EntityNotFoundException(entityName(), id);
        }
    }
}
