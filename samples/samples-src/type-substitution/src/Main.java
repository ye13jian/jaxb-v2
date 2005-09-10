/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
 
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// import java content classes generated by binding compiler
import com.example.ipo.*;

/*
 * $Id: Main.java,v 1.2 2005-09-10 19:08:17 kohsuke Exp $
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
 
public class Main {
    
    // This sample application demonstrates type substitution using
    // using schema example at http://www.w3.org/TR/xmlschema-0/#UseDerivInInstDocs.
    
    public static void main( String[] args ) {
        try {
            // create a JAXBContext capable of handling classes generated into
            // the com.example.ipo package.
            JAXBContext jc = JAXBContext.newInstance( "com.example.ipo" );
            
            // create an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();
            
            // unmarshal a po instance document into a tree of Java content
            // objects composed of classes from the "com.example.ipo" package
            JAXBElement<PurchaseOrderType>  poe = 
                (JAXBElement<PurchaseOrderType>)u.unmarshal( new FileInputStream( "ipo.xml" ) );
	    PurchaseOrderType po = poe.getValue();

            // create a Marshaller and marshal to a file
	    System.out.println("Original Purchase Order");
            Marshaller m = jc.createMarshaller();
            m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            m.marshal( poe, System.out );
	    System.out.println("******************************************************");

            // Process a return. Reverse purchase order addresses.
            Address billToAddress = po.getBillTo();
            Address shipToAddress = po.getShipTo();
	    po.setBillTo(shipToAddress);
	    po.setShipTo(billToAddress);

	    System.out.println("Return Merchandise. Ship and Bill address reversed.");
            m.marshal( poe, System.out );
	    System.out.println("******************************************************");

	    /*********************************************************************/
	    // Illustrate setting a type substitution on a javax.xml.bind.Element instance.

            USTaxExemptPurchaseOrderType uspo = 
		new ObjectFactory().createUSTaxExemptPurchaseOrderType();
	    uspo.setShipTo(billToAddress);
	    uspo.setBillTo(billToAddress);
            uspo.setTaxExemptId("charity007");
            uspo.setOrderDate(po.getOrderDate());
            uspo.setComment(po.getComment());

	    Items items = new ObjectFactory().createItems();
	    items.getItem().addAll(po.getItems().getItem());
	    uspo.setItems(items);
	    
            //PurchaseOrder element type in schema is "PurchaseOrderType".
            //Set it to an instance of type "USTaxExemptPurchaseOrderType" that
            //extends (derives using XML terminology) from "PurchaseOrderType".
	    poe.setValue(uspo);
	    System.out.println("Tax Exempt Purchase Order composed within Application.");
            m.marshal( poe, System.out );
	    System.out.println("******************************************************");

	    /*********************************************************************/
	    // Unmarshal and manipulate a global element that has a document specifed 
            // type substitution. (@xsi:type specified on element in instance document.)

            // unmarshal an instance document that identifies derived type 
	    // "ipo:USTaxExemptPurchaseOrder" for global root element <ipo:purchaseOrder>. 
            poe = (JAXBElement<PurchaseOrderType>)u.unmarshal( new FileInputStream( "ustaxexemptpo.xml" ) );

            // Access data added to element <ipo:purchaseOrder> via type substitution.
            // All data added by derivation by extension from the element's original
            // type specified in the schema must be accessed through this unwrapping
            // of the element.
            PurchaseOrderType pot = poe.getValue();
            if (poe.isTypeSubstituted() && 
		pot instanceof USTaxExemptPurchaseOrderType) {
		USTaxExemptPurchaseOrderType taxexemptpo = (USTaxExemptPurchaseOrderType)pot;
		System.out.println("US Tax exempt id: " + taxexemptpo.getTaxExemptId());
	    }

            // create a Marshaller and marshal to a file
	    System.out.println("Tax Exempt Purchase Order");
            m.marshal( poe, System.out );
	    System.out.println("******************************************************");

            
        } catch( JAXBException je ) {
            je.printStackTrace();
        } catch( IOException ioe ) {
            ioe.printStackTrace();
        } catch(Exception e) {
	    e.printStackTrace();
	}
    }
}
