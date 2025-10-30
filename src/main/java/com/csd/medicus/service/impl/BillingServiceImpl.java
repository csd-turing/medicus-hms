package com.csd.medicus.service.impl;
import com.csd.medicus.model.Billing; import com.csd.medicus.repository.BillingRepository; import com.csd.medicus.repository.PatientRepository; import com.csd.medicus.service.BillingService; import org.springframework.stereotype.Service; import java.time.LocalDateTime; import java.util.List; import java.util.stream.Collectors;
@Service public class BillingServiceImpl implements BillingService {
    private final BillingRepository billingRepo; private final PatientRepository patientRepo;
    public BillingServiceImpl(BillingRepository billingRepo, PatientRepository patientRepo){ this.billingRepo = billingRepo; this.patientRepo = patientRepo; }
    public Billing generateBill(Billing b){ if (b.getAmount()==null) throw new IllegalArgumentException("Amount required"); b.setIssuedAt(LocalDateTime.now()); b.setPaid(false); return billingRepo.save(b); }
    public List<Billing> listForPatient(Long patientId){ return billingRepo.findAll().stream().filter(x->x.getPatientId().equals(patientId)).collect(Collectors.toList()); }
    public void markPaid(Long id){ Billing b = billingRepo.findById(id).orElseThrow(()-> new RuntimeException("Bill not found")); b.setPaid(true); billingRepo.save(b); }
}
