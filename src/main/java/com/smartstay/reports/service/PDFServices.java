package com.smartstay.reports.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.smartstay.reports.config.FilesConfig;
import com.smartstay.reports.config.UploadFileToS3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;

@Service
public class PDFServices {
    private final TemplateEngine templateEngine;

    @Autowired
    private UploadFileToS3 uploadFileToS3;
    public PDFServices(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generatePdf(String invoiceId, String templateName, Context context) {
        String html = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            File pdfFile = FilesConfig.writePdf(outputStream.toByteArray(), "invoice-");
            return uploadFileToS3.uploadFileToS3(pdfFile, "invoices");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public String generateReceiptPdf(String templateName, Context context) {
        String html = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            File pdfFile = FilesConfig.writePdf(outputStream.toByteArray(), "receipts-");
            return uploadFileToS3.uploadFileToS3(pdfFile, "receipts");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public String generateTenantPdf(String templateName, Context context) {
        String html = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(
                    () -> Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream("fonts/ARIAL.ttf"),
                    "Arial"
            );
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            File pdfFile = FilesConfig.writePdf(outputStream.toByteArray(), "tenants-");
            return uploadFileToS3.uploadFileToS3(pdfFile, "tenants");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public String generateExpensesPdf(String templateName, Context context) {
        String html = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(
                    () -> Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream("fonts/ARIAL.ttf"),
                    "Arial"
            );
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            File pdfFile = FilesConfig.writePdf(outputStream.toByteArray(), "expenses-");
            return uploadFileToS3.uploadFileToS3(pdfFile, "expenses");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public String generateReceiptReportPDF(String templateName, Context context) {
        String html = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(
                    () -> Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream("fonts/ARIAL.ttf"),
                    "Arial"
            );
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            File pdfFile = FilesConfig.writePdf(outputStream.toByteArray(), "receipt-report-");
            return uploadFileToS3.uploadFileToS3(pdfFile, "receipts/reports");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public String generateInvoicePdf(String templateName, Context context) {
        String html = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(
                    () -> Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream("fonts/ARIAL.ttf"),
                    "Arial"
            );
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            File pdfFile = FilesConfig.writePdf(outputStream.toByteArray(), "invoice-report-");
            return uploadFileToS3.uploadFileToS3(pdfFile, "invoice/reports");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
