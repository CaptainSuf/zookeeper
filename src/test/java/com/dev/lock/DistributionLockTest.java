package com.dev.lock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DistributionLockTest {

    @Test
    public void acquireLock()throws Exception{
        Thread thread1 = new Thread(new DistributionThread());
        thread1.setName("线程1");
        Thread thread2 = new Thread(new DistributionThread());
        thread2.setName("线程2");
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
    }

    class DistributionThread implements Runnable{

        @Override
        public void run() {
            DistributionLock lock = new DistributionLock("/test");
            lock.acquireLock();
            try {
                Thread.sleep(5000);
            }catch (Exception e){
                e.printStackTrace();
            }
            lock.unLock();
        }
    }


}
