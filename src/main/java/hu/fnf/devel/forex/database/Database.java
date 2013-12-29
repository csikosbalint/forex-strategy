package hu.fnf.devel.forex.database;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.utils.RobotException;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.dukascopy.api.IOrder;

public class Database {
	private static final Logger logger = Logger.getLogger(Database.class);
	private static final EntityManagerFactory entityManagerFactory = Persistence
			.createEntityManagerFactory("forex-strategy");
	private static final EntityManager em = entityManagerFactory.createEntityManager();
	public static final EntityTransaction userTransaction = em.getTransaction();

	private static List<Order> orders = new ArrayList<Order>();

	public static Order get(long id) {
		Order ret = null;
		/*
		 * 1. from memory
		 */
		Iterator<Order> it = orders.iterator();
		while (it.hasNext()) {
			Order order = it.next();
			if (order.getId() == id) {
				return order;
			}
		}
		/*
		 * 2. from database
		 */
		Query query = em.createNamedQuery("Order.findByCreationTime").setParameter("getCreationTime", id);
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		switch (result.size()) {
		case 1:
			ret = (Order) result.get(0);
			break;
		default:
			ret = null;
			break;
		}
		return ret;
	}

	public static void remove(long id) throws RobotException {
		Order order = get(id);
		/*
		 * 1. from memory
		 */
		if (orders.contains(order)) {
			orders.remove(order);
		}
		if (order.getStrategyname().equalsIgnoreCase("TestState")) {
			try {
				userTransaction.begin();
				em.remove(order);
				userTransaction.commit();
				logger.debug("Order ctime: " + order.getId() + " successfully removed from database.");
			} catch (Exception e) {
				logger.error("Order data cannot be removed at database.Trying to send order data via mail.", e);
				Main.sendMail("Unmodified Order!", order.toString());
				throw new RobotException(e);
			}
		}
	}
	public static void remove(IOrder iOrder) throws RobotException {
		/*
		 * update in database
		 */
		Order order = get(iOrder.getCreationTime());
		if (orders.contains(order)) {
			orders.remove(order);
		}
		order.setProfit(iOrder.getProfitLossInUSD());
		order.setLaststate(iOrder.getState().name());
		order.setClose(iOrder.getCloseTime());
		merge(order);
	}
	

	public static synchronized void merge(Order order) throws RobotException {
		if (orders.contains(order)) {
			logger.info("#" + order.getId() + "/" + order.getOrderid() + " order cannot be found in memory!");
		}
		try {
			userTransaction.begin();
			em.merge(order);
			userTransaction.commit();
			logger.debug("Order ctime: " + order.getId() + " successfully updated at database.");
		} catch (Exception e) {
			logger.error("Order data cannotbe upadted at database.Trying to send order data via mail.", e);
			Main.sendMail("Unmodified Order!", order.toString());
			throw new RobotException(e);
		}
	}

	public static synchronized void add(Order order) throws RobotException {
		Order o = get(order.getId());
		if (o != null && !orders.contains(o) ) {
			orders.add(o);
		} else {
			try {
				order.setAddress(Inet4Address.getLocalHost().getHostAddress());
			} catch (UnknownHostException e1) {
				logger.error("Cannot determine host IP address for order!");
				order.setAddress("0.0.0.0");
			}
			try {
				userTransaction.begin();
				em.persist(order);
				userTransaction.commit();
				logger.debug("Order ctime: " + order.getId() + " successfully added to database.");
			} catch (Exception e) {
				logger.error("Order data cannotbe recorded to the database.Trying to send order data via mail.", e);
				Main.sendMail("Unrecorded Order!", order.toString());
				throw new RobotException(e);
			}
		}
	}

	public static void add(IOrder iorder) throws RobotException {

		Order order = new Order();

		order.setId(iorder.getCreationTime());
		order.setPeriod(iorder.getLabel().split("AND")[1]);
		order.setLaststate(iorder.getState().name());
		order.setStrategyname(iorder.getLabel().split("AND")[0]);
		add(order);
	}
}
