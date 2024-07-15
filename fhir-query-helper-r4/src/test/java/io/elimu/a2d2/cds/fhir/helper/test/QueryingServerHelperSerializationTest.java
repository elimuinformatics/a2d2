package io.elimu.a2d2.cds.fhir.helper.test;

import org.junit.Assert;
import org.junit.Test;

import com.mongodb.MongoClientSettings;

import java.io.StringWriter;

import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.json.JsonWriter;

import io.elimu.a2d2.cds.fhir.helper.r4.QueryingServerHelper;

public class QueryingServerHelperSerializationTest {

    @Test
    public void testSerialization() {
        QueryingServerHelper qsh = new QueryingServerHelper("http://fake-fhir-url.elimuinformatics.com/r4");
        qsh.addHeader("Authorization", "Bearer asdf.asdf.asdf");
        DocumentCodec codec = new DocumentCodec(CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())));
        Document doc = new Document();
        Document dataDoc = new Document();
        dataDoc.put("variableValue", qsh);
        doc.put("data", dataDoc);
        StringWriter writer = new StringWriter();
        codec.encode(new JsonWriter(writer), doc, EncoderContext.builder().build());
        Assert.assertNotNull(writer.toString());
        Assert.assertNotEquals("", writer.toString());
    }
}
