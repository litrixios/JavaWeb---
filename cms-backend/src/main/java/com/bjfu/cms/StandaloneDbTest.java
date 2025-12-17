package com.bjfu.cms;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class StandaloneDbTest {

    public static void main(String[] args) {
        System.out.println("⏳ 正在读取 application.properties 配置...");

        try {
            // 1. 使用 Java 原生方式读取 resources 下的文件
            InputStream in = StandaloneDbTest.class.getClassLoader().getResourceAsStream("application.properties");

            if (in == null) {
                System.err.println("❌ 找不到 application.properties 文件！请确认文件名正确。");
                return;
            }

            Properties props = new Properties();
            props.load(in); // 加载文件内容

            // 2. 提取配置
            String driver = props.getProperty("spring.datasource.driver-class-name");
            String url = props.getProperty("spring.datasource.url");
            String username = props.getProperty("spring.datasource.username");
            String password = props.getProperty("spring.datasource.password");

            // 3. 开始连接
            System.out.println("   URL: " + url);
            System.out.println("   User: " + username);

            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, username, password);

            if (connection != null) {
                System.out.println("\n✅✅✅ 连接成功！配置读取也没问题！✅✅✅");
                connection.close();
            }

        } catch (Exception e) {
            System.err.println("❌ 出错了：" + e.getMessage());
            e.printStackTrace();
        }
    }
}