package com.dev.config;

import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ZkConfig {

    @Value("${zookeeper.hostlist}")
    private String hostList;

    @Value("${zookeeper.session.timeout}")
    private int SESSION_TIMEOUT;

    @Value("${zookeeper.connection.timeout}")
    private int CONNECTION_TIMEOUT;

    @Value("${zookeeper.baseSleepTimeMs}")
    private int baseSleepTimeMs;

    @Value("${zookeeper.maxRetries}")
    private int maxRetries;

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

    @Bean
    public CuratorFramework curatorFramework(){
        // 添加验证权限
        List<AuthInfo> list = new ArrayList<>();
        list.add(new AuthInfo("digest","test:test".getBytes()));
        // 创建客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(hostList)
                .authorization(list)
                .sessionTimeoutMs(SESSION_TIMEOUT)
                .connectionTimeoutMs(CONNECTION_TIMEOUT)
                .retryPolicy(new ExponentialBackoffRetry(baseSleepTimeMs,maxRetries))
                .namespace("test")
                .build();
        client.start();
        return client;
    }


}
