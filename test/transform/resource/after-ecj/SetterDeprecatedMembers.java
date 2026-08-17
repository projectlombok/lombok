//version 9:
import lombok.Setter;
class SetterDeprecatedMembers {
  @Deprecated(since = "1.2",forRemoval = true) @Setter int both;
  SetterDeprecatedMembers() {
    super();
  }
  public @java.lang.Deprecated(since = "1.2",forRemoval = true) @java.lang.SuppressWarnings("all") @lombok.Generated void setBoth(final int both) {
    this.both = both;
  }
}
