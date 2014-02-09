package com.ichera.wolfviewer;

public abstract class RunnableArg<T> implements Runnable {

    T[] mArgs;

    public RunnableArg() {
    }

    public void run(T... args) {
        setArgs(args);
        run();
    }

    public void setArgs(T... args) {
        mArgs = args;
    }

    public int getArgCount() {
        return mArgs == null ? 0 : mArgs.length;
    }

    public Object[] getArgs() {
        return mArgs;
    }
}