package com.toilatester.event;

public interface EventHandlerFactory<T> {

    EventHandler<T> getEventHandler(String key);
}
