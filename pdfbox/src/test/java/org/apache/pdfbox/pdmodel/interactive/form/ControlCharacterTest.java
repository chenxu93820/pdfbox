/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.interactive.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test handling some special characters when setting a fields value.
 * 
 * Compare the results of setting the values using PDFBox with setting the values
 * via Acrobat using JavaScript and manual input.
 * 
 * The JavaScript used for acrobat is
 * 
 * <pre>
 * {@code
 * this.getField("acrobat-nul").value = "NUL\0NUL";
 * this.getField("acrobat-tab").value = "TAB\tTAB";
 * this.getField("acrobat-space").value = "SPACE SPACE";
 * this.getField("acrobat-cr").value = "CR\rCR";
 * this.getField("acrobat-lf").value = "LF\nLF";
 * this.getField("acrobat-crlf").value = "CRLF\r\nCRLF";
 * this.getField("acrobat-lfcr").value = "LFCR\n\rLFCR";
 * this.getField("acrobat-linebreak").value = "linebreak\u2028linebreak";
 * this.getField("acrobat-paragraphbreak").value = "paragraphbreak\u2029paragraphbreak";
 * }
 * </pre>
 * 
 * @see <a href="https://issues.apache.org/jira/browse/PDFBOX-3461">https://issues.apache.org/jira/browse/PDFBOX-3461</a>
 * 
 */
public class ControlCharacterTest {
    private static final File IN_DIR = new File("src/test/resources/org/apache/pdfbox/pdmodel/interactive/form");
    private static final String NAME_OF_PDF = "ControlCharacters.pdf";

    private PDDocument document;
    private PDAcroForm acroForm;

    @BeforeEach
    public void setUp() throws IOException
    {
        document = Loader.loadPDF(new File(IN_DIR, NAME_OF_PDF));
        acroForm = document.getDocumentCatalog().getAcroForm();
    }
    
    @Test
    public void characterNUL() throws IOException
    {
        assertThrows(IllegalArgumentException.class, () -> {
            acroForm.getField("pdfbox-nul").setValue("NUL\0NUL");
          });
    }

    @Test
    public void characterTAB() throws IOException
    {
    	acroForm.getField("pdfbox-tab").setValue("TAB\tTAB");
    }
    
    @Test
    public void characterSPACE() throws IOException
    {
    	PDField field = acroForm.getField("pdfbox-space");
    	field.setValue("SPACE SPACE");

    	List<String> pdfboxValues = getStringsFromStream(field);
    	List<String> acrobatValues = getStringsFromStream(acroForm.getField("acrobat-space"));

    	assertEquals(pdfboxValues, acrobatValues);
    }

    @Test
    public void characterCR() throws IOException
    {
    	PDField field = acroForm.getField("pdfbox-cr");
    	field.setValue("CR\rCR");

    	List<String> pdfboxValues = getStringsFromStream(field);
    	List<String> acrobatValues = getStringsFromStream(acroForm.getField("acrobat-cr"));

    	assertEquals(pdfboxValues, acrobatValues);
    }

    @Test
    public void characterLF() throws IOException
    {
    	PDField field = acroForm.getField("pdfbox-lf");
    	field.setValue("LF\nLF");

    	List<String> pdfboxValues = getStringsFromStream(field);
    	List<String> acrobatValues = getStringsFromStream(acroForm.getField("acrobat-lf"));

    	assertEquals(pdfboxValues, acrobatValues);
    }
    
    @Test
    public void characterCRLF() throws IOException
    {
    	PDField field = acroForm.getField("pdfbox-crlf");
    	field.setValue("CRLF\r\nCRLF");

    	List<String> pdfboxValues = getStringsFromStream(field);
    	List<String> acrobatValues = getStringsFromStream(acroForm.getField("acrobat-crlf"));

    	assertEquals(pdfboxValues, acrobatValues);
    }

    @Test
    public void characterLFCR() throws IOException
    {
    	PDField field = acroForm.getField("pdfbox-lfcr");
    	field.setValue("LFCR\n\rLFCR");
    	
    	List<String> pdfboxValues = getStringsFromStream(field);
    	List<String> acrobatValues = getStringsFromStream(acroForm.getField("acrobat-lfcr"));

    	assertEquals(pdfboxValues, acrobatValues);
    }
    
    @Test
    public void characterUnicodeLinebreak() throws IOException
    {
    	PDField field = acroForm.getField("pdfbox-linebreak");
    	field.setValue("linebreak\u2028linebreak");
    	
    	List<String> pdfboxValues = getStringsFromStream(field);
    	List<String> acrobatValues = getStringsFromStream(acroForm.getField("acrobat-linebreak"));

    	assertEquals(pdfboxValues, acrobatValues);
    }
    
    @Test
    public void characterUnicodeParagraphbreak() throws IOException
    {
    	PDField field = acroForm.getField("pdfbox-paragraphbreak");
    	field.setValue("paragraphbreak\u2029paragraphbreak");
    	
    	List<String> pdfboxValues = getStringsFromStream(field);
    	List<String> acrobatValues = getStringsFromStream(acroForm.getField("acrobat-paragraphbreak"));

    	assertEquals(pdfboxValues, acrobatValues);
    }
    
    @AfterEach
    public void tearDown() throws IOException
    {
        document.close();
    }
    
    private List<String> getStringsFromStream(PDField field) throws IOException
    {
    	PDAnnotationWidget widget = field.getWidgets().get(0);
        PDFStreamParser parser = new PDFStreamParser(
                widget.getNormalAppearanceStream());
    	
        List<Object> tokens = parser.parse();
    	
        // TODO: improve the string output to better match
        // trimming as Acrobat adds spaces to strings
        // where we don't
        return tokens.stream() //
                .filter(t -> t instanceof COSString) //
                .map(t -> ((COSString) t).getString().trim()) //
                .collect(Collectors.toList());
    }
}
