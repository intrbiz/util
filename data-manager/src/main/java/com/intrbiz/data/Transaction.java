package com.intrbiz.data;

@FunctionalInterface
public interface Transaction
{
    void run() throws DataException;
}
