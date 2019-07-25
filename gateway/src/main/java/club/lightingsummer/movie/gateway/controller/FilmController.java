package club.lightingsummer.movie.gateway.controller;

import club.lightingsummer.movie.film.api.api.FilmAsyncServiceApi;
import club.lightingsummer.movie.film.api.api.FilmInfoAPI;
import club.lightingsummer.movie.film.api.api.FilmRankAPI;
import club.lightingsummer.movie.film.api.vo.*;
import club.lightingsummer.movie.gateway.util.Setting;
import club.lightingsummer.movie.gateway.vo.FilmConditionVO;
import club.lightingsummer.movie.gateway.vo.FilmIndexVO;
import club.lightingsummer.movie.gateway.vo.FilmRequestVO;
import club.lightingsummer.movie.gateway.vo.ResponseVO;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author     ：lightingSummer
 * @date       ：2019/7/4 0004
 * @description：
 */
@RestController
@RequestMapping("/film/")
public class FilmController {
    private static final Logger logger = LoggerFactory.getLogger(FilmController.class);

    @Reference(interfaceClass = FilmInfoAPI.class, check = false)
    private FilmInfoAPI filmInfoAPI;

    @Reference(interfaceClass = FilmRankAPI.class, check = false)
    private FilmRankAPI filmRankAPI;

    @Reference(interfaceClass = FilmAsyncServiceApi.class, async = true, check = false)
    private FilmAsyncServiceApi filmAsyncServiceApi;

    /**
     * @author: lightingSummer
     * @date: 2019/7/23 0023
     * @description: 获取首页信息
     * @return club.lightingsummer.movie.gateway.vo.ResponseVO<club.lightingsummer.movie.gateway.vo.FilmIndexVO>
     */
    @RequestMapping(path = "getIndex", method = RequestMethod.GET)
    public ResponseVO<FilmIndexVO> getIndex() {
        FilmIndexVO filmIndexVO = new FilmIndexVO();
        // 获取banners
        CommonResponse<List<BannerVO>> bannersResponse = filmInfoAPI.getBanners();
        if (bannersResponse.getStatus() == 0) {
            filmIndexVO.setBanners(bannersResponse.getData());
        } else {
            logger.error("获取banners失败" + bannersResponse.getMsg());
        }
        //获取正在热映的电影
        CommonResponse<FilmVO> hostFilmsResponse = filmInfoAPI.getHotFilms(true, 8, 1, 1, 99, 99, 99);
        if (hostFilmsResponse.getStatus() == 0) {
            filmIndexVO.setHotFilms(hostFilmsResponse.getData());
        } else {
            logger.error("获取热映电影失败" + hostFilmsResponse.getMsg());
        }
        //获取即将上映的电影
        CommonResponse<FilmVO> soonFilmsResponse = filmInfoAPI.getSoonFilms(true, 8, 1, 1, 99, 99, 99);
        if (soonFilmsResponse.getStatus() == 0) {
            filmIndexVO.setSoonFilms(soonFilmsResponse.getData());
        } else {
            logger.error("获取即将上映电影失败" + soonFilmsResponse.getMsg());
        }
        //获取票房排行榜
        CommonResponse<List<FilmInfoVO>> boxRankResponse = filmRankAPI.getBoxRanking();
        if (boxRankResponse.getStatus() == 0) {
            filmIndexVO.setBoxRanking(boxRankResponse.getData());
        } else {
            logger.error("获取票房排行榜失败" + boxRankResponse.getMsg());
        }
        //获取期待电影排行榜
        CommonResponse<List<FilmInfoVO>> expectRankResponse = filmRankAPI.getExpectRanking();
        if (expectRankResponse.getStatus() == 0) {
            filmIndexVO.setExpectRanking(expectRankResponse.getData());
        } else {
            logger.error("获取期待电影排行榜" + expectRankResponse.getMsg());
        }
        //获取评分电影排行榜
        CommonResponse<List<FilmInfoVO>> topResponse = filmRankAPI.getTop();
        if (topResponse.getStatus() == 0) {
            filmIndexVO.setTop100(topResponse.getData());
        } else {
            logger.error("获取期待电影排行榜" + topResponse.getMsg());
        }
        return ResponseVO.success(filmIndexVO);
    }

