package club.lightingsummer.movie.gateway.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author     ：lightingSummer
 * @date       ：2019/8/5 0005
 * @description：
 */
@Component
public class WebConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private PassportInterceptor passportInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // cookie校验
        registry.addInterceptor(passportInterceptor);
        super.addInterceptors(registry);
    }
}
