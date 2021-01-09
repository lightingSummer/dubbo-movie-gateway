package club.lightingsummer.movie.gateway.vo;

import club.lightingsummer.movie.cinema.api.vo.AreaVO;
import club.lightingsummer.movie.cinema.api.vo.BrandVO;
import club.lightingsummer.movie.cinema.api.vo.HallTypeVO;
import lombok.Data;

import java.util.List;

/**
 * @author     ：lightingSummer
 * @date       ：2019/7/29 0029
 * @description： 影院查询条件列表
 */
@Data
public class CinemaConditionResponseVO {
    private List<BrandVO> brandList;
    private List<AreaVO> areaList;
    private List<HallTypeVO> halltypeList;
}
