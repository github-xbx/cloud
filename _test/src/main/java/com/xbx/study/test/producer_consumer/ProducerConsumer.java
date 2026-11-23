package com.xbx.study.test.producer_consumer;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 生产者 消费者问题，
 * 生产者往一个队列中写入数据，如有队列满了就不在写入 等消费者消费消费了之后再唤醒它
 * 消费者从一个对列中消费数据，如果队列空了就不在消费，等生产者生产之后再唤醒它
 */
public class ProducerConsumer {


    private final LinkedList<Integer> queue = new LinkedList<>();
    private final Integer MAX_SIZE = 10;



    public void producer() throws InterruptedException {
        int value = 0;
        while (true){
            synchronized (queue){
                //队列满了
                while (queue.size() == MAX_SIZE){
                    queue.wait();
                }
                // 生产一条数据
                queue.add(value);
                System.out.println("producer => "+ value);
                value++;

                queue.notifyAll(); //唤醒消费者 生产可数据之后 -> 唤醒消费者

            }
        }

    }


    public void consumer() throws InterruptedException {
        while (true){

            synchronized (queue){

                //队列空了->消费者等待
                while (queue.isEmpty()){
                    queue.wait();
                }

                //队列如果不空 就消费队列中的数据
                Integer removed = queue.removeFirst();

                System.out.println("consumer => "+removed);

                //唤醒生产者 继续生产数据
                queue.notifyAll();

            }


        }

    }



    //================================================================================================================

    private ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();



    public void lock_producer(){
        int value = 0;
        while (true){
            lock.lock();
            try {
                while (queue.size() == MAX_SIZE){
                    notFull.await();  //满了 -> 等待未满信号
                }

                queue.add(value);
                System.out.println("lock producer => " + value);
                value++;
                notEmpty.signalAll();  //有数据了 -> 唤醒消费者
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }finally {
                lock.unlock();
            }
        }
    }

    public void lock_consumer(){

        while (true){
            lock.lock();
            try {
                while (queue.isEmpty()){
                    notEmpty.await(); //队列空了 -> 等非空信号
                }
                Integer removed = queue.removeFirst();
                System.out.println("lock consumer => "+removed);
                notFull.signalAll();  //消费完成 有空间了 唤醒生产者

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }

    }







    public static void main(String[] args) {

        ProducerConsumer pc = new ProducerConsumer();

        String ab = new String("a") + new String("b");

//        new Thread(() -> {
//            try {
//                pc.lock_producer();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//        },"生产者").start();
//
//        new Thread(() -> {
//
//            try {
//                pc.consumer();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        },"消费者").start();

        new Thread(pc::lock_producer).start();
        new Thread(pc::lock_consumer).start();

    }





}
