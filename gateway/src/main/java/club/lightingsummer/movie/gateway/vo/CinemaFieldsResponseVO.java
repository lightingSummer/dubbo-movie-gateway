package club.lightingsummer.movie.gateway.vo;

import club.lightingsummer.movie.cinema.api.vo.CinemaInfoVO;
import club.lightingsummer.movie.cinema.api.vo.FilmInfoVO;
import lombok.Data;

import java.util.List;

/**
 * @author     ：lightingSummer
 * @date       ：2019/7/29 0029
 * @description：
 */
@Data
public class CinemaFieldsResponseVO {
    private CinemaInfoVO cinemaInfo;
    private List<FilmInfoVO> filmList;
}
