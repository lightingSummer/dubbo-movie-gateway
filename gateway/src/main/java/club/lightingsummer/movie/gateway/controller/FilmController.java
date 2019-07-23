package club.lightingsummer.movie.gateway.controller;

import club.lightingsummer.movie.film.api.api.FilmInfoAPI;
import club.lightingsummer.movie.film.api.api.FilmRankAPI;
import club.lightingsummer.movie.film.api.vo.*;
import club.lightingsummer.movie.gateway.vo.FilmConditionVO;
import club.lightingsummer.movie.gateway.vo.FilmIndexVO;
import club.lightingsummer.movie.gateway.vo.FilmRequestVO;
import club.lightingsummer.movie.gateway.vo.ResponseVO;
import com.alibaba.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        CommonResponse<FilmVO> hostFilmsResponse = filmInfoAPI.getHotFilms(true, 8);
        if (hostFilmsResponse.getStatus() == 0) {
            filmIndexVO.setHotFilms(hostFilmsResponse.getData());
        } else {
            logger.error("获取热映电影失败" + hostFilmsResponse.getMsg());
        }
        //获取即将上映的电影
        CommonResponse<FilmVO> soonFilmsResponse = filmInfoAPI.getSoonFilms(true, 8);
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
        return null;
    }
}
