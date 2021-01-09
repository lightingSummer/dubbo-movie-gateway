package club.lightingsummer.movie.gateway.interceptor;

import org.springframework.stereotype.Component;

/**
 * @author     ：lightingSummer
 * @date       ：2019/8/4 0004
 * @description： 用于存放用户信息
 */
@Component
public class HostHolder {
    // 父线程传给子线程 hystrix线程隔离
    private InheritableThreadLocal<Integer> user = new InheritableThreadLocal<>();

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
