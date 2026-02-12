package com.smartstay.reports.service;

import com.smartstay.reports.dao.BillTemplateType;
import com.smartstay.reports.dao.BillTemplates;
import com.smartstay.reports.ennum.BillConfigTypes;
import com.smartstay.reports.repositories.BillTemplateRepository;
import com.smartstay.reports.responses.invoice.TemplateInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {
    @Autowired
    private BillTemplateRepository billTemplateRepository;

    public TemplateInfo getTemplateDetails(String hostelId) {
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
            String colorCode = billTemplateType.getInvoiceTemplateColor();
            String colorValue = null;
            if (colorCode.startsWith("rgba")) {
                colorValue = colorCode.substring(5, colorCode.length()-1);
            }
            else if (colorCode.startsWith("rgb(")){
                colorValue = colorCode.substring(4, colorCode.length()-1);
            }
            String[] colorArr = colorValue.replaceAll(" ", "")
                            .split(",");

            int r = Integer.parseInt(colorArr[0].trim());
            int g = Integer.parseInt(colorArr[1].trim());
            int b = Integer.parseInt(colorArr[2].trim());

            String hexColor = String.format("#%02X%02X%02X", r, g, b);
            System.out.println(hexColor);

            String hostelLogo = null;
            String hostelPhone = null;
            String hostelEmailId = null;
            String termsAndCondition = null;
            String notes = null;

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

            termsAndCondition = billTemplateType.getInvoiceTermsAndCondition();
            notes = billTemplateType.getInvoiceNotes();

            return new TemplateInfo(hexColor,
                    hostelLogo,
                    hostelPhone,
                    hostelEmailId,
                    null,
                    termsAndCondition,
                    notes);
        }

        return null;
    }
}
