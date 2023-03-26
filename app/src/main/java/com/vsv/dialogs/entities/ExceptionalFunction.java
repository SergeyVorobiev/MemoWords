package com.vsv.dialogs.entities;

@FunctionalInterface
public interface ExceptionalFunction<T, R> {

    R apply(T t) throws Exception;
}