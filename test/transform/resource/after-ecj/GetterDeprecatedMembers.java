//version 9:
import lombok.Getter;
class GetterDeprecatedMembers {
  @Deprecated(since = "1.2",forRemoval = true) @Getter int both;
  @Deprecated(since = "3") @Getter int sinceOnly;
  @Deprecated(forRemoval = true) @Getter int forRemovalOnly;
  GetterDeprecatedMembers() {
    super();
  }
  public @java.lang.Deprecated(since = "1.2",forRemoval = true) @java.lang.SuppressWarnings("all") @lombok.Generated int getBoth() {
    return this.both;
  }
  public @java.lang.Deprecated(since = "3") @java.lang.SuppressWarnings("all") @lombok.Generated int getSinceOnly() {
    return this.sinceOnly;
  }
  public @java.lang.Deprecated(forRemoval = true) @java.lang.SuppressWarnings("all") @lombok.Generated int getForRemovalOnly() {
    return this.forRemovalOnly;
  }
}
