package com.csd.medicus;

import com.csd.medicus.model.Billing;
import com.csd.medicus.model.Patient;
import com.csd.medicus.repository.BillingRepository;
import com.csd.medicus.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BillingIntegrationTest {
	@Autowired
	private BillingRepository brepo;
	@Autowired
	private PatientRepository prepo;

	@Test
	void createBillForPatient() {
		Patient p = new Patient();
		p.setFirstName("T");
		p.setLastName("U");
		p.setEmail("t@u.com");
		Patient saved = prepo.save(p);
		Billing b = new Billing();
		b.setPatientId(saved.getId());
		b.setAmount(BigDecimal.valueOf(150));
		b.setPaid(false);
		Billing sb = brepo.save(b);
		assertThat(sb.getId()).isNotNull();
		assertThat(brepo.findAll()).isNotEmpty();
	}
}
