package club.lightingsummer.movie.gateway.controller;

import club.lightingsummer.movie.gateway.vo.ResponseVO;
import club.lightingsummer.movie.userapi.api.UserInfoAPI;
import club.lightingsummer.movie.userapi.api.UserLoginAPI;
import club.lightingsummer.movie.userapi.bo.CommonResponse;
import com.alibaba.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author     ：lightingSummer
 * @date       ：2019/8/6 0006
 * @description：
 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Reference(interfaceClass = UserInfoAPI.class, check = false)
    private UserInfoAPI userInfoAPI;

    @Reference(interfaceClass = UserLoginAPI.class, check = false)
    private UserLoginAPI userLoginAPI;

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public ResponseVO login(@RequestParam("userName") String userName,
                            @RequestParam("password") String password,
                            HttpServletResponse response) {
        CommonResponse commonResponse = userLoginAPI.login(userName, password);
        if (commonResponse.getStatus() != 0) {
            return ResponseVO.serviceFail(commonResponse.getMsg());
        }
        // 加cookie存储session_id
        Cookie cookie = new Cookie("m_ticket", commonResponse.getData().toString());
        cookie.setMaxAge(3600 * 24 * 7);
        cookie.setPath("/");
        response.addCookie(cookie);
        String token = commonResponse.getData().toString();
        return ResponseVO.success("登录成功", token);
    }
}
