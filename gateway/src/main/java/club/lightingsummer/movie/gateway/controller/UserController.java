package club.lightingsummer.movie.gateway.controller;

import club.lightingsummer.movie.gateway.vo.ResponseVO;
import club.lightingsummer.movie.userapi.api.UserInfoAPI;
import club.lightingsummer.movie.userapi.api.UserLoginAPI;
import club.lightingsummer.movie.userapi.bo.CommonResponse;
import club.lightingsummer.movie.userapi.bo.UserModel;
import com.alibaba.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author     ：lightingSummer
 * @date       ：2019/8/5 0005
 * @description：
 */
@RequestMapping("/user/")
@RestController
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Reference(interfaceClass = UserInfoAPI.class, check = false)
    private UserInfoAPI userInfoAPI;

    @Reference(interfaceClass = UserLoginAPI.class, check = false)
    private UserLoginAPI userLoginAPI;

    @RequestMapping(value = "register", method = RequestMethod.POST)
    public ResponseVO register(UserModel userModel, HttpServletResponse response) {
        CommonResponse commonResponse = userLoginAPI.register(userModel);
        if (commonResponse.getStatus() == 0) {
            // 加cookie存储session_id
            Cookie cookie = new Cookie("m_ticket", commonResponse.getData().toString());
            cookie.setMaxAge(3600 * 24 * 7);
            cookie.setPath("/");
            response.addCookie(cookie);
            return ResponseVO.success("注册成功");
        } else {
            logger.error(commonResponse.getMsg() + userModel);
            return ResponseVO.serviceFail(commonResponse.getMsg());
        }
    }

    @RequestMapping(value = "check", method = RequestMethod.POST)
    public ResponseVO check(String username) {
        if (username != null && username.trim().length() > 0) {
            // 当返回true的时候，表示用户名可用
            CommonResponse commonResponse = userInfoAPI.checkName(username);
            if (commonResponse.getStatus() != 0) {
                return ResponseVO.serviceFail(commonResponse.getMsg());
            }
            if ((boolean) commonResponse.getData()) {
                return ResponseVO.success("用户名不存在");
            } else {
                return ResponseVO.serviceFail("用户名已存在");
            }
        } else {
            return ResponseVO.serviceFail("用户名不能为空");
        }
    }


}
