package club.lightingsummer.movie.gateway.vo;

import club.lightingsummer.movie.film.api.vo.CatVO;
import club.lightingsummer.movie.film.api.vo.SourceVO;
import club.lightingsummer.movie.film.api.vo.YearVO;
import lombok.Data;

import java.util.List;

@Data
public class FilmConditionVO {

    private List<CatVO> catInfo;
    private List<SourceVO> sourceInfo;
    private List<YearVO> yearInfo;

}
