import java.util.Collections;
import java.util.List;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodTargetTyping.Extensions.class)
class ExtensionMethodTargetTyping {
	boolean test(OrderDto order) {
		return order.canRefund(Collections.emptyList());
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