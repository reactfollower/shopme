package com.shopme.admin.user.export;
//package com.shopme.admin.user;
//
//import java.util.List;
//
//import javax.servlet.http.HttpServletResponse;
//
//import org.Supercsv.prefs.CsvPreference;
//
//import com.shopme.common.entity.User;
//
//public class UserCsvExporter extends AbstractExporter {
//
//	  public void expoter(List<User> listUsers, HttpServletResponse response) throws IOException {
//		  super.setResponseHeader(response, ".csv", "text/csv");
//		  
//		  ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
//				  CsvPreference.STANDARD_PREFERENCE);
//		  
//		  String[] csvHeader = {"User ID", "E-mail", "Fisr Name", "Last Name", "Roles", "Enabled"};
//		  String[] fieldMapping = {"id", "email", "firstName", "lastName", "roles", "enabled"};
//		  
//		  csvWriter.writeHeader(csvHeader);
//		  
//		  for (User user : listUsers) {
//			  csvWriter.write(user, fieldMapping);
//		  }
//		  
//		  csvWriter.close();
//		
//	  }
//}
