# movie-gateway
movie-gateway
网关模块<br>

## 实现主要功能<br>
* 前后端分离的一个电影在线购票网站<br>
* 用户登陆、用户注册、用户信息修改、用户信息查询<br>
* 热映电影票房排行榜、新片预售排行榜、经典电影评分排行榜<br>
* 根据不同查询条件获取电影信息列表、影片信息、演员信息、导演信息、封面图信息等<br>
* 根据不同查询条件查询影院、影院信息获取<br>
* 根据影院查询场次信息、根据场次编号获取选座信息<br>
* 根据用户id获取订单信息、下单购票<br>

## 用到的一些技术<br>
* springboot+dubbo的使用，用到一些dubbo的特性<br>
  * 异步
  同时获取一些接口信息，通过异步调用来提高效率，使用Future来接收
  dubbo对springboot的支持，注解只支持接口，所以异步的方法单独封装成一个接口
  ```java
  @Reference(interfaceClass = FilmAsyncServiceApi.class, async = true, check = false)
    private FilmAsyncServiceApi filmAsyncServiceApi;
  
  String filmId = filmDetail.getFilmId();
            // 通过Dubbo的异步调用获取剩余详细信息
            // 获取影片描述信息
            filmAsyncServiceApi.getFilmDescAsync(filmId);
            Future<FilmDescVO> filmDescVOFuture = RpcContext.getContext().getFuture();
            // 获取图片信息
            filmAsyncServiceApi.getImgsAsync(filmId);
            Future<ImgVO> imgVOFuture = RpcContext.getContext().getFuture();
            // 获取导演信息
            filmAsyncServiceApi.getDectInfoAsync(filmId);
            Future<ActorVO> actorVOFuture = RpcContext.getContext().getFuture();
            // 获取演员信息
            filmAsyncServiceApi.getActorsAsync(filmId);
            Future<List<ActorVO>> actorsVOFutrue = RpcContext.getContext().getFuture();
  ```
  * 启动时检查
  启动时候会check服务是否可用，不可用时会抛出异常，阻止 Spring 初始化完成，默认是true，为了方便改为false，强烈推荐改为true
  ```java
  @Reference(interfaceClass = FilmRankAPI.class, check = false)
    private FilmRankAPI filmRankAPI;
  ```
  * 负载均衡
  dubbo本身提供了四种：随机、轮询、最少活跃、一致性hash,因为开发时候没有多开，所以代码里写了轮询，<br>
  存在的问题：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上。不过官方文档推荐一致性hash
  ```java
  @Component
  // 注解在provider上
  @Service(interfaceClass = CinemaInfoAPI.class, loadbalance = "roundrobin")
  public class CinemaInfoAPIImpl implements CinemaInfoAPI {
  }
  ```
  * 缓存类型
  dubbo提供了三种：lru、threadlocal、jcache，尝试使用lru，发现有的接口会报provider请求失败，对着网上说的改了timeout扔无效，至今未查出原因
  ```java
  @Reference(interfaceClass = CinemaInfoAPI.class,connections = 10,cache = "lru",check = false,timeout = 10000)
    private CinemaInfoAPI cinemaInfoAPI;
  ```
  * dubbo-admin
  下载了当当的dubboX并且把dubbo-admin打成了一个war包扔到了tomcat里，可以看到dubbo各种注册信息，除了界面丑点，其他还好
  
* Redis
  redis主要用作缓存层面，前后端交互的sessionId存储在redis里，用uuid作为key，userid作为时间，退出登陆的时候只需删除key<br>
  redis接下来改造的时候，可以把排行榜用redis的sort set来实现，用户信息等高频热点接口也可以使用sds做缓存扔进redis
  ```java
    @Autowired
    private JedisAdapter jedisAdapter;

    @Override
    public CommonResponse login(String userName, String password) {
        // 各种判断
        String ticket = UUID.randomUUID().toString().replaceAll("-", "");
        jedisAdapter.set(ticket, user.getUuid() + "");
        jedisAdapter.expire(ticket);
    }
    
    public boolean expire(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.expire(key, 60 * 24 * 7);
            return true;
        } catch (Exception e) {
            logger.error("Jedis get exception : " + e.getMessage());
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
  ```
