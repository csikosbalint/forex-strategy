package hu.fnf.devel.forex.database;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the Orders database table.
 * 
 */
@Entity
@Table(name="Orders")
@NamedQuery(name="Order.findAll", query="SELECT o FROM Order o")
public class Order implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(insertable=false, updatable=false)
	private int id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date close;

	private String laststate;

	private int orderid;

	private String period;

	private float profit;

	@Temporal(TemporalType.TIMESTAMP)
	private Date start;

	//bi-directional many-to-one association to Strategy
	@ManyToOne
	@JoinColumn(name="strategyid")
	private Strategy strategy;

	public Order() {
	}

	private int getId() {
		return this.id;
	}

	private void setId(int id) {
		this.id = id;
	}

	public Date getClose() {
		return this.close;
	}

	public void setClose(Date close) {
		this.close = close;
	}

	public String getLaststate() {
		return this.laststate;
	}

	public void setLaststate(String laststate) {
		this.laststate = laststate;
	}

	public int getOrderid() {
		return this.orderid;
	}

	public void setOrderid(int orderid) {
		this.orderid = orderid;
	}

	public String getPeriod() {
		return this.period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public float getProfit() {
		return this.profit;
	}

	public void setProfit(float profit) {
		this.profit = profit;
	}

	public Date getStart() {
		return this.start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Strategy getStrategy() {
		return this.strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

}