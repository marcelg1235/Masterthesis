package com.avento.shop.backend.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.avento.shop.core.log.ILogger;
import com.avento.shop.core.log.LogAdapter;
import com.avento.shop.core.util.IModelGenerator;
import com.avento.shop.db.domain.RowOrder;
import com.avento.shop.db.domain.RowOrderPosition;
import com.avento.shop.db.domain.RowTradeItem;

public class OrderSent implements IModelGenerator {

	private static final ILogger LOGGER = new LogAdapter(OrderSent.class);
	private static final String ORDER_PARAM = "order";
	private static final String TRADE_ITEMS_PARAM = "tradeItems";

	@Override
	public Map<String, Object> generateModel(Map<String, Object> parameters) {
		validateParameters(parameters);

		final RowOrder order = (RowOrder) parameters.get(ORDER_PARAM);
		@SuppressWarnings("unchecked")
		final List<RowTradeItem> tradeItems = (List<RowTradeItem>) parameters.get(TRADE_ITEMS_PARAM);

		final Map<String, Object> resultMap = new HashMap<>();

		PositionSentModel orderModel = createOrderModel(tradeItems);
		List<PositionSentModel> positions = createPositionModels(tradeItems);

		populateResultMap(resultMap, order, orderModel, positions);

		return resultMap;
	}

	private void validateParameters(Map<String, Object> parameters) {
		if (parameters == null || !parameters.containsKey(ORDER_PARAM) || !parameters.containsKey(TRADE_ITEMS_PARAM)) {
			throw new IllegalArgumentException("Required parameters are missing: 'order' and 'tradeItems' must be provided.");
		}

		if (parameters.get(ORDER_PARAM) == null || parameters.get(TRADE_ITEMS_PARAM) == null) {
			throw new IllegalArgumentException("Parameters cannot be null: 'order' and 'tradeItems' must have values.");
		}
	}

	private PositionSentModel createOrderModel(List<RowTradeItem> tradeItems) {
		if (tradeItems.isEmpty()) {
			LOGGER.warn("No trade items provided to create order model");
			return null;
		}

		PositionSentModel orderModel = new PositionSentModel();
		orderModel.fill(tradeItems.get(0).getRowOrderPosition());
		return orderModel;
	}

	private List<PositionSentModel> createPositionModels(List<RowTradeItem> tradeItems) {
		final List<PositionSentModel> positions = new ArrayList<>(tradeItems.size());

		for (final RowTradeItem tradeItem : tradeItems) {
			final RowOrderPosition rowOrderPosition = tradeItem.getRowOrderPosition();
			positions.add(createPositionModel(rowOrderPosition));
		}

		return positions;
	}

	private PositionSentModel createPositionModel(RowOrderPosition rowOrderPosition) {
		final PositionSentModel position = new PositionSentModel();
		position.setQuantity(1);
		position.setGtin13(rowOrderPosition.getGtin13());
		position.setSinglePrice(rowOrderPosition.getPriceGross());
		position.setName(rowOrderPosition.getName());
		return position;
	}

	private void populateResultMap(Map<String, Object> resultMap, RowOrder order,
								   PositionSentModel orderModel, List<PositionSentModel> positions) {
		resultMap.put("order", orderModel);
		resultMap.put("positions", positions);
		resultMap.put("platformAccountId", order.getRowPlatformAccount().getId());
		resultMap.put("resellerId", order.getRowPlatformAccount().getRowReseller().getId());
	}
}
