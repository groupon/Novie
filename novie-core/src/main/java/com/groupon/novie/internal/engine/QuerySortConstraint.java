package com.groupon.novie.internal.engine;

/**
 * This class represent an order/sort constraint. Dimension and information name are stored in upperCase
 * @author thomas
 * @since 10/01/2014.
 */

public class QuerySortConstraint {

    private OrderByDirection orderByDirection;
    private String dimensionName;
    private String informationName;

    public QuerySortConstraint(OrderByDirection orderByDirection, String dimensionName, String informationName) throws IllegalArgumentException {
        if (dimensionName == null) {
            throw new IllegalArgumentException("dimensionName can not be null");
        }
        this.orderByDirection = orderByDirection;
        this.dimensionName = dimensionName;
        this.informationName = (informationName == null) ? null : informationName.toUpperCase();
    }


    public OrderByDirection getOrderByDirection() {
        return orderByDirection;
    }


    public String getDimensionName() {
        return dimensionName;
    }

    public String getInformationName() {
        return informationName;
    }
}
