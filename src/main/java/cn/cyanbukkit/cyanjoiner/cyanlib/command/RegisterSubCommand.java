package cn.cyanbukkit.cyanjoiner.cyanlib.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterSubCommand {
    String subName();
    String permission() default "";
    String howToUse() default "";
}
