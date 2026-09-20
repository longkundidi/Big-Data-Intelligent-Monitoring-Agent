package sw.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sw.ai.domain.AlInput;

@Mapper
public interface AlInputMapper extends BaseMapper<AlInput> {

	@Delete("Delete from al_input where al_name = #{name} and al_class = #{alClass}")
	void removeByName(@Param("name") String name, @Param("alClass") String alClass);

}
