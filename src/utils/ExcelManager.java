package utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import java .io.FileOutputStream;

public class ExcelManager {
	
	private static ExcelManager excelManager = new ExcelManager();
	
	private ExcelManager(){
		
	}
	
	public static ExcelManager getInstance(){
		return excelManager;
	}
	
}
