package com.telelanguage.tlvx.model;

import javax.persistence.Entity;
import javax.persistence.Table;

public class CustomerUser {
	private String id;
	private Customer customer;
	private boolean authorized;
	private String email;
	private String passwordHash;
	private String loginToken;
	private String phone;
}