//CONF: lombok.equalsAndHashCode.callSuper = call

class ValueParent {
}
@lombok.Value
class ValueCallSuper extends ValueParent {
}
