package org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

/** Port generik CRUD. Pembuatan entity (save) didefinisikan di port turunan. */
public interface IRepository<T, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();

    T update(T entity) throws EntityNotFoundException;

    void deleteById(ID id) throws EntityNotFoundException;
}
