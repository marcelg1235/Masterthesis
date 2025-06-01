package com.avento.shop.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for calculating shipping and article fees.
 */
public class FeeUtil implements IFeeUtil {

	private static final BigDecimal ZERO = BigDecimal.ZERO;
	private static final int SCALE = 5;
	private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

	/**
	 * Calculates the shipping and article fee per unit.
	 *
	 * @param priceShip    the shipping price
	 * @param priceArticle the article price
	 * @param feeTotal     the total fee
	 * @param quantity     the quantity of items
	 * @return a Fee object containing the calculated fees
	 * @throws IllegalArgumentException if the price sum is negative
	 */
	@Override
	public Fee calculateShipAndArticleFeePerUnit(BigDecimal priceShip, BigDecimal priceArticle, BigDecimal feeTotal, BigDecimal quantity) {
		validateInputs(priceShip, priceArticle);

		final Fee resultFees = new Fee();
		final BigDecimal priceSum = priceShip.add(priceArticle);

		if (priceSum.compareTo(ZERO) == 0) {
			setZeroFees(resultFees);
		} else {
			calculateAndSetFees(priceShip, priceSum, feeTotal, quantity, resultFees);
		}

		return resultFees;
	}

	private void validateInputs(BigDecimal priceShip, BigDecimal priceArticle) {
		final BigDecimal priceSum = priceShip.add(priceArticle);
		if (priceSum.compareTo(ZERO) < 0) {
			throw new IllegalArgumentException("PriceSum [" + priceSum + "] is lower than 0!");
		}
	}

	private void setZeroFees(Fee resultFees) {
		resultFees.setShipFee(ZERO);
		resultFees.setArticleFee(ZERO);
	}

	private void calculateAndSetFees(BigDecimal priceShip, BigDecimal priceSum, BigDecimal feeTotal, BigDecimal quantity, Fee resultFees) {
		final BigDecimal factorToShip = priceShip.divide(priceSum, SCALE, ROUNDING_MODE);
		final BigDecimal shipFee = feeTotal.multiply(factorToShip);
		final BigDecimal articleFee = feeTotal.subtract(shipFee);

		final BigDecimal shipFeePerItem = shipFee.divide(quantity, 2, ROUNDING_MODE);
		final BigDecimal articleFeePerItem = articleFee.divide(quantity, 2, ROUNDING_MODE);

		resultFees.setShipFee(shipFeePerItem);
		resultFees.setArticleFee(articleFeePerItem);
	}
}
