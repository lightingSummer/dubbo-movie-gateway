package club.lightingsummer.movie.gateway.interceptor;

import org.springframework.stereotype.Component;

/**
 * @author     ：lightingSummer
 * @date       ：2019/8/4 0004
 * @description： 用于存放用户信息
 */
@Component
public class HostHolder {
    private ThreadLocal<Integer> user = new ThreadLocal<>();

    public void addUser(int userId) {
        user.set(userId);
    }

    public Integer getUser() {
        return user.get();
    }

    public void clear() {
        user.remove();
    }
}
