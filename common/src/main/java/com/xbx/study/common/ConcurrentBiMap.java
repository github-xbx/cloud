package com.xbx.study.common;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 线程安全的 双向Map
 */
public class ConcurrentBiMap<K, V> {

    private final ConcurrentHashMap<K,V> forward = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<V,K> backward = new ConcurrentHashMap<>();

    // 读写锁
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


    public void put(K key, V value){
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        //写锁
        lock.writeLock().lock();
        try{
            //如果 value 被占用，那先删除映射
            K k = backward.get(value);
            if (k != null && k.equals(key)){
                backward.remove(value);
                forward.remove(k);
            }

            //如果 key 存在 则覆盖就的值
            V v = forward.get(key);
            if (v != null && v.equals(value)){
                backward.remove(v);
            }

            forward.put(key, value);
            backward.put(value, key);

        }finally {
            //释放锁
            lock.writeLock().unlock();
        }

    }

    /**
     * 删除  根据key 删除
     * @param key
     */
    public void remove(K key){
        Objects.requireNonNull(key);
        lock.writeLock().lock();
        try {
            V value = forward.remove(key);
            if (value != null){
                backward.remove(value);
            }
        }finally {
            lock.writeLock().unlock();
        }

    }

    /**
     * 删除 根据value 删除
     * @param value
     */
    public void removeByValue(V value){
        Objects.requireNonNull(value);
        lock.writeLock().lock();
        try {
            K key = backward.remove(value);
            if (key != null){
                forward.remove(key);
            }
        }finally {
            lock.writeLock().unlock();
        }
    }


    public V get(K key){
        Objects.requireNonNull(key);

        //读锁
        lock.readLock().lock();
        try {
            return forward.get(key);

        }finally {
            lock.readLock().unlock();
        }
    }

    public K getKey(V value){
        Objects.requireNonNull(value);

        lock.readLock().lock();
        try {
            return backward.get(value);
        }finally {
            lock.readLock().unlock();
        }
    }

}
