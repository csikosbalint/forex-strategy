package hu.fnf.devel.forex.database;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the Strategies database table.
 * 
 */
@Entity
@Table(name="Strategies")
@NamedQuery(name="Strategy.findAll", query="SELECT s FROM Strategy s")
public class Strategy implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(insertable=false, updatable=false)
	private int id;

	private String name;

	//bi-directional many-to-one association to Order
	@OneToMany(mappedBy="strategy")
	private List<Order> orders;

	public Strategy() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Order> getOrders() {
		return this.orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public Order addOrder(Order order) {
		getOrders().add(order);
		order.setStrategy(this);

		return order;
	}

	public Order removeOrder(Order order) {
		getOrders().remove(order);
		order.setStrategy(null);

		return order;
	}

}