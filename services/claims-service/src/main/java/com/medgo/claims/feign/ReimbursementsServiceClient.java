package com.medgo.claims.feign;

import com.medgo.claims.config.FeignClientConfig;
import com.medgo.claims.config.FeignMultipartSupportConfig;
import com.medgo.commons.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "shared-reimbursement-service",
        url = "${shared.reimbursement.service.url}",
        configuration = {FeignClientConfig.class, FeignMultipartSupportConfig.class})
public interface ReimbursementsServiceClient {
    @GetMapping("/reimbursement/v1/history")
    CommonResponse getReimbursementHistory(@RequestParam(name = "memberCode") String memberCode);
    
    @PostMapping("/reimbursement/v1/viewAmount")
    CommonResponse getRequestDetails(@RequestBody Map<String, String> request);
    
    @PostMapping(value = "/reimbursement/v1/submitReimbursement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CommonResponse submitReimbursement(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "serviceInvoice", required = false) List<MultipartFile> serviceInvoice,
            @RequestPart(value = "itemizedBreakdown", required = false) List<MultipartFile> itemizedBreakdown,
            @RequestPart(value = "medcert", required = false) List<MultipartFile> medcert,
            @RequestPart(value = "operativeTechnique", required = false) List<MultipartFile> operativeTechnique,
            @RequestPart(value = "hospitalStatement", required = false) List<MultipartFile> hospitalStatement,
            @RequestPart(value = "certificateOfLiveBirth", required = false) List<MultipartFile> certificateOfLiveBirth,
            @RequestPart(value = "liveBirth", required = false) List<MultipartFile> liveBirth,
            @RequestPart(value = "doctorPrescription", required = false) List<MultipartFile> doctorPrescription,
            @RequestPart(value = "irDocument", required = false) List<MultipartFile> irDocument,
            @RequestPart(value = "irDocumentAccredited", required = false) List<MultipartFile> irDocumentAccredited,
            @RequestPart(value = "bankDocuments", required = false) MultipartFile bankDocuments);


    @PostMapping(value = "/reimbursement/v1/resubmitReimbursement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CommonResponse resubmitReimbursement(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "serviceInvoice", required = false) List<MultipartFile> serviceInvoice,
            @RequestPart(value = "itemizedBreakdown", required = false) List<MultipartFile> itemizedBreakdown,
            @RequestPart(value = "medcert", required = false) List<MultipartFile> medcert,
            @RequestPart(value = "operativeTechnique", required = false) List<MultipartFile> operativeTechnique,
            @RequestPart(value = "hospitalStatement", required = false) List<MultipartFile> hospitalStatement,
            @RequestPart(value = "certificateOfLiveBirth", required = false) List<MultipartFile> certificateOfLiveBirth,
            @RequestPart(value = "liveBirth", required = false) List<MultipartFile> liveBirth,
            @RequestPart(value = "doctorPrescription", required = false) List<MultipartFile> doctorPrescription,
            @RequestPart(value = "irDocument", required = false) List<MultipartFile> irDocument,
            @RequestPart(value = "irDocumentAccredited", required = false) List<MultipartFile> irDocumentAccredited,
            @RequestPart(value = "bankDocuments", required = false) MultipartFile bankDocuments);
    

}


