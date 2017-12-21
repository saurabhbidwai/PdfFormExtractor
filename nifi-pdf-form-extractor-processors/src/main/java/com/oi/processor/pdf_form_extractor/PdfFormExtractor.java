package com.oi.processor.pdf_form_extractor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PRAcroForm.FieldInformation;

@Tags({"pdf","extract","values","pdfform","json"})
@CapabilityDescription("Extract values from PDF Form")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})

public class PdfFormExtractor extends AbstractProcessor {

    public static final Relationship SUCCESS  = new Relationship.Builder()
            .name("success")
            .description("Any FlowFile that successfully has data extracted will be routed to success")
            .build();
    
    public static final Relationship FAILURE = new Relationship.Builder()
            .name("failure")
            .description("Any FlowFile that fails to have data extracted will be routed to failure")
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(SUCCESS);
        relationships.add(FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
    	
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
    	final AtomicReference<String> value = new AtomicReference<>();

    	FlowFile flowfile = session.get();
    	session.read(flowfile, new InputStreamCallback() {
            @Override
            public void process(InputStream in) throws IOException {
                try{
                	String result = getFieldsAsJson(in);
                    value.set(result);
                }catch(Exception ex){
                    ex.printStackTrace();
                    getLogger().error("Failed to read flowfile");
                }
            }
        });
    	
    	String results = value.get();
        if(results != null && !results.isEmpty()){
            flowfile = session.putAttribute(flowfile, "match", results);
        }

        // To write the results back out to flow file
        flowfile = session.write(flowfile, new OutputStreamCallback() {

            @Override
			public void process(java.io.OutputStream out) throws IOException {
				out.write(value.get().getBytes(StandardCharsets.UTF_8));
			}
        });

        session.transfer(flowfile, SUCCESS);
}
    	
public String getFieldsAsJson(InputStream in){
	PdfReader reader = null;
	try {
		reader = new PdfReader(in);
	} catch (IOException e) {
		e.printStackTrace();
	}
	TreeMap<String, String> valuesMap = new TreeMap<>();
	GsonBuilder builder = new GsonBuilder();
	//builder.disableHtmlEscaping();
	Gson gsonObj = builder.create();
	for (FieldInformation field : reader.getAcroForm().getFields()) {
		PdfObject flInfo = field.getInfo().get(PdfName.V);
		if (flInfo != null) {
			if (flInfo.isString()) {
				valuesMap.put(field.getName(), field.getInfo().getAsString(PdfName.V).toUnicodeString());
			} else
				valuesMap.put(field.getName(), flInfo.toString());
		}
	}
	//System.out.println(gsonObj.toJson(valuesMap));
	return  gsonObj.toJson(valuesMap);
}}
