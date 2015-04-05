/**
 * Employee Data fields
 */
package com.android.teamspace.models;

public class Employee {
	// Unique Id for the employee
	private String id;

	// Employee Name
	private String name;

	// Employee number
	private String number;

	public Employee() {
	}

	@Override
	public String toString() {
		return this.getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
