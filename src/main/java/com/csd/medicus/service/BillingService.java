package com.csd.medicus.service;

import com.csd.medicus.model.Billing;
import java.util.List;

public interface BillingService {
	Billing generateBill(Billing b);

	List<Billing> listForPatient(Long patientId);

	void markPaid(Long id);
}
