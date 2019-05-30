package cn.edu.nchu.stu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Student {

    private String name;

    private String department;

    private String major;

    private int maxBorrowNumber;

    private int id;

    private double fine;

    private int maxBorrowPeriodDay;

    private double finePerDay;

    public static List<Student> loadFromResultSet(ResultSet resultSet) throws SQLException {
        List<Student> result = new ArrayList<Student>();
        while (resultSet.next()) {
            Student student = new Student();
            student.name = resultSet.getString("name");
            student.department = resultSet.getString("department");
            student.major = resultSet.getString("major");
            student.maxBorrowNumber = resultSet.getInt("maxBorrowNumber");
            student.id = resultSet.getInt("id");
            student.fine = resultSet.getDouble("fine");
            student.maxBorrowPeriodDay = resultSet.getInt("maxBorrowPeriodDay");
            student.finePerDay = resultSet.getDouble("finePerDay");
            result.add(student);
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public int getMaxBorrowNumber() {
        return maxBorrowNumber;
    }

    public void setMaxBorrowNumber(int maxBorrowNumber) {
        this.maxBorrowNumber = maxBorrowNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    public int getMaxBorrowPeriodDay() {
        return maxBorrowPeriodDay;
    }

    public void setMaxBorrowPeriodDay(int maxBorrowPeriodDay) {
        this.maxBorrowPeriodDay = maxBorrowPeriodDay;
    }

    public double getFinePerDay() {
        return finePerDay;
    }

    public void setFinePerDay(double finePerDay) {
        this.finePerDay = finePerDay;
    }
}
