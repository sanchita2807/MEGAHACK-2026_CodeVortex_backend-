package com.smartinvoice.backend.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;
import javax.imageio.ImageIO;

@Service
public class OcrService {
    
    private final Tesseract tesseract;
    
    public OcrService() {
        this.tesseract = new Tesseract();
        String tessDataPath = System.getenv("TESSDATA_PREFIX");
        if (tessDataPath != null && !tessDataPath.isEmpty()) {
            this.tesseract.setDatapath(tessDataPath);
            System.out.println("OcrService initialized with Tesseract OCR");
        } else {
            this.tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        }
    }
    
    public String extractTextFromImage(byte[] imageData) throws IOException {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image == null) {
                throw new IOException("Failed to read image");
            }
            
            System.out.println("Image size: " + image.getWidth() + "x" + image.getHeight());
            System.out.println("Starting OCR extraction...");
            
            BufferedImage processedImage = preprocessImage(image);
            String extractedText = tesseract.doOCR(processedImage);
            
            System.out.println("OCR completed. Text length: " + extractedText.length());
            System.out.println("Extracted text:\n" + extractedText);
            
            return extractedText;
        } catch (TesseractException e) {
            System.err.println("OCR Error: " + e.getMessage());
            throw new IOException("OCR failed: " + e.getMessage(), e);
        }
    }
    
    private BufferedImage preprocessImage(BufferedImage image) {
        BufferedImage gray = convertToGrayscale(image);
        BufferedImage enhanced = enhanceContrast(gray);
        return enhanced;
    }
    
    private BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray_value = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                int gray_rgb = (gray_value << 16) | (gray_value << 8) | gray_value;
                gray.setRGB(x, y, gray_rgb);
            }
        }
        return gray;
    }
    
    private BufferedImage enhanceContrast(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        int min = 255, max = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = rgb & 0xFF;
                if (gray < min) min = gray;
                if (gray > max) max = gray;
            }
        }
        
        int range = max - min;
        if (range == 0) range = 1;
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = rgb & 0xFF;
                int stretched = (int) (((gray - min) * 255.0) / range);
                stretched = Math.max(0, Math.min(255, stretched));
                int new_rgb = (stretched << 16) | (stretched << 8) | stretched;
                result.setRGB(x, y, new_rgb);
            }
        }
        
        return result;
    }
    
    public Map<String, Object> parseInvoiceData(String extractedText) {
        Map<String, Object> invoiceData = new HashMap<>();
        
        if (extractedText == null || extractedText.trim().isEmpty()) {
            return createEmptyInvoice();
        }
        
        System.out.println("\n=== PARSING INVOICE DATA ===");
        
        String vendor = extractVendor(extractedText);
        invoiceData.put("vendor", vendor);
        
        String date = extractDate(extractedText);
        invoiceData.put("invoiceDate", date);
        
        List<Map<String, Object>> items = extractItems(extractedText);
        invoiceData.put("items", items);
        invoiceData.put("numberOfItems", items.size());
        
        Double totalAmount = extractTotalAmount(extractedText);
        invoiceData.put("totalAmount", totalAmount);
        
        Double tax = extractTax(extractedText);
        invoiceData.put("tax", tax);
        
        Double fee = extractFee(extractedText);
        invoiceData.put("fee", fee);
        
        invoiceData.put("rawText", extractedText);
        
        System.out.println("PARSED: Vendor=" + vendor + ", Items=" + items.size() + 
                         ", Total=" + totalAmount + ", Tax=" + tax + ", Fee=" + fee);
        System.out.println("=== END PARSING ===\n");
        
        return invoiceData;
    }
    
    private Map<String, Object> createEmptyInvoice() {
        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("vendor", "Unknown Vendor");
        invoiceData.put("invoiceDate", java.time.LocalDate.now().toString());
        invoiceData.put("items", new ArrayList<>());
        invoiceData.put("numberOfItems", 0);
        invoiceData.put("totalAmount", 0.0);
        invoiceData.put("tax", 0.0);
        invoiceData.put("fee", 0.0);
        invoiceData.put("rawText", "");
        return invoiceData;
    }
    
    private String extractVendor(String text) {
        String[] lines = text.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            String lower = line.toLowerCase();
            
            if (lower.contains("vendor:") || lower.contains("from:") || lower.contains("company:") || lower.contains("shop:")) {
                String vendor = line.replaceAll("(?i)(vendor:|from:|company:|shop:)", "").trim();
                if (!vendor.isEmpty() && vendor.length() > 2) {
                    System.out.println("Vendor (keyword): " + vendor);
                    return vendor;
                }
            }
        }
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && line.length() > 2 && !line.matches(".*\\d{4,}.*")) {
                System.out.println("Vendor (first line): " + line);
                return line;
            }
        }
        
        return "Unknown Vendor";
    }
    
    private String extractDate(String text) {
        Pattern datePattern = Pattern.compile("\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}");
        Matcher matcher = datePattern.matcher(text);
        if (matcher.find()) {
            String date = matcher.group();
            System.out.println("Date: " + date);
            return date;
        }
        
        String today = java.time.LocalDate.now().toString();
        System.out.println("Date (default): " + today);
        return today;
    }
    
    private List<Map<String, Object>> extractItems(String text) {
        List<Map<String, Object>> items = new ArrayList<>();
        String[] lines = text.split("\n");
        
        System.out.println("Analyzing " + lines.length + " lines for items...");
        
        int itemsStartIndex = -1;
        int itemsEndIndex = lines.length;
        
        for (int i = 0; i < lines.length; i++) {
            String lower = lines[i].toLowerCase().trim();
            
            if (itemsStartIndex == -1 && (lower.contains("item") || lower.contains("product") || 
                lower.contains("description") || lower.contains("qty") || lower.contains("price"))) {
                itemsStartIndex = i + 1;
            }
            
            if (itemsStartIndex != -1 && (lower.contains("total") || lower.contains("subtotal") || 
                lower.contains("tax") || lower.contains("amount due") || lower.contains("grand total"))) {
                itemsEndIndex = i;
                break;
            }
        }
        
        if (itemsStartIndex == -1) itemsStartIndex = 0;
        
        System.out.println("Items section: lines " + itemsStartIndex + " to " + itemsEndIndex);
        
        for (int i = itemsStartIndex; i < itemsEndIndex; i++) {
            String line = lines[i].trim();
            
            if (line.isEmpty()) continue;
            
            if (isHeaderLine(line) || isFooterLine(line)) {
                continue;
            }
            
            if (line.matches(".*\\d+.*")) {
                Map<String, Object> item = extractItemFromLine(line);
                if (item != null && !item.isEmpty() && item.containsKey("name")) {
                    String itemName = (String) item.get("name");
                    if (itemName.length() > 1 && !itemName.matches(".*\\d{4,}.*")) {
                        items.add(item);
                        System.out.println("  Item: " + item.get("name") + " | Qty: " + item.get("quantity") + " | Price: " + item.get("price"));
                    }
                }
            }
        }
        
        return items;
    }
    
    private Map<String, Object> extractItemFromLine(String line) {
        Map<String, Object> item = new HashMap<>();
        
        List<Double> numbers = new ArrayList<>();
        List<Integer> numberPositions = new ArrayList<>();
        
        Pattern numberPattern = Pattern.compile("([0-9]+(?:\\.[0-9]{1,2})?)\\s*");
        Matcher numberMatcher = numberPattern.matcher(line);
        
        while (numberMatcher.find()) {
            try {
                numbers.add(Double.parseDouble(numberMatcher.group(1)));
                numberPositions.add(numberMatcher.start());
            } catch (NumberFormatException e) {
                // Skip
            }
        }
        
        if (numbers.isEmpty()) {
            return null;
        }
        
        String itemName = line.substring(0, numberPositions.get(0)).trim();
        itemName = itemName.replaceAll("[^a-zA-Z0-9\\s&()\\-/]", "").trim();
        itemName = correctOcrErrors(itemName);
        
        if (itemName.isEmpty() || itemName.length() < 1) {
            return null;
        }
        
        item.put("name", itemName);
        
        if (numbers.size() >= 3) {
            item.put("quantity", numbers.get(0).intValue());
            item.put("price", numbers.get(numbers.size() - 1));
        } else if (numbers.size() == 2) {
            item.put("quantity", numbers.get(0).intValue());
            item.put("price", numbers.get(1));
        } else if (numbers.size() == 1) {
            item.put("quantity", 1);
            item.put("price", numbers.get(0));
        }
        
        return item;
    }
    
    private String correctOcrErrors(String text) {
        text = text.replaceAll("(?i)\\b0\\b", "O");
        text = text.replaceAll("(?i)\\bl\\b", "I");
        text = text.replaceAll("(?i)\\brn\\b", "m");
        text = text.replaceAll("(?i)\\bvv\\b", "w");
        text = text.replaceAll("(?i)\\b1\\b", "I");
        text = text.replaceAll("(?i)\\b5\\b", "S");
        text = text.replaceAll("(?i)\\b8\\b", "B");
        text = text.replaceAll("(?i)\\bll\\b", "H");
        text = text.replaceAll("(?i)\\bO0\\b", "00");
        text = text.replaceAll("(?i)\\bIl\\b", "11");
        text = text.replaceAll("(?i)\\bS5\\b", "55");
        return text;
    }
    
    private Double extractTotalAmount(String text) {
        Pattern totalPattern = Pattern.compile(
            "(?:total|grand total|net total|amount due)[:\\s]+([0-9]+\\.?[0-9]*)", 
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = totalPattern.matcher(text);
        
        if (matcher.find()) {
            try {
                Double total = Double.parseDouble(matcher.group(1));
                System.out.println("Total Amount: " + total);
                return total;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        
        List<Map<String, Object>> items = extractItems(text);
        double sum = 0;
        for (Map<String, Object> item : items) {
            Double price = (Double) item.get("price");
            Integer qty = (Integer) item.get("quantity");
            if (price != null && qty != null) {
                sum += price * qty;
            }
        }
        
        if (sum > 0) {
            System.out.println("Total Amount (calculated): " + sum);
            return sum;
        }
        
        System.out.println("Total Amount (not found): 0.0");
        return 0.0;
    }
    
    private Double extractTax(String text) {
        Pattern taxPattern = Pattern.compile(
            "(?:tax|gst|vat|sales tax)[:\\s]+([0-9]+\\.?[0-9]*)", 
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = taxPattern.matcher(text);
        
        if (matcher.find()) {
            try {
                Double tax = Double.parseDouble(matcher.group(1));
                System.out.println("Tax: " + tax);
                return tax;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        
        System.out.println("Tax: 0.0");
        return 0.0;
    }
    
    private Double extractFee(String text) {
        Pattern feePattern = Pattern.compile(
            "(?:fee|delivery|shipping|service charge|delivery charge)[:\\s]+([0-9]+\\.?[0-9]*)", 
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = feePattern.matcher(text);
        
        if (matcher.find()) {
            try {
                Double fee = Double.parseDouble(matcher.group(1));
                System.out.println("Fee: " + fee);
                return fee;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        
        System.out.println("Fee: 0.0");
        return 0.0;
    }
    
    private boolean isHeaderLine(String line) {
        String lower = line.toLowerCase();
        return lower.contains("invoice") || 
               lower.contains("vendor") || 
               lower.contains("date") ||
               lower.contains("item") ||
               lower.contains("qty") ||
               lower.contains("price") ||
               lower.contains("description") ||
               lower.contains("quantity") ||
               lower.contains("---") ||
               lower.contains("===") ||
               lower.contains("product");
    }
    
    private boolean isFooterLine(String line) {
        String lower = line.toLowerCase();
        return lower.contains("thank you") ||
               lower.contains("please") ||
               lower.contains("contact") ||
               lower.contains("phone") ||
               lower.contains("email") ||
               lower.contains("website");
    }
}
