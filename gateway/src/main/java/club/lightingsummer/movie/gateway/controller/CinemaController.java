package club.lightingsummer.movie.gateway.controller;

import club.lightingsummer.movie.cinema.api.api.CinemaInfoAPI;
import club.lightingsummer.movie.cinema.api.vo.*;
import club.lightingsummer.movie.gateway.util.Setting;
import club.lightingsummer.movie.gateway.vo.CinemaConditionResponseVO;
import club.lightingsummer.movie.gateway.vo.CinemaFieldResponseVO;
import club.lightingsummer.movie.gateway.vo.CinemaFieldsResponseVO;
import club.lightingsummer.movie.gateway.vo.ResponseVO;
import com.alibaba.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author     ：lightingSummer
 * @date       ：2019/7/29 0029
 * @description：
 */
@RestController
@RequestMapping("/cinema/")
public class CinemaController {
    private static final Logger logger = LoggerFactory.getLogger(CinemaController.class);

    @Reference(interfaceClass = CinemaInfoAPI.class, cache = "lru",connections = 10,check = false)
    private CinemaInfoAPI cinemaInfoAPI;

    /**
     * @author: lightingSummer
     * @date: 2019/7/29 0029
     * @description: 获取影院列表
     */
    @RequestMapping(value = "getCinemas")
    public ResponseVO getCinemas(CinemaQueryVO cinemaQueryVO) {
        try {
            Page<CinemaVO> cinemas = cinemaInfoAPI.getCinemas(cinemaQueryVO);
            if (cinemas.getData() == null || cinemas.getData().size() == 0) {
                return ResponseVO.success("没有查到符合条件的影院");
            } else {
                int nowPage = cinemas.getNowPage();
                int totalPage = cinemas.getTotalPage();
                return ResponseVO.success(nowPage, totalPage, Setting.imgDomain, cinemas.getData());
            }
        } catch (Exception e) {
            logger.error("获取影院列表失败" + e.getMessage());
            return ResponseVO.serviceFail("获取影院列表失败");
        }
    }

    /**
     * @author: lightingSummer
     * @date: 2019/7/29 0029
     * @description: 获取影院的查询条件
     */
    @RequestMapping(value = "getCondition")
    public ResponseVO getCondition(CinemaQueryVO cinemaQueryVO) {
        try {
            // 调用接口查询信息
            List<BrandVO> brandVOS = cinemaInfoAPI.getBrands(cinemaQueryVO.getBrandId());
            List<AreaVO> areaVOS = cinemaInfoAPI.getAreas(cinemaQueryVO.getDistrictId());
            List<HallTypeVO> hallTypeVOS = cinemaInfoAPI.getHallTypes(cinemaQueryVO.getHallType());
            // 封装返回类
            CinemaConditionResponseVO cinemaConditionResponseVO = new CinemaConditionResponseVO();
            cinemaConditionResponseVO.setAreaList(areaVOS);
            cinemaConditionResponseVO.setBrandList(brandVOS);
            cinemaConditionResponseVO.setHalltypeList(hallTypeVOS);
            return ResponseVO.success(cinemaConditionResponseVO);
        } catch (Exception e) {
            logger.error("获取影院的查询条件失败" + e.getMessage());
            return ResponseVO.serviceFail("获取影院的查询条件失败");
        }
    }

    /**
     * @author: lightingSummer
     * @date: 2019/7/29 0029
     * @description: 获取影院播放场次信息
     */
    @RequestMapping(value = "getFields")
    public ResponseVO getFields(Integer cinemaId) {
        try {
            // 影院信息
            CinemaInfoVO cinemaInfoVO = cinemaInfoAPI.getCinemaInfoById(cinemaId);
            // 上映信息
            List<FilmInfoVO> filmInfoVOS = cinemaInfoAPI.getFilmInfoByCinemaId(cinemaId);
            CinemaFieldsResponseVO cinemaFieldsResponseVO = new CinemaFieldsResponseVO();
            cinemaFieldsResponseVO.setCinemaInfo(cinemaInfoVO);
            cinemaFieldsResponseVO.setFilmList(filmInfoVOS);
            return ResponseVO.success(Setting.imgDomain, cinemaFieldsResponseVO);
        } catch (Exception e) {
            logger.error("获取影院播放场次信息失败" + e.getMessage());
            return ResponseVO.serviceFail("获取影院播放场次信息失败");
        }
    }

    /**
     * @author: lightingSummer
     * @date: 2019/7/29 0029
     * @description: 获取场次选座信息
     */
    @RequestMapping(value = "getFieldInfo", method = RequestMethod.POST)
    public ResponseVO getFieldInfo(Integer cinemaId, Integer fieldId) {
        try {
            // 影院信息
            CinemaInfoVO cinemaInfoVO = cinemaInfoAPI.getCinemaInfoById(cinemaId);
            // 影院信息
            FilmInfoVO filmInfoVO = cinemaInfoAPI.getFilmInfoByFieldId(fieldId);
            // 选座信息
            HallInfoVO hallInfoVO = cinemaInfoAPI.getFilmFieldInfo(fieldId);
            // 造几个销售的假数据，后续会对接订单接口
            hallInfoVO.setSoldSeats("1,2,3");
            CinemaFieldResponseVO cinemaFieldResponseVO = new CinemaFieldResponseVO();
            cinemaFieldResponseVO.setCinemaInfo(cinemaInfoVO);
            cinemaFieldResponseVO.setFilmInfo(filmInfoVO);
            cinemaFieldResponseVO.setHallInfo(hallInfoVO);
            return ResponseVO.success(Setting.imgDomain, cinemaFieldResponseVO);
        } catch (Exception e) {
            logger.error("获取场次选座信息失败" + e.getMessage());
            return ResponseVO.serviceFail("获取场次选座信息失败");
        }
    }
}
