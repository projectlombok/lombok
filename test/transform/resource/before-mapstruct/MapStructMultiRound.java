//skip-compare-content
//issue #3116: MapStructGeneratedTarget is written by MapStructTargetGenerator in an earlier round, and lombok reports it complete before it has processed it.
package multiround;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@lombok.Data class MapStructMultiRoundSource {
	private String name;
}

@lombok.Value @lombok.experimental.SuperBuilder class MapStructMultiRoundPlain {
	String title;
}

@Mapper interface MapStructMultiRound {
	@Mapping(source = "name", target = "title")
	MapStructMultiRoundPlain toPlain(MapStructMultiRoundSource source);
	
	@Mapping(source = "name", target = "title")
	MapStructGeneratedTarget toTarget(MapStructMultiRoundSource source);
}
