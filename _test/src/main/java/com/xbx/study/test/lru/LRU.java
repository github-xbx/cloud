package com.xbx.study.test.lru;

import java.util.HashMap;
import java.util.Map;

/**
 * 最近最久未使用 淘汰算法 实现 hashMap + 双向链表的方式
 *
 */
public class LRU {


    class Node {
        String key;
        int value;
        Node pre;
        Node next;
        Node(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private int capacity;
    private Map<String, Node> map;  //key -> node(节点) O(1)查找
    private Node head;  //链表 头
    private Node tail;  //链表 尾

    public void init(){

        capacity = 1;

        map = new HashMap<>();
        head = new Node("0",0);
        tail = new Node("0",0);

        head.next = tail;
        tail.pre = head;

    }


    public int get(String key){
        Node node = map.get(key);
        if (node == null){
            return  -1;
        }
        moveToHead(node);  //记录元素访问过 将node 移动到头部（第一个）
        return node.value;

    }

    public void put(String key, int value){

        Node node = map.get(key);
        if (node == null){
            //不存在直接插入
            Node node1 = new Node(key, value);

            map.put(key, node1);  //map添加记录
            addHead(node1); //将node 加到 头部
            //判断节点数 是否已将超过容量
            if (map.size() > capacity){
                Node last = tail.pre; // 最后一个元素
                map.remove(last.key);
                removeNode(last);
            }

        }else {
            //存在 更新 value
            node.value = value;

            moveToHead(node);  //记录元素访问过 将node 移动到头部（第一个）
        }

    }

    private void moveToHead(Node node) {

        removeNode(node); //先从原位置删除，
        addHead(node);  //再加到头部

    }

    private void removeNode(Node last) {

        Node node = last.pre;
        Node node1 = last.next;

        node.next = node1;
        node1.pre = node;
    }

    private void addHead(Node add) {
        Node node = head.next;

        head.next = add;
        add.pre = head;

        add.next = node;
        node.pre = add;
    }


}
