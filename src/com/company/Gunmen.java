package com.company;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

class Gun {
    static LinkedList<String> barrel = new LinkedList<>();
    static int capacity = 20;

    //装弹
    void load(String bullet) {
        if (bullet != null) {
            synchronized (barrel) {
                while (barrel.size() >= capacity) {
                    try {
                        barrel.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                barrel.addLast(bullet);
                System.out.println("Thread"+ Thread.currentThread().getId() +"装弹！ 弹夹容量: " + barrel.size() );
                barrel.notifyAll();
            }
        }
    }

    //射击
    String shot() {
        synchronized (barrel) {
            while (barrel.isEmpty()) {
                try {
                    barrel.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String bullet = barrel.removeFirst();
            System.out.println("Thread"+ Thread.currentThread().getId() +"射击！ 弹夹容量: " + barrel.size());
            barrel.notifyAll();
            return bullet;
        }
    }
}

public class Gunmen {
    static Gun gun = new Gun();
    static CountDownLatch countDownLatch;

    public static void main(String[] args) {
        int threadcount = 50;
        int count = 20;
        countDownLatch = new CountDownLatch(threadcount);
        AtomicInteger shotnum = new AtomicInteger();
        AtomicInteger loadnum = new AtomicInteger();
        for (int i = 0; i < threadcount/2; i ++) {
            Thread thread = new Thread(new Loadman(count, loadnum));
            thread.start();
            thread = new Thread(new Shotman(count, shotnum));
            thread.start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("共装弹的次数： " + loadnum);
        System.out.println("共射击的次数： " + shotnum);
    }

    //装弹手
    static class Loadman implements Runnable {
        int count;
        AtomicInteger loadnum;

        public Loadman(int count, AtomicInteger loadnum) {
            this.count = count;
            this.loadnum = loadnum;
        }

        @Override
        public void run() {
            while (count > 0) {
                gun.load("bullet");
                loadnum.incrementAndGet();
                count--;
            }
            countDownLatch.countDown();
        }
    }

    //射手
    static class Shotman implements Runnable {
        int count;
        AtomicInteger shotnum;

        public Shotman(int count, AtomicInteger shotnum) {
            this.count = count;
            this.shotnum = shotnum;
        }

        @Override
        public void run() {
            while (count > 0) {
                gun.shot();
                shotnum.incrementAndGet();
                count--;
            }
            countDownLatch.countDown();
        }
    }
}