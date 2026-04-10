import java.util.Collections;
import java.util.List;

class ExtensionMethodTargetTyping {
	boolean test(OrderDto order) {
		return ExtensionMethodTargetTyping.Extensions.canRefund(order, Collections.emptyList());
	}

	static class Extensions {
		public static boolean canRefund(OrderDto order, List<RefundOrderDto> refundOrders) {
			return order != null && refundOrders != null;
		}
	}

	static class OrderDto {
	}

	static class RefundOrderDto {
	}
}