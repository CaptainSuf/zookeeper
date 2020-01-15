package com.dev.config;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZkConfig {

    @Value("${zookeeper.hostlist}")
    private String hostList;

    @Bean
    public ZooKeeper zooKeeper()throws Exception{
        return new ZooKeeper(hostList, 30000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("事件类型为：" + event.getType());
                System.out.println("事件发生的路径：" + event.getPath());
                System.out.println("通知状态为：" + event.getState());
            }
        });
    }
}
