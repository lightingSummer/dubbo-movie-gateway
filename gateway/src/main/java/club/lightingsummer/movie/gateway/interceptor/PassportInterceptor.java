package club.lightingsummer.movie.gateway.interceptor;

import club.lightingsummer.movie.userapi.api.UserInfoAPI;
import club.lightingsummer.movie.userapi.bo.CommonResponse;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class PassportInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;
    @Reference(interfaceClass = UserInfoAPI.class, check = false)
    private UserInfoAPI userInfoAPI;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("m_ticket")) {
                    ticket = cookie.getValue();
                    break;
                }
            }
        }
        if (ticket != null) {
            CommonResponse<Integer> commonResponse = userInfoAPI.getUserIdByTicket(ticket);
            if (commonResponse.getStatus() == 0 && commonResponse.getData() != null) {
                hostHolder.addUser(commonResponse.getData());
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}