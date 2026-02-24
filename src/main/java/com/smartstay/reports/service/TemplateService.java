package com.smartstay.reports.service;

import com.smartstay.reports.dao.BillTemplateType;
import com.smartstay.reports.dao.BillTemplates;
import com.smartstay.reports.ennum.BillConfigTypes;
import com.smartstay.reports.ennum.InvoiceType;
import com.smartstay.reports.repositories.BillTemplateRepository;
import com.smartstay.reports.responses.hostel.TemplateInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {
    @Autowired
    private BillTemplateRepository billTemplateRepository;

    public TemplateInfo getTemplateDetails(String hostelId, String invoiceType) {
        BillTemplates billTemplates = billTemplateRepository.getByHostelId(hostelId);
        if (billTemplates != null) {
            BillTemplateType billTemplateType = null;
            if (invoiceType.equalsIgnoreCase(InvoiceType.RENT.name()) || invoiceType.equalsIgnoreCase(InvoiceType.SETTLEMENT.name()) ||  invoiceType.equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {
                billTemplateType = billTemplates
                        .getTemplateTypes()
                        .stream()
                        .filter(i -> i.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                        .findFirst()
                        .orElse(null);
            } else if (invoiceType.equalsIgnoreCase(InvoiceType.BOOKING.name()) || invoiceType.equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
                billTemplateType = billTemplates
                        .getTemplateTypes()
                        .stream()
                        .filter(i -> i.getInvoiceType().equalsIgnoreCase(BillConfigTypes.ADVANCE.name()))
                        .findFirst()
                        .orElse(null);
            }
            if (billTemplateType == null) {
                return null;
            }
            String hexColor = null;
            String colorCode = billTemplateType.getInvoiceTemplateColor();
            String colorValue = null;
            if (colorCode.startsWith("rgba")) {
                colorValue = colorCode.substring(5, colorCode.length()-1);
            }
            else if (colorCode.startsWith("rgb(")){
                colorValue = colorCode.substring(4, colorCode.length()-1);
            }
            else if (colorCode.startsWith("#")) {
                hexColor = colorCode;
            }
            if (colorValue != null) {
                String[] colorArr = colorValue.replaceAll(" ", "")
                        .split(",");

                int r = Integer.parseInt(colorArr[0].trim());
                int g = Integer.parseInt(colorArr[1].trim());
                int b = Integer.parseInt(colorArr[2].trim());

                hexColor = String.format("#%02X%02X%02X", r, g, b);
            }


            String hostelLogo = null;
            String hostelPhone = null;
            String hostelEmailId = null;
            String termsAndCondition = null;
            String signatureUrl = null;
            String notes = null;
            String qrCode = null;

            if (billTemplates.isLogoCustomized()) {
                hostelLogo = billTemplateType.getInvoiceLogoUrl();
            } else {
                hostelLogo = billTemplates.getHostelLogo();
            }

            if (hostelLogo == null) {
                hostelLogo = "https://smartstaydevs.s3.ap-south-1.amazonaws.com/smartstay/smartstay.png";
            }

            if (billTemplates.isMobileCustomized()) {
                hostelPhone = billTemplateType.getInvoicePhoneNumber();
            } else {
                hostelPhone = billTemplates.getMobile();
            }

            if (billTemplates.isEmailCustomized()) {
                hostelEmailId = billTemplateType.getInvoiceMailId();
            } else {
                hostelEmailId = billTemplates.getEmailId();
            }

            if (billTemplates.isSignatureCustomized()) {
                signatureUrl = billTemplateType.getInvoiceSignatureUrl();
            }
            else {
                signatureUrl = billTemplates.getDigitalSignature();
            }

            if (billTemplateType.getQrCode() != null) {
                qrCode = billTemplateType.getQrCode();
            }

            termsAndCondition = billTemplateType.getInvoiceTermsAndCondition();
            notes = billTemplateType.getInvoiceNotes();

            return new TemplateInfo(hexColor,
                    hostelLogo,
                    qrCode,
                    hostelPhone,
                    hostelEmailId,
                    signatureUrl,
                    termsAndCondition,
                    notes);
        }

        return null;
    }

    public TemplateInfo getReceiptTemplate(String hostelId) {
        BillTemplates billTemplates = billTemplateRepository.getByHostelId(hostelId);
        if (billTemplates != null) {
            BillTemplateType billTemplateType = billTemplates
                    .getTemplateTypes()
                    .stream()
                    .filter(i -> i.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                    .findFirst()
                    .orElse(null);
            if (billTemplateType == null) {
                return null;
            }
            String colorCode = billTemplateType.getReceiptTemplateColor();
            String colorValue = null;
            String hexColor = null;

            if (colorCode.startsWith("rgba")) {
                colorValue = colorCode.substring(5, colorCode.length()-1);
            }
            else if (colorCode.startsWith("rgb(")){
                colorValue = colorCode.substring(4, colorCode.length()-1);
            }
            else if (colorCode.startsWith("#")) {
                hexColor = colorCode;
            }
            if (colorValue != null) {
                String[] colorArr = colorValue.replaceAll(" ", "")
                        .split(",");

                int r = Integer.parseInt(colorArr[0].trim());
                int g = Integer.parseInt(colorArr[1].trim());
                int b = Integer.parseInt(colorArr[2].trim());

                hexColor = String.format("#%02X%02X%02X", r, g, b);
            }


            String hostelLogo = null;
            String hostelPhone = null;
            String hostelEmailId = null;
            String termsAndCondition = null;
            String signatureUrl = null;
            String notes = null;
            String qrCode = null;

            if (billTemplates.isLogoCustomized()) {
                hostelLogo = billTemplateType.getReceiptLogoUrl();
            } else {
                hostelLogo = billTemplates.getHostelLogo();
            }

            if (hostelLogo == null) {
                hostelLogo = "https://smartstaydevs.s3.ap-south-1.amazonaws.com/smartstay/smartstay.png";
            }

            if (billTemplates.isMobileCustomized()) {
                hostelPhone = billTemplateType.getReceiptPhoneNumber();
            } else {
                hostelPhone = billTemplates.getMobile();
            }

            if (billTemplates.isEmailCustomized()) {
                hostelEmailId = billTemplateType.getReceiptMailId();
            } else {
                hostelEmailId = billTemplates.getEmailId();
            }

            if (billTemplates.isSignatureCustomized()) {
                signatureUrl = billTemplateType.getReceiptSignatureUrl();
            }
            else {
                signatureUrl = billTemplates.getDigitalSignature();
            }
            if (billTemplateType.getQrCode() != null) {
                qrCode = billTemplateType.getQrCode();
            }
            termsAndCondition = billTemplateType.getReceiptTermsAndCondition();
            notes = billTemplateType.getReceiptNotes();

            return new TemplateInfo(hexColor,
                    hostelLogo,
                    qrCode,
                    hostelPhone,
                    hostelEmailId,
                    signatureUrl,
                    termsAndCondition,
                    notes);
        }

        return null;
    }
}
