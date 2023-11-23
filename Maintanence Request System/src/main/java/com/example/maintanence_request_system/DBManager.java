package com.example.maintanence_request_system;

import java.sql.*;
import java.time.LocalDateTime;

public class DBManager {
    final String databaseURL = "jdbc:sqlite:identifier.sqlite";
    static Connection connection;

    public boolean doesIDExist(String idString){
        try {
            int id = Integer.parseInt(idString);
            PreparedStatement ps = connection.prepareStatement("select * from tenants where id = ?;");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return true;
            }
        } catch (Exception ignored){}
        return false;
    }

    public String getRoomNo(int id){
        try{
            PreparedStatement ps = connection.prepareStatement(
                    "select aptNum from tenants where id = ?;"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.getObject(1).toString();
        } catch (Exception ignored){return "";}
    }

    public void insertTenant(String name, String phoneNum, String email, String checkInDate, String checkOutDate, String aptNum){
        try{
            PreparedStatement ps = connection.prepareStatement(
                    "insert into tenants(name, phoneNumber, email, checkInDate, checkOutDate, aptNum) values (?, ?, ?, ?, ?, ?);");
            ps.setString(1, name);
            ps.setString(2, phoneNum);
            ps.setString(3, email);
            ps.setString(4, checkInDate);
            ps.setString(5, checkOutDate);
            ps.setString(6, aptNum);
            ps.execute();
        } catch (Exception ignored){}
    }

    public void updateTenant(String id, String name, String phoneNum, String email, String checkInDate, String checkOutDate, String aptNum){
        try{
            PreparedStatement ps = connection.prepareStatement(
                    "update tenants set name = ?, phoneNumber = ?, email = ?, checkInDate = ?, checkOutDate = ?, aptNum = ?" +
                            " where id = ?;");
            ps.setString(1, name);
            ps.setString(2, phoneNum);
            ps.setString(3, email);
            ps.setString(4, checkInDate);
            ps.setString(5, checkOutDate);
            ps.setString(6, aptNum);
            ps.setString(7, id);
            ps.execute();
        } catch (Exception ignored){}
    }

    public void insertRequest(String tenantID, String location, String description, String photoURL){
        try{
            String aptNum = getRoomNo(Integer.parseInt(tenantID));
            if (aptNum.equals("")){ return; } // if the tenant id doesn't exist, don't insert the request

            PreparedStatement ps = connection.prepareStatement(
                    "insert into requests(tenantId, aptNum, location, description, time, photoURL, status)" +
                            "values (?, ?, ?, ?, current_date , ?, 'P');"
            );
            ps.setString(1, tenantID);
            ps.setString(2, aptNum);
            ps.setString(3, location);
            ps.setString(4, description);
            ps.setString(5, photoURL);
            ps.execute();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void deleteTenant(String tenantID){
        try{
            PreparedStatement ps = connection.prepareStatement("delete from tenants where id = ?;");
            ps.setInt(1, Integer.parseInt(tenantID));
            ps.execute();
        } catch (Exception ignored){}
    }

    public void closeRequest(String requestID){
        try{
            PreparedStatement ps = connection.prepareStatement(
                    "update requests set status = 'C' where id = ?;"
            );
            ps.setString(1, requestID);
            ps.execute();
        } catch (Exception e){
            System.out.println(e);
        }
    }

    public ResultSet getTenants(String id){
        try{
            PreparedStatement ps;
            if (id == null || id.equals("")){
                 ps = connection.prepareStatement("select * from tenants;");
            } else {
                ps = connection.prepareStatement("select * from tenants where id = ?;");
                ps.setString(1, id);
            }

            return ps.executeQuery();
        } catch (SQLException e){
            return null;
        }
    }

    public ResultSet getRequests(String aptField, String locationMenu, String startDateField, String endDateField, boolean statusBox, boolean pendingBox){
        try{
            if (aptField == null){ // Don't perform any special queries if loading the page initially
                PreparedStatement ps = connection.prepareStatement("select * from requests;");
                return ps.executeQuery();
            }

            String[] fields = new String[4];
            int currentField = 0;

            StringBuilder queryText = new StringBuilder();
            queryText.append("select * from requests where ");

            if (!aptField.equals("")){
                queryText.append("aptNum = ? and ");
                fields[currentField] = aptField;
                currentField++;
            }

            if (!locationMenu.equals("all")){
                queryText.append("location = ? and ");
                fields[currentField] = locationMenu;
                currentField++;
            }

            if (statusBox){
                if (pendingBox){
                    queryText.append("status = 'P' and ");
                } else {
                    queryText.append("status = 'C' and ");
                }
            }

            if (!startDateField.equals("")){
                queryText.append("time >= ? and ");
                fields[currentField] = startDateField;
                currentField++;
            }

            if (!endDateField.equals("")){
                queryText.append("time <= ?;");
                fields[currentField] = endDateField;
                currentField++;
            } else {
                queryText.append("1 = 1;"); // end the query with something that is always true to avoid syntax error w/ the final 'and'
            }

            PreparedStatement ps = connection.prepareStatement(queryText.toString());

            for (int i = 0; i < currentField; i++){
                ps.setString(i + 1, fields[i]);
            }

            return ps.executeQuery();
        } catch (SQLException e){
            return null;
        }
    }

    public void establishConnection(){
        try {
            connection = DriverManager.getConnection(databaseURL);
        } catch (SQLException e){
            System.out.println("Error, unable to connect to database.");
        }
    }

    public DBManager(){
        establishConnection();
    }
}
