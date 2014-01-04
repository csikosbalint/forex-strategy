package hu.fnf.devel.forex.database;

import java.io.Serializable;
import javax.persistence.*;


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
	@Column(updatable=false)
	private long id;

	@Column(updatable=false)
	private String address;

	private long close;

	private String laststate;

	private String orderid;

	private String period;

	private double profit;

	private String strategyname;

	public Order() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public long getClose() {
		return this.close;
	}

	public void setClose(long close) {
		this.close = close;
	}

	public String getLaststate() {
		return this.laststate;
	}

	public void setLaststate(String laststate) {
		this.laststate = laststate;
	}

	public String getOrderid() {
		return this.orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getPeriod() {
		return this.period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public double getProfit() {
		return this.profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public String getStrategyname() {
		return this.strategyname;
	}

	public void setStrategyname(String strategyname) {
		this.strategyname = strategyname;
	}

}