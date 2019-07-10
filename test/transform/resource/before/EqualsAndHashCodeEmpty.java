@lombok.EqualsAndHashCode
class EqualsAndHashCodeEmpty {
}

@lombok.EqualsAndHashCode(callSuper = true)
class EqualsAndHashCodeEmptyWithSuper extends EqualsAndHashCodeEmpty {
}
