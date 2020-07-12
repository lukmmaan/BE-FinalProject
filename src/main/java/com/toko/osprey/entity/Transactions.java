package com.toko.osprey.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;


import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Transactions {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private int totalPrice;
	private String BuktiTrf;
	private String status;
	private String tanggalBeli;
	private String tanggalAcc;
	private String jasaPengiriman;
	private String statusPengiriman;

	@ManyToOne(cascade = {CascadeType.DETACH,CascadeType.PERSIST,CascadeType.REFRESH})
	@JoinColumn(name = "user_id")
	private User user;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="transactions",cascade = CascadeType.ALL)
	private List<TransactionsDetails> transactionsDetails;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getBuktiTrf() {
		return BuktiTrf;
	}

	public void setBuktiTrf(String buktiTrf) {
		BuktiTrf = buktiTrf;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTanggalBeli() {
		return tanggalBeli;
	}

	public void setTanggalBeli(String tanggalBeli) {
		this.tanggalBeli = tanggalBeli;
	}

	public String getTanggalAcc() {
		return tanggalAcc;
	}

	public void setTanggalAcc(String tanggalAcc) {
		this.tanggalAcc = tanggalAcc;
	}

	public String getJasaPengiriman() {
		return jasaPengiriman;
	}

	public void setJasaPengiriman(String jasaPengiriman) {
		this.jasaPengiriman = jasaPengiriman;
	}

	public String getStatusPengiriman() {
		return statusPengiriman;
	}

	public void setStatusPengiriman(String statusPengiriman) {
		this.statusPengiriman = statusPengiriman;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<TransactionsDetails> getTransactionsDetails() {
		return transactionsDetails;
	}

	public void setTransactionsDetails(List<TransactionsDetails> transactionsDetails) {
		this.transactionsDetails = transactionsDetails;
	}
	
	
}
