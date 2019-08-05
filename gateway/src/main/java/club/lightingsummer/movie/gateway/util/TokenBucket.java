package club.lightingsummer.movie.gateway.util;

/**
 * @author     ：lightingSummer
 * @date       ：2019/8/5 0005
 * @description： 令牌桶限流算法
 */
public class TokenBucket {
    // 令牌最大值
    private int bucketNums = 100;
    // 令牌生产速率
    private int rate = 1;
    // 现在令牌桶的值
    private int nowTokens;
    // 上一次拿令牌的时间
    private long timeStamp = getNowTime();

    private long getNowTime() {
        return System.currentTimeMillis();
    }

    public boolean getTokens() {
        // 当前时间
        long nowTime = getNowTime();
        // 计算当前的令牌数量
        nowTokens += (int) ((nowTime - timeStamp) * rate);
        nowTokens = Math.min(nowTokens, bucketNums);
        System.out.println("现在有" + nowTokens + "个令牌");
        timeStamp = nowTime;
        if (nowTokens < 1) {
            return false;
        } else {
            nowTokens--;
            return true;
        }
    }
}
