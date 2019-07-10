//CONF: lombok.toString.callSuper = CALL
@lombok.ToString
class ToStringAutoSuperWithNoParent {
}

@lombok.ToString
class ToStringAutoSuperWithParent extends ToStringAutoSuperWithNoParent {
}
