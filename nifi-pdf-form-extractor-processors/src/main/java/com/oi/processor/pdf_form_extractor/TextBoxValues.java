package com.oi.processor.pdf_form_extractor;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.Item;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;

public class TextBoxValues {
	public static final String SRC = "E:/open-insights/Hermes/NiFi/PDF/pdfForm/data/OoPdfFormExample1.pdf";
    
    public static void main(String[] args) throws IOException {
    	TextBoxValues app = new TextBoxValues();
        app.getTextBoxValues(SRC);
    }
 
    public String getTextBoxValues(String src) throws IOException {
        PdfReader reader = new PdfReader(SRC);
        AcroFields fields = reader.getAcroFields();
        Map<String, Item> values = fields.getFields();
        Set<String> st = values.keySet();
        String output="";
        for(Object s: st) {
        	String key = s.toString();
        	//System.out.println("\n"+key);
        	
        	Item it=(Item)values.get(key);
        	 
            PdfDictionary dic = it.getMerged(0);
            if(dic.get(PdfName.V)!=null) {
            	//System.out.println(dic.get(PdfName.V).toString());
            	output=output+dic.get(PdfName.V).toString()+",";
            }
            
            
            PdfObject v = PdfReader.getPdfObjectRelease(dic.get(PdfName.V));
           
            if (v == null)
                continue;
            PdfObject ft = PdfReader.getPdfObjectRelease(dic.get(PdfName.FT));
            if (ft == null || PdfName.SIG.equals(ft))
                continue;
             
        	
        }        
        System.out.println("helo"+output);
		return output;
    }
}
