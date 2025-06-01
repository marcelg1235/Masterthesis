package com.avento.shop.backend.mail;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.avento.shop.core.util.IModelGenerator;
import com.avento.shop.db.domain.RowOrder;
import com.avento.shop.db.domain.RowOrderPosition;
import com.avento.shop.db.domain.RowOrderPositionType;

public class CustomerFeedbackSent implements IModelGenerator {

	@Override
	public Map<String, Object> generateModel(Map<String, Object> parameters) {
		final RowOrder rowOrder = (RowOrder) parameters.get("customerFeedback");
		if (rowOrder == null) {
			throw new IllegalArgumentException("customerFeedback is missing");
		}

		final CustomerFeedbackSentModel customerInfo = createCustomerFeedbackSentModel(rowOrder);
		populateOrderPositions(rowOrder, customerInfo);

		Map<String, Object> map = new HashMap<>();
		map.put("customerFeedback", customerInfo);
		map.put("platformAccountId", rowOrder.getRowPlatformAccount().getId());
		map.put("resellerId", rowOrder.getRowPlatformAccount().getRowReseller().getId());

		return map;
	}

	private CustomerFeedbackSentModel createCustomerFeedbackSentModel(RowOrder rowOrder) {
		CustomerFeedbackSentModel customerInfo = new CustomerFeedbackSentModel();
		customerInfo.setFirstName(rowOrder.getRowDeliveryAddress().getFirstname());
		customerInfo.setLastName(rowOrder.getRowDeliveryAddress().getLastname());
		customerInfo.setAddress1(rowOrder.getRowDeliveryAddress().getAddress1());
		customerInfo.setAddress2(rowOrder.getRowDeliveryAddress().getAddress2());
		customerInfo.setAddress3(rowOrder.getRowDeliveryAddress().getAddress3());
		customerInfo.setZip(rowOrder.getRowDeliveryAddress().getZip());
		customerInfo.setCity(rowOrder.getRowDeliveryAddress().getCity());
		customerInfo.setState(rowOrder.getRowDeliveryAddress().getState());
		customerInfo.setCountryName(rowOrder.getRowDeliveryAddress().getRowCountry().getName());
		customerInfo.setCompany(rowOrder.getRowDeliveryAddress().getCompany());
		customerInfo.setOrderId(rowOrder.getId());
		customerInfo.setPlatformOrderId(rowOrder.getPlatformOrderId());
		customerInfo.setOrderDate(rowOrder.getOrderDate());
		customerInfo.setOip(new LinkedList<>());
		customerInfo.setTotalCosts(BigDecimal.ZERO);
		customerInfo.setShippingCosts(BigDecimal.ZERO);
		customerInfo.setPlatformAccountName(rowOrder.getRowPlatformAccount().getAccountname());
		return customerInfo;
	}

	private void populateOrderPositions(RowOrder rowOrder, CustomerFeedbackSentModel customerInfo) {
		for (RowOrderPosition rowOrderPosition : rowOrder.getRowsOrderPosition()) {
			if (RowOrderPositionType.ARTICLE.equals(rowOrderPosition.getRowOrderPositionType())) {
				processArticlePosition(rowOrderPosition, customerInfo);
			} else if (RowOrderPositionType.SHIPPING.equals(rowOrderPosition.getRowOrderPositionType())) {
				customerInfo.setShippingCosts(customerInfo.getShippingCosts().add(rowOrderPosition.getPriceGross()));
			}
		}
	}

	private void processArticlePosition(RowOrderPosition rowOrderPosition, CustomerFeedbackSentModel customerInfo) {
		CustomerFeedbackSentModel.Position customerInfoPosition = createPosition(rowOrderPosition);

		boolean found = false;
		for (CustomerFeedbackSentModel.Position oip : customerInfo.getOip()) {
			if (oip.getGtin13().equals(rowOrderPosition.getGtin13())) {
				updateExistingPosition(oip, customerInfo);
				found = true;
				break;
			}
		}

		if (!found) {
			customerInfo.setTotalCosts(customerInfo.getTotalCosts().add(customerInfoPosition.getSinglePrice()));
			customerInfo.getOip().add(customerInfoPosition);
		}
	}

	private CustomerFeedbackSentModel.Position createPosition(RowOrderPosition rowOrderPosition) {
		CustomerFeedbackSentModel.Position position = new CustomerFeedbackSentModel.Position();
		position.setSinglePrice(rowOrderPosition.getPriceGross());
		position.setTotalPerItemPrice(rowOrderPosition.getPriceGross());
		position.setQuantity(BigDecimal.ONE);
		position.setName(rowOrderPosition.getName());
		position.setGtin13(rowOrderPosition.getGtin13());
		return position;
	}

	private void updateExistingPosition(CustomerFeedbackSentModel.Position oip, CustomerFeedbackSentModel customerInfo) {
		oip.setQuantity(oip.getQuantity().add(BigDecimal.ONE));
		oip.setTotalPerItemPrice(oip.getSinglePrice().multiply(oip.getQuantity()));
		customerInfo.setTotalCosts(customerInfo.getTotalCosts().add(oip.getSinglePrice()));
	}
}
