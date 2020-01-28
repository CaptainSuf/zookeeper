package com.dev;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AppTest {

    @Autowired
    ZooKeeper zooKeeper;

    @Test
    public void create()throws Exception {
        zooKeeper.create("/test/c", "c+++".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Test
    public void update()throws Exception {
        zooKeeper.setData("/test/c","88888".getBytes(),-1);
    }

    @Test
    public void delete()throws Exception {
        zooKeeper.delete("/test/c",0);
    }

    @Test
    public void getData()throws Exception{
        Stat stat = new Stat();
        byte[] data = zooKeeper.getData("/test/c",false,stat);
        System.out.println(new String(data));
        System.out.println("version:"+stat.getVersion());
    }
}
