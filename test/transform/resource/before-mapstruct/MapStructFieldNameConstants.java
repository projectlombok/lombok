//skip-compare-content
//issue #3116: javac attributes annotation arguments before any processor runs, so in the first round the Fields constants are error attributes. Lombok injects them and forces a second round, but mapstruct consumes the @Mapping mirror in the first round because lombok reports the types complete.
package fieldnameconstants;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@lombok.Data @lombok.experimental.FieldNameConstants class MapStructFieldNameConstantsSource {
	private String name;
}

@lombok.Data @lombok.experimental.FieldNameConstants class MapStructFieldNameConstantsTarget {
	private String title;
}

@Mapper interface MapStructFieldNameConstants {
	@Mapping(source = MapStructFieldNameConstantsSource.Fields.name, target = MapStructFieldNameConstantsTarget.Fields.title)
	MapStructFieldNameConstantsTarget toTarget(MapStructFieldNameConstantsSource source);
}
