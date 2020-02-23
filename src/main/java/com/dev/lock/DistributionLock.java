package com.dev.lock;

import com.dev.util.SpringUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DistributionLock {


    private static final String LOCK_ROOT_PATH = "/LOCK";

    private static CuratorFramework client = SpringUtil.getBean("curatorFramework",CuratorFramework.class);

    private CountDownLatch zkLatch;

    private String path;


    public DistributionLock(String path) {
        this.path = LOCK_ROOT_PATH+path;
        init();
    }

    /**
     * 初始化
     */
    private void init(){
        try {
            addWatchToLock();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 通过锁的父子节监听子节点变化
     * @throws Exception
     */
    private void addWatchToLock()throws Exception{
        PathChildrenCache childrenCache = new PathChildrenCache(client,LOCK_ROOT_PATH,true);
        /**
         * POST_INITIALIZED_EVENT：异步初始化，初始化后会触发时间
         * NORMAL：异步初始化
         * BUILD_INITIAL_CACHE：同步初始化
         */
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curator, PathChildrenCacheEvent event)
                    throws Exception {
                // 监听节点的删除事件
                if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
                    if(path.equals(event.getData().getPath())){
                        if(null != zkLatch)
                            zkLatch.countDown();
                    }
                }
            }
        });
    }

    /**
     * 获取锁
     * @throws Exception
     */
    public void acquireLock(){
        while (true){
            // 尝试获取锁
            if(tryLock()){
                System.out.println(Thread.currentThread().getName()+":获取锁成功");
                break;
            }
            // 等待锁
            waitLock();
        }
    }

    /**
     * 尝试获取锁
     * @return
     */
    private boolean tryLock(){
        try {
            Stat stat = client.checkExists().forPath(path);
            if(null == stat){
                client.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL).forPath(path);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;

    }

    /**
     * 等待锁释放
     */
    private void waitLock(){
        if(null == zkLatch){
            zkLatch = new CountDownLatch(1);
        }
        try {
            zkLatch.await(10,TimeUnit.SECONDS);
            System.out.println("等待结束，当前剩余："+zkLatch.getCount());
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * 释放锁
     * @return
     */
    public boolean unLock(){
        try{
            if(null != client.checkExists().forPath(path)){
                client.delete().forPath(path);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
