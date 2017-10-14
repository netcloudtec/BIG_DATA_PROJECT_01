package com.sxt.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.hadoop.conf.Configuration;

import com.sxt.common.GlobalConstants;

/**
 * jdbc管理
 * 
 * @author root
 *
 */
public class JdbcManager {
    /**
     * 根据配置获取获取关系型数据库的jdbc连接
     * 
     * @param conf
     *            hadoop配置信息
     * @param flag
     *            区分不同数据源的标志位
     * @return
     * @throws SQLException
     */
    public static Connection getConnection(Configuration conf, String flag) throws SQLException {
        String driverStr = String.format(GlobalConstants.JDBC_DRIVER, flag);
        String urlStr = String.format(GlobalConstants.JDBC_URL, flag);
        String usernameStr = String.format(GlobalConstants.JDBC_USERNAME, flag);
        String passwordStr = String.format(GlobalConstants.JDBC_PASSWORD, flag);

        String driverClass = conf.get(driverStr);
        String url = conf.get(urlStr);
        String username = conf.get(usernameStr);
        String password = conf.get(passwordStr);
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            // nothing
        }
        return DriverManager.getConnection(url, username, password);
    }
}
