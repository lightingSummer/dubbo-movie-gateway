package club.lightingsummer.movie.gateway.aop;

/**
 * @author: lightingSummer
 * @date: 2019/7/30 0030
 * @discription: log class
 */
public abstract class Tip {

    protected int status;
    protected String msg;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}