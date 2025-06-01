package com.avento.shop.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for fee calculations related to shipping and article costs.
 */
public class FeeUtil implements IFeeUtil {

	// Constants for precision and rounding
	private static final int DIVISION_SCALE = 5;
	private static final int FEE_SCALE = 2;
	private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

	@Override
	public Fee calculateShipAndArticleFeePerUnit(BigDecimal priceShip, BigDecimal priceArticle,
												 BigDecimal feeTotal, BigDecimal quantity) {
		// Validate inputs
		validateInputs(priceShip, priceArticle, feeTotal, quantity);

		final Fee resultFees = new Fee();
		final BigDecimal priceSum = priceShip.add(priceArticle);

		if (priceSum.compareTo(BigDecimal.ZERO) <= 0) {
			return handleZeroOrNegativePrice(priceSum, resultFees);
		}

		return calculateFees(priceShip, priceSum, feeTotal, quantity);
	}

	/**
	 * Validates that all input parameters are not null.
	 *
	 * @param priceShip The shipping price
	 * @param priceArticle The article price
	 * @param feeTotal The total fee
	 * @param quantity The quantity
	 * @throws IllegalArgumentException if any input is null
	 */
	private void validateInputs(BigDecimal priceShip, BigDecimal priceArticle,
								BigDecimal feeTotal, BigDecimal quantity) {
		if (priceShip == null) {
			throw new IllegalArgumentException("Shipping price cannot be null");
		}
		if (priceArticle == null) {
			throw new IllegalArgumentException("Article price cannot be null");
		}
		if (feeTotal == null) {
			throw new IllegalArgumentException("Total fee cannot be null");
		}
		if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Quantity must be positive");
		}
	}

	/**
	 * Handles the case when price sum is zero or negative.
	 *
	 * @param priceSum The sum of shipping and article prices
	 * @param resultFees The fee object to populate
	 * @return The populated fee object
	 */
	private Fee handleZeroOrNegativePrice(BigDecimal priceSum, Fee resultFees) {
		if (priceSum.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Price sum [" + priceSum + "] cannot be negative");
		}

		// Price sum is zero, set fees to zero to avoid division by zero
		resultFees.setShipFee(BigDecimal.ZERO);
		resultFees.setArticleFee(BigDecimal.ZERO);
		return resultFees;
	}

	/**
	 * Calculates shipping and article fees based on their proportion to the total price.
	 *
	 * @param priceShip The shipping price
	 * @param priceSum The sum of shipping and article prices
	 * @param feeTotal The total fee
	 * @param quantity The quantity
	 * @return The calculated fee object
	 */
	private Fee calculateFees(BigDecimal priceShip, BigDecimal priceSum,
							  BigDecimal feeTotal, BigDecimal quantity) {
		Fee resultFees = new Fee();

		// Calculate the proportion of shipping cost to total cost
		final BigDecimal shipProportion = priceShip.divide(priceSum, DIVISION_SCALE, DEFAULT_ROUNDING);

		// Distribute the total fee according to the proportion
		final BigDecimal shipFee = feeTotal.multiply(shipProportion);
		final BigDecimal articleFee = feeTotal.subtract(shipFee);

		// Calculate per-item fees
		final BigDecimal shipFeePerItem = shipFee.divide(quantity, FEE_SCALE, DEFAULT_ROUNDING);
		final BigDecimal articleFeePerItem = articleFee.divide(quantity, FEE_SCALE, DEFAULT_ROUNDING);

		resultFees.setShipFee(shipFeePerItem);
		resultFees.setArticleFee(articleFeePerItem);

		return resultFees;
	}
}
