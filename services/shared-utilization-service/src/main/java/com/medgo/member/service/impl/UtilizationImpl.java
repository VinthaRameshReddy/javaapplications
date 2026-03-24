package com.medgo.member.service.impl;

import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import com.medgo.member.constant.MemberConstants;
import com.medgo.member.domain.request.UtilizationRequest;
import com.medgo.member.domain.response.PdfResponse;
import com.medgo.member.domain.response.UtilizationResponse;
import com.medgo.member.repository.utilization.UtilizationLegacyRepository;
import com.medgo.member.service.Utilization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class UtilizationImpl implements Utilization {
    private static final Logger LOGGER = LoggerFactory.getLogger(UtilizationImpl.class);
    private final UtilizationLegacyRepository utilizationLegacyRepository;
    private final ObjectMapper objectMapper;

    public UtilizationImpl(UtilizationLegacyRepository utilizationLegacyRepository, ObjectMapper objectMapper) {
        this.utilizationLegacyRepository = utilizationLegacyRepository;
        this.objectMapper = objectMapper;
    }


    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public CommonResponse getUtilizationPdf(UtilizationRequest UtilizationRequest) {
        LOGGER.debug("Processing utilization PDF request: {}", UtilizationRequest);
        
        var periodResponse = handlePeriodType(UtilizationRequest);
        if (periodResponse != null) {
            LOGGER.info("****periodResponse***" + periodResponse);
            return periodResponse;
        }

        List<UtilizationResponse> dtoList = utilizationLegacyRepository.findUtilizationDataV6(UtilizationRequest);
        if (dtoList == null || dtoList.isEmpty()) {
            LOGGER.info("****dtoList***" + dtoList);
            return CommonResponse.error(
                    new ErrorResponse(400, MemberConstants.RECORDS),
                    400);
        }

        List<UtilizationResponse> processedList = postProcess(dtoList);

        byte[] pdfBytes;
        try {
            pdfBytes = generatePdf(processedList);
        } catch (IOException e) {
            return CommonResponse.error(
                    new ErrorResponse(400, MemberConstants.GENEREATINGPDF),
                    400);
        }

        String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
        String fileName = generatePdfFileName(UtilizationRequest, processedList);
        PdfResponse response = new PdfResponse(base64Pdf, fileName);
        return CommonResponse.success(response);
    }


    public byte[] generatePdf(List<UtilizationResponse> data) throws IOException {
        final String disclaimer = MemberConstants.DISCLAIMER;
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        final DateTimeFormatter availDateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        final float margin = 40f;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
            pdfDoc.setDefaultPageSize(PageSize.A4);
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new PageXofNEventHandler(disclaimer, 8f));

            Document document = new Document(pdfDoc);
            document.setMargins(margin, margin, margin, margin);

            Paragraph memberTitle = new Paragraph("MEMBER")
                    .setFontSize(15f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(memberTitle);

            // 2-column table for header pairs (Patient/Effectivity, Company/Validity, Period/empty)
            Table headerTable = new Table(new float[]{0.5f, 0.5f});  // Left/Right columns
            headerTable.setWidth(PageSize.A4.getWidth() - 2 * margin);
            headerTable.setBorder(Border.NO_BORDER);
            headerTable.setMarginTop(6f);  // Minimal vertical space above
            headerTable.setMarginBottom(6f);  // Minimal vertical space below

            // Row 1: Patient left, Effectivity right
            headerTable.addCell(new Cell()
                    .add(new Paragraph("Patient: " + (data != null && !data.isEmpty() ? data.get(0).getPatient() : "")).setFontSize(10f).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0f));
            headerTable.addCell(new Cell()
                    .add(new Paragraph("Effectivity: " + (data != null && !data.isEmpty() && data.get(0).getEffective() != null ? data.get(0).getEffective().toString() : "")).setFontSize(10f).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0f));

            // Row 2: Company left, Validity Date right
            headerTable.addCell(new Cell()
                    .add(new Paragraph("Company: " + (data != null && !data.isEmpty() ? data.get(0).getCompany() : "")).setFontSize(10f).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0f));
            headerTable.addCell(new Cell()
                    .add(new Paragraph("Validity Date: " + (data != null && !data.isEmpty() && data.get(0).getValid() != null ? data.get(0).getValid().toString() : "")).setFontSize(10f).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0f));

            // Row 3: Period left (full span), empty right
            headerTable.addCell(new Cell(1, 2)  // Span both columns for full width
                    .add(new Paragraph("Period: " +
                            (data != null && !data.isEmpty() && data.get(0).getPeriodFr() != null ? data.get(0).getPeriodFr().format(dateFormatter) : "") +
                            " - " +
                            (data != null && !data.isEmpty() && data.get(0).getPeriodTo() != null ? data.get(0).getPeriodTo().format(dateFormatter) : "")).setFontSize(10f).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0f));

            document.add(headerTable);

            Paragraph disclaimerAbove = new Paragraph(disclaimer).setFontSize(8f);
            disclaimerAbove.setMarginTop(5f);  // Minimal space to disclaimer
            document.add(disclaimerAbove);

            float[] colWidths = {0.14f, 0.14f, 0.18f, 0.18f, 0.18f, 0.08f};
            Table dataTable = new Table(colWidths);
            dataTable.setWidth(PageSize.A4.getWidth() - 2 * margin);

            String[] tableHeaders = {"Control No", "Availment Date", "Primary Diagnosis", "Remarks/Other Diagnosis", "Hospital Name / Doctor", "Approved"};
            for (String h : tableHeaders) {
                dataTable.addHeaderCell(createCell(h, 8f, false, TextAlignment.CENTER));
            }

            double totalApproved = 0;
            if (data != null && !data.isEmpty()) {
                for (UtilizationResponse row : data) {
                    dataTable.addCell(createCell(row.getControlCode() != null ? row.getControlCode() : "", 7f, false, TextAlignment.LEFT));
                    dataTable.addCell(createCell(row.getAvailFr() != null ? row.getAvailFr().format(availDateFormatter) : "", 7f, false, TextAlignment.LEFT));
                    dataTable.addCell(createCell(row.getDiagDesc() != null ? row.getDiagDesc() : "", 7f, false, TextAlignment.LEFT));
                    dataTable.addCell(createCell(row.getDxRem() != null ? row.getDxRem() : "", 7f, false, TextAlignment.LEFT));
                    String hospitalAndDoctor = row.getHospitalName() != null ? row.getHospitalName() : "";
                    if (row.getDoctorName() != null && !row.getDoctorName().isEmpty()) {
                        hospitalAndDoctor += " / " + row.getDoctorName();
                    }
                    dataTable.addCell(createCell(hospitalAndDoctor, 7f, false, TextAlignment.LEFT));
                    BigDecimal approved = row.getApproved() != null ? row.getApproved() : BigDecimal.ZERO;
                    dataTable.addCell(createCell(String.format("%.2f", approved), 7f, false, TextAlignment.RIGHT));
                    totalApproved += approved.doubleValue();
                }
            }

            Cell totalLabelCell = new Cell(1, 5)
                    .add(new Paragraph("TOTAL:")
                            .setFontSize(7f)
                            .setBold()
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER);

            dataTable.addCell(totalLabelCell);
            Cell totalAmountCell = createCell(String.format("%.2f", totalApproved), 7f, false, TextAlignment.RIGHT);
            dataTable.addCell(totalAmountCell);

            document.add(dataTable);
            document.close();
            return baos.toByteArray();
        }
    }

    private static class PageXofNEventHandler implements IEventHandler {
        private final String generatedBy;
        private final float smallFontSize;

        public PageXofNEventHandler(String disclaimer, float smallFontSize) {
            this.generatedBy = "Generated by System on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));
            this.smallFontSize = smallFontSize;
        }
        @Override
        public void handleEvent(com.itextpdf.kernel.events.Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);
            int totalPages = pdfDoc.getNumberOfPages();

            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(page);
            Canvas canvas = new Canvas(pdfCanvas, pdfDoc, pageSize);

            canvas.showTextAligned(new Paragraph(generatedBy).setFontSize(smallFontSize), pageSize.getWidth() - 40, 20, TextAlignment.RIGHT);
            String pageInfo = String.format("Page %d of %d", pageNumber, totalPages);
            canvas.showTextAligned(new Paragraph(pageInfo).setFontSize(smallFontSize), pageSize.getWidth() / 2, 20, TextAlignment.CENTER);
            canvas.close();
        }
    }

    private Cell createCell(String text, float fontSize, boolean bold, TextAlignment alignment) {
        Paragraph p = new Paragraph(text)
                .setFontSize(fontSize)
                .setTextAlignment(alignment);
        if (bold) p.setBold();
        return new Cell().add(p).setPadding(4f).setBorder(new SolidBorder(1.0f));
    }



    private String generatePdfFileName(UtilizationRequest request, List<UtilizationResponse> data) {
        String namePart = "Unknown";
        if (data != null && !data.isEmpty() && data.get(0).getPatient() != null) {
            namePart = data.get(0).getPatient()
                    .replaceAll("[,\\.\\s]", "")
                    .trim()
                    .toUpperCase();                // optional: ensure consistent uppercase
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String periodFr = request.getDateFr() != null ? request.getDateFr().format(formatter) : "Start";
        String periodTo = request.getDateTo() != null ? request.getDateTo().format(formatter) : "End";

        return String.format("MediCard_Utilization_%s_%s-%s.pdf", namePart, periodFr, periodTo);
    }


    CommonResponse handlePeriodType(UtilizationRequest utilizationRequest) {
        if (utilizationRequest == null) {
            LOGGER.error("UtilizationRequest is null");
            return CommonResponse.error(new ErrorResponse(400, "Request cannot be null"), 400);
        }

        if (utilizationRequest.getPeriodType() == null || utilizationRequest.getPeriodType().trim().isEmpty()) {
            LOGGER.warn("PeriodType is null or empty");
            return CommonResponse.error(new ErrorResponse(400, MemberConstants.PERIODTYPE), 400);
        }

        LocalDateTime now = LocalDateTime.now();
        switch (utilizationRequest.getPeriodType().toLowerCase()) {
            case "last12months" -> {
                utilizationRequest.setDateFr(now.minusYears(1));
                utilizationRequest.setDateTo(now);
            }
            case "2years" -> {
                utilizationRequest.setDateFr(now.minusYears(2));
                utilizationRequest.setDateTo(now);
            }
            case "3years" -> {
                utilizationRequest.setDateFr(now.minusYears(3));
                utilizationRequest.setDateTo(now);
            }
            case "4years" -> {
                utilizationRequest.setDateFr(now.minusYears(4));
                utilizationRequest.setDateTo(now);
            }
            case "5years" -> {
                utilizationRequest.setDateFr(now.minusYears(5));
                utilizationRequest.setDateTo(now);
            }

            case "custom" -> {
                if (utilizationRequest.getDateFr() == null || utilizationRequest.getDateTo() == null)
                    return CommonResponse.error(new ErrorResponse(400, MemberConstants.CUSTOM), 400);

                if (utilizationRequest.getDateFr().isAfter(utilizationRequest.getDateTo())) {
                    return CommonResponse.error(new ErrorResponse(400,  MemberConstants.FROMDATEANDTODATE), 400);
                }


                long yearsBetween = ChronoUnit.YEARS.between(utilizationRequest.getDateFr(), utilizationRequest.getDateTo());
                if (yearsBetween > 5) {
                    return CommonResponse.error(new ErrorResponse(400, MemberConstants.YEARS_BETWEEN), 400);
                }
            }
            default -> {
                return CommonResponse.error(new ErrorResponse(400, MemberConstants.INVALID_PERIOD), 400);
            }
        }
        return null;
    }





    public List<UtilizationResponse> postProcess(List<UtilizationResponse> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return dtoList;
        for (UtilizationResponse row : dtoList) {
            String rcvdBy = Optional.ofNullable(row.getRcvdBy()).orElse("");
            String checknum = Optional.ofNullable(row.getChecknum()).orElse("");
            String pf = Optional.ofNullable(row.getPf()).orElse("");
            String controlCode = Optional.ofNullable(row.getControlCode()).orElse("");

            boolean rcvdByMedicomsOrClinic = "Medicoms".equalsIgnoreCase(rcvdBy) || "CLINIC".equalsIgnoreCase(rcvdBy);
            boolean checknumIsUploadOrUPLOAD = "Upload".equalsIgnoreCase(checknum);
            boolean controlCodeIsNotMEDICINE = !"MEDICINE".equalsIgnoreCase(controlCode);

            if ((rcvdByMedicomsOrClinic || checknumIsUploadOrUPLOAD) && controlCodeIsNotMEDICINE) {
                controlCode = "UPLOADED-" + controlCode;
            }

            boolean checknumIsEmptyOrSpaceOrUpload = checknum.trim().isEmpty() || "Upload".equalsIgnoreCase(checknum) || "null".equalsIgnoreCase(checknum);
            if (!checknumIsEmptyOrSpaceOrUpload) {
                controlCode = "*" + controlCode;
            }

            boolean pfMatches = "1".equals(pf)
                    || "ADDITIONAL PF".equalsIgnoreCase(pf)
                    || "ADVANCE PAYMENT - PF".equalsIgnoreCase(pf)
                    || "PF ONLY".equalsIgnoreCase(pf);
            if (pfMatches) {
                controlCode = "*" + controlCode;
            }

            row.setControlCode(controlCode);
        }
        return dtoList;
    }

}

