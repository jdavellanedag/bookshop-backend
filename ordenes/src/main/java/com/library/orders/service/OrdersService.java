package com.library.orders.service;

import com.library.orders.model.db.Order;
import com.library.orders.model.request.OrderRequest;
import java.util.List;

public interface OrdersService {
	
	Order createOrder(OrderRequest request);

	Order getOrder(String id);

	List<Order> getOrders();

}
