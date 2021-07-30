package com.example.demo;



import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.RecordNotFoundException;
import com.example.demo.util.Utils;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalAlignEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;


public class ReportUtil {

    /**
     *
     * @param objectVos  list of object that is pre defined in Template @templateLocation
     * @param parameterMap Generic value that is perdefined in Template @templateLocation
     * @param templateLocation JRXml template
     * @return
     * @throws JRException
     * @throws IOException
     * @throws URISyntaxException
     * @throws RecordNotFoundException
     */
    synchronized public static JasperPrint generateReport(List objectVos, Map<String, Object> parameterMap, String templateLocation) throws JRException, IOException, URISyntaxException, RecordNotFoundException {

        if (Utils.isNullOrEmpty(templateLocation))
            throw new RecordNotFoundException("Report Template Not Found");
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(objectVos);

        if(parameterMap.containsKey("left_margin") && parameterMap.containsKey("right_margin")){
            Float total = Float.parseFloat(parameterMap.get("left_margin").toString()) + Float.parseFloat(parameterMap.get("right_margin").toString());
            if(total > 11)
                throw new BadRequestException("Total Left and Right Margin Should be Less than 12");
        }

        if(parameterMap.containsKey("top_margin") && parameterMap.containsKey("bottom_margin")){
            Float total = Float.parseFloat(parameterMap.get("top_margin").toString()) + Float.parseFloat(parameterMap.get("bottom_margin").toString());
            if(total > 13)
                throw new BadRequestException("Total Top and Bottom Margin Should be Less than 13");
        }

        JasperDesign design = loadDesign(templateLocation);
        int leftMargin = 20;
        int rightMargin = 20;
        int topMargin = 20;
        int bottomMargin = 20;
        if(parameterMap.containsKey("left_margin") && parameterMap.containsKey("right_margin")){ // this is to check if the form is thankyou letter
            int toCm = 48;
            if(!parameterMap.get("left_margin").toString().equals("0.0"))
                leftMargin = Math.round((Float) parameterMap.get("left_margin") * toCm);
            if(!parameterMap.get("right_margin").toString().equals("0.0"))
                rightMargin = Math.round((Float) parameterMap.get("right_margin") * toCm);

            if(leftMargin < 20)
                leftMargin = 20;
            if(rightMargin < 20)
                rightMargin = 20;

            int newPageWidth = design.getPageWidth() - leftMargin - rightMargin;
            design.setColumnWidth(newPageWidth);
            if(leftMargin != 0)
                //design.setLeftMargin(leftMargin);
                if(rightMargin != 0)
                    //design.setRightMargin(rightMargin);
                    if(!parameterMap.get("left_margin").toString().equals("0.0") || !parameterMap.get("right_margin").toString().equals("0.0")) {
                        int oldPageWidth = design.getPageWidth();
                        int elementWidth;
                        int newElementWidth;
                        int newX;
                        for(int i=0 ;i< design.getDetailSection().getBands().length ;i++){
                            for(int j=0;j<design.getDetailSection().getBands()[i].getElements().length;j++){
                                elementWidth = design.getDetailSection().getBands()[i].getElements()[j].getWidth();
                                if(design.getDetailSection().getBands()[i].getElements()[j].getWidth()==572)
                                    design.getDetailSection().getBands()[i].getElements()[j].setWidth(design.getDetailSection().getBands()[i].getElements()[j].getWidth() - leftMargin - rightMargin);
                                else
                                    design.getDetailSection().getBands()[i].getElements()[j].setWidth((newPageWidth * design.getDetailSection().getBands()[i].getElements()[j].getWidth())/oldPageWidth);
                                if(i!=0 && i!=1 && i!=2)
                                    design.getDetailSection().getBands()[i].getElements()[j].setX(design.getDetailSection().getBands()[i].getElements()[j].getX()+leftMargin);
                                else {
                                    newElementWidth = (newPageWidth * elementWidth)/oldPageWidth;
                                    newX = leftMargin + (newPageWidth/2) - newElementWidth/2;
                                    design.getDetailSection().getBands()[i].getElements()[j].setX(newX-20);

                                }
                                //design.getDetailSection().getBands()[i].getElements()[j].setX((newPageWidth * design.getDetailSection().getBands()[i].getElements()[j].getX())/oldPageWidth);
                            }
                        }
                    }
            int footerLeftMargin = 0;
            int footerRightMargin = 0;
            if(parameterMap.containsKey("footer_left_margin") && parameterMap.containsKey("footer_right_margin")){
                if(!parameterMap.get("footer_left_margin").toString().equals("0.0"))
                    footerLeftMargin = Math.round((Float) parameterMap.get("footer_left_margin") * toCm);
                if(!parameterMap.get("footer_right_margin").toString().equals("0.0"))
                    footerRightMargin = Math.round((Float) parameterMap.get("footer_right_margin") * toCm);
                JRDesignBand footerBand = (JRDesignBand) design.getPageFooter();
                footerBand.getElements()[0].setX(footerBand.getElements()[0].getX() + leftMargin + footerLeftMargin);
                footerBand.getElements()[0].setWidth(footerBand.getElements()[0].getWidth() - leftMargin - rightMargin - footerLeftMargin - footerRightMargin);
            }
        }

        if(parameterMap.get("footer_alignment") != null){
            JRDesignBand footerBand = (JRDesignBand) design.getPageFooter();
            if(parameterMap.get("footer_alignment").equals("center"))
                ((JRDesignTextField)footerBand.getElements()[0]).setHorizontalAlignment(HorizontalAlignEnum.CENTER);
            else if(parameterMap.get("footer_alignment").equals("right"))
                ((JRDesignTextField)footerBand.getElements()[0]).setHorizontalAlignment(HorizontalAlignEnum.RIGHT);
            else if(parameterMap.get("footer_alignment").equals("left"))
                ((JRDesignTextField)footerBand.getElements()[0]).setHorizontalAlignment(HorizontalAlignEnum.LEFT);
            else if(parameterMap.get("footer_alignment").equals("justify"))
                ((JRDesignTextField)footerBand.getElements()[0]).setHorizontalAlignment(HorizontalAlignEnum.JUSTIFIED);

        }

        if(parameterMap.containsKey("top_margin") && parameterMap.containsKey("bottom_margin")){ // this is to check if the form is thankyou letter
            int toCm = 48;
            if(!parameterMap.get("top_margin").toString().equals("0.0"))
                topMargin = Math.round((Float) parameterMap.get("top_margin") * toCm);
            if(!parameterMap.get("bottom_margin").toString().equals("0.0"))
                bottomMargin = Math.round((Float) parameterMap.get("bottom_margin") * toCm);
            if(topMargin!=0 || bottomMargin!=0) {
                JRDesignBand headerBand = (JRDesignBand) design.getPageHeader();
                headerBand.setHeight(topMargin);
                JRDesignBand footerBand = (JRDesignBand) design.getPageFooter();
                footerBand.setHeight(20+bottomMargin);
                //design.setTopMargin(topMargin);
                //design.setBottomMargin(bottomMargin);
            }
        }

        /*if(parameterMap.containsKey("watermark_path") && parameterMap.get("watermark_path") != null){
            JRDesignBand band = new JRDesignBand();
            band.setHeight(design.getPageHeight()-40);
            JRDesignExpression expression = new JRDesignExpression();
            expression.setText("\"" + parameterMap.get("watermark_path").toString() + "\"");
            JasperDesign jasperDesign = new JasperDesign();
            JRDesignImage image = new JRDesignImage(jasperDesign);
            image.setX(0);
            image.setY(0);
            image.setWidth(design.getPageWidth() - leftMargin - rightMargin);
            image.setHeight(design.getPageHeight()-40);
            //image.setStretchType(StretchTypeEnum.RELATIVE_TO_BAND_HEIGHT);
            image.setHorizontalAlignment(HorizontalAlignEnum.CENTER);
            image.setVerticalAlignment(VerticalAlignEnum.MIDDLE);
            image.setScaleImage(ScaleImageEnum.REAL_HEIGHT);
            image.setExpression(expression);
            image.setOnErrorType(OnErrorTypeEnum.BLANK);
            band.addElement(image);
            design.setBackground(band);
        }*/
        return fillReport(compileDesign(design), parameterMap, dataSource);
    }

    private static JasperDesign loadDesign(String fileName) throws JRException, IOException, RecordNotFoundException {
        JasperDesign jasperDesign;
        try{
            jasperDesign = JRXmlLoader.load(fileName);
        } catch(Exception e){
            throw new RecordNotFoundException("Design not found.");
        }
        return jasperDesign;
    }

    private static JasperReport compileDesign(JasperDesign jasperDesign) throws JRException {
        return JasperCompileManager.compileReport(jasperDesign);
    }

    private static JasperPrint fillReport(JasperReport jasperReport, Map<String, Object> parameterMap, JRBeanCollectionDataSource dataSource) throws JRException {
        return JasperFillManager.fillReport(jasperReport, parameterMap, dataSource);
    }

}

