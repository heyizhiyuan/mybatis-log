package com.cnj.mybatis.log.agent;

import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @author czz
 * @since 2022/11/12 下午4:51
 */
public class MyBatisLogPreMainAgent {

    public static final String LOG_CLASS_PATH = "org/apache/ibatis/executor/BaseExecutor";

    public static final String LOG_CLASS_NAME = "org.apache.ibatis.executor.BaseExecutor";

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("MybatisLogAgent 已加载");
        inst.addTransformer(new LogTransformer());
    }

    static class LogTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (!LOG_CLASS_PATH.equals(className)) {
                // 如果返回null则字节码不会被修改
                return null;
            }
            System.out.println("MybatisLogAgent 开始替换类字节码");
            try {
                CtClass cc = ClassPool.getDefault().get(LOG_CLASS_NAME);
                cc.removeMethod(cc.getDeclaredMethod("getConnection"));
                CtMethod ctMethod = CtMethod.make("protected java.sql.Connection getConnection(org.apache.ibatis.logging.Log statementLog) throws java.sql.SQLException {\n" +
                        "    java.sql.Connection connection = transaction.getConnection();\n" +
                        "    if (statementLog.isDebugEnabled()) {\n" +
                        "      return com.cnj.mybatis.log.MybatisConnectionLogger.newInstance(connection, statementLog, queryStack);\n" +
                        "    } else {\n" +
                        "      return connection;\n" +
                        "    }\n" +
                        "  }", cc);
                cc.addMethod(ctMethod);
                byte[] bytes = cc.toBytecode();
                cc.detach();
                System.out.println("MybatisLogAgent 替换类字节码成功");
                return bytes;
            } catch (Exception e) {
                System.out.println("MybatisLogAgent 替换类字节码失败");
                e.printStackTrace();
                return null;
            }
        }
    }

}
