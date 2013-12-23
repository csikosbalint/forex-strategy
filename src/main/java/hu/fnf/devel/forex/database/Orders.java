package hu.fnf.devel.forex.database;

import hu.fnf.devel.forex.Main;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Period;

@NamedQueries({
	@NamedQuery(name="Order.findByCreationTime",
			query="SELECT o FROM Order o WHERE o.create = :getCreationTime")
})
public class Orders {
	private static final Logger logger = Logger.getLogger(Orders.class);
	private static final EntityManagerFactory entityManagerFactory = Persistence
			.createEntityManagerFactory("forex-strategy");
	private static final EntityManager em = entityManagerFactory.createEntityManager();
	public static final EntityTransaction userTransaction = em.getTransaction();

	private List<Order> orders = new ArrayList<Order>();
	private Order order;
	private Thread transaction;
	
	public Orders() {
		/*
		 * em.createQuery(
		 * "SELECT c FROM Customer c WHERE c.name LIKE :custName")
		 * .setParameter("custName", name) .setMaxResults(10) .getResultList();
		 * }
		 */
	}
	
	public static Order getOrderByCreation(long creation) {
		Order ret = ((Order) em.createNamedQuery("Order.findByCreationTime").setParameter("getCreationTime",
				creation));
		return ret;
	}
	
	public List<Order> getOrders() {
		return orders;
	}

	public void pushOrder(IOrder o, Period p) {
		Strategy s = new Strategy();
		s.setName(o.getComment());

		order = new Order();
		order.setCreate(o.getCreationTime());
		order.setStrategy(s);
		order.setPeriod(p.name());
		order.setLaststate(o.getState().name());

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
					logger.info("Order successfully recorded to database.");
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
		long creation = order.getCreationTime();
		Order order_memory = null;
		Order order_database = null;
		/*
		 * searching in memory
		 */
		Iterator<Order> it = orders.iterator();
		while (it.hasNext()) {
			order_memory = it.next();
			if (order_memory.getCreate() != creation) {
				order_memory = null;
			} else {
				break;
			}
		}

		if (order_memory != null) {
			/*
			 * memory remove
			 */
			orders.remove(orders.indexOf(order_memory));
			logger.debug("Order ctime:" + order_memory.getCreate() + " removed from memory");
			/*
			 * database remove
			 */
			try {
				userTransaction.begin();
				order_database = getOrderByCreation(creation);
				order_database.setClose(order.getCloseTime());
				userTransaction.commit();
			} catch (Exception e) {
				logger.error("Cannot update order ctime:" + order_memory.getCreate() + " in database", e);
				return null;
			}
		} else {
			logger.error("Order ctime:" + creation + " cannot be found in memory!");
			logger.debug("Memory contains:");
			Iterator<Order> ite = orders.iterator();
			while (ite.hasNext()) {
				Order o = ite.next();
				logger.debug("\t" + o.getCreate() + "(#" + o.getOrderid() + ")");
			}
		}
		return order_memory;
	}
	
	public void updateOrderState(long creation, IOrder.State state) {
		/*
		 * searching in memory
		 */
		Order ret = null;
		Iterator<Order> it = orders.iterator();
		while (it.hasNext()) {
			ret = it.next();
			if (ret.getCreate() != creation) {
				ret = null;
			} else {
				break;
			}
		}
		if (ret != null) {
			/*
			 * memory
			 */
			ret.setLaststate(state.name());
			userTransaction.begin();
		}
	}
}
