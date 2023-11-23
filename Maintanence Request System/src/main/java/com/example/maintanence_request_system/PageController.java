package com.example.maintanence_request_system;

import org.springframework.expression.spel.ast.Selection;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.sql.ResultSet;
import java.sql.SQLException;

@RestController
@RequestMapping("/")
public class PageController {
    @GetMapping("/")
    public String showHomePage(){
        return readFile("src/main/resources/index.html");
    }

    @GetMapping("/newRequestPage.html")
    public String showNewRequestPage(){
        return readFile("src/main/resources/newRequestPage.html");
    }

    @GetMapping("/tenantListPage")
    public String showTenantListPage(@RequestParam(required = false) String id){
        String openingText = readFile("src/main/resources/TenantListPageFirstHalf.txt");
        String closingText = readFile("src/main/resources/TenantListPageSecondHalf.txt");
        return openingText + buildTenantTableText(id) + closingText;
    }

    public String buildTenantTableText(String inputID){
        StringBuilder tableText = new StringBuilder();
        try {
            ResultSet r = MaintenanceRequestSystemApplication.manager.getTenants(inputID);

            if (r == null || !r.next()){return "<p>No results found!</p>";}

            tableText.append("<table border=\"5px\"><tr class=\"evenrow\"><td width=\"75\">Tenant ID</td><td width=\"200\">Name</td>" +
                    "<td width=\"150\">Apartment Number</td><td width=\"200\">Email</td><td width=\"100\">Phone Number</td>" +
                    "<td  width=\"150\">Check In Date</td><td  width=\"150\">Check Out Date</td><td width=\"50\"></td></tr>");
            int i = 1;
            do{
                if (i % 2 == 0){
                    tableText.append("<tr class=\"evenrow\">");
                } else {
                    tableText.append("<tr class=\"oddrow\">");
                }

                String id = r.getObject(1).toString();
                String name = r.getObject(2).toString();
                String phoneNo = r.getObject(3).toString();
                String email = r.getObject(4).toString();
                String checkInDate = r.getObject(5).toString();
                String checkOutDate = r.getObject(6).toString();
                String aptNum = r.getObject(7).toString();

                tableText.append("<td>" + id + "</td>");
                tableText.append("<td>" + name + "</td>");
                tableText.append("<td>" + aptNum + "</td>");
                tableText.append("<td>" + email + "</td>");
                tableText.append("<td>" + phoneNo + "</td>");
                tableText.append("<td>" + checkInDate + "</td>");
                tableText.append("<td>" + checkOutDate + "</td>");

                tableText.append("<td>" +
                "<form action=\"/modifyTenantPage/" + id +"\" th:action=\"@{/modifyTenantPage/" + id + "}\" method=\"get\">" +
                        "<input type=\"submit\" value=\"Modify\"/></form></td>");

                i++;
            } while (r.next());
            tableText.append("</table>");

            return tableText.toString();
        } catch (SQLException e){
            return "<p>Error, unable to load table!</p>";
        }
    }

    @GetMapping("modifyTenantPage/{tenantID}")
    public String showModifyTenantPage(@PathVariable String tenantID){
        return showAddTenantPage(tenantID);
    }

    @GetMapping("addTenantPage")
    public String showAddTenantPage(String tenantID){
        String topText = "<!DOCTYPE html><html xmlns:th=\"https://www.thymeleaf.org\"><head><title>";
        String titleText;
        if (tenantID == null){
            titleText = "Add new Tenant";
        } else {
            titleText = "Modify Tenant";
        }
        String styleText = readFile("src/main/resources/ModifyTenantPageFirstHalf.txt");
        String bodyText = buildTenantBodyText(tenantID);
        String closingText = readFile("src/main/resources/ModifyTenantPageSecondHalf.txt");;

        return topText + titleText + styleText + titleText + bodyText + closingText;
    }