* 令牌桶限流算法
![](https://upload-images.jianshu.io/upload_images/13670604-782edda532646656.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/421/format/webp)
  令牌桶的限流算法大题思路是固定时间固定生成令牌，令牌数量有最大值，获取到令牌的请求会被处理，否则直接返回错误信息<br>
  实现了一个简单的令牌桶算法，因为没有用计时器，nowTokens不是很准，最后改用谷歌的RateLimiter来严格控制QPS
  ```java
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

    // 有问题，不是使用严格计时器导致 nowTokens不准
    public boolean getTokens() {
        synchronized (this.mutex()) {
            // 当前时间
            long nowTime = getNowTime();
            // 计算当前的令牌数量
            nowTokens += (int) ((nowTime - timeStamp)/30 * rate);
            nowTokens = Math.min(nowTokens, bucketNums);
            nowTokens--;
            if (nowTokens < 1) {
                return false;
            } else {
                timeStamp = nowTime;
                return true;
            }
        }
    }
}
  ```
  附上谷歌的文件，RateLimiter里提供tryAcquire()和acquire()，其中acquire()请求不到会一直阻塞，都是基于令牌桶思路<br>
  谷歌代码是对比获得令牌的时间和当前时间作对比，其中使用了底层的计时器来计算时间，不得不佩服谷歌的代码质量
  ···
  public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
        long timeoutMicros = Math.max(unit.toMicros(timeout), 0L);
        checkPermits(permits);
        long microsToWait;
        // 上锁
        synchronized(this.mutex()) {
            long nowMicros = this.stopwatch.readMicros();
            // 判断是否可得
            if (!this.canAcquire(nowMicros, timeoutMicros)) {
                return false;
            }
            microsToWait = this.reserveAndGetWaitLength(permits, nowMicros);
        }
        this.stopwatch.sleepMicrosUninterruptibly(microsToWait);
        return true;
    }
  ···
  
  * hystrix熔断器做限流
    hystrix是springcloud里的，感觉用在dubbo里怪怪的。<br>
    hystrix本名豪猪，主要目的是防止服务器雪崩<br>
    当在一定时间段内服务调用方调用服务提供方的服务的次数达到设定的阈值，并且出错的次数也达到设置的出错阈值，就会进行服务降级<br>
    hystrix具有自我修复功能，除了open和close还有half-open状态，当open时候，设置一段时间，测试调用状态，如果可以则close
    ```java
    // 注解配置
    @HystrixCommand(fallbackMethod = "error", commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"),
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "4000"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")},
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "1"),
                    @HystrixProperty(name = "maxQueueSize", value = "10"),
                    @HystrixProperty(name = "keepAliveTimeMinutes", value = "1000"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "8"),
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "1500")
            })
    @RequestMapping(value = "buyTickets", method = RequestMethod.POST)
    public ResponseVO buyTickets(Integer fieldId, String soldSeats, String seatsName) {
    }
    ```
    可以看出hystrix是独立出一个线程池，来保证服务不会雪崩，所以在拿sessionId的时候要把线程设置为InheritableThreadLocal<br>
    但是因为hystrix是线程池，复用之前存在的线程，可能会导致取到的sessionId不对？百度比较推荐阿里的ttl，这边以后有时间好好研究下。
  * 搭建ftp服务器
    七牛云的测试域名被回收了，临时搭了一下ftp服务器用于存放文件，以便于调试
    
# 以后学习的一些地方
* 排行榜改成redis，学习分布式事务改进项目
* dubbo的一些其他特性，比如路由等
* 深入学习一些hystrix，并弄清楚InheritableThreadLocal是否能解决问题
* 有机会学习一些支付宝的开源接口，开发支付模块
* 订单模块，没有分库分表，有机会研读一下美团的开源文档，实践一下分库分表

# 模块链接
用户模块 https://github.com/lightingSummer/dubbo-movie-user
订单模块 https://github.com/lightingSummer/dubbo-movie-order
影院模块 https://github.com/lightingSummer/dubbo-movie-cinema
电影模块 https://github.com/lightingSummer/dubbo-movie-film



