package cn.edu.nchu.stu;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecord {

    private int id;

    private int studentId;

    private int bookId;

    private Date beginDate;

    private boolean returned;

    public static List<BorrowRecord> loadFromResultSet(ResultSet resultSet) throws SQLException {
        List<BorrowRecord> result = new ArrayList<BorrowRecord>();
        while (resultSet.next()) {
            BorrowRecord record = new BorrowRecord();
            record.id = resultSet.getInt("id");
            record.studentId = resultSet.getInt("studentId");
            record.bookId = resultSet.getInt("bookId");
            record.beginDate = resultSet.getDate("beginDate");
            record.returned = resultSet.getBoolean("returned");
            result.add(record);
        }
        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }
}
