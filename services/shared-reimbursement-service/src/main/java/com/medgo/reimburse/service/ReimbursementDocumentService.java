package com.medgo.reimburse.service;

import com.medgo.reimburse.domain.dto.request.DocumentUploadRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReimbursementDocumentService {

    public List<DocumentUploadRequest> buildDocumentUploads(
            String serviceType,
            String natureOfClaim,
            List<MultipartFile> serviceInvoice,
            List<MultipartFile> itemizedBreakdown,
            List<MultipartFile> medcert,
            List<MultipartFile> operativeTechnique,
            List<MultipartFile> hospitalStatement,
            List<MultipartFile> certificateOfLiveBirth,
            List<MultipartFile> liveBirth,
            List<MultipartFile> doctorPrescription,
            List<MultipartFile> irDocument,
            List<MultipartFile> irDocumentAccredited,
            List<MultipartFile> bankDocuments) {

        List<DocumentUploadRequest> documentUploads = new ArrayList<>();
        boolean isInpatient = "Inpatient".equalsIgnoreCase(serviceType);

        // Common documents for both Inpatient and Outpatient
        if (serviceInvoice != null && !serviceInvoice.isEmpty()) {
            for (MultipartFile file : serviceInvoice) {
                if (file != null && !file.isEmpty()) {
                    documentUploads.add(DocumentUploadRequest.builder()
                            .documentType("Service Invoice / Official Receipt")
                            .file(file)
                            .build());
                }
            }
        }

        if (itemizedBreakdown != null && !itemizedBreakdown.isEmpty()) {
            for (MultipartFile file : itemizedBreakdown) {
                if (file != null && !file.isEmpty()) {
                    documentUploads.add(DocumentUploadRequest.builder()
                            .documentType("Itemized Breakdown of Charges")
                            .file(file)
                            .build());
                }
            }
        }

        if (medcert != null && !medcert.isEmpty()) {
            for (MultipartFile file : medcert) {
                if (file != null && !file.isEmpty()) {
                    documentUploads.add(DocumentUploadRequest.builder()
                            .documentType("Medcert/Document Indicating Diagnosis")
                            .file(file)
                            .build());
                }
            }
        }

        if (certificateOfLiveBirth != null && !certificateOfLiveBirth.isEmpty()) {
            for (MultipartFile file : certificateOfLiveBirth) {
                if (file != null && !file.isEmpty()) {
                    documentUploads.add(DocumentUploadRequest.builder()
                            .documentType("Certificate of Live Birth")
                            .file(file)
                            .build());
                }
            }
        }

        if (liveBirth != null && !liveBirth.isEmpty()) {
            for (MultipartFile file : liveBirth) {
                if (file != null && !file.isEmpty()) {
                    documentUploads.add(DocumentUploadRequest.builder()
                            .documentType("Live Birth")
                            .file(file)
                            .build());
                }
            }
        }

        // Inpatient-specific documents
        if (isInpatient) {
            if (hospitalStatement != null && !hospitalStatement.isEmpty()) {
                for (MultipartFile file : hospitalStatement) {
                    if (file != null && !file.isEmpty()) {
                        documentUploads.add(DocumentUploadRequest.builder()
                                .documentType("Hospital Statement of Account")
                                .file(file)
                                .build());
                    }
                }
            }

            // Operative Technique for inpatient surgical cases
            if (operativeTechnique != null && !operativeTechnique.isEmpty()) {
                String claim = natureOfClaim == null ? "" : natureOfClaim.trim().toLowerCase();
                if ("surgical".equals(claim)) {
                    for (MultipartFile file : operativeTechnique) {
                        if (file != null && !file.isEmpty()) {
                            documentUploads.add(DocumentUploadRequest.builder()
                                    .documentType("Operative Technique")
                                    .file(file)
                                    .build());
                        }
                    }
                }
            }
        }

        // Outpatient-specific: Operative Technique (for procedure cases only, NOT for laboratory)
        if (!isInpatient && operativeTechnique != null && !operativeTechnique.isEmpty()) {
            String claim = natureOfClaim == null ? "" : natureOfClaim.trim().toLowerCase();
            // Only add Operative Technique for procedure cases, not for laboratory/diagnostics
            if ("procedure".equals(claim) || "outpatient procedure".equals(claim)) {
                for (MultipartFile file : operativeTechnique) {
                    if (file != null && !file.isEmpty()) {
                        documentUploads.add(DocumentUploadRequest.builder()
                                .documentType("Operative Technique")
                                .file(file)
                                .build());
                    }
                }
            }
        }

        // Outpatient-specific: Hospital Statement of Account for emergency confinement/confinement
        if (!isInpatient && hospitalStatement != null && !hospitalStatement.isEmpty()) {
            String claim = natureOfClaim == null ? "" : natureOfClaim.trim().toLowerCase();
            if ("emergency confinement".equals(claim) || "confinement".equals(claim)) {
                for (MultipartFile file : hospitalStatement) {
                    if (file != null && !file.isEmpty()) {
                        documentUploads.add(DocumentUploadRequest.builder()
                                .documentType("Hospital Statement of Account")
                                .file(file)
                                .build());
                    }
                }
            }
        }

        if (doctorPrescription != null && !doctorPrescription.isEmpty()) {
            for (MultipartFile file : doctorPrescription) {
                if (file != null && !file.isEmpty()) {
                    documentUploads.add(DocumentUploadRequest.builder()
                            .documentType("Doctor's Prescription")
                            .file(file)
                            .build());
                }
            }
        }

        // IR Document - document type varies based on service type and nature of claim
        if (irDocument != null && !irDocument.isEmpty()) {
            String irDocumentType = getIRDocumentType(serviceType, natureOfClaim);
            for (MultipartFile file : irDocument) {
                if (file != null && !file.isEmpty()) {
                    documentUploads.add(DocumentUploadRequest.builder()
                            .documentType(irDocumentType)
                            .file(file)
                            .build());
                    log.debug("Added IR document to upload list. File name: {}, size: {}, type: {}",
                            file.getOriginalFilename(), file.getSize(), irDocumentType);
                }
            }
        } else {
            log.debug("IR document is null or empty. Will be validated as required document if needed.");
        }

        // IR Document for Accredited Hospitals/Doctors/Physicians - conditional requirement
        if (irDocumentAccredited != null && !irDocumentAccredited.isEmpty()) {
            String irAccreditedDocumentType = getIRAccreditedDocumentType(serviceType, natureOfClaim);
            for (MultipartFile file : irDocumentAccredited) {
                if (file != null && !file.isEmpty()) {
                    documentUploads.add(DocumentUploadRequest.builder()
                            .documentType(irAccreditedDocumentType)
                            .file(file)
                            .build());
                    log.debug("Added IR Accredited document to upload list. File name: {}, size: {}, type: {}",
                            file.getOriginalFilename(), file.getSize(), irAccreditedDocumentType);
                }
            }
        } else {
            log.debug("IR Accredited document is null or empty. Will be validated as required document if needed.");
        }

        // Bank Documents (for both types) - single file only; multiple files rejected by controller validation
        if (bankDocuments != null && bankDocuments.size() == 1) {
            MultipartFile file = bankDocuments.get(0);
            if (file != null && !file.isEmpty()) {
                documentUploads.add(DocumentUploadRequest.builder()
                        .documentType("Bank Documents")
                        .file(file)
                        .build());
            }
        }

        return documentUploads;
    }


    public void validateRequiredDocuments(String serviceType, String natureOfClaim, List<DocumentUploadRequest> uploads) {
        List<String> requiredDocs = getRequiredDocs(serviceType, natureOfClaim);

        // Normalize both required and present document types for comparison
        List<String> present = uploads.stream()
                .map(DocumentUploadRequest::getDocumentType)
                .map(String::trim)
                .toList();

        List<String> normalizedRequired = requiredDocs.stream()
                .map(String::trim)
                .toList();

        List<String> missing = new ArrayList<>();
        for (String req : normalizedRequired) {
            // Special handling for IR documents - check if any IR document type is present
            if (req.contains("IR") || req.contains("ir")) {
                boolean hasIRDocument = present.stream()
                        .anyMatch(doc -> doc != null && (doc.contains("IR") || doc.contains("ir")));
                if (!hasIRDocument) {
                    missing.add(req);
                    log.warn("Missing required IR document: '{}'. Present documents: {}", req, present);
                }
            } else {
                if (!present.contains(req)) {
                    missing.add(req);
                    log.warn("Missing required document: '{}'. Present documents: {}", req, present);
                }
            }
        }

        if (!missing.isEmpty()) {
            String errorMessage = String.format(
                    "Missing required documents: %s. ServiceType: %s, NatureOfClaim: %s, Present documents: %s",
                    String.join(", ", missing), serviceType, natureOfClaim, present);
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        log.debug("Document validation passed. All required documents are present.");
    }

    private List<String> getRequiredDocs(String serviceType, String natureOfClaim) {
        List<String> required = new ArrayList<>();
        boolean inpatient = "Inpatient".equalsIgnoreCase(serviceType);
        boolean outpatient = "Outpatient".equalsIgnoreCase(serviceType);

        // Standard required for all
        required.add("Service Invoice / Official Receipt");
        required.add("Itemized Breakdown of Charges");
        required.add("Medcert/Document Indicating Diagnosis");
        required.add("Bank Documents");

        String claim = natureOfClaim == null ? "" : natureOfClaim.trim().toLowerCase();

        if (inpatient) {
            switch (claim) {
                case "maternity" -> {
                    required.add("Certificate of Live Birth");
                    required.add("Hospital Statement of Account");
                    // Live Birth document is optional for maternity
                    // No IR required for Maternity
                }
                case "surgical" -> {
                    required.add("Operative Technique");
                    required.add("Hospital Statement of Account");
                    required.add("IR why confinement was not covered by MediCard");
                    // IR Document Accredited is optional for surgical
                }
                case "emergency confinement" -> {
                    required.add("Hospital Statement of Account");
                    required.add("IR why confinement was not covered by MediCard");
                    // IR Document Accredited is optional for emergency confinement/confinement
                }
                case "confinement medicine" -> {
                    required.add("Doctor's Prescription");
                    // No IR required
                }
                default -> {
                    // no additional
                }
            }
        }

        if (outpatient) {
            switch (claim) {
                case "outpatient medicine" -> {
                    required.add("Doctor's Prescription");
                    // No IR required
                }
                case "emergency confinement" -> {
                    required.add("Hospital Statement of Account");
                    required.add("IR why confinement was not covered by MediCard");
                    // IR Document Accredited is optional for emergency confinement/confinement
                }
                case "consultation" -> {
                    required.add("IR why consultation was not covered by MediCard");
                    // IR Document Accredited is optional for consultation
                }
                case "dental" -> {
                    required.add("IR why dental treatment was not covered by MediCard");
                    // IR Document Accredited is optional for dental
                }
                case "procedure", "outpatient procedure" -> {
                    required.add("Operative Technique");
                    required.add("IR why availment was not covered by MediCard");
                    required.add("IR is required if done in Accredited Hospitals/Accredited Physician");
                }
                case "laboratory/diagnostics" -> {
                    // Operative Technique removed for outpatient laboratory cases
                    required.add("IR why availment was not covered by MediCard");
                    // IR Document Accredited is optional for laboratory/diagnostics
                }
                case "optical", "therapy", "vaccine" -> {
                    // No additional documents required - standard set only
                }
                default -> {
                    // Optical, Therapy, Vaccine, others use standard set only
                }
            }
        }

        return required;
    }

    /**
     * Get the correct IR document type based on service type and nature of claim
     */
    private String getIRDocumentType(String serviceType, String natureOfClaim) {
        if (natureOfClaim == null) {
            return "IR Document"; // Default generic type
        }

        String claim = natureOfClaim.trim().toLowerCase();
        boolean inpatient = "Inpatient".equalsIgnoreCase(serviceType);
        boolean outpatient = "Outpatient".equalsIgnoreCase(serviceType);

        if (inpatient) {
            if ("surgical".equals(claim)) {
                return "IR why confinement was not covered by MediCard";
            }
        }

        if (outpatient) {
            if ("consultation".equals(claim)) {
                return "IR why consultation was not covered by MediCard";
            } else if ("dental".equals(claim)) {
                return "IR why dental treatment was not covered by MediCard";
            } else if ("emergency confinement".equals(claim) || "confinement".equals(claim)) {
                return "IR why confinement was not covered by MediCard";
            } else if ("procedure".equals(claim) || "outpatient procedure".equals(claim)) {
                return "IR why availment was not covered by MediCard";
            } else if (claim.contains("laboratory") || claim.contains("diagnostics")) {
                return "IR why availment was not covered by MediCard";
            }
        }

        // Default fallback
        return "IR Document";
    }

    /**
     * Get the IR document type for accredited hospitals/doctors/physicians
     */
    private String getIRAccreditedDocumentType(String serviceType, String natureOfClaim) {
        if (natureOfClaim == null) {
            return "IR is required if done in Accredited Hospitals";
        }

        String claim = natureOfClaim.trim().toLowerCase();
        boolean inpatient = "Inpatient".equalsIgnoreCase(serviceType);
        boolean outpatient = "Outpatient".equalsIgnoreCase(serviceType);

        if (inpatient) {
            if ("surgical".equals(claim) || claim.contains("confinement")) {
                return "IR is required if done in Accredited Hospitals";
            }
        }

        if (outpatient) {
            if ("consultation".equals(claim)) {
                return "IR is required if done in Accredited Hospitals";
            } else if ("dental".equals(claim)) {
                return "IR is required if done in Accredited Hospitals/Accredited Doctor";
            } else if ("emergency confinement".equals(claim) || "confinement".equals(claim)) {
                return "IR is required if done in Accredited Hospitals";
            } else if ("procedure".equals(claim) || "outpatient procedure".equals(claim)
                    || claim.contains("laboratory") || claim.contains("diagnostics")) {
                return "IR is required if done in Accredited Hospitals/Accredited Physician";
            }
        }

        // Default fallback
        return "IR is required if done in Accredited Hospitals";
    }
}
