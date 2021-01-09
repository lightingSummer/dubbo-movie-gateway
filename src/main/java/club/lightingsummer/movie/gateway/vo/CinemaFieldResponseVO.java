package club.lightingsummer.movie.gateway.vo;

import club.lightingsummer.movie.cinema.api.vo.CinemaInfoVO;
import club.lightingsummer.movie.cinema.api.vo.FilmInfoVO;
import club.lightingsummer.movie.cinema.api.vo.HallInfoVO;
import lombok.Data;

/**
 * @author     ：lightingSummer
 * @date       ：2019/7/29 0029
 * @description：
 */
@Data
public class CinemaFieldResponseVO {
    private CinemaInfoVO cinemaInfo;
    private FilmInfoVO filmInfo;
    private HallInfoVO hallInfo;
}
