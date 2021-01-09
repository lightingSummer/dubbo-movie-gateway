package club.lightingsummer.movie.gateway.aop;

/**
 * @author     ：lightingSummer
 * @date       ：2019/7/30 0030
 * @description： 全局异常
 */
public class ErrorTip extends Tip {
    public ErrorTip() {
        super();
        this.status = 999;
        this.msg = "系统出现异常，请联系管理员";
    }
}