    public String buildTenantBodyText(String tenantID){
        StringBuilder s = new StringBuilder();
        s.append("</h1>");

        String name = ""; String phoneNum = ""; String email = "";
        String aptNum = ""; String checkInDate = ""; String checkOutDate = "";

        if (tenantID != null){ // if this is not null, we are modifying an existing tenant
            ResultSet rs = MaintenanceRequestSystemApplication.manager.getTenants(tenantID);
            try {
                if (!rs.next()){
                    throw new Exception();
                }
                name = rs.getObject(2).toString();
                phoneNum = rs.getObject(3).toString();
                email = rs.getObject(4).toString();
                checkInDate = rs.getObject(5).toString();
                checkOutDate = rs.getObject(6).toString();
                aptNum = rs.getObject(7).toString();

                // delete button if modifying an existing tenant
                s.append("<form action=\"/deleteTenant/");
                s.append(tenantID);
                s.append("\" th:action=\"@{/deleteTenant/");
                s.append(tenantID);
                s.append("}\" method=\"get\"><p><input type=\"submit\" value=\"Delete Tenant\"/></p></form>");
            } catch (Exception ignored){
                tenantID = null; // if an error occurs, act as if we are creating a new tenant
            }
        }

        s.append("<form action=\"/addOrModifyTenant/");
        s.append(tenantID);
        s.append("\" th:action=\"@{/addOrModifyTenant/");
        s.append(tenantID);
        s.append("}\" method=\"get\">");
        s.append("<p>Name: <label for=\"nameField\"></label>\n" +
                 "<input type=\"text\" th:field=\"*{nameField}\" id=\"nameField\" name=\"nameField\" placeholder=\"Tenant name\" value = \"");
        s.append(name);
        s.append("\"></p>");

        s.append("<p>Apartment number: <label for=\"aptField\"></label>\n" +
                "<input type=\"text\" th:field=\"*{aptField}\" id=\"aptField\" name=\"aptField\" placeholder=\"Apartment Number\" value = \"");
        s.append(aptNum);
        s.append("\"></p>");

        s.append("<p>Email: <label for=\"emailField\"></label>\n" +
                "<input type=\"text\" th:field=\"*{emailField}\" id=\"emailField\" name=\"emailField\" placeholder=\"Tenant Email\" value = \"");
        s.append(email);
        s.append("\"></p>");

        s.append("<p>Phone Number:<label for=\"phoneField\"></label>\n" +
                "<input type=\"text\" th:field=\"*{phoneField}\" id=\"phoneField\" name=\"phoneField\" placeholder=\"Tenant Phone Number\" value = \"");
        s.append(phoneNum);
        s.append("\"></p>");

        s.append("<p>Check in date: <label for=\"checkInField\"></label>\n" +
                "<input type=\"date\" th:field=\"*{checkInField}\" id=\"checkInField\" name=\"checkInField\" value = \"");
        s.append(checkInDate);
        s.append("\"></p>");

        s.append("<p>Check out date: <label for=\"checkOutField\"></label>\n" +
                "<input type=\"date\" th:field=\"*{checkOutField}\" id=\"checkOutField\" name=\"checkOutField\" value = \"");
        s.append(checkOutDate);
        s.append("\"></p>");

        if (tenantID == null){
            s.append("<p><input type=\"submit\" value=\"Add Tenant\"/></p>");
        } else {
            s.append("<p><input type=\"submit\" value=\"Commit changes\"/></p>");
        }

        s.append("</form>");

        return s.toString();
    }

    @GetMapping("addOrModifyTenant/{tenantID}")
    public String addOrModifyTenant(@PathVariable String tenantID, @RequestParam String nameField, @RequestParam String aptField,
                                    @RequestParam String phoneField, @RequestParam String emailField, @RequestParam String checkInField,
                                    @RequestParam String checkOutField){
        if (MaintenanceRequestSystemApplication.manager.doesIDExist(tenantID)){
            MaintenanceRequestSystemApplication.manager.updateTenant(tenantID, nameField, phoneField, emailField, checkInField, checkOutField, aptField);
        } else {
            MaintenanceRequestSystemApplication.manager.insertTenant(nameField, phoneField, emailField, checkInField, checkOutField, aptField);
        }

        return showTenantListPage(null);
    }

    @GetMapping("deleteTenant/{tenantID}")
    public String deleteTenant(@PathVariable String tenantID){
        MaintenanceRequestSystemApplication.manager.deleteTenant(tenantID);
        return showTenantListPage(null);
    }

