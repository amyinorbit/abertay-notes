package com.cesarparent.utils;

import android.util.Log;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by cesar on 23/02/2016.
 *
 * Notification Center is basically a custom version of LocalBroadcastManager, except it passes
 * references to objects instead of using Serializable/Parcelable like Intents do. This means
 * the uniqueness of a Model object in memory is not breached.
 */
public class NotificationCenter {

    /// Observer objects are used to store the reference to the actual observers.
    private class Observer {
        // We don't want to create a nasty retain cycle, so we store a weak ref.
        public WeakReference<Object>    observer;
        // The name of the target method.
        public String                   target;
        // The notification we're listening for.
        public String                   name;
        // The actual reflection method object we'll call.
        public Method                   targetMethod;

        /// Returns a new observer, and tries to get the method object.
        public Observer(String name, Object observer, String target) {
            this.name = name;
            this.observer = new WeakReference<>(observer);
            this.target = target;

            try {
                targetMethod = observer.getClass().getDeclaredMethod(target, Object.class);
            }
            catch (NoSuchMethodException e) {
                targetMethod = null;
                Log.w("NotificationCenter", "No such method: " + e.getMessage());
            }
        }

        /// Call the observer's target method and pass it the notification payload.
        public void perform(Object object) {
            if(targetMethod == null) { return; }
            Log.d("NotificationCenter", "Calling Method: "+observer.get().getClass()+"::"+target);
            try {
                targetMethod.invoke(observer.get(), object);
            }
            catch(Exception e) {
                Log.w("NotificationCenter", "Invalid method invocation: "+e.getMessage());
            }

        }

        /// Used to check for equality in the ArrayList.
        /// We make sure to check for pointer equality between observers!
        public boolean equals(Observer other) {
            return (observer.get() == other.observer.get() &&
                    target.equals(other.target) &&
                    name.equals(other.name));
        }
    }


    /// The singleton instance.
    private static NotificationCenter _instance = null;
    // The Observer objects registered in that center
    private ArrayList<Observer> _observers;

    /// Returns a shared instance of NotificationCenter, this one should be used most of the time.
    public static synchronized NotificationCenter defaultCenter() {
        if(_instance == null) {
            _instance = new NotificationCenter();
        }
        return _instance;
    }

    /// Returns a new notification center, which should not be needed.
    public NotificationCenter() {
        _observers = new ArrayList<>();
    }

    /// Registers an object and method to receive a certain notification type.
    public void addObserver(String name, Object observer, String target) {
        Observer obs = new Observer(name, observer, target);
        if(!_observers.contains(obs)) {
            _observers.add(obs);
        }
    }

    /// Remove an object from the center's dispatch table.
    public void removeObserver(Object observer) {
        Iterator<Observer> it = _observers.iterator();
        while (it.hasNext()) {
            Observer obs = it.next();
            if(obs.observer == observer) {
                it.remove();
            }
        }
    }

    /// Notify all observers for a notification type, passing an optional payload.
    public void postNotification(String name, Object payload) {
        Log.d("NotificationCenter", "Posting Notification: "+name);
        for(Observer obs : _observers) {
            if(name.equals(obs.name)) {
                obs.perform(payload);
            }
        }
    }

}
