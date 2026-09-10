import java.util.Collections;
import java.util.List;
import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodTargetTyping.Extensions.class) class ExtensionMethodTargetTyping {
  static class Extensions {
    Extensions() {
      super();
    }
    public static boolean canRefund(OrderDto order, List<RefundOrderDto> refundOrders) {
      return ((order != null) && (refundOrders != null));
    }
  }
  static class OrderDto {
    OrderDto() {
      super();
    }
  }
  static class RefundOrderDto {
    RefundOrderDto() {
      super();
    }
  }
  ExtensionMethodTargetTyping() {
    super();
  }
  boolean test(OrderDto order) {
    return ExtensionMethodTargetTyping.Extensions.canRefund(order, Collections.emptyList());
  }
}