    @GetMapping("requestsPage")
    public String showRequestsPage(@RequestParam(required = false) String aptField, @RequestParam(required = false) String locationMenu,
                                   @RequestParam(required = false) String startDateField, @RequestParam(required = false) String endDateField,
                                   @RequestParam(required = false) boolean statusBox, @RequestParam(required = false) boolean pendingBox){
        String openingText = readFile("src/main/resources/RequestsPageFirstHalf.txt");
        String tableText = buildRequestsTable(aptField, locationMenu, startDateField, endDateField, statusBox, pendingBox);
        String endText = readFile("src/main/resources/RequestsPageSecondHalf.txt");

        return openingText + tableText + endText;
    }

    public String buildRequestsTable(String aptField, String locationMenu, String startDateField, String endDateField, boolean statusBox, boolean pendingBox){
        try {
            ResultSet rs = MaintenanceRequestSystemApplication.manager.getRequests(aptField, locationMenu, startDateField, endDateField, statusBox, pendingBox);
            if (!rs.next()){ return "<p>No requests found!</p>"; }
            StringBuilder s = new StringBuilder();
            s.append("<table border=\"5px\"><tr class=\"evenrow\"><td width=\"75\">Request ID</td><td width=\"200\">Image</td>" +
                    "<td width=\"150\">Apartment Number</td><td width=\"100\">Location</td><td width=\"250\">Description</td>" +
                    "<td  width=\"150\">Date</td><td  width=\"150\">Status</td><td width=\"50\"></td></tr>");

            int i = 1;
            do{
                if (i % 2 == 0){
                    s.append("<tr class=\"evenrow\">");
                } else {
                    s.append("<tr class=\"oddrow\">");
                }

                String reqID = rs.getString(1);
                String apt = rs.getString(3);
                String loc = rs.getString(4);
                String desc = rs.getString(5);
                String time = rs.getString(6);
                String url = rs.getString(7);
                String statusString = rs.getString(8);

                s.append("<td>" + reqID + "</td>");

                if (url.equals("")){
                    s.append("<td>(None)</td>");
                } else {
                    s.append("<td><img src=" + url + "></td>");
                }

                s.append("<td>" + apt + "</td>");
                s.append("<td>" + convertLocationParam(loc) + "</td>");
                s.append("<td>" + desc + "</td>");
                s.append("<td>" + time + "</td>");

                s.append("<td>");
                if (statusString.equals("P")){
                    s.append("Pending</td>");
                    s.append("<td>" +
                            "<form action=\"/closeRequest/" + reqID +"\" th:action=\"@{/closeRequest/" + reqID + "}\" method=\"get\">" +
                            "<input type=\"submit\" value=\"Close\"/></form></td>");
                } else {
                    s.append("Closed</td><td></td>");
                }

                s.append("</tr>");
                i++;
            } while(rs.next());

            s.append("</table>");
            return s.toString();
        } catch (Exception e){
            return "<p>Unable to load requests!</p>";
        }
    }

    public String convertLocationParam(String loc){
        return switch (loc) {
            case "kitchen" -> "Kitchen";
            case "balcony" -> "Balcony";
            case "bedroom" -> "Bedroom";
            case "office" -> "Office";
            case "outside" -> "Outside";
            case "livingRoom" -> "Living Room";
            case "other" -> "Other";
            default -> "Unknown";
        };
    }

    @GetMapping("closeRequest/{requestID}")
    public String closeRequest(@PathVariable String requestID){
        MaintenanceRequestSystemApplication.manager.closeRequest(requestID);
        return showRequestsPage("", "all", "", "", false, false);
    }

    @GetMapping("/addRequest")
    public String addRequest(@RequestParam String idField, @RequestParam String descField,
                             @RequestParam String urlField, @RequestParam String locationMenu){

        if (MaintenanceRequestSystemApplication.manager.doesIDExist(idField)){
            MaintenanceRequestSystemApplication.manager.insertRequest(idField, locationMenu, descField, urlField);
        }

        return showHomePage();
    }

    // read all the contents of a file and return it as a single string
    public String readFile(String filePath){
        try{
            StringBuilder result = new StringBuilder();

            File inputFile = new File(filePath);
            Scanner s = new Scanner(inputFile);
            while(s.hasNextLine()){
                result.append(s.nextLine());
            }

            s.close();
            return result.toString();
        } catch (FileNotFoundException e){
            System.err.println("Error, unable to read file!");
            return "";
        }
    }
}
