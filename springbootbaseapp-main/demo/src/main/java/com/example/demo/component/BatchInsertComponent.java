package com.example.demo.component;

import javax.persistence.EntityManager;

public interface BatchInsertComponent<I> {

    void beingTransaction();
    void submit(Object o);
    void commitTransaction();
    EntityManager getEntityManager();
}
