//CONF: lombok.ToString.includeFieldNames = false
//CONF: lombok.ToString.doNotUseGetters = true
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
