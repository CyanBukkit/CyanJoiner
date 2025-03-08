package cn.cyanbukkit.cyanjoiner.cyanlib.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * 注册主指令注解反射用于
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterCommand {
    String name();
    String[] alia() default {};
    String dep() default "";
    String permission() default "";
}
