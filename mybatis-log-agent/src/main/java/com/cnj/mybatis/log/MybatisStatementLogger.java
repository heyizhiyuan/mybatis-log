package com.cnj.mybatis.log;

import com.cnj.mybatis.log.util.FieldUtils;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.jdbc.BaseJdbcLogger;
import org.apache.ibatis.logging.jdbc.ResultSetLogger;
import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * @author czz
 * @since 2022/11/12 下午5:17
 */
public class MybatisStatementLogger extends BaseJdbcLogger implements InvocationHandler {

    private final String sql;

    private final PreparedStatement statement;

    public MybatisStatementLogger(String sql, Log log, int queryStack, PreparedStatement statement) {
        super(log, queryStack);
        this.sql = sql;
        this.statement = statement;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, params);
            }
            if (EXECUTE_METHODS.contains(method.getName())) {
                if (isDebugEnabled()) {
                    debug("Parameters: " + getParameterValueString(), true);
                }
                this.printSql();
                clearColumnInfo();
                if ("executeQuery".equals(method.getName())) {
                    ResultSet rs = (ResultSet) method.invoke(statement, params);
                    return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
                } else {
                    return method.invoke(statement, params);
                }
            } else if (SET_METHODS.contains(method.getName())) {
                if ("setNull".equals(method.getName())) {
                    setColumn(params[0], null);
                } else {
                    setColumn(params[0], params[1]);
                }
                return method.invoke(statement, params);
            } else if ("getResultSet".equals(method.getName())) {
                ResultSet rs = (ResultSet) method.invoke(statement, params);
                return rs == null ? null : ResultSetLogger.newInstance(rs, statementLog, queryStack);
            } else if ("getUpdateCount".equals(method.getName())) {
                int updateCount = (Integer) method.invoke(statement, params);
                if (updateCount != -1) {
                    debug("   Updates: " + updateCount, false);
                }
                return updateCount;
            } else {
                return method.invoke(statement, params);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    }

    private void printSql() {
        try {
            System.out.println("<========================================================================================>");
            System.out.println(buildSql(this.getParameterValues(), this.sql));
            System.out.println("<========================================================================================>");
        }catch (Throwable t){
            System.err.println("mybatis-log 打印sql出错了");
            t.printStackTrace();
        }
    }

    /**
     * Creates a logging version of a PreparedStatement.
     *
     * @param stmt         - the statement
     * @param statementLog - the statement log
     * @param queryStack   - the query stack
     * @return - the proxy
     */
    public static PreparedStatement newInstance(String sql, PreparedStatement stmt, Log statementLog, int queryStack) {
        InvocationHandler handler = new MybatisStatementLogger(sql, statementLog, queryStack, stmt);
        ClassLoader cl = PreparedStatement.class.getClassLoader();
        return (PreparedStatement) Proxy.newProxyInstance(cl, new Class[]{PreparedStatement.class, CallableStatement.class}, handler);
    }

    /**
     * Return the wrapped prepared statement.
     *
     * @return the PreparedStatement
     */
    public PreparedStatement getPreparedStatement() {
        return statement;
    }

    @SuppressWarnings("unchecked")
    protected List<ParameterValue> getParameterValues() {
        List<Object> columnValues;
        try {
            columnValues = (List<Object>) FieldUtils.getDeclaredField(this.getClass().getSuperclass(), "columnValues", true).get(this);
        } catch (IllegalAccessException e) {
            return Collections.emptyList();
        }
        List<ParameterValue> typeList = new ArrayList<>(columnValues.size());
        for (Object value : columnValues) {
            typeList.add(new ParameterValue(value));
        }
        return typeList;
    }

    public class ParameterValue{

        private final Object value;

        private final String stringValue;

        ParameterValue(Object value) {
            this.value = value;
            if(value == null){
                this.stringValue = "null";
            }else{
                this.stringValue = objectValueString(value);
            }
        }

        public String getStringValue() {
            return stringValue;
        }

        public boolean isNullOrNumber(){
            return value == null || value instanceof Number;
        }
    }

    private static String buildSql(List<ParameterValue> parameterValues, String sql) {
        char[] chars = sql.toCharArray();
        int index = 0;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (canReplacePlaceholder(chars[i], i == 0 ? null : chars[i - 1])) {
                ParameterValue parameterValue = parameterValues.get(index++);
                String value = parameterValue.isNullOrNumber()? parameterValue.getStringValue(): "'" + parameterValue.getStringValue() + "'";
                result.append(value);
            }else{
                result.append(chars[i]);
            }
        }
        return result.toString();
    }

    public static boolean canReplacePlaceholder(char c, Character prevChar) {
        if (c == '\'' || prevChar!= null && prevChar == '\'') {
            return false;
        }
        return c == '?';
    }


}
