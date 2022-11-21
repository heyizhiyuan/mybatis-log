package com.cnj.mybatis.log.core;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.jdbc.BaseJdbcLogger;
import org.apache.ibatis.logging.jdbc.StatementLogger;
import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.StringTokenizer;

/**
 * @author czz
 * @since 2022/11/12 下午7:32
 */
public class MybatisConnectionLogger extends BaseJdbcLogger implements InvocationHandler {

    private final Connection connection;

    private MybatisConnectionLogger(Connection conn, Log statementLog, int queryStack) {
        super(statementLog, queryStack);
        this.connection = conn;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params)
            throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, params);
            }
            if ("prepareStatement".equals(method.getName()) || "prepareCall".equals(method.getName())) {
                if (isDebugEnabled()) {
                    debug(" Preparing: " + MybatisConnectionLogger.removeExtraWhitespaces((String) params[0]), true);
                }
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                stmt = MybatisStatementLogger.newInstance((String) params[0], stmt, statementLog, queryStack);
                return stmt;
            } else if ("createStatement".equals(method.getName())) {
                Statement stmt = (Statement) method.invoke(connection, params);
                stmt = StatementLogger.newInstance(stmt, statementLog, queryStack);
                return stmt;
            } else {
                return method.invoke(connection, params);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    }

    /**
     * Creates a logging version of a connection.
     *
     * @param conn         the original connection
     * @param statementLog the statement log
     * @param queryStack   the query stack
     * @return the connection with logging
     */
    public static Connection newInstance(Connection conn, Log statementLog, int queryStack) {
        InvocationHandler handler = new MybatisConnectionLogger(conn, statementLog, queryStack);
        ClassLoader cl = Connection.class.getClassLoader();
        return (Connection) Proxy.newProxyInstance(cl, new Class[]{Connection.class}, handler);
    }

    /**
     * return the wrapped connection.
     *
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    public static String removeExtraWhitespaces(String original) {
        StringTokenizer tokenizer = new StringTokenizer(original);
        StringBuilder builder = new StringBuilder();
        boolean hasMoreTokens = tokenizer.hasMoreTokens();
        while (hasMoreTokens) {
            builder.append(tokenizer.nextToken());
            hasMoreTokens = tokenizer.hasMoreTokens();
            if (hasMoreTokens) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

}
