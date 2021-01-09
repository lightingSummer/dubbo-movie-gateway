package club.lightingsummer.movie.gateway.util;

/**
 * @author     ：lightingSummer
 * @date       ：2019/8/5 0005
 * @description： 令牌桶限流算法 已废弃 无计时器
 */
public class TokenBucket {
    // 令牌最大值
    private int bucketNums = 30;
    // 令牌生产速率
    private double rate = 1;
    // 现在令牌桶的值
    private int nowTokens;
    // 上一次拿令牌的时间
    private long timeStamp = getNowTime();
    // 互斥量
    private volatile Object object;

    // 单例懒汉模式
    private Object mutex() {
        Object mutex = this.object;
        if (mutex == null) {
            synchronized (this) {
                mutex = this.object;
                if (mutex == null) {
                    this.object = mutex = new Object();
                }
            }
        }
        return mutex;
    }

    private long getNowTime() {
        return System.currentTimeMillis();
    }

    // 有问题，无计时器导致 nowTokens不准
    public boolean getTokens() {
        synchronized (this.mutex()) {
            // 当前时间
            long nowTime = getNowTime();
            // 计算当前的令牌数量
            nowTokens += (int) ((nowTime - timeStamp)/30 * rate);
            nowTokens = Math.min(nowTokens, bucketNums);
            timeStamp = nowTime;
            nowTokens--;
            if (nowTokens < 1) {
                return false;
            } else {
                return true;
            }
        }
    }
}