    /**
     * @author: lightingSummer
     * @date: 2019/7/23 0023
     * @description: 获取查询条件信息
     * @param catId
     * @param sourceId
     * @param yearId
     * @return club.lightingsummer.movie.gateway.vo.ResponseVO
     */
    @RequestMapping(path = "getConditionList", method = RequestMethod.GET)
    public ResponseVO getConditionList(@RequestParam(name = "catId", required = false, defaultValue = "99") String catId,
                                       @RequestParam(name = "sourceId", required = false, defaultValue = "99") String sourceId,
                                       @RequestParam(name = "yearId", required = false, defaultValue = "99") String yearId) {

        FilmConditionVO filmConditionVO = new FilmConditionVO();
        // 分类条件
        List<CatVO> catVOList = filmInfoAPI.getCats();
        for (CatVO catVO : catVOList) {
            if (catVO.getCatId().equals(catId)) {
                catVO.setActive(true);
                break;
            }
        }
        // 片源条件
        List<SourceVO> sourceVOList = filmInfoAPI.getSources();
        for (SourceVO sourceVO : sourceVOList) {
            if (sourceVO.getSourceId().equals(sourceId)) {
                sourceVO.setActive(true);
                break;
            }
        }
        // 年代条件
        List<YearVO> yearVOList = filmInfoAPI.getYears();
        for (YearVO yearVO : yearVOList) {
            if (yearVO.getYearId().equals(yearId)) {
                yearVO.setActive(true);
                break;
            }
        }

        filmConditionVO.setCatInfo(catVOList);
        filmConditionVO.setSourceInfo(sourceVOList);
        filmConditionVO.setYearInfo(yearVOList);
        return ResponseVO.success(filmConditionVO);
    }

    /**
     * @author: lightingSummer
     * @date: 2019/7/23 0023
     * @description: 获取影片接口
     * @param filmRequestVO
     * @return club.lightingsummer.movie.gateway.vo.ResponseVO
     */
    @RequestMapping(value = "getFilms", method = RequestMethod.GET)
    public ResponseVO getFilms(FilmRequestVO filmRequestVO) {

        FilmVO filmVO = null;
        // 根据showType判断影片查询类型
        switch (filmRequestVO.getShowType()) {
            case 1:
                filmVO = filmInfoAPI.getHotFilms(
                        false, filmRequestVO.getPageSize(), filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(), filmRequestVO.getSourceId(), filmRequestVO.getYearId(),
                        filmRequestVO.getCatId()).getData();
                break;
            case 2:
                filmVO = filmInfoAPI.getSoonFilms(
                        false, filmRequestVO.getPageSize(), filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(), filmRequestVO.getSourceId(), filmRequestVO.getYearId(),
                        filmRequestVO.getCatId()).getData();
                break;
            case 3:
                filmVO = filmInfoAPI.getClassicFilms(
                        filmRequestVO.getPageSize(), filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(), filmRequestVO.getSourceId(),
                        filmRequestVO.getYearId(), filmRequestVO.getCatId()).getData();
                break;
            default:
                filmVO = filmInfoAPI.getHotFilms(
                        false, filmRequestVO.getPageSize(), filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(), filmRequestVO.getSourceId(), filmRequestVO.getYearId(),
                        filmRequestVO.getCatId()).getData();
                break;
        }
        // 根据sortId排序
        // 添加各种条件查询
        // 判断当前是第几页
        return ResponseVO.success(
                filmVO.getNowPage(), filmVO.getTotalPage(),
                Setting.imgDomain, filmVO.getFilmInfoVO());
    }

    /**
     * @author: lightingSummer
     * @date: 2019/7/25 0025
     * @description: 根据电影名字或者id获取电影信息
     * @param searchParam
     * @param searchType
     * @return club.lightingsummer.movie.gateway.vo.ResponseVO
     */
    @RequestMapping(value = "films/{searchParam}", method = RequestMethod.GET)
    public ResponseVO films(@PathVariable("searchParam") String searchParam,
                            int searchType) throws ExecutionException, InterruptedException {
        // 根据searchType，判断查询类型
        FilmDetailVO filmDetail = null;
        try {
            filmDetail = filmInfoAPI.getFilmDetail(searchType, searchParam);

            if (filmDetail == null) {
                return ResponseVO.serviceFail("没有可查询的影片");
            } else if (filmDetail.getFilmId() == null || filmDetail.getFilmId().trim().length() == 0) {
                return ResponseVO.serviceFail("没有可查询的影片");
            }

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

            // 组织info对象
            InfoRequstVO infoRequstVO = new InfoRequstVO();

            // 组织Actor属性
            ActorRequestVO actorRequestVO = new ActorRequestVO();
            actorRequestVO.setActors(actorsVOFutrue.get());
            actorRequestVO.setDirector(actorVOFuture.get());

            // 组织info对象
            infoRequstVO.setActors(actorRequestVO);
            infoRequstVO.setBiography(filmDescVOFuture.get().getBiography());
            infoRequstVO.setFilmId(filmId);
            infoRequstVO.setImgVO(imgVOFuture.get());

            // 组织成返回值
            filmDetail.setInfo04(infoRequstVO);
        } catch (InterruptedException e) {
            logger.error("获取电影信息失败" + e.getMessage());
            return ResponseVO.appFail("获取电影信息失败");
        }

        return ResponseVO.success(Setting.imgDomain, filmDetail);
    }
}
