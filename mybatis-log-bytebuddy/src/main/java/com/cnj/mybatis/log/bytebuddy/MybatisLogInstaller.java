package com.cnj.mybatis.log.bytebuddy;

import com.cnj.mybatis.log.core.MybatisConnectionLogger;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * @author czz
 * @since 2022/11/21 下午8:47
 */
public class MybatisLogInstaller {

    private MybatisLogInstaller() {
    }

    public static void install() {
        try {
            ByteBuddyAgent.install();
            new ByteBuddy()
                    .redefine(ConnectionLogger.class)
                    //匹配到ConnectionLogger 的newInstance方法
                    .method(named("newInstance"))
                    //用 MybatisConnectionLogger 的newInstance方法将其覆盖
                    .intercept(MethodDelegation.to(MybatisConnectionLogger.class))
                    .make()
                    .load(MybatisConnectionLogger.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
            System.out.println("MybatisLogByteBuddyAgent 已加载");
        } catch (Throwable throwable) {
            System.out.println("MybatisLogByteBuddyAgent 加载失败,ex=" + throwable.getMessage());
        }

    }

}
