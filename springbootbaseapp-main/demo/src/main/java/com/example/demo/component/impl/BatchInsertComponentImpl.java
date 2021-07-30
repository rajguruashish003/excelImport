package com.example.demo.component.impl;

import com.example.demo.component.BatchInsertComponent;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

@Component
public class BatchInsertComponentImpl implements BatchInsertComponent {

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    EntityManager entityManager;
    EntityTransaction entityTransaction;

    //@Value("${demo.batch.insert.size}")
    int batchSize = 25;

    int count=0;

    @Override
    public void beingTransaction() {
        entityManager = entityManagerFactory.createEntityManager();
        entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        count = 0;
    }

    @Override
    public void submit(Object o) {
        synchronized (BatchInsertComponentImpl.class) {
            entityManager.persist(o);
            if (++count % batchSize == 0) {
                //flush a batch of inserts and release memory
                entityManager.flush();
                entityManager.clear();
            }
        }
    }

    @Override
    public void commitTransaction() {
        entityTransaction.commit();
        count = 0;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}

