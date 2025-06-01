package com.avento.shop.backend.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.avento.shop.core.log.ILogger;
import com.avento.shop.core.log.LogAdapter;
import com.avento.shop.core.util.IModelGenerator;
import com.avento.shop.db.domain.RowOrder;
import com.avento.shop.db.domain.RowOrderPosition;
import com.avento.shop.db.domain.RowTradeItem;

public class OrderSent implements IModelGenerator {

	protected static ILogger logger = new LogAdapter(OrderSent.class);

	@Override
	public Map<String, Object> generateModel(Map<String, Object> parameters) {
		final RowOrder order = (RowOrder) parameters.get("order");
		@SuppressWarnings("unchecked")
		final List<RowTradeItem> tradeItems = (List<RowTradeItem>) parameters.get("tradeItems");

		validateParameters(order, tradeItems);

		final Map<String, Object> map = new HashMap<>();
		final List<PositionSentModel> positions = createPositionModels(tradeItems);

		map.put("order", createOrderModel(tradeItems));
		map.put("positions", positions);
		map.put("platformAccountId", order.getRowPlatformAccount().getId());
		map.put("resellerId", order.getRowPlatformAccount().getRowReseller().getId());

		return map;
	}

	private void validateParameters(RowOrder order, List<RowTradeItem> tradeItems) {
		if (order == null || tradeItems == null) {
			throw new IllegalArgumentException("Parameters are missing.");
		}
	}

	private List<PositionSentModel> createPositionModels(List<RowTradeItem> tradeItems) {
		List<PositionSentModel> positions = new ArrayList<>(tradeItems.size());
		for (final RowTradeItem cur : tradeItems) {
			final RowOrderPosition rowOrderPosition = cur.getRowOrderPosition();
			final PositionSentModel orderPosition = new PositionSentModel();

			orderPosition.setQuantity(1);
			orderPosition.setGtin13(rowOrderPosition.getGtin13());
			orderPosition.setSinglePrice(rowOrderPosition.getPriceGross());
			orderPosition.setName(rowOrderPosition.getName());

			positions.add(orderPosition);
		}
		return positions;
	}

	private PositionSentModel createOrderModel(List<RowTradeItem> tradeItems) {
		PositionSentModel orderModel = null;
		for (final RowTradeItem cur : tradeItems) {
			final RowOrderPosition rowOrderPosition = cur.getRowOrderPosition();
			if (orderModel == null) {
				orderModel = new PositionSentModel();
				orderModel.fill(rowOrderPosition);
			}
		}
		return orderModel;
	}
}
