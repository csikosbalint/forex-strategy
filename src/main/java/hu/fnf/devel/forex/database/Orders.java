package hu.fnf.devel.forex.database;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.utils.MarketWebProxy;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Period;

public class Orders {
	private static final Logger logger = Logger.getLogger(Orders.class);
	private static final EntityManagerFactory entityManagerFactory = Persistence
			.createEntityManagerFactory("forex-strategy");
	private static final EntityManager em = entityManagerFactory.createEntityManager();
	private static final EntityTransaction userTransaction = em.getTransaction();

	private List<Order> orders = new ArrayList<Order>();
	private Order order;
	private Thread transaction;

	public List<Order> getOrders() {
		return orders;
	}

	public void pushOrder(IOrder o, Period p) {
		Strategy s = new Strategy();
		s.setName(o.getComment());

		order = new Order();
		order.setStrategy(s);
		order.setPeriod(p.name());
		order.setLaststate(o.getState().name());
		order.setStart(new Date(o.getCreationTime()));
		try {
			order.setAddress(Inet4Address.getLocalHost().getCanonicalHostName());
		} catch (UnknownHostException e1) {
			logger.error("Cannot determine host IP address for order!");
			order.setAddress("Cannot determine host IP address for order!");
		}
		transaction = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					userTransaction.begin();
					em.persist(order);
					userTransaction.commit();
					logger.info("Order successfully recorded to the database.");
				} catch (Exception e) {
					logger.error("Order data cannotbe recorded to the database.Trying to send order data via mail.", e);
					Main.sendMail("Unrecorded Order!", order.toString(), Main.MASTER);
				}
			}
		});
		orders.add(order);
		transaction.start();
	}

	public Order popOrder(IOrder order) {
		Order o = (Order) em.createNamedQuery("Order.findByCreationTime").setParameter("creationTime",
				order.getCreationTime());
		Order ret = orders.get(orders.indexOf(o));
		orders.remove(orders.indexOf(o));
		return ret;
	}

}
