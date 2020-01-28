package com.dev;

import com.dev.util.AclUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CuratorTest {

    @Resource(name = "curatorFramework")
    private CuratorFramework client;

    @Test
    public void create()throws Exception {
        client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT).forPath("/d","ddd".getBytes());
    }

    @Test
    public void update()throws Exception {
        client.setData().forPath("/d","aaa".getBytes());
    }

    @Test
    public void delete()throws Exception {
        client.delete().deletingChildrenIfNeeded().forPath("/test1");
    }

    @Test
    public void getData()throws Exception{
        Stat stat = new Stat();
        byte[] data = client.getData().storingStatIn(stat).forPath("/c");
        System.out.println("data:"+new String(data));
        System.out.println("vsrsion:"+stat.getVersion());
    }

    @Test
    public void isExists()throws Exception{
        Stat stat = client.checkExists().forPath("/c");
        if(null != stat){
            System.out.println("version:"+stat.getVersion());
            System.out.println(stat);
        }
    }

    @Test
    public void watcher()throws Exception{
        client.getData().usingWatcher(new CuratorWatcher() {
            @Override
            public void process(WatchedEvent watchedEvent) throws Exception {
                System.out.println("触发路径："+watchedEvent.getPath());
            }
        }).forPath("/c");
        Thread.sleep(10000);
    }

    @Test
    public void nodeCache()throws Exception{
        NodeCache nodeCache = new NodeCache(client,"/c");
        nodeCache.start(true);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
               String data = new String(nodeCache.getCurrentData().getData());
               System.out.println("触发路径："+nodeCache.getPath());
               System.out.println("修改后数据："+data);
            }
        });
        Thread.sleep(10000);
    }

    @Test
    public void childNodeCache()throws Exception{
        PathChildrenCache childrenCache = new PathChildrenCache(client,"/",true);
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
                if(event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)){
                    System.out.println("初始化");
                }else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)){
                    System.out.println("新增节点");
                }else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)){
                    System.out.println("更新节点");
                }else if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
                    System.out.println("删除节点");
                }
                System.out.println("触发路径："+event.getData().getPath());
                System.out.println("数据："+new String(event.getData().getData()));
            }
        });
        Thread.sleep(100000);
    }

    @Test
    public void acl()throws Exception{
        // 添加目录的权限
        List<ACL> list = new ArrayList<>();
        Id id = new Id("digest", AclUtils.getDigestUserPwd("test:test"));
        list.add(new ACL(ZooDefs.Perms.CREATE,id));
        // 链接客户端
        client.create().withMode(CreateMode.PERSISTENT).withACL(list).forPath("/a/add");
    }

}
