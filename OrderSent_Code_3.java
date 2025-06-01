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
		@SuppressWarnings("unchecked") final List<RowTradeItem> tradeItems = (List<RowTradeItem>) parameters.get("tradeItems");

		if (order == null || tradeItems == null)
			throw new IllegalArgumentException("Parameters are missing.");

		final Map<String, Object> map = new HashMap<>();
		final List<PositionSentModel> positions = new ArrayList<>(tradeItems.size());

		PositionSentModel orderModel = null;
		for (final RowTradeItem cur : tradeItems) {
			final RowOrderPosition rowOrderPosition = cur.getRowOrderPosition();

			if (orderModel == null) {
				orderModel = new PositionSentModel();
				orderModel.fill(rowOrderPosition);
			}
			final PositionSentModel orderposition = new PositionSentModel();

			orderposition.setQuantity(1);
			orderposition.setGtin13(rowOrderPosition.getGtin13());
			orderposition.setSinglePrice(rowOrderPosition.getPriceGross());
			orderposition.setName(rowOrderPosition.getName());

			positions.add(orderposition);
		}

		map.put("order", orderModel);
		map.put("positions", positions);
		map.put("platformAccountId", order.getRowPlatformAccount().getId());
		map.put("resellerId", order.getRowPlatformAccount().getRowReseller().getId());

		return map;
	}
}
