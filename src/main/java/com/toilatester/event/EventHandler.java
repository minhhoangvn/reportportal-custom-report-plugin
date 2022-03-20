package com.toilatester.event;

public interface EventHandler<T> {

    void handle(T event);
}