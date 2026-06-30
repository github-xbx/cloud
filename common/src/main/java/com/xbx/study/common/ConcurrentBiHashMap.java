package com.xbx.study.common;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentBiHashMap<K, V> extends AbstractMap<K, V> {


    // ==================== 核心存储与锁 ====================
    private final Map<K, V> forward = new HashMap<>();
    private final Map<V, K> backward = new HashMap<>();

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    // ==================== 必须实现的唯一抽象方法 ====================
    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new BiMapIterator();
            }

            @Override
            public int size() {
                readLock.lock();
                try {
                    return forward.size();
                } finally {
                    readLock.unlock();
                }
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Entry)) return false;
                Entry<?, ?> e = (Entry<?, ?>) o;
                readLock.lock();
                try {
                    V val = forward.get(e.getKey());
                    return val != null && val.equals(e.getValue());
                } finally {
                    readLock.unlock();
                }
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof Entry)) return false;
                Entry<?, ?> e = (Entry<?, ?>) o;
                // 委托给外部类的 remove，保证双向删除
                return ConcurrentBiHashMap.this.remove(e.getKey()) != null;
            }

            @Override
            public void clear() {
                ConcurrentBiHashMap.this.clear();
            }
        };
    }

    // ==================== 自定义迭代器（强一致性 + 不可变 Entry） ====================
    private class BiMapIterator implements Iterator<Entry<K, V>> {
        private final Iterator<Entry<K, V>> forwardIt;
        private Entry<K, V> currentEntry;

        BiMapIterator() {
            readLock.lock();
            try {
                this.forwardIt = forward.entrySet().iterator();
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public boolean hasNext() {
            readLock.lock();
            try {
                return forwardIt.hasNext();
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public Entry<K, V> next() {
            readLock.lock();
            try {
                if (!forwardIt.hasNext()) throw new NoSuchElementException();
                currentEntry = forwardIt.next();
                // 关键：返回不可变 Entry，彻底阻止用户调用 setValue 破坏双向绑定
                return new SimpleImmutableEntry<>(currentEntry.getKey(), currentEntry.getValue());
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public void remove() {
            if (currentEntry == null) throw new IllegalStateException("next() must be called before remove()");
            ConcurrentBiHashMap.this.remove(currentEntry.getKey());
            currentEntry = null;
        }
    }

    // ==================== 核心写操作（必须加写锁保证原子性） ====================
    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(value, "Value must not be null");

        writeLock.lock();
        try {
            // 1. 检查 Value 是否已被其他 Key 绑定（维护 Value 全局唯一性）
            K existingKey = backward.get(value);
            if (existingKey != null && !existingKey.equals(key)) {
                throw new IllegalArgumentException(
                        "Value '" + value + "' is already bound to key: " + existingKey
                );
            }

            // 2. 处理旧值覆盖：若 Key 已存在，需清除旧的反向绑定
            V oldValue = forward.get(key);
            if (oldValue != null) {
                // 如果完全相同的 Key-Value 再次 put，直接返回
                if (oldValue.equals(value)) {
                    return value;
                }
                backward.remove(oldValue);
            }

            // 3. 执行双向绑定
            forward.put(key, value);
            backward.put(value, key);
            return oldValue;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        writeLock.lock();
        try {
            V value = forward.remove(key);
            if (value != null) {
                backward.remove(value);
            }
            return value;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            forward.clear();
            backward.clear();
        } finally {
            writeLock.unlock();
        }
    }

    // ==================== 查询操作（利用反向索引优化性能） ====================
    @Override
    public V get(Object key) {
        readLock.lock();
        try {
            return forward.get(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        readLock.lock();
        try {
            return forward.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 重要优化：AbstractMap 默认的 containsValue 是 O(n) 全表扫描。
     * 此处利用 backward 反向索引，降为 O(1) 复杂度。
     */
    @Override
    public boolean containsValue(Object value) {
        readLock.lock();
        try {
            return backward.containsKey(value);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return forward.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return forward.isEmpty();
        } finally {
            readLock.unlock();
        }
    }

    // ==================== 扩展方法：反向查找 ====================
    /**
     * 根据 Value 反向获取 Key（双向绑定的核心功能）
     */
    public K getKeyByValue(V value) {
        readLock.lock();
        try {
            return backward.get(value);
        } finally {
            readLock.unlock();
        }
    }



    /**
     * 自定义节点 node 参考 hashMap.Node
     * @param <K>
     * @param <V>
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        ConcurrentBiHashMap.Node<K,V> next;

        Node(int hash, K key, V value, ConcurrentBiHashMap.Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey() {
            return key;
        }
        public final V getValue() {
            return value;
        }
        public final String toString() {
            return key + "=" + value;
        }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;

            return o instanceof Map.Entry<?, ?> e && Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
        }
    }




}
