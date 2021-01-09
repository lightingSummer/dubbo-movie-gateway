package club.lightingsummer.movie.gateway.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author: lightingSummer
 * @date: 2019/5/21 0021
 * @discription: log class
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution(* club.lightingsummer.movie.gateway.controller.*.*(..))")
    public void controllerPc() {
    }

    @Before(value = "controllerPc()")
    public void before(JoinPoint jp) {
        StringBuilder sb = new StringBuilder();
        sb.append("The params is ");
        for (Object arg : jp.getArgs()) {
            if (arg != null) {
                sb.append(arg.toString() + ",");
            }
        }
        String name = jp.getSignature().getName();
        logger.info(name + " started. " + sb.toString());
    }

    @AfterReturning(value = "controllerPc()", returning = "result")
    public void after(JoinPoint jp, Object result) {
        logger.info(jp.getSignature() + " completed. " + "The result is " + result);
    }
}
