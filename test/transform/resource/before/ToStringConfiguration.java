//CONF: lombok.ToString.includeFieldNames = false
//CONF: lombok.ToString.doNotUseGetters = true
//CONF: lombok.ToString.counts = true
import lombok.ToString;
import lombok.Getter;
@ToString @Getter class ToStringConfiguration {
	int x;
}
@ToString(includeFieldNames=true) class ToStringConfiguration2 {
	int x;
}
@ToString(doNotUseGetters=false) @Getter class ToStringConfiguration3 {
	int x;
}
@ToString(counts=true, includeFieldNames=true) class ToStringConfiguration4 {
	java.lang.String[] array;
	java.util.List<java.lang.String> strings;
	java.util.Set<java.lang.Integer> set;
	java.util.Map<java.lang.String, java.lang.Integer> map;
	int x;
}
